package com.ailk.cache.memcache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ailk.cache.memcache.MemCacheFactory;
import com.ailk.cache.memcache.interfaces.IMemCache;

public class PressureTest {

	private IMemCache cache = MemCacheFactory.getCache(MemCacheFactory.BCC_CACHE);
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void test效率() {
		cache.set("123", "123");
		while (true) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < 10000; i++) {
				cache.get("123");
			}
			System.out.println("耗时:" + (System.currentTimeMillis() - start) + "毫秒");
		}
	}
	
	public void testBigObject() {
		int count = 10;
		String key = "123";
		List<Map<String, String>> value = new ArrayList<Map<String, String>>();
		for (int i = 0; i < 30000; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("1111111111111", "asdfasdfasdf");
			map.put("2222222222222", "sdfgsdfgsdfg");
			map.put("3333333333333", "dfhdfhgdfhdf");
			map.put("4444444444444", "wertwertwret");
			map.put("5555555555555", "wcewecwecw");
			map.put("6666666666666", "vvtrrtrtvrtvtrv");
			map.put("7777777777777", "myumyumuym");
			map.put("8888888888888", ",uk,uk,uk,uk,uk,");
			value.add(map);
		}
		cache.set(key, value);
		cache.get(key);

		long start = System.nanoTime();
		for (long i = 0; i < count; i++) {
			value = (List<Map<String, String>>)cache.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%f\n", count, tms, avg, 1000 / avg);
	}
	
}
