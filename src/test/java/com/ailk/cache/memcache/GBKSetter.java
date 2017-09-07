package com.ailk.cache.memcache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.ailk.cache.memcache.interfaces.IMemCache;

public class GBKSetter {
	
	private static final char[] hexchar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static MessageDigest alg;

	static {
		try {
			alg = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public static final String hexdigest(byte[] bytes) {
		alg.update(bytes);
		byte[] digest = alg.digest();
		StringBuilder sb = new StringBuilder(digest.length * 2);
		for (int i = 0; i < digest.length; i++) {
			sb.append(hexchar[(digest[i] & 0xf0) >>> 4]);
			sb.append(hexchar[digest[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static final String hexdigest(String str) {
		alg.update(str.getBytes());
		byte[] digest = alg.digest();
		StringBuilder sb = new StringBuilder(digest.length * 2);
		for (int i = 0; i < digest.length; i++) {
			sb.append(hexchar[(digest[i] & 0xf0) >>> 4]);
			sb.append(hexchar[digest[i] & 0x0f]);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(hexdigest("亚信联创"));
		System.out.println("file.encoding:" + System.getProperty("file.encoding"));
	}
}
