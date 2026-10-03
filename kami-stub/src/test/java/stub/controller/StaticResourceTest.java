package stub.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import stub.StubStart;

@SpringBootTest(classes = StubStart.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "server.address=127.0.0.1", "server.tomcat.accesslog.enabled=false",
                "server.tomcat.basedir=target/test-tomcat" })
class StaticResourceTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void servesExistingResourceWithoutChangingItsBytes() throws Exception {
        HttpResponse<byte[]> response = request("/redirect-giji.jsp", "GET");

        assertEquals(200, response.statusCode());
        try (var resource = getClass().getResourceAsStream("/static/redirect-giji.jsp")) {
            assertArrayEquals(resource.readAllBytes(), response.body());
        }
    }

    @Test
    void headReturnsNoBody() throws Exception {
        HttpResponse<byte[]> get = request("/redirect-giji.jsp", "GET");
        HttpResponse<byte[]> head = request("/redirect-giji.jsp", "HEAD");

        assertEquals(200, head.statusCode());
        assertEquals(0, head.body().length);
        assertEquals(get.headers().firstValue("Content-Type"), head.headers().firstValue("Content-Type"));
        assertEquals(Integer.toString(get.body().length), head.headers().firstValue("Content-Length").orElseThrow());
    }

    @Test
    void returnsNotFoundForMissingResource() throws Exception {
        assertEquals(404, request("/missing-migration-baseline-resource.txt", "GET").statusCode());
    }

    private HttpResponse<byte[]> request(String path, String method) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + this.port + path))
                .timeout(Duration.ofSeconds(10)).method(method, HttpRequest.BodyPublishers.noBody()).build();
        return this.client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}
