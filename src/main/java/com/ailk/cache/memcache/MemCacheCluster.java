package com.ailk.cache.memcache;

import java.util.TreeSet;

import com.ailk.cache.memcache.MemCacheAddress;

class MemCacheCluster {
	
	private String name = null;

	private int heartbeatSecond = 5;
	private int poolSize = 5;
	private boolean useNIO = false;
	private TreeSet<MemCacheAddress> address = null;

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

	public boolean isUseNIO() {
		return useNIO;
	}

	public void setUseNIO(boolean useNIO) {
		this.useNIO = useNIO;
	}
	
	public TreeSet<MemCacheAddress> getAddress() {
		return address;
	}
	
	public void setAddress(TreeSet<MemCacheAddress> address) {
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
