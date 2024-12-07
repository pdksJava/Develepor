package org.pdks.kgs.model;

import java.io.Serializable;

public class CihazPersonel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5454911434851152996L;

	private Long id;

	private String adi;

	private String soyadi;

	private String personelNo;

	private String kimlikNo;

	private String girisTarihi;

	private String cikisTarihi;

	private Boolean durum = Boolean.TRUE;

	public CihazPersonel() {
		super();
	}

	public CihazPersonel(Long id, String adi, String soyadi, String personelNo) {
		super();
		this.id = id;
		this.adi = adi;
		this.soyadi = soyadi;
		this.personelNo = personelNo;

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	public String getSoyadi() {
		return soyadi;
	}

	public void setSoyadi(String soyadi) {
		this.soyadi = soyadi;
	}

	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	public String getKimlikNo() {
		return kimlikNo;
	}

	public void setKimlikNo(String kimlikNo) {
		this.kimlikNo = kimlikNo;
	}

	public String getGirisTarihi() {
		return girisTarihi;
	}

	public void setGirisTarihi(String girisTarihi) {
		this.girisTarihi = girisTarihi;
	}

	public String getCikisTarihi() {
		return cikisTarihi;
	}

	public void setCikisTarihi(String cikisTarihi) {
		this.cikisTarihi = cikisTarihi;
	}

	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

}
