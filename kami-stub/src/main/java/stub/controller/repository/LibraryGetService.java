package stub.controller.repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
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
            @Qualifier(value = "repositoryAccessorApacheHttpClient5") RepositoryAccessor repositoryAccessor,
            LibraryGetProperties prop) {
        this.repositoryAccessor = repositoryAccessor;
        this.prop = prop;
    }

    public ResponseEntity<byte[]> exec() throws Exception {
        log.info("開始。");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        log.info(" [{}] [{}] [{}]", request.getMethod(), request.getServletPath(), request.getProtocol());

        RequestInfo input = new RequestInfo();
        input.setMethod(request.getMethod());
        input.setUrl(new StringBuilder(prop.getUrl()).append(request.getServletPath().replace(PATH, "")).toString());
        input.setHeaders(getRequestHeader(request));
        input.setBodyByteArray(getRequestBody(request));
        log.info("------------------------> [リクエスト送信]");
        ResponseInfo output = repositoryAccessor.execute(input);
        log.info("<------------------------ [レスポンス受信]");

        log.info(" ステータスコード：[{}]", output.getStatusCode());
        HttpHeaders responseHeader = new HttpHeaders();
        output.getHeaders().forEach((key, values) -> values.forEach(value -> {
            log.info(" [{}]：[{}]", key, value);
            responseHeader.add(key, value);
        }));
        //log.info("{}",new StringBuilder().append(System.lineSeparator()).append(new String(output.getBodyByteArray(), Charset.forName(request.getCharacterEncoding()))).toString());
        log.info("終了。");
        return ResponseEntity.status(output.getStatusCode()).headers(responseHeader).body(output.getBodyByteArray());
    }

    private Map<String, List<String>> getRequestHeader(HttpServletRequest request) {
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        request.getHeaderNames().asIterator().forEachRemaining(
                headerName -> request.getHeaders(headerName).asIterator().forEachRemaining(header -> {
                    log.info(" [{}]：[{}]", headerName, header);
                    requestHeaders.computeIfAbsent(headerName, function -> new ArrayList<String>(1)).add(header);
                }));
        return requestHeaders;
    }

    private byte[] getRequestBody(HttpServletRequest request) throws IOException {
        if (request.getContentLengthLong() <= 0L) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ServletInputStream inputStream = request.getInputStream();) {
            byte[] buffer = new byte[1024];
            for (int length = 0; (length = inputStream.read(buffer)) != -1;) {
                outputStream.write(buffer, 0, length);
            }
            byte[] requestBodyByteArray = outputStream.toByteArray();
            log.info("{}",
                    new StringBuilder().append(System.lineSeparator())
                            .append(new String(requestBodyByteArray, Charset.forName(request.getCharacterEncoding())))
                            .toString());
            return requestBodyByteArray;
        }
    }

}
