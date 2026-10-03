package stub.common.online.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.request.ServletWebRequest;

import stub.common.online.exception.OnlineServiceException;

class OnlineExceptionHandlerTest {

    private final OnlineExceptionHandler handler = new OnlineExceptionHandler();

    @Test
    void preservesRawStatusMultipleHeaderValuesAndBody() {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("X-Error-Info", "first");
        headers.add("X-Error-Info", "second");
        Map<String, String> body = Map.of("detail", "エラー本文");
        OnlineServiceException exception = new OnlineServiceException(499, headers, body);

        var response = this.handler.handleOnlineServiceException(exception,
                new ServletWebRequest(new MockHttpServletRequest()));

        assertEquals(499, response.getStatusCode().value());
        assertEquals(List.of("first", "second"), response.getHeaders().get("X-Error-Info"));
        assertSame(body, response.getBody());
    }

    @Test
    void acceptsAbsentHeadersAndBody() {
        var response = this.handler.handleOnlineServiceException(new OnlineServiceException(400),
                new ServletWebRequest(new MockHttpServletRequest()));

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getHeaders().isEmpty());
        assertNull(response.getBody());
    }
}
