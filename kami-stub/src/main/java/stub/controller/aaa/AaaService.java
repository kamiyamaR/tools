package stub.controller.aaa;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Service
public class AaaService {

    /**
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    public void exec(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("開始。");
        log.info("request=[{}]", request);
        log.info("response=[{}]", response);

        printRequest(request);

        log.info("終了。");
        throw new Exception("自作例外.");
    }

    /**
     * 
     * @param request
     * @throws IOException
     */
    private void printRequest(HttpServletRequest request) throws IOException {
        // リクエストメソッド出力
        log.info("リクエストメソッド={{}}", request.getMethod());

        // リクエストヘッダ出力
        StringBuilder sb = new StringBuilder();
        Enumeration<String> names = request.getHeaderNames();
        for (String name = names.hasMoreElements() ? names.nextElement() : null; name != null; name = names
                .nextElement()) {
            Enumeration<String> headers = request.getHeaders(name);
            sb.append(name).append('=');
            while (headers.hasMoreElements()) {
                String value = headers.nextElement();
                sb.append('[').append(value).append(']');
            }
            if (!names.hasMoreElements()) {
                break;
            }
            sb.append(',').append(' ');
        }
        log.info("リクエストヘッダ={{}}", sb.toString());

        // リクエストボディ出力
        try (BufferedReader reader = request.getReader(); Stream<String> stream = reader.lines();) {
            String body = stream.collect(Collectors.joining("\r\n"));
            log.info("リクエストボディ={{}}", body);
        }
    }

}
