package org.pdks.entity;

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

@Entity(name = CalismaModeliAy.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { CalismaModeliAy.COLUMN_NAME_DONEM, CalismaModeliAy.COLUMN_NAME_CALISMA_MODELI }) })
public class CalismaModeliAy extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4015750209129001721L;
	public static final String TABLE_NAME = "CALISMA_MODELI_AY";

	public static final String COLUMN_NAME_DONEM = "DONEM_ID";
	public static final String COLUMN_NAME_CALISMA_MODELI = "CALISMA_MODELI_ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_SURE = "SURE";
	public static final String COLUMN_NAME_OTOMATIK_FAZLA_CALISMA_ONAYLANSIN = "OTOMATIK_FAZLA_CALISMA_ONAYLANSIN";
	public static final String COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL = "HAREKET_KAYDI_VARDIYA_BUL";
	public static final String COLUMN_NAME_GUN_MAX_CALISMA_SURESI_ODENIR = "GUN_MAX_CALISMA_SURESI_ODENIR";
	public static final String COLUMN_NAME_HAFTA_TATIL_HAREKET_GUNCELLE = "HT_HAREKET_GUNCELLE";
	public static final String COLUMN_NAME_OFF_HAREKET_GUNCELLE = "OFF_HAREKET_GUNCELLE";

	private DenklestirmeAy denklestirmeAy;

	private CalismaModeli calismaModeli;

	private double sure = 0, toplamIzinSure = 0, negatifBakiyeDenkSaat = 0.0d;

	private Boolean durum = Boolean.TRUE, hareketKaydiVardiyaBul = Boolean.FALSE, otomatikFazlaCalismaOnaylansin = Boolean.FALSE, gunMaxCalismaOdemeDurum = Boolean.TRUE;
	private Boolean haftaTatilHareketGuncelle = Boolean.FALSE, offHareketGuncelle = Boolean.FALSE;

	public CalismaModeliAy() {
		super();
	}

	public CalismaModeliAy(DenklestirmeAy denklestirmeAy, CalismaModeli cm) {
		super();
		this.denklestirmeAy = denklestirmeAy;
		this.calismaModeli = cm;
		this.setDurum(Boolean.TRUE);
		if (cm != null) {
			this.negatifBakiyeDenkSaat = cm.getNegatifBakiyeDenkSaat();
			this.hareketKaydiVardiyaBul = cm.getHareketKaydiVardiyaBul();
			this.otomatikFazlaCalismaOnaylansin = cm.getOtomatikFazlaCalismaOnaylansin();
			this.gunMaxCalismaOdemeDurum = cm.isGunMaxCalismaOdenir();
			if (cm.isHareketKaydiVardiyaBulsunmu()) {
				this.haftaTatilHareketGuncelle = cm.getHaftaTatilHareketGuncelle();
				this.offHareketGuncelle = cm.getOffHareketGuncelle();
			}
		}
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DONEM)
	@Fetch(FetchMode.JOIN)
	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_CALISMA_MODELI)
	@Fetch(FetchMode.JOIN)
	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	@Column(name = "SURE")
	public double getSure() {
		return sure;
	}

	public void setSure(double sure) {
		this.sure = sure;
	}

	@Column(name = "SUT_IZNI_SURE")
	public double getToplamIzinSure() {
		return toplamIzinSure;
	}

	public void setToplamIzinSure(double toplamIzinSure) {
		this.toplamIzinSure = toplamIzinSure;
	}

	@Column(name = "NEGATIF_BAKIYE_SAAT")
	public double getNegatifBakiyeDenkSaat() {
		return negatifBakiyeDenkSaat;
	}

	public void setNegatifBakiyeDenkSaat(double negatifBakiyeDenkSaat) {
		this.negatifBakiyeDenkSaat = negatifBakiyeDenkSaat;
	}

	@Column(name = COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL)
	public Boolean getHareketKaydiVardiyaBul() {
		return hareketKaydiVardiyaBul;
	}

	public void setHareketKaydiVardiyaBul(Boolean hareketKaydiVardiyaBul) {
		this.hareketKaydiVardiyaBul = hareketKaydiVardiyaBul;
	}

	@Column(name = COLUMN_NAME_OTOMATIK_FAZLA_CALISMA_ONAYLANSIN)
	public Boolean getOtomatikFazlaCalismaOnaylansin() {
		return otomatikFazlaCalismaOnaylansin;
	}

	public void setOtomatikFazlaCalismaOnaylansin(Boolean otomatikFazlaCalismaOnaylansin) {
		this.otomatikFazlaCalismaOnaylansin = otomatikFazlaCalismaOnaylansin;
	}

	@Column(name = COLUMN_NAME_GUN_MAX_CALISMA_SURESI_ODENIR)
	public Boolean getGunMaxCalismaOdemeDurum() {
		return gunMaxCalismaOdemeDurum;
	}

	public void setGunMaxCalismaOdemeDurum(Boolean gunMaxCalismaOdemeDurum) {
		this.gunMaxCalismaOdemeDurum = gunMaxCalismaOdemeDurum;
	}

	@Column(name = COLUMN_NAME_HAFTA_TATIL_HAREKET_GUNCELLE)
	public Boolean getHaftaTatilHareketGuncelle() {
		return haftaTatilHareketGuncelle;
	}

	public void setHaftaTatilHareketGuncelle(Boolean haftaTatilHareketGuncelle) {
		this.haftaTatilHareketGuncelle = haftaTatilHareketGuncelle;
	}

	@Column(name = COLUMN_NAME_OFF_HAREKET_GUNCELLE)
	public Boolean getOffHareketGuncelle() {
		return offHareketGuncelle;
	}

	public void setOffHareketGuncelle(Boolean offHareketGuncelle) {
		this.offHareketGuncelle = offHareketGuncelle;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public static String getKey(DenklestirmeAy xDenklestirmeAy, CalismaModeli xCalismaModeli) {
		String key = (xDenklestirmeAy != null ? xDenklestirmeAy.getId() : 0) + "_" + (xCalismaModeli != null ? xCalismaModeli.getId() : 0);
		return key;
	}

	@Transient
	public boolean isHareketKaydiVardiyaBulsunmu() {
		return hareketKaydiVardiyaBul != null && hareketKaydiVardiyaBul.booleanValue();
	}

	@Transient
	public boolean isOtomatikFazlaCalismaOnaylansinmi() {
		return otomatikFazlaCalismaOnaylansin != null && otomatikFazlaCalismaOnaylansin.booleanValue();
	}

	@Transient
	public String getKey() {
		String key = getKey(denklestirmeAy, calismaModeli);
		return key;
	}

	@Transient
	public String getAciklama() {
		String key = calismaModeli != null ? calismaModeli.getAciklama() : "";
		return key;
	}

	@Transient
	public String getSortAciklama() {
 		String key = "";
		if (calismaModeli != null) {
			key += calismaModeli.getDepartman() != null ? calismaModeli.getDepartman().getAciklama() + "_" : "";
			key += calismaModeli.getSirket() != null ? calismaModeli.getSirket().getAd()+ "_" : "";
			key += calismaModeli.getTesis() != null ? calismaModeli.getTesis().getAciklama()+ "_" : "";
			key += calismaModeli.getAciklama();
		}

		return key;
	}

	@Transient
	public boolean isGunMaxCalismaOdenir() {
		return gunMaxCalismaOdemeDurum != null && gunMaxCalismaOdemeDurum;
	}

	public void entityRefresh() {

	}

}
