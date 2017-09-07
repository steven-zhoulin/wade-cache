package com.ailk.cache.localcache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapSpeedTest {
	
	private static final int MAP_SIZE = 10000;
	
	public static void main(String[] args) {
		String key = null;
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < MAP_SIZE; i++) {
			key = UUID.randomUUID().toString();
			map.put(key, key);
		}
		
		for (int i = 0; i < MAP_SIZE; i++) {
			map.get(key);
		}
		
		System.out.printf("map.size()=%d,在预热完毕...\n", MAP_SIZE);
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < 8; i++) {
			map.get(key);
		}
		long elipse = System.currentTimeMillis() - start;
		System.out.println("==============耗时:" + elipse);
	}
}
