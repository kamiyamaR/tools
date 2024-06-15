package stub.controller.retstatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import stub.common.online.AbstractService;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Service
public class ReturnStatusCodeService extends AbstractService<String, Void> {

    @Override
    public Void process(String statusCode) {
        log.info("開始.");
        log.info("statusCode=[{}]", statusCode);

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();

        int status = Integer.parseInt(statusCode);
        log.info("status=[{}]", status);
        response.setStatus(status);

        String body = "{\"foundation\": \"Mozilla\", \"model\": \"box\", \"week\": 45, \"transport\": \"car\", \"month\": 7}";
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));) {
            bufferedWriter.write(body);
            bufferedWriter.flush();
        } catch (IOException e) {
            log.info("", e);
        }

        log.info("終了.");
        return null;
    }

}
