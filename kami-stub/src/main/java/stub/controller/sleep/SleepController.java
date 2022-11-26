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
    @Autowired
    private SleepService sleepService;

    /**
     * 
     * @param sleepTime
     * @return
     */
    @RequestMapping(path = { "/sleep/{sleepTime}" })
    public ResponseEntity<Object> sleepCall(@PathVariable(name = "sleepTime") String sleepTime) {
        return this.sleepService.execute(sleepTime);
    }

}
