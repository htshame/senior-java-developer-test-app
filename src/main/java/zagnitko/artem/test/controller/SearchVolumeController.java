package zagnitko.artem.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import zagnitko.artem.test.dto.KeywordScoreDto;
import zagnitko.artem.test.exception.SearchVolumeControllerException;
import zagnitko.artem.test.exception.SearchVolumeServiceException;
import zagnitko.artem.test.service.SearchVolumeService;

/**
 * Search volume REST controller.
 * @author htshame@gmail.com
 */
@RestController
@RequestMapping("/volume")
public class SearchVolumeController {

    private static final Logger LOG = LoggerFactory.getLogger(SearchVolumeController.class);

    private final SearchVolumeService searchVolumeService;

    /**
     * Constructor.
     * @param searchVolumeService - estimation service.
     */
    @Autowired
    public SearchVolumeController(SearchVolumeService searchVolumeService) {
        this.searchVolumeService = searchVolumeService;
    }

    /**
     * Get keyword search volume score.
     * @param keyword - keyword to get score for.
     * @return keyword and score.
     * @throws SearchVolumeControllerException - client side exception.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeywordScoreDto> getKeywordEstimation(@RequestParam String keyword)
            throws SearchVolumeControllerException {
        LOG.info("Entering GET /volume?keyword={keyword}. Keyword is {}", keyword);
        try {
            int score = searchVolumeService.getSearchVolumeScoreWithTimeout(keyword);
            LOG.info("Estimation of keyword {} was acquired successfully", keyword);
            return ResponseEntity.ok(new KeywordScoreDto(keyword, score));
        } catch (SearchVolumeServiceException e) {
            LOG.error("Error getting estimation for keyword {}. Error message: {}", keyword, e.getMessage());
            throw new SearchVolumeControllerException(e.getMessage());
        }
    }
}
