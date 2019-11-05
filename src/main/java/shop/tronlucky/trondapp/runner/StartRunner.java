package shop.tronlucky.trondapp.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import shop.tronlucky.trondapp.controller.websocket.RoundInfoSocketServer;
import shop.tronlucky.trondapp.model.RoundInfo;
import shop.tronlucky.trondapp.service.RoundInfoService;

@Component
public class StartRunner implements ApplicationRunner {
    @Autowired
    private RoundInfoService roundInfoService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        try{
            while (true) {
                int i = 1;

                // TODO fetch roundNumber
                // TODO: if number > number in db, ?

                // todo access smart contract
                // optional setAssistant  // TODO exception

                // if price changes, updatePrice   // TODO exception
                // commitDeposit // TODO exception

                // todo commitHash   // TODO exception

                // TODO: game start
                // TODO: update DB

                Thread.sleep(40000);  // TODO: 5 mins + 1 mins

                // TODO: doLucky    // TODO exception （如果失败，可能是没有达到条件）

                // TODO: check status， maybe  doRefund

                // fetch lucky result
                // update DB
                RoundInfo round = new RoundInfo();
                roundInfoService.updateRoundInfo(round);
                //todo access smart contract
                String luckyAddress = "中奖号码";

                new Thread(() -> RoundInfoSocketServer.sendInfo(luckyAddress)).start();

                break;
            }
        } catch (Exception e) {
            // TODO initRoundData exception
            // TODO:  邮件
            // TODO:  write to db: maintain
        }
    }

    public static int checkStatus() {
        return 1;
    }
}
