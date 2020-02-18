package zagnitko.artem.test.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import zagnitko.artem.test.dto.autocomplete.AutocompleteSearchResultDto;
import zagnitko.artem.test.dto.autocomplete.SuggestionDto;
import zagnitko.artem.test.exception.AmazonApiGatewayException;
import zagnitko.artem.test.exception.SearchVolumeServiceException;
import zagnitko.artem.test.gateway.AmazonApiGateway;

/**
 * Search volume service.
 * @author htshame@gmail.com
 */
@Component
public class SearchVolumeService {

    private static final Logger LOG = LoggerFactory.getLogger(SearchVolumeService.class);

    private static final int HUNDRED_PERCENT = 100;

    private final AmazonApiGateway amazonApiGateway;

    @Value("${search.volume.execution.timeout}")
    private int timeout;

    @Value("${algorithm.weight.coefficient.spelling.corrected}")
    private int spellingCorrectedWeightCoefficient;

    @Value("${algorithm.weight.coefficient.blacklisted}")
    private int blacklistedWeightCoefficient;

    /**
     * Constructor.
     * @param amazonApiGateway - Amazon API gateway.
     */
    @Autowired
    public SearchVolumeService(AmazonApiGateway amazonApiGateway) {
        this.amazonApiGateway = amazonApiGateway;
    }

    /**
     * Get search volume score for given keyword with timeout.
     * @param keyword - keyword to get score for.
     * @return search volume score.
     * @throws SearchVolumeServiceException - service level exception.
     */
    public int getSearchVolumeScoreWithTimeout(String keyword) throws SearchVolumeServiceException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Object> task = () -> getKeywordScore(keyword);
        Future<Object> future = executor.submit(task);
        Integer result;
        try {
            result = (Integer) future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            LOG.error("Error executing task to calculate search volume for keyword {}", keyword, e);
            throw new SearchVolumeServiceException("Error executing task to calculate search volume for keyword "
                    + keyword, e);
        } finally {
            future.cancel(true);
        }
        return result;
    }

    /**
     * Get score for given keyword.
     *
     * This method returns the search volume score for provided keyword.
     * It makes two bunches of Amazon API calls: with and without site-variant parameter
     * (API returns different suggestions when site-variant is present and when it's not).
     * To find the appropriate suggestions, user's actions are being emulated: search starts with the first symbol of
     * the keyword and continues by adding one next character at a time until the search parameter will represent itself
     * the whole keyword.
     * After that, each API call result is getting processed and the final overall search volume score is calculated as
     * a sum of scores provided by EstimationService#calculateSearchVolume. At the end this sum of scores is divided
     * by two (since there were two bunches of API calls) keywords lengths, multiplied by 100% and rounded to integer.
     *
     * @param keyword - keyword to get score for.
     * @return score.
     */
    //todo proper HTTP connection pool is in order.
    //todo proper parallelism settings are in order (like -Djava.util.concurrent.ForkJoinPool.common.parallelism).
    public Integer getKeywordScore(String keyword) {
        List<String> prefixes = IntStream.range(0, keyword.length()).mapToObj(i -> keyword.substring(0, i + 1))
                .collect(Collectors.toList());
        List<AutocompleteSearchResultDto> autocompleteList = new ArrayList<>();
        //first group of Amazon API calls
        prefixes.parallelStream()
                .map((String prefix) -> searchByPrefix(prefix, true))
                .forEachOrdered(autocompleteList::add);
        //second group of Amazon API calls
        prefixes.parallelStream()
                .map((String prefix) -> searchByPrefix(prefix, false))
                .forEachOrdered(autocompleteList::add);

        float score = 0;
        for (AutocompleteSearchResultDto autocomplete : autocompleteList) {
            if (Objects.isNull(autocomplete)) {
                continue;
            }
            String prefix = autocomplete.getPrefix();
            List<SuggestionDto> suggestions = autocomplete.getSuggestions();
            for (int suggestionNumber = 0; suggestionNumber < suggestions.size(); suggestionNumber++) {
                SuggestionDto suggestion = suggestions.get(suggestionNumber);
                if (keyword.equalsIgnoreCase(suggestion.getValue())) {
                    //invoke the algorithm.
                    score += calculateSearchVolume(keyword.length(), prefix.length(), suggestionNumber + 1,
                            suggestions.size(), suggestion.getSpellCorrected(), suggestion.getBlackListed());
                    break;
                }
            }
        }
        return Math.round((score * HUNDRED_PERCENT) / (keyword.length() * 2));
    }

    /**
     * Calculate search volume score.
     *
     * Here's the actual volume score calculation algorithm. The idea behind it is as follows:
     * This method could be invoked by EstimationService#processResult if there's the exact match of keyword and
     * suggestion.
     * At first it calculates the symbolsTypedFactor - the variable based on the number of characters typed before
     * the keyword match appeared in suggestions list. It's calculated simply as a number of typed
     * characters divided by overall keyword length.
     * Secondly it calculates the keywordPositionFactor - the variable based on the position of matched suggestion
     * in the list of suggestions (I assume that the order of suggestions is indeed significant).
     * After that it multiplies symbolsTypedFactor and keywordPositionFactor. Result of this multiplication will be
     * the search volume score.
     * Lately, it checks whether the search prefix is spell corrected or not and also checks whether the search
     * prefix is blacklisted or not. If it does, then the appropriate coefficient s are added to the final result.
     *
     * @param keywordLength- keyword length.
     * @param prefixNumber - prefix for which Amazon returned the exact match.
     * @param suggestionNumber - position of keyword in the list of suggestions.
     * @param suggestionsNumber - total number of suggestions.
     * @param isSpellCorrected - was the keyword treated as misspelling or not.
     * @param isBlacklisted - is the item blacklisted or not.
     * @return search volume score.
     */
    private float calculateSearchVolume(int keywordLength, int prefixNumber, int suggestionNumber,
                                        int suggestionsNumber, Boolean isSpellCorrected, Boolean isBlacklisted) {
        float result = 0;
        if (prefixNumber == 0) {
            return result;
        }
        double charactersTypedFactor = 1;
        if (suggestionNumber != 1) {
            charactersTypedFactor = (double) (keywordLength - prefixNumber + 1) / (double) keywordLength;
        }

        double keywordPositionFactor = (double) (suggestionsNumber - suggestionNumber + 1) / (double) suggestionsNumber;
        result = (float) (charactersTypedFactor * keywordPositionFactor);

        if (isSpellCorrected) {
            result = result * spellingCorrectedWeightCoefficient / HUNDRED_PERCENT;
        }
        if (isBlacklisted) {
            result = result * blacklistedWeightCoefficient / HUNDRED_PERCENT;
        }
        return result;
    }

    /**
     * Search Amazon autocomplete API by prefix.
     * @param prefix - prefix to search by.
     * @return search results.
     */
    private AutocompleteSearchResultDto searchByPrefix(String prefix, boolean withSiteVariant) {
        try {
            return amazonApiGateway.searchByKeyword(prefix, withSiteVariant);
        } catch (AmazonApiGatewayException e) {
            LOG.error("Error acquiring data from Amazon autocomplete API by keyword {}. Message is {}", prefix,
                    e.getMessage());
            return null;
        }
    }
}
