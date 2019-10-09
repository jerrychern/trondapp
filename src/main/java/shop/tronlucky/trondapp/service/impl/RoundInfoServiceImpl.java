package shop.tronlucky.trondapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shop.tronlucky.trondapp.dao.DaoHelper;
import shop.tronlucky.trondapp.model.RoundInfo;
import shop.tronlucky.trondapp.service.RoundInfoService;

/**
 * @author Jerry Chern
 * Create Time: 2019/5/28
 */
@Service
public class RoundInfoServiceImpl implements RoundInfoService {

    @Autowired
    private DaoHelper daoHelper;

    @Override
    public RoundInfo addRoundInfo(RoundInfo roundInfo) {
        daoHelper.insert("shop.tronlucky.trondapp.roundIfo.addRoundInfo", roundInfo);
        return roundInfo;
    }

    @Override
    public RoundInfo updateRoundInfo(RoundInfo roundInfo) {
        daoHelper.update("shop.tronlucky.trondapp.roundIfo.updateRoundInfo", roundInfo);
        return roundInfo;
    }

    @Override
    public RoundInfo findRoundInfoByRound(String roundNumber) {
        return daoHelper.queryOne("shop.tronlucky.trondapp.roundIfo.findRoundInfoByRound", roundNumber);
    }
}
