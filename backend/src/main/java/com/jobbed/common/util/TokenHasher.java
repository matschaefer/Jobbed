package com.jobbed.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Erzeugt kryptographisch zufällige, opake Tokens und deren SHA-256-Hash.
 * In der Datenbank wird ausschließlich der Hash gespeichert – nie der Klartext.
 */
public final class TokenHasher {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private TokenHasher() {
    }

    /** Erzeugt einen URL-sicheren Zufallstoken (Standard: 32 Byte Entropie). */
    public static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }

    /** Bildet den SHA-256-Hex-Hash eines Klartext-Tokens. */
    public static String sha256(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nicht verfügbar", e);
        }
    }
}
