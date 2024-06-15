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
    private final DefaultService defaultService;

    public DefaultController(@Autowired DefaultService defaultService) {
        this.defaultService = defaultService;
    }

    /**
     * デフォルト.<br>
     * @param fileName
     */
    @RequestMapping(path = { "/default/{name}" }, method = {})
    public void defaultCall(@PathVariable(name = "name") String fileName) {
        defaultService.execute(fileName);
    }

    /**
     * デフォルト.<br>
     */
    @RequestMapping(path = { "/default" }, method = {})
    public void defaultCall() {
        defaultService.execute(null);
    }

}
