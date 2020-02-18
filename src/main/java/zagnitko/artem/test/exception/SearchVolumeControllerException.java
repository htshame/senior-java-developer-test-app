package zagnitko.artem.test.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Search volume API exception.
 * @author htshame@gmail.com
 */
@ResponseStatus(value = HttpStatus.REQUEST_TIMEOUT)
public class SearchVolumeControllerException extends Exception {

    /**
     * Constructor.
     * @param message - message.
     */
    public SearchVolumeControllerException(String message) {
        super(message);
    }
}
