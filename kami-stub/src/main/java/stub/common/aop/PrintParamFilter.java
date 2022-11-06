package stub.common.aop;

import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import stub.common.aop.annotation.OnlineProcessEntry;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Aspect
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class PrintParamFilter {

    /**
     * 
     * @param point
     * @param onlineProcessEntry
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(onlineProcessEntry)")
    public Object intercept(ProceedingJoinPoint point, OnlineProcessEntry onlineProcessEntry) throws Throwable {
        if (log.isDebugEnabled()) {
            // リクエストパラメータ(実行メソッドの引数)出力.
            log.debug("[HttpRequestParameter(Method args)]");
            CodeSignature codeSignature = (CodeSignature) point.getSignature();
            String[] parameterNames = codeSignature.getParameterNames();
            Object[] args = point.getArgs();
            for (int idx = 0; idx < parameterNames.length; idx++) {
                String parameterName = parameterNames[idx];
                Object arg = args[idx];
                if (Objects.nonNull(arg)) {
                    log.debug("{}：[{}]", parameterName, arg);
                } else {
                    log.debug("{} is null.", parameterName);
                }
            }
        }

        // 対象メソッド実行
        Object result = point.proceed();

        if (log.isDebugEnabled()) {
            // レスポンス情報の出力.
            if (Objects.nonNull(result)) {
                if (result instanceof ResponseEntity) {
                    @SuppressWarnings(value = { "unchecked" })
                    ResponseEntity<Object> responseEntity = (ResponseEntity<Object>) result;
                    log.debug("Status Code：{}", responseEntity.getStatusCodeValue());
                    Object body = responseEntity.getBody();
                    if (Objects.isNull(body)) {
                        log.debug("Response body is null.");
                    } else {
                        log.debug("Response body：[{}]", body);
                    }
                } else {
                    log.debug("Return object is not ResponseEntity.");
                    log.debug("result：[{}]", result);
                }
            } else {
                log.debug("result is null.");
            }
        }
        return result;
    }

}
