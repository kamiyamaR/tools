package stub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class StubStart {

    public static void main(String[] args) {
        try {
            log.info("スタブ起動開始.");
            createSpringApplication().run(args);
            log.info("スタブ起動完了.");
        } catch (Exception e) {
            log.error("スタブ起動で例外発生！", e);
        }
    }

    private static SpringApplication createSpringApplication() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(StubStart.class);
        return builder.build();
    }
}
