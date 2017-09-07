package com.ailk.cache.redis.client.performance;

import com.ailk.cache.redis.RedisFactory;
import com.ailk.cache.redis.client.BinaryRedisClient;

public class PerformanceTester {
	
	private static BinaryRedisClient client = RedisFactory.getRedisClient("sna");
	
	private static byte[] key = "KEY".getBytes();
	private static byte[] field = "FIELD".getBytes();
	private static byte[] value = "VALUE".getBytes();
	
	public static void main(String[] args) {
		
		client.del(key);
		for (int i = 0; i < 10000; i++) {
			client.lpush(key, value);
		}
		
		for (int i = 0; i < 10000; i++) {
			client.rpop(key);
		}
		
		while (true) {
			long start = System.currentTimeMillis();

			for (int i = 0; i < 10000; i++) {
				//client.lpush(key, value);
				client.set(key, value);
			}
			System.out.println("lpush耗时:" + (System.currentTimeMillis() - start) + "毫秒");
			
			start = System.currentTimeMillis();
			for (int i = 0; i < 10000; i++) {
				//client.rpop(key);
				client.get(key);
			}
			System.out.println("rpop耗时:" + (System.currentTimeMillis() - start) + "毫秒");
		}
		
	}
}
