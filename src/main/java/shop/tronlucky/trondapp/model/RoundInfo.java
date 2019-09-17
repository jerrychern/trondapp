package shop.tronlucky.trondapp.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsImg() {
        return goodsImg;
    }

    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public int getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(int goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public String getSeedValue() {
        return seedValue;
    }

    public void setSeedValue(String seedValue) {
        this.seedValue = seedValue;
    }

    public String getSeedHash() {
        return seedHash;
    }

    public void setSeedHash(String seedHash) {
        this.seedHash = seedHash;
    }

    public String getBlockValue() {
        return blockValue;
    }

    public void setBlockValue(String blockValue) {
        this.blockValue = blockValue;
    }

    public String getLuckyAddress() {
        return luckyAddress;
    }

    public void setLuckyAddress(String luckyAddress) {
        this.luckyAddress = luckyAddress;
    }

    public int getLuckyIndex() {
        return luckyIndex;
    }

    public void setLuckyIndex(int luckyIndex) {
        this.luckyIndex = luckyIndex;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }
}
