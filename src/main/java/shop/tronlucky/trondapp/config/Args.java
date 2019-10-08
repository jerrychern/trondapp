package shop.tronlucky.trondapp.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.typesafe.config.Config;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import shop.tronlucky.trondapp.common.crypto.ECKey;
import shop.tronlucky.trondapp.common.utils.WalletUtil;


@Slf4j
@Component
public class Args {

  public static Args instance;

  @Parameter(names = {"-c", "--config"}, description = "Config File")
  private String shellConfFileName = "";

  @Getter
  private String fullNode;
  @Getter
  private List<String> fullNodeList;
  @Getter
  private String solidity;

  @Getter
  private byte[] cooPrivateKey;

  @Getter
  private byte[] cooAddress;

  @Getter
  private byte[] bttContract;

  @Getter
  private Map<String, Properties> mysqlReadConfs = new HashMap<>();
  @Getter
  private Map<String, Properties> mysqlWriteConfs = new HashMap<>();


  /**
   * set parameters.
   */
  private Args() {
  }

  public static Args getInstance() {
    if (instance == null) {
      instance = new Args();
    }
    return instance;
  }

  public void setParam(String[] args) {
    JCommander.newBuilder().addObject(instance).build().parse(args);
    loadConf(shellConfFileName);
  }

  public void loadConf(String confName) {
    if (StringUtils.isEmpty(confName)) {
      confName = "config.conf";
    }
    Config config = Configuration.getByPath(confName);
    this.fullNode = config.getStringList("fullnode.iplist").get(0);
    this.fullNodeList = config.getStringList("fullnode.iplist");

    this.solidity = config.getStringList("solitity.iplist").get(0);

    this.bttContract = WalletUtil.decodeFromBase58Check(config.getString("BTT_CONTRACT"));
    this.cooPrivateKey = Hex.decode(config.getString("COO_PRIVATE_KEY"));
    ECKey ecKey = ECKey.fromPrivate(cooPrivateKey);
    this.cooAddress = ecKey.getAddress();
  }

  public static void main(String[] args) {
    Args.getInstance().setParam(args);
  }

}
