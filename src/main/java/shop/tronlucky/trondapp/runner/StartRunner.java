package shop.tronlucky.trondapp.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import shop.tronlucky.trondapp.service.ContractTriggerService;

@Component
@Slf4j
public class StartRunner implements ApplicationRunner {

    private ContractTriggerService contractService;

    @Autowired
    public void setContractService(ContractTriggerService contractService) {
        this.contractService = contractService;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        int withdraw = 0;
        while (true) {
            try {
                Thread.sleep(3 * 1000L);
                logger.info("go in loop");
                Integer status = contractService.getStatus();
                logger.info("status:{}", status);
                Integer round = contractService.getRound();
                logger.info("round:{}", round);
                switch (status) {
                    case 1:
                        switch (withdraw) {
                            case 4:
                                contractService.luckyWithdraw(round - 1);
                                withdraw = 0;
                                break;
                            case 5:
                                Integer jackpotRound = contractService.getJackpotRound();
                                contractService.luckyWithdraw(round - 1);
                                contractService.jackpotLuckyWithdraw(jackpotRound - 1);
                                withdraw = 0;
                                break;
                            default:
                        }
                        contractService.commitHash(round);
                        break;
                    case 2:
                        Thread.sleep(3 * 1000L);
                        break;
                    case 3:
                        Thread.sleep(100 * 1000L);
                        contractService.commitSecret(round);
                        Thread.sleep(6 * 1000L);
                        break;
                    case 4:
                        contractService.doLucky();
                        withdraw = 4;
                        break;
                    case 5:
                        contractService.doJackpot();
                        withdraw = 5;
                        break;
                    case 6:
                        Thread.sleep(15 * 1000L);
                        contractService.goNextRound();
                        break;
                    default:
                }
            } catch (Exception e) {
                logger.error("occur error in loop :", e);
            }
        }
    }
}