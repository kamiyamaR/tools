package tool.common.db.postgresql.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Mapper
public interface SequenceNumCreateDao {

    /**
     * 
     * @return
     */
    Integer nextVal();
}
