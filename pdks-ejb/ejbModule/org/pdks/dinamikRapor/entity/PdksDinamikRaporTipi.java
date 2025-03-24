/**
 * 
 */
package org.pdks.dinamikRapor.entity;

public enum PdksDinamikRaporTipi {

	VIEW(1), FUNCTION(2), STORE_PROCEDURE(3);

	private final Integer value;

	PdksDinamikRaporTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static PdksDinamikRaporTipi fromValue(Integer v) {
		for (PdksDinamikRaporTipi c : PdksDinamikRaporTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
