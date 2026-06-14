package org.pdks.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.enums.PuantajKatSayiTipi;
import org.pdks.genel.model.PdksUtil;

@Entity(name = VardiyaGun.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI, VardiyaGun.COLUMN_NAME_PERSONEL }) })
public class VardiyaGun extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5973285593488926867L;

	static Logger logger = Logger.getLogger(VardiyaGun.class);

	public static final String TABLE_NAME = "VARDIYA_GUN";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_VARDIYA_TARIHI = "VARDIYA_TARIHI";
	public static final String COLUMN_NAME_VARDIYA = "VARDIYA_ID";
	public static final String COLUMN_NAME_VARDIYA_SAAT = "VARDIYA_SAAT_ID";
	public static final String COLUMN_NAME_VARDIYA_ACIKLAMA = "VARDIYA_ACIKLAMA";
	public static final String COLUMN_NAME_PERSONEL_NO = "PERSONEL_NO";
	public static final String COLUMN_NAME_VERSION = "VERSION";
	public static final String COLUMN_NAME_ONAYLI = "ONAYLI";

	public static final String STYLE_CLASS_NORMAL_CALISMA = "calismaAylik";
	public static final String STYLE_CLASS_NORMAL_CALISMA_EVEN = "calismaAylikEven";
	public static final String STYLE_CLASS_OZEL_ISTEK = "ozelIstekAylik";
	public static final String STYLE_CLASS_OFF = "off";
	public static final String STYLE_CLASS_EGITIM = "ozelIstekEgitim";
	public static final String STYLE_CLASS_IZIN = "izinAylik";
	public static final String STYLE_CLASS_HAFTA_TATIL = "tatilAylik";
	public static final String STYLE_CLASS_DIGER_AY = "digerAy";
	public static final String STYLE_CLASS_ODD = "acik";
	public static final String STYLE_CLASS_EVEN = "koyu";
	public static final String STYLE_CLASS_HATA = "hata";
	public static boolean haftaTatilDurum;
	private static Date saniyeYuvarlaZaman;

	private Personel personel;
	private Vardiya vardiya, islemVardiya, oncekiVardiya, sonrakiVardiya, yeniVardiya, eskiVardiya;
	private Integer offFazlaMesaiBasDakika, haftaTatiliFazlaMesaiBasDakika;
	private Date vardiyaDate;
	private ArrayList<PersonelIzin> izinler;
	private ArrayList<Vardiya> vardiyalar;
	private VardiyaGun oncekiVardiyaGun, sonrakiVardiyaGun;
	private int beklemeSuresi = 6;
	private Double calismaSuaSaati = PersonelDenklestirme.getCalismaSaatiSua(), resmiTatilKanunenEklenenSure = 0.0d, icapciMesaiSaat = 0d;
	private Boolean izinHaftaTatilDurum;
	private boolean hareketHatali = Boolean.FALSE, planHareketEkle = Boolean.TRUE, kullaniciYetkili = Boolean.TRUE, zamanGuncelle = Boolean.TRUE, zamanGelmedi = Boolean.FALSE;
	private boolean fazlaMesaiTalepOnayliDurum = Boolean.FALSE, fazlaMesaiTalepDurum = Boolean.FALSE, ayarlamaBitti = false, bayramAyir = false;

	private double calismaSuresi = 0, normalSure = 0, resmiTatilSure = 0, ucretiOdenenFazlaMesaiSaat = 0, haftaTatilDigerSure = 0, gecenAyResmiTatilSure = 0, aksamKatSayisi = 0d, aksamVardiyaSaatSayisi = 0d;
	private double calisilmayanAksamSure = 0, fazlaMesaiSure = 0, bayramCalismaSuresi = 0, haftaCalismaSuresi = 0d, yasalMaxSure = 11.0d;
	private Integer basSaat, basDakika, bitSaat, bitDakika;
	private String tdClass = "", style = "", manuelGirisHTML = "", vardiyaKisaAciklama, personelNo, vardiyaDateStr, donemStr;
	private Tatil tatil;
	private PersonelIzin izin;
	private VardiyaSablonu vardiyaSablonu;
	private HashMap<Integer, BigDecimal> katSayiMap;
	private boolean bitmemisGun = Boolean.TRUE, islendi = Boolean.FALSE, ayrikHareketVar = Boolean.FALSE, gebeMi = false, sutIzniVar = false;
	private boolean ayinGunu = Boolean.TRUE, onayli = Boolean.TRUE, fiiliHesapla = Boolean.FALSE, gecmisHataliDurum = Boolean.FALSE, hataliDurum = Boolean.FALSE, cihazZamanSaniyeSifirla = Boolean.FALSE, donemAcik = Boolean.TRUE;
	private List<String> linkAdresler;
	private HashMap<String, Personel> gorevliPersonelMap;
	private CalismaModeli calismaModeli = null;
	private Boolean fazlaMesaiOnayla, vardiyaOnayli = Boolean.TRUE;
	private Integer version = 0;

	// private PersonelDonemselDurum sutIzniPersonelDonemselDurum, gebePersonelDonemselDurum, isAramaPersonelDonemselDurum;

	public VardiyaGun() {
		super();

	}

	/**
	 * @param xPersonel
	 * @param xVardiya
	 * @param xVardiyaDate
	 */
	public VardiyaGun(Personel xPersonel, Vardiya xVardiya, Date xVardiyaDate) {
		super();
		this.setPersonel(xPersonel);
		this.setVardiya(xVardiya);
		this.setVardiyaDate(xVardiyaDate);
		if (xVardiya != null && xVardiya.getKatSayiMap() != null)
			this.katSayiMap = xVardiya.getKatSayiMap();

		if (xVardiya != null)
			this.durum = !xVardiya.isCalisma();
	}

	@Version
	@Column(name = COLUMN_NAME_VERSION)
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer value) {
		this.version = value;
	}

	@Column(name = COLUMN_NAME_ONAYLI)
	public Boolean getVardiyaOnayli() {
		return vardiyaOnayli;
	}

	public void setVardiyaOnayli(Boolean value) {
		if (value == null || value.booleanValue()) {
			if (vardiyaOnayli != null && PdksUtil.isBooleanDegisti(vardiyaOnayli, value)) {
				boolean guncel = PdksUtil.isIntegerDegisti(this.version, 0);
				if (guncel && this.degisti == false) {
					this.version = 0;
					this.degisti = true;
				}
			}
		}
		this.vardiyaOnayli = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		if (personel != null) {
			setVardiyaSablonu(personel.getSablon());
			setCalismaModeli(personel.getCalismaModeli());
		}
		this.personel = personel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VARDIYA, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Vardiya getVardiya() {
		return vardiya;
	}

	public void setVardiya(Vardiya value) {
		Long oldId = eskiVardiya != null && eskiVardiya.getId() != null ? eskiVardiya.getId() : 0l;
		if (value != null && value.getId() != null)
			value.setIslemVardiyaGun(this);

		if (this.isGuncellendi() == false) {
			Long newId = value != null && value.getId() != null ? value.getId() : 0l;
			this.guncellendi = PdksUtil.isLongDegisti(oldId, newId);
		}

		this.vardiya = value;
	}

	@Column(name = COLUMN_NAME_VARDIYA_ACIKLAMA, insertable = false, updatable = false)
	public String getVardiyaKisaAciklama() {
		return vardiyaKisaAciklama;
	}

	public void setVardiyaKisaAciklama(String vardiyaKisaAciklama) {
		this.vardiyaKisaAciklama = vardiyaKisaAciklama;
	}

	@Column(name = COLUMN_NAME_PERSONEL_NO, insertable = false, updatable = false)
	public String getPersonelNo() {
		return personelNo;
	}

	public void setPersonelNo(String personelNo) {
		this.personelNo = personelNo;
	}

	@Transient
	public Integer getBasSaat() {
		return basSaat;
	}

	public void setBasSaat(Integer basSaat) {
		this.basSaat = basSaat;
	}

	@Transient
	public Integer getBasDakika() {
		return basDakika;
	}

	public void setBasDakika(Integer basDakika) {
		this.basDakika = basDakika;
	}

	@Transient
	public Integer getBitSaat() {
		return bitSaat;
	}

	public void setBitSaat(Integer bitSaat) {
		this.bitSaat = bitSaat;
	}

	@Transient
	public Integer getBitDakika() {
		return bitDakika;
	}

	public void setBitDakika(Integer bitDakika) {
		this.bitDakika = bitDakika;
	}

	@Temporal(value = TemporalType.DATE)
	@Column(name = COLUMN_NAME_VARDIYA_TARIHI, nullable = false)
	public Date getVardiyaDate() {
		return vardiyaDate;
	}

	@Transient
	public String getVardiyaDateStr() {
		if (vardiyaDateStr == null)
			vardiyaDateStr = vardiyaDate != null ? PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd") : "";
		return vardiyaDateStr;

	}

	public void setVardiyaDateStr(String vardiyaDateStr) {
		this.vardiyaDateStr = vardiyaDateStr;
	}

	@Transient
	public String getHeaderClass() {
		String str = "calismaGun";
		if (tatil != null)
			str = tatil.isYarimGunMu() ? "arife" : "bayram";
		return str;

	}

	public void setVardiyaDate(Date value) {
		boolean gunDurum = Boolean.TRUE;
		this.setVardiyaDateStr(null);
		if (value != null) {
			gunDurum = PdksUtil.tarihKarsilastirNumeric(Calendar.getInstance().getTime(), value) != 1;
			this.setVardiyaDateStr(PdksUtil.convertToDateString(value, "yyyyMMdd"));
		}
		setBitmemisGun(gunDurum);
		this.vardiyaDate = value;
	}

	@Transient
	public String getTarihStr() {
		return this.getVardiyaDateStr();
	}

	@Transient
	public Vardiya getIslemVardiya() {
		if (vardiyaDate != null && vardiya != null) {
			if (islemVardiya == null) {
				setVardiyaZamani();
				if (islemVardiya == null) {
					Vardiya vardiyaKopya = (Vardiya) vardiya.clone();
					vardiyaKopya.setKopya(Boolean.TRUE);
					setIslemVardiya(vardiyaKopya);
					if (islemVardiya != null) {
						if (oncekiVardiyaGun != null && oncekiVardiyaGun.getIslemVardiya() != null)
							islemVardiya.setOncekiVardiya(oncekiVardiyaGun.getIslemVardiya());
						islemVardiya.setVardiyaTarih(vardiyaDate);
						islemVardiya.setVardiyaZamani(this);
					}
				}
			}
		}
		return islemVardiya;
	}

	public void setIslemVardiya(Vardiya islemVardiya) {
		this.islemVardiya = islemVardiya;
	}

	@Transient
	public boolean isHareketHatali() {
		return hareketHatali;
	}

	public void setHareketHatali(boolean value) {
		if (value) {
			logger.debug(getVardiyaKeyStr());
		}
		this.hareketHatali = value;
	}

	@Transient
	public ArrayList<PersonelIzin> getIzinler() {
		return izinler;
	}

	public void setIzinler(ArrayList<PersonelIzin> izinler) {
		this.izinler = izinler;
	}

	@Transient
	public double getCalismaSuresi() {
		return calismaSuresi;
	}

	public void setCalismaSuresi(double value) {
		if (value != 0.0d) {
			if (this.getVardiyaDateStr().endsWith("0606"))
				logger.debug(value);
		}
		this.calismaSuresi = value;
	}

	@Transient
	public void addCalismaSuresi(double value) {
		if (value != 0.0d) {
			if (this.getVardiyaDateStr().endsWith("0606"))
				logger.debug(value);
		}

		calismaSuresi += value;
	}

	@Transient
	public double getHaftaTatilDigerSure() {
		return haftaTatilDigerSure;
	}

	public void setHaftaTatilDigerSure(double value) {
		this.haftaTatilDigerSure = value;
	}

	public void addHaftaTatilDigerSure(double value) {
		if (value != 0.0d)
			logger.debug(value);
		this.haftaTatilDigerSure += value;
	}

	@Transient
	public double getToplamSure() {
		double toplamSure = getResmiTatilSure() + getCalismaSuresi();
		return toplamSure;
	}

	@Transient
	public double getCalismaNetSuresi() {
		double netSure = calismaSuresi - (resmiTatilSure + haftaCalismaSuresi);
		return netSure;
	}

	@Transient
	public double getHaftaCalismaSuresi() {
		return haftaCalismaSuresi;
	}

	public void setHaftaCalismaSuresi(double value) {
		this.haftaCalismaSuresi = value;
	}

	@Transient
	public void addHaftaCalismaSuresi(double value) {
		if (value > 0.0d && isFiiliHesapla())
			logger.debug(value);
		haftaCalismaSuresi += value;
	}

	@Transient
	public void addPersonelIzin(PersonelIzin personelIzin) {
		if (izinler == null)
			izinler = new ArrayList<PersonelIzin>();
		if (personelIzin.isGunlukOldu()) {
			this.setIzin(personelIzin);
			personelIzin.setGunlukOldu(Boolean.TRUE);
		}
		boolean ekle = true;
		for (PersonelIzin izin : izinler) {
			if (personelIzin.getId() != null && izin.getId().equals(personelIzin.getId()))
				ekle = false;
		}
		if (ekle)
			izinler.add(personelIzin);
	}

	@Transient
	public Date getVardiyaFazlaMesaiBasZaman() {
		if (islemVardiya == null)
			setVardiyaZamani();
		return islemVardiya != null ? islemVardiya.getVardiyaFazlaMesaiBasZaman() : null;
	}

	@Transient
	public String getTdClass() {
		return tdClass;
	}

	public void setTdClass(String tdClass) {
		this.tdClass = tdClass;
	}

	@Transient
	public double getNormalSure() {
		return normalSure;
	}

	public void setNormalSure(double normalSure) {
		this.normalSure = normalSure;
	}

	@Transient
	public Tatil getTatil() {
		return tatil;
	}

	public void setTatil(Tatil value) {
		this.tatil = value;
		if (value != null && vardiya != null && value.getVardiyaMap() != null) {
			if (value.getVardiyaMap().containsKey(vardiya.getId())) {
				Tatil tatilNew = (Tatil) value.clone();
				Vardiya vardiyaTatil = value.getVardiyaMap().get(vardiya.getId());
				if (islemVardiya != null)
					islemVardiya.setArifeCalismaSure(vardiyaTatil.getArifeCalismaSure());
				vardiya.setArifeCalismaSure(vardiyaTatil.getArifeCalismaSure());
				tatilNew.setBasTarih(vardiyaTatil.getArifeBaslangicTarihi());
				if (tatilNew.getOrjTatil() != null && vardiyaTatil.getArifeBaslangicTarihi() != null) {
					Tatil orjTatil = (Tatil) tatilNew.getOrjTatil().clone();
					orjTatil.setBasTarih(vardiyaTatil.getArifeBaslangicTarihi());
					tatilNew.setOrjTatil(orjTatil);
				}
				this.tatil = tatilNew;
			}

		}

	}

	@Transient
	public boolean isIzinli() {
		boolean izinli = izin != null;
		if (!izinli)
			izinli = vardiya != null && vardiya.isIzinVardiya();
		return izinli;
	}

	@Transient
	public PersonelIzin getIzin() {

		return izin;
	}

	public void setIzin(PersonelIzin value) {
		if (vardiyaDateStr.equals("20241124")) {
			if (value != null)
				logger.debug(vardiyaDateStr + " " + value.getId() + " " + value.getAciklama());
			else
				logger.debug("");
		}
		this.izin = value;
	}

	@Transient
	public VardiyaSablonu getVardiyaSablonu() {
		return vardiyaSablonu;
	}

	public void setVardiyaSablonu(VardiyaSablonu vardiyaSablonu) {
		this.vardiyaSablonu = vardiyaSablonu;
	}

	public Vardiya setVardiyaZamani() {
		if (vardiya != null && vardiyaDate != null) {
			if (!islendi || islemVardiya == null) {
				setIslendi(Boolean.TRUE);
				if ((sonrakiVardiya == null && oncekiVardiyaGun == null) || islemVardiya == null) {
					Vardiya vardiyaKopya = (Vardiya) vardiya.clone();
					vardiyaKopya.setKopya(Boolean.TRUE);
					setIslemVardiya(vardiyaKopya);
				}
				if (islemVardiya != null) {
					if (oncekiVardiyaGun != null && oncekiVardiyaGun.getIslemVardiya() != null)
						islemVardiya.setOncekiVardiya(oncekiVardiyaGun.getIslemVardiya());
					islemVardiya.setSonrakiVardiya(sonrakiVardiya);
					islemVardiya.setVardiyaTarih(vardiyaDate);
					islemVardiya.setVardiyaZamani(this);
				}
			}
		}
		return islemVardiya;

	}

	public Vardiya setIslemVardiyaZamani() {
		if (vardiya != null && vardiyaDate != null && !islendi && islemVardiya == null) {
			setIslendi(Boolean.TRUE);
			Vardiya vardiyaKopya = (Vardiya) vardiya.clone();
			vardiyaKopya.setKopya(Boolean.TRUE);
			setIslemVardiya(vardiyaKopya);
			islemVardiya.setVardiyaTarih(vardiyaDate);
			islemVardiya.setVardiyaZamani(this);
		}
		return islemVardiya;

	}

	@Transient
	public String getBasSaatDakikaStr() {
		StringBuilder aciklama = new StringBuilder(PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(basSaat), '0', 2));
		if (basDakika > 0)
			aciklama.append(":" + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(basDakika), '0', 2));
		String str = aciklama.toString();
		aciklama = null;
		return str;
	}

	@Transient
	public String getBitSaatDakikaStr() {
		StringBuilder aciklama = new StringBuilder(PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(bitSaat), '0', 2));
		if (bitDakika > 0)
			aciklama.append(":" + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(bitDakika), '0', 2));
		String str = aciklama.toString();
		aciklama = null;
		return str;
	}

	@Transient
	public String getVardiyaAciklama() {
		String vardiyaAdi = vardiya != null ? vardiya.getVardiyaTipiAciklama() : "";
		if (vardiya != null && vardiya.isCalisma()) {
			setVardiyaZamani();
			if (getIslemVardiya() != null)
				vardiyaAdi = PdksUtil.convertToDateString(islemVardiya.getBasZaman(), PdksUtil.getSaatFormat()) + " - " + PdksUtil.convertToDateString(islemVardiya.getBitZaman(), PdksUtil.getSaatFormat());
		}
		return vardiyaAdi;
	}

	@Transient
	public String getVardiyaZamanAdi() {
		StringBuilder vardiyaAdi = new StringBuilder(PdksUtil.convertToDateString(vardiyaDate, PdksUtil.getDateFormat()));
		String vardiyaZaman = getVardiyaAciklama();
		vardiyaAdi.append(" " + vardiyaZaman);
		String str = vardiyaAdi.toString();
		vardiyaAdi = null;
		return str;
	}

	@Transient
	public String getVardiyaAdi() {
		String vardiyaAdi = null;
		try {
			vardiyaAdi = getVardiyaAciklama();
			if (izin != null && izin.isPlandaGoster())
				vardiyaAdi = izin.getIzinTipi().getIzinTipiTanim().getAciklama();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return vardiyaAdi;
	}

	@Transient
	public ArrayList<Vardiya> getVardiyalar() {
		return vardiyalar;
	}

	public void setVardiyalar(ArrayList<Vardiya> value) {
		if (value != null)
			logger.debug(id);
		this.vardiyalar = value;
	}

	/**
	 * @param value
	 */
	public void setKontrolVardiyalar(ArrayList<Vardiya> value) {
		this.vardiyalar = value;
		if (value != null && vardiya != null && (vardiya.isFMI() || vardiya.getDurum().equals(Boolean.FALSE)) && vardiya.getId() != null) {
			boolean ekle = true;
			Long vId = vardiya.getId();
			for (Vardiya vardiya1 : vardiyalar) {
				if (vardiya1.getId().equals(vId))
					ekle = false;
			}
			if (ekle) {
				ArrayList<Vardiya> yeniVardiyalar = new ArrayList<Vardiya>();
				yeniVardiyalar.addAll(value);
				yeniVardiyalar.add(vardiya);
				this.vardiyalar = yeniVardiyalar;
			}
		}

	}

	@Transient
	public List<Vardiya> getVardiyaList() {
		List<Vardiya> vardiyaList = null;
		if (tatil != null)
			vardiyaList = vardiyalar;
		else {
			vardiyaList = new ArrayList<Vardiya>();
			for (Iterator<Vardiya> iterator = vardiyaList.iterator(); iterator.hasNext();) {
				Vardiya vardiya = iterator.next();

				vardiyaList.add(vardiya);
			}
		}
		return vardiyaList;
	}

	@Transient
	public String getGunClass() {
		String classAdi = "";
		try {
			if (tatil != null)
				classAdi = !tatil.isYarimGunMu() ? "bayram" : "arife";
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			classAdi = "";
		}
		return classAdi;
	}

	@Transient
	public String getGunAdi() {
		String gunAdi = "";
		try {
			if (tatil != null)
				gunAdi = tatil.getAciklama();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			gunAdi = "";
		}
		return gunAdi;
	}

	@Transient
	public String getVardiyaKeyStr() {

		String key = (personel != null ? personel.getSicilNo() : "") + "_" + getVardiyaDateStr();
		return key;

	}

	@Transient
	public String getVardiyaKey() {
		// if (!fiiliHesapla)
		// setIzin(null);
		String key = getVardiyaKeyStr();
		return key;

	}

	@Transient
	public String getVardiyaGunAciklama() {
		String aciklama = vardiyaDate != null ? PdksUtil.convertToDateString(vardiyaDate, "d MMMMM EEEEE") : "";
		return aciklama;

	}

	@Transient
	public String getVardiyaStyle() {
		String style = "calismaVar";
		if (vardiya != null) {
			if ((izin != null && izin.isPlandaGoster()) || vardiya.isRadyasyonIzni() || vardiya.isFMI())
				style = "izin";
			else if (!vardiya.isCalisma()) {
				if (vardiya.isHaftaTatil())
					style = "haftaTatil";
				else if (vardiya.isOff())
					style = "off";

			}

		}
		return style;

	}

	@Transient
	public boolean isPazar() {
		boolean pazar = false;
		if (vardiyaDate != null) {
			int gun = getHaftaninGunu();
			pazar = gun == Calendar.SUNDAY;
		}
		return pazar;
	}

	@Transient
	public boolean isHaftaTatil() {
		boolean tatilGunu = false;
		if (vardiyaDate != null && vardiya != null) {
			tatilGunu = vardiya.isHaftaTatil();
			if (!tatilGunu)
				tatilGunu = tatil != null && !tatil.isYarimGunMu();

		}

		return tatilGunu;
	}

	@Transient
	public boolean isTatilGunu() {
		boolean tatilGunu = false;
		if (vardiyaDate != null) {
			tatilGunu = !isHaftaIci();
			if (!tatilGunu)
				tatilGunu = tatil != null && !tatil.isYarimGunMu();

		}

		return tatilGunu;
	}

	@Transient
	public boolean isHaftaIci() {
		boolean haftaIci = false;
		if (vardiyaDate != null) {
			int gun = getHaftaninGunu();
			haftaIci = gun != Calendar.SATURDAY && gun != Calendar.SUNDAY;

		}

		return haftaIci;
	}

	@Transient
	public int getHaftaninGunu() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(PdksUtil.getDate(vardiyaDate));
		int gun = cal.get(Calendar.DAY_OF_WEEK);
		return gun;
	}

	@Transient
	public boolean isBitmemisGun() {
		return bitmemisGun;
	}

	public void setBitmemisGun(boolean bitmemisGun) {
		this.bitmemisGun = bitmemisGun;
	}

	@Transient
	public Vardiya getSonrakiVardiya() {
		return sonrakiVardiya;
	}

	public void setSonrakiVardiya(Vardiya sonrakVardiya) {
		this.sonrakiVardiya = sonrakVardiya;
	}

	@Transient
	public Vardiya getYeniVardiya() {
		return yeniVardiya;
	}

	public void setYeniVardiya(Vardiya yeniVardiya) {
		this.yeniVardiya = yeniVardiya;
	}

	@Transient
	public double getResmiTatilSure() {
		return resmiTatilSure;
	}

	@Transient
	public double getResmiTatilToplamSure() {
		double resmiTatilToplamSure = resmiTatilSure + (resmiTatilKanunenEklenenSure != null ? resmiTatilKanunenEklenenSure.doubleValue() : 0.0d);

		if (resmiTatilToplamSure > 0.0d)
			logger.debug(resmiTatilToplamSure);
		return resmiTatilToplamSure;
	}

	@Transient
	public double getUcretiOdenenFazlaMesaiSaat() {
		return ucretiOdenenFazlaMesaiSaat;
	}

	public void setUcretiOdenenFazlaMesaiSaat(double ucretiOdenenFazlaMesaiSaat) {
		this.ucretiOdenenFazlaMesaiSaat = ucretiOdenenFazlaMesaiSaat;
	}

	public void setResmiTatilSure(double value) {
		if (value != 0.0d) {
			if (this.getVardiyaDateStr().endsWith("0319"))
				logger.debug(value);
		}
		this.resmiTatilSure = value;
	}

	public void addResmiTatilSure(double value) {
		if (value != 0.0d) {
			if (this.getVardiyaDateStr().endsWith("0319"))
				logger.debug(value);
		}
		this.resmiTatilSure += value;
	}

	@Transient
	public boolean isAylikGirisYap() {
		boolean aylikGirisYap = ayinGunu || donemAcik;
		return aylikGirisYap;
	}

	@Transient
	public boolean isAyinGunu() {
		return ayinGunu;
	}

	@Transient
	public boolean isTatilMesai() {
		double tatilSure = getResmiTatilToplamSure() + haftaCalismaSuresi;
		return tatilSure > 0.0d;
	}

	@Transient
	public String getStyleClass() {
		String styleClass = "";
		if (tatil != null) {
			styleClass = tatil.isYarimGunMu() ? "Arife" : "Bayram";
		}
		return styleClass;
	}

	public void setAyinGunu(boolean value) {
		this.ayinGunu = value;
	}

	@Transient
	public String getTdClassYaz() {
		String classAdi = tdClass;
		if (vardiya != null && PdksUtil.hasStringValue(vardiya.getStyleClass()))
			classAdi = vardiya.getStyleClass();
		return classAdi;
	}

	@Transient
	public double getFazlaMesaiSure() {
		return fazlaMesaiSure;
	}

	public void setFazlaMesaiSure(double fazlaMesaiSure) {
		this.fazlaMesaiSure = fazlaMesaiSure;
	}

	@Transient
	public boolean isCalismayaBaslamadi() {
		boolean iseBaslamadi = PdksUtil.tarihKarsilastirNumeric(personel.getIseGirisTarihi(), vardiyaDate) == 1;
		return iseBaslamadi;
	}

	@Transient
	public boolean isCalismayiBirakti() {
		boolean isiBirakti = (!isCalismayaBaslamadi()) && PdksUtil.tarihKarsilastirNumeric(vardiyaDate, personel.getSonCalismaTarihi()) == 1;
		return isiBirakti;
	}

	@Transient
	public boolean isCalisiyor() {
		boolean calisiyor = vardiyaDate != null && personel != null && personel.isCalisiyorGun(vardiyaDate);
		return calisiyor;
	}

	@Transient
	public boolean isKullaniciYetkili() {
		return kullaniciYetkili;
	}

	public void setKullaniciYetkili(boolean kullaniciYetkili) {
		this.kullaniciYetkili = kullaniciYetkili;
	}

	@Transient
	public String getVardiyaPlanAdi() {
		String str = getVardiyaAdi(this.vardiya);
		return str;
	}

	@Transient
	public String getVardiyaAdi(Vardiya vTemp) {
		String str = null;
		if (vTemp == null)
			vTemp = this.vardiya;
		if (vTemp != null) {
			str = vTemp.getKisaAdi();
			if (vTemp.isCalisma()) {
				if (this.vardiyaDate == null)
					this.vardiyaDate = new Date();
				VardiyaGun tmp = new VardiyaGun(this.personel, vTemp, this.vardiyaDate);
				tmp.setVardiyaZamani();
				if (vTemp.isCalisma()) {
					String pattern = PdksUtil.getSaatFormat();
					Vardiya tmpVardiya = tmp.getIslemVardiya();
					String ek = "";
					if (tmpVardiya.isSutIzniMi())
						ek = " - Süt İzni";
					else if (tmpVardiya.isSuaMi())
						ek = " - Şua";
					else if (tmpVardiya.isGebelikMi())
						ek = " - Gebe";
					str = PdksUtil.convertToDateString(tmpVardiya.getVardiyaBasZaman(), pattern) + " - " + PdksUtil.convertToDateString(tmpVardiya.getVardiyaBitZaman(), pattern) + " ( " + vTemp.getKisaAdi() + ek + " ) ";
					try {
						str += " Net Süre : " + PdksUtil.numericValueFormatStr(tmpVardiya.getNetCalismaSuresi(), null);
					} catch (Exception e) {

					}
				}

				tmp = null;
			} else if (!(vTemp.isOff() || vTemp.isHaftaTatil()))
				str += " - " + vTemp.getAdi();
		}

		return str;
	}

	@Transient
	public boolean isRaporIzni() {
		boolean raporIzni = Boolean.FALSE;
		if (vardiya != null) {
			if (izin != null)
				raporIzni = izin.getIzinTipi().isRaporIzin();
		}

		return raporIzni;
	}

	@Transient
	public boolean isEkleIzni() {
		boolean raporIzni = Boolean.FALSE;
		if (vardiya != null) {
			if (izin != null)
				raporIzni = izin.getIzinTipi().isEkleCGS();
		}

		return raporIzni;
	}

	@Transient
	public boolean isGorevli() {
		boolean gorevli = Boolean.FALSE;
		if (vardiya != null && izin != null)
			gorevli = izin.getIzinTipi().isGorevli();

		return gorevli;
	}

	@Transient
	public boolean isFiiliHesapla() {
		return fiiliHesapla;
	}

	public void setFiiliHesapla(boolean fiiliHesapla) {
		this.fiiliHesapla = fiiliHesapla;
	}

	@Transient
	public boolean isHataliDurum() {
		return hataliDurum;
	}

	public void setHataliDurum(boolean hataliDurum) {
		if (this.getVardiyaDateStr().endsWith("0606")) {
			logger.debug(hataliDurum);
		}
		this.hataliDurum = hataliDurum;
	}

	@Transient
	public List<String> getLinkAdresler() {

		return linkAdresler;
	}

	@Transient
	public void addLinkAdresler(String value) {
		if (PdksUtil.hasStringValue(value)) {
			if (linkAdresler == null)
				linkAdresler = new ArrayList<String>();
			if (!linkAdresler.contains(value))
				linkAdresler.add(value);

		}
	}

	public void setLinkAdresler(List<String> linkAdresler) {
		this.linkAdresler = linkAdresler;

	}

	@Transient
	public String getLinkAdresHtml() {
		String adres = null;
		if (linkAdresler != null) {
			StringBuilder sb = new StringBuilder();
			if (linkAdresler.size() > 1)
				sb.append("<B>Uyarılar</B></br>");
			for (String string : linkAdresler)
				if (string != null)
					sb.append("</br>" + string);
			adres = sb.toString();
			sb = null;
		}

		return adres;
	}

	@Transient
	public boolean isZamanGuncelle() {
		return zamanGuncelle;
	}

	public void setZamanGuncelle(boolean zamanGuncelle) {
		this.zamanGuncelle = zamanGuncelle;
	}

	@Transient
	public boolean isDonemAcik() {
		return donemAcik;
	}

	public void setDonemAcik(boolean donemAcik) {
		this.donemAcik = donemAcik;
	}

	@Transient
	private boolean helpPersonel(Personel personel) {
		return personel != null && gorevliPersonelMap != null && gorevliPersonelMap.containsKey(personel.getPdksSicilNo());

	}

	@Transient
	public boolean isCalisan() {
		boolean calisan = false;
		if (this.getVardiya() != null) {
			calisan = this.isKullaniciYetkili() || (this.getIzin() != null && !helpPersonel(this.getPersonel()));
		}
		return calisan;
	}

	@Transient
	public HashMap<String, Personel> getGorevliPersonelMap() {
		return gorevliPersonelMap;
	}

	public void setGorevliPersonelMap(HashMap<String, Personel> gorevliPersonelMap) {
		this.gorevliPersonelMap = gorevliPersonelMap;
	}

	@Transient
	public double getGecenAyResmiTatilSure() {
		return gecenAyResmiTatilSure;
	}

	public void setGecenAyResmiTatilSure(double value) {
		if (value != 0.0d) {
			if (vardiyaDateStr.endsWith("0609"))
				logger.debug(value);
		}

		this.gecenAyResmiTatilSure = value;
	}

	@Transient
	public boolean isOnayli() {
		return onayli;
	}

	public void setOnayli(boolean onayli) {
		this.onayli = onayli;
	}

	@Transient
	public String getPlanKey() {
		String key = (personel != null ? "perId=" + personel.getId() : "");
		if (vardiyaDate != null) {
			if (personel != null)
				key += "&";
			key += "tarih=" + PdksUtil.convertToDateString(vardiyaDate, "yyyyMMdd");
		}
		String planKey = PdksUtil.getEncodeStringByBase64(key);
		return planKey;
	}

	@Transient
	public boolean isZamanGelmedi() {
		return zamanGelmedi;
	}

	public void setZamanGelmedi(boolean value) {
		if (value)
			logger.debug(this.getVardiyaKeyStr());
		this.zamanGelmedi = value;
	}

	@Transient
	public boolean isIslendi() {
		return islendi;
	}

	public void setIslendi(boolean islendi) {
		this.islendi = islendi;
	}

	public void addGecenAyResmiTatilSure(double value) {
		if (value != 0.0d) {
			if (vardiyaDateStr.endsWith("0609"))
				logger.debug(value);
		}

		this.gecenAyResmiTatilSure += value;
	}

	@Transient
	public double getBayramCalismaSuresi() {
		return bayramCalismaSuresi;
	}

	public void setBayramCalismaSuresi(double value) {
		if (value > 0.0d)
			logger.debug(value);
		this.bayramCalismaSuresi = value;
	}

	@Transient
	public void addBayramCalismaSuresi(double value) {
		if (value != 0.0d)
			logger.debug(value);
		bayramCalismaSuresi += value;
	}

	@Transient
	public double getCalisilmayanAksamSure() {
		return calisilmayanAksamSure;
	}

	public void setCalisilmayanAksamSure(double calisilmayanAksamSure) {
		this.calisilmayanAksamSure = calisilmayanAksamSure;
	}

	@Transient
	public String getSortKey() {
		String sortKey = this.getPersonel().getSirket().getAd() + (this.getPersonel().getTesis() != null ? "_" + this.getPersonel().getTesis().getAciklama() : "") + "_" + this.getPersonel().getAdSoyad() + "_" + this.getVardiyaKeyStr();
		return sortKey;
	}

	@Transient
	public String getSortBolumKey() {
		Personel yonetici = this.getPersonel().getPdksYonetici();
		Sirket sirket = this.getPersonel().getSirket();
		Long departmanId = null, sirketId = null;
		String sirketIdStr = null;
		if (sirket != null) {
			Departman departman = sirket.getDepartman();
			departmanId = departman != null ? departman.getId() : null;
			if (sirket.getSirketGrup() != null)
				sirketId = -sirket.getSirketGrup().getId();
			else
				sirketId = sirket.getId();
		}
		if (departmanId == null)
			departmanId = 0L;
		if (sirketId != null)
			sirketIdStr = sirketId > 0L ? "S" + sirketId : "G" + (-sirketId);
		if (sirketIdStr == null)
			sirketIdStr = "";
		Tanim bolum = this.getPersonel().getEkSaha3(), altBolum = this.getPersonel().getEkSaha4();
		CalismaModeli calismaModeli = this.getPersonel().getCalismaModeli();
		String sortKey = departmanId + "_" + sirketIdStr + "_" + (yonetici != null ? "_" + yonetici.getAdSoyad() : "") + "_" + (bolum != null ? "_" + bolum.getAciklama() : "") + "_" + (calismaModeli != null ? "_" + calismaModeli.getAciklama() : "");
		sortKey += "_" + (altBolum != null ? "_" + altBolum.getAciklama() : "") + "_" + this.getPersonel().getAdSoyad() + "_" + this.getVardiyaKeyStr();
		return sortKey;
	}

	@Transient
	public double getAksamVardiyaSaatSayisi() {
		return aksamVardiyaSaatSayisi;
	}

	public void setAksamVardiyaSaatSayisi(double value) {
		if (value > 0) {
			logger.debug(value);
		}
		this.aksamVardiyaSaatSayisi = value;
	}

	public void addAksamVardiyaSaatSayisi(double vale) {
		if (vale > 0.0d)
			this.aksamVardiyaSaatSayisi += vale;
	}

	@Transient
	public void saklaVardiya() {
		Vardiya vardiyaKopya = null;
		if (vardiya != null) {
			vardiyaKopya = (Vardiya) vardiya.clone();
			vardiyaKopya.setKopya(Boolean.TRUE);
		}

		this.eskiVardiya = vardiyaKopya;
	}

	@Transient
	public Vardiya getEskiVardiya() {
		return eskiVardiya;
	}

	public void setEskiVardiya(Vardiya eskiVardiya) {
		this.eskiVardiya = eskiVardiya;
	}

	public static boolean isHaftaTatilDurum() {
		return haftaTatilDurum;
	}

	public static void setHaftaTatilDurum(boolean haftaTatilDurum) {
		VardiyaGun.haftaTatilDurum = haftaTatilDurum;
	}

	@Transient
	public Boolean getFazlaMesaiOnayla() {
		return fazlaMesaiOnayla;
	}

	public void setFazlaMesaiOnayla(Boolean value) {
		if (vardiyaDateStr.endsWith("01"))
			logger.debug(vardiyaDateStr + " " + value);
		this.fazlaMesaiOnayla = value;
	}

	@Transient
	public boolean isAyrikHareketVar() {
		return ayrikHareketVar;
	}

	public void setAyrikHareketVar(boolean ayrikHareketVar) {
		this.ayrikHareketVar = ayrikHareketVar;
	}

	@Transient
	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	@Transient
	public VardiyaGun getOncekiVardiyaGun() {
		return oncekiVardiyaGun;
	}

	public void setOncekiVardiyaGun(VardiyaGun oncekiVardiyaGun) {
		this.oncekiVardiyaGun = oncekiVardiyaGun;
	}

	@Transient
	public Vardiya getOncekiVardiya() {
		return oncekiVardiya;
	}

	public void setOncekiVardiya(Vardiya oncekiVardiya) {
		this.oncekiVardiya = oncekiVardiya;
	}

	@Transient
	public VardiyaGun getSonrakiVardiyaGun() {
		return sonrakiVardiyaGun;
	}

	public void setSonrakiVardiyaGun(VardiyaGun sonrakiVardiyaGun) {
		this.sonrakiVardiyaGun = sonrakiVardiyaGun;
	}

	@Transient
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	@Transient
	public boolean isFazlaMesaiTalepOnayliDurum() {
		return fazlaMesaiTalepOnayliDurum;
	}

	public void setFazlaMesaiTalepOnayliDurum(boolean value) {
		if (vardiyaDateStr.endsWith("01"))
			logger.debug("" + value);
		this.fazlaMesaiTalepOnayliDurum = value;
	}

	@Transient
	public int getBeklemeSuresi() {
		return beklemeSuresi;
	}

	public void setBeklemeSuresi(int beklemeSuresi) {
		this.beklemeSuresi = beklemeSuresi;
	}

	@Transient
	public Double getCalismaSuaSaati() {
		return calismaSuaSaati;
	}

	public void setCalismaSuaSaati(Double calismaSuaSaati) {
		this.calismaSuaSaati = calismaSuaSaati;
	}

	@Transient
	public String getManuelGirisHTML() {
		return manuelGirisHTML;
	}

	public void setManuelGirisHTML(String manuelGirisHTML) {
		this.manuelGirisHTML = manuelGirisHTML;
	}

	@Transient
	public Integer getOffFazlaMesaiBasDakika() {
		return offFazlaMesaiBasDakika;
	}

	public void setOffFazlaMesaiBasDakika(Integer offFazlaMesaiBasDakika) {
		this.offFazlaMesaiBasDakika = offFazlaMesaiBasDakika;
	}

	@Transient
	public Integer getHaftaTatiliFazlaMesaiBasDakika() {
		return haftaTatiliFazlaMesaiBasDakika;
	}

	public void setHaftaTatiliFazlaMesaiBasDakika(Integer haftaTatiliFazlaMesaiBasDakika) {
		this.haftaTatiliFazlaMesaiBasDakika = haftaTatiliFazlaMesaiBasDakika;
	}

	@Transient
	public String getStyleGun() {
		String style = "";
		if (vardiya != null && vardiya.getId() != null) {
			if (ayinGunu)
				style = ";font-weight: bold;";
			else {
				style = ";color:red;";
				if (donemStr != null && vardiyaDateStr != null) {
					if (vardiyaDateStr.compareTo(donemStr + "01") == 1) {
						style = ";color:orange;";
					}
				}
			}

		}

		return style;
	}

	@Transient
	public BigDecimal getKatSayi(Integer tipi) {
		BigDecimal katSayi = null;
		try {
			if (tipi != null && katSayiMap != null) {
				if (katSayiMap.containsKey(tipi))
					katSayi = katSayiMap.get(tipi);
			}
		} catch (Exception e) {
			katSayi = null;
		}
		return katSayi;
	}

	@Transient
	public Boolean getPlanSaatGebeSaatiKontrolEt() {
		boolean kontrolDurum = false;
		if (isGebeMi()) {
			BigDecimal value = getKatSayi(PuantajKatSayiTipi.GUN_GEBE_PLAN_KONTROL_ETME.value());
			kontrolDurum = value == null || value.intValue() == 0;
		}
		return kontrolDurum;
	}

	@Transient
	public HashMap<Integer, BigDecimal> getKatSayiMap() {
		return katSayiMap;
	}

	public void setKatSayiMap(HashMap<Integer, BigDecimal> katSayiMap) {
		this.katSayiMap = katSayiMap;
	}

	@Transient
	public String getDonemStr() {
		return donemStr;
	}

	public void setDonemStr(String donemStr) {
		this.donemStr = donemStr;
	}

	// SAAT_CALISAN_NORMAL_GUN(91), SAAT_CALISAN_IZIN_GUN(92), SAAT_CALISAN_HAFTA_TATIL(93), SAAT_CALISAN_RESMI_TATIL(
	@Transient
	public double getSaatCalisanNormalGunKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_SAAT_CALISAN_NORMAL_GUN.value());
			katSayi = decimal != null ? decimal.doubleValue() : 9.0d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanIzinGunKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_SAAT_CALISAN_IZIN_GUN.value());

			katSayi = decimal != null ? decimal.doubleValue() : 9.0d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanHaftaTatilKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_SAAT_CALISAN_HAFTA_TATIL.value());
			katSayi = decimal != null ? decimal.doubleValue() : 7.5d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanResmiTatilKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_SAAT_CALISAN_RESMI_TATIL.value());
			katSayi = decimal != null ? decimal.doubleValue() : 7.5d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanGunlukKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_SAAT_CALISAN_GUN.value());
			katSayi = decimal != null ? decimal.doubleValue() : 7.5d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanArifeNormalKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_SAAT_CALISAN_ARIFE_NORMAL_SAAT.value());
			katSayi = decimal != null ? decimal.doubleValue() : 3.75d;
		}
		return katSayi;
	}

	@Transient
	public double getSaatCalisanArifeTatilKatsayisi() {
		double katSayi = 0.0d;
		if (ayinGunu) {
			BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_SAAT_CALISAN_ARIFE_TATIL_SAAT.value());
			katSayi = decimal != null ? decimal.doubleValue() : 3.75d;
		}
		return katSayi;
	}

	@Transient
	public boolean isFazlaMesaiTalepDurum() {
		if (!fazlaMesaiTalepDurum && izin == null && ayinGunu) {
			BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_FMT_DURUM.value());
			fazlaMesaiTalepDurum = decimal != null && decimal.intValue() > 0;
		}
		return fazlaMesaiTalepDurum;
	}

	public void setFazlaMesaiTalepDurum(boolean fazlaMesaiTalepDurum) {
		this.fazlaMesaiTalepDurum = fazlaMesaiTalepDurum;
	}

	@Transient
	public Personel getPdksPersonel() {
		return personel;
	}

	@Transient
	public Boolean getIzinHaftaTatilDurum() {
		return izinHaftaTatilDurum;
	}

	public void setIzinHaftaTatilDurum(Boolean izinHaftaTatilDurum) {
		this.izinHaftaTatilDurum = izinHaftaTatilDurum;
	}

	@Transient
	public boolean isYemekHesabiSureEkle() {
		BigDecimal decimal = getKatSayi(PuantajKatSayiTipi.GUN_YEMEK_SURE_EKLE_DURUM.value());
		boolean tatilYemekHesabiSureEkle = decimal != null && decimal.intValue() > 0;
		return tatilYemekHesabiSureEkle;
	}

	@Transient
	public boolean isAyarlamaBitti() {
		return ayarlamaBitti;
	}

	public void setAyarlamaBitti(boolean ayarlamaBitti) {
		this.ayarlamaBitti = ayarlamaBitti;
	}

	@Transient
	public double getAksamKatSayisi() {
		return aksamKatSayisi;
	}

	public void setAksamKatSayisi(double value) {
		if (value != 0.0d)
			logger.debug(value);
		this.aksamKatSayisi = value;
	}

	@Transient
	public double getYasalMaxSure() {
		return yasalMaxSure;
	}

	public void setYasalMaxSure(double yasalMaxSure) {
		if (this.isFcsDahil())
			this.yasalMaxSure = yasalMaxSure;
	}

	@Transient
	public boolean isFcsDahil() {
		boolean fcsDahil = false;
		if (vardiya != null)
			fcsDahil = vardiya.getFcsHaric() == null || vardiya.getFcsHaric().booleanValue() == false;

		return fcsDahil;
	}

	@Transient
	public boolean isPlanHareketEkle() {
		return planHareketEkle;
	}

	public void setPlanHareketEkle(boolean planHareketEkle) {
		this.planHareketEkle = planHareketEkle;
	}

	@Transient
	public boolean isGebeMi() {
		return gebeMi;
	}

	public void setGebeMi(boolean gebeMi) {
		this.gebeMi = gebeMi;
	}

	@Transient
	public boolean isSutIzniVar() {
		return sutIzniVar;
	}

	public void setSutIzniVar(boolean sutIzniVar) {
		this.sutIzniVar = sutIzniVar;
	}

	@Transient
	public boolean isBayramAyir() {
		return bayramAyir;
	}

	public void setBayramAyir(boolean bayramAyir) {
		this.bayramAyir = bayramAyir;
	}

	@Transient
	public boolean isGecmisHataliDurum() {
		return gecmisHataliDurum;
	}

	public void setGecmisHataliDurum(boolean gecmisHataliDurum) {
		this.gecmisHataliDurum = gecmisHataliDurum;
	}

	public static Date getSaniyeYuvarlaZaman() {
		return saniyeYuvarlaZaman;
	}

	public static void setSaniyeYuvarlaZaman(Date saniyeYuvarlaZaman) {
		VardiyaGun.saniyeYuvarlaZaman = saniyeYuvarlaZaman;
	}

	@Transient
	public boolean isCihazZamanSaniyeSifirla() {
		return cihazZamanSaniyeSifirla;
	}

	public void setCihazZamanSaniyeSifirla(boolean cihazZamanSaniyeSifirla) {
		this.cihazZamanSaniyeSifirla = cihazZamanSaniyeSifirla;
	}

	@Transient
	public Double getResmiTatilKanunenEklenenSure() {
		return resmiTatilKanunenEklenenSure;
	}

	public void setResmiTatilKanunenEklenenSure(Double resmiTatilKanunenEklenenSure) {
		this.resmiTatilKanunenEklenenSure = resmiTatilKanunenEklenenSure;
	}

	@Transient
	public Double getIcapciMesaiSaat() {
		return icapciMesaiSaat;
	}

	public void setIcapciMesaiSaat(Double icapciMesaiSaat) {
		this.icapciMesaiSaat = icapciMesaiSaat;
	}

	@Transient
	public boolean isVardiyaOnay() {
		return vardiyaOnayli == null || vardiyaOnayli.booleanValue();
	}

	public void entityRefresh() {

	}

	@Transient
	public String getTableName() {
		return TABLE_NAME;
	}

}
