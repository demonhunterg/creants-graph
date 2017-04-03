package com.creants.graph.util;

import java.security.SecureRandom;

/**
 * @author LamHa
 *
 */
public class IdGenerator {
	private static final int UID_LENGHT = 28;
	private static final String AB = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
	private static final SecureRandom rnd = new SecureRandom();

	public static String randomString(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

	public static String generateUuid() {
		return randomString(UID_LENGHT);
	}

}
