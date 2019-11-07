package shop.tronlucky.trondapp.client;

import com.google.protobuf.ByteString;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import shop.tronlucky.trondapp.api.GrpcAPI.AddressPrKeyPairMessage;
import shop.tronlucky.trondapp.api.GrpcAPI.EmptyMessage;
import shop.tronlucky.trondapp.api.GrpcAPI.Return;
import shop.tronlucky.trondapp.api.GrpcAPI.TransactionExtention;
import shop.tronlucky.trondapp.common.crypto.ECKey;
import shop.tronlucky.trondapp.common.utils.AbiUtil;
import shop.tronlucky.trondapp.common.utils.Base58;
import shop.tronlucky.trondapp.common.utils.ByteArray;
import shop.tronlucky.trondapp.common.utils.Sha256Hash;
import shop.tronlucky.trondapp.common.utils.TransactionUtils;
import shop.tronlucky.trondapp.common.utils.WalletUtil;
import shop.tronlucky.trondapp.config.GameSetting;
import shop.tronlucky.trondapp.exception.FailedExceptionException;
import shop.tronlucky.trondapp.exception.TransactionInfoNotFoundException;
import shop.tronlucky.trondapp.protos.Contract;
import shop.tronlucky.trondapp.protos.Protocol.Account;
import shop.tronlucky.trondapp.protos.Protocol.Transaction;
import shop.tronlucky.trondapp.protos.Protocol.Transaction.Result;
import shop.tronlucky.trondapp.protos.Protocol.TransactionInfo;
import shop.tronlucky.trondapp.protos.Protocol.TransactionInfo.code;

@Slf4j
public class WalletClient {

    private RpcClient rpcCli;
    private ECKey ecKey;
    private byte[] address;

    public WalletClient(String target, byte[] priateKey) {
        rpcCli = new RpcClient(target);
        ecKey = ECKey.fromPrivate(priateKey);
        address = ecKey.getAddress();
    }

    public void loadPrivateKey(String hexPriateKey) {
        ecKey = ECKey.fromPrivate(Hex.decode(hexPriateKey));
        address = ecKey.getAddress();
    }

    public void loadPrivateKey(byte[] hexPriateKey) {
        ecKey = ECKey.fromPrivate(hexPriateKey);
        address = ecKey.getAddress();
    }

    public Optional<TransactionInfo> getTransactionInfoById(String txID) {
        return rpcCli.getTransactionInfoById(txID);
    }

    public Account queryAccount() {
        return rpcCli.queryAccount(this.address);//call rpc
    }

    public Account queryAccount(String address) {
        return rpcCli.queryAccount(WalletUtil.decodeFromBase58Check(address));//call rpc
    }

    public Account queryAccount(byte[] address) {
        return rpcCli.queryAccount(address);//call rpc
    }

    public ECKey generateAddress() {
        EmptyMessage.Builder builder = EmptyMessage.newBuilder();
        AddressPrKeyPairMessage result = rpcCli.generateAddress(builder.build());

        byte[] priKey = Base58.hexs2Bytes(result.getPrivateKey().getBytes());
        if (!Base58.priKeyValid(priKey)) {
            return null;
        }
        return ECKey.fromPrivate(priKey);
    }

    public byte[] triggerConstantContractWithReturn(byte[] contractAddress, String methodSign,
        List<Object> params, long callValue) {

        logger.info("trigger method: " + methodSign + " params: " + params.toString());
        logger.info("callValue: " + callValue);
        byte[] input = AbiUtil.parseMethod(methodSign, params);

        return triggerConstantContractWithReturn(contractAddress, callValue, input,
            GameSetting.FEE_LIMIT, GameSetting.TOKEN_CALL_VALUE, GameSetting.TOKEN_ID);
    }

    public byte[] triggerConstantContractWithReturn(byte[] contractAddress, long callValue,
        byte[] data) {

        return triggerConstantContractWithReturn(contractAddress, callValue, data,
            GameSetting.FEE_LIMIT, GameSetting.TOKEN_CALL_VALUE, GameSetting.TOKEN_ID);
    }

    public byte[] triggerConstantContractWithReturn(byte[] contractAddress, long callValue,
        byte[] data, long feeLimit, long tokenValue, Long tokenId) {
        TransactionExtention transactionExtention = this
            .triggerConstantMethod(contractAddress, callValue, data, feeLimit, tokenValue, tokenId);

        Transaction transaction = transactionExtention.getTransaction();
        if (transaction.getRetCount() != 0 &&
            transactionExtention.getConstantResult(0) != null &&
            transactionExtention.getResult() != null) {
            return transactionExtention.getConstantResult(0).toByteArray();
        }
        return new byte[0];
    }

