package stub.controller.aaa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Controller
public class AaaController {

    /** . */
    private final AaaService aaaService;

    /** . */
    public AaaController(@Autowired AaaService aaaService) {
        this.aaaService = aaaService;
    }

    /**
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    @PostMapping(value = { "/aaa" })
    public void exec(HttpServletRequest request, HttpServletResponse response) throws Exception {
        aaaService.exec(request, response);
    }

    /**
     * 
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(value = { Exception.class })
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleException(Exception e, WebRequest request) {
        log.info("AaaController::handleException() call.");

        StringBuilder sb = new StringBuilder();
        Iterator<String> names = request.getHeaderNames();
        for (String name = names.hasNext() ? names.next() : null; name != null; name = names.next()) {
            String[] values = request.getHeaderValues(name);
            sb.append(name).append('=');
            for (String value : values) {
                sb.append('[').append(value).append(']');
            }
            if (!names.hasNext()) {
                break;
            }
            sb.append(',').append(' ');
        }
        log.info("リクエストヘッダ={{}}", sb.toString());

        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            log.info("{}:{}", entry.getKey(), entry.getValue());
        }

        log.info("例外発生！！", e);

        Map<String, Object> map = new HashMap<>();
        map.put("aaaaa", "A");
        map.put("bbbbb", "B");
        map.put("ccccc", "C");
        //ResponseEntity<Object> responseEntity = new ResponseEntity<Object>(map, HttpStatus.OK);
        return map;
    }

    /**
     * 
     * @author kamiyama ryohei
     *
     */
    @Aspect
    @Component
    public static class AaaAop {

        /**
         * 
         * @param point
         * @param request
         * @param response
         * @return
         * @throws Throwable
         */
        @Around(value = "execution(public void stub.controller.aaa.AaaController.exec(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse) throws Exception) && args(request, response)", argNames = "point, request, response")
        public Object around(ProceedingJoinPoint point, HttpServletRequest request, HttpServletResponse response)
                throws Throwable {
            log.info("AaaAop::around() call.");

            CodeSignature signature = (CodeSignature) point.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = point.getArgs();
            for (int idx = 0; idx < names.length; idx++) {
                String name = names[idx];
                Object arg = args[idx];
                if (Objects.nonNull(arg)) {
                    log.info("{}=[{}]", name, arg);
                } else {
                    log.info("{} is null.", name);
                }
            }
            log.info("request=[{}]", request);
            log.info("response=[{}]", response);

            try {
                Object result = point.proceed();

                log.info("result=[{}]", result);
                return result;
            } catch (Throwable t) {
                log.info("発生した例外.", t);
                throw t;
            }
        }

        /**
         * 
         * @param point
         * @param request
         * @param response
         */
        @Before(value = "execution(public void stub.controller.aaa.AaaController.exec(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse) throws Exception) && args(request, response)", argNames = "point, request, response")
        public void before(JoinPoint point, HttpServletRequest request, HttpServletResponse response) {
            log.info("AaaAop::before() call.");

            CodeSignature signature = (CodeSignature) point.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = point.getArgs();
            for (int idx = 0; idx < names.length; idx++) {
                String name = names[idx];
                Object arg = args[idx];
                if (Objects.nonNull(arg)) {
                    log.info("{}=[{}]", name, arg);
                } else {
                    log.info("{} is null.", name);
                }
            }
            log.info("request=[{}]", request);
            log.info("response=[{}]", response);
        }

        /**
         * 
         * @param point
         * @param request
         * @param response
         * @param result
         */
        @AfterReturning(value = "execution(public void stub.controller.aaa.AaaController.exec(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse) throws Exception) && args(request, response)", returning = "result", argNames = "point, request, response, result")
        public void afterReturning(JoinPoint point, HttpServletRequest request, HttpServletResponse response,
                Object result) {
            log.info("AaaAop::afterReturning() call.");

            CodeSignature signature = (CodeSignature) point.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = point.getArgs();
            for (int idx = 0; idx < names.length; idx++) {
                String name = names[idx];
                Object arg = args[idx];
                if (Objects.nonNull(arg)) {
                    log.info("{}=[{}]", name, arg);
                } else {
                    log.info("{} is null.", name);
                }
            }
            log.info("request=[{}]", request);
            log.info("response=[{}]", response);
            log.info("result=[{}]", result);
        }

        /**
         * 
         * @param point
         * @param response
         * @param cause
         */
        @AfterThrowing(value = "execution(public void stub.controller.aaa.AaaController.exec(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse) throws Exception) && args(jakarta.servlet.http.HttpServletRequest, response)", throwing = "cause", argNames = "point, response, cause")
        public void afterThrowing(JoinPoint point, HttpServletResponse response, Throwable cause) {
            log.info("AaaAop::afterThrowing() call.");

            CodeSignature signature = (CodeSignature) point.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = point.getArgs();
            for (int idx = 0; idx < names.length; idx++) {
                String name = names[idx];
                Object arg = args[idx];
                if (Objects.nonNull(arg)) {
                    log.info("{}=[{}]", name, arg);
                } else {
                    log.info("{} is null.", name);
                }
            }

            log.info("response=[{}]", response);
            log.info("発生した例外.", cause);
        }

        /**
         * 
         * @param point
         * @param request
         * @param response
         */
        @After(value = "execution(public void stub.controller.aaa.AaaController.exec(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse) throws Exception) && args(request, response)", argNames = "point, request, response")
        public void after(JoinPoint point, HttpServletRequest request, HttpServletResponse response) {
            log.info("AaaAop::after() call.");

            CodeSignature signature = (CodeSignature) point.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = point.getArgs();
            for (int idx = 0; idx < names.length; idx++) {
                String name = names[idx];
                Object arg = args[idx];
                if (Objects.nonNull(arg)) {
                    log.info("{}=[{}]", name, arg);
                } else {
                    log.info("{} is null.", name);
                }
            }
            log.info("request=[{}]", request);
            log.info("response=[{}]", response);
        }

    }

}
