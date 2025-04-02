/**
 * 
 */
package org.pdks.dinamikRapor.enums;

public enum ENumBaslik {

	SIRKET("sirketAciklama"), PERSONEL_NO("personelNoAciklama"), VARDIYA("vardiyaAciklama"), CALISMA_MODELI("calismaModeliAciklama"), KIMLIK_NO("kimlikNoAciklama"), YONETICI("yoneticiAciklama"), TESIS("tesisAciklama")
	, BOLUM("bolumAciklama"), BAS_TARIH("basTarihAciklama"), BIT_TARIH("bitTarihAciklama");

	private final String value;

	ENumBaslik(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ENumBaslik fromValue(String v) {
		ENumBaslik deger = null;
		for (ENumBaslik c : ENumBaslik.values()) {
			if (c.value.equals(v)) {
				deger = c;
			}
		}
		return deger;

	}

}
