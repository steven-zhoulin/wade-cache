package com.ailk.cache.memcache.driver.util;

import java.util.Date;

/**
 * Copyright: Copyright (c) 2013 Asiainfo-Linkage
 * 
 * @className: FastConvertor
 * @description: 快速类型转换器
 * 
 * @version: v1.0.0
 * @author: zhoulin2
 * @date: 2013-2-25
 */
public final class FastConvertor {
	
	public static final int MARKER_COMPRESS      = 1 <<  0; // 压缩标识
	public static final int MARKER_BOOLEAN       = 1 <<  1;
	public static final int MARKER_BYTE          = 1 <<  2;
	public static final int MARKER_CHARACTER     = 1 <<  3;
	public static final int MARKER_SHORT         = 1 <<  4;
	public static final int MARKER_INTEGER       = 1 <<  5;
	public static final int MARKER_LONG          = 1 <<  6;
	public static final int MARKER_FLOAT         = 1 <<  7;
	public static final int MARKER_DOUBLE        = 1 <<  8;
	public static final int MARKER_STRING        = 1 <<  9;
	public static final int MARKER_STRINGBUFFER  = 1 << 10;
	public static final int MARKER_STRINGBUILDER = 1 << 11;
	public static final int MARKER_DATE          = 1 << 12;
	public static final int MARKER_BYTEARR       = 1 << 13;
	
	public static final int MARKER_OTHERS        = 0;
		
	public static final byte[] encode(Byte value) {
		byte[] b = new byte[1];
		b[0] = value.byteValue();
		return b;
	}

	public static final byte[] encode(Object obj, int flag) {
		byte[] datas = null;
		switch (flag) {
			case MARKER_BOOLEAN:       datas = encode((Boolean)obj);        break;	
			case MARKER_BYTE:          datas = encode((Byte) obj);          break;
			case MARKER_CHARACTER:     datas = encode((Character)obj);      break;
			case MARKER_SHORT:         datas = encode((Short)obj);          break;
			case MARKER_INTEGER:       datas = encode((Integer)obj);        break;
			case MARKER_LONG:          datas = encode((Long)obj);           break;
			case MARKER_FLOAT:         datas = encode((Float)obj);          break;
			case MARKER_DOUBLE:        datas = encode((Double)obj);         break;
			case MARKER_STRING:        datas = encode((String)obj);         break;
			case MARKER_STRINGBUFFER:  datas = encode((StringBuffer)obj);   break;
			case MARKER_STRINGBUILDER: datas = encode((StringBuilder)obj);  break;
			case MARKER_DATE:          datas = encode((Date)obj);           break;
			case MARKER_BYTEARR:       datas = encode((byte[])obj);         break;
		}		
		return datas;
	}
	
	/**
	 * Boolean序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(Boolean value) {
		byte[] b = new byte[1];

		if (value.booleanValue()) b[0] = 1;
		else					  b[0] = 0;

		return b;
	}

	/**
	 * Integer序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(int value) {
		return getBytes(value);
	}

	/**
	 * Long序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(long value) {
		return getBytes(value);
	}

	/**
	 * Date序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(Date value) {
		return getBytes(value.getTime());
	}

	/**
	 * Character序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(Character value) {
		return encode(value.charValue());
	}

	/**
	 * String序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(String value) {
		return value.getBytes();
	}

	/**
	 * StringBuffer序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(StringBuffer value) {
		return encode(value.toString());
	}

	/**
	 * Float序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(float value) {
		return encode((int) Float.floatToIntBits(value));
	}

	/**
	 * Short序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(Short value) {
		return encode((int) value.shortValue());
	}

	/**
	 * Double序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(double value) {
		return encode((long) Double.doubleToLongBits(value));
	}

	/**
	 * StringBuilder序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(StringBuilder value) {
		return encode(value.toString());
	}

	/**
	 * 字节数组的序列化
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] encode(byte[] value) {
		return value;
	}

	/**
	 * int转换成字节数组
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] getBytes(int value) {
		byte[] b = new byte[4];
		
		b[0] = (byte) ((value >> 24) & 0xFF); // 高位
		b[1] = (byte) ((value >> 16) & 0xFF);
		b[2] = (byte) ((value >>  8) & 0xFF);
		b[3] = (byte) ((value >>  0) & 0xFF); // 低位
				
		return b;
	}
		
	/**
	 * long转换成字节数组
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] getBytes(long value) {
		byte[] b = new byte[8];
		
		b[0] = (byte) ((value >> 56) & 0xFF); // 高位
		b[1] = (byte) ((value >> 48) & 0xFF);
		b[2] = (byte) ((value >> 40) & 0xFF);
		b[3] = (byte) ((value >> 32) & 0xFF);
		b[4] = (byte) ((value >> 24) & 0xFF);
		b[5] = (byte) ((value >> 16) & 0xFF);
		b[6] = (byte) ((value >>  8) & 0xFF);
		b[7] = (byte) ((value >>  0) & 0xFF); // 低位
		
		return b;
	}

	/**
	 * 根据标志，进行对象的反序列化
	 * 
	 * @param datas
	 * @param flag
	 * @return
	 */
	public static final Object decode(byte[] datas, int flag) {
		Object obj = null;
		switch (flag) {
			case MARKER_BOOLEAN:       obj = decodeBoolean(datas);       break;
			case MARKER_BYTE:          obj = decodeByte(datas);          break;
			case MARKER_CHARACTER:     obj = decodeCharacter(datas);     break;
			case MARKER_SHORT:         obj = decodeShort(datas);         break;
			case MARKER_INTEGER:       obj = decodeInteger(datas);       break;
			case MARKER_LONG:          obj = decodeLong(datas);          break;
			case MARKER_FLOAT:         obj = decodeFloat(datas);         break;
			case MARKER_DOUBLE:        obj = decodeDouble(datas);        break;
			case MARKER_STRING:        obj = decodeString(datas);        break;
			case MARKER_STRINGBUFFER:  obj = decodeStringBuffer(datas);  break;
			case MARKER_STRINGBUILDER: obj = decodeStringBuilder(datas); break;
			case MARKER_DATE:          obj = decodeDate(datas);          break;			
			case MARKER_BYTEARR:       obj = decodeByteArr(datas);       break;
		}
		
		return obj;
	}
	
