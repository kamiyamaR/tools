package tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

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
            createSpringApplication().run(args);
        } catch (Exception e) {
            log.error("例外発生.", e);
        }
    }

    /**
     * 
     * @return
     */
    private static SpringApplication createSpringApplication() {
        return new SpringApplicationBuilder(Main.class).web(WebApplicationType.SERVLET).build();
    }
}
