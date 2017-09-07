package com.ailk.cache.memcache;

public class InstanceofPerformance {

	private static final byte[] bytes = "123".getBytes();
	private static final StringBuffer s1 = new StringBuffer("12345");
	private static final StringBuilder s2 = new StringBuilder("12345");

	/*
	public static void test(Object obj) {

		if (obj instanceof String) {
			//System.out.println("string");
		} else if (obj instanceof Integer) {
			//System.out.println("int");
		} else if (obj instanceof Long) {
			//System.out.println("long");
		} else if (obj instanceof byte[]) {
			//System.out.println("byte[]");
		} else if (obj instanceof Float) {
			//System.out.println("float");
		} else if (obj instanceof Double) {
			//System.out.println("double");
		} else if (obj instanceof StringBuffer) {
			//System.out.println("stringbuffer");
		} else if (obj instanceof StringBuilder) {
			//System.out.println("stringbuilder");
		}
	}*/

	public static void test(String obj) {
		//System.out.println("string");
	}
	
	public static void test(Integer obj) {
		//System.out.println("integer");
	}
	
	public static void test(Long obj) {
		//System.out.println("long");
	}
	
	public static void test(byte[] obj) {
		//System.out.println("byte[]");
	}
	
	public static void test(Float obj) {
		//System.out.println("float");
	}
	
	public static void test(Double obj) {
		//System.out.println("double");
	}
	
	public static void test(StringBuffer obj) {
		//System.out.println("stringbuffer");
	}
	
	public static void test(StringBuilder obj) {
		//System.out.println("stringbuilder");
	}
	
 	public static void main(String[] args) {
 		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000000L; i++) {
			test("123");
			test(111);
			test(222L);
			test(bytes);
			test(123.456);
			test(6654.321d);
			test(s1);
			test(s2);
		}
		System.out.println("耗时: " + (System.currentTimeMillis() - start) + "毫秒"); // 2882,10226
	}
}
