package stub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@SpringBootApplication
public class StubStart {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            log.info("スタブ起動開始.");
            createApplicationContext(args);
            log.info("スタブ起動完了.");
        } catch (Exception e) {
            log.error("スタブ起動で例外発生！", e);
        }
    }

    /**
     * 
     * @param args
     * @return
     */
    private static ConfigurableApplicationContext createApplicationContext(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(StubStart.class);
        SpringApplication application = builder.build();
        return application.run(args);
    }
}
