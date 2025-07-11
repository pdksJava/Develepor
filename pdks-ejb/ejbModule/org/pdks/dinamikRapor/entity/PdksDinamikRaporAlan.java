package org.pdks.dinamikRapor.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.dinamikRapor.enums.ENumAlanHizalaTipi;
import org.pdks.dinamikRapor.enums.ENumBaslik;
import org.pdks.dinamikRapor.enums.ENumRaporAlanTipi;
import org.pdks.entity.BasePDKSObject;
import org.pdks.session.PdksUtil;

@Entity(name = PdksDinamikRaporAlan.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PdksDinamikRaporAlan.COLUMN_NAME_DINAMIK_RAPOR, PdksDinamikRaporAlan.COLUMN_NAME_DB_TANIM }) })
public class PdksDinamikRaporAlan extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3522453045311584506L;

	public static final String TABLE_NAME = "PDKS_DINAMIK_RAPOR_ALAN";

	public static final String COLUMN_NAME_DINAMIK_RAPOR = "DINAMIK_RAPOR_ID";
	public static final String COLUMN_NAME_DB_TANIM = "DB_TANIM";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_SIRA = "SIRA";
	public static final String COLUMN_NAME_ALAN_TIPI = "ALAN_TIPI";
	public static final String COLUMN_NAME_FILTER = "FILTER";
	public static final String COLUMN_NAME_GOSTER = "GOSTER";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	public static final String COLUMN_NAME_HIZALA = "HIZALA";

	private PdksDinamikRapor pdksDinamikRapor;

	private String aciklama, dbTanim;

	private ENumRaporAlanTipi raporAlanTipi;

	private Integer alanTipiId, sira;

	private Boolean durum = Boolean.TRUE, goster = Boolean.TRUE, filter = Boolean.FALSE;

	private Integer hizala = ENumAlanHizalaTipi.SOLA.value();
	private ENumAlanHizalaTipi alanHizalaTipi = ENumAlanHizalaTipi.SOLA;
	private ENumBaslik baslik;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DINAMIK_RAPOR, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PdksDinamikRapor getPdksDinamikRapor() {
		return pdksDinamikRapor;
	}

	public void setPdksDinamikRapor(PdksDinamikRapor pdksDinamikRapor) {
		this.pdksDinamikRapor = pdksDinamikRapor;
	}

	@Column(name = COLUMN_NAME_SIRA)
	public Integer getSira() {
		return sira;
	}

	public void setSira(Integer sira) {
		this.sira = sira;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String value) {
		if (PdksUtil.hasStringValue(value))
			baslik = ENumBaslik.fromValue(value);
		this.aciklama = value;
	}

	@Column(name = COLUMN_NAME_DB_TANIM)
	public String getDbTanim() {
		return dbTanim;
	}

	public void setDbTanim(String dbTanim) {
		this.dbTanim = dbTanim;
	}

	@Column(name = COLUMN_NAME_ALAN_TIPI)
	public Integer getAlanTipiId() {
		return alanTipiId;
	}

	public void setAlanTipiId(Integer value) {
		this.raporAlanTipi = null;
		if (value != null)
			this.raporAlanTipi = ENumRaporAlanTipi.fromValue(value);
		this.alanTipiId = value;
	}

	@Column(name = COLUMN_NAME_HIZALA)
	public Integer getHizala() {
		return hizala;
	}

	public void setHizala(Integer value) {
		this.alanHizalaTipi = null;
		if (value != null)
			alanHizalaTipi = ENumAlanHizalaTipi.fromValue(value);
		this.hizala = value;
	}

	@Column(name = COLUMN_NAME_FILTER)
	public Boolean getFilter() {
		return filter;
	}

	public void setFilter(Boolean filter) {
		this.filter = filter;
	}

	@Column(name = COLUMN_NAME_GOSTER)
	public Boolean getGoster() {
		return goster;
	}

	public void setGoster(Boolean goster) {
		this.goster = goster;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public boolean isHizalaSola() {
		return hizala != null && hizala.equals(ENumAlanHizalaTipi.SOLA.value());
	}

	@Transient
	public boolean isHizalaOrtala() {
		return hizala != null && hizala.equals(ENumAlanHizalaTipi.ORTALA.value());
	}

	@Transient
	public boolean isHizalaSaga() {
		return hizala != null && hizala.equals(ENumAlanHizalaTipi.SAGA.value());
	}

	@Transient
	public boolean isSaat() {
		return alanTipiId != null && alanTipiId.equals(ENumRaporAlanTipi.SAAT.value());
	}

	@Transient
	public boolean isTarihSaat() {
		return alanTipiId != null && alanTipiId.equals(ENumRaporAlanTipi.TARIH_SAAT.value());
	}

	@Transient
	public boolean isMantiksal() {
		return alanTipiId != null && alanTipiId.equals(ENumRaporAlanTipi.MANTIKSAL.value());
	}

	@Transient
	public String getPdksDinamikRaporAlanhHizalaAciklama() {
		return PdksDinamikRapor.getPdksDinamikRaporAlanhHizalaAciklama(hizala);
	}

	@Transient
	public String getPdksDinamikRaporAlanHizala() {
		String str = "";
		if (hizala != null) {
			if (hizala.equals(ENumAlanHizalaTipi.SAGA.value()))
				str = "right";
			else if (hizala.equals(ENumAlanHizalaTipi.SOLA.value()))
				str = "left";
			else if (hizala.equals(ENumAlanHizalaTipi.ORTALA.value()))
				str = "center";
		}
		return str;
	}

	@Transient
	public String getPdksRaporAlanTipiAciklama() {
		return PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(alanTipiId);
	}

	@Transient
	public boolean isKarakter() {
		return alanTipiId != null && alanTipiId.equals(ENumRaporAlanTipi.KARAKTER.value());
	}

	@Transient
	public boolean isSayisal() {
		return alanTipiId != null && alanTipiId.equals(ENumRaporAlanTipi.SAYISAL.value());
	}

	@Transient
	public boolean isTarih() {
		return alanTipiId != null && alanTipiId.equals(ENumRaporAlanTipi.TARIH.value());
	}

	@Transient
	public ENumRaporAlanTipi getRaporAlanTipi() {
		return raporAlanTipi;
	}

	public void setRaporAlanTipi(ENumRaporAlanTipi raporAlanTipi) {
		this.raporAlanTipi = raporAlanTipi;
	}

	@Transient
	public ENumAlanHizalaTipi getAlanHizalaTipi() {
		return alanHizalaTipi;
	}

	public void setAlanHizalaTipi(ENumAlanHizalaTipi alanHizalaTipi) {
		this.alanHizalaTipi = alanHizalaTipi;
	}

	@Transient
	public ENumBaslik getBaslik() {
		return baslik;
	}

	public void setBaslik(ENumBaslik baslik) {
		this.baslik = baslik;
	}

	public void entityRefresh() {

	}

	@Transient
	public Object clone() {
		BasePDKSObject object = null;
		try {
			object = (BasePDKSObject) super.clone();
			object.setId(null);
		} catch (CloneNotSupportedException e) {

		}
		return object;
	}

}
