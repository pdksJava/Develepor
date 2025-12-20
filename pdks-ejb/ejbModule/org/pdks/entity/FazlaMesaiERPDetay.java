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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.enums.MethodAlanAPI;

@Entity(name = FazlaMesaiERPDetay.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { FazlaMesaiERPDetay.COLUMN_NAME_FAZLA_MESAI_ERP, FazlaMesaiERPDetay.COLUMN_NAME_ALAN_TIPI }) })
public class FazlaMesaiERPDetay extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 929729411027043798L;
	static Logger logger = Logger.getLogger(FazlaMesaiERPDetay.class);
	public static final String TABLE_NAME = "FAZLA_MESAI_ERP_DETAY";
	public static final String COLUMN_NAME_FAZLA_MESAI_ERP = "FAZLA_MESAI_ERP";
	public static final String COLUMN_NAME_SIRA = "SIRA";
	public static final String COLUMN_NAME_ALAN_TIPI = "ALAN_TIPI";
	public static final String COLUMN_NAME_ALAN_ADI = "ALAN_ADI";
	public static final String COLUMN_NAME_ALAN_DEGER = "ALAN_DEGER";

	public static final String COLUMN_NAME_BASLIK_ALAN = "BASLIK_ALAN_DURUM";

	private FazlaMesaiERP fazlaMesaiERP;

	private Integer sira, indis;

	private String alanTipi, alanAdi, alanDeger;

	private boolean baslikAlan = Boolean.FALSE;

	private MethodAlanAPI methodAlanAPI;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = COLUMN_NAME_FAZLA_MESAI_ERP, nullable = false)
	@Fetch(FetchMode.JOIN)
	public FazlaMesaiERP getFazlaMesaiERP() {
		return fazlaMesaiERP;
	}

	public void setFazlaMesaiERP(FazlaMesaiERP fazlaMesaiERP) {
		this.fazlaMesaiERP = fazlaMesaiERP;
	}

	@Column(name = COLUMN_NAME_SIRA)
	public Integer getSira() {
		return sira;
	}

	public void setSira(Integer sira) {
		this.sira = sira;
	}

	@Column(name = COLUMN_NAME_ALAN_TIPI, nullable = false)
	public String getAlanTipi() {
		return alanTipi;
	}

	public void setAlanTipi(String value) {
		this.methodAlanAPI = value != null ? MethodAlanAPI.fromValue(value) : null;
		this.alanTipi = value;
	}

	@Column(name = COLUMN_NAME_ALAN_ADI, nullable = false)
	public String getAlanAdi() {
		return alanAdi;
	}

	public void setAlanAdi(String alanAdi) {
		this.alanAdi = alanAdi;
	}

	@Column(name = COLUMN_NAME_BASLIK_ALAN)
	public boolean isBaslikAlan() {
		return baslikAlan;
	}

	public void setBaslikAlan(boolean baslikAlan) {
		this.baslikAlan = baslikAlan;
	}

	@Column(name = COLUMN_NAME_ALAN_DEGER)
	public String getAlanDeger() {
		return alanDeger;
	}

	public void setAlanDeger(String alanDeger) {
		this.alanDeger = alanDeger;
	}

	@Transient
	public static String getAlanAciklama(String alan) {
		String str = "";
		if (alan != null) {
			str = alan;
			MethodAlanAPI alanAPI = MethodAlanAPI.fromValue(alan);
			if (alanAPI != null) {
				if (alanAPI.equals(MethodAlanAPI.KIMLIK))
					str = "Kimlik No";
				else if (alanAPI.equals(MethodAlanAPI.MASRAF_YERI))
					str = "Masraf Yeri No";
				else if (alanAPI.equals(MethodAlanAPI.SIRKET))
					str = "Şirket No";
				else if (alanAPI.equals(MethodAlanAPI.TESIS))
					str = "Tesis No";
				else if (alanAPI.equals(MethodAlanAPI.UOM))
					str = "Ücreti Ödenen Mesai";
				else if (alanAPI.equals(MethodAlanAPI.RT))
					str = "Resmi Tatil Mesai";
				else if (alanAPI.equals(MethodAlanAPI.HT))
					str = "Hafta Tatil Mesai";
				else if (alanAPI.equals(MethodAlanAPI.AKSAM_GUN))
					str = "Akşam Gün Sayısı";
				else if (alanAPI.equals(MethodAlanAPI.AKSAM_SAAT))
					str = "Akşam Saat Sayısı";
				else if (alanAPI.equals(MethodAlanAPI.YIL))
					str = "Yıl";
				else if (alanAPI.equals(MethodAlanAPI.AY))
					str = "Ay";
				else if (alanAPI.equals(MethodAlanAPI.USER_NAME))
					str = "Kullanıcı Adı";
				else if (alanAPI.equals(MethodAlanAPI.PASSWORD))
					str = "Şifre";

			}
		}
		return str;
	}

	@Transient
	public boolean isAlanDegerZorunlu() {
		boolean zorunlu = false;
		MethodAlanAPI alanAPI = alanTipi != null ? MethodAlanAPI.fromValue(alanTipi) : null;
		if (alanAPI != null)
			zorunlu = alanAPI.equals(MethodAlanAPI.USER_NAME) || alanAPI.equals(MethodAlanAPI.PASSWORD);
		return zorunlu;
	}

	@Transient
	public String getAlanAciklama() {
		String str = FazlaMesaiERPDetay.getAlanAciklama(alanAdi);
		return str;
	}

	@Transient
	public MethodAlanAPI getMethodAlanAPI() {
		return methodAlanAPI;
	}

	@Transient
	public Integer getIndis() {
		return indis;
	}

	public void setIndis(Integer indis) {
		this.indis = indis;
	}

	public void setMethodAlanAPI(MethodAlanAPI methodAlanAPI) {
		this.methodAlanAPI = methodAlanAPI;
	}

	public void entityRefresh() {

	}

}
