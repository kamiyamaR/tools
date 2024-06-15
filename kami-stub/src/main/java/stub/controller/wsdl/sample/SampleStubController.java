package stub.controller.wsdl.sample;

import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
public class SampleStubController {

    /** . */
    private final SampleStubService service;

    /**
     * 
     * @param service
     */
    public SampleStubController(@Autowired SampleStubService service) {
        this.service = service;
    }

    /**
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    @PostMapping(path = { "/wsdl/sample/operation1" }, consumes = { MediaType.TEXT_XML_VALUE }, produces = {
            MediaType.TEXT_XML_VALUE })
    public void operation1(HttpServletRequest request, HttpServletResponse response) throws Exception {
        service.operation1(request, response);
    }

    /**
     * 
     * @param request
     * @param response
     * @param sleepTime
     * @throws Exception
     */
    @PostMapping(path = { "/wsdl/sample/operation1/sleep/{time}" }, consumes = {
            MediaType.TEXT_XML_VALUE }, produces = { MediaType.TEXT_XML_VALUE })
    public void operation1(HttpServletRequest request, HttpServletResponse response,
            @PathVariable(name = "time") long sleepTime) throws Exception {
        service.operation1(request, response, sleepTime);
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
        Throwable cause = ExceptionUtils.getRootCause(e);
        if (Objects.isNull(cause)) {
            cause = e;
        }
        return new StringBuilder()//
                .append("<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">") //
                .append("<env:Body>") //
                .append("<env:Fault>") //
                .append("<faultcode>env:SampleStubError</faultcode>") //
                .append("<faultstring>").append(cause.getMessage()).append("</faultstring>") //
                .append("<faultactor>http://schemas.xmlsoap.org/soap/envelope</faultactor>") //
                .append("<detail>") //
                .append("<stackTrace>XXX</stackTrace>") //
                .append("</detail>") //
                .append("</env:Fault>") //
                .append("</env:Body>") //
                .append("</env:Envelope>") //
                .toString();
    }

}
