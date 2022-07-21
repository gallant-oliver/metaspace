package io.zeta.metaspace.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @author lixiang03
 * @Data 2019/9/10 14:29
 */
public class AESUtils {

    private AESUtils() {
    }

    private static final String AES = "AES";
    private static final String AesKey = "metaspace";

    /**
     * 加密
     */
    public static String aesEncode(String password) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(AES);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(AesKey.getBytes(StandardCharsets.UTF_8));
            keygen.init(128, secureRandom);
            SecretKey originalKey = keygen.generateKey();
            byte[] raw = originalKey.getEncoded();
            SecretKey key = new SecretKeySpec(raw, AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byteEncode = password.getBytes(StandardCharsets.UTF_8);
            byte[] byteAES = cipher.doFinal(byteEncode);
            return Base64.getEncoder().encodeToString(byteAES);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密
     */
    public static String aesDecode(String aesPassword) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(AES);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(AesKey.getBytes(StandardCharsets.UTF_8));
            keygen.init(128, secureRandom);
            SecretKey originalKey = keygen.generateKey();
            byte[] raw = originalKey.getEncoded();
            SecretKey key = new SecretKeySpec(raw, AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] byteContent = Base64.getDecoder().decode(aesPassword);
            byte[] byteDecode = cipher.doFinal(byteContent);
            return new String(byteDecode, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
