package com.sanyinggroup.corp.survey.server.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * json处理工具类
 * @author liujun
 *
 */
public final class JsonUtils {
	public static String toString(Object obj) throws Exception {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
		if (obj instanceof Collection) {
			return JSONArray.fromObject(obj).toString();
		} else {
			return JSONObject.fromObject(obj).toString();
		}
	}

	public static JSONObject toJSONObject(String obj) throws Exception {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
		return JSONObject.fromObject(obj);
	}

	public static JSONArray toJSONArray(String obj) throws Exception {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
		return JSONArray.fromObject(obj);
	}

	@SuppressWarnings("unchecked")
	public static <T> T toBean(String obj, Class<T> beanClass) throws Exception {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
		JSONObject jsonObj = JSONObject.fromObject(obj);
		return (T) JSONObject.toBean(jsonObj, beanClass);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(String obj, Class<T> beanClass) throws Exception {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
		JSONArray jsonArray = JSONArray.fromObject(obj);
		return (T[]) JSONArray.toArray(jsonArray, beanClass);
	}

	public static <T> List<T> toList(String jsonArrString, Class<T> beanClass) throws Exception {
		T[] t = toArray(jsonArrString, beanClass);
		return t == null ? null : Arrays.asList(t);
	}
}
