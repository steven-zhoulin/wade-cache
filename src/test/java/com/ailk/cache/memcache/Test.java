package com.ailk.cache.memcache;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.ailk.cache.memcache.MemCacheFactory;
import com.ailk.cache.memcache.interfaces.IMemCache;

public class Test {
	
	/**
	 * 10行记录，每行记录8个字段，平均：0.150ms一次
	 * 100行记录，每行记录8个字段，平均：0.526ms一次
	 * 
	 * 性能评测结果：效率取决于对象序列化性能，对象越大，开销越大。
	 * 
	 * @param client
	 * @param count
	 */
	public static void testGetList(IMemCache client, long count) {
		
		//System.out.println("\n------- 复杂对象 -------");
		String key = "123";
		List<Map<String, String>> value = new ArrayList<Map<String, String>>();
		for (int i = 0; i < 100; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("1111111111111", "asdfasdfasdf");
			map.put("2222222222222", "sdfgsdfgsdfg");
			map.put("3333333333333", "dfhdfhgdfhdf");
			map.put("4444444444444", "wertwertwret");
			map.put("5555555555555", "wcewecwecw");
			map.put("6666666666666", "vvtrrtrtvrtvtrv");
			map.put("7777777777777", "myumyumuym");
			map.put("8888888888888", ",uk,uk,uk,uk,uk,");
			value.add(map);
		}
		client.set(key, value);
		client.get(key);

		long start = System.nanoTime();
		for (long i = 0; i < count; i++) {
			value = (List<Map<String, String>>)client.get(key);
		}
		//System.out.println(value);
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%f %s\n", count, tms, avg, 1000 / avg, (value.size() == 100 ? "OK" : "FAIL"));
		
	}
	
