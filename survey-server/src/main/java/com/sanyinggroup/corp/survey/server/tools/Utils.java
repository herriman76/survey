package com.sanyinggroup.corp.survey.server.tools;
import java.util.UUID;

/**
 * UUID处理工具类
 * @author liujun
 *
 */
public final class Utils{

	
//=============UUID======================
  public static String getNormalUUID(){
    return UUID.randomUUID().toString();
  }
  
  public static String getShortUUID(){
    return UUID.randomUUID().toString().replace("-", "");
  }
  
  public static String getDoubleShortUUID(){
    return getShortUUID() + getShortUUID();
  }
  
}
