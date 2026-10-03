package tool.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import tool.Main;
import tool.controller.api001.CollectUserAgentDbAccessFunction;
import tool.common.exception.OnlineBLogicException;
import tool.common.db.mysql.dto.MysqlDbUserAgentInfDto;
import tool.common.db.postgresql.dto.PostgresqlDbUserAgentInfDto;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:db-test/application-db-test.properties")
@Import(CollectUserAgentDbIT.InsertFailureConfiguration.class)
class CollectUserAgentDbIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CollectUserAgentDbAccessFunction dbAccess;

    @Autowired
    private FailAfterMysqlInsertInterceptor insertFailure;

    @Autowired
    @Qualifier("mysqlDbDataSource")
    private DriverManagerDataSource mysqlDataSource;

    @Autowired
    @Qualifier("postgresqlDbDataSource")
    private DriverManagerDataSource postgresDataSource;

    private JdbcTemplate mysql;
    private JdbcTemplate postgres;
    private String userAgent;
    private String blockingUserAgent;

    @BeforeEach
    void prepareDedicatedConnections() {
        assertEquals("jdbc:mysql://127.0.0.1:13306/uac_integration?allowPublicKeyRetrieval=true&useSSL=false&characterEncoding=UTF-8",
                this.mysqlDataSource.getUrl());
        assertEquals("jdbc:postgresql://127.0.0.1:15432/uac_integration", this.postgresDataSource.getUrl());
        assertEquals("uac_test", this.mysqlDataSource.getUsername());
        assertEquals("uac_test", this.postgresDataSource.getUsername());
        this.mysql = new JdbcTemplate(this.mysqlDataSource);
        this.postgres = new JdbcTemplate(this.postgresDataSource);
        this.userAgent = "integration-日本語-" + UUID.randomUUID();
    }

    @AfterEach
    void removeOnlyThisTestsRows() {
        this.insertFailure.targetUserAgent = null;
        this.insertFailure.failBeforePostgresCommit = false;
        this.insertFailure.commitFailureArmed = false;
        if (this.userAgent != null) {
            this.mysql.update("DELETE FROM user_agent_inf WHERE user_agent IN (?, ?)", this.userAgent, this.blockingUserAgent);
            this.postgres.update("DELETE FROM user_agent_inf WHERE user_agent IN (?, ?)", this.userAgent, this.blockingUserAgent);
        }
    }

    @Test
    void registersAndRetrievesSameUserAgentWithoutDuplicateRows() throws Exception {
        assertNull(this.dbAccess.selectMysqlDbUserAgentInfByUserAgent(this.userAgent));
        assertNull(this.dbAccess.selectPostgresqlDbUserAgentInfByUserAgent(this.userAgent));

        this.mvc.perform(completeRequest()).andExpect(status().isOk())
                .andExpect(content().json("{\"detail_code\":\"0000\"}", JsonCompareMode.STRICT));

        var mysqlRecord = this.dbAccess.selectMysqlDbUserAgentInfByUserAgent(this.userAgent);
        var postgresRecord = this.dbAccess.selectPostgresqlDbUserAgentInfByUserAgent(this.userAgent);
        assertNotNull(mysqlRecord);
        assertNotNull(postgresRecord);
        assertEquals(this.userAgent, mysqlRecord.getUserAgent());
        assertEquals(this.userAgent, postgresRecord.getUserAgent());
        assertEquals(postgresRecord.getSequenceNum(), mysqlRecord.getSequenceNum());

        this.mvc.perform(completeRequest()).andExpect(status().isOk())
                .andExpect(content().json("{\"detail_code\":\"0001\"}", JsonCompareMode.STRICT));
        assertEquals(1, this.mysql.queryForObject("SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = ?",
                Integer.class, this.userAgent));
        assertEquals(1, this.postgres.queryForObject("SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = ?",
                Integer.class, this.userAgent));
        assertEquals(mysqlRecord.getSequenceNum(), this.dbAccess.selectMysqlDbUserAgentInfByUserAgent(this.userAgent).getSequenceNum());
        assertEquals(postgresRecord.getSequenceNum(), this.dbAccess.selectPostgresqlDbUserAgentInfByUserAgent(this.userAgent).getSequenceNum());
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, E_API001_0001",
            "true, false, E_API001_0002",
            "false, true, E_API001_0003"
    })
    void rejectsInconsistentRecordsWithoutChangingEitherDatabase(boolean hasPostgresRecord,
            boolean hasMysqlRecord, String messageId) throws Exception {
        Integer postgresNumber = this.postgres.queryForObject("SELECT NEXTVAL('sequence_num_create')", Integer.class);
        Integer mysqlNumber = this.postgres.queryForObject("SELECT NEXTVAL('sequence_num_create')", Integer.class);
        if (hasPostgresRecord) {
            this.postgres.update("INSERT INTO user_agent_inf (sequence_num, user_agent) VALUES (?, ?)",
                    postgresNumber, this.userAgent);
        }
        if (hasMysqlRecord) {
            this.mysql.update("INSERT INTO user_agent_inf (sequence_num, user_agent) VALUES (?, ?)",
                    mysqlNumber, this.userAgent);
        }
        var postgresBefore = this.postgres.queryForList(
                "SELECT sequence_num, user_agent FROM user_agent_inf WHERE user_agent = ?", this.userAgent);
        var mysqlBefore = this.mysql.queryForList(
                "SELECT sequence_num, user_agent FROM user_agent_inf WHERE user_agent = ?", this.userAgent);
        Long sequenceBefore = this.postgres.queryForObject("SELECT last_value FROM sequence_num_create", Long.class);

        var result = this.mvc.perform(completeRequest()).andExpect(status().isBadRequest())
                .andExpect(content().string("")).andReturn();

        OnlineBLogicException exception = assertInstanceOf(OnlineBLogicException.class, result.getResolvedException());
        assertEquals(messageId, exception.getMessageId());
        assertEquals(postgresBefore, this.postgres.queryForList(
                "SELECT sequence_num, user_agent FROM user_agent_inf WHERE user_agent = ?", this.userAgent));
        assertEquals(mysqlBefore, this.mysql.queryForList(
                "SELECT sequence_num, user_agent FROM user_agent_inf WHERE user_agent = ?", this.userAgent));
        assertEquals(sequenceBefore, this.postgres.queryForObject("SELECT last_value FROM sequence_num_create", Long.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "postgres", "mysql" })
    void observesRollbackWhenAnInsertFails(String failingDatabase) throws Exception {
        Integer nextNumber = this.postgres.queryForObject(
                "SELECT (CASE WHEN is_called THEN last_value + 1 ELSE last_value END)::integer FROM sequence_num_create",
                Integer.class);
        this.blockingUserAgent = this.userAgent + "-blocking-row";
        JdbcTemplate failingJdbc = "postgres".equals(failingDatabase) ? this.postgres : this.mysql;
        failingJdbc.update("INSERT INTO user_agent_inf (sequence_num, user_agent) VALUES (?, ?)",
                nextNumber, this.blockingUserAgent);

        var result = this.mvc.perform(completeRequest()).andExpect(status().isInternalServerError())
                .andExpect(content().string("")).andReturn();

        assertInstanceOf(DataIntegrityViolationException.class, result.getResolvedException());
        assertEquals(0, this.postgres.queryForObject("SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = ?",
                Integer.class, this.userAgent));
        assertEquals(0, this.mysql.queryForObject("SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = ?",
                Integer.class, this.userAgent));
        assertEquals(nextNumber, failingJdbc.queryForObject(
                "SELECT sequence_num FROM user_agent_inf WHERE user_agent = ?", Integer.class, this.blockingUserAgent));
        assertEquals(nextNumber.longValue(), this.postgres.queryForObject("SELECT last_value FROM sequence_num_create", Long.class));
    }

    private MockHttpServletRequestBuilder completeRequest() {
        return get("/api001").header("User-Agent", this.userAgent)
                .header("Sec-CH-UA", "\"Chromium\";v=\"103\"")
                .header("Sec-CH-UA-Mobile", "?0").header("Sec-CH-UA-Model", "\"\"")
                .header("Sec-CH-UA-Platform", "\"Windows\"")
                .header("Sec-CH-UA-Platform-Version", "\"10.0.0\"");
    }

    @ParameterizedTest
    @ValueSource(strings = { "postgres", "mysql" })
    void rollsBackBothDatabasesAfterSuccessfulInsert(String failingDatabase) throws Exception {
        this.insertFailure.targetUserAgent = this.userAgent;
        this.insertFailure.failingDatabase = failingDatabase;
        this.insertFailure.insertedRows = 0;
        this.insertFailure.autoCommit = null;

        var result = this.mvc.perform(completeRequest()).andExpect(status().isInternalServerError())
                .andExpect(content().string("")).andReturn();

        Throwable exception = result.getResolvedException();
        assertNotNull(exception);
        while (exception.getCause() != null) {
            exception = exception.getCause();
        }
        assertInstanceOf(InjectedAfterInsertException.class, exception);
        assertEquals(1, this.insertFailure.insertedRows);
        assertEquals(Boolean.FALSE, this.insertFailure.autoCommit);
        assertEquals(0, this.postgres.queryForObject("SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = ?",
                Integer.class, this.userAgent));
        assertEquals(0, this.mysql.queryForObject("SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = ?",
                Integer.class, this.userAgent));
    }

    @Test
    void documentsPartialCommitIfFailureOccursBeforePostgresCommit() throws Exception {
        this.insertFailure.targetUserAgent = this.userAgent;
        this.insertFailure.failingDatabase = "postgres";
        this.insertFailure.failBeforePostgresCommit = true;
        this.insertFailure.insertedRows = 0;
        this.insertFailure.autoCommit = null;

        var result = this.mvc.perform(completeRequest()).andExpect(status().isInternalServerError())
                .andExpect(content().string("")).andReturn();
        assertInstanceOf(InjectedBeforePostgresCommitException.class, result.getResolvedException());
        assertEquals(1, this.insertFailure.insertedRows);
        assertEquals(Boolean.FALSE, this.insertFailure.autoCommit);
        assertEquals(0, this.postgres.queryForObject("SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = ?",
                Integer.class, this.userAgent));
        assertEquals(1, this.mysql.queryForObject("SELECT COUNT(*) FROM user_agent_inf WHERE user_agent = ?",
                Integer.class, this.userAgent));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class InsertFailureConfiguration {
        @Bean
        FailAfterMysqlInsertInterceptor failAfterMysqlInsertInterceptor() {
            return new FailAfterMysqlInsertInterceptor();
        }

        @Bean
        static BeanPostProcessor postgresCommitFailurePostProcessor(
                ObjectProvider<FailAfterMysqlInsertInterceptor> failureProvider) {
            return new BeanPostProcessor() {
                @Override
                public Object postProcessAfterInitialization(Object bean, String beanName) {
                    if (!"postgresqlDbTransactionManager".equals(beanName)) {
                        return bean;
                    }
                    ProxyFactory proxy = new ProxyFactory(bean);
                    proxy.addAdvice((MethodInterceptor) invocation -> {
                        if ("commit".equals(invocation.getMethod().getName())
                                && failureProvider.getObject().commitFailureArmed) {
                            failureProvider.getObject().commitFailureArmed = false;
                            // Simulate a definite failure before the PostgreSQL
                            // commit and roll back its open transaction for cleanup.
                            ((PlatformTransactionManager) bean).rollback((TransactionStatus) invocation.getArguments()[0]);
                            throw new InjectedBeforePostgresCommitException();
                        }
                        return invocation.proceed();
                    });
                    return proxy.getProxy();
                }
            };
        }
    }

    @Intercepts(@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }))
    static class FailAfterMysqlInsertInterceptor implements Interceptor {
        private String targetUserAgent;
        private String failingDatabase;
        private boolean failBeforePostgresCommit;
        private boolean commitFailureArmed;
        private int insertedRows;
        private Boolean autoCommit;

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];
            boolean inject = ("mysql".equals(this.failingDatabase)
                    && "tool.common.db.mysql.dao.MysqlDbUserAgentInfDao.insert".equals(statement.getId())
                    && parameter instanceof MysqlDbUserAgentInfDto mysqlDto
                    && mysqlDto.getUserAgent().equals(this.targetUserAgent))
                    || ("postgres".equals(this.failingDatabase)
                    && "tool.common.db.postgresql.dao.PostgresqlDbUserAgentInfDao.insert".equals(statement.getId())
                    && parameter instanceof PostgresqlDbUserAgentInfDto postgresDto
                    && postgresDto.getUserAgent().equals(this.targetUserAgent));
            Object result = invocation.proceed();
            if (inject) {
                this.insertedRows = (Integer) result;
                this.autoCommit = ((Executor) invocation.getTarget()).getTransaction().getConnection().getAutoCommit();
                assertTrue(this.insertedRows > 0, "Real INSERT must succeed before injecting failure");
                if (this.failBeforePostgresCommit) {
                    this.commitFailureArmed = true;
                    return result;
                }
                throw new InjectedAfterInsertException();
            }
            return result;
        }
    }

    static class InjectedAfterInsertException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        InjectedAfterInsertException() {
            super("Test-only failure after successful INSERT");
        }
    }

    static class InjectedBeforePostgresCommitException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        InjectedBeforePostgresCommitException() {
            super("Test-only failure before outer PostgreSQL commit, after inner MySQL commit");
        }
    }
}
