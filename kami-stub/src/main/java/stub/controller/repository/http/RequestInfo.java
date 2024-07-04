package stub.controller.repository.http;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class RequestInfo {
    private String method;
    private String url;
    private Map<String, List<String>> headers;
    private byte[] bodyByteArray;
}
