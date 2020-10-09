package fr.olympa.core.bungee.login;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Passwords {

	private static final Random RANDOM = new SecureRandom();

	/**
	 * Generates a random password of a given length, using letters and digits.
	 * @param length the length of the password
	 * @return a random password
	 */
	public static String generateRandomPassword(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int c = RANDOM.nextInt(62);
			if (c <= 9)
				sb.append(String.valueOf(c));
			else if (c < 36)
				sb.append((char) ('a' + c - 10));
			else
				sb.append((char) ('A' + c - 36));
		}
		return sb.toString();
	}

	private static String getPBKDF2(String passwordToHash, String salt) {
		String generatedPassword = null;
		try {
			KeySpec spec = new PBEKeySpec(passwordToHash.toCharArray(), salt.getBytes(), 65536, 128 * 4);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = factory.generateSecret(spec).getEncoded();
			StringBuilder sb = new StringBuilder();
			for (byte b : hash)
				sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return generatedPassword;
	}

	public static String getPBKDF2(String passwordToHash) {
		return getPBKDF2(passwordToHash, "6irXA1wSbY5jkJLbZbU3j_tDB6hhT4MrtKtnUY_wkxCrBRbGeB");
	}
}