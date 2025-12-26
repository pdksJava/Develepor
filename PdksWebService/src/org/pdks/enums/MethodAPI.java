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

	/**
	 * @param methodAdi
	 * @return
	 */
	public static String getMethodAciklama(String methodAdi) {
		String str = "";
		if (methodAdi != null) {
			MethodAPI methodAPI = fromValue(methodAdi);
			if (methodAPI != null) {
				if (methodAPI.equals(POST))
					str = "Post";
				else if (methodAPI.equals(PUT))
					str = "Put";
				else if (methodAPI.equals(GET))
					str = "Get";

			}
		}
		return str;
	}

}
