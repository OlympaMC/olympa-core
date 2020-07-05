package fr.olympa.core.bungee.login;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

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
	
	private static String getSHA512(String passwordToHash, String salt) {
		String generatedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(salt.getBytes(StandardCharsets.UTF_8));
			byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes)
				sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generatedPassword;
	}
	
	public static String getSHA512(String passwordToHash) {
		return getSHA512(passwordToHash, "DYhG9guiRVoUubWwvn2G0Fg3b0qyJfIxfs2aC9mi");
	}
}