package com.tatil.model;

import java.io.Serializable;
import java.util.Date;

import org.pdks.genel.model.PdksUtil;

public class Holiday implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3333765528639041434L;

	private String adi, basGun, bitGun;

	private Date basTarih;

	private Integer gunAdet;

	public Holiday(String tipi, Date basTarih, Integer gunAdet) {
		super();
		this.adi = (tipi.equalsIgnoreCase("K") ? "Kurban" : "Ramazan") + " Bayramı";
		this.basTarih = basTarih;
		this.gunAdet = gunAdet;
	}

	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	public String getBasGun() {
		return basGun;
	}

	public void setBasGun(String basGun) {
		this.basGun = basGun;
	}

	public String getBitGun() {
		return bitGun;
	}

	public void setBitGun(String bitGun) {
		this.bitGun = bitGun;
	}

	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	public void setBitTarih(Date bitTarih) {
		if (bitTarih != null) {
			this.basGun = PdksUtil.convertToDateString(basTarih, "yyyy-MM-dd");
			this.bitGun = PdksUtil.convertToDateString(bitTarih, "yyyy-MM-dd");
			this.basTarih = null;
			this.gunAdet = null;
		}

	}

	public Integer getGunAdet() {
		return gunAdet;
	}

	public void setGunAdet(Integer gunAdet) {
		this.gunAdet = gunAdet;
	}

}
