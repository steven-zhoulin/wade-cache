package com.ailk.cache.redis.client;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ailk.cache.redis.RedisFactory;
import com.ailk.cache.redis.client.BinaryRedisClient;

public class BinaryRedisClientTest {

	private static BinaryRedisClient client = RedisFactory.getRedisClient("sna");
	private static byte[] key = "KEY".getBytes();
	private static byte[] field = "FIELD".getBytes();
	private static byte[] value = "VALUE".getBytes();

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testAppend() {

		client.del(key);
		byte[] bytes = "0123456789".getBytes();
		for (int i = 0; i < bytes.length; i++) {
			client.append(key, new byte[] { bytes[i] });
		}

		byte[] rtn = client.get(key);
		assertArrayEquals(rtn, bytes);
	}

	@Test
	public void testSet() {
		byte[] values = "ABCDEFG".getBytes();
		client.del(key);
		client.set(key, values);
		byte[] rtn = client.get(key);
		assertArrayEquals(rtn, values);
	}

	@Test
	public void testExpire() throws InterruptedException {
		client.set(key, value);
		assertTrue(client.exists(key));
		boolean b = client.expire(key, 2);
		System.out.println("testExpire ---> expire=" + b);
		assertTrue(client.exists(key));
		Thread.sleep(4000);
		System.out.println("testExpire ---> " + key + "=" + client.get(key));
		assertFalse(client.exists(key));
	}
	
	@Test
	public void testSetTTL() throws InterruptedException {
		byte[] values = "ABCDEFG".getBytes();
		client.del(key);
		client.set(key, values, 1);
		byte[] rtn = client.get(key);
		assertArrayEquals(rtn, values);

		Thread.sleep(2000);
		rtn = client.get(key);
		assertNull(rtn);
	}

	@Test
	public void testStrlen() {
		byte[] values = "ABCDEFG".getBytes();
		client.del(key);
		client.set(key, values);
		long rtn = client.strlen(key);
		assertEquals(rtn, values.length);
	}

	@Test
	public void testIncr() {
		client.del(key);
		for (int i = 0; i < 1000; i++) {
			client.incr(key);
		}

		long rtn = client.incr(key);
		assertEquals(rtn, 1001);
	}

	@Test
	public void testIncrBy() {
		client.del(key);
		for (int i = 0; i < 100; i++) {
			client.incrby(key, 2);
		}

		long rtn = client.incrby(key, 2);
		assertEquals(rtn, 202);
	}

	@Test
	public void testDecr() {
		client.del(key);
		for (int i = 0; i < 100; i++) {
			client.incrby(key, 2);
			client.decr(key);
		}

		long rtn = client.incr(key);
		assertEquals(rtn, 101);
	}

	@Test
	public void testDecrby() {
		client.del(key);
		for (int i = 0; i < 100; i++) {
			client.incrby(key, 2);
			client.decrby(key, 2);
		}

		long rtn = client.incr(key);
		assertEquals(rtn, 1);
	}

	@Test
	public void testSetbit() {
		boolean b = client.setbit(key, 1024, true);
		assertFalse(b);
		b = client.setbit(key, 1024, true);
		assertTrue(b);
		
		b = client.getbit(key, 1024);
		assertTrue(b);
		
		b = client.getbit(key, 0);
		assertFalse(b);
		b = client.getbit(key, 1);
		assertFalse(b);
	}
	
	@Test
	public void testBitcount() {
		client.del(key);
		client.setbit(key, 0, true);
		client.setbit(key, 1, true);
		assertTrue(2 == client.bitcount(key, 0, 0));
		assertTrue(0 == client.bitcount(key, 1, 1));
	}
	
	@Test
	public void testHdel() {
		client.del(key);
		client.hdel(key, field);
		long rtn = client.hdel(key, field);
		assertEquals(rtn, 0);
	}

	@Test
	public void testHincrby() {
		client.del(key);
		long rtn = 0;
		for (int i = 0; i < 100; i++) {
			rtn = client.hincrby(key, field, 2);
		}
		assertEquals(rtn, 200);
	}

	@Test
	public void testHmset() {
		// 由其它test方法替代其测试
	}

	@Test
	public void testHvals() {
		client.del(key);
		Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
		Set<String> expect = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			byte[] k = String.valueOf(i).getBytes();
			byte[] v = String.valueOf(i + 100).getBytes();
			map.put(k, v);
			expect.add(new String(v));
		}
		client.hmset(key, map);

		Set<String> real = new HashSet<String>();
		for (byte[] v : client.hvals(key)) {
			real.add(new String(v));
		}

