package com.ailk.cache.memcache.driver.io;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ailk.cache.memcache.driver.io.SockIOBucket;

/**
 * Copyright: Copyright (c) 2013 Asiainfo
 * 
 * @className: SimpleSockIOBucket
 * @description: 无备地址的SockIO桶
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-24
 */
public class SimpleSockIOBucket extends SockIOBucket {

	private static final Logger log = Logger.getLogger(SimpleSockIOBucket.class);
	
	private boolean useNIO = true;
	
	private String masterHost;
	private int masterPort;
	private int poolSize;
	
	/**
	 * 桶状态
	 */
	private int stateCode = STATE_RAW;
	
	/**
	 * 桶的版本号
	 */
	private int version = 0;
	
	private LinkedBlockingQueue<ISockIO> masterSocks = new LinkedBlockingQueue<ISockIO>();
	
	public SimpleSockIOBucket(String masterHost, int masterPort, int poolSize, boolean useNIO) {
		this.masterHost = masterHost;
		this.masterPort = masterPort;
		this.poolSize = poolSize;
		this.useNIO = useNIO;
	}
	
	/**
	 * 桶的初始化
	 * 
	 * @throws IOException
	 */
	public boolean init() throws IOException {
		
		// 每次初始化桶递增版本号
		this.version++;
		
		for (int i = 0; i < this.poolSize; i++) {
			
			ISockIO sock = null;
			if (this.useNIO) {
				sock = new SockNIO(this, masterHost, masterPort, version, true);
			} else {
				sock = new SockBIO(this, masterHost, masterPort, version, true);
			}
			
			if (sock.init()) {
				masterSocks.add(sock);
			} else { // 如果连不上, 就没必要初始化第二次，不卡
				break;
			}
		}
		
		if (masterSocks.size() == this.poolSize) {
			this.stateCode = STATE_OK;
			return true;
		} else {
			this.stateCode = STATE_ER;
			this.close();
		}
		
		return false;
	}
	
	/**
	 * 桶的销毁，关闭桶中所有的连接
	 */
	public void close() {
		
		for (int i = 0; i < poolSize; i++) {
			try {
				ISockIO sock = masterSocks.poll();
				if (null != sock) {
					sock.close();
				}
			} catch (Exception e) {
				log.error("memcached心跳发生异常!", e);
			}
		}
		
		masterSocks.clear();
	}
	
	/**
	 * 非阻塞方式获取一个socket连接
	 * 
	 * @return
	 */
	public ISockIO borrowSockIO() {
		return borrowSockIO(5);
	}
	
	/**
	 * 非阻塞方式获取一个socket连接
	 * 
	 * @return
	 */
	public ISockIO borrowSockIO(long timeout) {
		
		ISockIO sock = null;
		
		try {
			// 当连接池为空时，会堵塞一段时间
			sock = masterSocks.poll(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		
		return sock;
		
	}
	
	/**
	 * 归还一个socket连接
	 * 
	 * @param sock
	 */
	public void returnSockIO(ISockIO sock) {
		if (this.version != sock.getVersion()) {
			// 连接的版本号和桶的版本号不一致，说明是前次桶资源释放不完全。
			try {
				sock.close();
			} catch (IOException e) {
				log.error("memcached释放过期连接发生异常!", e);
			}
			return;
		}
		masterSocks.offer(sock);
	}
	
	public boolean addSock(ISockIO sock) {
		return masterSocks.add(sock);
	}

	public boolean delSock(ISockIO sock) {
		return masterSocks.remove(sock);
	}
	
	@Override
	public int healthCheck() throws IOException, InterruptedException {
		
		int curStateCode = STATE_ER;
		
		ISockIO io = this.borrowSockIO(); // 直接从连接池取连接，来判断桶的状态
		if (null != io) {
			curStateCode = io.isAlive() ? STATE_OK : STATE_ER;
			io.release(); // 记得释放
			return curStateCode;
		} 
		
		// 连接池取不到链接，尝试创建一个链接，来判断桶的状态
		ISockIO sock = null;
		if (this.useNIO) {
			sock = new SockNIO(this, masterHost, masterPort, version, true);
		} else {
			sock = new SockBIO(this, masterHost, masterPort, version, true);
		}
		
		if (null != sock) {
			if (sock.init()) {
				curStateCode = sock.isAlive() ? STATE_OK : STATE_ER;
			}
			sock.close(); // 记得释放
		}
		
		return curStateCode;
	}
	
	@Override
	public int getStateCode() {
		return this.stateCode;
	}
	
	@Override
	public void setStateCode(int stateCode) {
		this.stateCode = stateCode;
	}
	
	public String getAddress() {
		return masterHost + ":" + masterPort;
	}

	@Override
	public int compareTo(SockIOBucket o) {
		return getAddress().compareTo(o.getAddress());
	}
	
}
