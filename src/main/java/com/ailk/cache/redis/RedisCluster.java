package com.ailk.cache.redis;

import java.util.TreeSet;

/**
 * Copyright: Copyright (c) 2014 Asiainfo
 * 
 * @className: RedisCluster
 * @description: Redis集群对象
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2014-7-14
 */
class RedisCluster {
	private String name = null;

	private int heartbeatSecond = 5;
	private int poolSize = 5;
	private boolean useNIO = false;
	
	public boolean isUseNIO() {
		return useNIO;
	}

	public void setUseNIO(boolean useNIO) {
		this.useNIO = useNIO;
	}

	private TreeSet<RedisAddress> address = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getHeartbeatSecond() {
		return heartbeatSecond;
	}
	
	public void setHeartbeatSecond(int heartbeatSecond) {
		this.heartbeatSecond = heartbeatSecond;
	}
	
	public int getPoolSize() {
		return poolSize;
	}
	
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
	
	public TreeSet<RedisAddress> getAddress() {
		return address;
	}
	
	public void setAddress(TreeSet<RedisAddress> address) {
		this.address = address;
	}
	
	@Override
	public String toString() {
		StringBuilder sbuff = new StringBuilder();
		sbuff.append("{ name:" + name);
		sbuff.append(", heartbeatSecond:" + heartbeatSecond);
		sbuff.append(", poolSize:" + poolSize);
		sbuff.append(", address:" + address.toString());
		sbuff.append("}");
		return sbuff.toString();
	}
}
