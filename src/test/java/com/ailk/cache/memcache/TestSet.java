package com.ailk.cache.memcache;

import com.ailk.cache.memcache.interfaces.IMemCache;
import com.ailk.common.data.IData;
import com.ailk.common.data.IDataset;
import com.ailk.common.data.impl.DataMap;
import com.ailk.common.data.impl.DatasetList;

public class TestSet {
	public static void main(String[] args) throws InterruptedException {
		
		IDataset datas = new DatasetList();
		IData data = new DataMap();
		data.put("TRADE_TYPE_CODE", "1345");
		data.put("TRADE_TYPE", "客户资料登记");
		data.put("EPARCHY_CODE", "0735");
		data.put("BPM_CODE", "TCS_TradeFinish");
		data.put("PRIORITY", "320");
		data.put("TRADE_ATTR", "1");
		data.put("BACK_TAG", "0");
		data.put("INTF_TAG_SET", "10000101100000101000010000");
		data.put("UPDATE_TIME", "2009-06-18 19:39:12");
		datas.add(data);
		
		IMemCache cache = MemCacheFactory.getCache(MemCacheFactory.CODECODE_CACHE);
		boolean b = cache.set("=========================================", datas);
		System.out.println(b);
	}
}
