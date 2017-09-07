package com.ailk.cache.memcache.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;

import com.ailk.org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ailk.cache.memcache.MemCacheAddress;
import com.ailk.cache.memcache.MemCacheFactory;
import com.ailk.cache.memcache.driver.io.ISockIO;
import com.ailk.cache.memcache.driver.io.SockIOPool;
import com.ailk.cache.memcache.driver.util.FastConvertor;
import com.ailk.cache.memcache.interfaces.IMemCache;
import com.ailk.cache.util.serial.DefaultSerializable;
import com.ailk.cache.util.serial.ISerializable;
import com.ailk.cache.util.IOUtil;
import com.ailk.cache.util.MD5Util;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: TextClient
 * @description: 基于文本协议的客户端
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-3-19
 */
public final class TextClient implements IMemCache {

	private static final Logger log = Logger.getLogger(TextClient.class);

	private static final byte[] CMD_ADD = "add".getBytes();
	private static final byte[] CMD_APPEND = "append".getBytes();
	private static final byte[] CMD_SET = "set".getBytes();
	private static final byte[] CMD_GET = "get".getBytes();
	private static final byte[] CMD_DELETE = "delete".getBytes();
	private static final byte[] CMD_INCR = "incr".getBytes();
	private static final byte[] CMD_DECR = "decr".getBytes();
	private static final byte[] CMD_TOUCH = "touch".getBytes();

	private static final byte[] SPACE = " ".getBytes();
	private static final byte[] CRLF = "\r\n".getBytes();
	private static final byte[] ZERO = "0".getBytes();

	private static final byte[] SERVER_STATUS_STORED = "STORED".getBytes();
	private static final byte[] SERVER_STATUS_DELETED = "DELETED".getBytes();
	private static final byte[] SERVER_STATUS_BYTES_NOT_FOUND = "NOT_FOUND".getBytes();
	private static final byte[] SERVER_STATUS_BYTES_TOUCHED = "TOUCHED".getBytes();
	private static final byte[] SERVER_STATUS_BYTES_ERROR = "ERROR".getBytes();
	private static final byte[] SERVER_STATUS_BYTES_CLIENT_ERROR = "CLIENT_ERROR".getBytes();
	private static final byte[] SERVER_STATUS_BYTES_SERVER_ERROR = "SERVER_ERROR".getBytes();
	private static final byte[] SERVER_STATUS_BYTES_END = "END".getBytes();
	private static final byte[] SERVER_STATUS_BYTES_VALUE = "VALUE".getBytes();
	
	private SockIOPool pool;

	/**
	 * memcached限制key的最大长度为250byte。
	 */
	private static final int MAX_KEYSIZE = 250;
	
	/**
	 * memcached限制单个value最大体积为1MB。
	 */
	private static final int MAX_VALUESIZE = 1024 * 1024; // VALUE最大容量1M
	
	/**
	 * 对象序列化与反序列化接口。
	 */
	private static final ISerializable SERIALIZER = new DefaultSerializable();

	/**
	 * 构造函数
	 * 
	 * @param address 缓存服务器地址集
	 * @param poolSize 连接池大小
	 */
	public TextClient(MemCacheAddress[] address, int poolSize, int heartbeatSecond, boolean useNIO) {
		try {
			this.pool = new SockIOPool(address, poolSize, heartbeatSecond, useNIO);
		} catch (Exception e) {
			log.error("初始化memcached连接池出错！" + StringUtils.join(address, ','), e);
		}
	}
		
