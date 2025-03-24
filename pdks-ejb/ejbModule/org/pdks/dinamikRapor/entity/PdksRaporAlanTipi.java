/**
 * 
 */
package org.pdks.dinamikRapor.entity;

public enum PdksRaporAlanTipi {

	SAYISAL(1), KARAKTER(2), TARIH(3), SAAT(4), TARIH_SAAT(5);

	private final Integer value;

	PdksRaporAlanTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static PdksRaporAlanTipi fromValue(Integer v) {
		for (PdksRaporAlanTipi c : PdksRaporAlanTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
