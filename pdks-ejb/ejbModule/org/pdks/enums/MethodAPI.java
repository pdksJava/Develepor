package org.pdks.enums;

public enum MethodAPI {

	GET("get"), POST("post"), PUT("put");

	private final String value;

	MethodAPI(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static MethodAPI fromValue(String v) {
		MethodAPI methodAPI = null;
		for (MethodAPI c : MethodAPI.values()) {
			if (c.value.equals(v)) {
				methodAPI = c;
			}
		}
		return methodAPI;

	}

}