		assertEquals(expect.size(), real.size());
		assertTrue(expect.containsAll(real));
	}

	@Test
	public void testHexists() {
		client.del(key);
		Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
		for (int i = 0; i < 10; i++) {
			map.put(String.valueOf(i).getBytes(), String.valueOf(i + 100).getBytes());
		}
		client.hmset(key, map);
		assertTrue(client.hexists(key, "0".getBytes()));
		assertTrue(client.hexists(key, "1".getBytes()));
		assertTrue(client.hexists(key, "2".getBytes()));
		assertTrue(client.hexists(key, "3".getBytes()));
		assertTrue(client.hexists(key, "4".getBytes()));
		assertTrue(client.hexists(key, "5".getBytes()));
		assertTrue(client.hexists(key, "6".getBytes()));
		assertTrue(client.hexists(key, "7".getBytes()));
		assertTrue(client.hexists(key, "8".getBytes()));
		assertTrue(client.hexists(key, "9".getBytes()));
		assertFalse(client.hexists(key, "A".getBytes()));
	}

	@Test
	public void testHget() {
		client.del(key);
		Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
		for (int i = 0; i < 10; i++) {
			map.put(String.valueOf(i).getBytes(), String.valueOf(i).getBytes());
		}
		client.hmset(key, map);
		assertArrayEquals(client.hget(key, "0".getBytes()), "0".getBytes());
		assertArrayEquals(client.hget(key, "1".getBytes()), "1".getBytes());
		assertArrayEquals(client.hget(key, "2".getBytes()), "2".getBytes());
		assertArrayEquals(client.hget(key, "3".getBytes()), "3".getBytes());
		assertArrayEquals(client.hget(key, "4".getBytes()), "4".getBytes());
		assertArrayEquals(client.hget(key, "5".getBytes()), "5".getBytes());
		assertArrayEquals(client.hget(key, "6".getBytes()), "6".getBytes());
		assertArrayEquals(client.hget(key, "7".getBytes()), "7".getBytes());
		assertArrayEquals(client.hget(key, "8".getBytes()), "8".getBytes());
		assertArrayEquals(client.hget(key, "9".getBytes()), "9".getBytes());
	}

	@Test
	public void testHkeys() {
		client.del(key);
		Set<String> expect = new HashSet<String>();
		Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
		for (int i = 0; i < 10; i++) {
			byte[] k = String.valueOf(i).getBytes();
			expect.add(new String(k));
			map.put(k, k);
		}
		client.hmset(key, map);

		Set<String> keys = new HashSet<String>();
		for (byte[] k : client.hkeys(key)) {
			keys.add(new String(k));
		}
		assertEquals(expect.size(), keys.size());
		assertTrue(expect.containsAll(keys));
	}

	@Test
	public void testHset() {
		client.del(key);
		client.hset(key, field, value);
		assertArrayEquals(value, client.hget(key, field));
	}

	/*
	public void testHgetAll() {
		client.del(key);
		Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
		for (int i = 0; i < 10; i++) {
			map.put(String.valueOf(i).getBytes(), String.valueOf(i).getBytes());
		}
		client.hmset(key, map);
		Map<byte[], byte[]> all = client.hgetAll(key);
		for (byte[] k : all.keySet()) {
			assertArrayEquals(k, all.get(k));
		}
	}*/

	@Test
	public void testHlen() {
		client.del(key);
		Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
		for (int i = 0; i < 10; i++) {
			map.put(String.valueOf(i).getBytes(), String.valueOf(i).getBytes());
		}
		client.hmset(key, map);
		assertEquals(10, client.hlen(key));
	}

	@Test
	public void testHsetnx() {
		client.del(key);
		assertEquals(1, client.hsetnx(key, field, value));
		assertArrayEquals(value, client.hget(key, field));

		assertEquals(0, client.hsetnx(key, field, "000".getBytes()));
		assertArrayEquals(value, client.hget(key, field));

		assertEquals(0, client.hset(key, field, "111".getBytes()));
		assertArrayEquals("111".getBytes(), client.hget(key, field));
	}

	@Test
	public void testLlen() {
		client.del(key);
		client.lpush(key, "0".getBytes());
		client.lpush(key, "1".getBytes());
		client.lpush(key, "2".getBytes());
		client.lpush(key, "3".getBytes());
		assertEquals(4, client.llen(key));
	}

	@Test
	public void testLpop() {
		client.del(key);
		client.rpush(key, "0".getBytes());
		client.rpush(key, "1".getBytes());
		client.rpush(key, "2".getBytes());
		client.rpush(key, "3".getBytes());

		assertArrayEquals("0".getBytes(), client.lpop(key));
		assertArrayEquals("1".getBytes(), client.lpop(key));
		assertArrayEquals("2".getBytes(), client.lpop(key));
		assertArrayEquals("3".getBytes(), client.lpop(key));
	}

	@Test
	public void testRpop() {
		client.del(key);
		client.rpush(key, "0".getBytes());
		client.rpush(key, "1".getBytes());
		client.rpush(key, "2".getBytes());
		client.rpush(key, "3".getBytes());

		assertArrayEquals("3".getBytes(), client.rpop(key));
		assertArrayEquals("2".getBytes(), client.rpop(key));
		assertArrayEquals("1".getBytes(), client.rpop(key));
		assertArrayEquals("0".getBytes(), client.rpop(key));
	}
	
	@Test
	public void testBlpop() {
		
	}

	@Test
	public void testBrpop() {
		
	}

	@Test
	public void testLpush() {
		client.del(key);
		long n = client.lpush(key, value);
		assertEquals(1, n);

		byte[] rtn = client.lindex(key, 0);
		assertArrayEquals(value, rtn);
	}

	@Test
	public void testRpush() {
		client.del(key);
		long n = client.rpush(key, value);
		assertEquals(1, n);

		byte[] rtn = client.lindex(key, 0);
		assertArrayEquals(value, rtn);
	}

	@Test
	public void testLpushx() {
		client.del(key);
		long rtn = client.lpushx(key, value);
		assertEquals(0, rtn);

		rtn = client.lpush(key, value);
		rtn = client.lpushx(key, value);
		assertEquals(2, rtn);
	}

	@Test
	public void testRpushx() {
		client.del(key);
		long rtn = client.rpushx(key, value);
		assertEquals(0, rtn);

		rtn = client.rpush(key, value);
		rtn = client.rpushx(key, value);
		assertEquals(2, rtn);
	}

	@Test
	public void testLrange() {
		client.del(key);
		client.rpush(key, "0".getBytes());
		client.rpush(key, "1".getBytes());
		client.rpush(key, "2".getBytes());
		byte[][] rtn = client.lrange(key, 0, 10);
		for (int i = 0; i < rtn.length; i++) {
			assertArrayEquals(String.valueOf(i).getBytes(), rtn[i]);
		}
	}

	@Test
	public void testLindex() {
		client.del(key);
		client.lpush(key, "0".getBytes());
		client.lpush(key, "1".getBytes());
		client.lpush(key, "2".getBytes());

		byte[] rtn = client.lindex(key, 1);
		assertArrayEquals("1".getBytes(), rtn);
	}

	@Test
	public void testLset() {
		client.del(key);
		client.lpush(key, "0".getBytes());
		client.lpush(key, "1".getBytes());
		client.lpush(key, "2".getBytes());

		byte[] v = "HELLO".getBytes();
		client.lset(key, 1, v);
		byte[] rtn = client.lindex(key, 1);
		assertArrayEquals(v, rtn);
	}

	@Test
	public void testAuth() {
		// client.auth("123456".getBytes());
	}

	@Test
	public void testEcho() {
		byte[] message = "Hello Redis!".getBytes();
		byte[] rtn = client.echo(message);
		assertArrayEquals(message, rtn);
	}
		
	@Test
	public void testSaddByteByte() {
		client.del(key);
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		byte[][] value = new byte[2][];
		value[0] = "D".getBytes();
		value[1] = "E".getBytes();
		
		client.sadd(key, value);
		
		assertEquals(5, client.scard(key));
		Set<byte[]> rtn = client.smembers(key);

		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		expect.add("B");
		expect.add("C");
		expect.add("D");
		expect.add("E");
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));

	}
	
	@Test
	public void testSmembersByte() {
		client.del(key);
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		assertEquals(3, client.scard(key));
		Set<byte[]> rtn = client.smembers(key);
		
		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		expect.add("B");
		expect.add("C");
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	@Test
	public void testSremByteByte() {
		client.del(key);
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		client.srem(key, "B".getBytes());
		
		Set<byte[]> rtn = client.smembers(key);
		
		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		expect.add("C");
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	@Test
	public void testSpopByte() {
		client.del(key);
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		assertEquals(3, client.scard(key));
		client.spop(key);
		assertEquals(2, client.scard(key));
		client.spop(key);
		assertEquals(1, client.scard(key));
		client.spop(key);
		assertEquals(0, client.scard(key));
	}
	
	//@Test
	public void testSmove() {
		client.del(key);
		client.del("SET2".getBytes());
		
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		client.smove(key, "SET2".getBytes(), "B".getBytes());
		assertEquals(2, client.scard(key));
		assertEquals(1, client.scard("SET2".getBytes()));
	}
	
	@Test
	public void testScard() {
		client.del(key);
		assertEquals(0, client.scard(key));
		
		client.sadd(key, "A".getBytes());
		assertEquals(1, client.scard(key));
		
		client.sadd(key, "B".getBytes());
		assertEquals(2, client.scard(key));
		
		client.sadd(key, "C".getBytes());
		assertEquals(3, client.scard(key));
	}
	
	@Test
	public void testSismember() {
		client.del(key);
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		assertTrue(client.sismember(key, "A".getBytes()));
		assertTrue(client.sismember(key, "B".getBytes()));
		assertTrue(client.sismember(key, "C".getBytes()));
		
		assertFalse(client.sismember(key, "D".getBytes()));
	}
	
	//@Test
	public void testSinter() {
		byte[] key2 = "key2".getBytes();
		
		client.del(key);
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		client.sadd(key2, "B".getBytes());
		client.sadd(key2, "C".getBytes());
		client.sadd(key2, "D".getBytes());
		
		Set<byte[]> rtn = client.sinter(new byte[][] {key, key2});
		assertEquals(2, rtn.size());
		
		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("B");
		expect.add("C");
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	//@Test
	public void testSinterstore() {
		byte[] key2 = "key2".getBytes();
		byte[] key3 = "key3".getBytes();
		
		client.del(key);
		client.del(key2);
		client.del(key3);
		
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		client.sadd(key2, "B".getBytes());
		client.sadd(key2, "C".getBytes());
		client.sadd(key2, "D".getBytes());
		
		client.sinterstore(key3, new byte[][] {key, key2});
		Set<byte[]> rtn = client.smembers(key3);
		
		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("B");
		expect.add("C");
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	//@Test
	public void sunion() {
		byte[] key2 = "key2".getBytes();
		
		client.del(key);
		client.del(key2);
		
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		client.sadd(key2, "B".getBytes());
		client.sadd(key2, "C".getBytes());
		client.sadd(key2, "D".getBytes());
		
		Set<byte[]> rtn = client.sunion(new byte[][] {key, key2});
		assertEquals(4, rtn.size());
		
		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		expect.add("B");
		expect.add("C");
		expect.add("D");
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	//@Test
	public void sunionstore() {
		byte[] key2 = "key2".getBytes();
		byte[] key3 = "key3".getBytes();
		
		client.del(key);
		client.del(key2);
		client.del(key3);
		
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		client.sadd(key2, "B".getBytes());
		client.sadd(key2, "C".getBytes());
		client.sadd(key2, "D".getBytes());
		
		client.sunionstore(key3, new byte[][] {key, key2});
		Set<byte[]> rtn = client.smembers(key3);
		
		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		expect.add("B");
		expect.add("C");
		expect.add("D");
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	//@Test
	public void sdiff() {
		byte[] key2 = "key2".getBytes();
		
		client.del(key);
		client.del(key2);
		
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		client.sadd(key2, "B".getBytes());
		client.sadd(key2, "C".getBytes());
		client.sadd(key2, "D".getBytes());
		
		Set<byte[]> rtn = client.sdiff(new byte[][] {key, key2});
		assertEquals(1, rtn.size());
		
		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	//@Test
	public void sdiffstore() {
		byte[] key2 = "key2".getBytes();
		byte[] key3 = "key3".getBytes();
		
		client.del(key);
		client.del(key2);
		client.del(key3);
		
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		
		client.sadd(key2, "B".getBytes());
		client.sadd(key2, "C".getBytes());
		client.sadd(key2, "D".getBytes());
		
		client.sdiffstore(key3, new byte[][] {key, key2});
		Set<byte[]> rtn = client.smembers(key3);
		
		Set<String> real = new HashSet<String>();
		for (byte[] k : rtn) {
			real.add(new String(k));
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	@Test
	public void srandmember() {
		client.del(key);
		client.sadd(key, "A".getBytes());
		client.sadd(key, "B".getBytes());
		client.sadd(key, "C".getBytes());
		System.out.println("---------------------");
		System.out.println(new String(client.srandmember(key)));
		System.out.println(new String(client.srandmember(key)));
		System.out.println(new String(client.srandmember(key)));
		System.out.println(new String(client.srandmember(key)));
	}

    public static void main(String[] args) {
        System.out.println("中国");
    }
}
