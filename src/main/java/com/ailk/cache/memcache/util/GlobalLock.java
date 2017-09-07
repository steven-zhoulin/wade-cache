package com.ailk.cache.memcache.util;

import org.apache.log4j.Logger;

import com.ailk.cache.memcache.MemCacheFactory;
import com.ailk.cache.memcache.interfaces.IMemCache;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: GlobalLock
 * @description: 全局锁
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-5-27
 */
public final class GlobalLock {

	private static final transient Logger log = Logger.getLogger(GlobalLock.class);
	private static final int DEF_SEC_TTL = 120;
	private static final IMemCache cache = MemCacheFactory.getCache(MemCacheFactory.BCC_CACHE);
	
	/**
	 * 加全局锁，此加锁过程为原子操作，针对同一key，最多只有一个客户端能加锁成功。
	 * 
	 * 注: 此全局锁默认120秒超时，自动释放
	 * 
	 * @param key
	 * @return
	 */
	public static final boolean lock(final String key) {
		return lock(key, DEF_SEC_TTL);
	}
	
	/**
	 * 加全局锁，此加锁过程为原子操作，针对同一key，最多只有一个客户端能加锁成功。
	 * 
	 * @param key 全局锁名
	 * @param secTTL 全局锁生存期，超过此时间，全局锁将自动释放
	 * @return 加锁成功返回true，加锁失败返回false
	 */
    public static final boolean lock(final String key, int secTTL) {
    	boolean b = cache.add(key, 0L, secTTL);
    	if (log.isDebugEnabled()) {
    		log.debug("global lock " + (b ? "success" : "false") + " KEY[" + key + "]");
    	}
    	return b;
    }
    
    /**
     * 阻塞式加全局锁，可设置阻塞时间
     * 
     * @param key
     * @param secWait 阻塞时间
     * @return
     */
    public static final boolean lockWait(final String key, int secWait) {
    	return lockWait(key, DEF_SEC_TTL, secWait);
    }
    
    /**
     * 阻塞式加全局锁，可设置阻塞时间
     * 
     * @param key
     * @param secTTL 全局锁自动超时时间
     * @param secWait 阻塞时间
     * @return
     */
    public static final boolean lockWait(final String key, int secTTL, int secWait) {

        int msecWait = secWait * 1000;
        int msecSleep = 0;
        
        try {
	        while (true) {	            
	            
	        	boolean isSuccess = cache.add(key, 0L, secTTL);
	            if (isSuccess) {
	            	return true; // 加锁成功！
	            }

	            // 加锁失败，休眠20毫秒
	            Thread.sleep(20);
	            
	            msecSleep += 20;
	            if (msecSleep > msecWait) {
	            	return false; // 加锁等待超时，返回false
	            }
	            
	        }
        } catch (InterruptedException e) {
        	log.error("GlobalLock.lockWait加锁失败！", e);
		}
        
        return false; // 加锁异常，返回false
    }
    
    /**
     * 释放全局锁
     * 
     * @param key 全局锁名
     * @return 如果原先有全局锁，返回true; 如果以前没有全局锁，返回false
     */
    public static final boolean unlock(final String key) {
    	boolean b = cache.delete(key);
    	if (log.isDebugEnabled()) {
    		log.debug("global unlock KEY[" + key + "]");
    	}
    	return b;
    }
    
    /**
     * 延迟释放全局锁
     * 
     * @param key 全局锁名
     * @param secTTL 延迟秒数
     * @return 如果原先有全局锁，返回true; 如果以前没有全局锁，返回false
     */
    public static final boolean unlock(final String key, int secTTL) {
    	cache.touch(key, secTTL);
    	if (log.isDebugEnabled()) {
    		log.debug("global unlock KEY[" + key + "]");
    	}
    	return true;
    }
}