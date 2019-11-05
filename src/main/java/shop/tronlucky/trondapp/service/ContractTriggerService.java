package shop.tronlucky.trondapp.service;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.jcajce.provider.digest.Keccak;
import org.spongycastle.jcajce.provider.digest.SHA3;
import org.spongycastle.util.encoders.Hex;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import shop.tronlucky.trondapp.client.WalletClient;
import shop.tronlucky.trondapp.common.utils.AbiUtil;
import shop.tronlucky.trondapp.common.utils.DataWord;
import shop.tronlucky.trondapp.common.utils.ProtoBufUtils;
import shop.tronlucky.trondapp.config.Args;
import shop.tronlucky.trondapp.dao.DaoHelper;
import shop.tronlucky.trondapp.exception.FailedExceptionException;
import shop.tronlucky.trondapp.exception.TransactionInfoNotFoundException;
import shop.tronlucky.trondapp.model.Secret;
import shop.tronlucky.trondapp.protos.Protocol.TransactionInfo;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class ContractTriggerService {
    @Setter
    private DaoHelper daoHelper;

    @Setter
    JavaMailSender mailSender;

    private ExecutorService checkService = Executors.newFixedThreadPool(8);

    class TransactionChecker implements Callable<TransactionInfo> {

        String trxId;
        String msg;

        TransactionChecker(String trxId, String msg) {
            this.trxId = trxId;
            this.msg = msg;
        }

        @Override
        public TransactionInfo call() throws Exception {
            Thread.sleep(5000);
            Optional<TransactionInfo> transactionInfo = checkWallet.getTransactionInfoById(trxId);
            if (transactionInfo.isPresent()) {
                logger.info("Trx:" + trxId + " info:" + transactionInfo.get());
                logger.info(msg);
                logger.info(
                        "TrxInfo:" + ProtoBufUtils.printReadableTransactionInfo(transactionInfo.get()));
            } else {
                logger.warn("Trx:" + trxId + " failed");
            }
            return transactionInfo.get();
        }
    }

    private WalletClient triggerWallet;
    private WalletClient checkWallet;
    private int fullNodeIndex = 0;
    private String fullNode;

    private ContractTriggerService() {
        initWallet();
    }

    private void initWallet() {
        List<String> fullNodeList = Args.getInstance().getFullNodeList();
        fullNodeIndex %= fullNodeList.size();
        this.fullNode = fullNodeList.get(fullNodeIndex++);
        if (triggerWallet == null) {
            logger.info("connect to fullNode[" + fullNode + "]");
        } else {
            logger.warn("switch fullNode to [" + fullNode + "]");
        }
        triggerWallet = new WalletClient(this.fullNode, Args.getInstance().getCooPrivateKey());
        checkWallet = new WalletClient(this.fullNode, Args.getInstance().getCooPrivateKey());
    }

    public void leaveRoom(List<String> players, long roomId)
            throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
        String methodSign = "leaveRoom(address[],uint256)";
        List params = Arrays.asList(players, roomId);
        triggerWallet
                .triggerContractSync(Args.getInstance().getBttContract(), methodSign, params, 0);
    }

    public long endSuccessfulRound(long roomId, List<String> players)
            throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
        String methodSign = "endSuccessfulRound(uint256,address[])";
        List params = Arrays.asList(roomId, players);
        byte[] input = AbiUtil.parseMethod(methodSign, params);
        byte[] result = triggerWallet
                .triggerContractSync(Args.getInstance().getBttContract(), 0, input);
        return new DataWord(result).longValue();
    }

    public void endFailedRound(long roomId)
            throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
        String methodSign = "endFailedRound(uint256)";
        List params = Arrays.asList(roomId);
        triggerWallet
                .triggerContractSync(Args.getInstance().getBttContract(), methodSign, params, 0);

    }

    public byte[] triggerBet(List<String> players, long roomId)
            throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
        String methodSign = "triggerBet(address[],uint256)";
        List params = Arrays.asList(players, roomId);
        return triggerWallet
                .triggerContractSync(Args.getInstance().getBttContract(), methodSign, params, 0);
    }

    public void stopBet(long roomId)
            throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
        String methodSign = "stopBet(uint256)";
        List params = Arrays.asList(roomId);
        triggerWallet
                .triggerContractSync(Args.getInstance().getBttContract(), methodSign, params, 0);
    }

    // TODO 这里需要调整
    public List<String> getPaidPlayers(long roomId) {
        String methodSign = "getPaidPlayers(uint256)";
        logger.info("trigger " + methodSign + "[" + roomId + "]");
        List params = Arrays.asList(roomId);
        byte[] input = AbiUtil.parseMethod("getPaidPlayers(uint256)", params);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        logger.info("in getPaidPlayers, result: " + Hex.toHexString(result));
        return AbiUtil.unpackAddressArray(result);
    }

    public long getPrizeOfGame(long gameId) {
        String methodSign = "prize_of_game(uint256)";
        logger.info("trigger " + methodSign + "[" + gameId + "]");
        List params = Arrays.asList(gameId);
        byte[] input = AbiUtil.parseMethod(methodSign, params);
        byte[] result = checkWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        logger.info("in prize_of_game, result: " + Hex.toHexString(result));
        return new DataWord(result).longValue();
    }

    public String sellOrder(long trcToken, long amountA, long amountB, long price, long callValue, long tokenValue) {
        String methodSign = "sellOrder(trcToken,uint256,uint256,uint256)";
        List params = Arrays.asList(trcToken, amountA, amountB, price);
        return triggerWallet
                .triggerContractNormal(Args.getInstance().getBttContract(), methodSign, params,
                        callValue, trcToken, tokenValue);
    }

    public Long getOrderId(String txId)
            throws FailedExceptionException, TransactionInfoNotFoundException {
        return triggerWallet.getOrderId(txId);
    }

    public String cancelOrder(Long orderId) {

        long callValue = 0;
        String methodSign = "cancelOrder(uint256)";
        List params = Arrays.asList(orderId);
        return triggerWallet
                .triggerContractNormal(Args.getInstance().getBttContract(), methodSign, params,
                        callValue, 0, 0);
    }

    public Long getOrderStatus(Long orderId) {
        long callValue = 0;
        String methodSign = "getOrderInfo(uint256)";
        List params = Arrays.asList(orderId);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), methodSign, params, callValue);
        return DataWord.getDataWord(result, 1).longValue();
    }

    public void updateGameId(long roomId, long gameId)
            throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
        String methodSign = "updateGame(uint256,uint256)";
        List params = Arrays.asList(roomId, gameId);
        triggerWallet
                .triggerContractSync(Args.getInstance().getBttContract(), methodSign, params, 0);
    }

    public long getRoomStatus(long roomId) {
        String methodSign = "room_map(uint256)";
        List params = Arrays.asList(roomId);
        byte[] input = AbiUtil.parseMethod(methodSign, params);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return DataWord.getDataWord(result, 2).longValue();
    }

    public long getPlayerRoom(String address) {
        String methodSign = "room_of(address)";
        List params = Arrays.asList(address);
        byte[] input = AbiUtil.parseMethod(methodSign, params);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);

        return new DataWord(result).longValue();
    }


    /*the below is normal code*/
    public void test(int level, long roomId, long gameId)
            throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
        String methodSign = "t5()";
        byte[] input = AbiUtil.parseMethod(methodSign);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        logger.info("in test, result: " + Hex.toHexString(result));
    }

    public String getStatus() {
        String methodSign = "status()";
        byte[] input = AbiUtil.parseMethod(methodSign);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return Hex.toHexString(result);
    }

    public String getRound() {
        String methodSign = "round()";
        byte[] input = AbiUtil.parseMethod(methodSign);
        byte[] result = triggerWallet
                .triggerConstantContractWithReturn(Args.getInstance().getBttContract(), 0, input);
        return Hex.toHexString(result);
    }

    public void commitHash() {
        String key = generateUniqueKey();
        String methodSign = "commitHash(bytes32)";
        String hash = key;//todo
        List<Object> params = Collections.singletonList(hash);
        triggerWallet.triggerContractNormal(Args.getInstance().getBttContract(),
                methodSign, params, 0, 0, 0);
        Secret secret = new Secret();
        secret.setSecret(key);
        secret.setRoundNumber(getRound());
        daoHelper.insert("shop.tronlucky.trondapp.secret.addSecret", secret);
    }

    public void commitSecret() {
        Secret secret = daoHelper.queryOne("shop.tronlucky.trondapp.secret.findByRound", getRound());
        String methodSign = "commitSecret(bytes32)";
        String key = secret.getSecret();
        List<Object> params = Collections.singletonList(key);
        triggerWallet.triggerContractNormal(Args.getInstance().getBttContract(),
                methodSign, params, 0, 0, 0);
    }

    public void doLucky() {
        String methodSign = "doLucky(uint256)";
        int loopCnt = 5;
        List<Object> params = Collections.singletonList(loopCnt);
        triggerWallet.triggerContractNormal(Args.getInstance().getBttContract(),
                methodSign, params, 0, 0, 0);
    }

    public void doJackpot() {
        String methodSign = "doJackpot(uint256)";
        int loopCnt = 5;
        List<Object> params = Collections.singletonList(loopCnt);
        triggerWallet.triggerContractNormal(Args.getInstance().getBttContract(),
                methodSign, params, 0, 0, 0);
    }

    public void luckyWithdraw(String round) {
        String methodSign = "luckyWithdraw(uint128)";
        List<Object> params = Collections.singletonList(round);
        triggerWallet.triggerContractNormal(Args.getInstance().getBttContract(),
                methodSign, params, 0, 0, 0);
    }

    public void jackpotLuckyWithdraw(String round) {
        String methodSign = "jackpotLuckyWithdraw(uint256)";
        List<Object> params = Collections.singletonList(round);
        triggerWallet.triggerContractNormal(Args.getInstance().getBttContract(),
                methodSign, params, 0, 0, 0);
    }

    private String generateUniqueKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String secret = Hex.toHexString(bytes);
        Object secretInDB = daoHelper.queryOne("shop.tronlucky.trondapp.secret.findBySecret", secret);
        if (secretInDB == null) {
            return secret;
        } else {
            return generateUniqueKey();
        }
    }

    public static void main(String[] args) {
        String hello = "0x7465737400000000000000000000000000000000000000000000000000000000";
        SHA3.DigestSHA3 sha3 = new SHA3.DigestSHA3(256);
        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(hello.getBytes());

        byte[] digest = sha3.digest(hello.getBytes());

        System.out.println("SHA3-256 = " + Hex.toHexString(digest));


        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("cwangjie@qq.com");
        message.setTo("cwangjie@gmail.com");
        message.setSubject("主题：简单邮件");
        message.setText("测试邮件内容");
        JavaMailSender mailSender = new JavaMailSenderImpl();
        mailSender.send(message);

        /*Args args1 = Args.getInstance();
        args1.setParam(args);
        ContractTriggerService service = new ContractTriggerService();
        service.getStatus();*/
    }
}