	@Override
	public boolean set(String cacheKey, Object value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_OTHERS);
	}

	@Override
	public boolean set(String cacheKey, Byte value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_BYTE);
	}

	@Override
	public boolean set(String cacheKey, Integer value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_INTEGER);
	}

	@Override
	public boolean set(String cacheKey, Character value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_CHARACTER);
	}

	@Override
	public boolean set(String cacheKey, String value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_STRING);
	}

	@Override
	public boolean set(String cacheKey, StringBuffer value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_STRINGBUFFER);
	}

	@Override
	public boolean set(String cacheKey, StringBuilder value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_STRINGBUILDER);
	}

	@Override
	public boolean set(String cacheKey, Float value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_FLOAT);
	}

	@Override
	public boolean set(String cacheKey, Short value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_SHORT);
	}

	@Override
	public boolean set(String cacheKey, Double value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_DOUBLE);
	}

	@Override
	public boolean set(String cacheKey, Date value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_DATE);
	}

	@Override
	public boolean set(String cacheKey, byte[] value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_BYTEARR);
	}

	@Override
	public boolean set(String cacheKey, Boolean value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_BOOLEAN);
	}

	@Override
	public boolean set(String cacheKey, Long value) {
		return set(cacheKey, value, 0, FastConvertor.MARKER_LONG);
	}

	@Override
	public boolean set(String cacheKey, Object value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_OTHERS);
	}

	@Override
	public boolean set(String cacheKey, Byte value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_BYTE);
	}

	@Override
	public boolean set(String cacheKey, Integer value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_INTEGER);
	}

	@Override
	public boolean set(String cacheKey, Character value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_CHARACTER);
	}

	@Override
	public boolean set(String cacheKey, String value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_STRING);
	}

	@Override
	public boolean set(String cacheKey, StringBuffer value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_STRINGBUFFER);
	}

	@Override
	public boolean set(String cacheKey, StringBuilder value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_STRINGBUILDER);
	}

	@Override
	public boolean set(String cacheKey, Float value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_FLOAT);
	}

	@Override
	public boolean set(String cacheKey, Short value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_SHORT);
	}

	@Override
	public boolean set(String cacheKey, Double value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_DOUBLE);
	}

	@Override
	public boolean set(String cacheKey, Date value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_DATE);
	}

	@Override
	public boolean set(String cacheKey, byte[] value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_BYTEARR);
	}

	@Override
	public boolean set(String cacheKey, Boolean value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_BOOLEAN);
	}

	@Override
	public boolean set(String cacheKey, Long value, int secTTL) {
		return set(cacheKey, value, secTTL, FastConvertor.MARKER_LONG);
	}

	private boolean set(String bizCacheKey, Object value, int secTTL, int flag) {
		String cacheKey = bizCacheKey;
		
		if (null == cacheKey || null == value) return false;
		cacheKey = sanitizeKey(cacheKey);
		if (null == cacheKey) return false;

		byte[] datas = null;

		if (flag > 1) { // 原生类型，不走默认的序列化，提升性能
			datas = FastConvertor.encode(value, flag);
		} else {
			datas = SERIALIZER.encode(value);
		}
		
		// 如果对象体积过大，压缩处理
		if (datas.length > MAX_VALUESIZE) {
			int gzipBefore = datas.length;
			
			if (flag > 1) { // 压缩时，统一按普通对象处理
				datas = SERIALIZER.encode(value);
			}
			
			datas = SERIALIZER.encodeGzip(datas);
			flag = 1; // 置成压缩标识
			
			log.warn("cacheKey=" + bizCacheKey);
			log.warn("对象过大，开启压缩，压缩前" + gzipBefore + "byte，压缩后:" + datas.length + "byte，" + (datas.length > MAX_VALUESIZE ? ("仍不满足缓存条件！") : ("满足缓存条件。")));
		}
		
		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(cacheKey);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) {
			log.error("从MemCache连接池获取SockIO对象为空!");
			return false;
		}
		
		try {
			
			io.write(CMD_SET);
			io.write(SPACE);
			io.write(cacheKey.getBytes());
			io.write(SPACE);
			io.write(IOUtil.encode(flag));
			io.write(SPACE);
			io.write(IOUtil.encode(secTTL));
			io.write(SPACE);
			io.write(IOUtil.encode(datas.length));
			io.write(CRLF);
			io.write(datas);
			io.write(CRLF);
			io.flush();

			byte[] bytes = io.readLineBytes();

			if (null == bytes) {
				log.error("服务器返回空！");
				return false;
			}
			
			if (Arrays.equals(bytes, SERVER_STATUS_STORED)) {
				return true;
			} else if (equals(bytes, SERVER_STATUS_BYTES_CLIENT_ERROR, SERVER_STATUS_BYTES_CLIENT_ERROR.length)) {
				log.error("指令格式错误!" + new String(bytes));
				return false;
			} else if (equals(bytes, SERVER_STATUS_BYTES_SERVER_ERROR, SERVER_STATUS_BYTES_SERVER_ERROR.length)) {
				log.error("缓存服务器内部错误！" + new String(bytes));
				return false;
			}
		} catch (Exception e) {
			log.error("set时发生错误！", e);
		} finally {
			if (null != io) io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			MemCacheFactory.performance.report("set", cacheKey, cCost, eCost);
		}

		return false;
	}

	@Override
	public boolean keyExists(String cacheKey) {
		Object value = get(cacheKey);
		return null == value ? false : true;
	}

	@Override
	public Object get(String cacheKey) {

		if (null == cacheKey)  return null;
		cacheKey = sanitizeKey(cacheKey);
		if (null == cacheKey) return null;

		Object rtn = null;
		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(cacheKey);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) {
			log.error("从MemCache连接池获取SockIO对象为空!");
			return null;
		}
		
		try {

			io.write(CMD_GET);
			io.write(SPACE);
			io.write(cacheKey.getBytes());
			io.write(CRLF);
			io.flush();

			byte[] bytes = io.readLineBytes();
			if (null == bytes) {
				log.error("服务端返回空!");
				return null;
			}
			if (Arrays.equals(bytes, SERVER_STATUS_BYTES_ERROR)) {
				log.error("未知的指令！服务器返回信息:" + new String(bytes) + " KEY:" + cacheKey);
				return null;
			} else if (equals(bytes, SERVER_STATUS_BYTES_CLIENT_ERROR, SERVER_STATUS_BYTES_CLIENT_ERROR.length)) {
				log.error("指令格式错误!" + new String(bytes));
				return null;
			} else if (equals(bytes, SERVER_STATUS_BYTES_SERVER_ERROR, SERVER_STATUS_BYTES_SERVER_ERROR.length)) {
				log.error("缓存服务器内部错误！" + new String(bytes));
				return null;
			} else if (Arrays.equals(bytes, SERVER_STATUS_BYTES_END)) {
				return null; // 未命中缓存
			}
			
			if (equals(bytes, SERVER_STATUS_BYTES_VALUE, SERVER_STATUS_BYTES_VALUE.length)) {
				
				int[] part = cmdParse2(bytes);
				int flag = part[0];
				int size = part[1];
				
				byte[] datas = new byte[size];
				int cnt = 0;
				while (cnt < size) {
					cnt += io.read(datas, cnt, (size - cnt));
				}

				io.readLineBytes();
				bytes = io.readLineBytes();
				if (null == bytes) return rtn;
				if (Arrays.equals(bytes, SERVER_STATUS_BYTES_END)) {
					if (flag > 1) { // 原生类型，不走默认的序列化，提升性能。
						rtn = FastConvertor.decode(datas, flag);
					} else if (flag == 1) {
						// 压缩标识
						datas = SERIALIZER.decodeGzip(datas);
						rtn = SERIALIZER.decode(datas);
					} else {
						rtn = SERIALIZER.decode(datas);
					}
				}
			}
		} catch (Exception e) {
			log.error("get指令发生错误!", e);
		} finally {
			if (null != io) io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			MemCacheFactory.performance.report("get", cacheKey, cCost, eCost);
		}

		return rtn;
	}
	
	@Override
	public boolean add(String cacheKey, long value) {
		return add(cacheKey, value, 0);
	}
	
	@Override
	public boolean add(String cacheKey, long value, int secTTL) {

		if (null == cacheKey)  return false;
		cacheKey = sanitizeKey(cacheKey);
		if (null == cacheKey)  return false;
		
		int flag = FastConvertor.MARKER_LONG;
		
		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(cacheKey);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) {
			log.error("从MemCache连接池获取SockIO对象为空!");
			return false;
		}
		
		try {

			byte[] datas = FastConvertor.encode(value, flag);
			
			io.write(CMD_ADD);
			io.write(SPACE);
			io.write(cacheKey.getBytes());
			io.write(SPACE);
			io.write(IOUtil.encode(flag));
			io.write(SPACE);
			io.write(IOUtil.encode(secTTL)); // 超时标志
			io.write(SPACE);
			io.write(IOUtil.encode(datas.length)); // 对象大小
			io.write(CRLF);
			io.write(datas);
			io.write(CRLF);
			io.flush();

			byte[] bytes = io.readLineBytes();
			if (null == bytes) {
				log.error("服务端返回空!");
				return false;
			}
			
			if (Arrays.equals(bytes, SERVER_STATUS_STORED)) {
				return true;
			} else if (equals(bytes, SERVER_STATUS_BYTES_CLIENT_ERROR, SERVER_STATUS_BYTES_CLIENT_ERROR.length)) {
				log.error("指令格式错误!" + new String(bytes));
				return false;
			} else if (equals(bytes, SERVER_STATUS_BYTES_SERVER_ERROR, SERVER_STATUS_BYTES_SERVER_ERROR.length)) {
				log.error("缓存服务器内部错误！" + new String(bytes));
				return false;
			}

		} catch (Exception e) {
			log.error("add发生错误！", e);
		} finally {
			if (null != io) io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			MemCacheFactory.performance.report("add", cacheKey, cCost, eCost);
		}

		return false;
	}

	@Override
	public boolean append(String cacheKey, byte[] bytes) {
		
		if (null == cacheKey)  return false;
		cacheKey = sanitizeKey(cacheKey);
		if (null == cacheKey)  return false;
		
		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(cacheKey);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) {
			log.error("从MemCache连接池获取SockIO对象为空!");
			return false;
		}
		
		try {

			io.write(CMD_APPEND);
			io.write(SPACE);
			io.write(cacheKey.getBytes());
			io.write(SPACE);
			io.write(IOUtil.encode(FastConvertor.MARKER_BYTEARR));
			io.write(SPACE);
			io.write(ZERO); // 超时标志
			io.write(SPACE);
			io.write(IOUtil.encode(bytes.length)); // 对象大小
			io.write(CRLF);
			io.write(bytes);
			io.write(CRLF);
			io.flush();

			byte[] cmd = io.readLineBytes();
			if (Arrays.equals(cmd, SERVER_STATUS_STORED)) {
				return true;
			} else if (equals(bytes, SERVER_STATUS_BYTES_CLIENT_ERROR, SERVER_STATUS_BYTES_CLIENT_ERROR.length)) {
				log.error("指令格式错误!" + new String(bytes));
				return false;
			} else if (equals(bytes, SERVER_STATUS_BYTES_SERVER_ERROR, SERVER_STATUS_BYTES_SERVER_ERROR.length)) {
				log.error("缓存服务器内部错误！" + new String(bytes));
				return false;
			}

		} catch (Exception e) {
			log.error("append发生错误！", e);
		} finally {
			if (null != io) io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			MemCacheFactory.performance.report("append", cacheKey, cCost, eCost);
		}
		
		return false;
	}
	
	@Override
	public boolean delete(String cacheKey) {

		if (null == cacheKey)  return false;
		cacheKey = sanitizeKey(cacheKey);
		if (null == cacheKey)  return false;

		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(cacheKey);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) {
			log.error("从MemCache连接池获取SockIO对象为空!");
			return false;
		}
		
		try {

			io.write(CMD_DELETE);
			io.write(SPACE);
			io.write(cacheKey.getBytes());
			io.write(CRLF);
			io.flush();

			byte[] bytes = io.readLineBytes();
			if (Arrays.equals(bytes, SERVER_STATUS_DELETED)) {
				return true;
			} else if (equals(bytes, SERVER_STATUS_BYTES_CLIENT_ERROR, SERVER_STATUS_BYTES_CLIENT_ERROR.length)) {
				log.error("指令格式错误!" + new String(bytes));
				return false;
			} else if (equals(bytes, SERVER_STATUS_BYTES_SERVER_ERROR, SERVER_STATUS_BYTES_SERVER_ERROR.length)) {
				log.error("缓存服务器内部错误！" + new String(bytes));
				return false;
			}

		} catch (Exception e) {
			log.error("delete发生错误！", e);
		} finally {
			if (null != io) io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			MemCacheFactory.performance.report("delete", cacheKey, cCost, eCost);
		}

		return false;

	}

	@Override
	public long incr(String cacheKey) {
		return incrWithTTL(cacheKey, 1, 0);
	}

	@Override
	public long incr(String cacheKey, int inc) {
		return incrWithTTL(cacheKey, inc, 0); // 默认不超时
	}
	
	@Override
	public long incrWithTTL(String cacheKey, int secTTL) {
		return incrWithTTL(cacheKey, 1, secTTL);
	}
	
	@Override
	public long incrWithTTL(String cacheKey, int inc, int secTTL) {
		
		if (null == cacheKey)  return -1;
		cacheKey = sanitizeKey(cacheKey);
		if (null == cacheKey)  return -1;

		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(cacheKey);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) {
			log.error("从MemCache连接池获取SockIO对象为空!");
			return 0;
		}
		
		try {
			
			byte[] data = String.valueOf(inc).getBytes();
			
			io.write(CMD_INCR);
			io.write(SPACE);
			io.write(cacheKey.getBytes());
			io.write(SPACE);
			io.write(data);
			io.write(CRLF);
			io.flush();

			byte[] bytes = io.readLineBytes();
			if (Arrays.equals(bytes, SERVER_STATUS_BYTES_NOT_FOUND)) {

				io.write(CMD_ADD);
				io.write(SPACE);
				io.write(cacheKey.getBytes());
				io.write(SPACE);
				io.write(IOUtil.encode(FastConvertor.MARKER_STRING));
				io.write(SPACE);
				io.write(IOUtil.encode(secTTL)); // 超时标志
				io.write(SPACE);
				io.write(IOUtil.encode(data.length)); // 对象大小
				io.write(CRLF);
				io.write(data);
				io.write(CRLF);
				io.flush();

				byte[] cmd = io.readLineBytes();
				if (null == cmd) {
					log.error("服务端返回空!");
					return -1;
				} else if (Arrays.equals(cmd, SERVER_STATUS_STORED)) {
					return inc;
				} else if (equals(cmd, SERVER_STATUS_BYTES_CLIENT_ERROR, SERVER_STATUS_BYTES_CLIENT_ERROR.length)) {
					log.error("指令格式错误!" + new String(cmd));
					return -1;
				} else if (equals(cmd, SERVER_STATUS_BYTES_SERVER_ERROR, SERVER_STATUS_BYTES_SERVER_ERROR.length)) {
					log.error("缓存服务器内部错误！" + new String(cmd));
					return -1;
				}
				
			} else {
				return FastConvertor.strBytes2Long(bytes);
			}

		} catch (Exception e) {
			log.error("incrWithTTL发生错误！", e);
		} finally {
			if (null != io) io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			MemCacheFactory.performance.report("incr", cacheKey, cCost, eCost);
		}

		return -1L;
	}
	
	@Override
	public long decr(String cacheKey) {
		return decr(cacheKey, 1);
	}

	@Override
	public long decr(String cacheKey, int inc) {

		if (null == cacheKey) return -1;
		cacheKey = sanitizeKey(cacheKey);
		if (null == cacheKey) return -1;

		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(cacheKey);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) {
			log.error("从MemCache连接池获取SockIO对象为空!");
			return 0;
		}
		
		try {

			io.write(CMD_DECR);
			io.write(SPACE);
			io.write(cacheKey.getBytes());
			io.write(SPACE);
			io.write(IOUtil.encode(inc));
			io.write(CRLF);
			io.flush();

			byte[] bytes = io.readLineBytes();
			if (Arrays.equals(bytes, SERVER_STATUS_BYTES_NOT_FOUND)) {
				return -1;
			} else if (equals(bytes, SERVER_STATUS_BYTES_CLIENT_ERROR, SERVER_STATUS_BYTES_CLIENT_ERROR.length)) {
				log.error("指令格式错误!" + new String(bytes));
				return -1;
			} else if (equals(bytes, SERVER_STATUS_BYTES_SERVER_ERROR, SERVER_STATUS_BYTES_SERVER_ERROR.length)) {
				log.error("缓存服务器内部错误！" + new String(bytes));
				return -1;
			}

			return FastConvertor.strBytes2Long(bytes);

		} catch (Exception e) {
			log.error("decr发生错误！", e);
		} finally {
			if (null != io) io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			MemCacheFactory.performance.report("decr", cacheKey, cCost, eCost);
		}

		return -1;

	}

	@Override
	public boolean touch(String cacheKey, int secTTL) {

		if (null == cacheKey) return false;
		cacheKey = sanitizeKey(cacheKey);
		if (null == cacheKey) return false;

		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(cacheKey);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) {
			log.error("从MemCache连接池获取SockIO对象为空!");
			return false;
		}
		
		try {
			
			io.write(CMD_TOUCH);
			io.write(SPACE);
			io.write(cacheKey.getBytes());
			io.write(SPACE);
			io.write(IOUtil.encode(secTTL));
			io.write(CRLF);
			io.flush();

			byte[] bytes = io.readLineBytes();
			if (Arrays.equals(bytes, SERVER_STATUS_BYTES_ERROR)) {
				log.error("缓存服务器接收到不可识别的指令TOUCH，请确认服务器版本>=1.4.15，服务端返回：" + new String(bytes));
				return false;
			} else if (equals(bytes, SERVER_STATUS_BYTES_CLIENT_ERROR, SERVER_STATUS_BYTES_CLIENT_ERROR.length)) {
				log.error("指令格式错误!" + new String(bytes));
				return false;
			} else if (equals(bytes, SERVER_STATUS_BYTES_SERVER_ERROR, SERVER_STATUS_BYTES_SERVER_ERROR.length)) {
				log.error("缓存服务器内部错误！" + new String(bytes));
				return false;
			}
			
			if (Arrays.equals(bytes, SERVER_STATUS_BYTES_TOUCHED)) {
				return true;
			}
			
		} catch (Exception e) {
			log.error("touch发生错误！", e);
		} finally {
			if (null != io) io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			MemCacheFactory.performance.report("touch", cacheKey, cCost, eCost);
		}

		return false;

	}

	/**
	 * 对两字节数组，前len个字节进行比较
	 * 
	 * @param b1
	 * @param b2
	 * @param len
	 * @return
	 */
	private static final boolean equals(byte[] b1, byte[] b2, int len) {
		if (b1.length < len || b2.length < len) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (b1[i] != b2[i]) return false;
		}
		return true;
	}
	
	/**
	 * 命令行的快速解析，返回flag，size。
	 * 
	 * @param bytes
	 * @return
	 */
	private static final int[] cmdParse2(byte[] bytes) {
		int flagStart = 0, flagEnd = 0, sizeStart = 0, sizeEnd = 0;
		int count = 0;
		for (int i = 5; i < bytes.length; i++) {
			if (' ' == bytes[i]) {
				count++;
				if (2 == count) flagStart = i + 1;
				if (3 == count) {
					flagEnd = i;
					sizeStart = i + 1;
					sizeEnd = bytes.length;
				}
			}
		}
		int flag = FastConvertor.strBytes2Int(bytes, flagStart, flagEnd);
		int size = FastConvertor.strBytes2Int(bytes, sizeStart, sizeEnd);
		return new int[] {flag, size};
	}
	
	/**
	 * 对Key进行编码，避免memcached交互协议冲突
	 * 
	 * @param rawKey
	 * @return
	 */
	private static final String sanitizeKey(String rawKey) {
		
		String rtn = null;
		
		try {
			rtn = URLEncoder.encode(rawKey, "UTF-8");
			
			if (MAX_KEYSIZE < rtn.length()) {
				rtn = MD5Util.hexdigest(rtn);
			}
			
		} catch (UnsupportedEncodingException e) {
			log.error("编码时发生错误!", e);
		}
		
		return rtn;
		
	}
}