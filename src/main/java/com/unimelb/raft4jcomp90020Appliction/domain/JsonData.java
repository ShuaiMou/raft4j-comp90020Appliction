package com.unimelb.raft4jcomp90020Appliction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 功能：响应结果类
 *
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JsonData<T> implements Serializable {

	private int code;

	private T data;

	private String msg;

	public JsonData(int code, String msg){
		this.code = code;
		this.msg = msg;
	}

    /**
     *  成功
     *
     * @return 状态码，消息
     */
	public static JsonData buildSuccess() {
		return new JsonData<Object>(StateType.OK.getCode(), null, StateType.OK.value());
	}

	public static JsonData buildSuccess(int code, Object data, String msg) {
		return new JsonData<Object>(code, data, msg);
	}

	public static JsonData buildSuccess(int code, String msg) {
		return buildSuccess(code, null, msg);
	}

    /**
     * 成功，传入数据
     *
     * @return 状态码，消息，数据
     */
	public static JsonData buildSuccess(Object data) {
		return new JsonData<Object>(StateType.OK.getCode(), data, StateType.OK.value());
	}

    /**
     * 失败，传入描述信息,状态码
     *
     * @param msg 消息
     * @param code 状态码
     * @return 状态码，消息
     */
	public static JsonData buildError(Integer code, String msg) {
		return new JsonData<Object>(code, null, msg);
	}


	@Override
	public String toString() {
		return "JsonData [code=" + code + ", data=" + data + ", msg=" + msg
				+ "]";
	}

}
