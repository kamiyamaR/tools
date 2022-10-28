package tool.controller.api001;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.extern.slf4j.Slf4j;
import tool.common.constant.Constant;
import tool.common.db.mysql.dto.MysqlDbUserAgentInfDto;
import tool.common.db.postgresql.dto.PostgresqlDbUserAgentInfDto;
import tool.common.exception.OnlineBLogicException;

@Slf4j
@Service
public class CollectUserAgentService {

    /** . */
    @Autowired
    private CollectUserAgentDbAccessFunction collectUserAgentDbAccessFunction;

    /**
     * .<br>
     * @param userAgent
     * @param secChUa
     * @param secChUaMobile
     * @param secChUaModel
     * @param secChUaPlatform
     * @param secChUaPlatformVersion
     * @return
     */
    public ResponseEntity<CollectUserAgentResponseBody> exec(String userAgent, String secChUa, String secChUaMobile,
            String secChUaModel, String secChUaPlatform, String secChUaPlatformVersion) {

        if (Objects.isNull(secChUa) || Objects.isNull(secChUaMobile) || Objects.isNull(secChUaModel)
                || Objects.isNull(secChUaPlatform) || Objects.isNull(secChUaPlatformVersion)) {
            StringBuilder builder = new StringBuilder();
            builder.append(Constant.SEC_CH_UA);
            builder.append(", ");
            builder.append(Constant.SEC_CH_UA_MOBILE);
            builder.append(", ");
            builder.append(Constant.SEC_CH_UA_MODEL);
            builder.append(", ");
            builder.append(Constant.SEC_CH_UA_PLATFORM);
            builder.append(", ");
            builder.append(Constant.SEC_CH_UA_PLATFORM_VERSION);
            String value = builder.toString();

            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
            headers.add(Constant.ACCEPT_CH, value);
            headers.add(HttpHeaders.LOCATION, "/api001");

            ResponseEntity<CollectUserAgentResponseBody> responseEntity = new ResponseEntity<CollectUserAgentResponseBody>(
                    headers, HttpStatus.PERMANENT_REDIRECT);
            return responseEntity;
        }

        PostgresqlDbUserAgentInfDto pUserAgentInfDto = this.collectUserAgentDbAccessFunction
                .selectPostgresqlDbUserAgentInfByUserAgent(userAgent);

        MysqlDbUserAgentInfDto mUserAgentInfDto = this.collectUserAgentDbAccessFunction
                .selectMysqlDbUserAgentInfByUserAgent(userAgent);

        String detailCode = null;
        if (Objects.isNull(pUserAgentInfDto) && Objects.isNull(mUserAgentInfDto)) {
            log.debug("双方登録なしルート.");
            this.collectUserAgentDbAccessFunction.registDb(userAgent);
            detailCode = "0000";
        } else if (Objects.nonNull(pUserAgentInfDto) && Objects.nonNull(mUserAgentInfDto)) {
            log.debug("双方登録ありルート.");
            int pSequenceNum = pUserAgentInfDto.getSequenceNum().intValue();
            int mSequenceNum = mUserAgentInfDto.getSequenceNum().intValue();
            if (pSequenceNum != mSequenceNum) {
                log.info("シーケンス番号が一致してない. PostgreSQL側=[{}], MySQL側=[{}]", pSequenceNum, mSequenceNum);
                throw new OnlineBLogicException("E_API001_0001", HttpStatus.BAD_REQUEST, pSequenceNum, mSequenceNum);
            }

            log.debug("シーケンス番号が一致しているため問題なし.");
            detailCode = "0001";
        } else {
            log.info("片方だけ登録されているルート. PostgreSQL側=[{}], MySQL側=[{}]", pUserAgentInfDto, mUserAgentInfDto);
            if (Objects.nonNull(pUserAgentInfDto)) {
                int pSequenceNum = pUserAgentInfDto.getSequenceNum().intValue();
                throw new OnlineBLogicException("E_API001_0002", HttpStatus.BAD_REQUEST, pSequenceNum);
            } else {
                int mSequenceNum = mUserAgentInfDto.getSequenceNum().intValue();
                throw new OnlineBLogicException("E_API001_0003", HttpStatus.BAD_REQUEST, mSequenceNum);
            }
        }

        CollectUserAgentResponseBody body = new CollectUserAgentResponseBody();
        body.setDetailCode(detailCode);
        ResponseEntity<CollectUserAgentResponseBody> responseEntity = new ResponseEntity<CollectUserAgentResponseBody>(
                body, HttpStatus.OK);
        return responseEntity;
    }
}
