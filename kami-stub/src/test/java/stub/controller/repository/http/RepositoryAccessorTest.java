package stub.controller.repository.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import stub.controller.repository.http.apachehttpclient5.RepositoryAccessorApacheHttpClient5;
import stub.controller.repository.http.javanethttp.RepositoryAccessorJavaNetHttp;
import stub.controller.repository.http.javanethttp.prop.RepositoryAccessorJavaNetHttpProperties;
import stub.support.LocalHttpServer;

@Timeout(15)
class RepositoryAccessorTest {

    private RepositoryAccessor accessor;

    @AfterEach
    void closeAccessor() throws Exception {
        if (this.accessor instanceof RepositoryAccessorJavaNetHttp javaAccessor) {
            javaAccessor.close();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void returnsStatusRepeatedHeadersAndBinaryBody(boolean apache) throws Exception {
        byte[] body = { 0, 1, (byte) 0xff, (byte) 0x80 };
        try (LocalHttpServer server = new LocalHttpServer(201, body, false)) {
            ResponseInfo response = accessor(apache).execute(request(server, "GET", null));

            assertEquals(201, response.getStatusCode());
            assertArrayEquals(body, response.getBodyByteArray());
            assertEquals(List.of("first", "second"), header(response, "X-Upstream"));
            assertEquals(List.of("application/octet-stream"), header(response, "Content-Type"));
            var received = server.takeRequest();
            assertEquals("/artifact?version=1", received.uri().toString());
            assertEquals("GET", received.method());
            assertEquals(List.of("client-value"), received.headers().get("X-Client"));
            assertEquals(server.url().substring("http://".length()), received.headers().getFirst("Host"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void forwardsUtf8PostAndPutBodies(boolean apache) throws Exception {
        byte[] body = "こんにちは、送信テスト！".getBytes(StandardCharsets.UTF_8);
        RepositoryAccessor client = accessor(apache);
        for (String method : List.of("POST", "PUT")) {
            try (LocalHttpServer server = new LocalHttpServer(200, body, false)) {
                ResponseInfo response = client.execute(request(server, method, body));

                assertEquals(200, response.getStatusCode());
                assertArrayEquals(body, response.getBodyByteArray());
                var received = server.takeRequest();
                assertEquals(method, received.method());
                assertArrayEquals(body, received.body());
                assertEquals("text/plain;charset=UTF-8", received.headers().getFirst("Content-Type"));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void headReturnsHeadersWithoutBody(boolean apache) throws Exception {
        try (LocalHttpServer server = new LocalHttpServer(200, new byte[] { 1 }, false)) {
            ResponseInfo response = accessor(apache).execute(request(server, "HEAD", null));

            assertEquals(200, response.getStatusCode());
            assertEquals(List.of("first", "second"), header(response, "X-Upstream"));
            // Apache represents an absent entity as null; Java HttpClient returns an empty array.
            if (apache) {
                assertEquals(null, response.getBodyByteArray());
            } else {
                assertArrayEquals(new byte[0], response.getBodyByteArray());
            }
            assertEquals("HEAD", server.takeRequest().method());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void preservesUpstreamErrorResponse(boolean apache) throws Exception {
        byte[] body = "not found".getBytes(StandardCharsets.UTF_8);
        try (LocalHttpServer server = new LocalHttpServer(404, body, false)) {
            ResponseInfo response = accessor(apache).execute(request(server, "GET", null));

            assertEquals(404, response.getStatusCode());
            assertArrayEquals(body, response.getBodyByteArray());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void timesOutWhenUpstreamDoesNotSendResponseHeaders(boolean apache) throws Exception {
        try (LocalHttpServer server = new LocalHttpServer(200, new byte[0], true)) {
            RepositoryAccessor client = accessor(apache);
            RequestInfo input = request(server, "GET", null);

            if (apache) {
                assertThrows(SocketTimeoutException.class, () -> client.execute(input));
            } else {
                assertThrows(HttpTimeoutException.class, () -> client.execute(input));
            }
            assertEquals("GET", server.takeRequest().method());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PUT" })
    void recalculatesRequestFramingForForwardedBody(String method) throws Exception {
        byte[] body = "転送本文".getBytes(StandardCharsets.UTF_8);
        try (LocalHttpServer server = new LocalHttpServer(200, body, false)) {
            RequestInfo input = request(server, method, body);
            input.setHeaders(Map.of("hOsT", List.of("ignored.invalid"),
                    "cOnTeNt-LeNgTh", List.of("999"), "tRaNsFeR-EnCoDiNg", List.of("chunked"),
                    "Content-Type", List.of("text/plain;charset=UTF-8"),
                    "X-Client", List.of("client-value")));

            ResponseInfo response = accessor(true).execute(input);

            assertEquals(200, response.getStatusCode());
            assertArrayEquals(body, response.getBodyByteArray());
            var received = server.takeRequest();
            assertEquals(method, received.method());
            assertArrayEquals(body, received.body());
            assertEquals(List.of(Integer.toString(body.length)), received.headers().get("Content-Length"));
            assertNull(received.headers().getFirst("Transfer-Encoding"));
            assertEquals(server.url().substring("http://".length()), received.headers().getFirst("Host"));
            assertEquals("client-value", received.headers().getFirst("X-Client"));
        }
    }

    private RepositoryAccessor accessor(boolean apache) {
        this.accessor = apache ? new RepositoryAccessorApacheHttpClient5()
                : new RepositoryAccessorJavaNetHttp(new RepositoryAccessorJavaNetHttpProperties(1000, 3000));
        return this.accessor;
    }

    private RequestInfo request(LocalHttpServer server, String method, byte[] body) {
        RequestInfo input = new RequestInfo();
        input.setUrl(server.url() + "/artifact?version=1");
        input.setMethod(method);
        input.setHeaders(Map.of("Host", List.of("ignored.invalid"), "X-Client", List.of("client-value"),
                "Content-Type", List.of("text/plain;charset=UTF-8")));
        input.setBodyByteArray(body);
        return input;
    }

    private List<String> header(ResponseInfo response, String name) {
        return response.getHeaders().entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .findFirst().orElseThrow().getValue();
    }
}
