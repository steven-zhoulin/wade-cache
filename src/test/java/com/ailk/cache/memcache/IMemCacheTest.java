package com.ailk.cache.memcache;

import com.ailk.cache.memcache.interfaces.IMemCache;

public class IMemCacheTest {
	public static void main(String[] args) {
		int count = 10000;
		IMemCache client = MemCacheFactory.getCache(MemCacheFactory.BCC_CACHE);
		client.set("123", "0123456789");
		
		while (true) {
			long start = System.nanoTime();
			Object rtn = null;
			for (long i = 0; i < count; i++) {
				rtn = client.get("123");
			}
			double tms = (System.nanoTime() - start) * 1.0 / 1000000;
			double avg = tms * 1.0 / count;
			System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		}
	}
}
