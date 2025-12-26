package org.pdks.enums;

public enum MethodAlanAPI {

	SIRKET("0"), PERSONEL("1"), KIMLIK("2"), TESIS("3"), MASRAF_YERI("4"), UOM("UO"), RT("RT"), HT("HT"), AKSAM_GUN("A"), AKSAM_SAAT("AS"), YIL("90"), AY("91"), USER_NAME("98"), PASSWORD("99");

	private final String value;

	MethodAlanAPI(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static MethodAlanAPI fromValue(String v) {
		MethodAlanAPI methodAPI = null;
		for (MethodAlanAPI c : MethodAlanAPI.values()) {
			if (c.value.equals(v)) {
				methodAPI = c;
			}
		}
		return methodAPI;

	}

	/**
	 * @param alanTipi
	 * @return
	 */
	public static String getAlanAciklama(String alanTipi) {
		String str = "";
		if (alanTipi != null) {
			str = alanTipi;
			MethodAlanAPI alanAPI = fromValue(alanTipi);
			if (alanAPI != null) {
				if (alanAPI.equals(KIMLIK))
					str = "Kimlik No";
				else if (alanAPI.equals(MASRAF_YERI))
					str = "Masraf Yeri No";
				else if (alanAPI.equals(UOM))
					str = "Ücreti Ödenen Mesai";
				else if (alanAPI.equals(RT))
					str = "Resmi Tatil Mesai";
				else if (alanAPI.equals(HT))
					str = "Hafta Tatil Mesai";
				else if (alanAPI.equals(AKSAM_GUN))
					str = "Akşam Gün Sayısı";
				else if (alanAPI.equals(AKSAM_SAAT))
					str = "Akşam Saat Sayısı";
				else if (alanAPI.equals(YIL))
					str = "Yıl";
				else if (alanAPI.equals(AY))
					str = "Ay";
				else if (alanAPI.equals(USER_NAME))
					str = "Kullanıcı Adı";
				else if (alanAPI.equals(PASSWORD))
					str = "Şifre";

			}
		}
		return str;
	}

}
