package org.pdks.kgs.model;

import java.io.Serializable;

public class Sonuc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6477608820626540707L;

	private String fonksiyon;

	private Boolean durum;

	private String hata;

	public Sonuc() {
		super();

	}

	public Sonuc(String fonksiyon, Boolean durum, String hata) {
		super();
		this.fonksiyon = fonksiyon;
		this.durum = durum;
		this.hata = hata;
	}

	public String getFonksiyon() {
		return fonksiyon;
	}

	public void setFonksiyon(String fonksiyon) {
		this.fonksiyon = fonksiyon;
	}

	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	public String getHata() {
		return hata;
	}

	public void setHata(String hata) {
		this.hata = hata;
	}

}
