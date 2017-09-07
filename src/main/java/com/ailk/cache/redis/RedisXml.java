package com.ailk.cache.redis;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.ailk.org.apache.commons.lang3.StringUtils;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: RedisXml
 * @description: RedisXml解析类
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-16
 */
public final class RedisXml {
	
	private static final Logger log = Logger.getLogger(RedisXml.class);
	private static final String REDIS_XML = "redis.xml";
	
	/**
	 * 默认数据中心名
	 */
	private static String defaultDataCenter = null;
	
	/**
	 * 性能采集实现类
	 */
	private static String performanceClazz = "com.ailk.cache.redis.performance.impl.LazyWorkPerformance";
	
	/**
	 * 数据中心
	 */
	private static Map<String, Map<String, RedisCluster>> dataCenters = new TreeMap<String, Map<String, RedisCluster>>();
	
	private static Map<String, String> mapping = new HashMap<String, String>();
	
	/**
	 * 加载配置文件
	 */
	public void load() {
		
		SAXBuilder builder = new SAXBuilder();
		
		InputStream is = null;
		try {
			
			is = RedisXml.class.getClassLoader().getResourceAsStream(REDIS_XML);
			Document doc = builder.build(is);
			Element root = doc.getRootElement();

			// 获取默认数据中心
			Element e = root.getChild("default-datacenter");
			if (null != e) {
				defaultDataCenter = e.getText().trim();
			}
			
			// 性能数据输出接口类
			Element ePerformanceClazz = root.getChild("performance");
			if (null != ePerformanceClazz) {
				performanceClazz = ePerformanceClazz.getText().trim();
			}
			log.info("performanceClazz: " + performanceClazz);
			
			loadDataCenter(root);
			loadServer(root);
			
		} catch (Exception e) {
			log.error("加载" + REDIS_XML + "配置文件出错!", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				log.error("加载" + REDIS_XML + "配置文件出错!", e);
			}
		}
	}
	
	
	/**
	 * 读取数据中心配置
	 * 
	 * @param root
	 */
	private void loadDataCenter(Element root) {
		List<Element> datacenters = root.getChildren("datacenter");
		for (Element datacenter : datacenters) {
			
			String dataCenterName = datacenter.getAttributeValue("name"); // 数据中心名称
			if (StringUtils.isBlank(dataCenterName)) {
				throw new NullPointerException("数据中心名称不能为空!");
			}
			
			loadClusters(dataCenterName, datacenter);
		}
	}
	
	/**
	 * 读取集群组配置
	 * 
	 * @param parent
	 */
	private void loadClusters(String dataCenterName, Element parent) {
		List<Element> clusters = parent.getChildren("cluster");
		for (Element cluster : clusters) {
			
			String clusterName = cluster.getAttributeValue("name"); // 集群名
			
			if (StringUtils.isBlank(clusterName)) {
				throw new NullPointerException("集群名不能为空!");
			}
			
			loadCluster(dataCenterName, clusterName, cluster);
		}
	}
	
	/**
	 * 读取集群配置
	 * 
	 * @param clusterName
	 * @param parent
	 */
	private void loadCluster(String dataCenterName, String clusterName, Element parent) {
		
		RedisCluster redisCluster = new RedisCluster();
		redisCluster.setName(clusterName);
		
		// 获取IO模式
		Element eNIO = parent.getChild("use-nio");
		if (null != eNIO) {
			String sNIO = eNIO.getText().trim();
			if ("true".equals(sNIO)) {
				redisCluster.setUseNIO(true);
			}
		}
		
		Element eHearbeatSecond = parent.getChild("heartbeat-second");
		if (null != eHearbeatSecond) {
			String sHearbeatSecond = eHearbeatSecond.getText();
			int heartbeatSecond = Integer.parseInt(sHearbeatSecond);
			if (heartbeatSecond < 2) {
				log.warn(clusterName + " redis心跳周期配置太短:" + heartbeatSecond + "秒,自动设置为2秒。");
				heartbeatSecond = 2;
			}
			redisCluster.setHeartbeatSecond(heartbeatSecond);
		}
		
		Element ePoolSize = parent.getChild("pool-size");
		if (null != ePoolSize) {
			String sPoolSize = ePoolSize.getText();
			int poolSize = Integer.parseInt(sPoolSize);
			if (poolSize > 5) {
				log.warn(clusterName + " redis池配置太大:" + poolSize + ",自动设置为5个。");
				poolSize = 5;
			}
			redisCluster.setPoolSize(poolSize);
		}
		
		TreeSet<RedisAddress> RedisAddressTreeSet = new TreeSet<RedisAddress>();
		List<Element> addresses = parent.getChildren("address");
		for (Element address : addresses) {
			RedisAddress redisAddress = new RedisAddress();
			String master = address.getAttributeValue("master");
			
			if (StringUtils.isBlank(master)) {
				throw new NullPointerException("master地址不可为空!");
			}
			
			String slave = address.getAttributeValue("slave");
			redisAddress.setMaster(master);
			redisAddress.setSlave(slave);
			
			RedisAddressTreeSet.add(redisAddress);
		}
		
		redisCluster.setAddress(RedisAddressTreeSet);
		Map<String, RedisCluster> clusters = dataCenters.get(dataCenterName);
		if (null == clusters) {
			clusters = new HashMap<String, RedisCluster>();
			dataCenters.put(dataCenterName, clusters);
		}
		clusters.put(clusterName, redisCluster);
	}

	/**
	 * 读取server配置
	 * 
	 * @param root
	 */
	private void loadServer(Element root) {
		List<Element> servers = root.getChildren("server");
		for (Element e : servers) {
			String serverName = e.getAttributeValue("name");
			String connect = e.getAttributeValue("connect");
			mapping.put(serverName, connect);
		}
	}
	
	/**
	 * 返回默认数据中心名
	 * 
	 * @return
	 */
	public static String getDefaultDataCenter() {
		return defaultDataCenter;
	}
		
	public static Map<String, Map<String, RedisCluster>> getDataCenters() {
		return dataCenters;
	}
	
	public static Map<String, String> getMapping() {
		return mapping;
	}
	
	public static String getPerformanceClazz() {
		return performanceClazz;
	}
	
	public static void main(String[] args) {
		RedisXml xml = new RedisXml();
		xml.load();

		for (String datacenter : dataCenters.keySet()) {
			System.out.println(datacenter);
			Map<String, RedisCluster> redisCluster = dataCenters.get(datacenter);
			for (String key : redisCluster.keySet()) {
				System.out.println(redisCluster.get(key));
			}	
		}
	}
}