package com.ailk.cache.memcache.client;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ailk.cache.memcache.MemCacheFactory;
import com.ailk.cache.memcache.interfaces.IMemCache;
import com.ailk.cache.memcache.util.SharedCache;

public class TextClientTest {

	private IMemCache cache = MemCacheFactory.getCache(MemCacheFactory.BCC_CACHE);
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testSetStringObject() {
		String cacheKey = "123";
		Set<String> value = new HashSet<String>();
		value.add("ABC");
		value.add("EDF");
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringByte() {
		String cacheKey = "123";
		byte value = 'A';
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringInteger() {
		String cacheKey = "123";
		Integer value = new Integer(123);
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringCharacter() {
		String cacheKey = "123";
		Character value = 'z';
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringString() {
		String cacheKey = "123";
		String value = "zhoulin";
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringStringBuffer() {
		String cacheKey = "123";
		StringBuffer value = new StringBuffer("zhoulin");
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value.toString(), cache.get(cacheKey).toString());
	}

	@Test
	public void testSetStringStringBuilder() {
		String cacheKey = "123";
		StringBuilder value = new StringBuilder("zhoulin");
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value.toString(), cache.get(cacheKey).toString());
	}

	@Test
	public void testSetStringFloat() {
		String cacheKey = "123";
		Float value = 123.456f;
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringShort() {
		String cacheKey = "123";
		Short value = 123;
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringDouble() {
		String cacheKey = "123";
		Double value = 123.456;
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringDate() {
		String cacheKey = "123";
		Date value = new Date();
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testSetStringByteArray() {
		String cacheKey = "123";
		byte[] value = "ABC".getBytes();
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertArrayEquals(value, (byte[])cache.get(cacheKey));
	}

	@Test
	public void testSetStringBoolean() {
		String cacheKey = "123";
		boolean value1 = true;
		
		boolean b = cache.set(cacheKey, value1);
		assertTrue(b);
		assertEquals(value1, cache.get(cacheKey));
		
		Boolean value2 = new Boolean(false);
		
		b = cache.set(cacheKey, value2);
		assertTrue(b);
		assertEquals(value2, cache.get(cacheKey));
	}

	@Test
	public void testSetStringLong() {
		String cacheKey = "123";
		long value1 = 1234567890987645L;
		
		boolean b = cache.set(cacheKey, value1);
		assertTrue(b);
		assertEquals(value1, cache.get(cacheKey));
		
		Long value2 = new Long(1234567890987645L);
		
		b = cache.set(cacheKey, value2);
		assertTrue(b);
		assertEquals(value2, cache.get(cacheKey));
	}

	@Test
	public void testSetStringObjectInt() {
		String cacheKey = "123";
		int value = 12345678;
		
		boolean b = cache.set(cacheKey, value, 2);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
		try {
			Thread.sleep(3000);
			assertNull("测试K-V超时", cache.get(cacheKey));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSetStringByteInt() {
		
	}

	@Test
	public void testSetStringIntegerInt() {
		
	}

	@Test
	public void testSetStringCharacterInt() {
		
	}

	@Test
	public void testSetStringStringInt() {
		
	}

	@Test
	public void testSetStringStringBufferInt() {
		
	}

	@Test
	public void testSetStringStringBuilderInt() {
		
	}

	@Test
	public void testSetStringFloatInt() {
		
	}

	@Test
	public void testSetStringShortInt() {
		
	}

	@Test
	public void testSetStringDoubleInt() {
		
	}

	@Test
	public void testSetStringDateInt() {
		
	}

	@Test
	public void testSetStringByteArrayInt() {
		
	}

	@Test
	public void testSetStringBooleanInt() {
		
	}

	@Test
	public void testSetStringLongInt() {
		
	}

	@Test
	public void testKeyExists() {
		String cacheKey = "123";
		String value = "ABC";
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		assertTrue(cache.keyExists(cacheKey));
		cache.delete(cacheKey);
		assertFalse(cache.keyExists(cacheKey));
	}

	@Test
	public void testGet() {
		assertNull(cache.get(null));
		assertNull(cache.get(new String(new byte[500])));
	}

	@Test
	public void testAddStringLong() {
		String cacheKey = "123";
		long value = 123123123123123L;

		cache.delete(cacheKey);
		boolean b = cache.add(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testAddStringLongInt() {
		String cacheKey = "123456";
		long value = 123123123123123L;
		cache.delete(cacheKey);
		boolean b = cache.add(cacheKey, value);
		assertTrue(b);
		assertEquals(value, cache.get(cacheKey));
	}

	@Test
	public void testDelete() {
		String cacheKey = "123456";
		long value = 123123123123123L;
		
		boolean b = cache.set(cacheKey, value);
		assertTrue(b);
		cache.delete(cacheKey);
		assertNull(cache.get(cacheKey));
	}

	@Test
	public void testIncrString() {
		String cacheKey = "123";
		cache.delete(cacheKey);
		assertEquals(1, cache.incr(cacheKey));
		assertEquals(2, cache.incr(cacheKey));
		assertEquals(3, cache.incr(cacheKey));
	}

	@Test
	public void testIncrStringInt() {
		String cacheKey = "123";
		
		cache.delete(cacheKey);
		assertEquals(2, cache.incr(cacheKey, 2));
		assertEquals(4, cache.incr(cacheKey, 2));
		assertEquals(6, cache.incr(cacheKey, 2));
		
		cache.delete(cacheKey);
		assertEquals(5, cache.incr(cacheKey, 5));
		assertEquals(20, cache.incr(cacheKey, 15));
		assertEquals(45, cache.incr(cacheKey, 25));
	}

	@Test
	public void testIncrWithTTLStringInt() throws InterruptedException {
		String cacheKey = "123";
		cache.delete(cacheKey);
		
		cache.incrWithTTL(cacheKey, 1);
		assertNotNull(cache.get(cacheKey));
		Thread.sleep(2000);
		assertFalse(cache.keyExists(cacheKey));
	}

	@Test
	public void testIncrWithTTLStringIntInt() throws InterruptedException {
		String cacheKey = "123";
		cache.delete(cacheKey);
		
		cache.incrWithTTL(cacheKey, 10, 1);
		assertEquals("10", cache.get(cacheKey));
		Thread.sleep(2000);
		assertFalse(cache.keyExists(cacheKey));
	}

	
	public void testDecrString() {
		String cacheKey = "123";
		cache.delete(cacheKey);
		
		for (int i = 0; i < 100; i++) {
			cache.incr(cacheKey);
			cache.decr(cacheKey);
		}
		
		assertEquals("0", cache.get(cacheKey));
	}

	@Test
	public void testDecrStringInt() {
		String cacheKey = "123";
		cache.delete(cacheKey);
		
		for (int i = 0; i < 100; i++) {
			cache.incr(cacheKey, 2);
			cache.decr(cacheKey, 2);
		}
		
		assertEquals("0", cache.get(cacheKey));
	}

	@Test
	public void testTouch() throws InterruptedException {
		String cacheKey = "123";
		String value = "ABC";
		
		cache.delete(cacheKey);
		assertNull(cache.get(cacheKey));
		cache.set(cacheKey, value);
		assertTrue(cache.touch(cacheKey, 1));
		assertTrue(cache.keyExists(cacheKey));
		Thread.sleep(2000);
		assertFalse(cache.keyExists(cacheKey));
	}

	@Test
	public void testWrongKey() {
		
		boolean b = cache.set("BCC_CRM_CUST_TOUCH_13907319902 _23061", 0L);
		System.out.println("step1: b=" + b);
		
		b = cache.set("BCC_CRM_CUST_TOUCH_13907319902_23061", 0L);
		System.out.println("step2: b=" + b);
		
		long rtn = (Long)cache.get("BCC_CRM_CUST_TOUCH_13907319902_23061");
		System.out.println("step3: rtn=" + rtn);
	}
	
	public void testCurrent() throws InterruptedException {
		System.out.println("----------------------------------------");
		System.out.println("                并发测试                                ");
		System.out.println("----------------------------------------");
		
		for (int i = 1; i <= 10; i++) {
			new Thread("setworker-" + i) {
				public void run() {
					for (long n = 0; n < 100000000000000L; n++) {
						cache.set(String.valueOf(n), String.valueOf(n));
					}
				}
			}.start();
		}
	
		Thread.sleep(500);
		
		new Thread("getworker") {
			public void run() {
				for (long n = 0; n < 100000000000000L; n++) {
					String rtn = (String) cache.get(String.valueOf(n));
					System.out.println(rtn);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		
		Thread.sleep(1000 * 3000);
	}
	
}
