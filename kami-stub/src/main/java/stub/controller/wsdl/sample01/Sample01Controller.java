package stub.controller.wsdl.sample01;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Slf4j
@Controller
public class Sample01Controller {

    /** . */
    private Sample01Service service;

    /**
     * 
     * @param service
     */
    public Sample01Controller(@Autowired Sample01Service service) {
        this.service = service;
    }

    /**
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    @PostMapping(path = { "/wsdl/sample01/clearPDFAutoConverterEX" }, consumes = { MediaType.TEXT_XML_VALUE })
    public void clearPDFAutoConverterEX(HttpServletRequest request, HttpServletResponse response) throws Exception {
        service.clearPDFAutoConverterEX(request, response);
    }

    /**
     * 
     * @param request
     * @param response
     * @param sleepTime
     * @throws Exception
     */
    @RequestMapping(path = { "/wsdl/sample01/clearPDFAutoConverterEX/sleep/{time}" }, method = {})
    public void sleepClearPDFAutoConverterEX(HttpServletRequest request, HttpServletResponse response,
            @PathVariable(name = "time") long sleepTime) throws Exception {
        service.clearPDFAutoConverterEX(request, response, sleepTime);
    }

    /**
     * 
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(value = { Exception.class })
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e, WebRequest request) {
        log.error("例外発生！！", e);
        return new StringBuilder()//
                .append("<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">") //
                .append("<env:Body>") //
                .append("<env:Fault>") //
                .append("<faultcode>env:Sample01ServiceError</faultcode>") //
                .append("<faultstring>Handle Exception</faultstring>") //
                .append("<faultactor>http://schemas.xmlsoap.org/soap/envelope</faultactor>") //
                .append("<detail>") //
                .append("<stackTrace>XXX</stackTrace>") //
                .append("</detail>") //
                .append("</env:Fault>") //
                .append("</env:Body>") //
                .toString();
    }

}
