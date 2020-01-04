package shop.tronlucky.trondapp.service;

import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import shop.tronlucky.trondapp.client.WalletClient;
import shop.tronlucky.trondapp.common.crypto.Hash;
import shop.tronlucky.trondapp.common.utils.AbiUtil;
import shop.tronlucky.trondapp.common.utils.DataWord;
import shop.tronlucky.trondapp.config.Args;
import shop.tronlucky.trondapp.dao.DaoHelper;
import shop.tronlucky.trondapp.exception.JackpotWithdrawException;
import shop.tronlucky.trondapp.exception.WithdrawException;
import shop.tronlucky.trondapp.model.Secret;
import shop.tronlucky.trondapp.model.WithdrawFailLog;
import shop.tronlucky.trondapp.protos.Protocol;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@EnableRetry
public class ContractTriggerService {
    private static final String TIME_OUT_TAG = "curBlockNum - endBlock should <= expiredBlockCnt";
    private DaoHelper daoHelper;

    private MailService mailService;

    @Autowired
    public void setDaoHelper(DaoHelper daoHelper) {
        this.daoHelper = daoHelper;
    }

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    private WalletClient triggerWallet;
    private int fullNodeIndex = 0;

    public ContractTriggerService() {
        initWallet();
    }

    private void initWallet() {
        Args.getInstance().setParam(new String[0]);
        List<String> fullNodeList = Args.getInstance().getFullNodeList();
        fullNodeIndex %= fullNodeList.size();
        String fullNode = fullNodeList.get(fullNodeIndex++);
        if (triggerWallet == null) {
            logger.info("connect to fullNode[" + fullNode + "]");
        } else {
            logger.warn("switch fullNode to [" + fullNode + "]");
        }
        triggerWallet = new WalletClient(fullNode, Args.getInstance().getCooPrivateKey());
    }

    public Integer getStatus() {
        String methodSign = "status()";
        byte[] input = AbiUtil.parseMethod(methodSign);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return Integer.parseInt(Hex.toHexString(result), 16);
    }

    public Integer getRound() {
        String methodSign = "round()";
        byte[] input = AbiUtil.parseMethod(methodSign);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return Integer.parseInt(Hex.toHexString(result), 16);
    }

    public Integer getJackpotRound() {
        String methodSign = "jackpotRound()";
        byte[] input = AbiUtil.parseMethod(methodSign);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return Integer.parseInt(Hex.toHexString(result), 16);
    }

    @Retryable(maxAttempts = 5)
    public void commitHash(Integer round) throws InterruptedException {
        String key = generateUniqueKey();
        Secret secret = new Secret();
        secret.setKey(key);
        secret.setRoundNumber(round);
        daoHelper.update("shop.tronlucky.trondapp.secret.addSecret", secret);

        DataWord word = new DataWord(key);
        byte[] encoded = Hash.sha3(word.getData());
        String hash = Hex.toHexString(encoded);
        List<Object> params = Collections.singletonList(hash);
        String methodSign = "commitHash(bytes32)";
        triggerContractAndCatchNoEnergy(params, methodSign);
    }

    @Retryable(maxAttempts = 5)
    public void commitSecret(Integer round) throws InterruptedException {
        Secret secret = daoHelper.queryOne("shop.tronlucky.trondapp.secret.findByRound", round);
        String methodSign = "commitSecret(bytes32)";
        if (secret == null) {
            logger.error("occur error when commit hash, " +
                    "cannot find key in table `secret` where round = {}", round);
            return;
        }
        String key = secret.getKey();
        List<Object> params = Collections.singletonList(key);
        triggerContractAndCatchNoEnergy(params, methodSign);
    }

    public void doLucky(int round) throws InterruptedException {
        String methodSign = "doLucky(uint256)";
        int loopCnt = 5;
        List<Object> params = Collections.singletonList(loopCnt);
        triggerContractAndCatchNoEnergy(params, methodSign, String.valueOf(round));
    }

    public void doJackpot(int round) throws InterruptedException {
        String methodSign = "doJackpot(uint256)";
        int loopCnt = 5;
        List<Object> params = Collections.singletonList(loopCnt);
        triggerContractAndCatchNoEnergy(params, methodSign, String.valueOf(round));
    }

    @Retryable(maxAttempts = 2)
    public void luckyWithdraw(int round) throws WithdrawException {
        try {
            String methodSign = "luckyWithdraw(uint128)";
            List<Object> params = Collections.singletonList(round);
            triggerContractAndCatchNoEnergy(params, methodSign);
        } catch (Exception e) {
            throw new WithdrawException("withdraw fail");
        }
    }

