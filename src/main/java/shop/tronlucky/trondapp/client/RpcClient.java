package shop.tronlucky.trondapp.client;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import shop.tronlucky.trondapp.api.GrpcAPI.AddressPrKeyPairMessage;
import shop.tronlucky.trondapp.api.GrpcAPI.BytesMessage;
import shop.tronlucky.trondapp.api.GrpcAPI.EmptyMessage;
import shop.tronlucky.trondapp.api.GrpcAPI.Return;
import shop.tronlucky.trondapp.api.GrpcAPI.Return.response_code;
import shop.tronlucky.trondapp.api.GrpcAPI.TransactionExtention;
import shop.tronlucky.trondapp.api.WalletGrpc;
import shop.tronlucky.trondapp.common.utils.ByteArray;
import shop.tronlucky.trondapp.protos.Contract;
import shop.tronlucky.trondapp.protos.Protocol.Account;
import shop.tronlucky.trondapp.protos.Protocol.Transaction;
import shop.tronlucky.trondapp.protos.Protocol.TransactionInfo;

@Slf4j
public class RpcClient {
  private WalletGrpc.WalletBlockingStub blockingStub;

  public RpcClient(String target) {
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext(true).build();
    blockingStub = WalletGrpc.newBlockingStub(channel);
  }

  public TransactionExtention triggerContract(Contract.TriggerSmartContract request) {
    return blockingStub.triggerContract(request);
  }

  public Optional<TransactionInfo> getTransactionInfoById(String txID) {
    ByteString bsTxid = ByteString.copyFrom(ByteArray.fromHexString(txID));
    BytesMessage request = BytesMessage.newBuilder().setValue(bsTxid).build();
    TransactionInfo transactionInfo = blockingStub.getTransactionInfoById(request);
    return Optional.ofNullable(transactionInfo);
  }

  public boolean broadcastTransaction(Transaction signaturedTransaction) {
    int i = 10;
    Return response = blockingStub.broadcastTransaction(signaturedTransaction);
    while (response.getResult() == false && response.getCode() == response_code.SERVER_BUSY
        && i > 0) {
      i--;
      response = blockingStub.broadcastTransaction(signaturedTransaction);
      logger.info("repeate times = " + (11 - i));
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    if (!response.getResult()) {
      logger.info("Code = " + response.getCode());
      logger.info("Message = " + response.getMessage().toStringUtf8());
    }
    return response.getResult();
  }

  public TransactionExtention createTransaction2(Contract.TransferContract contract) {
    return blockingStub.createTransaction2(contract);
  }

  public Account queryAccount(byte[] address) {
    ByteString addressBS = ByteString.copyFrom(address);
    Account request = Account.newBuilder().setAddress(addressBS).build();
    return blockingStub.getAccount(request);
  }

  public AddressPrKeyPairMessage generateAddress(EmptyMessage emptyMessage) {
    return blockingStub.generateAddress(emptyMessage);
  }
}
