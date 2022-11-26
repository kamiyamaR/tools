package stub.controller.almighty;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import stub.controller.almighty.form.AlmightyForm;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@RestController
public class AlmightyController {

    /**
     * 
     * @param pathStr
     * @param pathInt
     * @param queryInt
     * @param queryStr
     * @return
     */
    @RequestMapping(path = { "/almighty/{almighty}/{almighty2}" }, method = { RequestMethod.GET })
    public ResponseEntity<Void> doGet(@PathVariable(name = "almighty") String pathStr,
            @PathVariable(name = "almighty2") int pathInt, @RequestParam(name = "aaa") int queryInt,
            @RequestParam(name = "bbb") String queryStr) {
        log.info("doGet() call.");
        log.info("pathStr=[{}]", pathStr);
        log.info("pathInt=[{}]", pathInt);
        log.info("queryInt=[{}]", queryInt);
        log.info("queryStr=[{}]", queryStr);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * 
     * @param pathStr
     * @param pathInt
     * @param queryInt
     * @param queryStr
     * @param form
     * @return
     */
    @RequestMapping(path = { "/almighty/{almighty}/{almighty2}" }, method = { RequestMethod.POST }, consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE }, produces = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
    public ResponseEntity<Void> doPostFormUrlencoded(@PathVariable(name = "almighty") String pathStr,
            @PathVariable(name = "almighty2") int pathInt, @RequestParam(name = "aaa") int queryInt,
            @RequestParam(name = "bbb") String queryStr, @ModelAttribute AlmightyForm form) {
        log.info("doPostFormUrlencoded() call.");
        log.info("pathStr=[{}]", pathStr);
        log.info("pathInt=[{}]", pathInt);
        log.info("queryInt=[{}]", queryInt);
        log.info("queryStr=[{}]", queryStr);
        log.info("form=[{}]", form);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * 
     * @param pathStr
     * @param pathInt
     * @param queryInt
     * @param queryStr
     * @param form
     * @return
     */
    @RequestMapping(path = { "/almighty/{almighty}/{almighty2}" }, method = { RequestMethod.POST }, consumes = {
            MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Void> doPostJson(@PathVariable(name = "almighty") String pathStr,
            @PathVariable(name = "almighty2") int pathInt, @RequestParam(name = "aaa") int queryInt,
            @RequestParam(name = "bbb") String queryStr, @RequestBody AlmightyForm form) {
        log.info("doPostJson() call.");
        log.info("pathStr=[{}]", pathStr);
        log.info("pathInt=[{}]", pathInt);
        log.info("queryInt=[{}]", queryInt);
        log.info("queryStr=[{}]", queryStr);
        log.info("form=[{}]", form);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
