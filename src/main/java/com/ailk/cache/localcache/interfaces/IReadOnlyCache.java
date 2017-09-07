package com.ailk.cache.localcache.interfaces;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: IReadOnlyCache
 * @description: 只读缓存接口
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public interface IReadOnlyCache {

	/**
	 * 缓存刷新
	 * 
	 * @throws Exception
	 */
	public void refresh() throws Exception;

	/**
	 * 获取指定key所对应的缓存数据
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) throws Exception;

	/**
	 * 缓存条数
	 * 
	 * @return
	 */
	public int size();

	/**
	 * 返回key集合
	 * 
	 * @return
	 */
	public Set<String> keySet();
		
	/**
	 * 获取刷新历史信息，Key为刷新时间，Value为刷新后记录集数量
	 * 
	 * @return
	 */
	public LinkedHashMap<Long, Integer> getRefreshHistory();
	
	/**
	 * 提供给业务侧实现的数据加载接口
	 * 
	 * @return
	 */
	public Map<String, Object> loadData() throws Exception;
	
	/**
	 * 获取只读缓存类路径
	 * 
	 * @return
	 */
	public String getClassName();

	/**
	 * 设置只读缓存类路径
	 * @param className
	 */
	public void setClassName(String className);
}