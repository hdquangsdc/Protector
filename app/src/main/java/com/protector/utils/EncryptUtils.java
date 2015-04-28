package com.protector.utils;

import android.util.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class EncryptUtils {
	private static String key = "AcjdIML1_oIVAq28";

	public static String decryptV1(String data) {
		if (key == null || data == null)
			return null;
		try {
			byte[] dataBytes = Base64.decode(data, Base64.DEFAULT);
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF8"));
			SecretKeyFactory secretKeyFactory = SecretKeyFactory
					.getInstance("DES");
			SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] dataBytesDecrypted = (cipher.doFinal(dataBytes));
			return new String(dataBytesDecrypted);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String encryptV1(String data) {
		if (key == null || data == null)
			return null;
		try {
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF8"));
			SecretKeyFactory secretKeyFactory = SecretKeyFactory
					.getInstance("DES");
			SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
			byte[] dataBytes = data.getBytes("UTF8");
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.encodeToString(cipher.doFinal(dataBytes),
					Base64.DEFAULT);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getMD5(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(text.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}
}
