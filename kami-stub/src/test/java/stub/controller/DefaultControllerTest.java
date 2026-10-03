package stub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import stub.StubStart;

@SpringBootTest(classes = StubStart.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = { "server.address=127.0.0.1", "server.tomcat.accesslog.enabled=false",
                "server.tomcat.basedir=target/test-tomcat" })
class DefaultControllerTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final List<Path> responseFiles = new ArrayList<>();

    @Test
    void acceptsJsonFormWithUtf8Text() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + this.port + "/almighty/sample/1?aaa=2&bbb=test"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"intVal\":3,\"strVal\":\"こんにちは\"}", StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = this.client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, response.statusCode());
        assertEquals("", response.body());
    }

    @Test
    void rejectsJsonFormWithIntegerOverflow() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + this.port + "/almighty/sample/1?aaa=2&bbb=test"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"intVal\":2147483648,\"strVal\":\"test\"}"))
                .build();

        HttpResponse<String> response = this.client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, response.statusCode());
    }

    @AfterEach
    void deleteResponseFiles() throws Exception {
        for (Path responseFile : this.responseFiles) {
            Files.deleteIfExists(responseFile);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "/default", "/default/default" })
    void returnsDefaultResponse(String requestPath) throws Exception {
        HttpResponse<String> response = request(requestPath);

        assertEquals(200, response.statusCode());
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElseThrow());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, "
                + "Sec-CH-UA-Platform-Version, Sec-CH-UA-Full-Version-List",
                response.headers().firstValue("Accept-CH").orElseThrow());
        JSONAssert.assertEquals("""
                {"quotes":[
                  {"high":"111.18","open":"109.75","bid":"111.09","currencyPairCode":"USDJPY","ask":"111.10","low":"109.59"},
                  {"high":"121.64","open":"119.27","bid":"121.48","currencyPairCode":"EURJPY","ask":"121.50","low":"119.05"},
                  {"high":"142.60","open":"140.43","bid":"142.55","currencyPairCode":"GBPJPY","ask":"142.58","low":"140.05"}
                ]}
                """, response.body(), true);
    }

    @Test
    void returnsStatusHeadersAndUtf8BodyFromNamedFile() throws Exception {
        String requestPath = responseFile("""
                <?xml version="1.0" encoding="UTF-8"?>
                <response>
                  <status>201</status>
                  <headers>
                    <header><name>Content-Type</name><value>text/plain;charset=UTF-8</value></header>
                    <header><name>X-Test-Response</name><value>named-file</value></header>
                  </headers>
                  <body>こんにちは、スタブ！</body>
                </response>
                """);

        HttpResponse<String> response = request(requestPath);

        assertEquals(201, response.statusCode());
        assertEquals("text/plain;charset=UTF-8", response.headers().firstValue("Content-Type").orElseThrow());
        assertEquals("named-file", response.headers().firstValue("X-Test-Response").orElseThrow());
        assertEquals("こんにちは、スタブ！", response.body());
    }

    @Test
    void defaultsToOkWhenStatusIsAbsent() throws Exception {
        HttpResponse<String> response = request(responseFile("<response><body>fallback</body></response>"));

        assertEquals(200, response.statusCode());
        assertEquals("fallback", response.body());
    }

    @Test
    void returnsInternalServerErrorWhenFileIsAbsent() throws Exception {
        HttpResponse<String> response = request("/default/missing-response-" + UUID.randomUUID());

        assertEquals(500, response.statusCode());
    }

    @Test
    void returnsInternalServerErrorWhenXmlIsInvalid() throws Exception {
        HttpResponse<String> response = request(responseFile("<response><body>invalid</response>"));

        assertEquals(500, response.statusCode());
    }

    private String responseFile(String xml) throws Exception {
        // DefaultService reads named response files from the working directory.
        Path responseFile = Files.createTempFile(Path.of("."), "test-response-", ".txt");
        this.responseFiles.add(responseFile);
        Files.writeString(responseFile, xml, StandardCharsets.UTF_8);
        String fileName = responseFile.getFileName().toString();
        return "/default/" + fileName.substring(0, fileName.length() - ".txt".length());
    }

    private HttpResponse<String> request(String requestPath) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + this.port + requestPath))
                .timeout(Duration.ofSeconds(10)).GET().build();
        return this.client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
