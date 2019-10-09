package shop.tronlucky.trondapp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RoundInfo {
    private String id;
    private int roundNumber;
    private String goodsName;
    private String goodsImg;
    private int goodsPrice;
    private String seedValue;
    private String seedHash;
    private String blockValue;
    private String luckyAddress;
    private int luckyIndex;
    private String beginTime;
}