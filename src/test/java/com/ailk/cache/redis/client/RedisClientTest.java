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
import com.ailk.cache.redis.client.RedisClient;

public class RedisClientTest {

	private static RedisClient client = RedisFactory.getRedisClient("sna");
	private static String key = "KEY";
	private static String field = "FIELD";
	private static String value = "VALUE";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDel() {
		client.del(key);
		boolean b = client.set(key, value);
		String rtn = client.get(key);
		assertEquals(value, rtn);
		
		assertEquals(1, client.del(key));
		assertNull(value, client.get(rtn));
	}

	
	public void testKeys() {
		client.del(key);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("Q-PREFIX-1", "11");
		map.put("Q-PREFIX-2", "22");
		map.put("Q-PREFIX-3", "33");
		for (String k : map.keySet()) {
			client.set(k, map.get(k));
		}
		
		Set<String> keys = client.keys("Q-PREFIX-*");
		assertTrue(keys.containsAll(map.keySet()));
		assertTrue(map.keySet().containsAll(keys));
	}

	@Test
	public void testExists() {
		System.out.println("testExists------------------------------");
		
		client.set("123", "456");
		assertTrue(client.exists("123"));
		client.del("123");
		assertFalse(client.exists("123"));
	}
	
	@Test
	public void testExpire() throws InterruptedException {
		System.out.println("");
		client.set("123", "456");
		client.expire("123", 2);
		assertTrue(client.exists("123"));
		Thread.sleep(3000);
		assertFalse(client.exists("123"));
	}
	
	@Test
	public void testAppend() {
		client.del(key);
		for (int i = 0; i < 10; i++) {
			client.append(key, String.valueOf(i));
		}
		assertEquals("0123456789", client.get(key));
	}

	@Test
	public void testSet() {
		client.del(key);
		client.set(key, value);
		assertEquals(value, client.get(key));
	}

	@Test
	public void testSetObject() {
		client.del(key);
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("11", "11");
		map.put("22", "22");
		map.put("33", "33");
		client.set(key, map);
		HashMap<String, String> rtn = (HashMap<String, String>)client.getObject(key);
		assertEquals("11", rtn.get("11"));
		assertEquals("22", rtn.get("22"));
		assertEquals("33", rtn.get("33"));
	}
	
	@Test
	public void testSetObjectTTL() throws InterruptedException {
		client.del(key);
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("11", "11");
		map.put("22", "22");
		map.put("33", "33");
		client.set(key, map, 2);
		HashMap<String, String> rtn = (HashMap<String, String>)client.getObject(key);
		assertEquals("11", rtn.get("11"));
		assertEquals("22", rtn.get("22"));
		assertEquals("33", rtn.get("33"));
		Thread.sleep(3000);
		rtn = (HashMap<String, String>)client.getObject(key);
		assertNull(rtn);
	}
	
	@Test
	public void testSetStringStringLong() throws InterruptedException {
		client.del(key);
		client.set(key, value, 1);
		Thread.sleep(1200);
		assertNull(client.get(key));
	}

	@Test
	public void testGetString() {
		client.del(key);
		client.set(key, value);
		assertEquals(value, client.get(key));
		
		client.set("AAA", "111");
		assertEquals("111", client.get("AAA"));
		
		client.set("BBB", "222");
		assertEquals("222", client.get("BBB"));
		
		client.set("CCC", "333");
		assertEquals("333", client.get("CCC"));
	}

	@Test
	public void testStrlenString() {
		client.del(key);
		client.set(key, value);
		assertEquals(value.length(), client.strlen(key));
	}

	@Test
	public void testIncrString() {
		client.del(key);
		long curr = 0;
		for (int i = 0; i < 1000; i++) {
			curr = client.incr(key);
		}
		assertEquals(1000, curr);
	}

	@Test
	public void testIncrbyStringInt() {
		client.del(key);
		long curr = 0;
		for (int i = 0; i < 1000; i++) {
			curr = client.incrby(key, 2);
		}
		assertEquals(2000, curr);
	}

	@Test
	public void testDecrString() {
		client.del(key);
		long curr = 0;
		for (int i = 0; i < 1000; i++) {
			curr = client.incrby(key, 2);
			curr = client.decr(key);
		}
		assertEquals(1000, curr);
	}

	@Test
	public void testDecrbyStringInt() {
		client.del(key);
		long curr = 0;
		for (int i = 0; i < 1000; i++) {
			curr = client.incrby(key, 3);
			curr = client.decrby(key, 2);
		}
		assertEquals(1000, curr);
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
		System.out.println("+-------------------------------------+");
		System.out.println("+-------------------------------------+");
	}
	
