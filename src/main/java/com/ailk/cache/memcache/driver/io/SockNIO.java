package com.ailk.cache.memcache.driver.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ailk.cache.memcache.driver.io.ISockIO;
import com.ailk.cache.memcache.driver.io.SockIOBucket;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: SockNIO
 * @description: 基于NIO实现
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-24
 */
public class SockNIO implements ISockIO {

	private static final Logger log = Logger.getLogger(SockNIO.class);
	
	private SockIOBucket bucket;
	private String host;
	private int port;
	private SocketChannel channel;
	
	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;
	private ByteArrayOutputStream bos;
	
	private boolean isMaster;
	private int version = 0;
	
	/**
	 * SockNIO构造函数
	 * 
	 * @param bucket
	 * @param host
	 * @param port
	 * @param version
	 * @param isMaster
	 */
	public SockNIO(SockIOBucket bucket, String host, int port, int version, boolean isMaster) {
		this.bucket = bucket;
		this.host = host;
		this.port = port;
		this.isMaster = isMaster;
		this.version = version;
		this.readBuffer = ByteBuffer.allocateDirect(1024 * 8);
		this.writeBuffer = ByteBuffer.allocateDirect(1024 * 8);
		this.bos = new ByteArrayOutputStream();
		readBuffer.flip(); // 为读取做准备
	}
	
	@Override
	public boolean init() {
		try {			
			InetAddress addr = InetAddress.getByName(host);
			channel = SocketChannel.open(new InetSocketAddress(addr, this.port));
			channel.configureBlocking(true); // 阻塞模式
			return true;
		} catch (IOException e) {
			log.error("memcached socket连接建立失败" + host + ":" + port + " " + e);
		}
		return false;
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		write(bytes, 0, bytes.length);
	}

	/**
	 * 将byte[]写入到channel，先填充满buffer，再一次性写出
	 * 
	 * @param b
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	private final void write(byte[] b, int off, int len) throws IOException {
		
		if (len == 0) {
			return;
		}
		
		if (writeBuffer.remaining() >= len) {
			writeBuffer.put(b, off, len);
		} else {
			int written = 0;
			int size = 0;
			int remain = 0;
			while ((remain = len - written) > 0) {
				size = writeBuffer.remaining(); // 缓冲区还能容纳的字节数
				size = size < remain ? size : remain; // Math.min(缓冲区还能容纳字节数, 还剩多少字节要write);
				writeBuffer.put(b, off + written, size);
				flush();
				written += size;
			}
		}   
		
	}
	
	public void flush() throws IOException {
		writeBuffer.flip();
		this.channel.write(writeBuffer);
		writeBuffer.clear();
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		
		int remain = readBuffer.limit() - readBuffer.position();
		if (len < remain) { // 缓存区未读完的数据能够满足要求
			readBuffer.get(b, off, len);
			return len;
		} else {
			readBuffer.get(b, off, remain); // 缓存区未读完的数据能满足一部分要求
			readChannel();
			return remain;
		}
		
	}

	private final byte read() throws IOException {
		try {
			byte b = readBuffer.get();
			return b;
		} catch (BufferUnderflowException e) {
			readChannel();
			return readBuffer.get();
		}
	}

	private final void readChannel() throws IOException {
		readBuffer.clear();
		this.channel.read(readBuffer);
		readBuffer.flip();
	}
	
	@Override
	public boolean isConnected() {
		return (this.channel != null && this.channel.isConnected());
	}

	@Override
	public boolean isAlive() {
		
		if (!isConnected())
			return false;

		try {
			this.write("version\r\n".getBytes());
			this.flush();
			byte[] rtn = readLineBytes();
			if (null == rtn) {
				return false;
			}
			
			return new String(rtn).startsWith("VERSION");
			
		} catch (Exception e) {
			log.error("心跳检测异常！", e);
			return false;
		}
	}

	@Override
	public void close() throws IOException {
		if (null != this.channel) {
			this.channel.close();
		}
		bucket.delSock(this);
	}

	@Override
	public byte[] readLineBytes() throws IOException {

		byte[] rtn = null;
		
		boolean eol = false;
		int one;
		while ((one = read()) != -1) {
			if (13 == one) {
				eol = true;
				continue;
			} else {
				if ((eol) && (10 == one)) {
					break;
				}
				eol = false;
			}
			
			bos.write((byte)one);
		}
		
		if (null == bos || bos.size() <= 0) {
			return null;
		}
		
		rtn = bos.toByteArray();
		bos.reset(); // 重置
		return rtn;
	}

	@Override
	public void release() {
		this.bucket.returnSockIO(this);
	}

	@Override
	public SockIOBucket getBucket() {
		return this.bucket;
	}

	@Override
	public String getHost() {
		return this.host;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Override
	public boolean isMaster() {
		return isMaster;
	}
}
