package zagnitko.artem.test.exception;

/**
 * Amazon API gateway exception.
 * @author htshame@gmail.com
 */
public class AmazonApiGatewayException extends Exception {

    /**
     * Constructor.
     * @param e - exception.
     */
    public AmazonApiGatewayException(Exception e) {
        super(e);
    }
}
