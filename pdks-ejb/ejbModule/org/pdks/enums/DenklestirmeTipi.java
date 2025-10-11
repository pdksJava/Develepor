package org.pdks.enums;

public enum DenklestirmeTipi {

	GECEN_AY_ODE(1), TAMAMI_ODE(2);

	private final Integer value;

	DenklestirmeTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static DenklestirmeTipi fromValue(Integer v) {
		DenklestirmeTipi denklestirmeTipi = null;
		for (DenklestirmeTipi c : DenklestirmeTipi.values()) {
			if (c.value.equals(v)) {
				denklestirmeTipi = c;
			}
		}
		return denklestirmeTipi;

	}

}
