package stub.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class StubController {

    private static final String CRLF = "\r\n";

    private static final String DEFAULT_RESPONSE_FILE = "default.txt";

    private static final String STATUS_TAG_NAME = "status";
    private static final String HEADER_TAG_NAME = "header";
    private static final String HEADER_NAME_TAG_NAME = "name";
    private static final String HEADER_VALUE_TAG_NAME = "value";
    private static final String BODY_TAG_NAME = "body";

    private static final String CONTENT_TYPE_LOWER = HttpHeaders.CONTENT_TYPE.toLowerCase();

    @RequestMapping(path = { "/**" })
    public void call() {
        log.info("開始.");
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        try {
            exec(request, response);
        } catch (Exception e) {
            log.error("例外発生！", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        log.info("終了.");
    }

    private void exec(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String responseFileName = DEFAULT_RESPONSE_FILE;
        setResponse(response, responseFileName);
    }

    private void printRequest(HttpServletRequest request) throws IOException {
        log.info("------------------------> [入力情報]");
        log.info("from=[{}:{}]", request.getRemoteAddr(), request.getRemotePort());
        log.info("URL=[{}]", request.getRequestURL().toString());
        log.info("Method=[{}]", request.getMethod());

        // リクエストヘッダ出力
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                log.info(" Header  key=[{}]/value=[{}]", headerName, headerValue);
            }
        }

        // リクエストパラメータ出力
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> set = parameterMap.keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String parameterName = iterator.next();
            String[] parameterValues = parameterMap.get(parameterName);
            log.info(" Parameter  key=[{}]/value={}", parameterName, Arrays.toString(parameterValues));
        }

        // ボディ出力
        try (BufferedReader bufferedReader = request.getReader(); Stream<String> stream = bufferedReader.lines();) {
            String body = stream.collect(Collectors.joining(CRLF));
            log.info(" Body=[{}]", body);
        }
    }

    /**
     * 
     * @param response
     * @param responseFileName
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private void setResponse(HttpServletResponse response, String responseFileName)
            throws ParserConfigurationException, SAXException, IOException {
        log.info("<------------------------ [出力情報]");

        Path responseFile = Paths.get(responseFileName);
        log.info("読み込みファイル=[{}]", responseFile.toAbsolutePath());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(responseFile.toFile());
        Element responseElement = document.getDocumentElement();

        // ステータスコード
        int status = 0;
        NodeList statusList = responseElement.getElementsByTagName(STATUS_TAG_NAME);
        if (statusList.getLength() >= 1) {
            Element statusElement = (Element) statusList.item(0);
            String statusText = statusElement.getTextContent();
            status = Integer.parseInt(statusText);
        } else {
            status = HttpStatus.OK.value();
        }
        log.info("status=[{}]", status);
        response.setStatus(status);

        // ヘッダー
        NodeList headerList = responseElement.getElementsByTagName(HEADER_TAG_NAME);
        for (int idx = 0; idx < headerList.getLength(); idx++) {
            Element headerElement = (Element) headerList.item(idx);

            NodeList nameList = headerElement.getElementsByTagName(HEADER_NAME_TAG_NAME);
            NodeList valueList = headerElement.getElementsByTagName(HEADER_VALUE_TAG_NAME);
            if (nameList.getLength() == 0 || valueList.getLength() == 0) {
                // name要素もしくはvalue要素が存在しない場合はスキップ.
                continue;
            }

            Element nameElement = (Element) nameList.item(0);
            Element valueElement = (Element) valueList.item(0);
            String name = nameElement.getTextContent();
            String value = valueElement.getTextContent();

            if (CONTENT_TYPE_LOWER.equals(name.toLowerCase())) {
                response.setContentType(value);
            } else {
                response.setHeader(name, value);
            }

            log.info(" Header key=[{}]/value=[{}]", name, value);
        }

        // ボディ
        NodeList bodyList = responseElement.getElementsByTagName(BODY_TAG_NAME);
        if (bodyList.getLength() >= 1) {
            Element bodyElement = (Element) bodyList.item(0);
            String body = bodyElement.getTextContent();
            log.info("body=[{}]", body);

            try (BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));) {
                bufferedWriter.write(body);
                bufferedWriter.flush();
            }
        }
    }
}
