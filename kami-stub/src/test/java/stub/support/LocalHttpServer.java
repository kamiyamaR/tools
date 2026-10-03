package stub.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

/** Loopback-only upstream; no external service or additional dependency is used. */
public final class LocalHttpServer implements AutoCloseable {

    public record ReceivedRequest(String method, URI uri, Headers headers, byte[] body) {
    }

    private final HttpServer server;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final BlockingQueue<ReceivedRequest> requests = new LinkedBlockingQueue<>();
    private final CountDownLatch releaseResponse = new CountDownLatch(1);

    public LocalHttpServer(int status, byte[] body, boolean withholdResponse) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.server.setExecutor(this.executor);
        this.server.createContext("/", exchange -> {
            try (exchange) {
                this.requests.add(new ReceivedRequest(exchange.getRequestMethod(), exchange.getRequestURI(),
                        exchange.getRequestHeaders(), exchange.getRequestBody().readAllBytes()));
                if (withholdResponse) {
                    try {
                        this.releaseResponse.await(15, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                }
                exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
                exchange.getResponseHeaders().add("X-Upstream", "first");
                exchange.getResponseHeaders().add("X-Upstream", "second");
                boolean head = "HEAD".equals(exchange.getRequestMethod());
                exchange.sendResponseHeaders(status, head ? -1 : body.length);
                if (!head) {
                    exchange.getResponseBody().write(body);
                }
            }
        });
        this.server.start();
    }

    public String url() {
        return "http://127.0.0.1:" + this.server.getAddress().getPort();
    }

    public ReceivedRequest takeRequest() throws InterruptedException {
        ReceivedRequest request = this.requests.poll(5, TimeUnit.SECONDS);
        if (request == null) {
            throw new AssertionError("The local upstream did not receive a request");
        }
        return request;
    }

    @Override
    public void close() {
        this.releaseResponse.countDown();
        this.server.stop(0);
        this.executor.shutdownNow();
    }
}
