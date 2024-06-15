package stub.controller.sleep;

import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import stub.common.online.AbstractService;
import stub.common.online.exception.OnlineServiceException;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Service
public class SleepService extends AbstractService<String, ResponseEntity<Object>> {

    @Override
    public ResponseEntity<Object> process(String param) {

        long timeout = Long.parseLong(param);

        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            throw new OnlineServiceException(e, HttpStatus.BAD_REQUEST.value());
        }

        return ResponseEntity.ok().build();
    }

}
