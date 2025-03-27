/**
 * 
 */
package org.pdks.dinamikRapor.entity;

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
		for (ENumAlanHizalaTipi c : ENumAlanHizalaTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
