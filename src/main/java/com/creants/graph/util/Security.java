package com.creants.graph.util;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.couchbase.client.java.document.json.JsonObject;

/**
 * @author LamHa
 *
 */
public class Security {

	public static String encryptMD5(String key) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(key.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1, digest);
		String hashtext = bigInt.toString(16);

		// Now we need to zero pad it if you actually want the full 32 chars.
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}

		return hashtext;
	}


	public static String encryptMD5(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(data);
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1, digest);
		String hashtext = bigInt.toString(16);

		// Now we need to zero pad it if you actually want the full 32 chars.
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}

		return hashtext;
	}


	public static String genPrivateKey(String token, long userId) {
		try {
			byte[] baseBin = Base64.encodeBase64((token + userId).getBytes());
			String encryptMD5 = encryptMD5(baseBin);
			return encryptMD5.substring(0, 10);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}


	public static String encrypt(String key, String clearText) throws Exception {
		Cipher c = Cipher.getInstance("RC4");
		Key mainKey = new SecretKeySpec(key.getBytes(), "RC4");
		c.init(Cipher.ENCRYPT_MODE, mainKey);
		byte[] encVal = c.doFinal(clearText.getBytes("UTF-8"));
		return Base64.encodeBase64String(encVal);
	}


	public static String decrypt(String key, String encryptedData) throws Exception {
		Cipher c = Cipher.getInstance("RC4");
		Key mainKey = new SecretKeySpec(key.getBytes(), "RC4");
		c.init(Cipher.DECRYPT_MODE, mainKey);
		byte[] decordedValue = Base64.decodeBase64(encryptedData);
		byte[] decValue = c.doFinal(decordedValue);
		String decryptedValue = new String(decValue, "UTF-8");
		return decryptedValue;
	}


	public static void main(String[] args) {
		try {
//			String genPrivateKey = genPrivateKey("eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjI1OCIsImV4cCI6MTQ5NTg1NzE5NSwiaXNzIjoiYXV0aDAiLCJ0dGwiOjg2NDAwMDAwMH0.nOVo0pmmmv_WmfU_lo42ff9I0SiaeMXwnoX3JffFbjE", 287);
			String genPrivateKey = "2b3df624f4";
			System.out.println("************ key:" + genPrivateKey);
			JsonObject jo = JsonObject.create();
			jo.put("password", "123456");
			jo.put("new_password", "123456789");
			jo.put("re_new_password", "123456789");
			String encrypt = encrypt(genPrivateKey, jo.toString());
			System.out.println("*******DATA: " + encrypt);

			System.out.println(decrypt(genPrivateKey, encrypt));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}