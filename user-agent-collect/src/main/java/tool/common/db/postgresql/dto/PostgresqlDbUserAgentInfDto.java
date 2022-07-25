package tool.common.db.postgresql.dto;

import lombok.Data;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Data
public class PostgresqlDbUserAgentInfDto {
    private Integer sequenceNum;
    private String userAgent;
}
