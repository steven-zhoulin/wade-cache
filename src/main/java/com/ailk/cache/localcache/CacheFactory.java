package com.ailk.cache.localcache;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.ailk.cache.localcache.CacheXml.ReadOnlyCacheItem;
import com.ailk.cache.localcache.CacheXml.ReadWriteCacheItem;
import com.ailk.cache.localcache.interfaces.IReadOnlyCache;
import com.ailk.cache.localcache.interfaces.IReadWriteCache;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: CacheFactory
 * @description: 本次缓存工厂类
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public final class CacheFactory {

	private static final Logger log = Logger.getLogger(CacheFactory.class);

	/**
	 * 本地只读缓存
	 */
	private static Map<Class, IReadOnlyCache> ROCACHES = new HashMap<Class, IReadOnlyCache>();

	/**
	 * 本地只读缓存名
	 */
	private static Set<String> ROCACHE_CLAZZNAME = new HashSet<String>();

	/**
	 * 需要被立即初始化的本地缓存名
	 */
	private static Set<String> ROCACHE_NEEDINIT = new HashSet<String>();

	/**
	 * 本地读写缓存
	 */
	private static Map<String, IReadWriteCache> RWCACHES = new HashMap<String, IReadWriteCache>();

	private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	private static Scheduler scheduler;

	private static List<ReadOnlyCacheItem> readonlyCacheItems;
	private static List<ReadWriteCacheItem> readwriteCacheItems;

	/**
	 * 判断本地缓存是否还未使用过，如果未使用过，就不会被记录在ROCACHES里。
	 * 
	 * @param clazz
	 * @return
	 */
	static boolean isNotUsed(Class clazz) {
		if (ROCACHES.containsKey(clazz)) {
			return false;
		}
		return true;
	}

	/**
	 * 获取本地只读缓存
	 * 
	 * @param clazz:
	 *            本地只读缓存类名
	 * @return
	 * @throws Exception
	 */
	public static IReadOnlyCache getReadOnlyCache(Class clazz) throws Exception {

		if (!ROCACHE_CLAZZNAME.contains(clazz.getName())) {
			log.error("缓存类在配置文件中未定义!" + clazz.getName());
			return null;
		}

		IReadOnlyCache cache = ROCACHES.get(clazz);

		if (null == cache) {

			/** 加锁初始化本地缓存 */
			synchronized (clazz) {
				/** 获得锁后，再次判断是否为空，防止重复初始化 */
				if ((cache = ROCACHES.get(clazz)) != null) {
					return cache;
				}

				long start = System.currentTimeMillis();
				cache = (IReadOnlyCache) clazz.newInstance();
				cache.setClassName(clazz.getName());
				cache.refresh();
				ROCACHES.put(clazz, cache);

				log.info("ReadOnlyCache:" + clazz.getName() + "刷新成功，加载数据量:" + cache.size() + "条，耗时:"
						+ (System.currentTimeMillis() - start) + "毫秒");
			}
		}

		return cache;
	}

	/**
	 * 获取本地读写缓存
	 * 
	 * @param cacheName
	 *            读写缓存名
	 * @return
	 */
	public static final IReadWriteCache getReadWriteCache(final String cacheName) {
		IReadWriteCache cache = RWCACHES.get(cacheName);
		return cache;
	}

	/**
	 * 本地只读缓存配置初始化
	 * 
	 * @param items
	 */
	private static final void initReadOnlyCaches(List<ReadOnlyCacheItem> items) {

		for (ReadOnlyCacheItem item : items) {
			ROCACHE_CLAZZNAME.add(item.className);
			if (item.isInitial) {
				ROCACHE_NEEDINIT.add(item.className);
			}

			try {

				Class clazz = Class.forName(item.className);

				if (null != item.cronExpr) {

					startSchedulerIfNotStarted();

					// 创建缓存自动刷新任务
					JobDetail jobDetail = new JobDetail("refresh_" + item.className + "_job", "CacheRefreshGroup",
							CacheAutoRefreshJob.class);
					jobDetail.getJobDataMap().put(CacheAutoRefreshJob.CACHE_TYPE, CacheAutoRefreshJob.READONLY_CACHE);
					jobDetail.getJobDataMap().put(CacheAutoRefreshJob.CACHE_NAME, clazz);
					CronTrigger trigger = new CronTrigger("refresh_" + item.className + "_trigger", "d");
					try {
						trigger.setCronExpression(item.cronExpr);
						scheduler.scheduleJob(jobDetail, trigger);
					} catch (ParseException e) {
						log.error(e);
					} catch (SchedulerException e) {
						log.error(e);
					}

				}
			} catch (Exception e) {
				log.error("ReadOnlyCache配置加载出错! " + item.className, e);
			}
		}
	}

	/**
	 * 本地读写缓存初始化
	 * 
	 * @param items
	 */
	private static final void initReadWriteCaches(List<ReadWriteCacheItem> items) {

		for (ReadWriteCacheItem item : items) {

			IReadWriteCache cache = new ReadWriteCache(item.maxSize);
			cache.setName(item.name);
			String name = item.name;
			RWCACHES.put(name, cache);

			if (null != item.cronExpr) {

				startSchedulerIfNotStarted();

				// 创建缓存自动刷新任务
				JobDetail jobDetail = new JobDetail("refresh_" + name + "_job", "CacheRefreshGroup",
						CacheAutoRefreshJob.class);
				jobDetail.getJobDataMap().put(CacheAutoRefreshJob.CACHE_TYPE, CacheAutoRefreshJob.READWRITE_CACHE);
				jobDetail.getJobDataMap().put(CacheAutoRefreshJob.CACHE_NAME, name);

				CronTrigger trigger = new CronTrigger("refresh_" + name + "_trigger", "d");
				try {
					trigger.setCronExpression(item.cronExpr);
					scheduler.scheduleJob(jobDetail, trigger);
				} catch (ParseException e) {
					log.error(e);
				} catch (SchedulerException e) {
					log.error(e);
				}
			}
		}
	}

	/**
	 * 获取只读缓存列表
	 * 
	 * @return
	 */
	public static final List<Map<String, String>> listReadOnlyCache() {
		List<Map<String, String>> rtn = new ArrayList<Map<String, String>>();
		for (ReadOnlyCacheItem item : readonlyCacheItems) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("className", item.className);
			map.put("init", String.valueOf(item.isInitial));
			map.put("cronExpr", item.cronExpr);
			rtn.add(map);
		}
		return rtn;
	}

	/**
	 * 获取读写缓存列表
	 * 
	 * @return
	 */
	public static final List<Map<String, String>> listReadWriteCache() {
		List<Map<String, String>> rtn = new ArrayList<Map<String, String>>();
		for (ReadWriteCacheItem item : readwriteCacheItems) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", item.name);
			map.put("maxSize", String.valueOf(item.maxSize));
			map.put("cronExpr", item.cronExpr);
			rtn.add(map);
		}
		return rtn;
	}

	/**
	 * 初始化本地缓存
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static final void init() {

		/** 加锁初始化本地缓存 */
		synchronized (CacheFactory.class) {
			// 初始化本地只读缓存，进行预加载工作
			for (String clazzName : ROCACHE_NEEDINIT) {
				try {
					long start = System.currentTimeMillis();

					Class clazz = Class.forName(clazzName);
					IReadOnlyCache cache = (IReadOnlyCache) clazz.newInstance();
					cache.setClassName(clazzName);
					cache.refresh();
					ROCACHES.put(clazz, cache);

					log.info("ReadOnlyCache:" + clazz.getName() + "刷新成功，加载数据量:" + cache.size() + "条，耗时:" + (System.currentTimeMillis() - start) + "毫秒");
				} catch (Exception e) {
					log.error("本地只读缓存初始化时发生错误!", e);
				}
			}
		}
	}

	/**
	 * 销毁quartz调度线程池
	 */
	public static final void destroy() {
		if (null != scheduler) {
			try {
				scheduler.shutdown();
			} catch (SchedulerException e) {
				log.error("销毁quartz调度线程池失败!", e);
			}
		}
	}

	/**
	 * 启动 Quartz Scheduler，如果 Scheduler 没启动的话
	 */
	private static final void startSchedulerIfNotStarted() {

		if (null != scheduler) {
			return;
		}

		try {
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			log.error("缓存定时刷新调度器初始化失败! " + e);
		}

	}

	static {

		try {
			CacheXml cacheXml = CacheXml.getInstance();
			readonlyCacheItems = cacheXml.getReadOnlyCacheItems();
			readwriteCacheItems = cacheXml.getReadWriteCacheItems();
			initReadOnlyCaches(readonlyCacheItems); // 初始化本地只读缓存
			initReadWriteCaches(readwriteCacheItems); // 初始化本地读写缓存
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
