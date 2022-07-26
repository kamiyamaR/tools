package tool.common.aop;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

        if (log.isDebugEnabled()) {
            // RequestAttributesの取得.
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();

            // リクエストパス出力.
            log.debug("[[{}]]", request.getServletPath());

            // リクエストメソッド出力.
            log.debug("[HttpRequestMethod]");
            log.debug("[{}]", request.getMethod());

            // リクエストヘッダ出力.
            log.debug("[HttpRequestHeader]");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    String headerValue = headerValues.nextElement();
                    if (Objects.nonNull(headerValue)) {
                        log.debug("{}：[{}]", headerName, headerValue);
                    } else {
                        log.debug("{} is null.", headerName);
                    }
                }
            }

            // リクエストパラメータ(実行メソッドの引数)出力.
            log.debug("[HttpRequestParameter(Method args)]");
            CodeSignature codeSignature = (CodeSignature) signature;
            String[] parameterNames = codeSignature.getParameterNames();
            Object[] args = pjp.getArgs();
            for (int idx = 0; idx < parameterNames.length; idx++) {
                String parameterName = parameterNames[idx];
                Object arg = args[idx];
                if (Objects.nonNull(arg)) {
                    log.debug("{}：[{}]", parameterName, arg);
                } else {
                    log.debug("{} is null.", parameterName);
                }
            }

            // リクエストパラメータ(クエリパラメータ＆フォームデータ)出力.
            log.debug("[HttpRequestParameter(Query parameter & Form data)]");
            Map<String, String[]> parameterMap = request.getParameterMap();
            Set<String> set = parameterMap.keySet();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String parameterName = iterator.next();
                String[] parameterValues = parameterMap.get(parameterName);
                if (Objects.nonNull(parameterValues)) {
                    log.debug("{}：{}", parameterName, Arrays.toString(parameterValues));
                } else {
                    log.debug("{} is null.", parameterName);
                }
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
