package zagnitko.artem.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservice for keyword search volume calculation.
 * @author htshame@gmail.com
 */
@SpringBootApplication
public class SearchVolumeScoreApplication {

    /**
     * Application starting point.
     * @param args - args.
     */
    public static void main(String[] args) {
        SpringApplication.run(SearchVolumeScoreApplication.class, args);
    }
}
