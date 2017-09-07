package com.ailk.cache.redis.client;

import com.ailk.cache.redis.RedisFactory;

public class HATester {
	private static RedisClient client = RedisFactory.getRedisClient("sna");
	
	private static byte[] key = "KEY".getBytes();
	private static byte[] field = "FIELD".getBytes();
	private static byte[] value = "VALUE".getBytes();

	public static void main(String[] args) {
		while (true) {
			try {
				System.out.println("----- 开始挂接任务队列 ---->");
				
				byte[][] rtn = client.brpop(1, "QUEUE-LOG-RECORD".getBytes());
				if (null != rtn) {
					System.out.println("-----    返回数据      ------");
					for (byte[] s : rtn) {
						System.out.println("-> " + new String(s));
					}
				}
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
