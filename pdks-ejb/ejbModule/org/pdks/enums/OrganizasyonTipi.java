/**
 * 
 */
package org.pdks.enums;

public enum OrganizasyonTipi {

	TESIS(1), BOLUM(2);

	private final Integer value;

	OrganizasyonTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static OrganizasyonTipi fromValue(Integer v) {
		for (OrganizasyonTipi c : OrganizasyonTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
