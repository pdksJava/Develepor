/**
 * 
 */
package org.pdks.dinamikRapor.entity;

public enum ENumDinamikRaporTipi {

	VIEW(1), FUNCTION(2), STORE_PROCEDURE(3);

	private final Integer value;

	ENumDinamikRaporTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static ENumDinamikRaporTipi fromValue(Integer v) {
		for (ENumDinamikRaporTipi c : ENumDinamikRaporTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
