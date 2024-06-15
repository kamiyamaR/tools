package stub.controller.repository.http.javanethttp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import stub.common.http.javanethttp.JavaNetHttpAccessorBase;
import stub.controller.repository.http.RepositoryAccessor;
import stub.controller.repository.http.RequestInfo;
import stub.controller.repository.http.ResponseInfo;
import stub.controller.repository.http.javanethttp.prop.RepositoryAccessorJavaNetHttpProperties;

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

        BodyPublisher publisher = null;
        if (ArrayUtils.isNotEmpty(input.getBodyByteArray())) {
            publisher = BodyPublishers.ofByteArray(input.getBodyByteArray());
        } else {
            publisher = BodyPublishers.noBody();
        }
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(input.getUrl()));
        requestBuilder.method(input.getMethod(), publisher);

        for (Entry<String, List<String>> entry : input.getHeaders().entrySet()) {
            // 設定出来ないヘッダーを除外する
            switch (entry.getKey().toLowerCase()) {
            case "host":
            case "connection":
                continue;
            default:
                break;
            }

            for (String value : entry.getValue()) {
                requestBuilder.header(entry.getKey(), value);
            }
        }

        requestBuilder.timeout(Duration.ofMillis(prop.getSocketTimeout())); // ソケットタイムアウト
        HttpRequest request = requestBuilder.build();

        HttpResponse<byte[]> response = send(request, BodyHandlers.ofByteArray());

        Map<String, List<String>> responseHeaders = new HashMap<>();
        for (Entry<String, List<String>> entry : response.headers().map().entrySet()) {
            responseHeaders.put(entry.getKey(), entry.getValue());
        }

        ResponseInfo output = new ResponseInfo();
        output.setVersion(response.version().name());
        output.setStatusCode(response.statusCode());
        output.setHeaders(responseHeaders);
        output.setBodyByteArray(response.body());
        return output;
    }

}
