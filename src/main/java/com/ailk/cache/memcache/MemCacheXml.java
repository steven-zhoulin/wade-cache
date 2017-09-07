package com.ailk.cache.memcache;

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
 * Copyright: Copyright (c) 2013 Asiainfo
 * 
 * @className: MemCacheXml
 * @description: MemCacheXml解析类
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-16
 */
public final class MemCacheXml {
	
	private static final Logger log = Logger.getLogger(MemCacheXml.class);
	private static final String MEMCACHE_XML = "memcache.xml";
		
	/**
	 * 默认数据中心
	 */
	private static String defaultDataCenter = null;

	/**
	 * 性能采集实现类
	 */
	private static String performanceClazz = "com.ailk.cache.memcache.performance.impl.LazyWorkPerformance";
	
	/**
	 * 数据中心
	 */
	private static Map<String, Map<String, MemCacheCluster>> dataCenters = new TreeMap<String, Map<String, MemCacheCluster>>();
	
	private static Map<String, String> mapping = new HashMap<String, String>();
	
	public static Map<String, String> getMapping() {
		return mapping;
	}
	
	public static String getDefaultDataCenter() {
		return defaultDataCenter;
	}
	
	public static Map<String, Map<String, MemCacheCluster>> getDataCenters() {
		return dataCenters;
	}

	public static void setDataCenters(Map<String, Map<String, MemCacheCluster>> dataCenters) {
		MemCacheXml.dataCenters = dataCenters;
	}
		
	public void load() {
		
		SAXBuilder builder = new SAXBuilder();
		
		InputStream is = null;
		
		try {
			
			is = MemCacheXml.class.getClassLoader().getResourceAsStream(MEMCACHE_XML);
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
			log.error("加载" + MEMCACHE_XML + "配置文件出错!", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				log.error("加载" + MEMCACHE_XML + "配置文件出错!", e);	
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
		for (Element e : datacenters) {
			
			String dataCenterName = e.getAttributeValue("name"); // 数据中心名称
			
			if (StringUtils.isBlank(dataCenterName)) {
				throw new NullPointerException("数据中心名称不能为空!");
			}
			
			loadClusters(dataCenterName, e);
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
	 * @param dataCenterName
	 * @param parent
	 */
	private void loadCluster(String dataCenterName, String clusterName, Element parent) {
		
		MemCacheCluster mcCluster = new MemCacheCluster();
		mcCluster.setName(clusterName);
		
		// 获取IO模式
		Element eNIO = parent.getChild("use-nio");
		if (null != eNIO) {
			String sNIO = eNIO.getText().trim();
			if ("true".equals(sNIO)) {
				mcCluster.setUseNIO(true);
			}
		}
		
		Element eHearbeatSecond = parent.getChild("heartbeat-second");
		if (null != eHearbeatSecond) {
			String sHearbeatSecond = eHearbeatSecond.getText();
			int heartbeatSecond = Integer.parseInt(sHearbeatSecond);
			if (heartbeatSecond < 2) {
				log.warn(clusterName + " memcached心跳周期配置太短:" + heartbeatSecond + "秒,自动设置为2秒。");
				heartbeatSecond = 2;
			}
			mcCluster.setHeartbeatSecond(heartbeatSecond);
		}
		
		Element ePoolSize = parent.getChild("pool-size");
		if (null != ePoolSize) {
			String sPoolSize = ePoolSize.getText();
			int poolSize = Integer.parseInt(sPoolSize);
			if (poolSize > 5) {
				log.warn(clusterName + " memcached池配置太大:" + poolSize + ",自动设置为5个。");
				poolSize = 5;
			}
			mcCluster.setPoolSize(poolSize);
		}
		
		TreeSet<MemCacheAddress> mcAddressTreeSet = new TreeSet<MemCacheAddress>();		
		List<Element> addresses = parent.getChildren("address");
		for (Element address : addresses) {						
			MemCacheAddress mcAddress = new MemCacheAddress();
			String master = address.getAttributeValue("master");
				
			if (StringUtils.isBlank(master)) {
				throw new NullPointerException("master地址不可为空!");
			}
				
			String slave = address.getAttributeValue("slave");
			
			mcAddress.setMaster(master);
			mcAddress.setSlave(slave);
			mcAddressTreeSet.add(mcAddress);
		}
		
		mcCluster.setAddress(mcAddressTreeSet);
		Map<String, MemCacheCluster> clusters = dataCenters.get(dataCenterName);
		if (null == clusters) {
			clusters = new HashMap<String, MemCacheCluster>();
			dataCenters.put(dataCenterName, clusters);
		}
		clusters.put(clusterName, mcCluster);
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

	public static String getPerformanceClazz() {
		return performanceClazz;
	}
	
	public static void main(String[] args) {
		MemCacheXml xml = new MemCacheXml();
		xml.load();
		
		for (String datacenter : dataCenters.keySet()) {
			System.out.println(datacenter);
			
			Map<String, MemCacheCluster> mcCluster = dataCenters.get(datacenter);
			for (String key : mcCluster.keySet()) {
				System.out.println(mcCluster.get(key));
			}	
		}
	}
	
}
