package com.ailk.cache.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ailk.cache.redis.RedisCluster;
import com.ailk.cache.redis.client.RedisClient;
import com.ailk.cache.redis.performance.IRedisPerformance;
import com.ailk.org.apache.commons.lang3.StringUtils;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: RedisFactory
 * @description: Redis工厂类
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
public final class RedisFactory {
	
	private static final Logger log = Logger.getLogger(RedisFactory.class);
	private static final Map<String, RedisClient> redises = new HashMap<String, RedisClient>();
	public static IRedisPerformance performance;
	
	private RedisFactory() {
		// 工厂类，无需实例化
	}
	
	/**
	 * 根据集群名获取RedisClient实例
	 * 
	 * @param clusterName
	 * @return
	 */
	public static final RedisClient getRedisClient(String clusterName) {
		RedisClient client = redises.get(clusterName);
		if (null == client) {
			throw new NullPointerException("工厂中未找到对应的RedisClient客户端实例,cluster:" + clusterName);
		}
		return client;
	}
	
	static {
		
		RedisXml xml = new RedisXml();
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
			performance = (IRedisPerformance)clazz.newInstance();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		
		Map<String, Map<String, RedisCluster>> centers = xml.getDataCenters();
		Map<String, RedisCluster> clusters = centers.get(dataCenter);
		if (null == clusters) {
			throw new NullPointerException("根据中心编码:" + dataCenter + ",无法获取中心配置数据!");
		}
		
		for (String clusterName : clusters.keySet()) {
			RedisCluster cluster = clusters.get(clusterName);
			RedisAddress[] address = cluster.getAddress().toArray(new RedisAddress[0]);

			int poolSize = cluster.getPoolSize();
			int heartbeatSecond = cluster.getHeartbeatSecond();

			RedisClient client = new RedisClient(address, poolSize, heartbeatSecond, cluster.isUseNIO());
			redises.put(clusterName, client);
			
			if (log.isInfoEnabled()) {
				log.info("Redis连接初始化成功，分组组名:" + clusterName);
			}
			
			if (log.isInfoEnabled()) {
				log.info("------ redis连接池初始化成功! ------");
				log.info("分组组名: " + clusterName);
				log.info("地址集合:");
				for (RedisAddress addr : address) {
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