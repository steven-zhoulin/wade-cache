package com.ailk.cache.localcache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ailk.cache.localcache.interfaces.IReadOnlyCache;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: AbstractReadOnlyCache
 * @description: 只读缓存抽象类，作为业务只读缓存类的基类。
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public abstract class AbstractReadOnlyCache implements IReadOnlyCache {

	private static final Logger LOG = Logger.getLogger(AbstractReadOnlyCache.class);
	
	private Map<String, Object> cache;
	
	/**
	 * 只读缓存类路径
	 */
	private String className;
	
	/**
	 * 刷新历史记录，Key为刷新时间，Value为刷新后记录集数量
	 */
	private LinkedHashMap<Long, Integer> refreshHistory = new LinkedHashMap<Long, Integer>();

	public AbstractReadOnlyCache() {

	}

	public AbstractReadOnlyCache(Map<String, Object> map) {
		this.cache = map;
		updateRefreshHistory();
	}

	/**
	 * 获取只读缓存类路径
	 * 
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * 设置只读缓存类路径
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * 缓存刷新接口
	 */
	@Override
	public synchronized void refresh() throws Exception {
		
		LOG.info("只读缓存刷新! className:" + getClassName());
		
		Map<String, Object> newCache = loadData();
		Map<String, Object> oldCache = this.cache;
		
		// 直接切换引用
		this.cache = newCache;
		updateRefreshHistory();
		
		if (null != oldCache) {
			oldCache.clear(); // 尽早释放老数据
			oldCache = null;
		}
	}

	/**
	 * 获取缓存对象
	 */
	@Override
	public Object get(String key) {
		return cache.get(key);
	}

	/**
	 * 获取缓存记录条数
	 */
	@Override
	public int size() {
		return cache.size();
	}

	/**
	 * 返回key集合
	 * 
	 * @return
	 */
	@Override
	public Set<String> keySet() {
		return cache.keySet();
	}

	/**
	 * 更新刷新记录
	 */
	private final void updateRefreshHistory() {
		this.refreshHistory.put(System.currentTimeMillis(), this.cache.size());
		
		/**
		 * 只保留最近10条统计数据，淘汰老数据
		 */
		if (this.refreshHistory.size() > 10) {
			Iterator<Long> i = this.refreshHistory.keySet().iterator();
			i.next();
			i.remove();
		}
	}

	/**
	 * 获取刷新历史信息，Key为刷新时间，Value为刷新后记录集数量
	 * 
	 * @return
	 */
	@Override
	public LinkedHashMap<Long, Integer> getRefreshHistory() {
		return refreshHistory;
	}	
	
    public abstract Map<String, Object> loadData() throws Exception;
}