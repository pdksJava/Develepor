package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;

@Entity(name = ERPSistem.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { ERPSistem.COLUMN_NAME_ERP_SIRKET }) })
public class ERPSistem extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6721777646547486510L;
	static Logger logger = Logger.getLogger(ERPSistem.class);
	public static final String TABLE_NAME = "ERP_SISTEM";
	public static final String COLUMN_NAME_ERP_SIRKET = "ERP_SIRKET_ADI";
	public static final String COLUMN_NAME_BASLANGIC_ZAMANI = "BASLANGIC_TARIHI";
	public static final String COLUMN_NAME_BITIS_ZAMANI = "BITIS_TARIHI";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	private String sirketAdi;

	private Boolean durum = Boolean.TRUE;
	private Date basTarih, bitTarih;

	@Column(name = COLUMN_NAME_ERP_SIRKET, nullable = false)
	public String getSirketAdi() {
		return sirketAdi;
	}

	public void setSirketAdi(String sirketAdi) {
		this.sirketAdi = sirketAdi;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BASLANGIC_ZAMANI, nullable = false)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BITIS_ZAMANI, nullable = false)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	public void entityRefresh() {

	}

}
