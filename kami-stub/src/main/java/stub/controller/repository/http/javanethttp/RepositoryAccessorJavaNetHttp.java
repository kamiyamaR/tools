package stub.controller.repository.http.javanethttp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import stub.common.http.javanethttp.JavaNetHttpAccessorBase;
import stub.controller.repository.http.RepositoryAccessor;
import stub.controller.repository.http.RequestInfo;
import stub.controller.repository.http.ResponseInfo;
import stub.controller.repository.http.javanethttp.prop.RepositoryAccessorJavaNetHttpProperties;

@Slf4j
@Component
public class RepositoryAccessorJavaNetHttp extends JavaNetHttpAccessorBase implements RepositoryAccessor {

    private final RepositoryAccessorJavaNetHttpProperties prop;

    public RepositoryAccessorJavaNetHttp(RepositoryAccessorJavaNetHttpProperties prop) {
        super(HttpClient.newBuilder() //
                .connectTimeout(Duration.ofMillis(prop.getConnectTimeout())) // コネクションタイムアウト
                .build());
        this.prop = prop;
    }

    @Override
    public ResponseInfo execute(RequestInfo input) throws Exception {

        Builder requestBuilder = HttpRequest.newBuilder() //
                .uri(URI.create(input.getUrl())) // 接続先
                .method(input.getMethod(), // リクエストメソッド
                        ArrayUtils.isNotEmpty(input.getBodyByteArray()) // 
                                ? BodyPublishers.ofByteArray(input.getBodyByteArray()) // リクエストボディ
                                : BodyPublishers.noBody()) // 
                .timeout(Duration.ofMillis(prop.getSocketTimeout())); // ソケットタイムアウト

        if (Objects.nonNull(input.getHeaders())) {
            input.getHeaders().forEach((key, values) -> {
                switch (key.toLowerCase()) {
                case "host":
                case "connection":
                    break;
                default:
                    values.forEach(value -> requestBuilder.header(key, value));
                }
            });
        }

        HttpResponse<byte[]> response = send(requestBuilder.build(), BodyHandlers.ofByteArray());

        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
        response.headers().map().forEach((key, values) -> {
            if (key.contains(":")) {
                log.debug("除外します。key=[{}], values=[{}]", key, values); // 「:status」というヘッダがなんか悪さしてる。
                return;
            }
            responseHeaders.put(key, values);
        });

        ResponseInfo output = new ResponseInfo();
        output.setStatusCode(response.statusCode());
        output.setHeaders(responseHeaders);
        output.setBodyByteArray(response.body());
        return output;
    }

}
