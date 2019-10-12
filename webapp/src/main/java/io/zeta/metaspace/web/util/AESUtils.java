// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.util;

import org.junit.Test;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author lixiang03
 * @Data 2019/9/10 14:29
 */
public class AESUtils {
    private static final String AES = "AES";
    private static final String CHARACTER = "utf-8";
    private static final String AESKey = "metaspace";

    /**
     * 加密
     * @param password
     * @return
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static String AESEncode(String password) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(AES);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
            secureRandom.setSeed(AESKey.getBytes(CHARACTER));
            keygen.init(128,secureRandom);
            SecretKey originalKey = keygen.generateKey();
            byte[] raw = originalKey.getEncoded();
            SecretKey key = new SecretKeySpec(raw,AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] byteEncode = password.getBytes(CHARACTER);
            byte[] byteAES = cipher.doFinal(byteEncode);
            return new String(new BASE64Encoder().encode(byteAES));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 解密
     * @param AESPassword
     * @return
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static String AESDecode(String AESPassword) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(AES);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
            secureRandom.setSeed(AESKey.getBytes(CHARACTER));
            keygen.init(128,secureRandom);
            SecretKey originalKey = keygen.generateKey();
            byte[] raw = originalKey.getEncoded();
            SecretKey key = new SecretKeySpec(raw,AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE,key);
            byte[] byteContent = new BASE64Decoder().decodeBuffer(AESPassword);
            byte[] byteDecode = cipher.doFinal(byteContent);
            return new String(byteDecode,CHARACTER);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
//    @Test
//    public void test()  {
//        String password = "abcdefg";
//        String AESPassword = AESEncode(password);
//        String password2 = AESDecode(AESPassword);
//        System.out.println(AESPassword);
//        System.out.println(password2);
//    }
}
