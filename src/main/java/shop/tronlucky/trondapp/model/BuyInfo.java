package shop.tronlucky.trondapp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BuyInfo {
    private String id;
    private int roundNumber;
    private String buyTime;
    private String buyAddress;
    private int buyAmount;
    private int luckyAmount;
}
