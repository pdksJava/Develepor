/**
 * 
 */
package org.pdks.dinamikRapor.entity;

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
		for (ENumEsitlik c : ENumEsitlik.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
