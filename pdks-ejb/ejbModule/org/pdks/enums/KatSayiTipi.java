/**
 * 
 */
package org.pdks.enums;

public enum KatSayiTipi {

	HAREKET_BEKLEME_SURESI(1), SUA_GUNLUK_SAAT_SURESI(2), YUVARLAMA_TIPI(3), OFF_FAZLA_MESAI_TIPI(4), HT_FAZLA_MESAI_TIPI(5), ERKEN_GIRIS_TIPI(6), ERKEN_CIKIS_TIPI(11), GEC_GIRIS_TIPI(12), GEC_CIKIS_TIPI(7), FMT_DURUM(8), IZIN_HAFTA_TATIL_DURUM(9), YEMEK_SURE_EKLE_DURUM(10), FAZLA_MESAI_YUVARLAMA(13), BAYRAM_AYIR(14), UOM_YUVARLAMA(21), RT_YUVARLAMA(22), CIHAZ_ZAMAN_SANIYE_SIFIRLA(15), SAAT_CALISAN_GUN(90), SAAT_CALISAN_NORMAL_GUN(
			91), SAAT_CALISAN_IZIN_GUN(92), SAAT_CALISAN_HAFTA_TATIL(93), SAAT_CALISAN_RESMI_TATIL(94), SAAT_CALISAN_ARIFE_NORMAL_SAAT(95), SAAT_CALISAN_ARIFE_TATIL_SAAT(96);

	private final Integer value;

	KatSayiTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static KatSayiTipi fromValue(Integer v) {
		KatSayiTipi deger = null;
		for (KatSayiTipi c : KatSayiTipi.values()) {
			if (c.value.equals(v)) {
				deger = c;
			}
		}
		return deger;
	}

}
