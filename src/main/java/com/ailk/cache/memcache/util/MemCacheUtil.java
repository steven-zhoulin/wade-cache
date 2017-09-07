package com.ailk.cache.memcache.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import com.ailk.org.apache.commons.lang3.StringUtils;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: MemCacheUtil
 * @description: MemCacheUtil工具类
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-4-23
 */
public final class MemCacheUtil {

	private MemCacheUtil() {
		// 工具类，无需实例化
	}

	public static Map<String, Object> statsSlabs(String address) {
		
		Map<String, Object> rtn = new HashMap<String, Object>();
		
		Socket socket = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		
		try {
			
			String[] part = StringUtils.split(address, ':');
			socket = new Socket(part[0], Integer.parseInt(part[1]));
	
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			out.write("stats slabs\r\n".getBytes());
			out.flush();

			String cmd = readLine(in);
			while (!"END".equals(cmd)) {
				cmd = cmd.substring(5);
				if (cmd.equals("active_slabs")) {
					//rtn.put("", value);
				}
				
				if (cmd.equals("total_malloced")) {
					
				}
				
				String[] items = StringUtils.split(cmd, " ");
				rtn.put(items[1], items[2]);
				cmd = readLine(in);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (null != in)     in.close();
				if (null != out)    out.close();
				if (null != socket) socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return rtn;
	}
	
	public static Map<String, String> statsSettings(String address) {
		
		Map<String, String> rtn = new HashMap<String, String>();
		
		Socket socket = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		
		try {
			String[] part = StringUtils.split(address, ':');
			socket = new Socket(part[0], Integer.parseInt(part[1]));
	
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			out.write("stats settings\r\n".getBytes());
			out.flush();

			String cmd = readLine(in);
			while (!"END".equals(cmd)) {
				String[] items = StringUtils.split(cmd, " ");
				rtn.put(items[1], items[2]);
				cmd = readLine(in);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (null != in)     in.close();
				if (null != out)    out.close();
				if (null != socket) socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return rtn;
	}
	
	/**
	 * 缓存信息收集
	 * 
	 * @param cacheName
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> stats(String address) {

		Map<String, String> rtn = new HashMap<String, String>();
		
		Socket socket = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		
		try {
			String[] part = StringUtils.split(address, ':');
			socket = new Socket(part[0], Integer.parseInt(part[1]));
	
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			out.write("stats\r\n".getBytes());
			out.flush();
	
			String cmd = readLine(in);
			while (!"END".equals(cmd)) {
				String[] items = StringUtils.split(cmd, " ");
				rtn.put(items[1], items[2]);
				cmd = readLine(in);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (null != in)     in.close();
				if (null != out)    out.close();
				if (null != socket) socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return rtn;
	}

	private static String readLine(BufferedInputStream in) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		boolean eol = false;
		byte[] b = new byte[1];
		while (in.read(b, 0, 1) != -1) {
			if (13 == b[0]) {
				eol = true;
				continue;
			} else {
				if ((eol) && (10 == b[0])) {
					break;
				}
				eol = false;
			}
			baos.write(b, 0, 1);
		}

		if (null == baos || baos.size() <= 0) {
			return null;
		}

		return new String(baos.toByteArray());
	}

	public static void main(String[] args) {
		Map<String, String> info = stats("192.168.102.254:11211");
		System.out.println(info);
		info = statsSettings("192.168.102.254:11211");
		System.out.println(info);
	}
	
}
