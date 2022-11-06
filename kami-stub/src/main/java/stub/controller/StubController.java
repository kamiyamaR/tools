package stub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import stub.controller.retstatus.ReturnStatusCodeService;
import stub.controller.sleep.SleepService;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@RestController
public class StubController {

    /** . */
    @Autowired
    private DefaultService defaultService;

    /** . */
    @Autowired
    private ReturnStatusCodeService returnStatusCodeService;

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

    /**
     * 
     * @param statusCode
     */
    @RequestMapping(path = { "/retStatus/{status}" })
    public void returnStatusCodeCall(@PathVariable(name = "status") String statusCode) {
        this.returnStatusCodeService.execute(statusCode);
    }

    /**
     * デフォルト.<br>
     * @param fileName
     */
    @RequestMapping(path = { "/default/{name}" })
    public void defaultCall(@PathVariable(name = "name") String fileName) {
        this.defaultService.execute(fileName);
    }

    /**
     * デフォルト.<br>
     */
    @RequestMapping(path = { "/**" })
    public void defaultCall() {
        this.defaultService.execute(null);
    }

}
