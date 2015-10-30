package com.freeme.themeclub.updateself;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CryptUtil {

    private static final String algorithm = "DES/ECB/NoPadding";

    public static byte[] encrypt(byte[] src, byte[] key) throws Exception {
        src = padding(src, (byte) 0);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

        DESKeySpec dks = new DESKeySpec(key);
        SecretKey securekey = keyFactory.generateSecret(dks);

        Cipher cipher = Cipher.getInstance(algorithm);

        cipher.init(1, securekey);

        return cipher.doFinal(src);
    }

    public static byte[] decrypt(byte[] src, byte[] key) throws Exception {
        DESKeySpec dks = new DESKeySpec(key);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);

        Cipher cipher = Cipher.getInstance(algorithm);

        cipher.init(2, securekey);

        return cipher.doFinal(src);
    }

    private static byte[] padding(byte[] sourceBytes, byte b) {
        int paddingSize = 8 - (sourceBytes.length % 8);
        byte[] paddingBytes = new byte[paddingSize];
        for (int i = 0; i < paddingBytes.length; ++i) {
            paddingBytes[i] = b;
        }
        sourceBytes = addAll(sourceBytes, paddingBytes);
        return sourceBytes;
    }

    public static byte[] addAll(byte[] array1, byte[] array2) {
        if (array1 == null)
            return clone(array2);
        if (array2 == null) {
            return clone(array1);
        }
        byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static byte[] clone(byte[] array) {
        if (array == null) {
            return null;
        }
        return ((byte[]) (byte[]) array.clone());
    }
}
