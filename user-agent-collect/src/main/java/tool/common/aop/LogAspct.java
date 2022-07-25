package tool.common.aop;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Aspect
@Component
public class LogAspct {

    /**
     * 
     * 
     * @param pjp
     * @param apiEntry
     * @return
     * @throws Throwable
     */
    @Around(value = "@annotation(apiEntry)")
    public Object printParamFilter(ProceedingJoinPoint pjp, ApiEntry apiEntry) throws Throwable {
        Signature signature = pjp.getSignature();
        String execClassPath = signature.getDeclaringTypeName();
        String execMethodName = signature.getName();
        String execTarget = new StringBuilder().append(execClassPath).append('.').append(execMethodName).toString();
        log.info("{} 開始.", execTarget);
        log.info("apiEntry：[{}]", apiEntry);

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();

        // リクエストパス出力.
        log.debug("[[{}]]", httpServletRequest.getServletPath());

        // リクエストヘッダ出力.
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        log.debug("[HttpRequestHeader]");
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = httpServletRequest.getHeader(headerName);
            if (Objects.nonNull(headerValue)) {
                log.debug("{}：[{}]", headerName, headerValue);
            } else {
                log.debug("{} is null.", headerName);
            }
        }

        // リクエストパラメータ(実行メソッドの引数)出力.
        CodeSignature codeSignature = (CodeSignature) signature;
        String[] paramNames = codeSignature.getParameterNames();
        Object[] paramValues = pjp.getArgs();
        log.debug("[HttpRequestParameter(Method args)]");
        for (int idx = 0; idx < paramNames.length; idx++) {
            String paramName = paramNames[idx];
            Object paramValue = paramValues[idx];
            if (Objects.nonNull(paramValue)) {
                log.debug("{}：[{}]", paramName, paramValue);
            } else {
                log.debug("{} is null.", paramName);
            }
        }

        // リクエストパラメータ(クエリパラメータ＆フォームデータ)出力.
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        log.debug("[HttpRequestParameter(Query parameter & Form data)]");
        for (Iterator<String> it = parameterMap.keySet().iterator(); it.hasNext();) {
            String paramName = it.next();
            Object paramValue = parameterMap.get(paramName);
            if (Objects.nonNull(paramValue)) {
                log.debug("{}：{}", paramName, paramValue);
            } else {
                log.debug("{} is null.", paramName);
            }
        }

        // 対象メソッド実行
        Object result = pjp.proceed();

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

        log.info("{} 終了.", execTarget);
        return result;
    }
}
