package binary.wz.im.common.util;

import binary.wz.im.common.exception.ImException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @author binarywz
 * @date 2022/5/29 23:47
 * @description:
 */
public class Encryption {
    /**
     * 加密
     * @param key
     * @param initVector
     * @param value
     * @return
     */
    public static byte[] encrypt(String key, String initVector, byte[] value) {
        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            return Base64.encodeBase64(cipher.doFinal(value));
        } catch (Exception e) {
            throw new ImException("[Encryption] encrypt chat msg failed", e);
        }
    }

    /**
     * 解密
     * @param key
     * @param initVector
     * @param encrypted
     * @return
     */
    public static byte[] decrypt(String key, String initVector, byte[] encrypted) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(Base64.decodeBase64(encrypted));
        } catch (Exception e) {
            throw new ImException("[Encryption] decrypt chat msg failed", e);
        }
    }
}
