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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import stub.common.online.AbstractService;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Service
public class DefaultService extends AbstractService<String, Void> {
    private static final String CRLF = "\r\n";

    private static final String DEFAULT_FILE_NAME = "default";
    private static final String EXTENSION = ".txt";

    private static final String STATUS_TAG_NAME = "status";
    private static final String HEADERS_TAG_NAME = "headers";
    private static final String HEADER_TAG_NAME = "header";
    private static final String HEADER_NAME_TAG_NAME = "name";
    private static final String HEADER_VALUE_TAG_NAME = "value";
    private static final String BODY_TAG_NAME = "body";

    private static final String CONTENT_TYPE_LOWER = HttpHeaders.CONTENT_TYPE.toLowerCase();

    @Override
    public Void process(String fileName) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();

        String responseFileName = Paths.get((Objects.nonNull(fileName) ? fileName : DEFAULT_FILE_NAME) + EXTENSION)
                .getFileName().toString();

        try {
            printRequest(request);
            setResponse(response, responseFileName);
        } catch (Exception e) {
            log.error("例外発生！", e);
            try {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
            } catch (Exception e1) {
                log.error("例外発生！！", e1);
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }

        return null;
    }

    /**
     * リクエスト情報を出力する.<br>
     * @param request
     * @throws IOException
     */
    private void printRequest(HttpServletRequest request) throws Exception {
        log.info("------------------------> [入力情報]");

        // サーブレットコンテキスト情報
        String contentType = request.getContentType();
        //log.info(" changeSessionId=[{}]", request.changeSessionId()); // 例外が起きる
        if (request.isAsyncStarted()) {
            log.info(" AsyncContext=[{}]", request.getAsyncContext());
        }
        {
            Enumeration<String> attributeNames = request.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                log.info(" Attribute Names=[{}]/value=[{}]", attributeName, request.getAttribute(attributeName));
            }
        }
        log.info(" AuthType=[{}]", request.getAuthType());
        log.info(" CharacterEncoding=[{}]", request.getCharacterEncoding());
        log.info(" Class=[{}]", request.getClass());
        log.info(" ContentLength=[{}]", request.getContentLength());
        log.info(" ContentLengthLong=[{}]", request.getContentLengthLong());
        log.info(" ContentType=[{}]", contentType);
        log.info(" ContextPath=[{}]", request.getContextPath());
        log.info(" Cookies=[{}]", Arrays.toString(request.getCookies()));
        log.info(" DispatcherType=[{}]", request.getDispatcherType());
        {
            HttpServletMapping mapping = request.getHttpServletMapping();
            log.info(" HttpServletMapping=[{}]", mapping);
            log.info(" HttpServletMapping.Class=[{}]", mapping.getClass());
            log.info(" HttpServletMapping.MappingMatch=[{}]", mapping.getMappingMatch());
            log.info(" HttpServletMapping.MatchValue=[{}]", mapping.getMatchValue());
            log.info(" HttpServletMapping.Pattern=[{}]", mapping.getPattern());
            log.info(" HttpServletMapping.ServletName=[{}]", mapping.getServletName());
        }
        log.info(" LocalAddr=[{}]", request.getLocalAddr());
        log.info(" Locale=[{}]", request.getLocale());
        {
            Enumeration<Locale> locales = request.getLocales();
            log.info(" Locale=[{}]", locales);
            while (locales.hasMoreElements()) {
                Locale locale = locales.nextElement();
                log.info(" Locales=[{}]", locale);
            }
        }
        log.info(" LocalName=[{}]", request.getLocalName());
        log.info(" LocalPort=[{}]", request.getLocalPort());
        if (Objects.nonNull(contentType) && contentType.toLowerCase().startsWith("multipart/")) {
            log.info(" Parts=[{}]", request.getParts());
        }
        log.info(" Method=[{}]", request.getMethod());
        log.info(" PathInfo=[{}]", request.getPathInfo());
        log.info(" PathTranslated=[{}]", request.getPathTranslated());
        log.info(" ProtocolRequestId=[{}]", request.getProtocolRequestId());
        log.info(" QueryString=[{}]", request.getQueryString());
        log.info(" RemoteAddr=[{}]", request.getRemoteAddr());
        log.info(" RemoteHost=[{}]", request.getRemoteHost());
        log.info(" RemotePort=[{}]", request.getRemotePort());
        log.info(" RemoteUser=[{}]", request.getRemoteUser());
        log.info(" RequestedSessionId=[{}]", request.getRequestedSessionId());
        log.info(" RequestId=[{}]", request.getRequestId());
        log.info(" RequestURI=[{}]", request.getRequestURI());
        log.info(" RequestURL=[{}]", request.getRequestURL());
        log.info(" Scheme=[{}]", request.getScheme());
        log.info(" ServerName=[{}]", request.getServerName());
        log.info(" ServerPort=[{}]", request.getServerPort());
        {
            ServletConnection connection = request.getServletConnection();
            log.info(" ServletConnection=[{}]", connection);
            log.info(" ServletConnection.Class=[{}]", connection.getClass());
            log.info(" ServletConnection.ConnectionId=[{}]", connection.getConnectionId());
            log.info(" ServletConnection.Protocol=[{}]", connection.getProtocol());
            log.info(" ServletConnection.ProtocolConnectionId=[{}]", connection.getProtocolConnectionId());
            log.info(" ServletConnection.isSecure=[{}]", connection.isSecure());
        }
        {
            ServletContext context = request.getServletContext();
            log.info(" ServletContext=[{}]", context);
            log.info(" ServletContext.Class=[{}]", context.getClass());
            Enumeration<String> attributeNames = context.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                log.info(" ServletContext.Attribute Names=[{}]/value=[{}]=[{}]", name, context.getAttribute(name));

            }
        }
        {
            HttpSession session = request.getSession();
            log.info(" Session=[{}]", session);
            log.info(" Session.Class=[{}]", session.getClass());
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                log.info(" Session.Attribute Names=[{}]/value=[{}]=[{}]", name, session.getAttribute(name));
            }
            log.info(" Session.CreationTime=[{}]", session.getCreationTime());
            log.info(" Session.Id=[{}]", session.getId());
            log.info(" Session.LastAccessedTime=[{}]", session.getLastAccessedTime());
            log.info(" Session.MaxInactiveInterval=[{}]", session.getMaxInactiveInterval());
            {
                ServletContext context = session.getServletContext();
                Enumeration<String> contextAttributeNames = context.getAttributeNames();
                while (contextAttributeNames.hasMoreElements()) {
                    String name = contextAttributeNames.nextElement();
                    log.info(" Session.ServletContext.Attribute Names=[{}]/value=[{}]=[{}]", name,
                            context.getAttribute(name));
                }
            }
            log.info(" Session.ServletContext=[{}]", session.getServletContext());
        }
        log.info(" TrailerFields=[{}]", request.getTrailerFields());
        log.info(" UserPrincipal=[{}]", request.getUserPrincipal());

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
     * レスポンス情報を作成する.<br>
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
        NodeList statusList = responseElement.getElementsByTagName(STATUS_TAG_NAME);
        if (Objects.nonNull(statusList) && statusList.getLength() >= 1) {
            Node statusNode = statusList.item(0);
            String statusText = statusNode.getTextContent();
            int status = Integer.parseInt(statusText);
            log.info("status=[{}]", status);
            response.setStatus(status);
        } else {
            log.info("ステータスコードの設定なし。 [200 OK]を設定。");
            response.setStatus(HttpStatus.OK.value());
        }

        // ヘッダー
        NodeList headersList = responseElement.getElementsByTagName(HEADERS_TAG_NAME);
        if (Objects.nonNull(headersList) && headersList.getLength() >= 1) {
            Element headersElement = (Element) headersList.item(0);
            NodeList headerList = headersElement.getElementsByTagName(HEADER_TAG_NAME);
            if (Objects.nonNull(headerList)) {
                for (int idx = 0; idx < headerList.getLength(); idx++) {
                    Element headerElement = (Element) headerList.item(idx);

                    NodeList nameList = headerElement.getElementsByTagName(HEADER_NAME_TAG_NAME);
                    NodeList valueList = headerElement.getElementsByTagName(HEADER_VALUE_TAG_NAME);
                    if ((Objects.isNull(nameList) || nameList.getLength() <= 0)
                            || (Objects.isNull(valueList) || valueList.getLength() <= 0)) {
                        // name要素もしくはvalue要素が存在しない場合はスキップ.
                        continue;
                    }

                    Node nameNode = nameList.item(0);
                    Node valueNode = valueList.item(0);
                    String name = nameNode.getTextContent();
                    String value = valueNode.getTextContent();

                    if (CONTENT_TYPE_LOWER.equals(name.toLowerCase())) {
                        response.setContentType(value);
                    } else {
                        response.setHeader(name, value);
                    }

                    log.info(" Header key=[{}]/value=[{}]", name, value);
                }
            } else {
                log.info("ヘッダーの設定なし。");
            }
        } else {
            log.info("ヘッダーの設定なし。");
        }

        // ボディ
        NodeList bodyList = responseElement.getElementsByTagName(BODY_TAG_NAME);
        if (Objects.nonNull(bodyList) && bodyList.getLength() >= 1) {
            Node bodyNode = bodyList.item(0);
            String body = bodyNode.getTextContent();
            log.info("body=[{}]", body);

            try (BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));) {
                bufferedWriter.write(body);
                bufferedWriter.flush();
            }
        } else {
            log.info("ボディの設定なし。");
        }
    }

}
