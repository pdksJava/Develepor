package org.pdks.enums;

public enum MethodAlanAPI {

	SIRKET("1"), TESIS("2"), KIMLIK("3"), MASRAF_YERI("4");

	private final String value;

	MethodAlanAPI(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static MethodAlanAPI fromValue(String v) {
		MethodAlanAPI methodAPI = null;
		for (MethodAlanAPI c : MethodAlanAPI.values()) {
			if (c.value.equals(v)) {
				methodAPI = c;
			}
		}
		return methodAPI;

	}

}
