package com.ailk.cache.redis;

/**
 * Copyright: Copyright (c) 2014 Asiainfo
 * 
 * @className: RedisAddress
 * @description: Redis地址对象
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2014-7-14
 */
public class RedisAddress implements Comparable<RedisAddress> {
	
	/**
	 * 主地址
	 */
	private String master;
	
	/**
	 * 备地址
	 */
	private String slave;
	
	public String getMaster() {
		return master;
	}
	
	public void setMaster(String master) {
		this.master = master;
	}
	
	public String getSlave() {
		return slave;
	}
	
	public void setSlave(String slave) {
		this.slave = slave;
	}

	/**
	 * 仅按master排序
	 * 
	 * @param anotherRedisAddress
	 * @return
	 */
	@Override
	public int compareTo(RedisAddress o) {
		String anotherMaster = o.getMaster();
		return this.master.compareTo(anotherMaster);
	}

	@Override
	public String toString() {
		return this.master + " -> " + this.slave;
	}
}
