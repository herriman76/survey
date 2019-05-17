package com.sanyinggroup.corp.survey.server.tools;

 
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

 
/**
 * 权重处理工具类
 * <P>描述：根据权重获取可用的对象</P>
 * @author liujun
 * @date 2018年1月15日 下午3:35:02
 * @param <K>
 * @param <V>
 */
public class WeightRandomSelect<K,V extends Number> {
	private static final Logger logger =Logger.getLogger(WeightRandomSelect.class);
    private TreeMap<Integer, K> weightMap = new TreeMap<Integer, K>();
 
    public WeightRandomSelect(Map<K, V> map) {
    	
    	Iterator iter = map.entrySet().iterator(); 
    	while (iter.hasNext()) { 
    	    Map.Entry<K, V> entry = (Map.Entry<K, V>) iter.next(); 
            int lastWeight = this.weightMap.size() == 0 ? 0 : this.weightMap.lastKey().intValue();//统一转为double
            this.weightMap.put(entry.getValue().intValue() + lastWeight, entry.getKey());//权重累加
        }
    	logger.debug("size is:"+weightMap.size());
    	logger.debug("size is:"+weightMap.toString());
    }
 
    /**
     * 总权重的随机数，得到tailMap后的第一个值，即为返回的值
     * <P></P>
     * @return
     */
    public K random() {
        Integer randomWeight = (int) (this.weightMap.lastKey().intValue() * Math.random());
        logger.debug("randomWeight is:"+randomWeight);
        SortedMap<Integer, K> tailMap = this.weightMap.tailMap(randomWeight, true);
        return this.weightMap.get(tailMap.firstKey());
    }
    
	public static void main(String[] args) throws IOException {
		Map<String, Integer> canUseClient = new HashMap<String, Integer>();
		canUseClient.put("aaa", 31);
		canUseClient.put("bbb", 13);
		canUseClient.put("ccc", 12);
		canUseClient.put("ddd", 70);
		WeightRandomSelect weightRandom=new WeightRandomSelect(canUseClient);
		String key=(String) weightRandom.random();
		logger.debug(key);
	}
 
}
