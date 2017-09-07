package com.ailk.cache.memcache;

import java.io.UnsupportedEncodingException;

import com.ailk.cache.memcache.interfaces.IMemCache;

public class AppendTest {
	
	private static final String KEY = "SUPERUSR";
	private static final byte[] BYTES = "[2013-07-20 15:07:31,792] (BindingStatement.java:99) DEBUG - SQL SELECT eparchy_code,moffice_id,switch_id,serialnumber_s,serialnumber_e,imsi_s,imsi_e,to_char(update_time,'yyyy-mm-dd hh24:mi:ss') update_time,update_staff_id,update_depart_id[2013-07-20 15:07:31,792] (BindingStatement.java:99) DEBUG - SQL SELECT eparchy_code,moffice_id,switch_id,serialnumber_s,serialnumber_e,imsi_s,imsi_e,to_char(update_time,'yyyy-mm-dd hh24:mi:ss') update_time,update_staff_id,update_depart_id[2013-07-20 15:07:31,792] (BindingStatement.java:99) DEBUG - SQL SELECT eparchy_code,moffice_id,switch_id,serialnumber_s,serialnumber_e,imsi_s,imsi_e,to_char(update_time,'yyyy-mm-dd hh24:mi:ss') update_time,update_staff_id,update_depart_id[2013-07-20 15:07:31,792] (BindingStatement.java:99) DEBUG - SQL SELECT eparchy_code,moffice_id,switch_id,serialnumber_s,serialnumber_e,imsi_s,imsi_e,to_char(update_time,'yyyy-mm-dd hh24:mi:ss') update_time,update_staff_id,update_depart_id".getBytes();
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		
		IMemCache cache = MemCacheFactory.getCache("session_data_cache");
		cache.set(KEY, "".getBytes());
		
		System.out.println("go...");
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1024; i++) {
			boolean b = cache.append(KEY, BYTES);
			System.out.println(b);
		}
		System.out.println("耗时:" + (System.currentTimeMillis() - start) + "毫秒");
		/*
		byte[] value = (byte[])cache.get(KEY);
		System.out.println(value.length);
		System.out.println("数据: -------------------" + value.length);
		System.out.println(new String(value));
		*/
	}
	
}
