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

	private Long sirketId, tesisId;
	private Integer tipNo;

	private Sirket sirket;
	private Tanim tesis;
	private Vardiya vardiya;
	private BigDecimal deger;
	private Boolean durum;
	private PuantajKatSayiTipi tipi;

	public KatSayi() {
		super();
	}

	@Column(name = COLUMN_NAME_TIPI)
	public Integer getTipNo() {
		return tipNo;
	}

	public void setTipNo(Integer value) {
		this.tipi = value != null ? PuantajKatSayiTipi.fromValue(value) : null;
		this.tipNo = value;
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
	public PuantajKatSayiTipi getTipi() {

		return tipi;
	}

	public void setTipi(PuantajKatSayiTipi tipi) {
		this.tipi = tipi;
	}

	@Transient
	public String getTipAciklama() {
		String aciklama = "";
		if (tipNo != null) {
			Integer key = tipNo;
			if (key.equals(PuantajKatSayiTipi.AYLIK_SUA_GUNLUK_SAAT_SURESI.value()))
				aciklama = "Şua Günlük Saat";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_SAATLIK_GUN_HESAP_TIPI.value()))
				aciklama = "Aylık Günlük Saat Hesap Tipi";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_HAREKET_BEKLEME_SURESI.value()))
				aciklama = "Hareket Bekleme Süresi";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_YUVARLAMA_TIPI.value()))
				aciklama = "Aylık Yuvarlama Tipi";
			else if (key.equals(PuantajKatSayiTipi.GUN_OFF_FAZLA_MESAI_TIPI.value()))
				aciklama = "Off Mesai Başlama Dakikası";
			else if (key.equals(PuantajKatSayiTipi.GUN_HT_FAZLA_MESAI_TIPI.value()))
				aciklama = "Hafta Tatili Mesai Başlama Dakikası";
			else if (key.equals(PuantajKatSayiTipi.GUN_FMT_DURUM.value()))
				aciklama = "Fazla Mesai Talep Oluşturma Durumu";
			else if (key.equals(PuantajKatSayiTipi.GUN_VARDIYA_MOLA.value()))
				aciklama = "Vardiya Mola Tarih Başlangıç Tarihi";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_DENKLESTIRME_TIPI.value()))
				aciklama = "Denkleştirme Tipi";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_RADYOLOJI_MAX_GUN.value()))
				aciklama = "Aylık Radyoloji Max Çalışma Gün Sayısı";
			else if (key.equals(PuantajKatSayiTipi.GUN_SAAT_CALISAN_IZIN_GUN.value()))
				aciklama = "İdari Çalışan  Günlük İzin Saati";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_FAZLA_MESAI_YUVARLAMA.value()))
				aciklama = "Fazla Mesai Yuvarlama Tipi";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_IZIN_HAFTA_TATIL_DURUM.value()))
				aciklama = "İzin Hafta Tatil Pazar";
			else if (key.equals(PuantajKatSayiTipi.GUN_SAAT_CALISAN_GUN.value()))
				aciklama = "Saat Çalışan Gün Katsayısı ";
			else if (key.equals(PuantajKatSayiTipi.GUN_SAAT_CALISAN_NORMAL_GUN.value()))
				aciklama = "Saat Çalışan Normal Gün Katsayısı ";
			else if (key.equals(PuantajKatSayiTipi.GUN_YEMEK_SURE_EKLE_DURUM.value()))
				aciklama = "Yemek Süre Ekle";
			else if (key.equals(PuantajKatSayiTipi.GUN_ERKEN_CIKIS_TIPI.value()))
				aciklama = "Vardiya Erken Çıkış Dakika";
			else if (key.equals(PuantajKatSayiTipi.GUN_ERKEN_GIRIS_TIPI.value()))
				aciklama = "Vardiya Erken Giriş Dakika";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_BAYRAM_AYIR.value()))
				aciklama = "Bayram Mesai Ayır";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_CIHAZ_ZAMAN_SANIYE_SIFIRLA.value()))
				aciklama = "Cihaz Saniye Sıfırla";
			else if (key.equals(PuantajKatSayiTipi.GUN_GEC_CIKIS_TIPI.value()))
				aciklama = "Vardiya Geç Çıkış Dakika";
			else if (key.equals(PuantajKatSayiTipi.GUN_GEC_GIRIS_TIPI.value()))
				aciklama = "Vardiya Geç Giriş Dakika";
			else if (key.equals(PuantajKatSayiTipi.GUN_GEBE_PLAN_KONTROL_ETME.value()))
				aciklama = "Vardiya Plan Saati Gebe Kontrol Etme";
			else if (key.equals(PuantajKatSayiTipi.GUN_SUT_IZIN_PLAN_KONTROL_ETME.value()))
				aciklama = "Vardiya Plan Saati Süt İzin Kontrol Etme";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_RT_YUVARLAMA.value()))
				aciklama = "Resmi Tatil Yuvarlama Tipi";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_UOM_YUVARLAMA.value()))
				aciklama = "Ücreti Ödenen Mesai Yuvarlama Tipi";
			else if (key.equals(PuantajKatSayiTipi.AYLIK_RT_KANUNEN_EKLEME.value()))
				aciklama = "Resmi Tatil 7.5 Saat Yuvarlama";
			else if (key.equals(PuantajKatSayiTipi.GUN_FAZLA_MESAI_ENTEGRASYON_MAIL_KAPAT.value()))
				aciklama = "Fazla Mesai Entegrasyon Mail Kapatma";
			else if (key.equals(PuantajKatSayiTipi.GUN_ICAP_KATSAYI.value()))
				aciklama = "İcap Yasal Kat Sayı";
			else if (key.equals(PuantajKatSayiTipi.GUN_ICAP_NORMAL.value()))
				aciklama = "İcap Normal Süre (Saat)";
			else if (key.equals(PuantajKatSayiTipi.GUN_ICAP_TATIL.value()))
				aciklama = "İcap Tatil Süre (Saat)";
			else
				aciklama = tipNo + " tanımsız tip";

		}
		return aciklama;
	}

	public void entityRefresh() {

	}

	@Transient
	public String getTableName() {
		return TABLE_NAME;
	}

}
