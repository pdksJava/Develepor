package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = SirketEntegrasyon.TABLE_NAME)
public class SirketEntegrasyon extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3061455027464207804L;
	static Logger logger = Logger.getLogger(SirketEntegrasyon.class);
	public static final String TABLE_NAME = "SIRKET_ENTEGRASYON";
	public static final String COLUMN_NAME_SIRKET = "SIRKET_ID";
	public static final String COLUMN_NAME_URL_PERSONEL = "URL_PERSONEL";
	public static final String COLUMN_NAME_URL_IZIN = "URL_IZIN";
	public static final String COLUMN_NAME_URL_MESAI = "URL_MESAI";
	public static final String COLUMN_NAME_GUNCELLEME_ZAMANI_PERSONEL = "GUNCELLEME_ZAMANI_PERSONEL";
	public static final String COLUMN_NAME_GUNCELLEME_ZAMANI_IZIN = "GUNCELLEME_ZAMANI_IZIN";
	public static final String COLUMN_NAME_GUNCELLEME_ZAMANI_MESAI = "GUNCELLEME_ZAMANI_MESAI";

	private Sirket sirket;

	private String urlPersonel, urlIzin, urlMesai;
	private Date guncelemeZamaniPersonel, guncelemeZamaniIzin, guncelemeZamaniMesai;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_SIRKET)
	@Fetch(FetchMode.JOIN)
	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	@Column(name = COLUMN_NAME_URL_PERSONEL)
	public String getUrlPersonel() {
		return urlPersonel;
	}

	public void setUrlPersonel(String urlPersonel) {
		this.urlPersonel = urlPersonel;
	}

	@Column(name = COLUMN_NAME_URL_IZIN)
	public String getUrlIzin() {
		return urlIzin;
	}

	public void setUrlIzin(String urlIzin) {
		this.urlIzin = urlIzin;
	}

	@Column(name = COLUMN_NAME_URL_MESAI)
	public String getUrlMesai() {
		return urlMesai;
	}

	public void setUrlMesai(String urlMesai) {
		this.urlMesai = urlMesai;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_ZAMANI_PERSONEL)
	public Date getGuncelemeZamaniPersonel() {
		return guncelemeZamaniPersonel;
	}

	public void setGuncelemeZamaniPersonel(Date guncelemeZamaniPersonel) {
		this.guncelemeZamaniPersonel = guncelemeZamaniPersonel;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_ZAMANI_IZIN)
	public Date getGuncelemeZamaniIzin() {
		return guncelemeZamaniIzin;
	}

	public void setGuncelemeZamaniIzin(Date guncelemeZamaniIzin) {
		this.guncelemeZamaniIzin = guncelemeZamaniIzin;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_ZAMANI_MESAI)
	public Date getGuncelemeZamaniMesai() {
		return guncelemeZamaniMesai;
	}

	public void setGuncelemeZamaniMesai(Date guncelemeZamaniMesai) {
		this.guncelemeZamaniMesai = guncelemeZamaniMesai;
	}

	public void entityRefresh() {

	}
}
