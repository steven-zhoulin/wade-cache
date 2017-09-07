package com.ailk.cache.memcache.driver.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.ailk.cache.memcache.MemCacheAddress;
import com.ailk.cache.memcache.MemCacheXml;
import com.ailk.org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: SockIOPool
 * @description: Socket连接池
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-24
 */
public class SockIOPool {

	private static final Logger log = Logger.getLogger(SockIOPool.class);

	/**
	 * 一个Pool可以跨多个memcached实例，每个实例是一个SockIOBucket桶，根据cacheKey的hash值定位到桶
	 */
	private List<SockIOBucket> buckets = new ArrayList<SockIOBucket>();

	/**
	 * 挂掉的桶列表
	 */
	private List<SockIOBucket> deadBuckets = new ArrayList<SockIOBucket>();

	private int heartbeatSecond = 5;
	
	/**
	 * 连接池的构造函数
	 * 
	 * @param address
	 *            地址(复数)
	 * @param poolSize
	 *            连接池大小
	 */
	public SockIOPool(MemCacheAddress[] address, int poolSize, int heartbeatSecond, boolean useNIO) {
		
		this.heartbeatSecond = heartbeatSecond;
		
		for (MemCacheAddress addr : address) {

			// 主地址解析
			String[] masterPart = StringUtils.split(addr.getMaster(), ':');
			if (2 != masterPart.length) {
				throw new IllegalArgumentException("memcached主地址格式不正确！" + addr.getMaster());
			}

			String masterHost = masterPart[0];
			int masterPort = Integer.parseInt(masterPart[1]);
			
			// 备地址解析
			String[] slavePart = StringUtils.split(addr.getSlave(), ':');
			if (null != slavePart) {
				if (2 != slavePart.length) {
					throw new IllegalArgumentException("memcached备地址格式不正确！" + addr.getSlave());
				}
			}
			
			SockIOBucket bucket = null;

			try {
				if (null == addr.getSlave()) {
					bucket = new SimpleSockIOBucket(masterHost, masterPort, poolSize, useNIO);
				} else {
					String slaveHost = slavePart[0];
					int slavePort = Integer.parseInt(slavePart[1]);
					bucket = new HASockIOBucket(masterHost, masterPort, slaveHost, slavePort, poolSize, useNIO);
				}
				
				buckets.add(bucket);
				bucket.init();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		/**
		 * 启动连接池心跳线程
		 */
		MaintTask task = new MaintTask();
		task.setDaemon(true);
		task.start();
	}

	/**
	 * 根据key获取一个socket连接
	 * 
	 * @param cacheKey
	 * @return
	 */
	public ISockIO getSock(String cacheKey) {
		int hashCode = hash(cacheKey);
		int divisor = buckets.size();

		if (0 == divisor) {
			return null; // 当桶全部挂死时
		}

		int position = hashCode % divisor;
		position = (position < 0) ? -position : position;

		SockIOBucket bucket = buckets.get(position);
		return bucket.borrowSockIO();
	}

	/**
	 * 给cacheKey做hash运算
	 * 
	 * @param cacheKey
	 * @return
	 */
	private static int hash(String cacheKey) {
		int h = cacheKey.hashCode(); // 当前是直接调用的String.hashCode()，是否不同版本JDK实现不一致?
		return h;
	}

	/**
	 * 连接池后台维护线程
	 */
	private final class MaintTask extends Thread {

		public void run() {

			while (true) {
				try {
					Thread.sleep(1000 * heartbeatSecond);

					// 桶心跳检查
					bucketHeartbeat();

					// 桶失败重连
					bucketReconnect();
				} catch (Exception e) {
					log.error("memcache连接池心跳线程发生异常!", e);
				}
			}
		}

		/**
		 * 桶心跳检查
		 */
		private void bucketHeartbeat() {
			try {
				Iterator<SockIOBucket> iter = buckets.iterator();
				while (iter.hasNext()) {
					SockIOBucket bucket = iter.next();
					
					int preStateCode = bucket.getStateCode();
					int curStateCode = bucket.healthCheck();
					
					if (SockIOBucket.STATE_ER == curStateCode || SockIOBucket.STATE_ERER == curStateCode) {
						bucket.close();
						iter.remove();
						deadBuckets.add(bucket);
						log.error("memcached桶心跳失败！" + bucket.getAddress());
					} else if (preStateCode != curStateCode) { // 触发状态变更
						log.info("桶状态变更: " + SockIOBucket.STATES[preStateCode] + " -> " + SockIOBucket.STATES[curStateCode]);
						
						// 1. 回收资源
						bucket.close();
						
						// 2. 设置新状态
						bucket.setStateCode(curStateCode);
						
						// 3. 按新状态申请资源
						bucket.init();
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}

		/**
		 * 重试心跳失败的桶，重试成功的桶放回原来的位置，并从待检查桶列表里挪走
		 */
		private void bucketReconnect() {
			
			Iterator<SockIOBucket> iter = deadBuckets.iterator();
			while (iter.hasNext()) {
				SockIOBucket bucket = iter.next();
				try {
					boolean success = bucket.init();
					if (!success) {
						continue;
					}
					
					iter.remove();
					buckets.add(bucket);
					
					Collections.sort(buckets);
					log.info("-------------------------");
					for (SockIOBucket bkt : buckets) {
						log.info("-- " + bkt.getAddress());
					}
					log.info("-------------------------");
					log.info("memcached桶复活！" + bucket.getAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
