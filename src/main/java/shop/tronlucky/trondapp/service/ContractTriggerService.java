package shop.tronlucky.trondapp.service;

import java.util.ArrayList;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@EnableRetry
public class ContractTriggerService {
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
        return Integer.valueOf(Hex.toHexString(result));
    }

    public Integer getRound() {
        String methodSign = "round()";
        byte[] input = AbiUtil.parseMethod(methodSign);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return Integer.valueOf(Hex.toHexString(result));
    }

    public Integer getJackpotRound() {
        String methodSign = "jackpotRound()";
        byte[] input = AbiUtil.parseMethod(methodSign);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return Integer.valueOf(Hex.toHexString(result));
    }

    public void commitHash(Integer round) {
        String key;
        Secret secretInDB = daoHelper.queryOne("shop.tronlucky.trondapp.secret.findByRound", round);
        if (secretInDB != null) {
            key = secretInDB.getKey();
        } else {
            key = generateUniqueKey();
            Secret secret = new Secret();
            secret.setKey(key);
            secret.setRoundNumber(getRound());
            daoHelper.insert("shop.tronlucky.trondapp.secret.addSecret", secret);
        }
        DataWord word = new DataWord(key);
        byte[] encoded = Hash.sha3(word.getData());
        String hash = Hex.toHexString(encoded);
        List<Object> params = Collections.singletonList(hash);
        String methodSign = "commitHash(bytes32)";
        triggerContractAndCatchNoEnergy(methodSign, params);
    }

    public void commitSecret(Integer round) {
        Secret secret = daoHelper.queryOne("shop.tronlucky.trondapp.secret.findByRound", round);
        String methodSign = "commitSecret(bytes32)";
        if (secret == null) {
            logger.error("occur error when commit hash, " +
                    "cannot find key in table `secret` where round = {}", round);
            return;
        }
        String key = secret.getKey();
        List<Object> params = Collections.singletonList(key);
        triggerContractAndCatchNoEnergy(methodSign, params);
    }

    public void doLucky() {
        String methodSign = "doLucky(uint256)";
        int loopCnt = 5;
        List<Object> params = Collections.singletonList(loopCnt);
        triggerContractAndCatchNoEnergy(methodSign, params);
    }

    public void doJackpot() {
        String methodSign = "doJackpot(uint256)";
        int loopCnt = 5;
        List<Object> params = Collections.singletonList(loopCnt);
        triggerContractAndCatchNoEnergy(methodSign, params);
    }

    @Retryable(maxAttempts = 2)
    public void luckyWithdraw(int round) throws WithdrawException {
        try {
            String methodSign = "luckyWithdraw(uint128)";
            List<Object> params = Collections.singletonList(round);
            triggerContractAndCatchNoEnergy(methodSign, params);
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
            triggerContractAndCatchNoEnergy(methodSign, params);
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

    public void goNextRound() {
        String methodSign = "goNextRound()";
        triggerContractAndCatchNoEnergy(methodSign, new ArrayList<>());
    }

    private void triggerContractAndCatchNoEnergy(String methodSign, List<Object> params) {
        String txId = triggerWallet.triggerContractNormal(Args.getInstance().getBttContract(),
                methodSign, params, 0, 0, 0);
        Optional<Protocol.TransactionInfo> t = triggerWallet.getTransactionInfoById(txId);
        if (t.isPresent()) {
            Protocol.Transaction.Result.contractResult result = t.get().getReceipt().getResult();
            if (result == Protocol.Transaction.Result.contractResult.OUT_OF_ENERGY) {
                logger.info("we have no energy when {}, txId = {}", methodSign, txId);
                mailService.sendMail("OUT_OF_ENERGY", "Emergency! " +
                        "\n we have no energy when " + methodSign + ", txId = " + txId);
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

    public static void main(String[] args) {
        String hello = "7465737400000000000000000000000000000000000000000000000000000000";
        DataWord word = new DataWord(hello);
        byte[] encoded = Hash.sha3(word.getData());
        logger.info("SHA3-256 = {}", Hex.toHexString(encoded));
    }
}