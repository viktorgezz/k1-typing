package ru.viktorgezz.avatarmodule.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Утилитный класс для загрузки RSA-ключей из ресурсов.
 */
public class KeyUtils {

    private KeyUtils() {
    }

    public static PublicKey loadPublicKey(final String pemPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        final String key = readKeyFromResource(pemPath).replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        final byte[] decoded = Base64.getDecoder().decode(key);
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    private static String readKeyFromResource(String pemPath) throws IOException {
        try (final InputStream inputStream = KeyUtils.class.getClassLoader().getResourceAsStream(pemPath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("file not found: " + pemPath);
            }
            return new String(inputStream.readAllBytes());
        }
    }
}
