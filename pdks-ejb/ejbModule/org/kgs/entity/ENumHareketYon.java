/**
 * 
 */
package org.kgs.entity;

public enum ENumHareketYon {

	GIRIS(1), CIKIS(2), CIFT_YON(0);

	private final Integer value;

	ENumHareketYon(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static ENumHareketYon fromValue(Integer v) {
		ENumHareketYon deger = null;
		for (ENumHareketYon c : ENumHareketYon.values()) {
			if (c.value.equals(v)) {
				deger = c;
				break;
			}
		}
		return deger;
	}

}
