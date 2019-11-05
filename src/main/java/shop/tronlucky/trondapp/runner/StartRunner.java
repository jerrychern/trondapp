package shop.tronlucky.trondapp.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import shop.tronlucky.trondapp.service.ContractTriggerService;

@Component
@Slf4j
public class StartRunner implements ApplicationRunner {
    @Autowired
    private ContractTriggerService contractService;

    @Autowired
    JavaMailSender mailSender;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        int withdraw = 0;
        while (true) {
            try {
                Thread.sleep(3 * 1000L);
                logger.info("go in loop");
                Integer status = Integer.valueOf(contractService.getStatus());
                switch (status) {
                    case 1:
                        logger.info("status:{}", 1);
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
                        logger.info("status: {}", 2);
                        Thread.sleep(3 * 1000L);
                        break;
                    case 3:
                        logger.info("status: {}", 3);
                        Thread.sleep(100 * 1000L);
                        contractService.commitSecret();
                        Thread.sleep(6 * 1000L);
                        break;
                    case 4:
                        logger.info("status: {}", 4);
                        contractService.doLucky();
                        withdraw = 4;
                        break;
                    case 5:
                        logger.info("status: {}", 5);
                        contractService.doJackpot();
                        withdraw = 5;
                        break;
                    default:
                }
            } catch (Exception e) {
                logger.error("fatal error:", e);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("cwangjie@qq.com");
                message.setTo("cwangjie@gmail.com", "tjchern@qq.com");
                message.setSubject("fatal error");
                message.setText(e.getMessage());
                mailSender.send(message);
            }
        }
    }
}
