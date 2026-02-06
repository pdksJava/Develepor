package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.session.PdksUtil;

@Entity(name = SirketEntegrasyon.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { SirketEntegrasyon.COLUMN_NAME_SIRKET }) })
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

	private Sirket sirket;

	private String urlPersonel, urlIzin, urlMesai;
	private Date guncelemeZamaniPersonel, guncelemeZamaniIzin;

	public SirketEntegrasyon() {
		super();

	}

	public SirketEntegrasyon(Sirket sirket) {
		super();
		this.sirket = sirket;
	}

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

	public void setUrlPersonel(String value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isStrDegisti(urlPersonel, value));
		this.urlPersonel = value;
	}

	@Column(name = COLUMN_NAME_URL_IZIN)
	public String getUrlIzin() {
		return urlIzin;
	}

	public void setUrlIzin(String value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isStrDegisti(urlIzin, value));
		this.urlIzin = value;
	}

	@Column(name = COLUMN_NAME_URL_MESAI)
	public String getUrlMesai() {
		return urlMesai;
	}

	public void setUrlMesai(String value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isStrDegisti(urlMesai, value));
		this.urlMesai = value;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_ZAMANI_PERSONEL)
	public Date getGuncelemeZamaniPersonel() {
		return guncelemeZamaniPersonel;
	}

	public void setGuncelemeZamaniPersonel(Date value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isDateDegisti(guncelemeZamaniPersonel, value));
		this.guncelemeZamaniPersonel = value;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_ZAMANI_IZIN)
	public Date getGuncelemeZamaniIzin() {
		return guncelemeZamaniIzin;
	}

	public void setGuncelemeZamaniIzin(Date value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isDateDegisti(guncelemeZamaniIzin, value));
		this.guncelemeZamaniIzin = value;
	}

	public void entityRefresh() {

	}
}
