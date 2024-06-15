package stub.controller.repository.http.javanethttp.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "stub.controller.repository.http.javanethttp.prop")
public class RepositoryAccessorJavaNetHttpProperties {

    private final long connectTimeout;
    private final long socketTimeout;

}
