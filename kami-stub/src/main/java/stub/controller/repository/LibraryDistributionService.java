package stub.controller.repository;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LibraryDistributionService {

    public static final String PATH = "/repository/libdistribution";

    public void exec() throws Exception {
        log.info("開始。");

        ///////////////////////////////////
        // リクエスト情報取得.
        ///////////////////////////////////
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();

        log.info("------------------------> [リクエスト情報]");
        String method = requestAttributes.getRequest().getMethod();
        String servletPath = requestAttributes.getRequest().getServletPath();
        String protocol = requestAttributes.getRequest().getProtocol();
        log.info(" [{}] [{}] [{}]", method, servletPath, protocol);

        // リクエストヘッダ出力
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = requestAttributes.getRequest().getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                log.info(" [{}]：[{}]", headerName, headerValue);

                List<String> value = requestHeaders.get(headerName);
                if (Objects.nonNull(value)) {
                    value.add(headerValue);
                } else {
                    value = new ArrayList<String>();
                    value.add(headerValue);
                    requestHeaders.put(headerName, value);
                }
            }
        }

        String characterEncoding = requestAttributes.getRequest().getCharacterEncoding();
        long contentLengthLong = requestAttributes.getRequest().getContentLengthLong();
        //log.info(" changeSessionId=[{}]", requestAttributes.getRequest().changeSessionId());
        if (requestAttributes.getRequest().isAsyncStarted()) {
            log.info(" AsyncContext=[{}]", requestAttributes.getRequest().getAsyncContext());
        }
        Enumeration<String> attributeNames = requestAttributes.getRequest().getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            log.info(" AttributeNames=[{}]", attributeNames.nextElement());
        }
        log.info(" AttributeNames=[{}]", requestAttributes.getRequest().getAttributeNames());
        log.info(" AuthType=[{}]", requestAttributes.getRequest().getAuthType());
        log.info(" CharacterEncoding=[{}]", characterEncoding);
        log.info(" Class=[{}]", requestAttributes.getRequest().getClass());
        log.info(" ContentLength=[{}]", requestAttributes.getRequest().getContentLength());
        log.info(" ContentLengthLong=[{}]", contentLengthLong);
        log.info(" ContentType=[{}]", requestAttributes.getRequest().getContentType());
        log.info(" ContextPath=[{}]", requestAttributes.getRequest().getContextPath());
        log.info(" Cookies=[{}]", Arrays.toString(requestAttributes.getRequest().getCookies()));
        log.info(" DispatcherType=[{}]", requestAttributes.getRequest().getDispatcherType());
        log.info(" HttpServletMapping=[{}]", requestAttributes.getRequest().getHttpServletMapping());
        log.info(" LocalAddr=[{}]", requestAttributes.getRequest().getLocalAddr());
        log.info(" Locale=[{}]", requestAttributes.getRequest().getLocale());
        log.info(" Locales=[{}]", requestAttributes.getRequest().getLocales());
        log.info(" LocalName=[{}]", requestAttributes.getRequest().getLocalName());
        log.info(" LocalPort=[{}]", requestAttributes.getRequest().getLocalPort());
        //log.info(" Parts=[{}]", requestAttributes.getRequest().getParts());
        log.info(" PathInfo=[{}]", requestAttributes.getRequest().getPathInfo());
        log.info(" PathTranslated=[{}]", requestAttributes.getRequest().getPathTranslated());
        log.info(" ProtocolRequestId=[{}]", requestAttributes.getRequest().getProtocolRequestId());
        log.info(" QueryString=[{}]", requestAttributes.getRequest().getQueryString());
        log.info(" RemoteAddr=[{}]", requestAttributes.getRequest().getRemoteAddr());
        log.info(" RemoteHost=[{}]", requestAttributes.getRequest().getRemoteHost());
        log.info(" RemotePort=[{}]", requestAttributes.getRequest().getRemotePort());
        log.info(" RemoteUser=[{}]", requestAttributes.getRequest().getRemoteUser());
        log.info(" RequestedSessionId=[{}]", requestAttributes.getRequest().getRequestedSessionId());
        log.info(" RequestId=[{}]", requestAttributes.getRequest().getRequestId());
        log.info(" RequestURI=[{}]", requestAttributes.getRequest().getRequestURI());
        log.info(" Scheme=[{}]", requestAttributes.getRequest().getScheme());
        log.info(" ServerName=[{}]", requestAttributes.getRequest().getServerName());
        log.info(" ServerPort=[{}]", requestAttributes.getRequest().getServerPort());
        log.info(" ServletConnection=[{}]", requestAttributes.getRequest().getServletConnection());
        log.info(" ServletContext=[{}]", requestAttributes.getRequest().getServletContext());
        log.info(" Session=[{}]", requestAttributes.getRequest().getSession());
        log.info(" TrailerFields=[{}]", requestAttributes.getRequest().getTrailerFields());
        log.info(" UserPrincipal=[{}]", requestAttributes.getRequest().getUserPrincipal());
        Charset encoding = Charset.forName(characterEncoding);

        // リクエストパラメータ出力
        Map<String, String[]> parameterMap = requestAttributes.getRequest().getParameterMap();
        Set<String> set = parameterMap.keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String parameterName = iterator.next();
            String[] parameterValues = parameterMap.get(parameterName);
            log.info(" [{}]＝[{}]", parameterName, Arrays.toString(parameterValues));
        }

        // ボディ出力
        /* java.nio.charset.MalformedInputException: Input length = 1 が発生するものがあるためスキップ
        String requestBody = null;
        if (contentLengthLong > 0) {
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = requestAttributes.getRequest().getReader();) {
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(System.lineSeparator()).append(line);
                }
            }
            requestBody = builder.toString();
            log.info(requestBody);
        }
        */

    }

}
