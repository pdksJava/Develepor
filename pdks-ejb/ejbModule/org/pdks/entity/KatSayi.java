package org.pdks.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.enums.PuantajKatSayiTipi;

@Entity
@Table(name = KatSayi.TABLE_NAME)
public class KatSayi extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2957604543022120666L;

	/**
	 * 
	 */

	static Logger logger = Logger.getLogger(KatSayi.class);

	public static final String TABLE_NAME = "KAT_SAYI";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_TIPI = "TIPI";
	public static final String COLUMN_NAME_BAS_TARIH = "BAS_TARIH";
	public static final String COLUMN_NAME_BIT_TARIH = "BIT_TARIH";
	public static final String COLUMN_NAME_DEGER = "DEGER";
	public static final String COLUMN_NAME_SIRKET = "SIRKET_ID";
	public static final String COLUMN_NAME_TESIS = "TESIS_ID";
	public static final String COLUMN_NAME_VARDIYA = "VARDIYA_ID";

	private Date basTarih, bitTarih;
	private PuantajKatSayiTipi tipi;
	private Long sirketId, tesisId;
	private Integer tipNo;

	private Sirket sirket;
	private Tanim tesis;
	private Vardiya vardiya;
	private BigDecimal deger;
	private Boolean durum;

	public KatSayi() {
		super();
	}

	@Column(name = COLUMN_NAME_TIPI)
	public Integer getTipNo() {
		return tipNo;
	}

	public void setTipNo(Integer tipNo) {
		this.tipNo = tipNo;
	}

	@Column(name = COLUMN_NAME_TIPI, updatable = false, insertable = false)
	public PuantajKatSayiTipi getTipi() {
		return tipi;
	}

	public void setTipi(PuantajKatSayiTipi tipi) {
		this.tipi = tipi;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BAS_TARIH)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BIT_TARIH)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	@Column(name = COLUMN_NAME_DEGER)
	public BigDecimal getDeger() {
		return deger;
	}

	public void setDeger(BigDecimal deger) {
		this.deger = deger;
	}

	@Column(name = COLUMN_NAME_SIRKET)
	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_SIRKET, updatable = false, insertable = false)
	@Fetch(FetchMode.JOIN)
	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	@Column(name = COLUMN_NAME_TESIS)
	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_TESIS, updatable = false, insertable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getTesis() {
		return tesis;
	}

	public void setTesis(Tanim tesis) {
		this.tesis = tesis;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA)
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya() {
		return vardiya;
	}

	public void setVardiya(Vardiya value) {

		this.vardiya = value;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public String getTipAciklama() {
		String aciklama = "";
		if (tipi != null) {
			aciklama = tipi.value() + " tanımsız tip";
			if (tipi.equals(PuantajKatSayiTipi.AYLIK_SUA_GUNLUK_SAAT_SURESI))
				aciklama = "Şua Günlük Saat";
			else if (tipi.equals(PuantajKatSayiTipi.GUN_OFF_FAZLA_MESAI_TIPI))
				aciklama = "Off Mesai Başlama Dakikası";
			else if (tipi.equals(PuantajKatSayiTipi.AYLIK_IZIN_HAFTA_TATIL_DURUM))
				aciklama = "İzin Hafta Tatil Pazar";
			else if (tipi.equals(PuantajKatSayiTipi.GUN_YEMEK_SURE_EKLE_DURUM))
				aciklama = "Yemek Süre Ekle";
			else if (tipi.equals(PuantajKatSayiTipi.GUN_ERKEN_CIKIS_TIPI))
				aciklama = "Vardiya Erken Çıkış Dakika";
			else if (tipi.equals(PuantajKatSayiTipi.GUN_ERKEN_GIRIS_TIPI))
				aciklama = "Vardiya Erken Giriş Dakika";
			else if (tipi.equals(PuantajKatSayiTipi.AYLIK_BAYRAM_AYIR))
				aciklama = "Bayram Mesai Ayır";
			else if (tipi.equals(PuantajKatSayiTipi.AYLIK_CIHAZ_ZAMAN_SANIYE_SIFIRLA))
				aciklama = "Cihaz Saniye Sıfırla";
			else if (tipi.equals(PuantajKatSayiTipi.GUN_GEC_CIKIS_TIPI))
				aciklama = "Vardiya Geç Çıkış Dakika";
			else if (tipi.equals(PuantajKatSayiTipi.GUN_GEC_GIRIS_TIPI))
				aciklama = "Vardiya Geç Giriş Dakika";

		}
		return aciklama;
	}

	public void entityRefresh() {

	}

}
