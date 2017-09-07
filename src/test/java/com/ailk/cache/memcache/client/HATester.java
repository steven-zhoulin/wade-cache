package com.ailk.cache.memcache.client;

import java.util.Date;

import com.ailk.cache.memcache.MemCacheFactory;
import com.ailk.cache.memcache.interfaces.IMemCache;

public class HATester {

	private static final IMemCache cache = MemCacheFactory.getCache(MemCacheFactory.BCC_CACHE);

	public static void main(String[] args) {
		for (int i = 0; i < 10000000000L; i++) {
			try {
				boolean b = cache.set(String.valueOf(i), i);
				System.out.println(new Date() + " set key=" + i + ", retcode=" + b);
				
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
