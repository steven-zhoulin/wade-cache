package com.ailk.cache.memcache;

import java.util.HashMap;
import java.util.Map;

import com.ailk.org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ailk.cache.memcache.interfaces.IMemCache;
import com.ailk.cache.memcache.performance.IMemCachePerformance;
import com.ailk.cache.memcache.client.TextClient;
import com.ailk.cache.redis.RedisFactory;
import com.ailk.cache.redis.performance.IRedisPerformance;

/**
 * Copyright: Copyright (c) 2013 Asiainfo
 * 
 * @className: MemCacheFactory
 * @description: Memcache工厂类
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-18
 */
public final class MemCacheFactory {
	
	private static final Logger log = Logger.getLogger(MemCacheFactory.class);
	private static final Map<String, IMemCache> caches = new HashMap<String, IMemCache>();
	public static IMemCachePerformance performance;
	
	/**
	 * 不可能的值，为默认值而设计，防止缓存被击穿。
	 */
	public static final String IMPOSSIBLE_VALUE = "<-- IMPOSSIBLE_VALUE -->";
	
	/**
	 * 会话缓存
	 */
	public static final String SESSION_CACHE = "session_cache";
	
	/**
	 * 权限缓存
	 */
	public static final String PRIV_CACHE = "priv_cache";
	
	/**
	 * CODECODE结果集缓存
	 */
	public static final String CODECODE_CACHE = "codecode_cache";
	
	/**
	 * 静态参数缓存
	 */
	public static final String STATICPARAM_CACHE = "staticparam_cache";
	
	/**
	 * 并发控制缓存
	 */
	public static final String BCC_CACHE = "bcc_cache";
	
	
	/**
	 * DMB结果集缓存
	 */
	public static final String DMB_CACHE = "dmb_cache";
	
	private MemCacheFactory() {
		// 工厂类，无需实例化
	}
		
	/**
	 * 根据名称获取对应的Cache
	 * 
	 * @param cacheName
	 * @return
	 */
	public static IMemCache getCache(String cacheName) {
		
		IMemCache cache = caches.get(cacheName);
		
		if (null == cache) {
			throw new IllegalArgumentException(cacheName + "连接池中没有可用的连接，请确认缓存地址是否配置正确、缓存是否开启！");
		}
		
		return cache;
		
	}
		
	public static void init() throws Exception {
		// 初始化分布式缓存		
	}
	
	static {
				
		MemCacheXml xml = new MemCacheXml();
		xml.load();
		
		// 默认数据中心（配了默认中心就走默认中心）
		String dataCenter = xml.getDefaultDataCenter();
		
		if (StringUtils.isBlank(dataCenter)) {
			String serverName = System.getProperty("wade.server.name");
			if (StringUtils.isBlank(serverName)) {
				throw new NullPointerException("生产模式下必须配置wade.server.name启动参数!");
			}
			
			for (String key : xml.getMapping().keySet()) {
				String prefix = StringUtils.stripEnd(key, "*");
				if (serverName.startsWith(prefix)) { // 找到当前server所对应的数据中心名
					dataCenter = xml.getMapping().get(key);
					break;
				}
			}
		}
		
		String performanceClazz = xml.getPerformanceClazz();
		try {
			Class<?> clazz = Class.forName(performanceClazz);
			performance = (IMemCachePerformance)clazz.newInstance();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		
		Map<String, Map<String, MemCacheCluster>> centers = xml.getDataCenters();
		Map<String, MemCacheCluster> clusters = centers.get(dataCenter);
		if (null == clusters) {
			throw new NullPointerException("根据中心编码:" + dataCenter + ",无法获取中心配置数据!");
		}
		
		for (String clusterName : clusters.keySet()) {
			MemCacheCluster cluster = clusters.get(clusterName);
			MemCacheAddress[] address = cluster.getAddress().toArray(new MemCacheAddress[0]);
			
			int poolSize = cluster.getPoolSize();
			int heartbeatSecond = cluster.getHeartbeatSecond();
			
			IMemCache cache = new TextClient(address, poolSize, heartbeatSecond, cluster.isUseNIO());
			caches.put(clusterName, cache);
			
			if (log.isInfoEnabled()) {
				log.info("------ memcached连接池初始化成功! ------");
				log.info("分组组名: " + clusterName);
				log.info("地址集合:");
				for (MemCacheAddress addr : address) {
					if (null != addr.getSlave()) {
						log.info("  master " + addr.getMaster() + " -> slave " + addr.getSlave());
					} else {
						log.info("  " + addr.getMaster());
					}
				}
				log.info("连接数量: " + poolSize);
				log.info("心跳周期: " + heartbeatSecond);
				log.info("IO模式: " + (cluster.isUseNIO() ? "NIO" : "BIO") + "\n"); 
			}
			
		}

	}
	
}
