package com.ailk.cache.util.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: AbstractSerializable
 * @description: 序列化与反序列化抽象类
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public abstract class AbstractSerializable implements ISerializable {

	private static final transient Logger log = Logger.getLogger(AbstractSerializable.class);
	
	/**
	 * 对字节数组进行压缩
	 * 
	 * @param obj
	 * @return
	 */
	@Override
	public byte[] encodeGzip(byte[] bytes) {
	
		ByteArrayOutputStream bos = null;
		GZIPOutputStream gos = null;
		
		try {
			bos = new ByteArrayOutputStream(bytes.length);
	        gos = new GZIPOutputStream(bos);
	        gos.write(bytes, 0, bytes.length);
	        gos.finish();
	        return bos.toByteArray();
		} catch (IOException e) {
			
		} finally {
			if (null != gos) {
				try {
					gos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
        return null;
	}

	/**
	 * byte数组解压
	 * 
	 * @param bytes
	 * @return
	 */
	@Override
	public byte[] decodeGzip(byte[] datas) {
		
		GZIPInputStream gis = null;
		ByteArrayOutputStream baos = null;
		
		try {
			gis = new GZIPInputStream(new ByteArrayInputStream(datas));
			baos = new ByteArrayOutputStream(datas.length);

			int cnt = 0;
			byte[] tmp = new byte[datas.length];
			while ((cnt = gis.read(tmp)) != -1) {
				baos.write(tmp, 0, cnt);
			}

			return baos.toByteArray();
			
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (null != gis) {
				try {
					gis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (null != baos) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
}
