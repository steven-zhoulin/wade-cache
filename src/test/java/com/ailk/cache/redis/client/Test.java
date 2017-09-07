package com.ailk.cache.redis.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ailk.cache.redis.RedisFactory;
import com.ailk.org.apache.commons.lang3.SerializationUtils;

public class Test {
	
	private static final String KEY = "--------------";
	
	public static void main(String[] args) {
			
		RedisClient client = RedisFactory.getRedisClient("sna");
		
		Set<String> serviceNames = new HashSet<String>();
//		serviceNames.add("1");
//		serviceNames.add("2");
//		serviceNames.add("3");
		String[] names = serviceNames.toArray(new String[] {});
		client.sadd(KEY, names);
		System.out.println(client.smembers(KEY));
	
		
	}
}