    @Recover
    public void recover(WithdrawException e) {
        Integer round = getRound() - 1;
        mailService.sendMail("fatal: withdraw fail", "hello: \n withdraw fail, " +
                "please manually intervene. round = " + round + "," + e);
        WithdrawFailLog log = new WithdrawFailLog();
        log.setRoundNumber(round);
        log.setWithdrawType(1);
        daoHelper.insert("shop.tronlucky.trondapp.withdrawfaillog.addWithdrawFailLog", log);
    }

    @Retryable(maxAttempts = 2)
    public void jackpotLuckyWithdraw(int round) throws JackpotWithdrawException {
        try {
            String methodSign = "jackpotLuckyWithdraw(uint256)";
            List<Object> params = Collections.singletonList(round);
            triggerContractAndCatchNoEnergy(params, methodSign);
        } catch (Exception e) {
            throw new JackpotWithdrawException("jackpot withdraw fail");
        }
    }

    @Recover
    public void recover(JackpotWithdrawException e) {
        Integer round = getJackpotRound() - 1;
        mailService.sendMail("fatal: jackpot withdraw fail", "hello: \n jackpot withdraw fail, " +
                "please manually intervene. jackpot round = " + round + "," + e);
        WithdrawFailLog log = new WithdrawFailLog();
        log.setRoundNumber(round);
        log.setWithdrawType(2);
        daoHelper.insert("shop.tronlucky.trondapp.withdrawfaillog.addWithdrawFailLog", log);
    }

    public void goNextRound() throws InterruptedException {
        String methodSign = "goNextRound()";
        triggerContractAndCatchNoEnergy(new ArrayList<>(), methodSign);
    }

    private void doRefund() throws InterruptedException {
        String methodSign = "doRefund()";
        triggerContractAndCatchNoEnergy(new ArrayList<>(), methodSign);
    }

    private void sendRefund(int round) throws InterruptedException {
        int playerCnt = roundPlayersCnt(round);
        int loops = playerCnt / 10;
        for (int i = 0; i <= loops; i++) {
            String methodSign = "sendRefund(uint128 r, uint256 fromIndex, uint256 loopCnt)";
            List<Object> params = new ArrayList<>();
            params.add(round);
            params.add(i * 10);
            int loopCnt = 10;
            if (i == loops) {
                loopCnt = playerCnt % 10;
            }
            params.add(loopCnt);
            triggerContractAndCatchNoEnergy(params, methodSign);
        }
    }

    private int roundPlayersCnt(int round) {
        String methodSign = "roundPlayersCnt(uint128)";
        byte[] input = AbiUtil.parseMethod(methodSign, String.valueOf(round));
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return Integer.parseInt(Hex.toHexString(result), 16);
    }

    private void triggerContractAndCatchNoEnergy(List<Object> params, String... args) throws InterruptedException {
        String txId = triggerWallet.triggerContractNormal(Args.getInstance().getBttContract(),
                args[0], params, 0, 0, 0);
        Thread.sleep(3 * 1000L);
        Optional<Protocol.TransactionInfo> t = triggerWallet.getTransactionInfoById(txId);
        if (t.isPresent()) {
            Protocol.Transaction.Result.contractResult result = t.get().getReceipt().getResult();
            if (result == Protocol.Transaction.Result.contractResult.OUT_OF_ENERGY) {
                logger.info("we have no energy when {}, txId = {}", args[0], txId);
                mailService.sendMail("OUT_OF_ENERGY", "Emergency! " +
                        "\n we have no energy when " + args[0] + ", txId = " + txId);
            } else if (result == Protocol.Transaction.Result.contractResult.REVERT) {
                logger.info("we have REVERT when {}, txId = {}", args[0], txId);
                String timeOut = t.get().getContractResult(0).toStringUtf8();
                String msg = timeOut != null ? timeOut.substring(68, 116) : null;
                mailService.sendMail("REVERT", "Emergency! " +
                        "\n we have REVERT when " + args[0] + ", txId = " + txId
                        + ", msg: " + msg);
                if (TIME_OUT_TAG.equals(msg)) {
                    doRefund();
                    sendRefund(Integer.valueOf(args[1]));
                }
            }
        }
    }

    private String generateUniqueKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String key = Hex.toHexString(bytes);

        Secret secretInDB = daoHelper.queryOne("shop.tronlucky.trondapp.secret.findByKey", key);
        if (secretInDB == null) {
            return key;
        } else {
            return generateUniqueKey();
        }
    }
}