package zagnitko.artem.test.exception;

/**
 * Search volume service level exception.
 * @author htshame@gmail.com
 */
public class SearchVolumeServiceException extends Exception {

    /**
     * Constructor.
     * @param message - error message.
     * @param e - thrown exception.
     */
    public SearchVolumeServiceException(String message, Exception e) {
        super(message, e);
    }
}
