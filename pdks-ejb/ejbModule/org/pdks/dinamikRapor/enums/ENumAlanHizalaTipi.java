/**
 * 
 */
package org.pdks.dinamikRapor.enums;

public enum ENumAlanHizalaTipi {

	SOLA(1), ORTALA(2), SAGA(3);

	private final Integer value;

	ENumAlanHizalaTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static ENumAlanHizalaTipi fromValue(Integer v) {
 		ENumAlanHizalaTipi deger = null;
		for (ENumAlanHizalaTipi c : ENumAlanHizalaTipi.values()) {
			if (c.value.equals(v)) {
				deger = c;
			}
		}
		return deger;
	}

}
