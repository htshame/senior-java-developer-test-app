package zagnitko.artem.test.dto.autocomplete;

import java.util.List;

/**
 * Object to parse Amazon autocomplete API response into.
 * @author htshame@gmail.com
 */
public class AutocompleteSearchResultDto {

    private String prefix;

    private List<SuggestionDto> suggestions;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<SuggestionDto> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<SuggestionDto> suggestions) {
        this.suggestions = suggestions;
    }
}
