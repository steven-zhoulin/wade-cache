package com.ailk.cache.redis.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ailk.cache.redis.Command;
import com.ailk.cache.redis.RedisAddress;
import com.ailk.cache.redis.RedisFactory;
import com.ailk.cache.redis.driver.io.ISockIO;
import com.ailk.cache.redis.driver.io.SockIOPool;
import com.ailk.cache.util.IOUtil;
import com.ailk.cache.util.serial.DefaultSerializable;
import com.ailk.cache.util.serial.ISerializable;
import com.ailk.org.apache.commons.lang3.StringUtils;

/**
 * Copyright: Copyright (c) 2013 Asiainfo
 * 
 * @className: BinaryRedisClient
 * @description: 客户端（基于原始二进制方式实现）
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-5
 */
public class BinaryRedisClient {

	private static final Logger log = Logger.getLogger(BinaryRedisClient.class);

	public static final byte[] REDIS_CMD_EX = "EX".getBytes();
	public static final byte[] CRLF = "\r\n".getBytes();
	public static final byte[] REDIS_REPLAY_OK = "OK".getBytes();
	
	public static final byte REDIS_REPLY_STRING = '$';
	public static final byte REDIS_REPLY_ARRAY = '*';
	public static final byte REDIS_REPLY_STATUS = '+';
	public static final byte REDIS_REPLY_ERROR = '-';
	public static final byte REDIS_REPLY_INTEGER = ':';

	/**
	 * 对象序列化与反序列化接口。
	 */
	protected static final ISerializable SERIALIZER = new DefaultSerializable();
	
	private SockIOPool pool;

	public BinaryRedisClient(RedisAddress[] address, int poolSize, int heartbeatSecond, boolean useNIO) {
		try {
			this.pool = new SockIOPool(address, poolSize, heartbeatSecond, useNIO);
		} catch (Exception e) {
			log.error("初始化redis连接池出错！" + StringUtils.join(address, ','), e);
		}
	}

	/**
	 * 分片检查
	 */
	protected final void assertNotSharding () {
		if (pool.isSharding()) {
			throw new RuntimeException("该指令不支持Sharding,请采用单节点,不要配置Redis集群!");
		}
	}
	
	/******************************************************************
	 *                          Keys相关指令
	 *****************************************************************/

	/**
	 * 删除一个或多个KEY
	 * 
	 * @param key
	 * @return 被删除KEY的个数
	 */
	public long del(byte[]... keys) {
		if (null == keys || 0 == keys.length) {
			throw new IllegalArgumentException("keys不可为空!");
		}
		
		if (keys.length > 1) {
			assertNotSharding();
		}
		
		Long n = (Long) cliSendCommand(Command.DEL, keys);
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 查找所有满足指定模式的key集合
	 * 
	 * @param pattern KEY的模式串
	 * @return
	 */
	public Set<byte[]> keys(byte[] pattern) {
		
		assertNotSharding();
		
		if (null == pattern) {
			throw new IllegalArgumentException("pattern不可为空!");
		}
		
		byte[][] keys = (byte[][]) cliSendCommand(Command.KEYS, pattern);
		Set<byte[]> rtn = new HashSet<byte[]>(keys.length);
		for (byte[] key : keys) {
			rtn.add(key);
		}
		return rtn;
	}

	/**
	 * 判断一个key是否存在
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.EXISTS, key);
		if (null == n) {
			return false;
		}
		
		return n == 1L ? true : false;
	}
	
	/**
	 * 设置一个key，多少秒钟后超时
	 * 
	 * @param key
	 * @param secTTL
	 * @return
	 */
	public boolean expire(byte[] key, int secTTL) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.EXPIRE, key, IOUtil.encode(secTTL));
		if (null == n) {
			return false;
		}
		
