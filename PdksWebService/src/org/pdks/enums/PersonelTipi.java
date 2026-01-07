package org.pdks.enums;

public enum PersonelTipi {

	TUM(0), IK(1), SUPERVISOR(2);

	private final Integer value;

	PersonelTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static PersonelTipi fromValue(Integer v) {
		PersonelTipi personelTipi = null;
		for (PersonelTipi c : PersonelTipi.values()) {
			if (c.value.equals(v)) {
				personelTipi = c;
			}
		}
		return personelTipi;

	}

}
