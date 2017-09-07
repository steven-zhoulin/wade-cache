package com.ailk.cache.localcache.interfaces;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: IReadWriteCache
 * @description: 读写缓存接口
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public interface IReadWriteCache {

	/**
	 * 缓存刷新
	 * 
	 * @throws Exception
	 */
	public void refresh();

	/**
	 * 获取指定key对应的缓存数据
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) throws Exception;

	/**
	 * 往缓存里放数据(Key-Value)
	 * 
	 * @param key
	 * @param value
	 * @return 返回key原先关联的value，如果key原先未关联value，则返回null。
	 * @throws Exception
	 */
	public Object put(String key, Object value) throws Exception;

	/**
	 * 判断缓存中是否包含该key
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key);
	
	/**
	 * 判断缓存是否为空
	 * 
	 * @return
	 */
	public boolean isEmpty();
	
	/**
	 * 返回key集合
	 * 
	 * @return
	 */
	public Set<String> keySet();
	
	/**
	 * 根据key，从缓存中删除对象
	 * 
	 * @param key
	 * @return
	 */
	public Object remove(String key);
	
	/**
	 * 返回缓存记录条数
	 * 
	 * @return
	 */
	public int size() throws Exception;

	/**
	 * 获取刷新历史信息，Key为刷新时间，Value为刷新后记录集数量
	 * 
	 * @return
	 */
	public LinkedHashMap<Long, Integer> getRefreshHistory();
	
	/**
	 * 获取只读缓存名
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * 设置只读缓存名
	 * 
	 * @param name
	 */
	public void setName(String name);
}