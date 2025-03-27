/**
 * 
 */
package org.pdks.enums;

public enum PersonelDurumTipi {

	GEBE(1), SUT_IZNI(2), IS_ARAMA_IZNI(3);

	private final Integer value;

	PersonelDurumTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static PersonelDurumTipi fromValue(Integer v) {
		PersonelDurumTipi deger = null;
		for (PersonelDurumTipi c : PersonelDurumTipi.values()) {
			if (c.value.equals(v)) {
				deger = c;
			}
		}
		return deger;
	}

}
