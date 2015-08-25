package com.automic.azure.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility Class to generate authentication
 * 
 *
 */
public final class StorageAuthenticationUtil {

    private StorageAuthenticationUtil() {

    }

    /**
     * 
     * @param headerStringToSign
     * @param secretKey
     * @return
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public static byte[] generateHMACSHA256WithKey(String headerStringToSign, String secretKey)
            throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] dataToMac = headerStringToSign.getBytes("UTF-8");
        byte[] rawMac = null;
        byte[] decodedSecretKey = Base64.decodeBase64(secretKey);

        // generate signing key
        SecretKeySpec signingKey = new SecretKeySpec(decodedSecretKey, "HmacSHA256");

        // initialize MAC with signing key
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(signingKey);

        // sign header with MAC
        rawMac = hmacSha256.doFinal(dataToMac);

        // encode signature with base64
        return Base64.encodeBase64(rawMac);
    }

}
