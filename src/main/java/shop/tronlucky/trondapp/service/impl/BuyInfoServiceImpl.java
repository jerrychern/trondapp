package shop.tronlucky.trondapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shop.tronlucky.trondapp.dao.DaoHelper;
import shop.tronlucky.trondapp.model.BuyInfo;
import shop.tronlucky.trondapp.service.BuyInfoService;

import java.util.List;

/**
 * @author Jerry Chern
 * Create Time:2019/5/28
 */
@Service
public class BuyInfoServiceImpl implements BuyInfoService {

    @Autowired
    private DaoHelper daoHelper;

    @Override
    public BuyInfo addBuyInfo(BuyInfo buyInfo) {
        daoHelper.insert("shop.tronlucky.trondapp.buyInfo.addBuyInfo", buyInfo);
        return buyInfo;
    }

    @Override
    public BuyInfo updateBuyInfo(BuyInfo buyInfo) {
        int update = daoHelper.update("shop.tronlucky.trondapp.buyInfo.updateBuyInfo", buyInfo);
        return buyInfo;
    }

    @Override
    public List<BuyInfo> listOfBuyInfo(BuyInfo buyInfo) {
        return daoHelper.queryForList("shop.tronlucky.trondapp.buyInfo.listOfBuyInfo", buyInfo);
    }
}