	/**
	 * 10000次递增，平均：0.044ms一次
	 * 
	 * 性能评测结果：效率主要取决于网络开销
	 * 
	 * @param client
	 * @param count
	 */
	public static void testIncr(IMemCache client, int count) {
		
		System.out.println("\n------- incr -------");
		String key = "12345";
		client.delete(key);
		long start = System.nanoTime();
		long curr = 0;
		for (int i = 0; i < count; i++) {
			curr = client.incrWithTTL(key, 3600);
			System.out.println(curr);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%f\n", count, tms, avg, 1000 / avg);
		//System.out.println(curr);
	}
	
	/**
	 * 10000次递减，平均：0.048ms一次
	 * 
	 * 性能评测结果：效率主要取决于网络开销
	 * 
	 * @param client
	 * @param count
	 */
	public static void testDecr(IMemCache client, int count) {
		String key = "12345";
		long start = System.nanoTime();
		long curr = 0;
		for (int i = 0; i < count; i++) {
			curr = client.decr(key);
			System.out.println(curr);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%f\n", count, tms, avg, 1000 / avg);
		System.out.println(curr);
	}
	
	public static void testTouch(IMemCache client, int count) {
		String key = "123";
		client.set(key, "A", 60);
		long start = System.nanoTime();
		boolean b = false;
		for (int i = 0; i < count; i++) {
			b = client.touch(key, 10);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%f\n", count, tms, avg, 1000 / avg);
		System.out.println(b);
	}
	
	public static void testDelete(IMemCache client, int count) {
		String key = "123", value = "ABC";
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		System.out.println(client.delete(key));
		System.out.println(client.get(key));
	}
		
	public static void testGetChar(IMemCache client, int count) {
		String key = "123";
		char value = 'A';
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println(rtn);
	}
	
	public static void testGetByte(IMemCache client, int count) {
		String key = "123";
		byte value = 'A';
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println(rtn);
	}
	
	public static void testGetInteger(IMemCache client, int count) {
		String key = "123";
		int value = 1982;
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println(rtn);
	}
	
	public static void testGetLong(IMemCache client, int count) {
		String key = "123";
		long value = 19820725;
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println(rtn);
	}
	
	public static void testGetBoolean(IMemCache client, long count) {
		
		System.out.println("\n------- Boolean -------");
		String key = "123456789Z";
		Boolean value = true;
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (long i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println("返回值" + rtn);
	}
	
	public static void testGetShort(IMemCache client, int count) {
		
		System.out.println("\n------- Short -------");
		String key = "123";
		short value = 321;
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println("返回值" + rtn);
	}
	
	public static void testGetFloat(IMemCache client, int count) {
		
		System.out.println("\n------- Float -------");
		String key = "123";
		float value = 123.321f;
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println("返回值" + rtn);
	}
	
	public static void testGetDouble(IMemCache client, int count) {
		
		System.out.println("\n------- Double -------");
		String key = "123";
		double value = 123.987654321;
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println("返回值" + rtn);
	}
	
	public static void testGetStringBuffer(IMemCache client, int count) {
		
		System.out.println("\n------- StringBuffer -------");
		String key = "123";
		StringBuffer value = new StringBuffer("steven-zhoulin");
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println("返回值" + rtn);
	}
	
	public static void testGetStringBuilder(IMemCache client, int count) {
		
		System.out.println("\n------- StringBuilder -------");
		String key = "123";
		StringBuilder value = new StringBuilder("steven-zhoulin");
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println("返回值" + rtn);
	}

	/**
	 * value = "1": 
	 *      本机测试，平均0.052ms一次
	 *      
	 * value = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz"：
	 *      本机测试，平均0.052ms一次
	 *  
	 *  性能评测结果：
	 *   	ByteArrayOutputStream.toString()性能开销最大。
	 *   
	 * @param client
	 * @param count
	 */
	public static void testGetString(IMemCache client, long count) {
		
		//System.out.println("\n------- String -------");
		String key = "123", value = "CACHE VALUE";
		boolean setSuccess = client.set(key, value);
		//System.out.println("setSuccess=" + setSuccess);
		long start = System.nanoTime();
		Object rtn = null;
		for (long i = 0; i < count; i++) {
			rtn  = client.get(key);
			//System.out.println(rtn);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f \n", count, tms, avg, 1000 / avg);
		//System.out.println(rtn);
	}

	public static void testGetDate(IMemCache client, int count) {
		
		System.out.println("\n------- Date -------");
		String key = "123";
		Date value = new Date();
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		Object rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println("返回值" + rtn);
	} 
	
	public static void testGetByteArr(IMemCache client, int count) {
		
		System.out.println("\n------- byte[] -------");
		String key = "123";
		byte[] value = new byte[] {'A', 'B', 'C', 'D', 'E'};
		System.out.println(client.set(key, value));
		System.out.println(client.get(key));
		
		long start = System.nanoTime();
		byte[] rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = (byte[])client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println("返回值" + (char)rtn[0] + (char)rtn[1] + (char)rtn[2] + (char)rtn[3] + (char)rtn[4]);
	} 
	
	public static void testCompress(IMemCache client, int count) {
		
		System.out.println("\n------- 大对象压缩测试 -------");
		String key = "123";
		StringBuilder value = new StringBuilder(1000000);
		for (int i = 0; i < 100000; i++) {
			value.append("123456789" + i);
		}
		value.append("Z");
		System.out.println(client.set(key, value));
		
		long start = System.nanoTime();
		StringBuilder rtn = null;
		for (int i = 0; i < count; i++) {
			rtn  = (StringBuilder)client.get(key);
		}
		double tms = (System.nanoTime() - start) * 1.0 / 1000000;
		double avg = tms * 1.0 / count;
		System.out.printf("总共处理%d次，总耗时：%.3fms，平均：%.3fms一次，QPS：%.3f\n", count, tms, avg, 1000 / avg);
		System.out.println(rtn.length());
	} 
		
	public static void main(String[] args) throws Exception {

		IMemCache client = MemCacheFactory.getCache("shc_cache");	
		
		/**
		 * GET
		 */
		while (true) {
			//client.get("123");
			Thread.sleep(1000 * 1000);
		}
		//testGetBoolean(client, 1L);
		//testGetByte(client, 10000);
		//testGetChar(client, 10000);
		//testGetShort(client, 10000);
		//testGetInteger(client, 10000);
		//testGetLong(client, 10000);
		//testGetFloat(client, 10000);
		//testGetDouble(client, 10000);
		//testGetDate(client, 10000);
		//testGetString(client, 10000);
		//testGetStringBuilder(client, 10000);
		//testGetStringBuffer(client, 10000);
		//testGetByteArr(client, 10000);
		
		/**
		 * 复杂对象测试 
		 */
		//while (true)
		//testGetList(client, 10000);
		
		/**
		 * 大对象，压缩测试
		 */
		//testCompress(client, 1);
		//testCompress(client, 10);
		//testIncr(client, 10000);
		//testDecr(client, 10000);
		//testTouch(client, 1);
		//testDelete(client, 1);	
	}
}