	/**
	 * Byte的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Byte decodeByte(byte[] b) {
		return new Byte(b[0]);
	}

	/**
	 * Boolean的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Boolean decodeBoolean(byte[] b) {
		boolean value = b[0] == 1;
		return (value) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Integer的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Integer decodeInteger(byte[] b) {
		return new Integer(toInt(b));
	}

	/**
	 * Long的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Long decodeLong(byte[] b) {
		return new Long(toLong(b));
	}

	/**
	 * Character的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Character decodeCharacter(byte[] b) {
		return new Character((char) decodeInteger(b).intValue());
	}

	/**
	 * String的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final String decodeString(byte[] b) {
		return new String(b);
	}

	/**
	 * StringBuffer的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final StringBuffer decodeStringBuffer(byte[] b) {
		return new StringBuffer(decodeString(b));
	}

	/**
	 * StringBuilder的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final StringBuilder decodeStringBuilder(byte[] b) {
		return new StringBuilder(decodeString(b));
	}
	
	/**
	 * Float的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Float decodeFloat(byte[] b) {
		Integer integer = decodeInteger(b);
		return new Float(Float.intBitsToFloat(integer.intValue()));
	}

	/**
	 * Short的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Short decodeShort(byte[] b) {
		return new Short((short) decodeInteger(b).intValue());
	}

	/**
	 * Double的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Double decodeDouble(byte[] b) {
		Long l = decodeLong(b);
		return new Double(Double.longBitsToDouble(l.longValue()));
	}

	/**
	 * Date的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final Date decodeDate(byte[] b) {
		return new Date(toLong(b));
	}
	
	/**
	 * 字节数组的反序列化
	 * 
	 * @param b
	 * @return
	 */
	public static final byte[] decodeByteArr(byte[] b) {
		return b;
	}
		
	/**
	 * 字节数组转整形
	 */
	public static final int toInt(byte[] b) {
		return (
			((((int)b[0]) & 0xFF) << 24) + // 高位
			((((int)b[1]) & 0xFF) << 16) +
			((((int)b[2]) & 0xFF) <<  8) +
			((((int)b[3]) & 0xFF) <<  0)   // 低位
		);
	}
	
	/**
	 * 字节数组转长整形
	 * 
	 * @param b
	 * @return
	 */
	public static final long toLong(byte[] b) {
		return (
			((((long) b[0]) & 0xFF) << 56) +
			((((long) b[1]) & 0xFF) << 48) +
			((((long) b[2]) & 0xFF) << 40) +
			((((long) b[3]) & 0xFF) << 32) +
			((((long) b[4]) & 0xFF) << 24) +
			((((long) b[5]) & 0xFF) << 16) +
			((((long) b[6]) & 0xFF) <<  8) +
			((((long) b[7]) & 0xFF) <<  0)
		);
	}
		
	/**
	 * 字符串对应的字节数组，转int操作
	 * 
	 * @param bytes
	 * @param start
	 * @param end
	 * @return
	 */
	public static final int strBytes2Int(byte[] bytes, int start, int end) {
		int rtn = 0;
		for (int i = start; i < end; i++) {
			rtn = rtn * 10 + (bytes[i] - '0');
		}
		return rtn;
	}
	
	/**
	 * 字符串对应的字节数组，进行转long操作
	 * 
	 * @param bytes
	 * @return
	 */
	public static final long strBytes2Long(byte[] bytes) {
		long rtn = 0;
		for (int i = 0; i < bytes.length; i++) {
			rtn = rtn * 10 + (bytes[i] - '0');
		}
		return rtn;
	}
	
	public static void main(String[] args) {
		long i = 1111111111111119123L;
		byte[] bytes = getBytes(i);
		System.out.println(toLong(bytes));
	}
	
}
