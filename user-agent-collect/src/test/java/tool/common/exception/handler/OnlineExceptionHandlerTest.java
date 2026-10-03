package tool.common.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;

import tool.common.exception.OnlineBLogicException;
import tool.common.log.MessageLogger;

class OnlineExceptionHandlerTest {

    private OnlineExceptionHandler handler() {
        OnlineExceptionHandler handler = new OnlineExceptionHandler();
        ReflectionTestUtils.setField(handler, "messageLogger", mock(MessageLogger.class));
        return handler;
    }

    @Test
    void preservesStatusRepeatedHeadersAndBody() {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("X-Error-Info", "first");
        headers.add("X-Error-Info", "second");
        Map<String, String> body = Map.of("detail", "エラー本文");
        OnlineBLogicException exception = new OnlineBLogicException("E_API001_0001", HttpStatus.BAD_REQUEST,
                headers, body);

        var response = handler().handleException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(List.of("first", "second"), response.getHeaders().get("X-Error-Info"));
        assertSame(body, response.getBody());
    }

    @Test
    void acceptsAbsentHeadersAndBody() {
        var response = handler().handleException(new OnlineBLogicException("E_API001_0001", HttpStatus.BAD_REQUEST));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getHeaders().isEmpty());
        assertNull(response.getBody());
    }
}
