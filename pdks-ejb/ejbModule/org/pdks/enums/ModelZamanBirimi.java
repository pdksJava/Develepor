package org.pdks.enums;

public enum ModelZamanBirimi {

	 DAKIKA(60), SAAT(1);

	private final Integer value;

	ModelZamanBirimi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static ModelZamanBirimi fromValue(Integer v) {
		ModelZamanBirimi zamanBirimi = null;
		for (ModelZamanBirimi c : ModelZamanBirimi.values()) {
			if (c.value.equals(v)) {
				zamanBirimi = c;
			}
		}
		return zamanBirimi;

	}

}
