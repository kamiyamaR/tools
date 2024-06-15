package stub.controller.sleep;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@RestController
public class SleepController {

    /** . */
    private final SleepService sleepService;

    public SleepController(@Autowired SleepService sleepService) {
        this.sleepService = sleepService;
    }

    /**
     * 
     * @param sleepTime
     * @return
     */
    @RequestMapping(path = { "/sleep/{sleepTime}" }, method = {})
    public ResponseEntity<Object> sleepCall(@PathVariable(name = "sleepTime") String sleepTime) {
        return this.sleepService.execute(sleepTime);
    }

}
