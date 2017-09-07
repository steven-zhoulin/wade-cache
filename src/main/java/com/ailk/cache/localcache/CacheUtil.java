package com.ailk.cache.localcache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: CacheUtil
 * @description: Cache工具类
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public final class CacheUtil {
	
	private static final Logger log = Logger.getLogger(CacheUtil.class);
	
	private CacheUtil() {
		// 工具类，无需实例化 
	}
	
	/**
	 * 计算对象所占内存大小
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static final int objectSize(Object obj) {
		int size = 0;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);

			byte[] bytes = bos.toByteArray();
			size = bytes.length;
		} catch (IOException e) {
			log.error("计算对象所占内存大小发生错误！");
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				log.error("关闭ObjectOutputStream时发生错误！");
			}
		}

		return size;
	}
}
