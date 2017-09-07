package com.ailk.cache.memcache.performance;

public interface IMemCachePerformance {
	public void report(String cmd, String key, long cCost, long eCost);
}