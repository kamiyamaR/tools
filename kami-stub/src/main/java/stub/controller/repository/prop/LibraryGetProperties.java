package stub.controller.repository.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "stub.controller.repository.prop")
public class LibraryGetProperties {

    private final String url;

}
