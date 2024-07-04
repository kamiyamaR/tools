package stub.controller.close;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CloseController implements InitializingBean, DisposableBean {

    private final ConfigurableApplicationContext contest;
    private final boolean isClose;

    /**
     * 
     * @param contest
     */
    public CloseController(ConfigurableApplicationContext contest) {
        log.info("CloseController::コンストラクタ call. [{}]", contest, new Exception("これは例外ではありません。"));
        this.contest = contest;
        isClose = Objects.isNull(contest);
    }

    @RequestMapping(path = { "/close" }, method = {})
    public CloseResponse exec() {
        log.info("開始。", new Exception("これは例外ではありません。"));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            log.info("スレッド処理開始。");
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                log.error("例外発生！！", e);
            } finally {
                if (isClose) {
                    contest.close();
                }
            }
            log.info("スレッド処理終了。", new Exception("これは例外ではありません。"));
        });
        executorService.shutdown();
        log.info("終了。");

        CloseResponse res = new CloseResponse();
        res.setNum(1000);
        res.setStr("あいうえお");
        return res;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("CloseController::afterPropertiesSet() call.", new Exception("これは例外ではありません。"));

        contest.getBeansOfType(Object.class).forEach((id, bean) -> {
            log.info("  BEAN id=[{}], bean=[{}], class=[{}]", id, bean, bean.getClass());
        });

    }

    @Override
    public void destroy() throws Exception {
        log.info("CloseController::destroy() call.", new Exception("これは例外ではありません。"));
    }

    @Data
    public static class CloseResponse {
        private int num;
        private String str;
    }

}
