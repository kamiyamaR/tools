package tool.controller.api001;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tool.common.aop.ApiEntry;
import tool.common.constant.Constant;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@RestController
public class CollectUserAgentController {

    /** . */
    @Autowired
    private CollectUserAgentService service;

    /**
     * 
     * @param userAgent
     * @param secChUa
     * @param secChUaMobile
     * @param secChUaModel
     * @param secChUaPlatform
     * @param secChUaPlatformVersion
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
        return this.service.exec(userAgent, secChUa, secChUaMobile, secChUaModel, secChUaPlatform,
                secChUaPlatformVersion);
    }
}
