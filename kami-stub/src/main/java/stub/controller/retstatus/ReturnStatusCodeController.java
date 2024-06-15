package stub.controller.retstatus;

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
public class ReturnStatusCodeController {

    /** . */
    private final ReturnStatusCodeService returnStatusCodeService;

    public ReturnStatusCodeController(@Autowired ReturnStatusCodeService returnStatusCodeService) {
        this.returnStatusCodeService = returnStatusCodeService;
    }

    /**
     * 
     * @param statusCode
     */
    @RequestMapping(path = { "/retStatus/{status}" }, method = {})
    public void returnStatusCodeCall(@PathVariable(name = "status") String statusCode) {
        this.returnStatusCodeService.execute(statusCode);
    }

}
