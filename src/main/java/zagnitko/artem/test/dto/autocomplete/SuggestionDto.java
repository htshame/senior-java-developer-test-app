package zagnitko.artem.test.dto.autocomplete;

/**
 * Object to parse suggestions returned by Amazon autocomplete API into.
 * @author htshame@gmail.com
 */
public class SuggestionDto {

    private String value;

    private Boolean spellCorrected;

    private Boolean blackListed;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getSpellCorrected() {
        return spellCorrected;
    }

    public void setSpellCorrected(Boolean spellCorrected) {
        this.spellCorrected = spellCorrected;
    }

    public Boolean getBlackListed() {
        return blackListed;
    }

    public void setBlackListed(Boolean blackListed) {
        this.blackListed = blackListed;
    }
}
