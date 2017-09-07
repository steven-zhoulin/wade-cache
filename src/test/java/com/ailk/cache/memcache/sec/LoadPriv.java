package com.ailk.cache.memcache.sec;

import java.util.HashMap;
import java.util.Map;

import com.ailk.cache.redis.RedisFactory;
import com.ailk.cache.redis.client.RedisClient;

public class LoadPriv {

	private static final RedisClient redis = RedisFactory.getRedisClient("console");

	public static void main(String[] args) throws InterruptedException {

		Map<String, String> privs = new HashMap<String, String>();

		int begin = 80000000;
		int size = 30;//Integer.parseInt(args[0]);
		
		long start = 0L;
		for (int i = begin; i < begin + 200000; i++) {
			privs.put(String.valueOf(i), "");
		}
		
		start = System.currentTimeMillis();
		redis.hmset("SUPERUSR", privs);
		System.out.println("往Redis存20万权限，耗时:" + (System.currentTimeMillis() - start) + "毫秒");
		
		String[] distincts = new String[size];
		for (int i = begin; i < begin + size; i++) {
			distincts[i - begin] = String.valueOf(i);
		}
		
		System.out.println("待判权限集合大小:" + distincts.length);
		
		
		Map<String, String> m = redis.hmget("SUPERUSRXXXXXXXXXXXXXXXXXXXXXXXX", distincts);
		System.out.println(m);
		
		/*
		while (true) {
			start = System.nanoTime();
			redis.hmget("SUPERUSR", distincts);
			System.out.println("耗时:" + (System.nanoTime() - start) + "ns");
		}
		*/
	}

}
