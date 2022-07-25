package tool.common.db.mysql.dto;

import lombok.Data;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Data
public class MysqlDbUserAgentInfDto {
    private Integer sequenceNum;
    private String userAgent;
}