		return n == 1L ? true : false;
	}
	
	/******************************************************************
	 *                        Strings相关指令
	 *****************************************************************/
	
	/**
	 * 往指定key对应的value里追加数据
	 * 
	 * @param key KEY
	 * @param value 追加数据
	 * @return 追加后，key对应value的长度。
	 */
	public long append(byte[] key, byte[] value) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == value) {
			throw new IllegalArgumentException("value不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.APPEND, key, value);
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 设置一对KV值
	 * 
	 * @param key
	 * @param value
	 * @return 成功true，失败false
	 */
	public boolean set(byte[] key, byte[] value) {

		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == value) {
			throw new IllegalArgumentException("value不可为空!");
		}
		
		byte[] rtn = (byte[]) cliSendCommand(Command.SET, key, value);
		return Arrays.equals(rtn, REDIS_REPLAY_OK);
	}

	/**
	 * 设置一对KV值,带超时秒数
	 * 
	 * @param key
	 * @param value
	 * @param secTTL
	 * @return 成功true，失败false
	 */
	public boolean set(byte[] key, byte[] value, long secTTL) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == value) {
			throw new IllegalArgumentException("value不可为空!");
		}
		
		byte[] rtn = (byte[]) cliSendCommand(Command.SET, key, value, REDIS_CMD_EX, IOUtil.encode(secTTL));
		return Arrays.equals(rtn, REDIS_REPLAY_OK);
	}

	/**
	 * 根据指定的KEY，获取对应的VALUE
	 * 
	 * @param key
	 * @return
	 */
	public byte[] get(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		return (byte[]) cliSendCommand(Command.GET, key);
	}

	/**
	 * 获取指定KEY，对应VALUE的长度
	 * 
	 * @param key
	 * @return
	 */
	public long strlen(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.STRLEN, key);
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 按步长1，递增
	 * 
	 * @param key
	 * @return
	 */
	public long incr(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.INCR, key);
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 按指定步长，递增
	 * 
	 * @param key
	 * @param increment
	 * @return
	 */
	public long incrby(byte[] key, int increment) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.INCRBY, key, IOUtil.encode(increment));
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 按步长-1,递减
	 * 
	 * @param key
	 * @return
	 */
	public long decr(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.DECR, key);
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 按指定步长，递减
	 * 
	 * @param key
	 * @param decrement
	 * @return
	 */
	public long decrby(byte[] key, int decrement) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.DECRBY, key, IOUtil.encode(decrement));
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 设置或清除字符串指定位置上bit位
	 * 
	 * @param key
	 * @param offset 偏移
	 * @param b true: 设置, false: 清除
	 * @return 指定偏移量原来存储的位
	 */
	public boolean setbit(byte[] key, int offset, boolean b) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (offset < 0) {
			throw new IllegalArgumentException("offset值必须>=0");
		}
		
		long value = b ? 1 : 0;
		Long n = (Long) cliSendCommand(Command.SETBIT, key, IOUtil.encode(offset), IOUtil.encode(value));
		return n == 0 ? false : true;
	}

	/**
	 * 对 key所储存的字符串值，获取指定偏移量上的位(bit)，当 offset比字符串值的长度大，或者 key不存在时，返回false
	 * 
	 * @param key
	 * @param offset
	 * @return 字符串值指定偏移量上的位(bit)，为1返回true，为0返回false
	 */
	public boolean getbit(byte[] key, int offset) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (offset < 0) {
			throw new IllegalArgumentException("offset值必须>=0");
		}
		
		Long n = (Long) cliSendCommand(Command.GETBIT, key, IOUtil.encode(offset));
		return n == 0 ? false : true;
	}
	
	/**
	 * 计算给定字符串中，被设置为true的比特位的数量，通过指定额外的start或end参数，可以只在特定的位上进行。
	 * 
	 * @param key
	 * @param start 按字节进行偏移
	 * @param end 按字节进行偏移
	 * @return
	 */
	public long bitcount(byte[] key, int start, int end) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (start < 0) {
			throw new IllegalArgumentException("start值必须>=0");
		}
		
		Long n = (Long) cliSendCommand(Command.BITCOUNT, key, IOUtil.encode(start), IOUtil.encode(end));
		return n;
	}
	
	/******************************************************************
	 *                        Hashs相关指令
	 *****************************************************************/
	
	/**
	 * 删除一个或多个hash field
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public long hdel(byte[] key, byte[]... fields) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == fields || 0 == fields.length) {
			throw new IllegalArgumentException("fields不可为空!");
		}
		
		byte[][] args = new byte[fields.length + 1][];
		args[0] = key;
		System.arraycopy(fields, 0, args, 1, fields.length);
		Long rtn = (Long) cliSendCommand(Command.HDEL, args);
		return null == rtn ? 0 : rtn;
	}

	/**
	 * 按指定步长，递增某个field
	 * 
	 * @param key 
	 * @param field
	 * @return
	 */
	public long hincrby(byte[] key, byte[] field, long value) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == field) {
			throw new IllegalArgumentException("field不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.HINCRBY, key, field, IOUtil.encode(value));
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 批量设置K-V
	 * 
	 * @param key 
	 * @param field
	 * @return
	 */
	public boolean hmset(byte[] key, Map<byte[], byte[]> map) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == map) {
			throw new IllegalArgumentException("map不可为空!");
		}
		
		byte[][] args = new byte[map.size() * 2 + 1][];
		args[0] = key;
		int i = 1;
		for (byte[] k : map.keySet()) {
			args[i++] = k;
			args[i++] = map.get(k);
		}
		byte[] rtn = (byte[]) cliSendCommand(Command.HMSET, args);
		return Arrays.equals(rtn, REDIS_REPLAY_OK);
	}

	/**
	 * 批量获取K-V
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	public Map<byte[], byte[]> hmget(byte[] key, byte[]... fields) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == fields || 0 == fields.length) {
			throw new IllegalArgumentException("fields不可为空!");
		}
		
		Map<byte[], byte[]> rtn = new HashMap<byte[], byte[]>();

		byte[][] args = new byte[fields.length + 1][];
		args[0] = key;
		System.arraycopy(fields, 0, args, 1, fields.length);
		byte[][] values = (byte[][]) cliSendCommand(Command.HMGET, args);

		int i = 0;
		for (byte[] field : fields) {
			byte[] curr = values[i++];
			if (null == curr) {
				continue;
			}
			
			rtn.put(field, curr);
		}
		
		return rtn;
	}

	/**
	 * 获取hash中所有的value
	 * 
	 * @param key
	 * @return
	 */
	public Set<byte[]> hvals(byte[] key) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Set<byte[]> rtn = new HashSet<byte[]>();
		byte[][] values = (byte[][]) cliSendCommand(Command.HVALS, key);
		for (byte[] value : values) {
			rtn.add(value);
		}
		return rtn;
	}

	/**
	 * 判断hash中是否存在指定的field
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public boolean hexists(byte[] key, byte[] field) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == field) {
			throw new IllegalArgumentException("field不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.HEXISTS, key, field);
		if (null == n) {
			return false;
		}
		
		return n == 1L ? true : false;
	}

	/**
	 * 获取hash中field对应的value
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public byte[] hget(byte[] key, byte[] field) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == field) {
			throw new IllegalArgumentException("field不可为空!");
		}
		
		return (byte[]) cliSendCommand(Command.HGET, key, field);
	}

	/**
	 * 获取hash中所有的key
	 * 
	 * @param key
	 * @return
	 */
	public Set<byte[]> hkeys(byte[] key) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Set<byte[]> rtn = new HashSet<byte[]>();
		byte[][] values = (byte[][]) cliSendCommand(Command.HKEYS, key);
		for (byte[] value : values) {
			rtn.add(value);
		}
		return rtn;
	}

	/**
	 * 往hash里设置K-V
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return 如果hash中不存在filed则返回1，否则返回0
	 */
	public long hset(byte[] key, byte[] field, byte[] value) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == field) {
			throw new IllegalArgumentException("field不可为空!");
		}
		
		if (null == value) {
			throw new IllegalArgumentException("value不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.HSET, key, field, value);
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	/**
	 * 获取hash中所有的K-V，屏蔽此函数
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unused")
	private Map<byte[], byte[]> hgetAll(byte[] key) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Map<byte[], byte[]> rtn = new HashMap<byte[], byte[]>();
		byte[][] entry = (byte[][]) cliSendCommand(Command.HGETALL, key);

		if (null == entry) {
			return rtn;
		}

		for (int i = 0, size = (entry.length - 1); i < size; i += 2) {
			rtn.put(entry[i], entry[i + 1]);
		}
		return rtn;
	}

	/**
	 * 返回hash中K-V对数量
	 * 
	 * @param key
	 * @return
	 */
	public long hlen(byte[] key) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.HLEN, key);
		if (null == n) {
			return 0;
		}
		
		return n;
	}
	
	/**
	 * 当field不存在时，设置field-value对
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public long hsetnx(byte[] key, byte[] field, byte[] value) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == field) {
			throw new IllegalArgumentException("field不可为空!");
		}
		
		if (null == value) {
			throw new IllegalArgumentException("value不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.HSETNX, new byte[][] { key, field, value });
		if (null == n) {
			return 0;
		}
		
		return n;
	}

	
	/******************************************************************
	 *                        Lists相关指令
	 *****************************************************************/
    
	/**
	 * 返回指定队列的长度
	 * 
	 * @param key 队列名
	 * @return
	 */
	
	public long llen(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.LLEN, key);
		if (null == n) {
			return 0;
		}
		
		return n;
	}
	
	/**
	 * 从队列左边弹出一个数据，如果队列为空，则返回null
	 * 
	 * @param secTTL 多少秒后超时，传0表示不超时
	 * @param keys 队列名
	 * @return
	 */
	public byte[] lpop(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		return (byte[]) cliSendCommand(Command.LPOP, key);
	}
	
	/**
	 * 从队列右边弹出一个数据，如果队列为空，则返回null
	 * 
	 * @param secTTL 多少秒后超时，传0表示不超时
	 * @param keys 队列名
	 * @return
	 */
	public byte[] rpop(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		return (byte[]) cliSendCommand(Command.RPOP, key);
	}
	
	/**
	 * 从队列左边弹出一个数据，如果队列为空，则阻塞secTTL秒
	 * 
	 * @param secTTL 多少秒后超时，传0表示不超时
	 * @param keys 队列名
	 * @return
	 */
	public byte[][] blpop(long secTTL, byte[]... keys) {
		
		if (null == keys) {
			throw new IllegalArgumentException("keys不可为空!");
		}
		
		if (keys.length > 1) {
			assertNotSharding();
		}
		
		byte[][] args = new byte[keys.length + 1][];
		System.arraycopy(keys, 0, args, 0, keys.length);
		args[keys.length] = IOUtil.encode(secTTL);
		byte[][] rtn = (byte[][]) cliSendCommand(Command.BLPOP, args);
		return rtn;
	}
	
	/**
	 * 从队列右边弹出一个数据，如果队列为空，则阻塞secTTL秒
	 * 
	 * @param secTTL 多少秒后超时，传0表示不超时
	 * @param keys 队列名
	 * @return
	 */
	public byte[][] brpop(long secTTL, byte[]... keys) {
		
		if (null == keys) {
			throw new IllegalArgumentException("keys不可为空!");
		}
		
		if (keys.length > 1) {
			assertNotSharding();
		}
		
		byte[][] args = new byte[keys.length + 1][];
		System.arraycopy(keys, 0, args, 0, keys.length);
		args[keys.length] = IOUtil.encode(secTTL);
		byte[][] rtn = (byte[][]) cliSendCommand(Command.BRPOP, args);
		return rtn;
	}
	
	/**
	 * 往队列左端追加数据
	 * 
	 * @param key 队列名
	 * @param values 数据
	 * @return 返回追加数据后的队列长度
	 */
	public long lpush(byte[] key, byte[]... values) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == values || 0 == values.length) {
			throw new IllegalArgumentException("values不可为空!");
		}
		
		byte[][] args = new byte[values.length + 1][];
		args[0] = key;
		System.arraycopy(values, 0, args, 1, values.length);
		
		Long n = (Long) cliSendCommand(Command.LPUSH, args);
		if (null == n) {
			return 0;
		}
		
		return n;
	}
	
	/**
	 * 往队列右端追加数据
	 * 
	 * @param key 队列名
	 * @param values 数据
	 * @return 返回追加数据后的队列长度
	 */
	public long rpush(byte[] key, byte[]... values) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == values || 0 == values.length) {
			throw new IllegalArgumentException("values不可为空!");
		}
		
		byte[][] args = new byte[values.length + 1][];
		args[0] = key;
		System.arraycopy(values, 0, args, 1, values.length);
		
		Long n = (Long) cliSendCommand(Command.RPUSH, args);
		if (null == n) {
			return 0;
		}
		
		return n;
		
	}
	
	/**
	 * 当队列已经存在时，往队列左端追加数据
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public long lpushx(byte[] key, byte[] value) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == value) {
			throw new IllegalArgumentException("values不可为空!");
		}
		
		Long n = (Long) cliSendCommand(Command.LPUSHX, key, value);
		if (null == n) {
			return 0;
		}
		
		return n;
	}
	
	/**
	 * 当队列已经存在时，往队列右端追加数据
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public long rpushx(byte[] key, byte[] value) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == value) {
			throw new IllegalArgumentException("values不可为空!");
		}
		
		Long rtn = (Long) cliSendCommand(Command.RPUSHX, key, value);
		return null == rtn ? 0 : rtn;
	}
	
	/**
	 * 从队列中，获取指定开始与结束位置之间的所有数据。
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public byte[][] lrange(byte[] key, int start, int end) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		byte[] startByte = IOUtil.encode(start);
		byte[] endByte = IOUtil.encode(end);
		return (byte[][]) cliSendCommand(Command.LRANGE, key, startByte, endByte);
	}
	
	/**
	 * 获取队列中指定位置的元素
	 * 
	 * @param key
	 * @param index
	 * @return
	 */
	public byte[] lindex(byte[] key, int index) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		return (byte[]) cliSendCommand(Command.LINDEX, key, IOUtil.encode(index));
	}
	
	/**
	 * 往队列指定位置设置数据
	 * 
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 */
	public boolean lset(byte[] key, int index, byte[] value) {
		
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == value) {
			throw new IllegalArgumentException("values不可为空!");
		}
		
		byte[] rtn = (byte[]) cliSendCommand(Command.LSET, key, IOUtil.encode(index), value);
		return Arrays.equals(rtn, REDIS_REPLAY_OK);
	}
	
	/******************************************************************
	 *                         Sets相关指令
	 *****************************************************************/

	/**
	 * 向集合中添加一个元素
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public long sadd(byte[] key, byte[]... member) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == member || 0 == member.length) {
			throw new IllegalArgumentException("member不可为空!");
		}
		
		byte[][] args = new byte[member.length + 1][];
		args[0] = key;
		System.arraycopy(member, 0, args, 1, member.length);
		Long rtn = (Long) cliSendCommand(Command.SADD, args);
		return null == rtn ? 0 : rtn;
	}
	
	/**
	 * 获取集合中所有元素
	 * 
	 * @param key
	 * @return
	 */
	public Set<byte[]> smembers(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
				
		byte[][] members = (byte[][]) cliSendCommand(Command.SMEMBERS, key);
		Set<byte[]> rtn = new HashSet<byte[]>(members.length);
		for (byte[] member : members) {
			rtn.add(member);
		}
		
		return rtn;
	}
	
	/**
	 * 从集合中删除一个元素
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public long srem(byte[] key, byte[] member) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == member) {
			throw new IllegalArgumentException("member不可为空!");
		}
		
		Long rtn = (Long) cliSendCommand(Command.SREM, key, member);
		return null == rtn ? 0 : rtn;
	}
	
	/**
	 * 随机从集合中弹出一个元素
	 * 
	 * @param key
	 * @return
	 */
	public byte[] spop(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		return (byte[]) cliSendCommand(Command.SPOP, key);
	}
	
	/**
	 * 将元素从一个集合移动到另一个集合
	 * 
	 * @param srckey
	 * @param dstkey
	 * @param member
	 * @return
	 */
	public long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		
		assertNotSharding();
		
		if (null == srckey) {
			throw new IllegalArgumentException("srckey不可为空!");
		}
		
		if (null == dstkey) {
			throw new IllegalArgumentException("dstkey不可为空!");
		}
		
		if (null == member) {
			throw new IllegalArgumentException("member不可为空!");
		}
		
		Long rtn = (Long) cliSendCommand(Command.SMOVE, srckey, dstkey, member);
		return null == rtn ? 0 : rtn;
	}
	
	/**
	 * 获取集合中元素个数
	 * 
	 * @param key
	 * @return
	 */
	public long scard(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		Long rtn = (Long) cliSendCommand(Command.SCARD, key);
		return null == rtn ? 0 : rtn;
	}
	
	/**
	 * 判断元素是否存在于集合中
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public boolean sismember(byte[] key, byte[] member) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		
		if (null == member) {
			throw new IllegalArgumentException("member不可为空!");
		}
		
		Long rtn = (Long) cliSendCommand(Command.SISMEMBER, key, member);
		if (null == rtn) {
			return false;
		}
		
		return 1L == rtn ? true : false;
	}
	
	/**
	 * 计算两个集合的交集
	 * 
	 * @param keys
	 * @return
	 */
	public Set<byte[]> sinter(byte[][] keys) {
		
		assertNotSharding();
		
		if (null == keys || keys.length < 2) {
			throw new IllegalArgumentException("keys不可为空!");
		}
				
		byte[][] members = (byte[][]) cliSendCommand(Command.SINTER, keys);
		Set<byte[]> rtn = new HashSet<byte[]>(members.length);
		for (byte[] member : members) {
			rtn.add(member);
		}
		
		return rtn;
	}
	
	/**
	 * 计算两个集合的交集，并存进另一个集合中
	 * 
	 * @param dstkey
	 * @param keys
	 * @return
	 */
	public long sinterstore(byte[] dstkey, byte[][] keys) {
		
		assertNotSharding();
		
		if (null == dstkey) {
			throw new IllegalArgumentException("dstkey不可为空!");
		}
		
		if (null == keys || keys.length < 2) {
			throw new IllegalArgumentException("keys不可为空!");
		}
		
		byte[][] args = new byte[keys.length + 1][];
		args[0] = dstkey;
		System.arraycopy(keys, 0, args, 1, keys.length);
		
		Long rtn = (Long) cliSendCommand(Command.SINTERSTORE, args);
		return null == rtn ? 0 : rtn;
	}
	
	/**
	 * 计算两个集合的并集
	 * 
	 * @param keys
	 * @return
	 */
	public Set<byte[]> sunion(byte[][] keys) {
		
		assertNotSharding();
		
		if (null == keys || keys.length < 2) {
			throw new IllegalArgumentException("keys不可为空!");
		}
		
		byte[][] members = (byte[][]) cliSendCommand(Command.SUNION, keys);
		Set<byte[]> rtn = new HashSet<byte[]>(members.length);
		for (byte[] member : members) {
			rtn.add(member);
		}
		
		return rtn;
	}
	
	/**
	 * 计算两个集合的并集，并存进另一个集合中
	 * 
	 * @param dstkey
	 * @param keys
	 * @return
	 */
	public long sunionstore(byte[] dstkey, byte[][] keys) {
		
		assertNotSharding();
		
		if (null == dstkey) {
			throw new IllegalArgumentException("dstkey不可为空!");
		}
		
		if (null == keys || keys.length < 2) {
			throw new IllegalArgumentException("keys不可为空!");
		}
		
		byte[][] args = new byte[keys.length + 1][];
		args[0] = dstkey;
		System.arraycopy(keys, 0, args, 1, keys.length);
		
		Long rtn = (Long) cliSendCommand(Command.SUNIONSTORE, args);
		return null == rtn ? 0 : rtn;
	}
	
	/**
	 * 计算两个集合的差集
	 * 
	 * @param keys
	 * @return
	 */
	public Set<byte[]> sdiff(byte[][] keys) {
		
		assertNotSharding();
		
		if (null == keys || keys.length < 2) {
			throw new IllegalArgumentException("keys不可为空!");
		}
		
		byte[][] members = (byte[][]) cliSendCommand(Command.SDIFF, keys);
		Set<byte[]> rtn = new HashSet<byte[]>(members.length);
		for (byte[] member : members) {
			rtn.add(member);
		}
		
		return rtn;
	}
	
	/**
	 * 计算两个集合的差集，并存储进另一个集合
	 * 
	 * @param dstkey
	 * @param keys
	 * @return
	 */
	public long sdiffstore(byte[] dstkey, byte[][] keys) {
		
		assertNotSharding();
		
		if (null == dstkey) {
			throw new IllegalArgumentException("dstkey不可为空!");
		}
		
		if (null == keys || keys.length < 2) {
			throw new IllegalArgumentException("keys不可为空!");
		}
		
		byte[][] args = new byte[keys.length + 1][];
		args[0] = dstkey;
		System.arraycopy(keys, 0, args, 1, keys.length);
		
		Long rtn = (Long) cliSendCommand(Command.SDIFFSTORE, args);
		return null == rtn ? 0 : rtn;
	}
	
	/**
	 * 随机获取集合中的一个元素
	 * 
	 * @param key
	 * @return
	 */
	public byte[] srandmember(byte[] key) {
		if (null == key) {
			throw new IllegalArgumentException("key不可为空!");
		}
		return (byte[]) cliSendCommand(Command.SRANDMEMBER, key);
	}
	
	/******************************************************************
	 *                       Connection相关指令
	 *****************************************************************/
	
	/**
	 * 服务端认证
	 */
	public void auth(byte[] password) {
		if (null == password) {
			throw new IllegalArgumentException("key不可为空!");
		}
		cliSendCommand(Command.AUTH, password);
	}
		
	/**
	 * 服务端回显
	 * 
	 * @param message
	 * @return
	 */
	public byte[] echo(byte[] message) {
		if (null == message) {
			throw new IllegalArgumentException("key不可为空!");
		}
		return (byte[]) cliSendCommand(Command.ECHO, message);
	}
	
	/**
	 * 发送命令
	 * 
	 * @param command
	 * @param args
	 * @return
	 */
	protected Object cliSendCommand(Command command, byte[]... args) {
		
		String key = new String(args[0]);
		
		long cStart = System.currentTimeMillis();
		ISockIO io = pool.getSock(key);
		long eStart = System.currentTimeMillis();
		long cCost = eStart - cStart;
		
		if (null == io) { // 当桶都挂掉时，返回null
			return null;
		}
		
		try {
			io.write(REDIS_REPLY_ARRAY);
			io.write(IOUtil.encode(args.length + 1));
			io.write(CRLF);
			io.write(REDIS_REPLY_STRING);
			io.write(IOUtil.encode(command.raw.length));
			io.write(CRLF);
			io.write(command.raw);
			io.write(CRLF);

			for (byte[] arg : args) {
				io.write(REDIS_REPLY_STRING);
				io.write(IOUtil.encode(arg.length));
				io.write(CRLF);
				io.write(arg);
				io.write(CRLF);
			}

			io.flush();
			Object rtn = cliReadReply(io);			
			return rtn;
			
		} catch (Exception e) {
			log.error("redis发生错误！", e);
		} finally {
			io.release();
			
			// 发送性能日志
			long eCost = System.currentTimeMillis() - eStart;
			RedisFactory.performance.report(command.name(), key, cCost, eCost);
		}

		
		return null;
	}

	/**
	 * 读取返回结果
	 * 
	 * @param io
	 * @return
	 * @throws IOException
	 */
	private static Object cliReadReply(ISockIO io) throws IOException {
		byte b = io.read();
		switch (b) {
			case REDIS_REPLY_STRING:
				return processBulkReply(io);
			case REDIS_REPLY_ARRAY:
				return processMultiBulkReply(io);
			case REDIS_REPLY_STATUS:
				return processStatusCodeReply(io);
			case REDIS_REPLY_INTEGER:
				return processInteger(io);
			case REDIS_REPLY_ERROR:
				processError(io);
				break;
			default:
				throw new RuntimeException("Unknown reply: " + (char) b);
		}

		return null;
	}

	/**
	 * 返回整型
	 * 
	 * @param io
	 * @return
	 * @throws IOException
	 */
	private static final Long processInteger(ISockIO io) throws IOException {
		byte[] bytes = io.readLineBytes();
		return Long.valueOf(new String(bytes));
	}

	/**
	 * 返回单数据
	 * 
	 * @param io
	 * @return
	 * @throws IOException
	 */
	private static final byte[] processBulkReply(ISockIO io) throws IOException {
		byte[] bytes = io.readLineBytes();
		long size = IOUtil.parseLong(bytes);
		int iSize = (int) size;
		if (iSize < 0) {
			return null;
		}

		bytes = new byte[iSize];
		int cnt = 0;
		while (cnt < iSize) {
			cnt += io.read(bytes, cnt, (iSize - cnt));
		}
		
		io.readLineBytes();
		return bytes;
	}

	/**
	 * 返回多数据
	 * 
	 * @param io
	 * @return
	 * @throws IOException
	 */
	private static byte[][] processMultiBulkReply(ISockIO io) throws IOException {
		byte[] bytes = io.readLineBytes();
		int num = IOUtil.parseInt(bytes);
		if (num <= -1) {
			return null;
		}

		byte[][] rtn = new byte[num][];

		for (int i = 0; i < num; i++) {
			rtn[i] = (byte[]) cliReadReply(io);
		}
		return rtn;
	}
	
	/**
	 * 返回状态
	 * 
	 * @param io
	 * @return
	 */
	private static byte[] processStatusCodeReply(ISockIO io) {
		byte[] rtn = null;
		try {
			rtn = io.readLineBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rtn;
	}

	/**
	 * 返回异常
	 * 
	 * @param io
	 */
	private static void processError(ISockIO io) {
		String message = null;
		try {
			message = new String(io.readLineBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.error("redis异常:" + message);
	}
}
