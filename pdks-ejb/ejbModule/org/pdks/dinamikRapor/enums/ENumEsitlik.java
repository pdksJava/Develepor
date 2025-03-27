/**
 * 
 */
package org.pdks.dinamikRapor.enums;

public enum ENumEsitlik {

	BUYUK(">"), BUYUKESIT(">="), KUCUK("<"), KUCUKESIT("<="), ICEREN("like");

	private final String value;

	ENumEsitlik(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ENumEsitlik fromValue(String v) {
 		ENumEsitlik deger = null;
		for (ENumEsitlik c : ENumEsitlik.values()) {
			if (c.value.equals(v)) {
				deger = c;
			}
		}
		return deger;
	}

}