    public Object triggerContract(byte[] contractAddress, long callValue, byte[] data,
        long feeLimit, long tokenValue, Long tokenId) {
        byte[] owner = address;
        Contract.TriggerSmartContract triggerContract = triggerCallContract(owner, contractAddress,
            callValue, data, tokenValue, tokenId);
        TransactionExtention transactionExtention = rpcCli.triggerContract(triggerContract);
        if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
            logger.warn(
                "RPC create call trx failed:" + transactionExtention.getResult().getCode() + "\n"
                    + transactionExtention.getResult().getMessage().toStringUtf8());
            return null;
        }

        Transaction transaction = transactionExtention.getTransaction();
        if (transaction.getRetCount() != 0 &&
            transactionExtention.getConstantResult(0) != null &&
            transactionExtention.getResult() != null) {
            byte[] result = transactionExtention.getConstantResult(0).toByteArray();
            logger.info("message:" + transaction.getRet(0).getRet() + "\n" + ByteArray
                .toStr(transactionExtention.getResult().getMessage().toByteArray()));
            logger.info("Result:" + Hex.toHexString(result));
            return transactionExtention;
        }

        TransactionExtention.Builder texBuilder = TransactionExtention.newBuilder();
        Transaction.Builder transBuilder = Transaction.newBuilder();
        Transaction.raw.Builder rawBuilder = transactionExtention.getTransaction().getRawData()
            .toBuilder();
        rawBuilder.setFeeLimit(feeLimit);
        transBuilder.setRawData(rawBuilder);
        for (int i = 0; i < transactionExtention.getTransaction().getSignatureCount(); i++) {
            ByteString s = transactionExtention.getTransaction().getSignature(i);
            transBuilder.setSignature(i, s);
        }
        for (int i = 0; i < transactionExtention.getTransaction().getRetCount(); i++) {
            Result r = transactionExtention.getTransaction().getRet(i);
            transBuilder.setRet(i, r);
        }
        texBuilder.setTransaction(transBuilder);
        texBuilder.setResult(transactionExtention.getResult());
        texBuilder.setTxid(transactionExtention.getTxid());
        transactionExtention = texBuilder.build();

