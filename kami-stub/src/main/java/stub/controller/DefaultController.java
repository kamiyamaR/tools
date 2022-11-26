package stub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@RestController
public class DefaultController {

    /** . */
    @Autowired
    private DefaultService defaultService;

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
    @RequestMapping(path = { "/default" })
    public void defaultCall() {
        this.defaultService.execute(null);
    }

}
