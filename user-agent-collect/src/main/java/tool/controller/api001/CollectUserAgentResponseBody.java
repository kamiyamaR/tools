package tool.controller.api001;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 
 * @author kamiyama ryohei
 *
 */
@Data
public class CollectUserAgentResponseBody {
    @JsonProperty(value = "detail_code")
    private String detailCode;
}
