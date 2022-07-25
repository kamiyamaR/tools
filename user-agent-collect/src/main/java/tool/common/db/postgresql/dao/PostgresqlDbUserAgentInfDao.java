package tool.common.db.postgresql.dao;

import org.apache.ibatis.annotations.Mapper;

import tool.common.db.postgresql.dto.PostgresqlDbUserAgentInfDto;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Mapper
public interface PostgresqlDbUserAgentInfDao {

    /**
     * 
     * @param input
     * @return
     */
    PostgresqlDbUserAgentInfDto select(PostgresqlDbUserAgentInfDto input);

    /**
     * 
     * @param input
     * @return
     */
    int insert(PostgresqlDbUserAgentInfDto input);

    /**
     * 
     * @param input
     * @return
     */
    int update(PostgresqlDbUserAgentInfDto input);
}
