package com.ailk.cache.memcache;

import java.util.HashMap;
import java.util.Map;

import com.ailk.cache.memcache.interfaces.IMemCache;
import com.ailk.cache.redis.RedisFactory;
import com.ailk.cache.redis.client.RedisClient;
import com.ailk.org.apache.commons.lang3.SerializationUtils;

public class TestGet {
	
	private static final String KEY = "--------------";
	
	public static void main(String[] args) throws InterruptedException {
		IMemCache cache = MemCacheFactory.getCache(MemCacheFactory.BCC_CACHE);
		
		HashMap<String, String> m = new HashMap<String, String>();
		for (int i = 10000001; i <= 10005000; i++) {
			String k = String.valueOf(i);
			m.put(k, k);
		}
		
		
		cache.delete(KEY);
		
		boolean b = cache.set(KEY, m);
		System.out.println("set " + (b ? "成功" : "失败"));
		
		Object rtn = cache.get(KEY);
		//System.out.println(rtn);
		System.out.println("数据大小:" + ((Map<String, String>) rtn).size());
	}
	
}
