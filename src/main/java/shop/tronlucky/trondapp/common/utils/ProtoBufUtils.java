package shop.tronlucky.trondapp.common.utils;

import java.util.List;
import shop.tronlucky.trondapp.protos.Protocol.InternalTransaction;
import shop.tronlucky.trondapp.protos.Protocol.ResourceReceipt;
import shop.tronlucky.trondapp.protos.Protocol.TransactionInfo;
import shop.tronlucky.trondapp.protos.Protocol.TransactionInfo.code;


public class ProtoBufUtils {

  public static String printReadableTransactionInfo(TransactionInfo transactionInfo) {
    String result = "";
    result += "txid: ";
    result += ByteArray.toHexString(transactionInfo.getId().toByteArray());
    result += "\n";
    result += "fee: ";
    result += transactionInfo.getFee();
    result += "\n";
//    result += "blockNumber: ";
//    result += transactionInfo.getBlockNumber();
//    result += "\n";
//    result += "blockTimeStamp: ";
//    result += transactionInfo.getBlockTimeStamp();
//    result += "\n";
    result += "result: ";

    if (transactionInfo.getResult().equals(code.SUCESS)) {
      result += "SUCCESS";
    } else {
      result += "FAILED";
    }
    result += "\n";
    result += "resMessage: ";
    result += ByteArray.toStr(transactionInfo.getResMessage().toByteArray());
    result += "\n";
    result += "contractResult: ";
    result += ByteArray.toHexString(transactionInfo.getContractResult(0).toByteArray());
    result += "\n";
    result += "contractAddress: ";
    result += WalletUtil.encode58Check(transactionInfo.getContractAddress().toByteArray());
//    result += "\n";
//    result += "logList: ";
//    result += "\n";
//    result += printLogList(transactionInfo.getLogList());
//    result += "\n";
//    result += "receipt: ";
//    result += printReceipt(transactionInfo.getReceipt());
    result += "\n";
    result += "InternalTransactionList: ";
    result += printInternalTransactionList(transactionInfo.getInternalTransactionsList());
    result += "\n";
    result += "contractResult: ";
    result += ByteArray.toHexString(transactionInfo.getContractResult(0).toByteArray());
    result += "\n";
    result += "receipt: ";
    result += "\n";
    result += printReceipt(transactionInfo.getReceipt());
    return result;
  }


  public static String printInternalTransactionList(List<InternalTransaction> internalTransactions){
    StringBuilder result = new StringBuilder("");
    internalTransactions.forEach(internalTransaction -> {
          result.append("[\n");
          result.append("  hash:\n");
          result.append("  " + ByteArray.toHexString(internalTransaction.getHash().toByteArray()));
          result.append("  \n");
          result.append("  caller_address:\n");
          result.append("  " +ByteArray.toHexString(internalTransaction.getCallerAddress().toByteArray()));
          result.append("  \n");
          result.append("  transferTo_address:\n");
          result.append("  " +ByteArray.toHexString(internalTransaction.getTransferToAddress().toByteArray()));
          result.append("  \n");
          result.append("  callValueInfo:\n");
          StringBuilder callValueInfo = new StringBuilder("");

          internalTransaction.getCallValueInfoList().forEach(token -> {
            callValueInfo.append("  [\n");
            callValueInfo.append("    TokenName(Default trx):\n");
            if (null == token.getTokenId()|| token.getTokenId().length() == 0){
              callValueInfo.append("    TRX(TRX)");
            }
            else {
              callValueInfo.append("    " + token.getTokenId());
            }
            callValueInfo.append("    \n");
            callValueInfo.append("    callValue:\n");
            callValueInfo.append("    " +token.getCallValue());
            callValueInfo.append("  \n");
            callValueInfo.append("  ]\n");
            callValueInfo.append("    \n");
          });
          result.append(callValueInfo);
          result.append("  note:\n");
          result.append("  " + new String(internalTransaction.getNote().toByteArray()));
          result.append("  \n");
          result.append("  rejected:\n");
          result.append("  " + internalTransaction.getRejected());
          result.append("  \n");
          result.append("]\n");
        }
    );

    return result.toString();

  }



  private static String printReceipt(ResourceReceipt receipt){
    String result = "";
    result += "EnergyUsage: ";
    result += receipt.getEnergyUsage();
    result += "\n";
    result += "EnergyFee(TRX): ";
    result += receipt.getEnergyFee();
    result += "\n";
    result += "OriginEnergyUsage: ";
    result += receipt.getOriginEnergyUsage();
    result += "\n";
    result += "EnergyUsageTotal: ";
    result += receipt.getEnergyUsageTotal();
    result += "\n";
    result += "NetUsage: ";
    result += receipt.getNetUsage();
    result += "\n";
    result += "NetFee: ";
    result += receipt.getNetFee();
    result += "\n";
    return result;
  }



}