        return processTransactionExtention(transactionExtention);
    }

    public TransactionExtention triggerConstantMethod(byte[] contractAddress, long callValue,
        byte[] data, long feeLimit, long tokenValue, Long tokenId) {

        byte[] owner = address;
        Contract.TriggerSmartContract triggerContract = triggerCallContract(owner, contractAddress,
            callValue, data, tokenValue, tokenId);
        TransactionExtention transactionExtention = rpcCli.triggerContract(triggerContract);
        if (!transactionExtention.getResult().getResult()) {
            logger.warn("RPC create call trx failed!");
            logger.warn("Code = " + transactionExtention.getResult().getCode());
            logger
                .warn("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
            return null;
        }

        return transactionExtention;
    }


    public static Contract.TriggerSmartContract triggerCallContract(byte[] address,
        byte[] contractAddress,
        long callValue, byte[] data, long tokenValue, Long tokenId) {
        Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
        builder.setOwnerAddress(ByteString.copyFrom(address));
        builder.setContractAddress(ByteString.copyFrom(contractAddress));
        builder.setData(ByteString.copyFrom(data));
        builder.setCallValue(callValue);
        builder.setTokenId(tokenId);
        builder.setCallTokenValue(tokenValue);
        return builder.build();
    }

  public byte[] triggerContractSync(byte[] contractAddress, String methodSign, long callValue)
      throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
    logger.info("trigger method " + methodSign);
    byte[] input = AbiUtil.parseMethod(methodSign);
    return triggerContractSync(contractAddress, callValue, input, GameSetting.FEE_LIMIT,
        GameSetting.TOKEN_CALL_VALUE, GameSetting.TOKEN_ID);
  }

    public byte[] triggerContractSync(byte[] contractAddress, String methodSign,
        List<Object> params, long callValue)
        throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {
        logger.info("trigger method " + methodSign + " params: " + params.toString());
        byte[] input = AbiUtil.parseMethod(methodSign, params);
        return triggerContractSync(contractAddress, callValue, input, GameSetting.FEE_LIMIT,
            GameSetting.TOKEN_CALL_VALUE, GameSetting.TOKEN_ID);
    }

    public String triggerContractNormal(byte[] contractAddress, String methodSign,
        List<Object> params, long callValue, long trcToken, long tokenValue) {
        logger.info("trigger method: " + methodSign + " params: " + params.toString());
        logger.info(
            "callValue: " + callValue + ", trcToken: " + trcToken + ", tokenValue: " + tokenValue);
        byte[] input;
        if (params.isEmpty()) {
            input = AbiUtil.parseMethod(methodSign);
        } else {
            input = AbiUtil.parseMethod(methodSign, params);
        }
        if (tokenValue > 0) {
            return triggerContractNormal(contractAddress, callValue, input, GameSetting.FEE_LIMIT,
                tokenValue, trcToken);
        } else {
            return triggerContractNormal(contractAddress, callValue, input, GameSetting.FEE_LIMIT,
                GameSetting.TOKEN_CALL_VALUE, GameSetting.TOKEN_ID);
        }
    }


    public byte[] triggerContractSync(byte[] contractAddress, long callValue, byte[] input)
        throws InterruptedException, FailedExceptionException, TransactionInfoNotFoundException {

        return triggerContractSync(contractAddress, callValue, input, GameSetting.FEE_LIMIT,
            GameSetting.TOKEN_CALL_VALUE, GameSetting.TOKEN_ID);
    }

    private byte[] triggerContractSync(byte[] contractAddress, long callValue, byte[] data,
        long feeLimit, long tokenValue, Long tokenId)
        throws InterruptedException, TransactionInfoNotFoundException, FailedExceptionException {
        String txId = (String) triggerContract(contractAddress, callValue, data, feeLimit,
            tokenValue, tokenId);
        logger.info("txId:" + txId);
        logger.warn("data:" + Hex.toHexString(data));
        Thread.sleep(4000);
        Optional<TransactionInfo> transactionInfo = getTransactionInfoById(txId);
        int tries = 4;
        while (tries > 0) {
            TransactionInfo info = transactionInfo.get();
            if (info.getBlockTimeStamp() == 0L) {
                Thread.sleep(1000);
                --tries;
                continue;
            }
            if (info.getResult().equals(code.SUCESS)) {
                return info.getContractResult(0).toByteArray();
            } else {
                throw new FailedExceptionException(info.getResMessage().toStringUtf8());
            }
        }
        throw new TransactionInfoNotFoundException(txId);
    }

    private String triggerContractNormal(byte[] contractAddress, long callValue, byte[] data,
        long feeLimit, long tokenValue, Long tokenId) {
        String txId = (String) triggerContract(contractAddress, callValue, data, feeLimit,
            tokenValue, tokenId);
        logger.info("txId:" + txId);
        logger.warn("data:" + Hex.toHexString(data));
        if (txId == null) {
            throw new RuntimeException("tx is null");
        }
        return txId;
    }

    public Long getOrderId(String txId)
        throws TransactionInfoNotFoundException, FailedExceptionException {
        Optional<TransactionInfo> transactionInfo = getTransactionInfoById(txId);
        int tries = 2;
        while (tries > 0) {
            TransactionInfo info = transactionInfo.get();
            if (info.getBlockTimeStamp() == 0L) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                --tries;
                continue;
            }
            if (info.getResult().equals(code.SUCESS)) {
                return Long.parseLong(
                    ByteArray.toHexString(info.getLog(0).getData().toByteArray()).substring(0, 64),
                    16);
            } else {
                throw new FailedExceptionException(
                    "tx failed: " + info.getResMessage().toStringUtf8());
            }
        }
        throw new TransactionInfoNotFoundException("tx not found: " + txId);
    }

    private String processTransactionExtention(TransactionExtention transactionExtention) {
        if (transactionExtention == null) {
            return null;
        }

        Return ret = transactionExtention.getResult();
        if (!ret.getResult()) {
            logger.warn("Code = " + ret.getCode());
            logger.warn("Message = " + ret.getMessage().toStringUtf8());
            return null;
        }
        Transaction transaction = transactionExtention.getTransaction();
        if (transaction == null || transaction.getRawData().getContractCount() == 0) {
            logger.info("Transaction is empty");
            return null;
        }

        if (transaction.getRawData().getTimestamp() == 0) {
            transaction = TransactionUtils.setTimestamp(transaction);
        }
        transaction = TransactionUtils.sign(transaction, this.ecKey);
        String txId = ByteArray
            .toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
//    logger.info("Receive txid = " + txId);
        return rpcCli.broadcastTransaction(transaction) ? txId : null;
    }

}
