package com.ailk.cache.memcache.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SharedCacheTest {

	private static final String KEY = "1234567890";
	private static final String VALUE = "ABC";
	
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testSetStringObject() {
		assertTrue(SharedCache.set(KEY, VALUE));
		assertEquals(VALUE, SharedCache.get(KEY));
	}

	@Test
	public void testSetStringObjectInt() throws InterruptedException {
		assertTrue(SharedCache.set(KEY, VALUE, 2)); // 两秒后超时
		Thread.sleep(1000 * 3);
		assertFalse(SharedCache.keyExist(KEY));
	}

	@Test
	public void testKeyExist() throws InterruptedException {
		assertTrue(SharedCache.set(KEY, VALUE, 1)); // 两秒后超时
		Thread.sleep(1000 * 2);
		assertFalse(SharedCache.keyExist(KEY));
	}

	@Test
	public void testGet() {
		assertTrue(SharedCache.set(KEY, VALUE));
		assertEquals(VALUE, SharedCache.get(KEY));
	}

	@Test
	public void testDelete() {
		assertTrue(SharedCache.set(KEY, VALUE));
		assertTrue(SharedCache.delete(KEY));
		assertEquals(null, SharedCache.get(KEY));
	}

}
