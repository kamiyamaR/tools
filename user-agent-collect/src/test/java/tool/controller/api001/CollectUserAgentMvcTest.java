package tool.controller.api001;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import test.base.BaseUT;
import tool.common.db.mysql.dto.MysqlDbUserAgentInfDto;
import tool.common.db.postgresql.dto.PostgresqlDbUserAgentInfDto;

@SpringBootTest(classes = { CollectUserAgentTestConfiguration.class })
@AutoConfigureMockMvc
class CollectUserAgentMvcTest extends BaseUT {

    private static final String USER_AGENT = "baseline-user-agent";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CollectUserAgentDbAccessFunction collectUserAgentDbAccessFunction;

    @Test
    void rejectsMissingUserAgentBeforeAccessingDatabase() throws Exception {
        var result = this.mvc.perform(get("/api001")).andExpect(status().isBadRequest())
                .andExpect(content().string("")).andReturn();

        var exception = assertInstanceOf(MissingRequestHeaderException.class, result.getResolvedException());
        assertEquals("User-Agent", exception.getHeaderName());
        verifyNoInteractions(this.collectUserAgentDbAccessFunction);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void returnsJsonForNewAndRegisteredUserAgent(boolean registered) throws Exception {
        registeredRecords(registered ? 7 : null, registered ? 7 : null);

        this.mvc.perform(completeRequest()).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"detail_code\":\"" + (registered ? "0001" : "0000") + "\"}", JsonCompareMode.STRICT));

        verifyReads();
        if (registered) {
            verify(this.collectUserAgentDbAccessFunction, never()).registDb(any());
        } else {
            verify(this.collectUserAgentDbAccessFunction).registDb(USER_AGENT);
        }
        verifyNoMoreInteractions(this.collectUserAgentDbAccessFunction);
    }

    @ParameterizedTest
    @CsvSource({ "7,8", "7,", ",8" })
    void returnsBadRequestForInconsistentRecords(Integer postgresqlSequence, Integer mysqlSequence) throws Exception {
        registeredRecords(postgresqlSequence, mysqlSequence);

        this.mvc.perform(completeRequest()).andExpect(status().isBadRequest()).andExpect(content().string(""));

        verifyReads();
        verify(this.collectUserAgentDbAccessFunction, never()).registDb(any());
        verifyNoMoreInteractions(this.collectUserAgentDbAccessFunction);
    }

    @ParameterizedTest
    @ValueSource(strings = { "postgresql", "mysql", "registration" })
    void returnsInternalServerErrorAndStopsProcessingOnDbFailure(String failureStage) throws Exception {
        RuntimeException failure = new IllegalStateException("simulated DB failure: " + failureStage);
        switch (failureStage) {
        case "postgresql":
            doThrow(failure).when(this.collectUserAgentDbAccessFunction)
                    .selectPostgresqlDbUserAgentInfByUserAgent(USER_AGENT);
            break;
        case "mysql":
            doThrow(failure).when(this.collectUserAgentDbAccessFunction)
                    .selectMysqlDbUserAgentInfByUserAgent(USER_AGENT);
            break;
        case "registration":
            doThrow(failure).when(this.collectUserAgentDbAccessFunction).registDb(USER_AGENT);
            break;
        default:
            throw new IllegalArgumentException(failureStage);
        }

        this.mvc.perform(completeRequest()).andExpect(status().isInternalServerError())
                .andExpect(content().string(""))
                .andExpect(result -> assertSame(failure, result.getResolvedException()));

        verify(this.collectUserAgentDbAccessFunction).selectPostgresqlDbUserAgentInfByUserAgent(USER_AGENT);
        if ("postgresql".equals(failureStage)) {
            verify(this.collectUserAgentDbAccessFunction, never()).selectMysqlDbUserAgentInfByUserAgent(any());
        } else {
            verify(this.collectUserAgentDbAccessFunction).selectMysqlDbUserAgentInfByUserAgent(USER_AGENT);
        }
        if ("registration".equals(failureStage)) {
            verify(this.collectUserAgentDbAccessFunction).registDb(USER_AGENT);
        } else {
            verify(this.collectUserAgentDbAccessFunction, never()).registDb(any());
        }
        verifyNoMoreInteractions(this.collectUserAgentDbAccessFunction);
    }

    private MockHttpServletRequestBuilder completeRequest() {
        return get("/api001").header("User-Agent", USER_AGENT)
                .header("Sec-CH-UA", "\"Chromium\";v=\"103\"")
                .header("Sec-CH-UA-Mobile", "?0").header("Sec-CH-UA-Model", "\"\"")
                .header("Sec-CH-UA-Platform", "\"Windows\"").header("Sec-CH-UA-Platform-Version", "\"10.0.0\"");
    }

    private void registeredRecords(Integer postgresqlSequence, Integer mysqlSequence) {
        PostgresqlDbUserAgentInfDto postgresql = null;
        if (postgresqlSequence != null) {
            postgresql = new PostgresqlDbUserAgentInfDto();
            postgresql.setSequenceNum(postgresqlSequence);
            postgresql.setUserAgent(USER_AGENT);
        }
        MysqlDbUserAgentInfDto mysql = null;
        if (mysqlSequence != null) {
            mysql = new MysqlDbUserAgentInfDto();
            mysql.setSequenceNum(mysqlSequence);
            mysql.setUserAgent(USER_AGENT);
        }
        doReturn(postgresql).when(this.collectUserAgentDbAccessFunction)
                .selectPostgresqlDbUserAgentInfByUserAgent(USER_AGENT);
        doReturn(mysql).when(this.collectUserAgentDbAccessFunction).selectMysqlDbUserAgentInfByUserAgent(USER_AGENT);
    }

    private void verifyReads() {
        verify(this.collectUserAgentDbAccessFunction).selectPostgresqlDbUserAgentInfByUserAgent(USER_AGENT);
        verify(this.collectUserAgentDbAccessFunction).selectMysqlDbUserAgentInfByUserAgent(USER_AGENT);
    }
}
