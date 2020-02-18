package zagnitko.artem.test.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Object to return to client.
 * @author htshame@gmail.com
 */
@JsonPropertyOrder({"keyword", "score"}) //Just in case the order is relevant
public class KeywordScoreDto {

    @JsonProperty("Keyword")
    private String keyword;

    private int score;

    /**
     * All-args constructor.
     * @param keyword - keyword.
     * @param score - score.
     */
    public KeywordScoreDto(String keyword, int score) {
        this.keyword = keyword;
        this.score = score;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
