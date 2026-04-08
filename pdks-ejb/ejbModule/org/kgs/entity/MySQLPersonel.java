package org.kgs.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.pdks.session.PdksUtil;

@Entity(name = MySQLPersonel.TABLE_NAME)
public class MySQLPersonel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7372405329789736986L;
	public static final String TABLE_NAME = "MYSQL_PERSONEL";
	public static final String COLUMN_NAME_ID = "PersonelId";
	public static final String COLUMN_NAME_ADI = "Adi";
	public static final String COLUMN_NAME_SOYADI = "Soyadi";
	public static final String COLUMN_NAME_SICIL_NO = "SicilNo";
	public static final String COLUMN_NAME_KART_NO = "KartId";
	public static final String COLUMN_NAME_KIMLIK_NO = "KimlikNo";
	public static final String COLUMN_NAME_ISE_GIRIS_TARIHI = "IsGirisTarihi";
	public static final String COLUMN_NAME_ISTEN_CIKIS_TARIHI = "IsCikisTarihi";
	public static final String COLUMN_NAME_DOGUM_TARIHI = "DogumTarihi";

	private Long id;
	private String adi, soyadi, sicilNo, kartId, kimlikNo;
	private Date isGirisTarihi, isCikisTarihi, dogumTarihi;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = COLUMN_NAME_ADI)
	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	@Column(name = COLUMN_NAME_SOYADI)
	public String getSoyadi() {
		return soyadi;
	}

	public void setSoyadi(String soyadi) {
		this.soyadi = soyadi;
	}

	@Column(name = COLUMN_NAME_SICIL_NO)
	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	@Column(name = COLUMN_NAME_KART_NO)
	public String getKartId() {
		return kartId;
	}

	public void setKartId(String kartId) {
		this.kartId = kartId;
	}

	@Column(name = COLUMN_NAME_KIMLIK_NO)
	public String getKimlikNo() {
		return kimlikNo;
	}

	public void setKimlikNo(String kimlikNo) {
		this.kimlikNo = kimlikNo;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_ISE_GIRIS_TARIHI)
	public Date getIsGirisTarihi() {
		return isGirisTarihi;
	}

	public void setIsGirisTarihi(Date isGirisTarihi) {
		this.isGirisTarihi = isGirisTarihi;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_ISTEN_CIKIS_TARIHI)
	public Date getIsCikisTarihi() {
		return isCikisTarihi;
	}

	public void setIsCikisTarihi(Date isCikisTarihi) {
		this.isCikisTarihi = isCikisTarihi;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_DOGUM_TARIHI)
	public Date getDogumTarihi() {
		return dogumTarihi;
	}

	public void setDogumTarihi(Date dogumTarihi) {
		this.dogumTarihi = dogumTarihi;
	}

	@Transient
	public String getAdiSoyadi() {
		String adSoyad = PdksUtil.getAdSoyad(adi, soyadi);
		return adSoyad;
	}

}
