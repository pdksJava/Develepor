/**
 * 
 */
package org.pdks.enums;

public enum KatSayiVardiyaGunTipi {

	OFF_FAZLA_MESAI_TIPI(4), HT_FAZLA_MESAI_TIPI(5), ERKEN_GIRIS_TIPI(6), GEC_CIKIS_TIPI(7), FMT_DURUM(8), YEMEK_SURE_EKLE_DURUM(10), ERKEN_CIKIS_TIPI(11), GEC_GIRIS_TIPI(12), VARDIYA_MOLA(16), SAAT_CALISAN_GUN(90), SAAT_CALISAN_NORMAL_GUN(91), SAAT_CALISAN_IZIN_GUN(92), SAAT_CALISAN_HAFTA_TATIL(93), SAAT_CALISAN_RESMI_TATIL(94), SAAT_CALISAN_ARIFE_NORMAL_SAAT(95), SAAT_CALISAN_ARIFE_TATIL_SAAT(96);

	private final Integer value;

	KatSayiVardiyaGunTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static KatSayiVardiyaGunTipi fromValue(Integer v) {
		KatSayiVardiyaGunTipi deger = null;
		for (KatSayiVardiyaGunTipi c : KatSayiVardiyaGunTipi.values()) {
			if (c.value.equals(v)) {
				deger = c;
			}
		}
		return deger;
	}

}
