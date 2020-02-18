package zagnitko.artem.test.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import zagnitko.artem.test.dto.autocomplete.AutocompleteSearchResultDto;
import zagnitko.artem.test.exception.AmazonApiGatewayException;

/**
 * Amazon API gateway.
 * @author htshame@gmail.com
 */
@Component
public class AmazonApiGateway {

    private static final Logger LOG = LoggerFactory.getLogger(AmazonApiGateway.class);

    private final RestTemplate restTemplate;

    @Value("${amazon.autocomplete.api.url}")
    private String amazonAutocompleteApiUrl;

    @Value("${amazon.autocomplete.api.param.site.variant}")
    private String amazonAutocompleteApiSiteVariantParam;

    /**
     * Constructor.
     * @param restTemplate - custom rest template bean.
     */
    public AmazonApiGateway(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Search by keyword using Amazon autocomplete API.
     * @param keyword - keyword to search by.
     * @param withSiteVariant - include site-variant parameter or not.
     * @return search result.
     * @throws AmazonApiGatewayException - is error has happened during API call.
     */
    public AutocompleteSearchResultDto searchByKeyword(String keyword, boolean withSiteVariant)
            throws AmazonApiGatewayException {
        //todo use UriComponentsBuilder
        String requestUrl = String.format(amazonAutocompleteApiUrl, keyword);
        if (withSiteVariant) {
            requestUrl += "&site-variant=" + amazonAutocompleteApiSiteVariantParam;
        }
        try {
            LOG.info("Calling GET {}", requestUrl);
            ResponseEntity<AutocompleteSearchResultDto> responseEntity = restTemplate.getForEntity(requestUrl,
                    AutocompleteSearchResultDto.class);
            LOG.info("GET {} was called successfully", requestUrl);
            return responseEntity.getBody();
        } catch (RestClientException e) {
            LOG.error("Error calling GET {}", requestUrl, e);
            throw new AmazonApiGatewayException(e);
        }
    }

}
