package org.pdks.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = YemekKartsiz.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { YemekKartsiz.COLUMN_NAME_TARIH, YemekKartsiz.COLUMN_NAME_SIRKET, YemekKartsiz.COLUMN_NAME_KAPI, YemekKartsiz.COLUMN_NAME_OGUN }) })
public class YemekKartsiz extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8560853194023608673L;

	public static final String TABLE_NAME = "YEMEK_KARTSIZ";
	public static final String COLUMN_NAME_TARIH = "TARIH";
	public static final String COLUMN_NAME_SIRKET = "SIRKET_ID";
	public static final String COLUMN_NAME_KAPI = "KAPI_ID";
	public static final String COLUMN_NAME_OGUN = "OGUN_ID";

	private YemekOgun yemekOgun;
	private Kapi yemekKapi;
	private Sirket sirket;
	private Date tarih;
	private Integer adet;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_OGUN)
	@Fetch(FetchMode.JOIN)
	public YemekOgun getYemekOgun() {
		return yemekOgun;
	}

	public void setYemekOgun(YemekOgun yemekOgun) {
		this.yemekOgun = yemekOgun;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_KAPI)
	@Fetch(FetchMode.JOIN)
	public Kapi getYemekKapi() {
		return yemekKapi;
	}

	public void setYemekKapi(Kapi yemekKapi) {
		this.yemekKapi = yemekKapi;
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

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_TARIH)
	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	@Column(name = "ADET")
	public Integer getAdet() {
		return adet;
	}

	public void setAdet(Integer adet) {
		this.adet = adet;
	}

	@Transient
	public HareketKGS getKgsHareket() {
		HareketKGS hareket = new HareketKGS();
		hareket.setId("KY" + id);
		hareket.setYemekOgun(yemekOgun);
		hareket.setZaman(tarih);
		hareket.setYemekYiyenSayisi(adet);
		hareket.setKapiView(yemekKapi.getKapiNewView());
		return hareket;
	}

	public void entityRefresh() {
		// TODO entityRefresh

	}

}