	@Test
	public void testBitcount() {
		client.del(key);
		client.setbit(key, 0, true);
		client.setbit(key, 1, true);
		client.setbit(key, 100, true);
		assertTrue(2 == client.bitcount(key, 0, 0));
		assertTrue(0 == client.bitcount(key, 1, 1));
		System.out.println("+-------------------------------------+");
		byte[] rtn = client.get(key.getBytes());
		System.out.println(toFullBinaryString(rtn));
		System.out.println("+-------------------------------------+");
	}
	
    private static String toFullBinaryString(byte[] bytes) {
    	
    	StringBuilder sb = new StringBuilder(2048);
    	for (byte b : bytes) {
    		int[] buff = new int[8];
    		for (int i = 7; i >= 0; i--) {
    			buff[i] = b >> i & 1;
    		}
    		
    		for (int i = 7; i >= 0; i--) {
    			sb.append(buff[i]);
    		}
    	}
    	return sb.toString();
    }
	
	/*
	public void testHdelStringStringArray() {
		client.del(key);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("A", "1");
		map.put("B", "2");
		map.put("C", "3");
		client.hmset(key, map);
		client.hdel(key, "A", "B");
		map = client.hgetAll(key);
		assertEquals(1, map.size());
	}*/

	@Test
	public void testHincrbyStringStringLong() {
		client.del(key);
		long curr = 0;
		for (int i = 0; i < 1000; i++) {
			curr = client.hincrby(key, field, 2);
		}
		assertEquals(2000, curr);
	}

	/*
	public void testHmsetStringMapOfStringString() {
		client.del(key);
		Map<String, String> map = new HashMap<String, String>();
		map.put("A", "1");
		map.put("B", "2");
		map.put("C", "3");
		client.hmset(key, map);

		Map<String, String> rtn = client.hgetAll(key);
		assertEquals(map.size(), rtn.size());
		assertTrue(rtn.keySet().containsAll(map.keySet()));
		assertTrue(map.keySet().containsAll(rtn.keySet()));
	}*/

	@Test
	public void testHmgetStringStringArray() {
		client.del(key);
		Map<String, String> map = new HashMap<String, String>();
		map.put("A", "1");
		map.put("B", "2");
		map.put("C", "3");
		client.hmset(key, map);
		
		Map<String, String> rtn = client.hmget(key, "A", "B");
		assertEquals("1", rtn.get("A"));
		assertEquals("2", rtn.get("B"));
	}

	@Test
	public void testHvalsString() {
		client.del(key);
		Map<String, String> map = new HashMap<String, String>();
		map.put("A", "1");
		map.put("B", "2");
		map.put("C", "3");
		client.hmset(key, map);
		Set<String> rtn = client.hvals(key);
		assertTrue(rtn.containsAll(map.values()));
		assertTrue(map.values().containsAll(rtn));
	}

