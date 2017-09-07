package com.ailk.cache.memcache.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GlobalLockTest {

	@Before
	public void setUp() throws Exception {
		GlobalLock.unlock("1234567890");
	}

	@After
	public void tearDown() throws Exception {
		GlobalLock.unlock("1234567890");
	}

	@Test
	public void testLockString() {
		assertTrue(GlobalLock.lock("1234567890"));
		assertFalse(GlobalLock.lock("1234567890"));
		
		assertTrue(GlobalLock.unlock("1234567890"));
		assertFalse(GlobalLock.unlock("1234567890"));
	}

	@Test
	public void testLockStringInt() throws InterruptedException {
		assertTrue(GlobalLock.lock("1234567890", 2));
		Thread.sleep(1000 * 3);
		assertTrue(GlobalLock.lock("1234567890"));
	}

	@Test
	public void testLockWaitStringInt() throws InterruptedException {
		assertTrue(GlobalLock.lock("1234567890", 2));
		assertFalse(GlobalLock.lockWait("1234567890", 1));
		assertTrue(GlobalLock.lockWait("1234567890", 2));
	}

	@Test
	public void testLockWaitStringIntInt() {
		assertTrue(GlobalLock.lock("1234567890", 2)); // 两秒超时
		assertFalse(GlobalLock.lockWait("1234567890", 2, 1));
		assertTrue(GlobalLock.lockWait("1234567890", 2, 2));
	}

}
