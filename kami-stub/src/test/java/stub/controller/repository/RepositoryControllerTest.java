package stub.controller.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import stub.StubStart;
import stub.support.LocalHttpServer;

@SpringBootTest(classes = StubStart.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "server.address=127.0.0.1", "server.tomcat.accesslog.enabled=false",
                "server.tomcat.basedir=target/test-tomcat" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RepositoryControllerTest {

    private static final byte[] BODY = "ローカル中継応答".getBytes(StandardCharsets.UTF_8);
    private static LocalHttpServer upstream;

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    @DynamicPropertySource
    static void configureUpstream(DynamicPropertyRegistry registry) throws IOException {
        upstream = new LocalHttpServer(201, BODY, false);
        registry.add("stub.controller.repository.prop.url", upstream::url);
    }

    @AfterAll
    static void closeUpstream() {
        if (upstream != null) {
            upstream.close();
        }
    }

    @Test
    void forwardsGetPathHeadersAndResponse() throws Exception {
        HttpResponse<byte[]> response = request("GET", null);

        assertResponse(response);
        var received = upstream.takeRequest();
        assertEquals("GET", received.method());
        assertEquals("/artifact", received.uri().toString());
        assertEquals("proxy-value", received.headers().getFirst("X-Client"));
    }

    @Test
    void forwardsUtf8PostBody() throws Exception {
        byte[] body = "中継する日本語本文".getBytes(StandardCharsets.UTF_8);
        HttpResponse<byte[]> response = request("POST", body);

        assertResponse(response);
        var received = upstream.takeRequest();
        assertEquals("POST", received.method());
        assertArrayEquals(body, received.body());
        assertEquals("text/plain;charset=UTF-8", received.headers().getFirst("Content-Type"));
    }

    private HttpResponse<byte[]> request(String method, byte[] body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + this.port
                + "/repository/libget/artifact")).timeout(Duration.ofSeconds(10))
                .header("X-Client", "proxy-value").header("Content-Type", "text/plain;charset=UTF-8");
        if ("GET".equals(method)) {
            builder.GET();
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofByteArray(body));
        }
        return this.client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private void assertResponse(HttpResponse<byte[]> response) {
        assertEquals(201, response.statusCode());
        assertArrayEquals(BODY, response.body());
        assertEquals(List.of("first", "second"), response.headers().allValues("X-Upstream"));
        assertEquals("application/octet-stream", response.headers().firstValue("Content-Type").orElseThrow());
    }
}
