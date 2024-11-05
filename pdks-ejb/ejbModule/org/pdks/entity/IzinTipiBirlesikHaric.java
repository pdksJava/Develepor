package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = IzinTipiBirlesikHaric.TABLE_NAME)
public class IzinTipiBirlesikHaric extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 56230242920057020L;
	public static final String TABLE_NAME = "IZINTIPIBIRLESIKHARIC";
	
	public static final String COLUMN_NAME_IZIN_ARALIK_SAAT = "IZIN_ARALIK_SAAT";
	public static final String COLUMN_NAME_BIRLESIK_IZIN_TIPI = "BIRLESIK_IZIN_TIPI_ID";
	public static final String COLUMN_NAME_IZIN_TIPI = "IZIN_TIPI_ID";

	private Tanim izinTipiTanim;
	private Tanim birlesikIzinTipiTanim;
	private double izinAralikSaat;

	private Integer version = 0;

	@Column(name = "VERSION")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_BIRLESIK_IZIN_TIPI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getBirlesikIzinTipiTanim() {
		return birlesikIzinTipiTanim;
	}

	public void setBirlesikIzinTipiTanim(Tanim birlesikIzinTipiTanim) {
		this.birlesikIzinTipiTanim = birlesikIzinTipiTanim;
	}

	@Column(name = COLUMN_NAME_IZIN_ARALIK_SAAT)
	public double getIzinAralikSaat() {
		return izinAralikSaat;
	}

	public void setIzinAralikSaat(double izinAralikSaat) {
		this.izinAralikSaat = izinAralikSaat;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_IZIN_TIPI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getIzinTipiTanim() {
		return izinTipiTanim;
	}

	public void setIzinTipiTanim(Tanim izinTipiTanim) {
		this.izinTipiTanim = izinTipiTanim;
	}

	public void entityRefresh() {
		// TODO entityRefresh

	}

}
