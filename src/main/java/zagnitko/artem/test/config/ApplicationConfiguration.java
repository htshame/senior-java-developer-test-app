package zagnitko.artem.test.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration.
 * @author htshame@gmail.com
 */
@Configuration
public class ApplicationConfiguration {

    @Value("${rest.template.timeout}")
    private int timeout;

    /**
     * Configure custom rest template bean.
     * @param restTemplateBuilder - rest template builder.
     * @return custom rest template.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .build();
    }
}
