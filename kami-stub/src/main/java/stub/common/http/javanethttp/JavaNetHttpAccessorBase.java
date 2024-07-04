package stub.common.http.javanethttp;

import java.io.Closeable;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.DisposableBean;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
public abstract class JavaNetHttpAccessorBase implements Closeable, DisposableBean {

    /**
     * 
     */
    private final HttpClient client;

    /**
     * 
     * @param client
     */
    public JavaNetHttpAccessorBase(HttpClient client) {
        this.client = client;
    }

    /**
     * 
     * @param <T>
     * @param request
     * @param responseBodyHandler
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    protected <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return client.send(request, responseBodyHandler);
    }

    @Override
    public void close() {
        if (log.isDebugEnabled()) {
            log.debug("{}::close() call.", getClass().getSimpleName());
        }

        Optional<Executor> optional = client.executor();
        if (optional.isEmpty()) {
            return;
        }

        Executor executor = optional.get();
        if (!(executor instanceof ExecutorService)) {
            return;
        }

        ((ExecutorService) executor).shutdown();
    }

    @Override
    public void destroy() {
        close();
    }

}