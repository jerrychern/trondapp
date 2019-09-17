package shop.tronlucky.trondapp.service;

import org.apache.ibatis.annotations.Param;
import shop.tronlucky.trondapp.model.RoundInfo;

/**
 * @author Jerry Chern
 * Create Time: 2019/5/28
 */
public interface RoundInfoService {
    RoundInfo addRoundInfo(RoundInfo roundInfo);

    RoundInfo updateRoundInfo(RoundInfo roundInfo);

    RoundInfo findRoundInfoByRound(String roundNum);
}
