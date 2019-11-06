package shop.tronlucky.trondapp.model;

import lombok.Data;

@Data
public class WithdrawFailLog {
    private Integer id;

    /*轮次信息*/
    private Integer roundNumber;

    /*提现类型，普通提现和大奖提现*/
    private Integer withdrawType;
}