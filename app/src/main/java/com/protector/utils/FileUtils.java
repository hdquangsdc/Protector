package com.protector.utils;

import java.io.File;
import java.io.RandomAccessFile;

public class FileUtils {
	private static final int NUM_BYTE = 128;
	private static final byte[] SECURITY_BYTES = "test".getBytes();
	
	private static byte[] getByte(byte[] paramArrayOfByte) {
		byte[] arrayOfByte = paramArrayOfByte.clone();
		int byteToReplace = arrayOfByte.length < SECURITY_BYTES.length ? arrayOfByte.length
				: SECURITY_BYTES.length;
		for (int i = 0; i < byteToReplace; i++) {
			arrayOfByte[i] = (byte) (arrayOfByte[i] ^ SECURITY_BYTES[i]);
		}

		return arrayOfByte;
	}
	
	public static boolean changeAcessFile(String filePath) {
		try {
			File file = new File(filePath);
			RandomAccessFile localRandomAccessFile = new RandomAccessFile(file,
					"rw");
			long numByte = Math.min(NUM_BYTE, file.length());
			byte[] arrayOfByte = new byte[(int) numByte];
			localRandomAccessFile.read(arrayOfByte, 0, arrayOfByte.length);
			localRandomAccessFile.seek(0L);
			if (localRandomAccessFile.length() > arrayOfByte.length) {
				localRandomAccessFile.write(getByte(arrayOfByte));
			}
			localRandomAccessFile.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
