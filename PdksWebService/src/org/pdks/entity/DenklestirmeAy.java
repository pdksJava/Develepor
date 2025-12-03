package org.pdks.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.pdks.genel.model.PdksUtil;
import org.pdks.security.entity.User;

@Entity(name = DenklestirmeAy.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { DenklestirmeAy.COLUMN_NAME_YIL, DenklestirmeAy.COLUMN_NAME_AY }) })
public class DenklestirmeAy extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4284922207901564192L;

	public static final String TABLE_NAME = "DENKLESTIRMEAY";
	public static final String COLUMN_NAME_YIL = "YIL";
	public static final String COLUMN_NAME_AY = "AY";
	public static final String COLUMN_NAME_IK_OTOMATIK_ONAY_TARIHI = "IK_OTOMATIK_ONAY_TARIHI";
	public static final String COLUMN_NAME_IK_OTOMATIK_ONAY_BASLANGIC_TARIHI = "IK_OTOMATIK_ONAY_BASLANGIC_TARIHI";
	public static final String COLUMN_NAME_FAZLA_MESAI_ONAY_DURUM = "FAZLA_MESAI_ONAY_DURUM";
	public static final String COLUMN_NAME_DENKLESTIRME_KESINTI_YAP = "DENKLESTIRME_KESINTI_YAP";
	public static final String COLUMN_NAME_GUN_MAX_CALISMA_SURESI = "GUN_MAX_CALISMA_SURESI";
	public static final String COLUMN_NAME_BAKIYE_SIFIRLA = "BAKIYE_SIFIRLA";
	public static final String COLUMN_NAME_DENKLESTIRME_DEVRET = "DENKLESTIRME_DEVRET";
	public static final String COLUMN_NAME_RADYOLOJI_GUN_MAX_SURESI = "RADYOLOJI_GUN_MAX_SURESI";
	public static final String COLUMN_NAME_DENKLESTIRME_TIPI = "DENKLESTIRME_TIPI";
	public static final String COLUMN_NAME_TASERON_DENKLESTIRME_TIPI = "TASERON_DENKLESTIRME_TIPI";
	public static final String COLUMN_NAME_DONEM_KODU = "DONEM_KODU";
	private int ay, yil;
	private Integer denklestirmeTipi, taseronDenklestirmeTipi;

	private double sure = 0, toplamIzinSure = 0;
	private Double yemekMolasiYuzdesi, fazlaMesaiMaxSure, radyolojiFazlaMesaiMaxSure;
	private Long donemKodu;

	private Date otomatikOnayIKTarih, otomatikOnayIKBaslangicTarih, basTarih, bitTarih;
	private String ayAdi = "", trClass;
	private List<CalismaModeliAy> modeller;
	private TreeMap<Long, CalismaModeliAy> modelMap;
	private Boolean fazlaMesaiOnayDurum = Boolean.TRUE, bakiyeSifirlaDurum = Boolean.FALSE, denklestirmeDevret = Boolean.FALSE;
	private Integer denklestirmeKesintiYap;

	private Boolean guncelleIK = Boolean.FALSE;

	public DenklestirmeAy() {
		super();
		this.olusturmaTarihi = new Date();
	}

	@Column(name = COLUMN_NAME_AY, nullable = false)
	public int getAy() {
		return ay;
	}

	public void setAy(int value) {
		this.ay = value;
		if (value > 0 && value < 13)
			donemGuncelle();
	}

	@Column(name = COLUMN_NAME_YIL, nullable = false)
	public int getYil() {
		return yil;
	}

	public void setYil(int value) {
		this.yil = value;
		if (value >= PdksUtil.getSistemBaslangicYili())
			donemGuncelle();
	}

	@Column(name = COLUMN_NAME_DONEM_KODU, insertable = false, updatable = false)
	public Long getDonemKodu() {
		return donemKodu;
	}

	public void setDonemKodu(Long donemKodu) {
		this.donemKodu = donemKodu;
	}

	@Column(name = COLUMN_NAME_DENKLESTIRME_TIPI)
	public Integer getDenklestirmeTipi() {
		return denklestirmeTipi;
	}

	public void setDenklestirmeTipi(Integer value) {

		this.denklestirmeTipi = value;
	}

	@Column(name = COLUMN_NAME_TASERON_DENKLESTIRME_TIPI)
	public Integer getTaseronDenklestirmeTipi() {
		return taseronDenklestirmeTipi;
	}

	public void setTaseronDenklestirmeTipi(Integer value) {

		this.taseronDenklestirmeTipi = value;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_IK_OTOMATIK_ONAY_TARIHI)
	public Date getOtomatikOnayIKTarih() {
		return otomatikOnayIKTarih;
	}

	public void setOtomatikOnayIKTarih(Date otomatikOnayIKTarih) {
		this.otomatikOnayIKTarih = otomatikOnayIKTarih;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_IK_OTOMATIK_ONAY_BASLANGIC_TARIHI)
	public Date getOtomatikOnayIKBaslangicTarih() {
		return otomatikOnayIKBaslangicTarih;
	}

	public void setOtomatikOnayIKBaslangicTarih(Date otomatikOnayIKBaslangicTarih) {
		this.otomatikOnayIKBaslangicTarih = otomatikOnayIKBaslangicTarih;
	}

	@Column(name = "YEMEK_MOLASI_CALISMA_YUZDESI")
	public Double getYemekMolasiYuzdesi() {
		return yemekMolasiYuzdesi;
	}

	public void setYemekMolasiYuzdesi(Double yemekMolasiYuzdesi) {
		this.yemekMolasiYuzdesi = yemekMolasiYuzdesi;
	}

	@Column(name = COLUMN_NAME_GUN_MAX_CALISMA_SURESI)
	public Double getFazlaMesaiMaxSure() {
		return fazlaMesaiMaxSure;
	}

	public void setFazlaMesaiMaxSure(Double fazlaMesaiMaxSure) {
		this.fazlaMesaiMaxSure = fazlaMesaiMaxSure;
	}

	@Column(name = COLUMN_NAME_RADYOLOJI_GUN_MAX_SURESI)
	public Double getRadyolojiFazlaMesaiMaxSure() {
		return radyolojiFazlaMesaiMaxSure;
	}

	public void setRadyolojiFazlaMesaiMaxSure(Double radyolojiFazlaMesaiMaxSure) {
		this.radyolojiFazlaMesaiMaxSure = radyolojiFazlaMesaiMaxSure;
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

	@Column(name = "GUNCELLE_IK")
	public Boolean getGuncelleIK() {
		return guncelleIK;
	}

	public void setGuncelleIK(Boolean guncelleIK) {
		this.guncelleIK = guncelleIK;
	}

	@Column(name = COLUMN_NAME_FAZLA_MESAI_ONAY_DURUM)
	public Boolean getFazlaMesaiOnayDurum() {
		return fazlaMesaiOnayDurum;
	}

	public void setFazlaMesaiOnayDurum(Boolean fazlaMesaiOnayDurum) {
		this.fazlaMesaiOnayDurum = fazlaMesaiOnayDurum;
	}

	@Column(name = COLUMN_NAME_BAKIYE_SIFIRLA)
	public Boolean getBakiyeSifirlaDurum() {
		return bakiyeSifirlaDurum;
	}

	public void setBakiyeSifirlaDurum(Boolean bakiyeSifirlaDurum) {
		this.bakiyeSifirlaDurum = bakiyeSifirlaDurum;
	}

	@Column(name = COLUMN_NAME_DENKLESTIRME_DEVRET)
	public Boolean getDenklestirmeDevret() {
		return denklestirmeDevret;
	}

	public void setDenklestirmeDevret(Boolean denklestirmeDevret) {
		this.denklestirmeDevret = denklestirmeDevret;
	}

	@Column(name = COLUMN_NAME_DENKLESTIRME_KESINTI_YAP)
	public Integer getDenklestirmeKesintiYap() {
		return denklestirmeKesintiYap;
	}

	public void setDenklestirmeKesintiYap(Integer denklestirmeKesintiYap) {
		this.denklestirmeKesintiYap = denklestirmeKesintiYap;
	}

	@Transient
	public String getAyAdi() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, ay - 1);
		cal.set(Calendar.YEAR, yil);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		ayAdi = PdksUtil.convertToDateString(cal.getTime(), "MMMM");
		return ayAdi;
	}

	public void setAyAdi(String ayAdi) {
		this.ayAdi = ayAdi;
	}

	@Transient
	public boolean isDurum(User user) {
		boolean guncelleDurum = this.getDurum();
		if (!guncelleDurum && user != null && (user.isIK() || user.isAdmin())) {
			guncelleDurum = guncelleIK != null && guncelleIK;
		}
		return guncelleDurum;
	}

	@Transient
	public boolean isDurumu() {
		boolean statu = durum != null && durum;
		// if (yil == 2016 && ay == 2)
		// statu = true;
		return statu;
	}

	@Transient
	public List<CalismaModeliAy> getModeller() {
		return modeller;
	}

	public void setModeller(List<CalismaModeliAy> modeller) {
		this.modeller = modeller;
	}

	@Transient
	public String getTrClass() {
		return trClass;
	}

	public void setTrClass(String trClass) {
		this.trClass = trClass;
	}

	@Transient
	public TreeMap<Long, CalismaModeliAy> getModelMap() {
		return modelMap;
	}

	public void setModelMap(TreeMap<Long, CalismaModeliAy> modelMap) {
		this.modelMap = modelMap;
	}

	@Transient
	public CalismaModeliAy getCalismaModeliAy(Long cmId) {
		CalismaModeliAy cma = null;
		try {
			if (cmId != null && modelMap != null)
				cma = modelMap.get(cmId);
		} catch (Exception e) {
		}

		return cma;
	}

	private void donemGuncelle() {
		if (ay > 0 && ay < 13 && yil >= PdksUtil.getSistemBaslangicYili() && basTarih == null && bitTarih == null) {
			basTarih = PdksUtil.convertToJavaDate(String.valueOf(yil * 100 + ay) + "01", "yyyyMMdd");
			bitTarih = PdksUtil.tariheGunEkleCikar(PdksUtil.tariheAyEkleCikar(basTarih, 1), -1);
		}
	}

	@Transient
	public long getDonem() {
		long donem = donemKodu != null ? donemKodu : yil * 100 + ay;
		return donem;
	}

	@Transient
	public Date getBasTarih() {
		if (basTarih == null)
			donemGuncelle();
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Transient
	public Date getBitTarih() {
		if (bitTarih == null)
			donemGuncelle();
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	public void entityRefresh() {

	}

}
