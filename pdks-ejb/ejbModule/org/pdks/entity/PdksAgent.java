package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@Entity(name = PdksAgent.TABLE_NAME)
public class PdksAgent extends BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 222120492743031240L;

	static Logger logger = Logger.getLogger(PdksAgent.class);

	public static final String TABLE_NAME = "PDKS_AGENT";

	public static final String COLUMN_NAME_SP = "STORE_PROCEDURE";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_DAKIKA = "DAKIKA";
	public static final String COLUMN_NAME_SAAT = "SAAT";
	public static final String COLUMN_NAME_GUN = "GUN";
	public static final String COLUMN_NAME_HAFTA = "HAFTA";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_UPDATE_SP = "UPDATE_SP";

	private String storeProcedureAdi, aciklama;

	private String dakikaBilgi, saatBilgi, gunBilgi, haftaBilgi;

	private Boolean durum = Boolean.TRUE, updateSP = Boolean.TRUE, start = false;

	@Column(name = COLUMN_NAME_SP)
	public String getStoreProcedureAdi() {
		return storeProcedureAdi;
	}

	public void setStoreProcedureAdi(String storeProcedureAdi) {
		this.storeProcedureAdi = storeProcedureAdi;
	}

	@Column(name = COLUMN_NAME_DAKIKA)
	public String getDakikaBilgi() {
		return dakikaBilgi;
	}

	public void setDakikaBilgi(String dakikaBilgi) {
		this.dakikaBilgi = dakikaBilgi;
	}

	@Column(name = COLUMN_NAME_SAAT)
	public String getSaatBilgi() {
		return saatBilgi;
	}

	public void setSaatBilgi(String saatBilgi) {
		this.saatBilgi = saatBilgi;
	}

	@Column(name = COLUMN_NAME_GUN)
	public String getGunBilgi() {
		return gunBilgi;
	}

	public void setGunBilgi(String gunBilgi) {
		this.gunBilgi = gunBilgi;
	}

	@Column(name = COLUMN_NAME_HAFTA)
	public String getHaftaBilgi() {
		return haftaBilgi;
	}

	public void setHaftaBilgi(String haftaBilgi) {
		this.haftaBilgi = haftaBilgi;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = COLUMN_NAME_UPDATE_SP)
	public Boolean getUpdateSP() {
		return updateSP;
	}

	public void setUpdateSP(Boolean updateSP) {
		this.updateSP = updateSP;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public Boolean getStart() {
		return start;
	}

	public void setStart(Boolean start) {
		this.start = start;
	}

	public void entityRefresh() {
		// TODO Auto-generated method stub

	}

}