	@Test
	public void testHexistsStringString() {
		client.del(key);
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < 10; i++) {
			map.put(String.valueOf(i), String.valueOf(i + 100));
		}
		client.hmset(key, map);
		assertTrue(client.hexists(key, "0"));
		assertTrue(client.hexists(key, "1"));
		assertTrue(client.hexists(key, "2"));
		assertTrue(client.hexists(key, "3"));
		assertTrue(client.hexists(key, "4"));
		assertTrue(client.hexists(key, "5"));
		assertTrue(client.hexists(key, "6"));
		assertTrue(client.hexists(key, "7"));
		assertTrue(client.hexists(key, "8"));
		assertTrue(client.hexists(key, "9"));
		assertFalse(client.hexists(key, "A"));
	}

	@Test
	public void testHgetStringString() {
		client.del(key);
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < 10; i++) {
			map.put(String.valueOf(i), String.valueOf(i));
		}
		client.hmset(key, map);
		assertEquals(client.hget(key, "0"), "0");
		assertEquals(client.hget(key, "1"), "1");
		assertEquals(client.hget(key, "2"), "2");
		assertEquals(client.hget(key, "3"), "3");
		assertEquals(client.hget(key, "4"), "4");
		assertEquals(client.hget(key, "5"), "5");
		assertEquals(client.hget(key, "6"), "6");
		assertEquals(client.hget(key, "7"), "7");
		assertEquals(client.hget(key, "8"), "8");
		assertEquals(client.hget(key, "9"), "9");
		
	}

	@Test
	public void testHkeysString() {
		client.del(key);
		Set<String> expect = new HashSet<String>();
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < 10; i++) {
			expect.add(String.valueOf(i));
			map.put(String.valueOf(i), String.valueOf(i));
		}
		client.hmset(key, map);

		Set<String> keys = new HashSet<String>();
		for (String k : client.hkeys(key)) {
			keys.add(k);
		}
		assertEquals(expect.size(), keys.size());
		assertTrue(expect.containsAll(keys));
	}

	@Test
	public void testHsetStringStringString() {
		client.del(key);
		client.hset(key, field, value);
		assertEquals(value, client.hget(key, field));
	}

	@Test
	public void testHgetAllString() {
		client.del(key);
		client.hset(key, field, value);
		assertEquals(value, client.hget(key, field));
	}

	@Test
	public void testHlenString() {
		client.del(key);
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < 10; i++) {
			map.put(String.valueOf(i), String.valueOf(i));
		}
		client.hmset(key, map);
		assertEquals(10, client.hlen(key));
	}

	@Test
	public void testHsetnxStringStringString() {
		client.del(key);
		assertEquals(1, client.hsetnx(key, field, value));
		assertEquals(value, client.hget(key, field));

		assertEquals(0, client.hsetnx(key, field, "000"));
		assertEquals(value, client.hget(key, field));

		assertEquals(0, client.hset(key, field, "111"));
		assertEquals("111", client.hget(key, field));
	}

	@Test
	public void testLlenString() {
		client.del(key);
		client.lpush(key, "0");
		client.lpush(key, "1");
		client.lpush(key, "2");
		client.lpush(key, "3");
		assertEquals(4, client.llen(key));
	}

	@Test
	public void testLpopString() {
		client.del(key);
		client.rpush(key, "0");
		client.rpush(key, "1");
		client.rpush(key, "2");
		client.rpush(key, "3");

		assertEquals("0", client.lpop(key));
		assertEquals("1", client.lpop(key));
		assertEquals("2", client.lpop(key));
		assertEquals("3", client.lpop(key));
	}

	@Test
	public void testRpopString() {
		client.del(key);
		client.rpush(key, "0");
		client.rpush(key, "1");
		client.rpush(key, "2");
		client.rpush(key, "3");

		assertEquals("3", client.rpop(key));
		assertEquals("2", client.rpop(key));
		assertEquals("1", client.rpop(key));
		assertEquals("0", client.rpop(key));
	}

	@Test
	public void testBlpopLongStringArray() {
		
	}

	@Test
	public void testBrpopLongStringArray() {
		
	}

	@Test
	public void testLpushStringStringArray() {
		client.del(key);
		long n = client.lpush(key, value);
		assertEquals(1, n);

		String rtn = client.lindex(key, 0);
		assertEquals(value, rtn);
	}

	@Test
	public void testRpushStringStringArray() {
		client.del(key);
		long n = client.rpush(key, value);
		assertEquals(1, n);

		String rtn = client.lindex(key, 0);
		assertEquals(value, rtn);
	}

	@Test
	public void testLpushxStringString() {
		client.del(key);
		long rtn = client.lpushx(key, value);
		assertEquals(0, rtn);

		rtn = client.lpush(key, value);
		rtn = client.lpushx(key, value);
		assertEquals(2, rtn);
	}

	@Test
	public void testRpushxStringString() {
		client.del(key);
		long rtn = client.rpushx(key, value);
		assertEquals(0, rtn);

		rtn = client.rpush(key, value);
		rtn = client.rpushx(key, value);
		assertEquals(2, rtn);
	}

	@Test
	public void testLrangeStringIntInt() {
		client.del(key);
		client.rpush(key, "0");
		client.rpush(key, "1");
		client.rpush(key, "2");
		String[] rtn = client.lrange(key, 0, 10);
		for (int i = 0; i < rtn.length; i++) {
			assertEquals(String.valueOf(i), rtn[i]);
		}
	}

	@Test
	public void testLindexStringInt() {
		client.del(key);
		client.lpush(key, "0");
		client.lpush(key, "1");
		client.lpush(key, "2");

		String rtn = client.lindex(key, 1);
		assertEquals("1", rtn);	
	}

	@Test
	public void testLsetStringIntString() {
		client.del(key);
		client.lpush(key, "0");
		client.lpush(key, "1");
		client.lpush(key, "2");

		String v = "HELLO";
		client.lset(key, 1, v);
		String rtn = client.lindex(key, 1);
		assertEquals(v, rtn);
	}

	@Test
	public void reconnect() throws InterruptedException {
		
		//client.del(key);
		//for (int i = 0; i < 1000000000; i++) {
		//	client.append(key, "1");
		//	Thread.sleep(300);
		//	System.out.println("append....");
		//}
	}
	
	@Test
	public void sadd() {
		client.del(key);
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		assertEquals(3, client.scard(key));
		Set<String> rtn = client.smembers(key);

		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		expect.add("B");
		expect.add("C");
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	@Test
	public void smembers() {
		client.del(key);
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		assertEquals(3, client.scard(key));
		Set<String> rtn = client.smembers(key);
		
		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		expect.add("B");
		expect.add("C");
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	@Test
	public void srem() {
		client.del(key);
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		client.srem(key, "B");
		
		Set<String> rtn = client.smembers(key);
		
		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		expect.add("C");
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	@Test
	public void spop() {
		client.del(key);
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		
		assertEquals(3, client.scard(key));
		client.spop(key);
		assertEquals(2, client.scard(key));
		client.spop(key);
		assertEquals(1, client.scard(key));
		client.spop(key);
		assertEquals(0, client.scard(key));
	}
	
	//@Test
	public void smove() {
		client.del(key);
		client.del("SET2");
		
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		
		client.smove(key, "SET2", "B");
		assertEquals(2, client.scard(key));
		assertEquals(1, client.scard("SET2"));
	}
	
	@Test
	public void scard() {
		client.del(key);
		assertEquals(0, client.scard(key));
		
		client.sadd(key, "A");
		assertEquals(1, client.scard(key));
		
		client.sadd(key, "B");
		assertEquals(2, client.scard(key));
		
		client.sadd(key, "C");
		assertEquals(3, client.scard(key));
	}
	
	@Test
	public void sismember() {
		client.del(key);
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		assertTrue(client.sismember(key, "A"));
		assertTrue(client.sismember(key, "B"));
		assertTrue(client.sismember(key, "C"));
		
		assertFalse(client.sismember(key, "D"));
	}
	
	//@Test
	public void sinter() {
		
		client.del(key);
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		
		client.sadd("key2", "B");
		client.sadd("key2", "C");
		client.sadd("key2", "D");
		
		Set<String> rtn = client.sinter(new String[] {key, "key2"});
		assertEquals(2, rtn.size());
		
		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("B");
		expect.add("C");
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	//@Test
	public void sinterstore() {
		String key2 = "key2";
		String key3 = "key3";
		
		client.del(key);
		client.del(key2);
		client.del(key3);
		
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		
		client.sadd(key2, "B");
		client.sadd(key2, "C");
		client.sadd(key2, "D");
		
		client.sinterstore(key3, new String[] {key, key2});
		Set<String> rtn = client.smembers(key3);
		
		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("B");
		expect.add("C");
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	//@Test
	public void sunion() {
		String key2 = "key2";
		
		client.del(key);
		client.del(key2);
		
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		
		client.sadd(key2, "B");
		client.sadd(key2, "C");
		client.sadd(key2, "D");
		
		Set<String> rtn = client.sunion(new String[] {key, key2});
		assertEquals(4, rtn.size());
		
		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
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
		String key2 = "key2";
		String key3 = "key3";
		
		client.del(key);
		client.del(key2);
		client.del(key3);
		
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		
		client.sadd(key2, "B");
		client.sadd(key2, "C");
		client.sadd(key2, "D");
		
		client.sunionstore(key3, new String[] {key, key2});
		Set<String> rtn = client.smembers(key3);
		
		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
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
		String key2 = "key2";
		
		client.del(key);
		client.del(key2);
		
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		
		client.sadd(key2, "B");
		client.sadd(key2, "C");
		client.sadd(key2, "D");
		
		Set<String> rtn = client.sdiff(new String[] {key, key2});
		assertEquals(1, rtn.size());
		
		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
		}
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	//@Test
	public void sdiffstore() {
		String key2 = "key2";
		String key3 = "key3";
		
		client.del(key);
		client.del(key2);
		client.del(key3);
		
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		
		client.sadd(key2, "B");
		client.sadd(key2, "C");
		client.sadd(key2, "D");
		
		client.sdiffstore(key3, new String[] {key, key2});
		Set<String> rtn = client.smembers(key3);
		
		Set<String> real = new HashSet<String>();
		for (String k : rtn) {
			real.add(k);
		}
		
		Set<String> expect = new HashSet<String>();
		expect.add("A");
		
		assertTrue(expect.containsAll(real));
		assertTrue(real.containsAll(expect));
	}
	
	@Test
	public void srandmember() {
		client.del(key);
		client.sadd(key, "A");
		client.sadd(key, "B");
		client.sadd(key, "C");
		System.out.println("---------------------");
		System.out.println(client.srandmember(key));
		System.out.println(client.srandmember(key));
		System.out.println(client.srandmember(key));
		System.out.println(client.srandmember(key));
	}

}
