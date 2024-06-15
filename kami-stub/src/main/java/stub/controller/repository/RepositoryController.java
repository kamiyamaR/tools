package stub.controller.repository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 */
@RestController
public class RepositoryController {

    /** . */
    private final LibraryGetService libraryGetService;
    /** . */
    private final LibraryDistributionService libraryDistributionService;

    /**
     * 
     * @param libraryGetService
     * @param libraryDistributionService
     */
    public RepositoryController(LibraryGetService libraryGetService,
            LibraryDistributionService libraryDistributionService) {
        this.libraryGetService = libraryGetService;
        this.libraryDistributionService = libraryDistributionService;
    }

    /**
     * 
     */
    @RequestMapping(path = { LibraryGetService.PATH + "/**" }, method = {})
    public ResponseEntity<byte[]> getLibrary() throws Exception {
        return libraryGetService.exec();
    }

    /**
     * 
     */
    @RequestMapping(value = { LibraryDistributionService.PATH + "/**" }, method = {})
    public void distributLibrary() throws Exception {
        libraryDistributionService.exec();
    }

}
