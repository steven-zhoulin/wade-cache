package com.ailk.cache.memcache;

import com.ailk.cache.memcache.interfaces.IMemCache;

public class TestPerformance {
	
	private static IMemCache cache = MemCacheFactory.getCache(MemCacheFactory.BCC_CACHE);
	private static long count = 10000;
	
	public static void run() {
		cache.set("123", "123");
		while (true) {
			long start = System.nanoTime();
			for (int i = 0; i < count; i++) {
				cache.get("123");
			}
			double tms = (System.nanoTime() - start) * 1.0 / 1000000;
			double avg = tms * 1.0 / 10000;
			System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%f\n", count, tms, avg, 1000 / avg);
		}
	}
	
	public static void main(String[] args) {
		run();
	}
}
