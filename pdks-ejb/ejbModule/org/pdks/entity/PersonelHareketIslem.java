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
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

@Entity(name = PersonelHareketIslem.TABLE_NAME)
@Immutable
public class PersonelHareketIslem extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1206902759557452794L;

	static Logger logger = Logger.getLogger(PersonelHareketIslem.class);

	public static final String TABLE_NAME = "PDKS_ISLEM";

	public static final String COLUMN_NAME_ZAMAN = "ZAMAN";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_ONAY_DURUM = "ONAY_DURUM";
	public static final String COLUMN_NAME_ISLEM_TIPI = "ISLEM_TIPI";
	public static final String COLUMN_NAME_OLUSTURMA_ZAMANI = "OLUSTURMA_ZAMANI";
	public static final String COLUMN_NAME_GUNCELLEYEN_PERSONEL = "GUNCELLEYEN_PERSONEL_ID";
	public static final String COLUMN_NAME_NEDEN = "NEDEN_ID";
	public static final String COLUMN_NAME_ONAYLAYAN = "ONAYLAYAN_ID";

	public static final int ONAY_DURUM_ISLEM_YAPILMADI = 0;
	public static final int ONAY_DURUM_ONAYLANDI = 1;
	public static final int ONAY_DURUM_ONAYLANMADI = 2;

	private String islemTipi, aciklama, orjinalId;

	private Date zaman, olusturmaZamani;

	private User guncelleyenUser, onaylayanUser;

	private Tanim neden;

	private int onayDurum;

	private boolean puantajOnayDurum = Boolean.TRUE;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_ZAMAN)
	public Date getZaman() {
		return this.zaman;
	}

	public void setZaman(Date vaue) {

		this.zaman = vaue;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_OLUSTURMA_ZAMANI)
	public Date getOlusturmaZamani() {
		return olusturmaZamani;
	}

	public void setOlusturmaZamani(Date olusturmaZamani) {
		this.olusturmaZamani = olusturmaZamani;
	}

	@Column(name = COLUMN_NAME_ONAY_DURUM)
	public int getOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(int onayDurum) {
		this.onayDurum = onayDurum;
	}

	@Column(name = COLUMN_NAME_ISLEM_TIPI)
	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_GUNCELLEYEN_PERSONEL)
	@Fetch(FetchMode.JOIN)
	public User getGuncelleyenUser() {
		return guncelleyenUser;
	}

	public void setGuncelleyenUser(User guncelleyenUser) {
		this.guncelleyenUser = guncelleyenUser;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_ONAYLAYAN)
	@Fetch(FetchMode.JOIN)
	public User getOnaylayanUser() {
		return onaylayanUser;
	}

	public void setOnaylayanUser(User onaylayanUser) {
		this.onaylayanUser = onaylayanUser;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_NEDEN)
	@Fetch(FetchMode.JOIN)
	public Tanim getNeden() {
		return neden;
	}

	public void setNeden(Tanim neden) {
		this.neden = neden;
	}

	@Transient
	public String getOrjinalId() {
		return orjinalId;
	}

	public void setOrjinalId(String orjinalId) {
		this.orjinalId = orjinalId;
	}

	@Transient
	public boolean isAciklamaVar() {
		return PdksUtil.hasStringValue(aciklama);
	}

	@Transient
	public String getOnayDurumAciklama() {
		String onayladimi = "";
		if (onayDurum == PersonelHareketIslem.ONAY_DURUM_ONAYLANDI) {
			onayladimi = "onaylandi";
		} else if (onayDurum == PersonelHareketIslem.ONAY_DURUM_ONAYLANMADI) {
			onayladimi = "onaylanmadi";
		} else if (onayDurum == PersonelHareketIslem.ONAY_DURUM_ISLEM_YAPILMADI) {
			onayladimi = "islem yapilmadi";
		}
		return onayladimi;
	}

	@Transient
	public boolean isPuantajOnayDurum() {
		return puantajOnayDurum;
	}

	public void setPuantajOnayDurum(boolean puantajOnayDurum) {
		this.puantajOnayDurum = puantajOnayDurum;
	}

	public void entityRefresh() {

	}

	@Transient
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		throw new InternalError();
	}

}
