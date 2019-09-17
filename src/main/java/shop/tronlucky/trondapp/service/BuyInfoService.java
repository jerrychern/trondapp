package shop.tronlucky.trondapp.service;

import shop.tronlucky.trondapp.model.BuyInfo;

import java.util.List;

/**
 * @author Jerry Chern
 * Create Time: 2019/5/28
 */
public interface BuyInfoService {
    BuyInfo addBuyInfo(BuyInfo buyInfo);

    BuyInfo updateBuyInfo(BuyInfo buyInfo);

    List<BuyInfo> listOfBuyInfo(BuyInfo buyInfo);
}
