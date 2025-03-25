/**
 * 
 */
package org.pdks.dinamikRapor.entity;

public enum PdksRaporAlanHizalaTipi {

	SOLA(1), ORTALA(2), SAGA(3);

	private final Integer value;

	PdksRaporAlanHizalaTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static PdksRaporAlanHizalaTipi fromValue(Integer v) {
		for (PdksRaporAlanHizalaTipi c : PdksRaporAlanHizalaTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
