package com.sanyinggroup.corp.survey.server.tools;

 
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.sanyinggroup.corp.survey.server.Constants;
import com.sanyinggroup.corp.survey.server.container.AppContainer;

 
/**
 * 权重处理工具类
 * <P>描述：根据权重获取可用的对象</P>
 * @author liujun
 * @date 2018年1月15日 下午3:35:02
 * @param <K>
 * @param <V>
 */
public class RankAndLeverTreeMapSelect<K,V extends Number> {
	private static final Logger logger =Logger.getLogger(RankAndLeverTreeMapSelect.class);
    private TreeMap<Integer, K> treeMap = new TreeMap<Integer, K>();
    private int _userRank =0;
    private int _baseLever=Constants.CLIENT_BASE_LEVER;
 
    public RankAndLeverTreeMapSelect(Map<K, V> map, int userRank) {
    	_userRank=userRank;
    	Iterator iter = map.entrySet().iterator(); 
    	while (iter.hasNext()) { 
    	    Map.Entry<K, V> entry = (Map.Entry<K, V>) iter.next(); 
    	    if(entry.getValue().intValue()>0){//0分的不用
    	    	this.treeMap.put(entry.getValue().intValue(), entry.getKey());//权重累加
    	    }
        }
    	logger.debug("size is:"+treeMap.size());
    	logger.debug("size is:"+treeMap.toString());
    }
 
    /**
     * 策略：
     * 1.如果有用户级别值，比如90分，那100、80、70、60、40、20分的客户端中，选择最近的80分的客户端。
     * 2.如果用户没有级别，那80/70/60/40/20中，选择及格的最低的60，如果都不及格，选择最高的40。
     * 3.选择了一个分值的客户端，如果这里面有多个，再随机选择一个（未来根据完成情况或者性能）
     * <P></P>
     * @return
     */
    public K choose() {
    	if(treeMap.size()==0) return null;
    	if(treeMap.size()==1) return treeMap.firstEntry().getValue();
        if(_userRank>0d){//如果有用户级别
        	logger.debug("有用户级别值，找接近最大的。_userRank:"+_userRank);
        	SortedMap<Integer, K> headMap = this.treeMap.headMap(_userRank, true);
        	logger.debug("_userRank & headMap.size:"+_userRank+"|"+headMap.size());
        	if(headMap.size()==0) return treeMap.firstEntry().getValue();//如果找不到，给最低的。
        	return this.treeMap.get(headMap.lastKey());
        }
        else{//如果无用户级别
        	logger.debug("无用户级别值，找及格里最小的。_baseLever:"+_baseLever);
        	SortedMap<Integer, K> tailMap = this.treeMap.tailMap(_baseLever, true);
        	logger.debug("_baseLever & headMap.size:"+_baseLever+"|"+tailMap.size());
        	if(tailMap.size()==0) return treeMap.lastEntry().getValue();//如果都生活及格，找一个最大的。
        	return this.treeMap.get(tailMap.firstKey());
        }
    }
    
    /**
     * 考虑到重复情况，不能返回key了，只能返回特定value后再循环处理。
     * @return
     */
    public Integer chooseValue() {
    	if(treeMap.size()==0) return null;
    	if(treeMap.size()==1) return treeMap.firstEntry().getKey();
        if(_userRank>0d){//如果有用户级别
        	logger.debug("有用户级别值，找接近最大的。_userRank:"+_userRank);
        	SortedMap<Integer, K> headMap = this.treeMap.headMap(_userRank, true);
        	logger.debug("_userRank & headMap.size:"+_userRank+"|"+headMap.size());
        	if(headMap.size()==0) return treeMap.firstEntry().getKey();//如果找不到，给最低的。
        	return headMap.lastKey();
        }
        else{//如果无用户级别
        	logger.debug("无用户级别值，找及格里最小的。_baseLever:"+_baseLever);
        	SortedMap<Integer, K> tailMap = this.treeMap.tailMap(_baseLever, true);
        	logger.debug("_baseLever & headMap.size:"+_userRank+"|"+tailMap.size());
        	if(tailMap.size()==0) return treeMap.lastEntry().getKey();//如果都生活及格，找一个最大的。
        	return tailMap.firstKey();
        }
    }
    
    
	public static void main(String[] args) throws IOException {
		Map<String, Integer> canUseClient = new HashMap<String, Integer>();
		canUseClient.put("aaa", 31);
		canUseClient.put("bbb", 13);
		canUseClient.put("ccc", 70);
		canUseClient.put("ddd", 22);
		canUseClient.put("eee", 80);
		RankAndLeverTreeMapSelect weightRandom=new RankAndLeverTreeMapSelect(canUseClient,80);
//		RankAndLeverTreeMapSelect weightRandom=new RankAndLeverTreeMapSelect(canUseClient,70);
//		RankAndLeverTreeMapSelect weightRandom=new RankAndLeverTreeMapSelect(canUseClient,80);
		String key=(String) weightRandom.choose();
		logger.debug(key);
		Integer keyValue=weightRandom.chooseValue();
		logger.debug(keyValue);
	}
 
}
