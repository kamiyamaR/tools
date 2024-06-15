package stub.controller.repository;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.ServletInputStream;
import lombok.extern.slf4j.Slf4j;
import stub.controller.repository.http.RepositoryAccessor;
import stub.controller.repository.http.RequestInfo;
import stub.controller.repository.http.ResponseInfo;
import stub.controller.repository.prop.LibraryGetProperties;

@Slf4j
@Service
public class LibraryGetService {

    public static final String PATH = "/repository/libget";

    private final RepositoryAccessor repositoryAccessor;

    private final LibraryGetProperties prop;

    public LibraryGetService(
            @Qualifier(value = "repositoryAccessorJavaNetHttp") RepositoryAccessor repositoryAccessor,
            LibraryGetProperties prop) {
        this.repositoryAccessor = repositoryAccessor;
        this.prop = prop;
        log.debug("prop=[{}]", prop);
    }

    public ResponseEntity<byte[]> exec() throws Exception {
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
        {
            Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headers = requestAttributes.getRequest().getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    String header = headers.nextElement();
                    log.info(" [{}]：[{}]", headerName, header);

                    List<String> value = requestHeaders.get(headerName);
                    if (Objects.nonNull(value)) {
                        value.add(header);
                    } else {
                        value = new ArrayList<String>();
                        value.add(header);
                        requestHeaders.put(headerName, value);
                    }
                }
            }
        }

        String characterEncoding = requestAttributes.getRequest().getCharacterEncoding();
        long contentLengthLong = requestAttributes.getRequest().getContentLengthLong();
        String contentType = requestAttributes.getRequest().getContentType();
        {
            //log.info(" changeSessionId=[{}]", requestAttributes.getRequest().changeSessionId());
            if (requestAttributes.getRequest().isAsyncStarted()) {
                log.info(" AsyncContext=[{}]", requestAttributes.getRequest().getAsyncContext());
            }
            Enumeration<String> attributeNames = requestAttributes.getRequest().getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                log.info(" AttributeNames=[{}]", attributeNames.nextElement());
            }
            log.info(" AuthType=[{}]", requestAttributes.getRequest().getAuthType());
            log.info(" CharacterEncoding=[{}]", characterEncoding);
            log.info(" Class=[{}]", requestAttributes.getRequest().getClass());
            log.info(" ContentLength=[{}]", requestAttributes.getRequest().getContentLength());
            log.info(" ContentLengthLong=[{}]", contentLengthLong);
            log.info(" ContentType=[{}]", contentType);
            log.info(" ContextPath=[{}]", requestAttributes.getRequest().getContextPath());
            log.info(" Cookies=[{}]", Arrays.toString(requestAttributes.getRequest().getCookies()));
            log.info(" DispatcherType=[{}]", requestAttributes.getRequest().getDispatcherType());
            log.info(" HttpServletMapping=[{}]", requestAttributes.getRequest().getHttpServletMapping());
            log.info(" LocalAddr=[{}]", requestAttributes.getRequest().getLocalAddr());
            log.info(" Locale=[{}]", requestAttributes.getRequest().getLocale());
            log.info(" Locales=[{}]", requestAttributes.getRequest().getLocales());
            log.info(" LocalName=[{}]", requestAttributes.getRequest().getLocalName());
            log.info(" LocalPort=[{}]", requestAttributes.getRequest().getLocalPort());
            if (Objects.nonNull(contentType) && contentType.toLowerCase().startsWith("multipart/")) {
                log.info(" Parts=[{}]", requestAttributes.getRequest().getParts());
            }
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
        }
        Charset encoding = Charset.forName(characterEncoding);

        // リクエストパラメータ出力
        {
            Map<String, String[]> parameterMap = requestAttributes.getRequest().getParameterMap();
            Set<String> set = parameterMap.keySet();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String parameterName = iterator.next();
                String[] parameterValues = parameterMap.get(parameterName);
                log.info(" [{}]＝[{}]", parameterName, Arrays.toString(parameterValues));
            }
        }

        // ボディ出力
        byte[] requestBodyByteArray = null;
        if (contentLengthLong > 0L) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ServletInputStream inputStream = requestAttributes.getRequest().getInputStream();) {
                byte[] buffer = new byte[1024];
                for (int length = 0; (length = inputStream.read(buffer)) != -1;) {
                    outputStream.write(buffer, 0, length);
                }
                requestBodyByteArray = outputStream.toByteArray();
                log.info("{}", new StringBuilder().append(System.lineSeparator())
                        .append(new String(requestBodyByteArray, encoding)).toString());
            }
        }

        ///////////////////////////////////
        // リモートリポジトリアクセス.
        ///////////////////////////////////
        String url = new StringBuilder(prop.getUrl()).append(servletPath.replace(PATH, "")).toString();
        RequestInfo input = new RequestInfo();
        input.setMethod(method);
        input.setUrl(url);
        input.setProtocol(protocol);
        input.setHeaders(requestHeaders);
        input.setBodyByteArray(requestBodyByteArray);
        log.info("------------------------> [リクエスト送信]");
        ResponseInfo output = repositoryAccessor.execute(input);
        log.info("<------------------------ [レスポンス受信]");

        log.info(" [{}] [{}]", output.getVersion(), output.getStatusCode());

        BodyBuilder builder = ResponseEntity.status(output.getStatusCode());
        for (Entry<String, List<String>> entry : output.getHeaders().entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            builder.header(key, value.toArray(new String[value.size()]));
            for (String v : value) {
                log.info(" [{}]：[{}]", key, v);
            }
        }

        log.info("終了。");
        return builder.body(output.getBodyByteArray());
    }

}
