package tool.controller.api001;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "tool.common", "tool.controller.api001" })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class CollectUserAgentTestConfiguration {
}
