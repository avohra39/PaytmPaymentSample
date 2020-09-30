// 
// Decompiled by Procyon v0.5.36
// 

package com.sample.paytmpaymentsample.api;

import android.annotation.SuppressLint;

import java.util.Formatter;
import java.security.MessageDigest;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;
import java.io.IOException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchProviderException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import java.util.Base64;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;

public class PaytmChecksum
{
    private static final String ALGTHM_TYPE_AES = "AES";
    private static final String ALGTHM_CBC_PAD_AES = "AES/CBC/PKCS5PADDING";
    private static final String ALGTHM_PROVIDER_BC = "SunJCE";
    private static final byte[] ivParamBytes;
    
    public static String encrypt(final String input, final String key) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String encryptedValue = "";
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(1, new SecretKeySpec(key.getBytes(), "AES"), new IvParameterSpec(PaytmChecksum.ivParamBytes));
        @SuppressLint("NewApi") final byte[] baseEncodedByte = Base64.getEncoder().encode(cipher.doFinal(input.getBytes()));
        encryptedValue = new String(baseEncodedByte);
        return encryptedValue;
    }
    
    public static String decrypt(final String input, final String key) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, IOException, BadPaddingException, IllegalBlockSizeException {
        String decryptedValue = "";
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(2, new SecretKeySpec(key.getBytes(), "AES"), new IvParameterSpec(PaytmChecksum.ivParamBytes));
        @SuppressLint("NewApi") final byte[] baseDecodedByte = Base64.getDecoder().decode(input);
        decryptedValue = new String(cipher.doFinal(baseDecodedByte));
        return decryptedValue;
    }
    
    public static String generateSignature(final TreeMap<String, String> params, final String key) throws Exception {
        return generateSignature(getStringByParams(params), key);
    }
    
    public static String generateSignature(final String params, final String key) throws Exception {
        final String salt = generateRandomString(4);
        return calculateChecksum(params, key, salt);
    }
    
    public static boolean verifySignature(final TreeMap<String, String> params, final String key, final String checksum) throws Exception {
        return verifySignature(getStringByParams(params), key, checksum);
    }
    
    public static boolean verifySignature(final String params, final String key, final String checksum) throws Exception {
        final String paytm_hash = decrypt(checksum, key);
        final String salt = paytm_hash.substring(paytm_hash.length() - 4);
        final String calculatedHash = calculateHash(params, salt);
        return paytm_hash.equals(calculatedHash);
    }
    
    private static String generateRandomString(final int length) {
        final String ALPHA_NUM = "9876543210ZYXWVUTSRQPONMLKJIHGFEDCBAabcdefghijklmnopqrstuvwxyz!@#$&_";
        final StringBuilder random = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            final int ndx = (int)(Math.random() * ALPHA_NUM.length());
            random.append(ALPHA_NUM.charAt(ndx));
        }
        return random.toString();
    }
    
    private static String getStringByParams(final TreeMap<String, String> params) {
        if (params == null) {
            return "";
        }
        final Set<String> keys = params.keySet();
        final StringBuilder string = new StringBuilder();
        final TreeSet<String> parameterSet = new TreeSet<String>(keys);
        for (final String paramName : parameterSet) {
            final String value = (params.get(paramName) == null) ? "" : params.get(paramName);
            string.append(value).append("|");
        }
        return string.substring(0, string.length() - 1);
    }
    
    private static String calculateChecksum(final String params, final String key, final String salt) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        final String hashString = calculateHash(params, salt);
        String checksum = encrypt(hashString, key);
        if (checksum != null) {
            checksum = checksum.replaceAll("\r\n", "").replaceAll("\r", "").replaceAll("\n", "");
        }
        return checksum;
    }
    
    private static String calculateHash(final String params, final String salt) throws NoSuchAlgorithmException {
        final String finalString = params + "|" + salt;
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        final Formatter hash = new Formatter();
        for (final byte b : messageDigest.digest(finalString.getBytes())) {
            hash.format("%02x", b);
        }
        return hash.toString().concat(salt);
    }
    
    static {
        ivParamBytes = new byte[] { 64, 64, 64, 64, 38, 38, 38, 38, 35, 35, 35, 35, 36, 36, 36, 36 };
    }
}