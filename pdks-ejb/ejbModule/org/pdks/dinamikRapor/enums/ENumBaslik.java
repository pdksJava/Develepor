/**
 * 
 */
package org.pdks.dinamikRapor.enums;

public enum ENumBaslik {

	SIRKET("sirketAciklama"), PERSONEL_NO("personelNoAciklama"), VARDIYA("vardiyaAciklama"), CALISMA_MODELI("calismaModeliAciklama"), KIMLIK_NO("kimlikNoAciklama"), YONETICI("yoneticiAciklama"), TESIS("tesisAciklama"), BOLUM("bolumAciklama");

	private final String value;

	ENumBaslik(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ENumBaslik fromValue(String v) {
		for (ENumBaslik c : ENumBaslik.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException();
	}

}
