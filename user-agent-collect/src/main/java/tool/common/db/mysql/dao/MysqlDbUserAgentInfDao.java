package tool.common.db.mysql.dao;

import org.apache.ibatis.annotations.Mapper;

import tool.common.db.mysql.dto.MysqlDbUserAgentInfDto;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Mapper
public interface MysqlDbUserAgentInfDao {

    /**
     * 
     * @param input
     * @return
     */
    MysqlDbUserAgentInfDto select(MysqlDbUserAgentInfDto input);

    /**
     * 
     * @param input
     * @return
     */
    int insert(MysqlDbUserAgentInfDto input);

    /**
     * 
     * @param input
     * @return
     */
    int update(MysqlDbUserAgentInfDto input);
}
