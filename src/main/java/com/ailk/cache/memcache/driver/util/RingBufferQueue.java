package com.ailk.cache.memcache.driver.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: RingBufferQueue
 * @description: 环形并发队列
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-8-18
 */
public class RingBufferQueue<E> {
	
	private final int mask;
	private final E[] buffer;

	private final AtomicLong tail = new AtomicLong(0);
	private final AtomicLong head = new AtomicLong(0);

	@SuppressWarnings("unchecked")
	public RingBufferQueue(int capacity) {
		capacity = findNextPositivePowerOfTwo(capacity);
		mask = capacity - 1;
		buffer = (E[]) new Object[capacity];
	}

	public static int findNextPositivePowerOfTwo(final int value) {
		return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
	}

	public boolean add(final E e) {
		if (offer(e)) {
			return true;
		}

		throw new IllegalStateException("Queue is full");
	}

	public boolean offer(final E e) {
		if (null == e) {
			throw new NullPointerException("Null is not a valid element");
		}

		final long currentTail = tail.get();
		final long wrapPoint = currentTail - buffer.length;
		if (head.get() <= wrapPoint) {
			return false;
		}

		buffer[(int) currentTail & mask] = e;
		tail.lazySet(currentTail + 1);

		return true;
	}

	public E poll() {
		final long currentHead = head.get();
		if (currentHead >= tail.get()) {
			return null;
		}

		final int index = (int) currentHead & mask;
		final E e = buffer[index];
		buffer[index] = null;
		head.lazySet(currentHead + 1);

		return e;
	}
}
