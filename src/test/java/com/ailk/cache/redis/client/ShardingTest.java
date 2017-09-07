package com.ailk.cache.redis.client;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ailk.cache.redis.RedisFactory;
import com.ailk.cache.redis.client.BinaryRedisClient;

public class ShardingTest {

	private static BinaryRedisClient client = RedisFactory.getRedisClient("mq");
	private static byte[] key = "KEY".getBytes();
	private static byte[] field = "FIELD".getBytes();
	private static byte[] value = "VALUE".getBytes();

	@Test
	public void setget() throws InterruptedException {

		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < 20; i++) {
			String key = UUID.randomUUID().toString();
			String value = key;
			map.put(key, value);
		}
		
		for (String key : map.keySet()) {
			client.set(key.getBytes(), map.get(key).getBytes());
		}
	
		for (String key : map.keySet()) {
			String value = map.get(key);
			byte[] bytes = client.get(key.getBytes());
			String actual = new String(bytes);
			assertEquals(value, actual);
		}
		Thread.sleep(1000 * 1000);
	}
}
