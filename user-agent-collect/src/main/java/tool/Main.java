package tool;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class Main {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            createApplicationContext(args);
        } catch (Exception e) {
            log.error("例外発生.", e);
        }
    }

    /**
     * 
     * @param args
     * @return
     */
    private static ConfigurableApplicationContext createApplicationContext(String[] args) {
        return new SpringApplicationBuilder(Main.class).web(WebApplicationType.SERVLET).build().run(args);
    }
}
