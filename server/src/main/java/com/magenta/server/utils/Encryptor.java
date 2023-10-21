package com.magenta.server.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Encryptor {
    private static final int RADIX = 16;
    private static final int LENGTH = 32;

    private Encryptor() {
    }

    public static String encryptThisString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(RADIX);
            while (hashtext.length() < LENGTH) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid algorithm was chosen. Check carefully and try again.");
        }
    }
}
