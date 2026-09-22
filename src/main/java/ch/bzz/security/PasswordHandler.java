package ch.bzz.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Erzeugt Salt und Hash fuer Benutzerpasswoerter. Das Klartextpasswort wird
 * nirgends gespeichert.
 */
public final class PasswordHandler {

    private static final String ALGORITHM = "SHA-256";

    private static final int SALT_LENGTH = 16;

    private PasswordHandler() {
        // Utility-Klasse, wird nicht instanziert
    }

    /**
     * Erzeugt ein zufaelliges Salt. Fuer jedes Passwort wird ein eigenes Salt
     * verwendet, damit gleiche Passwoerter nicht denselben Hash ergeben.
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Berechnet den Hash aus Salt und Passwort.
     */
    public static byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.update(salt);
        return digest.digest(password.getBytes());
    }
}
