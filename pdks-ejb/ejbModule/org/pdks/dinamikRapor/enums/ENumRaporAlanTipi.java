/**
 * 
 */
package org.pdks.dinamikRapor.enums;

public enum ENumRaporAlanTipi {

	SAYISAL(1), KARAKTER(2), TARIH(3), SAAT(4), TARIH_SAAT(5), MANTIKSAL(6);

	private final Integer value;

	ENumRaporAlanTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static ENumRaporAlanTipi fromValue(Integer v) {
 		ENumRaporAlanTipi deger = null;
		for (ENumRaporAlanTipi c : ENumRaporAlanTipi.values()) {
			if (c.value.equals(v)) {
				deger = c;
			}
		}
		return deger;
	}

}
