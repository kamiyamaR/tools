package stub.controller.wsdl.sample01;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class Sample01Service {

    /** . */
    private static final Path BASE_DIR = Paths.get("response_file/wsdl/sample01");

    /** . */
    private static final Path DEFAULT_RESPONSE_FILE = BASE_DIR.resolve("default_response.txt");

    /**
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    public void clearPDFAutoConverterEX(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 初期化処理（フォルダ作成）
        if (Files.notExists(BASE_DIR)) {
            Files.createDirectories(BASE_DIR);
        }

        // リクエストヘッダ出力
        StringBuilder sb = new StringBuilder();
        Enumeration<String> names = request.getHeaderNames();
        for (String name = names.hasMoreElements() ? names.nextElement() : null; Objects
                .nonNull(name); name = names.nextElement()) {
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
        String body = null;
        try (BufferedReader reader = request.getReader(); Stream<String> stream = reader.lines();) {
            body = stream.collect(Collectors.joining("\r\n"));
            log.info("リクエストボディ={{}}", body);
        }

        // 応答ファイルの決定
        String tempID = StringUtils.substringBetween(body, "<tempID>", "</tempID>");
        Path resFile = BASE_DIR.resolve(tempID + ".txt");
        if (Files.notExists(resFile)) {
            resFile = DEFAULT_RESPONSE_FILE;
        }
        log.info("tempID=[{}], 応答ファイル=[{}]", tempID, resFile.toAbsolutePath());

        // 応答ファイル読み込み
        List<String> resInfo = Files.readAllLines(resFile, StandardCharsets.UTF_8);
        int retStatus = Integer.parseInt(resInfo.get(0)); // 応答ファイル1行目はステータスコード
        StringBuilder resSb = new StringBuilder();
        for (int idx = 1; idx < resInfo.size(); idx++) {
            String line = resInfo.get(idx);
            resSb.append(line);
        }
        String resBody = resSb.toString();

        // レスポンス情報設定
        if (StringUtils.isNotEmpty(resBody)) {
            response.setStatus(retStatus);
            response.setContentType(MediaType.TEXT_XML_VALUE);
            try (PrintWriter writer = response.getWriter()) {
                writer.write(resBody);
            }
        } else {
            HttpStatus status = HttpStatus.valueOf(retStatus);
            switch (status.series()) {
            case INFORMATIONAL:
            case SUCCESSFUL:
            case REDIRECTION:
                response.setStatus(retStatus);
                break;
            case CLIENT_ERROR:
            case SERVER_ERROR:
                response.sendError(retStatus);
                break;
            default:
                response.setStatus(retStatus);
            }
        }

    }

    /**
     * 
     * @param request
     * @param response
     * @param sleepTime
     * @throws Exception
     */
    public void clearPDFAutoConverterEX(HttpServletRequest request, HttpServletResponse response, long sleepTime)
            throws Exception {
        log.info("スリープ処理 開始. [{}]", sleepTime);
        TimeUnit.MILLISECONDS.sleep(sleepTime);
        log.info("スリープ処理 終了. [{}]", sleepTime);
        clearPDFAutoConverterEX(request, response);
    }
}
