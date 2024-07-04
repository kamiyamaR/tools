package stub.controller.repository.http;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ResponseInfo {
    private int statusCode;
    private Map<String, List<String>> headers;
    private byte[] bodyByteArray;
}
