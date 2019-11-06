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
    public void run(ApplicationArguments applicationArguments) throws Exception {
        int withdraw = 0;
        while (true) {
            try {
                Thread.sleep(3 * 1000L);
                logger.info("go in loop");
                Integer status = Integer.valueOf(contractService.getStatus());
                logger.info("status:{}", status);
                switch (status) {
                    case 1:
                        String round = contractService.getRound();
                        switch (withdraw) {
                            case 4:
                                contractService.luckyWithdraw(String.valueOf(Integer.valueOf(round) - 1));
                                withdraw = 0;
                                break;
                            case 5:
                                String jackpotRound = contractService.getJackpotRound();
                                contractService.luckyWithdraw(String.valueOf(Integer.valueOf(round) - 1));
                                contractService.jackpotLuckyWithdraw(String.valueOf(Integer.valueOf(jackpotRound) - 1));
                                withdraw = 0;
                                break;
                            default:
                        }
                        contractService.commitHash();
                        break;
                    case 2:
                        Thread.sleep(3 * 1000L);
                        break;
                    case 3:
                        Thread.sleep(100 * 1000L);
                        contractService.commitSecret();
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
                    default:
                }
            } catch (Exception e) {
                logger.error("fatal error:", e);
            }
        }
    }
}