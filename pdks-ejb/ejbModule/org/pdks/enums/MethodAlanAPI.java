package org.pdks.enums;

public enum MethodAlanAPI {

	SIRKET("0"), PERSONEL("1"), KIMLIK("2"), TESIS("3"), MASRAF_YERI("4"), UOM("UO"), RT("RT"), HT("HT"), AKSAM_GUN("A"), AKSAM_SAAT("AS"), YIL("90"), AY("91"), USER_NAME("98"), PASSWORD("99");

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
