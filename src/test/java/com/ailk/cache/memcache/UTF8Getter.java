package com.ailk.cache.memcache;

import com.ailk.cache.memcache.interfaces.IMemCache;

public class UTF8Getter {
	public static void main(String[] args) {
		IMemCache cache = MemCacheFactory.getCache("bcc_cache");
		String s = (String) cache.get("123");
		System.out.println(s);
		
		
		String value = "亚信联创";
		System.setProperty("file.encoding", "UTF-8");
		
		
		System.setProperty("file.encoding", "GBK");
	}
}
