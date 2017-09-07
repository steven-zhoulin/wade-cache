package com.ailk.cache.localcache;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReadWriteCacheTest {
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testRemove() throws Exception {
		ReadWriteCache cache = new ReadWriteCache(10);
		for (int i = 0; i < 10; i++) {
			String k = String.valueOf(i);
			cache.put(k, k);
		}
		
		assertTrue(cache.containsKey("0"));
		assertTrue(cache.containsKey("1"));
		assertTrue(cache.containsKey("2"));
		assertTrue(cache.containsKey("9"));
		String oldValue = (String)cache.remove("9");
		assertTrue(oldValue.equals("9"));
		assertTrue(!cache.containsKey("9"));
	}
	
	@Test
	public void testIsEmpty() throws Exception {
		ReadWriteCache cache = new ReadWriteCache(10);
		assertTrue(cache.isEmpty());
		
		for (int i = 0; i < 100; i++) {
			String k = String.valueOf(i);
			cache.put(k, k);
		}
		
		assertTrue(!cache.isEmpty());
	}
	
	@Test
	public void testContainsKey() throws Exception {
		ReadWriteCache cache = new ReadWriteCache(10);
		for (int i = 0; i < 100; i++) {
			String k = String.valueOf(i);
			cache.put(k, k);
		}
		
		assertTrue(cache.containsKey("99"));
		System.out.println(cache.keySet());
	}

	@Test
	public void testPutGet() throws Exception {
		ReadWriteCache cache = new ReadWriteCache(17);
		for (int i = 0; i < 100; i++) {
			String k = String.valueOf(i);
			cache.put(k, k);
		}
		
		for (int i = 0; i < 100; i++) {
			String k = String.valueOf(i);
			String v = (String) cache.get(k);
			if (null != v) {
				System.out.println(v);
			}
		}
	}
}
