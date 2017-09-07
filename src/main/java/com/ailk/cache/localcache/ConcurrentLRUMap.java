package com.ailk.cache.localcache;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: ConcurrentLRUMap
 * @description: 线程安全的LRUMap，采用ConcurrentHashMap机制实现细粒度锁，提高并发度.<br>
 *               最大容量会自动转换成能整除16的最小倍数，默认容量为1024。
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-11-8
 */
public class ConcurrentLRUMap<K, V> implements Serializable {

	private static final long serialVersionUID = -7309303699204841905L;

	/** 默认大小 */
	private static final int DEFAULT_INITIAL_CAPACITY = 1024;

	/** 默认最大分区 */
	private static final int DEFAULT_MAX_SEGMENTS = 16;

	/** 最大容量 */
	private static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * Mask value for indexing into segments. The upper bits of a key's hash
	 * code are used to choose the segment.
	 */
	private final int segmentMask;

	/**
	 * Shift value for indexing within segments.
	 */
	private final int segmentShift;

	private SegmentHashMap<K, V>[] segments;

	/**
	 * 构造函数，默认最大容量为1024，采用LRU机制淘汰数据
	 */
	public ConcurrentLRUMap() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	/**
	 * 构造函数，最大容量会自动转换成16的倍数。
	 * 
	 * @param maxSize
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentLRUMap(int maxSize) {

		if (maxSize < 0) {
			throw new IllegalArgumentException("maxSize must > 0");
		}

		if (0 != maxSize % DEFAULT_MAX_SEGMENTS) {
			maxSize = (maxSize / DEFAULT_MAX_SEGMENTS + 1) * DEFAULT_MAX_SEGMENTS;
		}

		// Find power-of-two sizes best matching arguments
		int sshift = 0;
		int ssize = 1; // 分区大小：2的倍数
		while (ssize < DEFAULT_MAX_SEGMENTS) {
			++sshift;
			ssize <<= 1;
		}

		if (ssize != DEFAULT_MAX_SEGMENTS) {
			throw new IllegalArgumentException("size must be power-of-two!");
		}

		segmentShift = 32 - sshift;
		segmentMask = ssize - 1;
		this.segments = new SegmentHashMap[ssize];

		if (maxSize > MAXIMUM_CAPACITY)
			maxSize = MAXIMUM_CAPACITY;
		int c = maxSize / ssize;
		if (c * ssize != maxSize) {
			throw new IllegalArgumentException("make sure: maxSize / 16 == 0");
		}

		if (c * ssize < maxSize) {
			++c;
		}
		int cap = 1; // 平摊到每个分区Map的size
		while (cap < c) {
			cap <<= 1;
		}

		for (int i = 0; i < this.segments.length; i++) {
			this.segments[i] = new SegmentHashMap<K, V>(cap);
		}
	}

	/**
	 * 获取元素
	 * 
	 * @param key
	 * @return
	 */
	public V get(K key) {
		int hash = hash(key.hashCode());
		return segmentFor(hash).getEntry(key);
	}

	/**
	 * 添加元素
	 * 
	 * @param key
	 * @param value
	 */
	public Object put(K key, V value) {
		if (null == value) {
			throw new NullPointerException("value could not be null!");
		}

		int hash = hash(key.hashCode());
		return segmentFor(hash).addEntry(key, value);
	}

	/**
	 * 删除元素
	 * 
	 * @param key
	 */
	public Object remove(K key) {
		int hash = hash(key.hashCode());
		return segmentFor(hash).remove(key);
	}

	/**
	 * 判断缓存中是否包含该key
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(K key) {
		int hash = hash(key.hashCode());
		return segmentFor(hash).containsKey(key);
	}
	
	/**
	 * 判断缓存是否为空
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public Set<K> keySet() {
		Set<K> rtn = new HashSet<K>(); 
		for (int i = 0; i < this.segments.length; i++) {
			Set<K> set = segments[i].keySet();
			rtn.addAll(set);
		}
		return rtn;
	}
	
	/**
	 * 清空
	 */
	public synchronized void clear() {
		for (int i = 0; i < this.segments.length; i++) {
			segments[i].clear();
		}
	}

	/**
	 * 返回K-V元素个数
	 * 
	 * @return
	 */
	public int size() {
		int sum = 0;
		for (int i = 0; i < this.segments.length; i++) {
			sum += segments[i].size();
		}
		return sum;
	}

	private static final int hash(int h) {
		h += (h << 15) ^ 0xFFFFCD7D;
		h ^= (h >>> 10);
		h += (h << 3);
		h ^= (h >>> 6);
		h += (h << 2) + (h << 14);
		return h ^ (h >>> 16);
	}

	private final SegmentHashMap<K, V> segmentFor(int hash) {
		return segments[(hash >>> segmentShift) & segmentMask];
	}

	private static final class SegmentHashMap<KK, VV> extends LinkedHashMap<KK, VV> {

		private static final long serialVersionUID = 6488943653970934521L;

		private final Lock lock = new ReentrantLock();

		/**
		 * 容量上限
		 */
		private int maxSize;

		/**
		 * 构造函数
		 * 
		 * @param maxSize
		 *            用量上限
		 */
		public SegmentHashMap(int maxSize) {
			super(maxSize);
			this.maxSize = maxSize;
		}

		/**
		 * 存放元素(线程安全)
		 * 
		 * @param key
		 * @param value
		 */
		public VV addEntry(KK key, VV value) {
			lock.lock();
			try {
				VV oldvalue = super.put(key, value);
				return oldvalue;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 获取元素(线程安全)
		 * 
		 * @param key
		 * @return
		 */
		public VV getEntry(KK key) {
			lock.lock();
			try {
				
				VV value = get(key);
				if (null == value) {
					super.remove(key);
					return null;
				}
				
				return value;
				
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 删除元素(线程安全)
		 */
		@Override
		public VV remove(Object key) {
			lock.lock();
			try {
				return super.remove(key);
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 清理(线程安全)
		 */
		@Override
		public void clear() {
			lock.lock();
			try {
				super.clear();
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 判断是否需要淘汰元素
		 */
		@Override
		public boolean removeEldestEntry(Map.Entry<KK, VV> eldest) {
			return size() > maxSize;
		}
	}

}
