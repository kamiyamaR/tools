package tool.controller.api001;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.extern.slf4j.Slf4j;
import test.base.BaseUT;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@SpringBootTest(classes = { CollectUserAgentTestConfiguration.class })
class CollectUserAgentTest extends BaseUT {

    @Autowired
    private CollectUserAgent target;

    @MockBean
    private CollectUserAgentDbAccessFunction collectUserAgentDbAccessFunction;

    @Test
    void test0001() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = null;
        String secChUaMobile = "?0";
        String secChUaModel = "\"\"";
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = "\"10.0.0\"";
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
    }

    @Test
    void test0002() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = null;
        String secChUaModel = "\"\"";
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = "\"10.0.0\"";
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
    }

    @Test
    void test0003() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = "?0";
        String secChUaModel = null;
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = "\"10.0.0\"";
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
    }

    @Test
    void test0004() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = "?0";
        String secChUaModel = "\"\"";
        String secChUaPlatform = null;
        String secChUaPlatformVersion = "\"10.0.0\"";
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
    }

    @Test
    void test0005() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = "?0";
        String secChUaModel = "\"\"";
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = null;
        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.PERMANENT_REDIRECT, result.getStatusCode());
        assertEquals(1, result.getHeaders().get("Accept-CH").size());
        assertEquals("Sec-CH-UA, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version",
                result.getHeaders().get("Accept-CH").get(0));
        assertEquals(1, result.getHeaders().get("Location").size());
        assertEquals("/api001", result.getHeaders().get("Location").get(0));
        assertNull(result.getBody());
    }

    @Test
    void test0006() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        String secChUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
        String secChUaMobile = "?0";
        String secChUaModel = "\"\"";
        String secChUaPlatform = "\"Windows\"";
        String secChUaPlatformVersion = "\"10.0.0\"";

        doReturn(null).when(this.collectUserAgentDbAccessFunction).selectPostgresqlDbUserAgentInfByUserAgent(any());
        doReturn(null).when(this.collectUserAgentDbAccessFunction).selectPostgresqlDbUserAgentInfByUserAgent(any());
        doNothing().when(this.collectUserAgentDbAccessFunction).registDb(any());

        ResponseEntity<CollectUserAgentResponseBody> result = this.target.exec(userAgent, secChUa, secChUaMobile,
                secChUaModel, secChUaPlatform, secChUaPlatformVersion);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("0000", result.getBody().getDetailCode());
    }
}
