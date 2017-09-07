package com.ailk.cache.localcache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ailk.cache.localcache.interfaces.IReadWriteCache;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: ReadWriteCache
 * @description: 读写缓存实现类。
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public class ReadWriteCache implements IReadWriteCache {

	private static final Logger LOG = Logger.getLogger(ReadWriteCache.class);
	
	/**
	 * 刷新历史记录，Key为刷新时间，Value为刷新后记录集数量
	 */
	private LinkedHashMap<Long, Integer> refreshHistory = new LinkedHashMap<Long, Integer>();
	
	private ConcurrentLRUMap<String, Object> cache;
	
	/**
	 * 读写缓存名
	 */
	private String name;
	
	/**
	 * 获取只读缓存名
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置只读缓存名
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public ReadWriteCache(int maxSize) {
		this.cache = new ConcurrentLRUMap<String, Object>(maxSize);
	}

	/**
	 * 缓存刷新
	 */
	@Override
	public void refresh() {
		LOG.info("读写缓存刷新! name: " + getName());
		updateRefreshHistory();
		this.cache.clear();
	}

	/**
	 * 从缓存中获取key所对应的对象
	 */
	@Override
	public Object get(String key) throws Exception {
		return cache.get(key);
	}

	/**
	 * 往缓存里放数据(Key-Value)
	 */
	@Override
	public Object put(String key, Object value) throws Exception {
		return cache.put(key, value);
	}
	
	/**
	 * 获取缓存记录条数
	 */
	@Override
	public int size() throws Exception {
		return cache.size();
	}

	/**
	 * 判断缓存中是否包含该key
	 */
	@Override
	public boolean containsKey(String key) {
		return cache.containsKey(key);
	}

	/**
	 * 判断缓存是否为空
	 */
	@Override
	public boolean isEmpty() {
		return cache.isEmpty();
	}

	/**
	 * 返回key的集合
	 */
	@Override
	public Set<String> keySet() {
		return cache.keySet();
	}

	/**
	 * 从缓存中删除key所对应的对象
	 */
	@Override
	public Object remove(String key) {
		return cache.remove(key);
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
}