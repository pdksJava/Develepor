/**
 * 
 */
package org.pdks.enums;

public enum KatSayiPuantajTipi {

	HAREKET_BEKLEME_SURESI(1), SUA_GUNLUK_SAAT_SURESI(2), YUVARLAMA_TIPI(3) , IZIN_HAFTA_TATIL_DURUM(9), FAZLA_MESAI_YUVARLAMA(13), BAYRAM_AYIR(
			14), UOM_YUVARLAMA(21), RT_YUVARLAMA(22), HT_YUVARLAMA(23), DENKLESTIRME_TIPI(24), RADYOLOJI_MAX_GUN(25), RT_KANUNEN_EKLEME(26), CIHAZ_ZAMAN_SANIYE_SIFIRLA(15),  IK_MAIL_GONDER(17), YONETICI_MAIL_GONDER(18);

	private final Integer value;

	KatSayiPuantajTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static KatSayiPuantajTipi fromValue(Integer v) {
		KatSayiPuantajTipi deger = null;
		for (KatSayiPuantajTipi c : KatSayiPuantajTipi.values()) {
			if (c.value.equals(v)) {
				deger = c;
			}
		}
		return deger;
	}

}
