package org.pdks.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.session.PdksUtil;

@Entity(name = PersonelFazlaMesai.TABLE_NAME)
public class PersonelFazlaMesai extends BaseObject implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8455550990499778693L;

	public static final String TABLE_NAME = "PERSONELFAZLAMESAI";
	public static final String COLUMN_NAME_VARDIYA_GUN = "VARDIYA_GUN";
	public static final String COLUMN_NAME_HAREKET = "HAREKET_ID";
	public static final String COLUMN_NAME_ONAY = "ONAY_ID";
	public static final String COLUMN_NAME_ONAY_DURUM = "ONAY_DURUM";
	public static final String COLUMN_NAME_TATIL_DURUM = "TATIL_DURUM";
	public static final String COLUMN_NAME_ONAY_ACIKLAMA = "ONAY_ACIKLAMA_ID";
	public static final String COLUMN_NAME_FAZLA_MESAI_TALEP = "FAZLA_MESAI_TALEP_ID";
	public static final String COLUMN_NAME_BAS_ZAMAN = "BAS_ZAMAN";
	public static final String COLUMN_NAME_BIT_ZAMAN = "BIT_ZAMAN";
	public static final String COLUMN_NAME_FAZLA_MESAI_SAATI = "FAZLA_MESAI_SAATI";
	// public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final int DURUM_ONAYLANMADI = 0;
	public static final int DURUM_ONAYLANDI = 1;

	public static final int ARIFE = 2;
	public static final int BAYRAM = 1;

	private String hareketId;

	private VardiyaGun vardiyaGun;

	private HareketKGS hareket;

	private Tanim fazlaMesaiOnayDurum;

	private Double fazlaMesaiSaati = 0.0d, fazlaMesaiMaxSaati;

	private Date basZaman, bitZaman;

	private FazlaMesaiTalep fazlaMesaiTalep;

	private OzelAciklama nedenOzelAciklama;

	private Integer onayDurum = DURUM_ONAYLANDI, tatilDurum;

	private String aciklama;

	@Column(name = COLUMN_NAME_HAREKET)
	public String getHareketId() {
		return hareketId;
	}

	public void setHareketId(String hareketId) {
		this.hareketId = hareketId;
	}

	@Transient
	public HareketKGS getHareket() {
		return hareket;
	}

	public void setHareket(HareketKGS hareket) {
		if (hareket != null && hareketId == null)
			hareketId = hareket.getId();
		this.hareket = hareket;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA_GUN)
	@Fetch(FetchMode.JOIN)
	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun value) {
		this.vardiyaGun = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_FAZLA_MESAI_TALEP)
	@Fetch(FetchMode.JOIN)
	public FazlaMesaiTalep getFazlaMesaiTalep() {
		return fazlaMesaiTalep;
	}

	public void setFazlaMesaiTalep(FazlaMesaiTalep fazlaMesaiTalep) {
		this.fazlaMesaiTalep = fazlaMesaiTalep;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BAS_ZAMAN, nullable = false)
	public Date getBasZaman() {
		return basZaman;
	}

	public void setBasZaman(Date basZaman) {
		this.basZaman = basZaman;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_BIT_ZAMAN, nullable = false)
	public Date getBitZaman() {
		return bitZaman;
	}

	public void setBitZaman(Date bitZaman) {
		this.bitZaman = bitZaman;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_ONAY)
	@Fetch(FetchMode.JOIN)
	public Tanim getFazlaMesaiOnayDurum() {
		return fazlaMesaiOnayDurum;
	}

	public void setFazlaMesaiOnayDurum(Tanim fazlaMesaiOnayDurum) {
		this.fazlaMesaiOnayDurum = fazlaMesaiOnayDurum;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_ONAY_ACIKLAMA)
	@Fetch(FetchMode.JOIN)
	public OzelAciklama getNedenOzelAciklama() {
		return nedenOzelAciklama;
	}

	public void setNedenOzelAciklama(OzelAciklama nedenOzelAciklama) {
		this.nedenOzelAciklama = nedenOzelAciklama;
	}

	@Column(name = COLUMN_NAME_ONAY_DURUM)
	public Integer getOnayDurum() {
		return onayDurum;
	}

	public void setOnayDurum(Integer onayDurum) {
		this.onayDurum = onayDurum;
	}

	@Column(name = COLUMN_NAME_TATIL_DURUM)
	public Integer getTatilDurum() {
		return tatilDurum;
	}

	public void setTatilDurum(Integer tatilDurum) {
		this.tatilDurum = tatilDurum;
	}

	@Column(name = COLUMN_NAME_FAZLA_MESAI_SAATI)
	public Double getFazlaMesaiSaati() {
		return fazlaMesaiSaati;
	}

	public void setFazlaMesaiSaati(Double fazlaMesaiSaati) {
		this.fazlaMesaiSaati = fazlaMesaiSaati;
	}

	// @Column(name = COLUMN_NAME_ACIKLAMA)
	@Transient
	public String getNedenAciklama() {
		String nedenAciklama = nedenOzelAciklama != null && PdksUtil.hasStringValue(nedenOzelAciklama.getAciklama()) ? nedenOzelAciklama.getAciklama().trim() : null;
		return nedenAciklama;
	}

	@Transient
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	// public void setNedenAciklama(String nedenAciklama) {
	// this.nedenAciklama = nedenAciklama;
	// }

	@Transient
	public String getOnayDurumAciklama() {
		String onayladimi = "";
		String mesaiSaati = "";
		try {
			if (onayDurum != null) {
				if (onayDurum == DURUM_ONAYLANDI) {
					onayladimi = getFazlaMesaiOnayDurum().getAciklama() + (nedenOzelAciklama != null ? " [ " + getNedenAciklama() + " ] " : "");
					mesaiSaati = " FazlaMesai Saati : " + getFazlaMesaiSaati();
				} else if (onayDurum == DURUM_ONAYLANMADI) {
					onayladimi = getFazlaMesaiOnayDurum().getAciklama();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return onayladimi + mesaiSaati;
	}

	@Transient
	public boolean isOnaylandi() {
		return onayDurum != null && onayDurum == DURUM_ONAYLANDI;
	}

	@Transient
	public boolean isArife() {
		return tatilDurum != null && tatilDurum == ARIFE;
	}

	@Transient
	public boolean isBayram() {
		return tatilDurum != null && tatilDurum == BAYRAM;
	}

	@Transient
	public Double getFazlaMesaiMaxSaati() {
		return fazlaMesaiMaxSaati;
	}

	public void setFazlaMesaiMaxSaati(Double fazlaMesaiMaxSaati) {
		this.fazlaMesaiMaxSaati = fazlaMesaiMaxSaati;
	}

	public void entityRefresh() {

	}

	public String getTableName() {
		return TABLE_NAME;
	}

}
