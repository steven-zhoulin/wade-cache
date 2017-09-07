package com.ailk.cache.redis.performance;

/**
 * Copyright: Copyright (c) 2016 Asiainfo
 * 
 * @className: IRedisPerformance
 * @description: Redis接口性能日志输出接口
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2016-7-25
 */
public interface IRedisPerformance {
	public void report(String cmd, String key, long cCost, long eCost);
}
