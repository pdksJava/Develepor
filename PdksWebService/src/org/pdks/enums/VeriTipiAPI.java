package org.pdks.enums;

public enum VeriTipiAPI {

	JSON(1), XML(2);

	private final Integer value;

	VeriTipiAPI(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static VeriTipiAPI fromValue(Integer v) {
		VeriTipiAPI veriTipiAPI = null;
		for (VeriTipiAPI c : VeriTipiAPI.values()) {
			if (c.value.equals(v)) {
				veriTipiAPI = c;
			}
		}
		return veriTipiAPI;

	}

	/**
	 * @param servisVeriTipi
	 * @return
	 */
	public static String getServisTipiAciklama(Integer servisVeriTipi) {
		String str = "";
		if (servisVeriTipi != null) {
			VeriTipiAPI veriTipiAPI = fromValue(servisVeriTipi);
			if (veriTipiAPI != null) {
				if (veriTipiAPI.equals(JSON))
					str = "Json";
				else if (veriTipiAPI.equals(XML))
					str = "Xml";

			}
		}
		return str;
	}

}
