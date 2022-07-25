package tool.controller.api001;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import tool.common.aop.ApiEntry;
import tool.common.constant.Constant;
import tool.common.db.mysql.dto.MysqlDbUserAgentInfDto;
import tool.common.db.postgresql.dto.PostgresqlDbUserAgentInfDto;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@RestController
public class CollectUserAgent {

    @Autowired
    private CollectUserAgentDbAccessFunction collectUserAgentDbAccessFunction;

    /**
     * 
     * @param userAgent
     * @return
     */
    @ApiEntry
    @RequestMapping(path = "/api001", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<CollectUserAgentResponseBody> exec(
            @RequestHeader(name = HttpHeaders.USER_AGENT, required = true) String userAgent,
            @RequestHeader(name = Constant.SEC_CH_UA, required = false) String secChUa,
            @RequestHeader(name = Constant.SEC_CH_UA_MOBILE, required = false) String secChUaMobile,
            @RequestHeader(name = Constant.SEC_CH_UA_MODEL, required = false) String secChUaModel,
            @RequestHeader(name = Constant.SEC_CH_UA_PLATFORM, required = false) String secChUaPlatform,
            @RequestHeader(name = Constant.SEC_CH_UA_PLATFORM_VERSION, required = false) String secChUaPlatformVersion) {

        if (Objects.isNull(secChUa) || Objects.isNull(secChUaMobile) || Objects.isNull(secChUaModel)
                || Objects.isNull(secChUaPlatform) || Objects.isNull(secChUaPlatformVersion)) {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
            headers.add(Constant.ACCEPT_CH, Constant.SEC_CH_UA);
            headers.add(Constant.ACCEPT_CH, Constant.SEC_CH_UA_MOBILE);
            headers.add(Constant.ACCEPT_CH, Constant.SEC_CH_UA_MODEL);
            headers.add(Constant.ACCEPT_CH, Constant.SEC_CH_UA_PLATFORM);
            headers.add(Constant.ACCEPT_CH, Constant.SEC_CH_UA_PLATFORM_VERSION);
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
                throw new RuntimeException();
            }

            log.debug("シーケンス番号が一致しているため問題なし.");
            detailCode = "0001";
        } else {
            log.debug("片方だけ登録されているルート. PostgreSQL側=[{}], MySQL側=[{}]", pUserAgentInfDto, mUserAgentInfDto);
            throw new RuntimeException();
        }

        CollectUserAgentResponseBody body = new CollectUserAgentResponseBody();
        body.setDetailCode(detailCode);
        ResponseEntity<CollectUserAgentResponseBody> responseEntity = new ResponseEntity<CollectUserAgentResponseBody>(
                body, HttpStatus.OK);
        return responseEntity;
    }
}
