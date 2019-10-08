package shop.tronlucky.trondapp.common.crypto;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.spongycastle.util.encoders.Hex;

public class DESUtil {

  private static SecureRandom random = new SecureRandom();

  /**
   * 加密
   *
   * @param content
   *            待加密内容
   * @param key
   *            加密的密钥
   * @return
   */
  public static String encrypt(byte[] content, byte[] key) {
    try {
      DESKeySpec desKey = new DESKeySpec(key);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey securekey = keyFactory.generateSecret(desKey);
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
      return Hex.toHexString(cipher.doFinal(content));
    } catch (Throwable e) {

    }
    return null;
  }

  /**
   * 解密
   *
   * @param content
   *            待解密内容
   * @param key
   *            解密的密钥
   * @return
   */
  public static byte[] decrypt(byte[] content, byte[] key) {
    try {
      DESKeySpec desKey = new DESKeySpec(key);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey securekey = keyFactory.generateSecret(desKey);
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(Cipher.DECRYPT_MODE, securekey, random);
      return cipher.doFinal(content);
    } catch (Throwable e) {

    }
    return null;
  }
}
