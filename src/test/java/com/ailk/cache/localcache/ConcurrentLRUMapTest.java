package com.ailk.cache.localcache;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentLRUMapTest {
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testSize() {
		ConcurrentLRUMap<Integer, Integer> map = new ConcurrentLRUMap<Integer, Integer>(12);
		map.put(1, 1);
		
		assertTrue(map.size() == 1);
		
		map.put(1, 2);
		assertTrue(map.size() == 1);
		
		map.put(2, 2);
		assertTrue(map.size() == 2);
		
		map.put(3, 3);
		assertTrue(map.size() == 3);
		
		map.remove(3);
		assertTrue(map.size() == 2);
	}
	
	@Test
	public void testClear() {
		ConcurrentLRUMap<Integer, Integer> map = new ConcurrentLRUMap<Integer, Integer>(12);
		map.put(1, 2);
		map.clear();
		assertTrue(map.isEmpty());
	}
	
	
	
	@Test
	public void testContainsKey() {
		ConcurrentLRUMap<Integer, Integer> map = new ConcurrentLRUMap<Integer, Integer>(12);
		map.put(1, 2);
		assertTrue(map.containsKey(1));
		assertTrue(!map.containsKey(2));
	}
	
	@Test
	public void testIsEmpty() {
		ConcurrentLRUMap<Integer, Integer> map = new ConcurrentLRUMap<Integer, Integer>(12);
		assertTrue(map.isEmpty());
		map.put(1, 2);
		assertTrue(map.size() == 1);
		map.remove(0);
		assertTrue(map.size() == 1);
		map.remove(1);
		assertTrue(map.size() == 0);
	}
	
	@Test
	public void testKeySet() {
		ConcurrentLRUMap<Integer, Integer> map = new ConcurrentLRUMap<Integer, Integer>(12);
		for (int i = 0 ; i < 30; i++) {
			map.put(i, i);
		}
		System.out.println("keySet:" + map.keySet());
		
	}
	
	@Test
	public void testConcurrent() throws InterruptedException {
		final ConcurrentLRUMap<Integer, Integer> map = new ConcurrentLRUMap<Integer, Integer>();
		System.out.println("开始...");

		new Thread() {
			public void run() {
				System.out.println("写线程:" + Thread.currentThread().getId() + "启动");
				long start = System.currentTimeMillis();
				for (int i = 0; i < 8000000; i++) {
					map.put(i, i);
				}
				System.out.println("耗时:" + (System.currentTimeMillis() - start) + "毫秒");
			}
		}.start();
		Thread.sleep(10000);
		
		for (int i = 0; i < 1; i++) {
			new Thread() {
				public void run() {
					System.out.println("读线程:" + Thread.currentThread().getId() + "启动");
					long start = System.currentTimeMillis();
					for (int i = 0; i < 8000000; i++) {
						map.get(i);
					}
					System.out.println("耗时:" + (System.currentTimeMillis() - start) + "毫秒");
				}
			}.start();
		}
	}
}
