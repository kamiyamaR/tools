package tool.controller.api001;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tool.common.db.mysql.dao.MysqlDbUserAgentInfDao;
import tool.common.db.mysql.dto.MysqlDbUserAgentInfDto;
import tool.common.db.mysql.transaction.MysqlDbTransactionalRead;
import tool.common.db.mysql.transaction.MysqlDbTransactionalWrite;
import tool.common.db.postgresql.dao.PostgresqlDbUserAgentInfDao;
import tool.common.db.postgresql.dao.SequenceNumCreateDao;
import tool.common.db.postgresql.dto.PostgresqlDbUserAgentInfDto;
import tool.common.db.postgresql.transaction.PostgresqlDbTransactionalRead;
import tool.common.db.postgresql.transaction.PostgresqlDbTransactionalWrite;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Component
public class CollectUserAgentDbAccessFunction {

    @Autowired
    private SequenceNumCreateDao sequenceNumCreateDao;

    @Autowired
    private PostgresqlDbUserAgentInfDao postgresqlDbUserAgentInfDao;

    @Autowired
    private MysqlDbUserAgentInfDao mysqlDbUserAgentInfDao;

    /**
     * 
     * @param userAgent
     * @return
     */
    @PostgresqlDbTransactionalRead
    public PostgresqlDbUserAgentInfDto selectPostgresqlDbUserAgentInfByUserAgent(String userAgent) {
        PostgresqlDbUserAgentInfDto input = new PostgresqlDbUserAgentInfDto();
        input.setUserAgent(userAgent);
        PostgresqlDbUserAgentInfDto output = this.postgresqlDbUserAgentInfDao.select(input);
        log.debug("取得結果(PostgreSQL)：[{}]", output);
        return output;
    }

    /**
     * 
     * @param userAgent
     * @return
     */
    @MysqlDbTransactionalRead
    public MysqlDbUserAgentInfDto selectMysqlDbUserAgentInfByUserAgent(String userAgent) {
        MysqlDbUserAgentInfDto input = new MysqlDbUserAgentInfDto();
        input.setUserAgent(userAgent);
        MysqlDbUserAgentInfDto output = this.mysqlDbUserAgentInfDao.select(input);
        log.debug("取得結果(MySQL)：[{}]", output);
        return output;
    }

    /**
     * 
     * @param userAgent
     */
    @PostgresqlDbTransactionalWrite
    @MysqlDbTransactionalWrite
    public void registDb(String userAgent) {

        Integer sequenceNum = this.sequenceNumCreateDao.nextVal();
        log.debug("シーケンス番号：[{}]", sequenceNum);

        PostgresqlDbUserAgentInfDto postgresqlInput = new PostgresqlDbUserAgentInfDto();
        postgresqlInput.setSequenceNum(sequenceNum);
        postgresqlInput.setUserAgent(userAgent);
        int postgresqlUpdateCnt = this.postgresqlDbUserAgentInfDao.insert(postgresqlInput);
        log.debug("登録件数(PostgreSQL)：[{}]", postgresqlUpdateCnt);

        MysqlDbUserAgentInfDto mysqlInput = new MysqlDbUserAgentInfDto();
        mysqlInput.setSequenceNum(sequenceNum);
        mysqlInput.setUserAgent(userAgent);
        int mysqlUpdateCnt = this.mysqlDbUserAgentInfDao.insert(mysqlInput);
        log.debug("登録件数(MySQL)：[{}]", mysqlUpdateCnt);
    }
}
