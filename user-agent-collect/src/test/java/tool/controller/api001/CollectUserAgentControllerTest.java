package tool.controller.api001;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import test.base.BaseUT;
import tool.common.db.mysql.dto.MysqlDbUserAgentInfDto;
import tool.common.db.postgresql.dto.PostgresqlDbUserAgentInfDto;
import tool.common.exception.OnlineBLogicException;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@SpringBootTest(classes = { CollectUserAgentTestConfiguration.class })
class CollectUserAgentControllerTest extends BaseUT {

    @Autowired
    private CollectUserAgentController target;

    @MockitoBean
    private CollectUserAgentDbAccessFunction collectUserAgentDbAccessFunction;

    @Test
    void test0001() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = null;
        String secChUaMobile = "?0";
        String secChUaModel = "\"\"";
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = "\"10.0.0\"";
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
        verifyNoInteractions(this.collectUserAgentDbAccessFunction);
    }

    @Test
    void test0002() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = null;
        String secChUaModel = "\"\"";
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = "\"10.0.0\"";
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
        verifyNoInteractions(this.collectUserAgentDbAccessFunction);
    }

    @Test
    void test0003() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = "?0";
        String secChUaModel = null;
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = "\"10.0.0\"";
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
        verifyNoInteractions(this.collectUserAgentDbAccessFunction);
    }

    @Test
    void test0004() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = "?0";
        String secChUaModel = "\"\"";
        String secChUaPlatform = null;
        String secChUaPlatformVersion = "\"10.0.0\"";
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
        verifyNoInteractions(this.collectUserAgentDbAccessFunction);
    }

    @Test
    void test0005() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = "?0";
        String secChUaModel = "\"\"";
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = null;
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
        verifyNoInteractions(this.collectUserAgentDbAccessFunction);
    }

    @Test
    void test0006() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = "?0";
        String secChUaModel = "\"\"";
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = "\"10.0.0\"";

        doReturn(null).when(this.collectUserAgentDbAccessFunction).selectPostgresqlDbUserAgentInfByUserAgent(any());
        doReturn(null).when(this.collectUserAgentDbAccessFunction).selectMysqlDbUserAgentInfByUserAgent(any());
        doNothing().when(this.collectUserAgentDbAccessFunction).registDb(any());

        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("0000", result.getBody().getDetailCode());
        verify(this.collectUserAgentDbAccessFunction).selectPostgresqlDbUserAgentInfByUserAgent(userAgent);
        verify(this.collectUserAgentDbAccessFunction).selectMysqlDbUserAgentInfByUserAgent(userAgent);
        verify(this.collectUserAgentDbAccessFunction).registDb(userAgent);
        verifyNoMoreInteractions(this.collectUserAgentDbAccessFunction);
    }

    @Test
    void test0007() {
        registeredRecords(7, 7);

        ResponseEntity<CollectUserAgentResponseBody> result = completeRequest();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("0001", result.getBody().getDetailCode());
        verifyReadsWithoutRegistration();
    }

    @Test
    void test0008() {
        registeredRecords(7, 8);

        OnlineBLogicException exception = assertThrows(OnlineBLogicException.class, this::completeRequest);

        assertEquals("E_API001_0001", exception.getMessageId());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertArrayEquals(new Object[] { 7, 8 }, exception.getMessageBindParams());
        verifyReadsWithoutRegistration();
    }

    @Test
    void test0009() {
        registeredRecords(7, null);

        OnlineBLogicException exception = assertThrows(OnlineBLogicException.class, this::completeRequest);

        assertEquals("E_API001_0002", exception.getMessageId());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertArrayEquals(new Object[] { 7 }, exception.getMessageBindParams());
        verifyReadsWithoutRegistration();
    }

    @Test
    void test0010() {
        registeredRecords(null, 8);

        OnlineBLogicException exception = assertThrows(OnlineBLogicException.class, this::completeRequest);

        assertEquals("E_API001_0003", exception.getMessageId());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertArrayEquals(new Object[] { 8 }, exception.getMessageBindParams());
        verifyReadsWithoutRegistration();
    }

    private ResponseEntity<CollectUserAgentResponseBody> completeRequest() {
        return this.target.exec("baseline-user-agent", "\"Chromium\";v=\"103\"", "?0", "\"\"",
                "\"Windows\"", "\"10.0.0\"");
    }

    private void registeredRecords(Integer postgresqlSequence, Integer mysqlSequence) {
        PostgresqlDbUserAgentInfDto postgresql = null;
        if (postgresqlSequence != null) {
            postgresql = new PostgresqlDbUserAgentInfDto();
            postgresql.setSequenceNum(postgresqlSequence);
            postgresql.setUserAgent("baseline-user-agent");
        }
        MysqlDbUserAgentInfDto mysql = null;
        if (mysqlSequence != null) {
            mysql = new MysqlDbUserAgentInfDto();
            mysql.setSequenceNum(mysqlSequence);
            mysql.setUserAgent("baseline-user-agent");
        }
        doReturn(postgresql).when(this.collectUserAgentDbAccessFunction)
                .selectPostgresqlDbUserAgentInfByUserAgent("baseline-user-agent");
        doReturn(mysql).when(this.collectUserAgentDbAccessFunction)
                .selectMysqlDbUserAgentInfByUserAgent("baseline-user-agent");
    }

    private void verifyReadsWithoutRegistration() {
        verify(this.collectUserAgentDbAccessFunction).selectPostgresqlDbUserAgentInfByUserAgent("baseline-user-agent");
        verify(this.collectUserAgentDbAccessFunction).selectMysqlDbUserAgentInfByUserAgent("baseline-user-agent");
        verify(this.collectUserAgentDbAccessFunction, never()).registDb(any());
        verifyNoMoreInteractions(this.collectUserAgentDbAccessFunction);
    }
}
