package org.pdks.dinamikRapor.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
import org.pdks.entity.BasePDKSObject;

@Entity(name = PdksDinamikRaporParametre.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PdksDinamikRaporParametre.COLUMN_NAME_DINAMIK_RAPOR, PdksDinamikRaporParametre.COLUMN_NAME_DB_TANIM }) })
public class PdksDinamikRaporParametre extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7408349167376237472L;

	public static final String TABLE_NAME = "PDKS_DINAMIK_RAPOR_PARAMETRE";
	public static final String COLUMN_NAME_DINAMIK_RAPOR = "DINAMIK_RAPOR_ID";
	public static final String COLUMN_NAME_DB_TANIM = "DB_TANIM";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_SIRA = "SIRA";
	public static final String COLUMN_NAME_ALAN_TIPI = "ALAN_TIPI";
	public static final String COLUMN_NAME_ZORUNLU = "ZORUNLU";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	private PdksDinamikRapor pdksDinamikRapor;

	private String aciklama, dbTanim;

	private PdksRaporAlanTipi raporAlanTipi;

	private Integer alanTipiId, sira;

	private Boolean durum = Boolean.TRUE;

	private Date tarihDeger;

	private String karakterDeger;

	private BigDecimal decimalDeger;

	private Double doubleDeger;
	private Boolean zorunlu = Boolean.TRUE;

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

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
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
			this.raporAlanTipi = PdksRaporAlanTipi.fromValue(value);
		this.alanTipiId = value;
	}

	@Column(name = COLUMN_NAME_ZORUNLU)
	public Boolean getZorunlu() {
		return zorunlu;
	}

	public void setZorunlu(Boolean zorunlu) {
		this.zorunlu = zorunlu;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public String getPdksRaporAlanTipiAciklama() {
		return PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(alanTipiId);
	}

	@Transient
	public boolean isKarakter() {
		return alanTipiId != null && alanTipiId.equals(PdksRaporAlanTipi.KARAKTER.value());
	}

	@Transient
	public boolean isSayisal() {
		return alanTipiId != null && alanTipiId.equals(PdksRaporAlanTipi.SAYISAL.value());
	}

	@Transient
	public boolean isTarih() {
		return alanTipiId != null && alanTipiId.equals(PdksRaporAlanTipi.TARIH.value());
	}

	@Transient
	public PdksRaporAlanTipi getRaporAlanTipi() {
		return raporAlanTipi;
	}

	public void setRaporAlanTipi(PdksRaporAlanTipi raporAlanTipi) {
		this.raporAlanTipi = raporAlanTipi;
	}

	@Transient
	public Date getTarihDeger() {
		return tarihDeger;
	}

	public void setTarihDeger(Date tarihDeger) {
		this.tarihDeger = tarihDeger;
	}

	@Transient
	public String getKarakterDeger() {
		return karakterDeger;
	}

	public void setKarakterDeger(String karakterDeger) {
		this.karakterDeger = karakterDeger;
	}

	@Transient
	public BigDecimal getDecimalDeger() {
		return decimalDeger;
	}

	public void setDecimalDeger(BigDecimal decimalDeger) {
		this.decimalDeger = decimalDeger;
	}

	@Transient
	public Double getDoubleDeger() {
		return doubleDeger;
	}

	public void setDoubleDeger(Double doubleDeger) {
		this.doubleDeger = doubleDeger;
	}

	public void entityRefresh() {
		// TODO Auto-generated method stub

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
