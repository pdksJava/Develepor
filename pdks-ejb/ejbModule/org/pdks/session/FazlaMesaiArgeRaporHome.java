package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelDinamikAlan;
import org.pdks.entity.PersonelFazlaMesai;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaEkSaat;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaHafta;
import org.pdks.entity.VardiyaPlan;
import org.pdks.entity.VardiyaSaat;
import org.pdks.entity.YemekIzin;
import org.pdks.enums.BordroDetayTipi;
import org.pdks.security.action.UserHome;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

@Name("fazlaMesaiArgeRaporHome")
public class FazlaMesaiArgeRaporHome extends EntityHome<DepartmanDenklestirmeDonemi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1549353535370361547L;

	static Logger logger = Logger.getLogger(FazlaMesaiArgeRaporHome.class);

	public static String sayfaURL = "fazlaMesaiArgeRapor";

	@RequestParameter
	Long personelDenklestirmeId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	UserHome userHome;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;
	@Out(scope = ScopeType.SESSION, required = false)
	String linkAdres;
	@Out(scope = ScopeType.SESSION, required = false)
	String bordroAdres;
	@Out(scope = ScopeType.SESSION, required = false)
	VardiyaGun fazlaMesaiVardiyaGun;

	@In(required = true, create = true)
	Renderer renderer;

	private List<PersonelDenklestirme> personelDenklestirmeList;

	private List<SelectItem> bolumDepartmanlari, gorevYeriList, tesisList;

	private List<AylikPuantaj> aylikPuantajList, onayList;

	private List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList;

	private List<PersonelDenklestirme> baslikDenklestirmeDonemiList;

	private HashMap<Long, List<HareketKGS>> personelHareketMap;

	private HashMap<String, List<Tanim>> ekSahaListMap;

	private List<Tanim> denklestirmeDinamikAlanlar, personelDinamikAlanlar;

	private VardiyaGun vardiyaGun;

	private Sirket sirket;

	private DenklestirmeAy denklestirmeAy;

	private TreeMap<String, Tatil> tatilGunleriMap;
	private TreeMap<String, PersonelDinamikAlan> personelDinamikAlanMap;
	private AylikPuantaj seciliAylikPuantaj;
	private CalismaModeli seciliCalismaModeli;

	private Boolean hataYok, argeGoster = Boolean.FALSE, argeTableGoster = Boolean.FALSE, fazlaMesaiIzinKullan = Boolean.FALSE, fazlaMesaiOde = Boolean.FALSE, yetkili = Boolean.FALSE, resmiTatilVar = Boolean.FALSE, haftaTatilVar = Boolean.FALSE, kaydetDurum = Boolean.FALSE;
	private Boolean onayla, hastaneSuperVisor = Boolean.FALSE, sirketIzinGirisDurum = Boolean.FALSE, hataliPuantajVar = Boolean.FALSE, secimDurum;
	private Boolean kimlikGoster = Boolean.FALSE, aksamGun = Boolean.FALSE, maasKesintiGoster = Boolean.FALSE, aksamSaat = Boolean.FALSE, hataliPuantajGoster = Boolean.FALSE, stajerSirket, departmanBolumAyni = Boolean.FALSE;
	private Boolean modelGoster = Boolean.FALSE, kullaniciPersonel = Boolean.FALSE, sirketGoster = Boolean.FALSE, tesisDurum = Boolean.FALSE, denklestirmeAyDurum = Boolean.FALSE, yoneticiERP1Kontrol = Boolean.FALSE, icapciSaatGoster = Boolean.FALSE, yasalFazlaCalismaAsanSaat = Boolean.FALSE;
	private boolean adminRole, ikRole, bordroPuantajEkranindaGoster = false, vardiyaPlanTopluAdet = false, fazlaMesaiVar = false, saatlikMesaiVar = false, aylikMesaiVar = false;
	private Boolean gerceklesenMesaiKod = Boolean.FALSE, isAramaDurum = Boolean.FALSE, devredenBakiyeKod = Boolean.FALSE, normalCalismaSaatKod = Boolean.FALSE, haftaTatilCalismaSaatKod = Boolean.FALSE, resmiTatilCalismaSaatKod = Boolean.FALSE, izinSureSaatKod = Boolean.FALSE;
	private Boolean normalCalismaGunKod = Boolean.FALSE, haftaTatilCalismaGunKod = Boolean.FALSE, resmiTatilCalismaGunKod = Boolean.FALSE, izinSureGunKod = Boolean.FALSE, ucretliIzinGunKod = Boolean.FALSE, ucretsizIzinGunKod = Boolean.FALSE, hastalikIzinGunKod = Boolean.FALSE;
	private Boolean normalGunKod = Boolean.FALSE, haftaTatilGunKod = Boolean.FALSE, resmiTatilGunKod = Boolean.FALSE, artikGunKod = Boolean.FALSE, bordroToplamGunKod = Boolean.FALSE, devredenMesaiKod = Boolean.FALSE, ucretiOdenenKod = Boolean.FALSE;

	private Boolean suaDurum = Boolean.FALSE, sutIzniDurum = Boolean.FALSE, gebeDurum = Boolean.FALSE, partTime = Boolean.FALSE, pdfBirlestirDurum = Boolean.FALSE, pdfTopluAktarDurum = Boolean.FALSE;
	private Boolean brutUcretGoster = Boolean.FALSE, resmiTatilKanunenEklenenSureGoster = Boolean.FALSE , izinGoster = Boolean.FALSE;
	private Long vardiyaAdet, yukseklik;
	private List<VardiyaGun> tumVardiyaList;
	private Parameter aylikVardiyaTabloHareketExcelParameter, aylikVardiyaTabloHareketPDFParameter;

	private List<Vardiya> izinTipiVardiyaList;
	private TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap;

	private HashMap<String, Long> vardiyaAdetMap;

	private TreeMap<String, Boolean> baslikMap;

	private int ay, yil, maxYil, colSpan = 1, rowSpan = 1;
	private HashMap<String, String> vardiyaZamanMap;
	private List<User> toList, ccList, bccList;

	private TreeMap<Long, List<FazlaMesaiTalep>> fmtMap;

	private List<FazlaMesaiTalep> fmtList;

	private List<SelectItem> aylar;

	private AylikPuantaj aylikPuantajDefault;

	private TreeMap<String, Tanim> ekSahaTanimMap;

	private String msgError, msgFazlaMesaiError, sanalPersonelAciklama, bolumAciklama;
	private String sicilNo = "", excelDosyaAdi, mailKonu, mailIcerik, duzeltTipi;
	private List<YemekIzin> yemekAraliklari;
	private CalismaModeli perCalismaModeli;
	private Long seciliEkSaha3Id, sirketId = null, departmanId, gorevTipiId, tesisId;
	private Tanim gorevYeri, seciliBolum, tesis, ekSaha4Tanim;

	private Double toplamFazlamMesai = 0D;
	private Double aksamCalismaSaati = null, aksamCalismaSaatiYuzde = null;
	private byte[] excelData;

	private boolean mailGonder, tekSirket;
	private Boolean bakiyeGuncelle, ayrikHareketVar, hatalariDuzelt;

	private List<SelectItem> pdksSirketList, departmanList, duzeltTipleri;
	private Departman departman;
	private String adres, personelIzinGirisiStr, personelHareketStr, personelFazlaMesaiOrjStr, personelFazlaMesaiStr, vardiyaPlaniStr;
	private List<String> sabahVardiyalar;
	private Vardiya sabahVardiya;
	private Integer aksamVardiyaBasSaat, aksamVardiyaBasDakika, aksamVardiyaBitDakika;
	private TreeMap<String, Tanim> fazlaMesaiMap;
	private Session session;

	@Override
	public Object getId() {
		if (personelDenklestirmeId == null) {
			return super.getId();
		} else {
			return personelDenklestirmeId;
		}
	}

	@Override
	public void create() {
		super.create();
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			pdksEntityController.sessionRefresh(session, entityManager, getInstance());
	}

	/**
	 * 
	 */
	private void adminRoleDurum() {
		adminRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIKAdmin();
		ikRole = PdksUtil.getIkRole(authenticatedUser);
	}

	private void aylikPuantajListClear() {
		if (aylikPuantajList != null)
			aylikPuantajList.clear();
		else
			aylikPuantajList = ortakIslemler.getSelectItemList("aylikPuantaj", authenticatedUser);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		Connection connection = pdksEntityController.sessionKapat(session);
		if (pdksEntityController.isSessionKapali(connection, session))
			session = PdksUtil.getSessionUserCalistiSayfa(entityManager, authenticatedUser, sayfaURL);
		ortakIslemler.setUserMenuItemTime(entityManager, session, sayfaURL);
		resmiTatilKanunenEklenenSureGoster = Boolean.FALSE;
		aylikPuantajListClear();
		if (personelDinamikAlanMap == null)
			personelDinamikAlanMap = new TreeMap<String, PersonelDinamikAlan>();
		else
			personelDinamikAlanMap.clear();
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals(sayfaURL);
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa(sayfaURL);
		tumVardiyaList = null;
		vardiyaAdetMap = null;
		if (baslikMap == null)
			baslikMap = new TreeMap<String, Boolean>();
		else
			baslikMap.clear();
		duzeltTipleri = ortakIslemler.getSelectItemList("duzeltTip", authenticatedUser);
		duzeltTipleri.add(new SelectItem("T", "Tam çalışmış"));
		duzeltTipleri.add(new SelectItem("S", "Süre sayma"));
		duzeltTipi = (String) duzeltTipleri.get(0).getValue();
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

		String ayStr = (String) req.getParameter("ay");
		String yilStr = (String) req.getParameter("yil");
		String linkAdresKey = null;
		sirketGoster = Boolean.FALSE;
		fazlaMesaiVardiyaGun = null;
		adminRoleDurum();
		if (!authenticatedUser.isAdmin()) {
			if (departmanId == null && !authenticatedUser.isYoneticiKontratli())
				setDepartmanId(authenticatedUser.getDepartman().getId());

			// fillSirketList();
		}
		try {
			suaDurum = false;
			sutIzniDurum = Boolean.FALSE;
			partTime = Boolean.FALSE;
			gebeDurum = Boolean.FALSE;
			isAramaDurum = Boolean.FALSE;
			modelGoster = Boolean.FALSE;
			departmanBolumAyni = Boolean.FALSE;
			bakiyeGuncelle = null;
			stajerSirket = Boolean.FALSE;
			argeGoster = Boolean.FALSE;
			argeTableGoster = Boolean.FALSE;
			mailGonder = Boolean.FALSE;
			kimlikGoster = Boolean.FALSE;
			yasalFazlaCalismaAsanSaat = Boolean.FALSE;
			icapciSaatGoster = Boolean.FALSE;
			hataliPuantajVar = Boolean.FALSE;
			fazlaMesaiOde = Boolean.FALSE;
			fazlaMesaiIzinKullan = Boolean.FALSE;
			setSirket(null);
			sirketId = null;
			setTesisId(null);
			setTesisList(null);
			seciliEkSaha3Id = null;
			Calendar cal = Calendar.getInstance();
			ortakIslemler.gunCikar(cal, 2);
			ay = cal.get(Calendar.MONTH) + 1;
			yil = cal.get(Calendar.YEAR);
			maxYil = yil + 1;

			setInstance(new DepartmanDenklestirmeDonemi());
			// setSirket(null);

			if (authenticatedUser.isSuperVisor() || authenticatedUser.isProjeMuduru()) {
				setSirket(authenticatedUser.getPdksPersonel().getSirket());
				bolumDoldur();
			}

			Departman pdksDepartman = null;
			if (!authenticatedUser.isAdmin())
				pdksDepartman = authenticatedUser.getDepartman();

			getInstance().setDepartman(pdksDepartman);

			hastaneSuperVisor = Boolean.FALSE;
			if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()) && authenticatedUser.getSuperVisorHemsirePersonelNoList() != null) {
				String superVisorHemsireSayfalari = ortakIslemler.getParameterKey("superVisorHemsireSayfalari");
				List<String> sayfalar = PdksUtil.hasStringValue(superVisorHemsireSayfalari) ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
				hastaneSuperVisor = sayfalar != null && sayfalar.contains("fazlaMesaiOzetRapor");

			}

			if (!hastaneSuperVisor && (authenticatedUser.isAdmin() || authenticatedUser.getDepartman().isAdminMi())) {
				List<Tanim> statuTanimList = null;
				HashMap fields = new HashMap();
				if (authenticatedUser.isYonetici() || authenticatedUser.isYoneticiKontratli()) {
					if (!authenticatedUser.isIKAdmin())
						fields.put("pdksSicilNo <> ", authenticatedUser.getPdksPersonel().getPdksSicilNo());
					fields.put("pdksSicilNo", authenticatedUser.getYetkiTumPersonelNoList());
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Personel> list = pdksEntityController.getObjectByInnerObjectListInLogic(fields, Personel.class);
					TreeMap<Long, Tanim> tanimMap = new TreeMap<Long, Tanim>();
					for (Personel personel : list) {
						if (personel.getEkSaha3() != null)
							tanimMap.put(personel.getEkSaha3().getId(), personel.getEkSaha3());

					}
					statuTanimList = new ArrayList<Tanim>(tanimMap.values());
					tanimMap = null;
					list = null;
				} else {
					Tanim parent = ortakIslemler.getSQLTanimByTipKodu(Tanim.TIPI_PERSONEL_EK_SAHA, "ekSaha3", session);
					if (parent != null) {
						StringBuilder sb = new StringBuilder();
						fields = new HashMap();
						sb.append("select  * from " + Tanim.TABLE_NAME + "  " + PdksEntityController.getSelectLOCK() + " ");
						sb.append(" where " + Tanim.COLUMN_NAME_PARENT_ID + " = :t and " + Tanim.COLUMN_NAME_DURUM + " = 1 ");
						fields.put("t", parent.getId());
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						statuTanimList = pdksEntityController.getObjectBySQLList(sb, fields, Tanim.class);
					}

				}

				if (statuTanimList != null && !statuTanimList.isEmpty()) {

					if (statuTanimList.size() > 1)
						statuTanimList = PdksUtil.sortObjectStringAlanList(statuTanimList, "getAciklama", null);
					else {
						gorevYeri = statuTanimList.get(0);
						seciliEkSaha3Id = gorevYeri.getId();
					}

				}

			}

			setPersonelDenklestirmeList(new ArrayList<PersonelDenklestirme>());

			boolean hareketDoldur = false;
			String gorevTipiIdStr = null, gorevYeriIdStr = null, sirketIdStr = null, tesisIdStr = null;
			LinkedHashMap<String, Object> veriLastMap = null;
			if (linkAdresKey == null) {
				veriLastMap = ortakIslemler.getLastParameter(sayfaURL, session);
				if (veriLastMap != null) {
					tesisId = null;
					seciliEkSaha3Id = null;
					if (veriLastMap.containsKey("yil"))
						yilStr = (String) veriLastMap.get("yil");
					if (veriLastMap.containsKey("ay"))
						ayStr = (String) veriLastMap.get("ay");
					if (veriLastMap.containsKey("sirketId"))
						sirketIdStr = (String) veriLastMap.get("sirketId");
					if (veriLastMap.containsKey("tesisId"))
						tesisIdStr = (String) veriLastMap.get("tesisId");
					if (veriLastMap.containsKey("bolumId"))
						gorevYeriIdStr = (String) veriLastMap.get("bolumId");
					if ((authenticatedUser.isIK() || authenticatedUser.isAdmin()) && veriLastMap.containsKey("sicilNo"))
						sicilNo = (String) veriLastMap.get("sicilNo");
					if (veriLastMap.containsKey("duzeltTipi")) {
						duzeltTipi = (String) veriLastMap.get("duzeltTipi");
						hatalariDuzelt = PdksUtil.hasStringValue(duzeltTipi);

					}

				}
			}
			if (linkAdresKey != null || (ayStr != null && yilStr != null)) {
				if (linkAdresKey != null) {

					PersonelDenklestirme pd = (PersonelDenklestirme) pdksEntityController.getSQLParamByFieldObject(PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.COLUMN_NAME_ID, Long.parseLong(linkAdresKey), PersonelDenklestirme.class, session);

					if (pd != null) {
						ortakIslemler.setPersonelDenklestirmeDevir(pd, null, session);

						DenklestirmeAy da = pd.getDenklestirmeAy();
						yilStr = String.valueOf(da.getYil());
						ayStr = String.valueOf(da.getAy());
						Personel personel = pd.getPdksPersonel();
						sirketIdStr = String.valueOf(personel.getSirket().getId());
						if (personel.getEkSaha3() != null)
							gorevYeriIdStr = String.valueOf(personel.getEkSaha3().getId());
						if (personel.getSirket().getTesisDurum() && personel.getTesis() != null)
							tesisIdStr = String.valueOf(personel.getTesis().getId());
					}

				} else if (veriLastMap == null || veriLastMap.isEmpty()) {
					gorevTipiIdStr = (String) req.getParameter("gorevTipiId");
					gorevYeriIdStr = (String) req.getParameter("gorevYeriId");
					tesisIdStr = (String) req.getParameter("tesisId");
					sirketIdStr = (String) req.getParameter("sirketId");
				}

				if (yilStr != null && ayStr != null) {
					yil = Integer.parseInt(yilStr);
					ay = Integer.parseInt(ayStr);
					if (sirketIdStr != null) {
						sirketId = Long.parseLong(sirketIdStr);
						if (sirket != null) {
							if (!sirket.getId().equals(sirketId))
								sirket = null;
						}

						sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);

						if (sirket != null) {
							departmanId = sirket.getDepartman().getId();
							fillSirketList();
							if (sirket != null)
								sirketId = sirket.getId();
							if (tesisId != null)
								tesisDoldur(false);
						}

					}
					if (sirket != null) {
						departmanId = sirket.getDepartman().getId();
						setDepartman(sirket.getDepartman());
					}
					if (gorevTipiIdStr != null)
						gorevTipiId = Long.parseLong(gorevTipiIdStr);
					if (gorevYeriIdStr != null)
						seciliEkSaha3Id = Long.parseLong(gorevYeriIdStr);
					hareketDoldur = true;

				}

			}
			linkAdres = null;
			if (!authenticatedUser.isAdmin() && !authenticatedUser.isIK() && !authenticatedUser.isYoneticiKontratli()) {
				sirket = authenticatedUser.getPdksPersonel().getSirket();
				sirketId = sirket.getId();
			}
			setDepartman(departmanId != null ? (Departman) pdksEntityController.getSQLParamByFieldObject(Departman.TABLE_NAME, Departman.COLUMN_NAME_ID, departmanId, Departman.class, session) : null);
			if (sirketId == null)
				fillSirketList();
			if (tesisIdStr != null) {
				if (!tesisList.isEmpty())
					setTesisId(Long.parseLong(tesisIdStr));
				else
					tesisIdStr = null;
			}
			if (departman != null && !departman.isAdminMi()) {
				if (bolumDepartmanlari == null && departman != null)
					bolumDepartmanlari = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, denklestirmeAy != null ? getFazlaMesaiAylikPuantaj() : null, Boolean.TRUE, session);
			} else if (sirket != null && sirket.isTesisDurumu())
				tesisDoldur(false);
			if (tesisIdStr != null)
				setTesisId(Long.parseLong(tesisIdStr));
			if (tesisId != null || gorevYeriIdStr != null)
				bolumDoldur();
			if (veriLastMap == null && hareketDoldur)
				fillFazlaMesaiArgeRaporList();
			denklestirmeAyDurum = denklestirmeAy != null && denklestirmeAy.getDurum();
			if (denklestirmeAyDurum.equals(Boolean.FALSE))
				hataliPuantajGoster = denklestirmeAyDurum;
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
		if (kullaniciPersonel) {
			tesisList = null;
			sicilNo = authenticatedUser.getPdksPersonel().getPdksSicilNo();
		}
		fillEkSahaTanim();
		yilDegisti();
		linkAdresKey = (String) req.getParameter("linkAdresKey");
		pdfTopluAktarDurum = false;
		if (linkAdresKey != null)
			fillFazlaMesaiArgeRaporList();
		return "";
	}

	public String topluSec() {
		for (AylikPuantaj ap : aylikPuantajList) {
			ap.setSecili(secimDurum);
		}
		return "";
	}

	public String pdfKontrol() {
		boolean islemYap = false;
		String fileName = ortakIslemler.getParameterKey("mesaiDenklestirmeBelge");
		File file = null;
		if (onayList == null)
			onayList = new ArrayList<AylikPuantaj>();
		else
			onayList.clear();
		if (PdksUtil.hasStringValue(fileName)) {
			file = new File("/opt/pdks/" + fileName);
			if (file.exists()) {
				boolean renk = true;
				for (AylikPuantaj ap : aylikPuantajList)
					if (ap.isSecili()) {
						ap.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
						onayList.add(ap);
						renk = !renk;
					}
				islemYap = onayList.isEmpty() == false;
				if (islemYap == false)
					PdksUtil.addMessageWarn("İşlem yapılacak seçili kayıt yok!");
			} else if (islemYap == false)
				PdksUtil.addMessageWarn("İşlem yapılacak dosya örneği yok!");
		}

		return "";
	}

	/**
	 * @param tempIzin
	 * @return
	 * @throws Exception
	 */
	public String pdfTopluAktar(boolean zip) throws Exception {
		String sayfa = "";
		HashMap<Long, AylikPuantaj> veriMap = new HashMap<Long, AylikPuantaj>();
		for (AylikPuantaj ap : onayList)
			veriMap.put(ap.getPdksPersonel().getId(), ap);
		String fileName = ortakIslemler.getParameterKey("mesaiDenklestirmeBelge");
		File file = new File("/opt/pdks/" + fileName);
		List<String> list = PdksUtil.getStringListFromFile(file);
		LinkedHashMap<Long, byte[]> map1 = null;
		map1 = ortakIslemler.getOnayPdf(denklestirmeAy, veriMap, new ArrayList<String>(list), session);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		if (zip) {
			String path = "/tmp/";
			File tmp = new File(path);
			if (!tmp.exists())
				tmp.mkdir();
			ZipOutputStream zos = new ZipOutputStream(outputStream);
			for (Long key : map1.keySet()) {
				byte[] bytes = map1.get(key);
				Personel personel = veriMap.get(key).getPdksPersonel();
				String zipDosyaAdi = (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() + "/" : "") + personel.getAdSoyad() + "_" + personel.getPdksSicilNo() + ".pdf";
				ZipEntry zipEntry = new ZipEntry(zipDosyaAdi);
				zos.putNextEntry(zipEntry);
				int length = bytes.length;
				zos.write(bytes, 0, length);
				zos.closeEntry();
			}
			zos.close();
		} else {
			Document document = new Document(PageSize.A4.rotate());
			PdfCopy copy = new PdfCopy(document, outputStream);
			document.open();
			PdfContentByte pageContentByte = copy.getDirectContent();
			for (Long key : map1.keySet()) {
				byte[] data = map1.get(key);
				PdfReader reader = new PdfReader(data);
				for (int i = 1; i <= reader.getNumberOfPages(); i++) {
					document.newPage();
					// import the page from source pdf
					PdfImportedPage page = copy.getImportedPage(reader, i);
					pageContentByte.addTemplate(page, 0, 0);
					copy.addPage(page);
				}

			}
			document.close();
		}
		outputStream.flush();
		outputStream.close();
		String characterEncoding = "ISO-8859-9";
		String contentType = "application/" + (zip ? "zip" : "pdf") + ";charset=" + characterEncoding;
		String disposition = zip || map1.size() > 1 ? "attachment" : "inline";
		String dosyaAdi = fileName.substring(0, fileName.lastIndexOf(".")) + (zip ? ".zip" : ".pdf");
		String fileNameURL = PdksUtil.encoderURL(dosyaAdi, characterEncoding);
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.setContentType(contentType);
		response.setCharacterEncoding(characterEncoding);
		response.setHeader("Content-Disposition", disposition + ";filename=" + fileNameURL);
		PdksUtil.writeByteArrayOutputStream(response, outputStream);
		file = new File(dosyaAdi);
		if (file != null && file.exists())
			file.delete();
		sayfa = null;
		map1 = null;
		list = null;
		veriMap = null;
		return sayfa;
	}

	/**
	 * 
	 */
	private void setSeciliDenklestirmeAy() {
		aylikPuantajList.clear();
		if (denklestirmeAy == null && ay > 0) {
			HashMap fields = new HashMap();
			denklestirmeAy = ortakIslemler.getSQLDenklestirmeAy(yil, ay, session);
			if (denklestirmeAy != null) {
				if (denklestirmeAy.getFazlaMesaiMaxSure() == null)
					fazlaMesaiOrtakIslemler.setFazlaMesaiMaxSure(denklestirmeAy, session);
				StringBuilder sb = new StringBuilder();
				sb.append("select " + PersonelDenklestirme.COLUMN_NAME_ID + "  from " + PersonelDenklestirme.TABLE_NAME + "  " + PdksEntityController.getSelectLOCK());
				sb.append(" where " + PersonelDenklestirme.COLUMN_NAME_DONEM + " = :t and " + PersonelDenklestirme.COLUMN_NAME_DENKLESTIRME_DURUM + " = 1");
				fields.put("t", denklestirmeAy.getId());
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List idList = pdksEntityController.getObjectBySQLList(sb, fields, null);
				if (idList.isEmpty()) {
					denklestirmeAy = null;
					PdksUtil.addMessageAvailableWarn((ay > 0 ? yil + " " + (aylar.get(ay - 1).getLabel()) : "") + " döneme ait denkleştirme verisi tanımlanmamıştır!");
				}
				idList = null;
			} else
				PdksUtil.addMessageAvailableError((ay > 0 ? yil + " " + (aylar.get(ay - 1).getLabel()) : "") + " döneme ait çalışma planı tanımlanmamıştır!");
		}
		setDenklestirmeAyDurum(fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy));
	}

	private AylikPuantaj getFazlaMesaiAylikPuantaj() {
		AylikPuantaj ap = new AylikPuantaj(denklestirmeAy);
		ap.setArge(true);
		return ap;
	}

	private void fillDepartmanList() {
		if (denklestirmeAy == null)
			setSeciliDenklestirmeAy();
		List<SelectItem> departmanListe = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(denklestirmeAy != null ? getFazlaMesaiAylikPuantaj() : null, true, session);
		if (!departmanListe.isEmpty()) {
			Long onceki = departmanId;
			if (departmanListe.size() == 1)
				departmanId = (Long) departmanListe.get(0).getValue();
			else if (onceki != null) {
				for (SelectItem st : departmanListe) {
					if (st.getValue().equals(onceki))
						departmanId = onceki;
				}
			}
		} else
			departmanId = null;
		setDepartmanList(departmanListe);
	}

	private void yilDegisti() {

		aylar = ortakIslemler.getSelectItemList("ay", authenticatedUser);
		ay = fazlaMesaiOrtakIslemler.aylariDoldur(yil, ay, aylar, session);
	}

	public String departmanDegisti(boolean degisti) {
		yilDegisti();
		if (ay <= 0 && !aylar.isEmpty())
			ay = (Integer) aylar.get(aylar.size() - 1).getValue();

		if (degisti) {
			setSeciliDenklestirmeAy();
			if (tesisList != null)
				tesisList.clear();
			if (gorevYeriList != null)
				gorevYeriList.clear();
			if (bolumDepartmanlari != null)
				bolumDepartmanlari.clear();
			denklestirmeAy = null;
		}
		fillSirketList();
		if (!pdksSirketList.isEmpty()) {
			boolean bolumDoldurulmadi = true;
			if (sirketId != null || pdksSirketList.size() == 1) {
				Long tesisIdOnceki = tesisId;
				if (pdksSirketList.size() == 1)
					sirketId = (Long) pdksSirketList.get(0).getValue();
				try {

					tesisDoldur(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (tesisList.size() == 1) {
					tesisId = (Long) tesisList.get(0).getValue();
					bolumDoldur();
					bolumDoldurulmadi = false;
				} else if (tesisIdOnceki != null && !tesisList.isEmpty()) {
					for (SelectItem si : tesisList) {
						Long id = (Long) si.getValue();
						if (id.equals(tesisIdOnceki))
							tesisId = tesisIdOnceki;
					}
					if (tesisId == null)
						seciliEkSaha3Id = null;
				}
			}
			if (bolumDoldurulmadi)
				if (tesisId != null || seciliEkSaha3Id != null || (sirket != null && sirket.isTesisDurumu() == false))
					bolumDoldur();
		}
		aylikPuantajList.clear();
		return "";
	}

	private void bolumleriTemizle() {
		gorevYeriList = null;

	}

	public Boolean puantajSec(AylikPuantaj ap) {
		setSeciliAylikPuantaj(ap);
		return ap == null;
	}

	public void fillSirketList() {
		Long oncekiSirketId = sirketId;
		if (adminRole)
			fillDepartmanList();
		List<SelectItem> sirketler = null;
		bolumleriTemizle();
		ekSaha4Tanim = null;
		try {
			if (denklestirmeAy == null)
				setSeciliDenklestirmeAy();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (departmanId != null) {
			setDepartman((Departman) pdksEntityController.getSQLParamByFieldObject(Departman.TABLE_NAME, Departman.COLUMN_NAME_ID, departmanId, Departman.class, session));

		} else
			setDepartman(null);

		if (gorevYeriList != null)
			gorevYeriList.clear();

		if (ikRole || authenticatedUser.isYonetici()) {
			Long depId = departman != null ? departman.getId() : null;
			sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(depId, denklestirmeAy != null ? getFazlaMesaiAylikPuantaj() : null, true, session);
			sirket = null;
			if (!sirketler.isEmpty()) {

				if (sirketler.size() == 1) {
					sirketId = (Long) sirketler.get(0).getValue();
				} else if (oncekiSirketId != null) {
					if (ikRole)
						sirketId = null;
					for (SelectItem st : sirketler) {
						if (st.getValue().equals(oncekiSirketId))
							sirketId = oncekiSirketId;
					}
				}
				if (sirketId != null) {
					sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);

				}
			}
			setPdksSirketList(sirketler);
		} else {
			setSirket(authenticatedUser.getPdksPersonel().getSirket());
		}
		ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);

		aylikPuantajList.clear();
		setPersonelDenklestirmeList(new ArrayList<PersonelDenklestirme>());

	}

	public String fillPersonelSicilDenklestirmeList() {
		if (!PdksUtil.hasStringValue(sicilNo))
			aylikPuantajList.clear();
		else {
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			fillFazlaMesaiArgeRaporList();
		}

		return "";
	}

	/**
	 * @param aylikPuantaj
	 * @return
	 */
	@Transactional
	public String saveFazlaMesaiLastParameter(AylikPuantaj aylikPuantaj) {
		Map<String, String> map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
		PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirme();
		DenklestirmeAy denklestirmeAy = personelDenklestirme.getDenklestirmeAy();
		String adres = map1.containsKey("host") ? map1.get("host") : "";
		Personel seciliPersonel = aylikPuantaj.getPdksPersonel();
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("yil", "" + denklestirmeAy.getYil());
		lastMap.put("ay", "" + denklestirmeAy.getAy());
		Sirket sirket = seciliPersonel.getSirket();
		if (sirket.getDepartman() != null)
			lastMap.put("departmanId", "" + sirket.getDepartman().getId());
		if (sirket != null)
			lastMap.put("sirketId", "" + sirket.getId());
		if (sirket.getTesisDurum() && seciliPersonel.getTesis() != null)
			lastMap.put("tesisId", "" + seciliPersonel.getTesis().getId());
		if (seciliPersonel.getEkSaha3() != null)
			lastMap.put("bolumId", "" + seciliPersonel.getEkSaha3().getId());
		if (seciliPersonel.getEkSaha4() != null) {
			Tanim ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
			if (ekSaha4Tanim != null)
				lastMap.put("altBolumId", "" + seciliPersonel.getEkSaha4().getId());
		}

		lastMap.put("sicilNo", seciliPersonel.getPdksSicilNo());
		String sayfa = MenuItemConstant.fazlaMesaiHesapla;
		if (personelDenklestirme.getSonDurum().equals(Boolean.TRUE) || personelDenklestirme.isOnaylandi()) {
			lastMap.put("calistir", Boolean.TRUE);
			lastMap.put("sayfaURL", FazlaMesaiHesaplaHome.sayfaURL);
		} else {
			lastMap.put("sayfaURL", VardiyaGunHome.sayfaURL);
			sayfa = MenuItemConstant.vardiyaPlani;
		}

		bordroAdres = "<a href='http://" + adres + "/" + sayfaURL + "?linkAdresKey=" + aylikPuantaj.getPersonelDenklestirme().getId() + "'>" + ortakIslemler.getCalistiMenuAdi(sayfaURL) + " Ekranına Geri Dön</a>";
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		return sayfa;
	}

	@Transactional
	public String fillFazlaMesaiArgeRaporList() {
		pdfTopluAktarDurum = false;

		bordroAdres = null;
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		maasKesintiGoster = Boolean.FALSE;
		sirketGoster = Boolean.FALSE;
		mailGonder = !(authenticatedUser.isIK() || authenticatedUser.isAdmin());
		linkAdres = null;
		session.clear();
		secimDurum = Boolean.FALSE;
		// fillSirketList();

		personelDenklestirmeList.clear();
		ayrikHareketVar = false;

		denklestirmeAy = ortakIslemler.getSQLDenklestirmeAy(yil, ay, session);
		denklestirmeAyDurum = denklestirmeAy != null && denklestirmeAy.getDurum();
		tatilGunleriMap = null;
		AylikPuantaj aylikPuantajSablon = null;
		if (denklestirmeAy != null) {
			try {
				DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
				aylikPuantajSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
				tatilGunleriMap = denklestirmeDonemi.getTatilGunleriMap();
				denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
				fillFazlaMesaiArgeRaporDevam(aylikPuantajSablon, denklestirmeDonemi);
			} catch (Exception ee) {
				logger.error(ee);
				ee.printStackTrace();
			}

		} else
			PdksUtil.addMessageWarn("İlgili döneme ait fazla mesai bulunamadı!");
		if (!(authenticatedUser.isIK() || authenticatedUser.isAdmin()))
			departmanBolumAyni = false;
		return "";
	}

	public String kaydetSec() {
		for (AylikPuantaj puantaj : aylikPuantajList) {
			PersonelDenklestirme personelDenklestirmeAylik = puantaj.getPersonelDenklestirme();
			if (puantaj.isDonemBitti() && personelDenklestirmeAylik.isOnaylandi() && personelDenklestirmeAylik.getSonDurum() && puantaj.isFazlaMesaiHesapla() && !personelDenklestirmeAylik.isErpAktarildi())
				puantaj.setKaydet(kaydetDurum);
			else
				puantaj.setKaydet(Boolean.FALSE);

		}
		return "";
	}

	/**
	 * @param denklestirmeDonemi
	 * @param puantajList
	 */
	protected void calismaPlaniDenklestir(DepartmanDenklestirmeDonemi denklestirmeDonemi, List<AylikPuantaj> puantajList) {
		String idariVardiyaKisaAdi = ortakIslemler.getParameterKey("idariVardiyaKisaAdi");
		Vardiya normalCalismaVardiya = ortakIslemler.getNormalCalismaVardiya(idariVardiyaKisaAdi, session);
		LinkedHashMap<String, Object> dataMap = new LinkedHashMap<String, Object>();
		if (ortakIslemler.getParameterKeyHasStringValue("bayramAyirGun"))
			dataMap.put("bayramAyirGun", ortakIslemler.getBayramAyirGun());
		dataMap.put("aylikPuantajList", puantajList);
		dataMap.put("basTarih", denklestirmeDonemi.getBaslangicTarih());
		dataMap.put("bitTarih", denklestirmeDonemi.getBitisTarih());
		dataMap.put("normalCalismaVardiya", normalCalismaVardiya);
		dataMap.put("denklestirmeAyDurum", denklestirmeAyDurum);
		dataMap.put("tatilGunleriMap", tatilGunleriMap);
		fazlaMesaiOrtakIslemler.calismaPlaniDenklestir(dataMap, session);
	}

	/**
	 * @param aylikPuantajSablon
	 * @param denklestirmeDonemi
	 */
	@Transactional
	public void fillFazlaMesaiArgeRaporDevam(AylikPuantaj apSablon, DepartmanDenklestirmeDonemi denklestirmeDonemi) throws Exception {
		fazlaMesaiVardiyaGun = null;
		tumVardiyaList = null;
		personelHareketMap = null;
		resmiTatilKanunenEklenenSureGoster = false;
 
		HashMap<Long, Vardiya> vardiyaMap = new HashMap<Long, Vardiya>();
		if (vardiyaZamanMap == null)
			vardiyaZamanMap = new HashMap<String, String>();
		else
			vardiyaZamanMap.clear();
		vardiyaAdetMap = null;
		if (seciliEkSaha3Id != null && vardiyaPlanTopluAdet)
			vardiyaAdetMap = new HashMap<String, Long>();
		aylikVardiyaTabloHareketExcelParameter = ortakIslemler.getAylikVardiyaTabloHareketExcelParameter(session);
		aylikVardiyaTabloHareketPDFParameter = ortakIslemler.getAylikVardiyaTabloHareketPDFParameter(session);
		bordroPuantajEkranindaGoster = ortakIslemler.getParameterKey("bordroPuantajEkranindaGoster").equals("1");
		fazlaMesaiVar = false;
		saatlikMesaiVar = false;
		aylikMesaiVar = false;
		pdfTopluAktarDurum = false;
		pdfBirlestirDurum = false;
		yoneticiERP1Kontrol = !ortakIslemler.getParameterKeyHasStringValue("yoneticiERP1Kontrol");
		Map<String, String> map1 = null;
		sanalPersonelAciklama = ortakIslemler.sanalPersonelAciklama();
		sabahVardiya = null;
		departmanBolumAyni = Boolean.FALSE;
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		haftaTatilVar = Boolean.FALSE;
		maasKesintiGoster = Boolean.FALSE;
		fazlaMesaiIzinKullan = Boolean.FALSE;
		kimlikGoster = Boolean.FALSE;
		argeGoster = Boolean.FALSE;
		argeTableGoster = Boolean.FALSE;
		yasalFazlaCalismaAsanSaat = Boolean.FALSE;
		icapciSaatGoster = Boolean.FALSE;
		hataliPuantajVar = Boolean.FALSE;
		fazlaMesaiOde = Boolean.FALSE;
		sirketIzinGirisDurum = Boolean.FALSE;
		suaDurum = Boolean.FALSE;
		sutIzniDurum = Boolean.FALSE;
		partTime = Boolean.FALSE;
		boolean renk = Boolean.TRUE;
		gebeDurum = Boolean.FALSE;
		isAramaDurum = Boolean.FALSE;
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		if (fmtMap == null)
			fmtMap = new TreeMap<Long, List<FazlaMesaiTalep>>();
		else
			fmtMap.clear();
		lastMap.put("yil", "" + yil);
		lastMap.put("ay", "" + ay);
		if (departmanId != null)
			lastMap.put("departmanId", "" + departmanId);
		if (sirketId != null)
			lastMap.put("sirketId", "" + sirketId);
		if (tesisId != null)
			lastMap.put("tesisId", "" + tesisId);
		if (seciliEkSaha3Id != null)
			lastMap.put("bolumId", "" + seciliEkSaha3Id);

		if ((authenticatedUser.isIK() || authenticatedUser.isAdmin()) && PdksUtil.hasStringValue(sicilNo))
			lastMap.put("sicilNo", sicilNo.trim());
		if (denklestirmeAyDurum && hatalariDuzelt != null) {
			if (hatalariDuzelt && PdksUtil.hasStringValue(duzeltTipi))
				lastMap.put("duzeltTipi", duzeltTipi.trim());
		}
		try {

			map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
			lastMap.put("sayfaURL", sayfaURL);
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		List<AylikPuantaj> puantajList = new ArrayList();

		try {
			departmanBolumAyni = sirket != null && sirket.isTesisDurumu() == false;
			adres = map1.containsKey("host") ? map1.get("host") : "";
			if (sicilNo != null)
				sicilNo = sicilNo.trim();
			hataYok = Boolean.FALSE;

			kaydetDurum = Boolean.FALSE;
			String aksamBordroBasZamani = ortakIslemler.getParameterKey("aksamBordroBasZamani"), aksamBordroBitZamani = ortakIslemler.getParameterKey("aksamBordroBitZamani");
			Integer[] basZaman = ortakIslemler.getSaatDakika(aksamBordroBasZamani), bitZaman = ortakIslemler.getSaatDakika(aksamBordroBitZamani);
			aksamVardiyaBasSaat = basZaman[0];
			aksamVardiyaBasDakika = basZaman[1];
			aksamVardiyaBitDakika = bitZaman[1];

			seciliBolum = null;

			setVardiyaGun(null);
			HashMap map = new HashMap();

			Sirket sirket = null;
			if (sirketId != null && sirketId > 0) {
				sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);

			}
			apSablon.setArge(true);
			List<Personel> donemPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, null, denklestirmeAy != null ? apSablon : null, true, session);

			List<Long> perIdList = new ArrayList<Long>();

			for (Personel personel : donemPerList) {
				if (PdksUtil.hasStringValue(sicilNo) == false || sicilNo.trim().equals(personel.getPdksSicilNo().trim())) {
					perIdList.add(personel.getId());
				}

			}
			List<PersonelDenklestirme> personelDenklestirmeler = null;
			if (!perIdList.isEmpty())
				personelDenklestirmeler = getPdksPersonelDenklestirmeler(perIdList);
			else
				personelDenklestirmeler = new ArrayList<PersonelDenklestirme>();

			HashMap<Long, PersonelDenklestirme> personelDenklestirmeMap = new HashMap<Long, PersonelDenklestirme>();
			TreeMap<Long, PersonelDenklestirme> personelDenklestirmeDonemMap = new TreeMap<Long, PersonelDenklestirme>();
			if (personelDenklestirmeler.isEmpty()) {

				PdksUtil.addMessageWarn("Çalışma planı kaydı bulunmadı!");

			}
			donemPerList.clear();
			for (Iterator iterator = personelDenklestirmeler.iterator(); iterator.hasNext();) {
				PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator.next();
				if (personelDenklestirme == null || personelDenklestirme.getPersonel() == null) {
					iterator.remove();
					continue;
				}
				personelDenklestirmeDonemMap.put(personelDenklestirme.getPersonelId(), personelDenklestirme);
				personelDenklestirme.setGuncellendi(personelDenklestirme.getId() == null);
				if (personelDenklestirme.isDenklestirmeDurum()) {
					personelDenklestirmeMap.put(personelDenklestirme.getPersonelId(), personelDenklestirme);
					donemPerList.add(personelDenklestirme.getPdksPersonel());
				} else
					iterator.remove();

			}

			personelDinamikAlanlar = PdksUtil.getAktifList(ortakIslemler.getSQLTanimListByTipKodu(Tanim.TIPI_PERSONEL_DINAMIK_TANIM, null, session));
			personelDinamikAlanMap = ortakIslemler.getPersonelDinamikAlanMap(donemPerList, personelDinamikAlanlar, session);
			TreeMap<Long, Tanim> tanimMap = new TreeMap<Long, Tanim>();
			personelDinamikAlanlar.clear();
			for (String key : personelDinamikAlanMap.keySet()) {
				PersonelDinamikAlan personelDinamikAlan = personelDinamikAlanMap.get(key);
				if (personelDinamikAlan.getId() != null && !tanimMap.containsKey(personelDinamikAlan.getAlan().getId()))
					tanimMap.put(personelDinamikAlan.getAlan().getId(), personelDinamikAlan.getAlan());
			}
			if (!tanimMap.isEmpty()) {
				personelDinamikAlanlar.addAll(PdksUtil.sortTanimList(null, new ArrayList(tanimMap.values())));
			}
			tanimMap = null;
			Calendar cal = Calendar.getInstance();
			// apSablon = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
			// apSablon.setArge(Boolean.TRUE);
			List<VardiyaHafta> vardiyaHaftaList = new ArrayList<VardiyaHafta>();
			fazlaMesaiOrtakIslemler.haftalikVardiyaOlustur(vardiyaHaftaList, apSablon, denklestirmeDonemi, tatilGunleriMap, null);
			setAylikPuantajDefault(apSablon);

			if (!personelDenklestirmeler.isEmpty()) {

				fazlaMesaiMap = ortakIslemler.getFazlaMesaiMap(session);
				Map<String, String> requestHeaderMap = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
				adres = requestHeaderMap.containsKey("host") ? requestHeaderMap.get("host") : "";

				map.clear();
				setInstance(denklestirmeDonemi);
				if (tatilGunleriMap == null || tatilGunleriMap.isEmpty() == false)
					tatilGunleriMap = ortakIslemler.getTatilGunleri(null, ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemi.getBaslangicTarih(), -1), ortakIslemler.tariheGunEkleCikar(cal, denklestirmeDonemi.getBitisTarih(), 1), session);
				List<PersonelDenklestirmeTasiyici> list = null;
				try {
					denklestirmeDonemi.setPersonelDenklestirmeDonemMap(personelDenklestirmeDonemMap);
					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					for (PersonelDenklestirme personelDenklestirme : personelDenklestirmeler) {
						PersonelDenklestirmeTasiyici denklestirmeTasiyici = new PersonelDenklestirmeTasiyici();
						denklestirmeTasiyici.setPersonelDenklestirme(personelDenklestirme);
						denklestirmeTasiyici.setPersonel(personelDenklestirme.getPersonel());
						denklestirmeTasiyici.setCalismaModeli(personelDenklestirme.getCalismaModeli());
						denklestirmeTasiyici.setDenklestirmeAy(denklestirmeAy);
						list.add(denklestirmeTasiyici);
					}
					aylikPuantajDefault.setArge(Boolean.TRUE);
					ortakIslemler.personelDenklestirmeDuzenle(list, aylikPuantajDefault, tatilGunleriMap, session);
				} catch (Exception ex) {
					list = new ArrayList<PersonelDenklestirmeTasiyici>();
					logger.equals(ex);
					ex.printStackTrace();
				}
				HashMap<Long, Long> sirketMap = new HashMap<Long, Long>();
				if (list.size() > 1) {
					list = PdksUtil.sortObjectStringAlanList(list, "getAdSoyad", null);
					if (seciliEkSaha3Id == null) {
						List<Tanim> bolumList = new ArrayList<Tanim>();
						HashMap<Long, List<PersonelDenklestirmeTasiyici>> map2 = new HashMap<Long, List<PersonelDenklestirmeTasiyici>>();
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							PersonelDenklestirmeTasiyici personelDenklestirmeTasiyici = (PersonelDenklestirmeTasiyici) iterator.next();
							Personel personel = personelDenklestirmeTasiyici.getPersonel();
							sirketMap.put(personel.getSirket().getId(), personel.getId());
							if (personel.getEkSaha3() == null)
								continue;
							Tanim tanim = personel.getEkSaha3();
							List<PersonelDenklestirmeTasiyici> list2 = map2.containsKey(tanim.getId()) ? map2.get(tanim.getId()) : new ArrayList<PersonelDenklestirmeTasiyici>();
							if (list2.isEmpty()) {
								bolumList.add(tanim);
								map2.put(tanim.getId(), list2);
							}
							list2.add(personelDenklestirmeTasiyici);
							iterator.remove();
						}
						if (bolumList.size() > 1)
							bolumList = PdksUtil.sortObjectStringAlanList(bolumList, "getAciklama", null);
						for (Tanim tanim : bolumList) {
							list.addAll(map2.get(tanim.getId()));
						}
						bolumList = null;
						map2 = null;
					}
				}

				TreeMap<String, String> izinGrupMap = ortakIslemler.getIzinGrupMap(session);
				resmiTatilVar = false;
				List<Long> cmIdList = new ArrayList<Long>();
				AylikPuantaj puantajToplam = apSablon;
				VardiyaPlan vardiyaPlanToplam = new VardiyaPlan();
				Personel personelSablon = new Personel();
				personelSablon.setAd("ARGE");
				personelSablon.setSoyad("Toplamı");
				puantajToplam.setPdksPersonel(personelSablon);
				puantajToplam.setVardiyaPlan(vardiyaPlanToplam);
				for (PersonelDenklestirmeTasiyici pdt : list) {
					Personel personel = pdt.getPersonel();
					if (!kimlikGoster)
						kimlikGoster = PdksUtil.hasStringValue(personel.getPersonelKGS().getKimlikNo());

					AylikPuantaj puantaj = new AylikPuantaj();
					puantaj.setPdksPersonel(personel);
					VardiyaPlan vardiyaPlan = new VardiyaPlan();
					puantaj.setVardiyaPlan(vardiyaPlan);
					List<VardiyaHafta> haftaList = new ArrayList<VardiyaHafta>();
					vardiyaPlan.setVardiyaHaftaList(haftaList);
					puantaj.setVardiyalar(new ArrayList<VardiyaGun>());
					puantaj.setCalisiyor(personel.isCalisiyorGun(apSablon.getSonGun()));
					puantaj.setVgMap(new TreeMap<String, VardiyaGun>());
					PersonelDenklestirme pd = pdt.getPersonelDenklestirme();
					puantaj.setPersonelDenklestirme(pd);
					CalismaModeli cm = puantaj.getCalismaModeli();
					if (cm != null && cmIdList.contains(cm.getId()) == false)
						cmIdList.add(cm.getId());
					Double toplamSaat = cm.getHaftaIci() * 5.0d + cm.getCumartesiSaat();
					// Double gunlukSaat = toplamSaat / 6.0d;
					TreeMap<String, VardiyaGun> vmMap = puantaj.getVgMap();

					for (VardiyaGun vg : pdt.getVardiyalar())
						vmMap.put(vg.getVardiyaDateStr(), vg);

					puantaj.setVardiyaHaftaList(haftaList);
					double toplamCalismaSaati = 0.0d;
					int toplamArgeGun = 0, toplamResmiTatil = 0, toplamHaftaTatil = 0, toplamArge = 0, toplamDgs = 0, argeGun = 0, calismaGun = 0, kalanDgs = 0;
					for (VardiyaHafta vh : apSablon.getVardiyaHaftaList()) {
						if (resmiTatilVar == false)
							resmiTatilVar = vh.isResmiTatilVar();
						VardiyaHafta vardiyaHafta = new VardiyaHafta();
						haftaList.add(vardiyaHafta);
						vardiyaHafta.setHafta(vh.getHafta());
						Double kusuratliSGKHaftalikToplamCalismaSaati = 0.0d, dgSaatToplam = 0.0d;
						int resmiTatil = 0;
						for (VardiyaGun vardiyaGun : vh.getVardiyaGunler()) {
							String key = vardiyaGun.getVardiyaDateStr();
							VardiyaGun vg = vmMap.get(vardiyaGun.getVardiyaDateStr());
							Double normalSaat = 0.0d, dgsSuresi = 0.0d;
							if (vg != null) {
								int dayOfWeek = PdksUtil.getDateField(vg.getVardiyaDate(), Calendar.DAY_OF_WEEK);

								puantaj.getVgMap().put(key, vg);
								if (vg.getId() != null) {
									VardiyaSaat vs = vg.getVardiyaSaat();
									if (tatilGunleriMap.containsKey(key) && vg.getIzin() == null) {
										++resmiTatil;
										normalSaat += cm.getSaat(dayOfWeek);
									} else if (vg.getIzin() != null) {
										double sure = cm.getIzinSaat(dayOfWeek);
										PersonelIzin personelIzin = vg.getIzin();
										IzinTipi izinTipi = personelIzin.getIzinTipi();
										if (izinTipi.isSenelikIzin()) {
											normalSaat += sure;
										} else {
											BordroDetayTipi izinBordroDetayTipi = izinGrupMap != null ? ortakIslemler.getBordroDetayTipi(izinTipi, izinGrupMap) : null;
											if (izinBordroDetayTipi != null) {
												if (izinBordroDetayTipi.equals(BordroDetayTipi.ARGE_IZIN) || izinBordroDetayTipi.equals(BordroDetayTipi.YILLIK_IZIN))
													normalSaat += sure;
											}
										}
									} else if (vs != null) {
										normalSaat = vs.getCalismaSuresi();
										dgsSuresi = vs.getDgsSuresi() != null ? vs.getDgsSuresi().doubleValue() : 0.0d;
									}
									puantaj.getVardiyalar().add(vg);

								}
							}

							vg.setDgsSuresi(dgsSuresi);
							vg.setCalismaSuresi(normalSaat);
							kusuratliSGKHaftalikToplamCalismaSaati += normalSaat + dgsSuresi;
							dgSaatToplam += dgsSuresi;
							vardiyaHafta.getVardiyaGunler().add(vg);
						}
						vardiyaHafta.setResmiTatilVar(vh.isResmiTatilVar());
						vardiyaHafta.setKusuratliSGKHaftalikToplamCalismaSaati(kusuratliSGKHaftalikToplamCalismaSaati);
						toplamCalismaSaati += kusuratliSGKHaftalikToplamCalismaSaati;
						Double kusuratliArgeGun = (kusuratliSGKHaftalikToplamCalismaSaati * 6.0d) / toplamSaat;
						vardiyaHafta.setKusuratliArgeGun(kusuratliArgeGun);
						int argeHaftaGun = kusuratliArgeGun.intValue();
						double kusurat = (kusuratliArgeGun - argeHaftaGun) * 100.0d;
						if (kusurat >= 75.0d)
							++argeHaftaGun;
						vardiyaHafta.setResmiTatil(resmiTatil);
						vardiyaHafta.setArgeGun(argeHaftaGun + resmiTatil);
						toplamArgeGun += vardiyaHafta.getArgeGun();
						toplamResmiTatil += resmiTatil;
						toplamHaftaTatil += vardiyaHafta.getHaftaTatil();
						toplamDgs += vardiyaHafta.getDgs();
					}
					vardiyaPlan.setToplamCalismaSaati(toplamCalismaSaati);
					vardiyaPlan.setToplamKusuratliArgeGun(toplamCalismaSaati / 6.0d);
					vardiyaPlan.setToplamArgeGun(toplamArgeGun);
					vardiyaPlan.setArgeGun(toplamArgeGun);
					vardiyaPlan.setToplamResmiTatil(toplamResmiTatil);
					vardiyaPlan.setToplamHaftaTatil(toplamHaftaTatil);
					vardiyaPlan.setToplamDgs(toplamDgs);
					puantaj.setPersonelDenklestirmeTasiyici(pdt);
					puantaj.setPdksPersonel(personel);
					puantaj.setVardiyalar(new ArrayList<VardiyaGun>(vmMap.values()));
					puantaj.setTrClass(renk ? VardiyaGun.STYLE_CLASS_ODD : VardiyaGun.STYLE_CLASS_EVEN);
					puantaj.setFazlaMesaiHesapla(pd.getDurum());

					renk = !renk;
					puantajList.add(puantaj);
				}

				modelGoster = cmIdList.size() > 0;
				cmIdList = null;
			}
			colSpan = 2;
			rowSpan = 2;
		} catch (Exception ex) {
			ex.printStackTrace();
			ortakIslemler.loggerErrorYaz(sayfaURL, ex);
			throw new Exception(ex);

		} finally {

		}
		denklestirmeDinamikAlanlar = ortakIslemler.setDenklestirmeDinamikDurum(puantajList, session);
		ortakIslemler.sortAylikPuantajList(puantajList, true);
		setAylikPuantajList(puantajList);

		yukseklik = (long) 200 + (puantajList.size() > 50 ? 50 : puantajList.size()) * 20;

		if (!vardiyaMap.isEmpty()) {
			if (tumVardiyaList != null)
				tumVardiyaList.clear();
			else
				tumVardiyaList = new ArrayList<VardiyaGun>();
			List<Vardiya> list = new ArrayList<Vardiya>(vardiyaMap.values());
			List<Vardiya> list2 = new ArrayList<Vardiya>();
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Vardiya vardiya = (Vardiya) iterator.next();
				if (vardiya.isCalisma() == false) {
					list2.add(vardiya);
					iterator.remove();
				}
			}
			if (!list.isEmpty())
				list = PdksUtil.sortObjectStringAlanList(list, "getKisaAdi", null);
			if (!list2.isEmpty())
				list.addAll(list2);
			Date bugun = PdksUtil.getDate(new Date());
			for (Vardiya vardiya : list) {
				VardiyaGun vg = new VardiyaGun(null, vardiya, bugun);
				vardiyaZamanIsle(vg.getIslemVardiya());
				tumVardiyaList.add(vg);
			}
			list = null;
			list2 = null;
		}
		vardiyaMap = null;
	}

	/**
	 * @param islemVardiya
	 * @throws Exception
	 */
	private void vardiyaZamanIsle(Vardiya islemVardiya) throws Exception {
		if (islemVardiya != null) {
			Double netSure = islemVardiya.getNetCalismaSuresi();
			String keyStr = String.valueOf(islemVardiya.getId());
			boolean calisma = islemVardiya != null && islemVardiya.isCalisma();
			if (calisma && !vardiyaZamanMap.containsKey(keyStr)) {
				vardiyaZamanMap.put(keyStr, islemVardiya.getAdi());
				String aciklama = authenticatedUser.timeFormatla(islemVardiya.getBasZaman()) + " : " + authenticatedUser.timeFormatla(islemVardiya.getBitZaman());
				vardiyaZamanMap.put("S" + keyStr, aciklama);
				vardiyaZamanMap.put("K" + keyStr, islemVardiya.getKisaAdi());
				vardiyaZamanMap.put("N" + keyStr, PdksUtil.numericValueFormatStr(netSure, null));
			}
		}
	}

	/**
	 * @param vardiya
	 * @param personel
	 * @return
	 */
	public Long getPersonelVardiyaAdet(Vardiya vardiya, Personel personel) {
		Long adet = null;
		if (personel != null && vardiya != null) {
			String key = personel.getId() + "_" + vardiya.getId();
			adet = vardiyaAdetMap.containsKey(key) ? vardiyaAdetMap.get(key) : null;
		}
		vardiyaAdet = adet;
		return adet;
	}

	/**
	 * @param vg
	 * @return
	 */
	public String vardiyaAciklama(VardiyaGun vg) {
		String str = "";
		String gosterimTipi = "K";
		Vardiya v = vg != null ? vg.getIslemVardiya() : null;
		if (v != null) {
			if (vg.getIzin() == null) {
				if (v.isCalisma()) {
					String key = gosterimTipi + v.getId();
					if (vardiyaZamanMap.containsKey(key))
						str = vardiyaZamanMap.get(key);
				} else
					str = v.getKisaAdi();
			} else {
				str = vg.getIzin().getIzinTipi().getKisaAciklama();
			}

		}
		return str;
	}

	/**
	 * @param vg
	 * @param yer
	 * @return
	 */
	public String vardiyaTitle(VardiyaGun vg, String yer) {
		String str = "";
		String gosterimTipi = "K";
		Vardiya v = vg != null ? vg.getIslemVardiya() : null;
		if (v != null) {
			String ek = "";
			String key = "";
			if (yer != null && (vg.getIzin() != null || v.isCalisma())) {
				if (yer.equalsIgnoreCase("P"))
					ek = "<BR></BR>";
				else if (yer.equalsIgnoreCase("E"))
					ek = "\n";
			}
			if (vg.getIzin() == null) {
				if (v.isCalisma()) {
					key = (gosterimTipi.equals("K") ? "S" : "K") + v.getId();
					if (vardiyaZamanMap.containsKey(key)) {
						str = vardiyaZamanMap.get(key);
						key = "N" + v.getId();
						if (vardiyaZamanMap.containsKey(key))
							str += " ---  Net Süre : " + vardiyaZamanMap.get(key);
					}
				}
			} else {
				PersonelIzin izin = vg.getIzin();
				IzinTipi izinTipi = izin.getIzinTipi();
				if (izinTipi != null)
					str = izinTipi.getIzinTipiTanim().getAciklama();
				if (!str.equals(""))
					str += ek;
				str += authenticatedUser.dateTimeFormatla(izin.getBaslangicZamani()) + " - " + authenticatedUser.dateTimeFormatla(izin.getBitisZamani());
				key = gosterimTipi + v.getId();
				if (vardiyaZamanMap.containsKey(key)) {
					str += ek + vardiyaZamanMap.get(key);
					key = "N" + v.getId();
					if (vardiyaZamanMap.containsKey(key)) {
						str += " ---  Net Süre : " + vardiyaZamanMap.get(key);
					}
				}
			}

		}
		return str;
	}

	/**
	 * @param idList
	 * @return
	 */
	private List<PersonelDenklestirme> getPdksPersonelDenklestirmeler(List<Long> idList) {
		String fieldName = "p";
		HashMap fields = new HashMap();
		StringBuilder sb = new StringBuilder();
		sb.append("select S.* from " + PersonelDenklestirme.TABLE_NAME + " S " + PdksEntityController.getSelectLOCK() + " ");
		// sb.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = S." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		// sb.append(" and P." + Personel.getIseGirisTarihiColumn() + " is not null and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " is not null ");
		sb.append(" where S." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = " + denklestirmeAy.getId() + " and S." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :" + fieldName);
		fields.put(fieldName, idList);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		// List<PersonelDenklestirme> list = pdksEntityController.getObjectBySQLList(sb, fields, PersonelDenklestirme.class);
		List<PersonelDenklestirme> list = pdksEntityController.getSQLParamList(idList, sb, fieldName, fields, PersonelDenklestirme.class, session);
		ortakIslemler.setPersonelDenklestirmeDevir(null, list, session);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			PersonelDenklestirme personelDenklestirme = (PersonelDenklestirme) iterator.next();
			if (!personelDenklestirme.isDenklestirmeDurum())
				iterator.remove();

		}
		fields = null;
		sb = null;
		return list;
	}

	/**
	 * @return
	 */
	public String puantajKartiPDF() {
		String sayfa = "";
		List<AylikPuantaj> list = new ArrayList<AylikPuantaj>();
		for (Iterator iter = aylikPuantajList.iterator(); iter.hasNext();) {
			AylikPuantaj ap = (AylikPuantaj) iter.next();
			PersonelDenklestirme pd = ap.getPersonelDenklestirme();
			if (pd.getDurum())
				if (ap.isSecili() || aylikPuantajList.size() == 1) {
					list.add(ap);

				}

		}
		if (list.isEmpty()) {
			PdksUtil.addMessageAvailableWarn("Seçili kayıt yoktur!!");
		} else {
			if (personelHareketMap == null)
				try {
					fillHareketList(aylikPuantajList);
				} catch (Exception e) {
					logger.error(e);
				}
			HashMap<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("puantajList", list);
			dataMap.put("denklestirmeAy", denklestirmeAy);
			dataMap.put("pdfBirlestirDurum", list.size() > 1 && pdfBirlestirDurum);
			sayfa = ortakIslemler.puantajKartiPDF(dataMap);

		}

		return sayfa;
	}

	public String fazlaMesaiExcel() {
		try {
			for (Iterator iter = aylikPuantajList.iterator(); iter.hasNext();) {
				AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
				aylikPuantaj.setSecili(Boolean.TRUE);
			}

			Workbook wb = fazlaMesaiExcelDevam(aylikPuantajList);
			if (wb != null) {
				String gorevYeriAciklama = (sirket != null ? "_" + (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama()) : "");
				gorevYeriAciklama += (tesisId != null ? "_" + ortakIslemler.getSelectItemText(tesisId, tesisList) : "") + (seciliEkSaha3Id != null ? "_" + ortakIslemler.getSelectItemText(seciliEkSaha3Id, gorevYeriList) : "");
				if (aylikVardiyaTabloHareketExcelDurum(false)) {
					HashMap<String, Object> map = aylikVardiyaTabloHareketExcelParametre(gorevYeriAciklama);
					map.put("wb", wb);
					ortakIslemler.aylikVardiyaTabloHareketExcelOlustur(map, aylikPuantajList);

				}
				ByteArrayOutputStream baosDosya = new ByteArrayOutputStream();
				wb.write(baosDosya);
				String dosyaAdi = "FazlaMesai" + gorevYeriAciklama + "_" + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	/**
	 * @param ayri
	 * @return
	 */
	public Boolean aylikVardiyaTabloHareketExcelDurum(boolean ayri) {
		boolean excelDurum = false;
		if (aylikVardiyaTabloHareketExcelParameter != null)
			excelDurum = ayri || authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || seciliEkSaha3Id != null || aylikPuantajList.size() < 160;
		return excelDurum;
	}

	/**
	 * @param ayri
	 * @return
	 */
	public Boolean aylikVardiyaTabloHareketPDFDurum() {
		boolean excelDurum = false;
		if (aylikVardiyaTabloHareketPDFParameter != null)
			excelDurum = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || seciliEkSaha3Id != null || aylikPuantajList.size() < 160;
		return excelDurum;
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	/**
	 * @param list
	 * @throws Exception
	 */
	private void fillHareketList(List<AylikPuantaj> list) throws Exception {
		personelHareketMap = new HashMap<Long, List<HareketKGS>>();
		HashMap<Long, AylikPuantaj> apMap = new HashMap<Long, AylikPuantaj>();
		List<Long> perIdList = new ArrayList<Long>();
		for (AylikPuantaj ap : list) {
			Personel personel = ap.getPdksPersonel();
			apMap.put(personel.getPersonelKGS().getId(), ap);
			perIdList.add(personel.getId());
		}
		HashMap<Long, List<PersonelFazlaMesai>> fmMap = new HashMap<Long, List<PersonelFazlaMesai>>();
		Date tarih1 = PdksUtil.tariheGunEkleCikar(aylikPuantajDefault.getIlkGun(), -1);
		Date tarih2 = PdksUtil.tariheGunEkleCikar(aylikPuantajDefault.getSonGun(), 1);
		List<Long> kapiIdler = ortakIslemler.getPdksDonemselKapiIdler(tarih1, tarih2, session);
		List<HareketKGS> kgsList = ortakIslemler.getHareketBilgileri(kapiIdler, new ArrayList<Long>(apMap.keySet()), tarih1, tarih2, HareketKGS.class, session);
		if (kgsList.isEmpty() == false) {
			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			sb.append("select I.* from " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK() + " ");
			String fieldName = "p";
			parametreMap.put(fieldName, perIdList);
			sb.append(" inner join " + PersonelFazlaMesai.TABLE_NAME + " I " + PdksEntityController.getJoinLOCK() + " on V." + VardiyaGun.COLUMN_NAME_ID + " = I." + PersonelFazlaMesai.COLUMN_NAME_VARDIYA_GUN);
			sb.append(" and I." + PersonelFazlaMesai.COLUMN_NAME_DURUM + " = 1 ");
			sb.append(" left join " + VardiyaSaat.TABLE_NAME + " S " + PdksEntityController.getJoinLOCK() + " on S." + VardiyaSaat.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_VARDIYA_SAAT);
			sb.append(" left join " + VardiyaEkSaat.TABLE_NAME + " ES " + PdksEntityController.getJoinLOCK() + " on ES." + VardiyaEkSaat.COLUMN_NAME_ID + " =  S." + VardiyaSaat.COLUMN_NAME_VARDIYA_EK_SAAT);
			sb.append(" where V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " > :v1 and V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " < :v2 ");
			sb.append(" and V." + VardiyaGun.COLUMN_NAME_PERSONEL + " :" + fieldName);
			parametreMap.put("v1", tarih1);
			parametreMap.put("v2", tarih2);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelFazlaMesai> fazlaMesailer = pdksEntityController.getSQLParamList(perIdList, sb, fieldName, parametreMap, PersonelFazlaMesai.class, session);
			for (PersonelFazlaMesai pfm : fazlaMesailer) {
				if (pfm.isOnaylandi()) {
					Long key = pfm.getVardiyaGun().getId();
					List<PersonelFazlaMesai> list2 = fmMap.containsKey(key) ? fmMap.get(key) : new ArrayList<PersonelFazlaMesai>();
					if (list2.isEmpty())
						fmMap.put(key, list2);
					list2.add(pfm);
				}

			}
			fazlaMesailer = null;
		}
		perIdList = null;
		for (HareketKGS hareket : kgsList) {
			Long key = hareket.getPersonelKGS().getId();
			List<HareketKGS> list2 = personelHareketMap.containsKey(key) ? personelHareketMap.get(key) : new ArrayList<HareketKGS>();
			if (list2.isEmpty())
				personelHareketMap.put(key, list2);
			list2.add(hareket);
		}
		kgsList = null;
		for (Long key : personelHareketMap.keySet()) {
			AylikPuantaj ap = apMap.get(key);
			List<HareketKGS> list2 = personelHareketMap.get(key);
			if (list2.size() > 1)
				list2 = PdksUtil.sortListByAlanAdi(list2, "zaman", Boolean.FALSE);
			List<VardiyaGun> vardiyaList = ap.getAyinVardiyalari();
			for (VardiyaGun vg : vardiyaList) {
				vg.setHareketler(null);
				vg.setCikisHareketleri(null);
				vg.setGirisHareketleri(null);
				if (vg.isAyinGunu() && vg.getVardiya() != null) {
					vg.setFazlaMesailer(fmMap.containsKey(vg.getId()) ? fmMap.get(vg.getId()) : null);
					for (Iterator iterator = list2.iterator(); iterator.hasNext();) {
						HareketKGS hareket = (HareketKGS) iterator.next();
						if (vg.addHareket(hareket, Boolean.TRUE)) {
							iterator.remove();
						}
					}
				}

			}
			list2 = null;
		}
		apMap = null;
		personelHareketMap.clear();

	}

	public String aylikVardiyaTabloHareketExcel() {

		try {
			List<AylikPuantaj> list = new ArrayList<AylikPuantaj>(aylikPuantajList);

			if (!list.isEmpty()) {
				if (personelHareketMap == null)
					try {
						fillHareketList(list);
					} catch (Exception e) {
					}

				ByteArrayOutputStream baosDosya = aylikVardiyaTabloHareketExcelDevam(list);
				if (baosDosya != null) {
					String dosyaAdi = "FazlaMesai" + (sirket != null ? "_" + (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama()) : "");
					dosyaAdi += (tesisId != null ? "_" + ortakIslemler.getSelectItemText(tesisId, tesisList) : "") + (seciliEkSaha3Id != null ? "_" + ortakIslemler.getSelectItemText(seciliEkSaha3Id, gorevYeriList) : "");
					dosyaAdi += "_" + PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyyMM") + ".xlsx";
					PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
				}
			}
			list = null;
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";

	}

	/**
	 * @param puantajList
	 * @return
	 */
	private ByteArrayOutputStream aylikVardiyaTabloHareketExcelDevam(List<AylikPuantaj> puantajList) {
		ByteArrayOutputStream baos = null;
		String dosyaAdi = (sirket != null ? "_" + (sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama()) : "");
		dosyaAdi += (tesisId != null ? "_" + ortakIslemler.getSelectItemText(tesisId, tesisList) : "") + (seciliEkSaha3Id != null ? "_" + ortakIslemler.getSelectItemText(seciliEkSaha3Id, gorevYeriList) : "");
		try {

			if (authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi()) {
				if (aylikVardiyaTabloHareketExcelParameter != null) {
					HashMap<String, Object> harekaketMap = aylikVardiyaTabloHareketExcelParametre(dosyaAdi);

					Workbook wb = ortakIslemler.aylikVardiyaTabloHareketExcelOlustur(harekaketMap, puantajList);
					baos = new ByteArrayOutputStream();
					wb.write(baos);
				}
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			baos = null;
		}

		return baos;

	}

	/**
	 * @param dosyaAdi
	 * @return
	 */
	private HashMap<String, Object> aylikVardiyaTabloHareketExcelParametre(String dosyaAdi) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("aylikPuantajDefault", aylikPuantajDefault);
		map.put("bolumAciklama", bolumAciklama);
		map.put("seciliEkSaha3Id", seciliEkSaha3Id);
		map.put("gorevYeriAciklama", dosyaAdi);
		map.put("yasalFazlaCalismaAsanSaat", yasalFazlaCalismaAsanSaat);
		map.put("icapciSaatGoster", icapciSaatGoster);
		map.put("gerceklesenMesaiKod", gerceklesenMesaiKod);
		map.put("devredenMesaiKod", devredenMesaiKod);
		map.put("resmiTatilVar", resmiTatilVar);
		map.put("haftaTatilVar", haftaTatilVar);
		map.put("devredenBakiyeKod", devredenBakiyeKod);
		map.put("aksamGun", aksamGun);
		map.put("aksamSaat", aksamSaat);
		map.put("resmiTatilKanunenEklenenSureGoster", resmiTatilKanunenEklenenSureGoster);
		return map;
	}

	/**
	 * @param list
	 * @return
	 */
	private Workbook fazlaMesaiExcelDevam(List<AylikPuantaj> list) {
		Workbook wb = new XSSFWorkbook();
		TreeMap<String, String> sirketMap = new TreeMap<String, String>();
		sirket = null;
		tesis = null;
		boolean kismiOdemeGoster = false;
 		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			PersonelDenklestirme pd = aylikPuantaj.getPersonelDenklestirme();
			if (!kismiOdemeGoster)
				kismiOdemeGoster = pd.getKismiOdemeSure() != null && pd.getKismiOdemeSure().doubleValue() != 0.0d;
			Personel personel = aylikPuantaj.getPdksPersonel();
			if (personel.getSirket() != null) {
				sirket = personel.getSirket();
				if (sirket.isTesisDurumu() && tesis == null && tesisId != null && personel.getTesis() != null)
					tesis = personel.getTesis();
			}

			String tekSirketTesis = (personel.getSirket() != null ? personel.getSirket().getId() : "") + "_" + (personel.getTesis() != null ? personel.getTesis().getId() : "");
			String tekSirketTesisAdi = (personel.getSirket() != null ? personel.getSirket().getAd() : "") + " " + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			sirketMap.put(tekSirketTesis, tekSirketTesisAdi);
		}
		String boslukSifirStr = ortakIslemler.getParameterKey("boslukSifir");
		Double boslukSifirDouble = null;
		if (PdksUtil.hasStringValue(boslukSifirStr))
			try {
				boslukSifirDouble = Double.parseDouble(boslukSifirStr);
			} catch (Exception e) {
				boslukSifirDouble = null;
			}

		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "MMMMM yyyy") + " Arge Puantaj", Boolean.TRUE);
		CreationHelper helper = wb.getCreationHelper();
		ClientAnchor anchor = helper.createClientAnchor();
		CellStyle izinBaslik = ExcelUtil.getStyleHeader(wb);
		CellStyle styleTutarEven = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedTutarEven = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		ExcelUtil.setFontColor(styleRedTutarEven, Color.RED);
		CellStyle styleTutarOdd = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		CellStyle styleRedTutarOdd = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		ExcelUtil.setFontColor(styleRedTutarOdd, Color.RED);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleRedOdd = ExcelUtil.getStyleOdd(null, wb);
		ExcelUtil.setFontColor(styleRedOdd, Color.RED);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleRedEven = ExcelUtil.getStyleEven(null, wb);
		ExcelUtil.setFontColor(styleRedEven, Color.RED);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleRedOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleRedOddCenter, Color.RED);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleRedEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleRedEvenCenter, Color.RED);
		CellStyle styleTutarOddRed = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		ExcelUtil.setFontColor(styleTutarOddRed, Color.RED);
		CellStyle styleTutarEvenRed = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		ExcelUtil.setFontColor(styleTutarEvenRed, Color.RED);
		CellStyle styleCenterEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleRedCenterEvenDay = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleRedCenterEvenDay, Color.RED);
		CellStyle styleCenterOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleRedCenterOddDay = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleRedCenterOddDay, Color.RED);

		CellStyle styleDay = null, styleGenel = null, styleTutar = null, styleStrDay = null;
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		CellStyle styleTatil = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleTatilRed = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleTatilRed, Color.RED);
		CellStyle styleCenterOddDayRed = ExcelUtil.getStyleDayOdd(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleCenterOddDayRed, Color.RED);
		CellStyle styleCenterEvenDayRed = ExcelUtil.getStyleDayEven(ExcelUtil.ALIGN_CENTER, wb);
		ExcelUtil.setFontColor(styleCenterEvenDayRed, Color.RED);
		CellStyle styleIstek = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleEgitim = ExcelUtil.getStyleDataCenter(wb);
		CellStyle styleOff = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleOff, Color.WHITE);
		CellStyle styleOffRed = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFontColor(styleTatilRed, Color.RED);
		ExcelUtil.setFillForegroundColor(izinBaslik, 146, 208, 80);
		CellStyle styleIcap = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFillForegroundColor(styleIcap, 254, 235, 41);
		CellStyle styleIzin = ExcelUtil.getStyleDataCenter(wb);
		ExcelUtil.setFillForegroundColor(styleIzin, 146, 208, 80);

		CellStyle styleCalisma = ExcelUtil.getStyleDataCenter(wb);
		XSSFCellStyle headerYatay = (XSSFCellStyle) ExcelUtil.getStyleHeader(9, wb);
		XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(9, wb);
		header.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		header.setRotation((short) 90);
		ExcelUtil.setFillForegroundColor(styleTatil, 255, 153, 204);

		ExcelUtil.setFillForegroundColor(styleIstek, 255, 255, 0);

		ExcelUtil.setFillForegroundColor(styleCalisma, 255, 255, 255);

		ExcelUtil.setFillForegroundColor(styleEgitim, 0, 0, 255);

		ExcelUtil.setFillForegroundColor(styleOff, 13, 12, 89);
		ExcelUtil.setFontColor(styleOff, 256, 256, 256);
		int row = 2, col = 0;

		ExcelUtil.getCell(sheet, ++row, col, styleGenel).setCellValue("");
		col = 0;
		ExcelUtil.getCell(sheet, ++row, col++, header).setCellValue("Sıra");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		if (kimlikGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.kimlikNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		if (tesisId == null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		if (seciliEkSaha3Id == null)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (sirketGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Görevi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşe Giriş Tarihi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İşten Ayrılma Tarihi");
		if (modelGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.calismaModeliAciklama());

		Calendar cal = Calendar.getInstance();
		cal.setTime(aylikPuantajDefault.getIlkGun());

		CellStyle headerHaftaGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFontColor(headerHaftaGun, 255, 0, 0);
		ExcelUtil.setFillForegroundColor(headerHaftaGun, 255, 255, 0);
		headerHaftaGun.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);

		CellStyle headerToplamGun1 = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFontColor(headerToplamGun1, 0, 0, 0);
		ExcelUtil.setFillForegroundColor(headerToplamGun1, 175, 134, 134);
		headerToplamGun1.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);

		CellStyle headerToplamGun2 = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFontColor(headerToplamGun2, 255, 0, 0);
		ExcelUtil.setFillForegroundColor(headerToplamGun2, 175, 134, 134);
		headerToplamGun2.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);

		CellStyle headerVardiyaGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaGun, 99, 182, 153);
		headerVardiyaGun.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

		CellStyle headerVardiyaTatilYarimGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFontColor(headerVardiyaTatilYarimGun, 255, 255, 0);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilYarimGun, 144, 185, 63);
		headerVardiyaTatilYarimGun.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

		CellStyle headerVardiyaTatilGun = ExcelUtil.getStyleHeader(9, wb);
		ExcelUtil.setFillForegroundColor(headerVardiyaTatilGun, 92, 127, 45);
		ExcelUtil.setFontColor(headerVardiyaTatilGun, 255, 255, 0);
		headerVardiyaTatilGun.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		Drawing drawing = sheet.createDrawingPatriarch();

		for (int i = 0; i < col; i++) {
			Cell cell = sheet.getRow(row).getCell(i);
			ExcelUtil.getCell(sheet, row + 1, i, cell.getCellStyle()).setCellValue("");
			try {
				sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) i, (int) row + 1, (int) i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (VardiyaHafta hafta : aylikPuantajDefault.getVardiyaHaftaList()) {
			List<VardiyaGun> gunler = hafta.getVardiyaGunler();
			for (Iterator iterator = gunler.iterator(); iterator.hasNext();) {
				VardiyaGun vg = (VardiyaGun) iterator.next();
				cal.setTime(vg.getVardiyaDate());
				CellStyle headerVardiya = headerVardiyaGun;
				String title = null;
				if (vg.getTatil() != null) {
					Tatil tatil = vg.getTatil();
					title = tatil.getAd();
					headerVardiya = tatil.isYarimGunMu() ? headerVardiyaTatilYarimGun : headerVardiyaTatilGun;
				}
				Cell cell = ExcelUtil.getCell(sheet, row, col++, headerVardiya);
				ExcelUtil.baslikCell(cell, anchor, helper, drawing, cal.get(Calendar.DAY_OF_MONTH) + "\n " + authenticatedUser.getTarihFormatla(cal.getTime(), "EEE"), title);

				ExcelUtil.getCell(sheet, row + 1, col - 1, headerVardiya).setCellValue("N");
				ExcelUtil.getCell(sheet, row + 1, col, headerVardiya).setCellValue("DS");
				cell = ExcelUtil.getCell(sheet, row, col++, headerVardiya);
				cell.setCellValue("");
				try {
					sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) col - 2, (int) row, (int) col - 1));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			int haftaBas = col;
			ExcelUtil.getCell(sheet, row, col++, headerHaftaGun).setCellValue("SGK Toplam Saat");
			ExcelUtil.getCell(sheet, row, col++, headerHaftaGun).setCellValue("Kusüratlı Arge Gün");
			ExcelUtil.getCell(sheet, row, col++, headerHaftaGun).setCellValue("Arge Gün");
			if (hafta.isResmiTatilVar())
				ExcelUtil.getCell(sheet, row, col++, headerHaftaGun).setCellValue("Resmi Tatil");
			ExcelUtil.getCell(sheet, row, col++, headerHaftaGun).setCellValue("Hafta Tatil");
			ExcelUtil.getCell(sheet, row, col++, headerHaftaGun).setCellValue("Arge Toplamı");
			ExcelUtil.getCell(sheet, row, col++, headerHaftaGun).setCellValue("Dgs");
			for (int i = haftaBas; i < col; i++) {

				Cell cell = sheet.getRow(row).getCell(i);
				ExcelUtil.getCell(sheet, row + 1, i, cell.getCellStyle()).setCellValue("");
				try {
					sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) i, (int) row + 1, (int) i));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		int haftaBas = col;
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun1).setCellValue("Toplam Çalışma Saati");
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun1).setCellValue("Toplam Küsüratli Arge Gün");
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun1).setCellValue("Toplam Arge Gün");
		if (resmiTatilVar)
			ExcelUtil.getCell(sheet, row, col++, headerToplamGun1).setCellValue("Toplam Resmi Tatil");
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun1).setCellValue("Toplam Hafta Tatil");
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun1).setCellValue("Toplam Arge");
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun1).setCellValue("Toplam Dgs");
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun2).setCellValue("Arge Gün");
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun2).setCellValue("Çalışma Gün");
		ExcelUtil.getCell(sheet, row, col++, headerToplamGun2).setCellValue("Kalan Dgs");
		for (int i = haftaBas; i < col; i++) {
			sheet.setColumnWidth(i, 5 * 256);
			Cell cell = sheet.getRow(row).getCell(i);
			ExcelUtil.getCell(sheet, row + 1, i, cell.getCellStyle()).setCellValue("");
			try {
				sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) i, (int) row + 1, (int) i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Row basrow = sheet.getRow(row);

		// float finalHeight = Math.max(dynamicHeight, sheet.getDefaultRowHeightInPoints());
		// row.setHeightInPoints(finalHeight);

		row++;
		int sira = 0;
		double maxSure = denklestirmeAy.getFazlaMesaiMaxSure() != null ? denklestirmeAy.getFazlaMesaiMaxSure() : 11.0d;
		Date bugun = PdksUtil.getDate(new Date());
		TreeMap<String, String> izinGrupMap = ortakIslemler.getIzinGrupMap(session);
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AylikPuantaj aylikPuantaj = (AylikPuantaj) iter.next();
			Personel personel = aylikPuantaj.getPdksPersonel();
			if (personel == null || PdksUtil.hasStringValue(personel.getSicilNo()) == false)
				continue;

			boolean hataVar = aylikPuantaj.isFazlaMesaiHesapla() == false;

			row++;
			col = 0;
			CellStyle styleHataCenter = null;
			try {
				boolean help = helpPersonel(aylikPuantaj.getPdksPersonel());
				try {
					if (row % 2 != 0) {
						styleCenter = styleOddCenter;
						styleStrDay = styleCenterOddDay;
						styleGenel = styleOdd;
						styleTutar = styleTutarOdd;
						if (hataVar) {
							styleCenter = styleRedOddCenter;
							styleGenel = styleRedOdd;
							styleTutar = styleRedTutarOdd;
							styleHataCenter = styleRedCenterOddDay;
						}

					} else {
						styleCenter = styleEvenCenter;
						styleStrDay = styleCenterEvenDay;
						styleGenel = styleEven;
						styleTutar = styleTutarEven;
						if (hataVar) {
							styleCenter = styleRedEvenCenter;
							styleGenel = styleRedEven;
							styleTutar = styleRedTutarEven;
							styleHataCenter = styleRedCenterEvenDay;
						}

					}

					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(++sira);
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
					if (kimlikGoster) {
						if (PdksUtil.hasStringValue(personel.getPersonelKGS().getKimlikNo()))
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPersonelKGS().getKimlikNo());
						else
							ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue("");
					}
					Cell personelCell = ExcelUtil.getCell(sheet, row, col++, styleGenel);
					personelCell.setCellValue(personel.getAdSoyad());
					if (tesisId == null)
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
					if (seciliEkSaha3Id == null)
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(aylikPuantaj.getYonetici() != null && aylikPuantaj.getYonetici().getId() != null ? aylikPuantaj.getYonetici().getAdSoyad() : "");
					if (sirketGoster)
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getSirket() != null ? personel.getSirket().getAd() : "");
					ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(personel.getGorevTipi() != null ? personel.getGorevTipi().getAciklama() : "");

					if (personel.getIseBaslamaTarihi() != null)
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(authenticatedUser.dateFormatla(personel.getIseBaslamaTarihi()));
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");

					if (personel.isCalisiyor()) {
						if (boslukSifirDouble == null)
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(boslukSifirStr);
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(boslukSifirDouble.intValue());
					} else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(authenticatedUser.dateFormatla(personel.getSonCalismaTarihi()));

					if (modelGoster) {
						String modelAciklama = "";
						if (aylikPuantaj.getPersonelDenklestirme() != null && aylikPuantaj.getPersonelDenklestirme().getCalismaModeliAy() != null) {
							CalismaModeliAy calismaModeliAy = aylikPuantaj.getPersonelDenklestirme().getCalismaModeliAy();
							if (calismaModeliAy.getCalismaModeli() != null)
								modelAciklama = calismaModeliAy.getCalismaModeli().getOzelAciklama();
						}
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue(modelAciklama);
					}

					for (VardiyaHafta hafta : aylikPuantaj.getVardiyaHaftaList()) {
						List vardiyaList = hafta.getVardiyaGunler();
						for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
							VardiyaGun vg = (VardiyaGun) iterator.next();
							VardiyaGun vardiyaGun = aylikPuantaj.getVardiya(vg);
							Vardiya vardiya = vardiyaGun.getVardiya();
							String styleText = vardiyaGun.getAylikClassAdi(aylikPuantaj.getTrClass());
							Double sure = vardiyaGun.getCalismaSuresi();
							boolean maxSureGecti = vardiyaGun.isFcsDahil() && sure != null && sure.doubleValue() > maxSure;
							styleDay = styleStrDay;
							if (maxSureGecti)
								styleDay = row % 2 != 0 ? styleCenterOddDayRed : styleCenterEvenDayRed;
							if (styleText.equals(VardiyaGun.STYLE_CLASS_HAFTA_TATIL)) {
								styleDay = maxSureGecti == false ? styleTatil : styleTatilRed;

							} else if (styleText.equals(VardiyaGun.STYLE_CLASS_IZIN)) {
								styleDay = styleIzin;

							} else if (styleText.equals(VardiyaGun.STYLE_CLASS_OZEL_ISTEK)) {
								styleDay = styleIstek;

							} else if (styleText.equals(VardiyaGun.STYLE_CLASS_EGITIM)) {
								styleDay = styleEgitim;

							} else if (styleText.equals(VardiyaGun.STYLE_CLASS_OFF)) {
								styleDay = maxSureGecti == false ? styleOff : styleOffRed;

							} else if (styleText.equals(VardiyaGun.STYLE_CLASS_ICAP))
								styleDay = styleIcap;

							boolean onayHata = false;
							String aciklama = "X";
							if (vardiya != null && vardiyaGun.getDurum() == false && vardiyaGun.getVardiyaDate().before(bugun)) {
								styleDay = styleHataCenter;
								onayHata = true;
							}

							Cell cell = ExcelUtil.getCell(sheet, row, col++, styleDay);
							if (!onayHata) {
								if (vardiyaGun.getVardiyaDateStr().endsWith("30"))
									logger.debug("");
								aciklama = !help || calisan(vardiyaGun) ? vardiyaGun.getFazlaMesaiOzelAciklama(Boolean.TRUE, authenticatedUser.sayiFormatliGoster(sure)) : "";
								if (vardiyaGun.getIzin() != null) {
									IzinTipi izinTipi = vardiyaGun.getIzin().getIzinTipi();
									BordroDetayTipi izinBordroDetayTipi = ortakIslemler.getBordroDetayTipi(izinTipi, izinGrupMap);
									if (izinBordroDetayTipi != null && izinBordroDetayTipi.equals(BordroDetayTipi.UCRETLI_IZIN) == false)
										aciklama = izinTipi.getKisaAciklama();
								}
								if (vardiya != null) {
									if (aciklama.equals("0")) {
										if (vardiya.isCalisma() == false || vardiyaGun.getTatil() != null || vardiyaGun.isIzinli())
											aciklama = ".";

									}
								} else {
									aciklama = vardiyaGun.isCalismayiBirakti() ? "İSTİFA" : "";
								}

							}
							String title = vardiya != null ? vardiyaGun.fazlaMesaiTitle(authenticatedUser) : null;

							ExcelUtil.baslikCell(cell, anchor, helper, drawing, aciklama, title);

							cell = ExcelUtil.getCell(sheet, row, col++, styleDay);
							if (vardiyaGun.getDgsSuresi() > 0.0d)
								cell.setCellValue(vardiyaGun.getDgsSuresi());
							else
								cell.setCellValue("");

						}

						if (hafta.getKusuratliSGKHaftalikToplamCalismaSaati() > 0.0d)
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(hafta.getKusuratliSGKHaftalikToplamCalismaSaati());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						if (hafta.getKusuratliArgeGun() > 0.0d)
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(hafta.getKusuratliArgeGun());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						if (hafta.getArgeGun() > 0.0d)
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(hafta.getArgeGun());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						if (hafta.isResmiTatilVar()) {
							if (hafta.getResmiTatil() > 0.0d)
								ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(hafta.getResmiTatil());
							else
								ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						}
						if (hafta.getHaftaTatil() > 0.0d)
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(hafta.getHaftaTatil());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						if (hafta.getHaftalikArgeToplami() > 0.0d)
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(hafta.getHaftalikArgeToplami());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
						if (hafta.getDgs() > 0.0d)
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(hafta.getDgs());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					}
					VardiyaPlan vp = aylikPuantaj.getVardiyaPlan();
					if (vp.getToplamCalismaSaati() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getToplamCalismaSaati());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (vp.getToplamKusuratliArgeGun() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getToplamKusuratliArgeGun());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (vp.getToplamArgeGun() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getToplamArgeGun());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (resmiTatilVar) {
						if (vp.getToplamResmiTatil() > 0.0d)
							ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getToplamResmiTatil());
						else
							ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					}
					if (vp.getToplamHaftaTatil() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getToplamHaftaTatil());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (vp.getToplamArge() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getToplamArge());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (vp.getToplamDgs() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getToplamDgs());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (vp.getArgeGun() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getArgeGun());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (vp.getCalismaGun() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getCalismaGun());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					if (vp.getKalanDgs() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, styleTutar).setCellValue(vp.getKalanDgs());
					else
						ExcelUtil.getCell(sheet, row, col++, styleGenel).setCellValue("");
					styleGenel = null;
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());
					logger.error(row);

				}
			} catch (Exception ex) {
				logger.error(ex);
				ex.printStackTrace();
			}

		}

		try {
			row = 0;
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			int dynamicHeight = 0;
			for (int i = 0; i < col; i++) {
				Cell cell = basrow.getCell(i);
				try {
					String str = cell.getStringCellValue();
					if (str != null && str.length() > dynamicHeight)
						dynamicHeight = str.length();
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
			basrow.setHeightInPoints(dynamicHeight * 6);
			headerHaftaGun.setRotation((short) 90);
			headerToplamGun1.setRotation((short) 90);
			headerToplamGun2.setRotation((short) 90);

			col = 0;

			if (sirket != null) {
				String baslik = sirket.getSirketGrup() == null ? sirket.getAd() : sirket.getSirketGrup().getAciklama();
				baslik += (tesisId != null ? " " + ortakIslemler.getSelectItemText(tesisId, tesisList) : "") + (seciliEkSaha3Id != null ? " " + ortakIslemler.getSelectItemText(seciliEkSaha3Id, gorevYeriList) : "");

				ExcelUtil.getCell(sheet, row, col, headerYatay).setCellValue(baslik);
				for (int i = 0; i < 3; i++)
					ExcelUtil.getCell(sheet, row, col + i + 1, headerYatay).setCellValue("");

				try {
					sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) 0, (int) row, (int) 4));
				} catch (Exception e) {
					e.printStackTrace();
				}
				col = 0;
				++row;
			}
			String aciklamaExcel = PdksUtil.replaceAll(PdksUtil.convertToDateString(aylikPuantajDefault.getIlkGun(), "yyyy MMMMMM  "), "_", "");
			ExcelUtil.getCell(sheet, row, col, headerYatay).setCellValue(aciklamaExcel);
			for (int i = 0; i < 3; i++)
				ExcelUtil.getCell(sheet, row, col + i + 1, headerYatay).setCellValue("");

			try {
				sheet.addMergedRegion(ExcelUtil.getRegion((int) row, (int) 0, (int) row, (int) 4));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return wb;

	}

	/**
	 * @param per
	 * @param vardiya
	 * @return
	 */
	public Integer getVardiyaAdet(Personel per, Vardiya vardiya) {
		Integer adet = fazlaMesaiOrtakIslemler.getVardiyaAdet(izinTipiPersonelVardiyaMap, per, vardiya);
		return adet;
	}

	/**
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @param deger
	 * @return
	 */
	public Cell setCell(Sheet sheet, int rowNo, int columnNo, CellStyle style, Double deger) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);

		try {
			if (deger != 0.0d) {
				cell.setCellValue(authenticatedUser.sayiFormatliGoster(deger));
			}

		} catch (Exception e) {
		}
		return cell;
	}

	/**
	 * @param sheet
	 * @param rowNo
	 * @param columnNo
	 * @param style
	 * @param deger
	 * @return
	 */
	public Cell setCellDate(Sheet sheet, int rowNo, int columnNo, CellStyle style, Date date) {
		Cell cell = ExcelUtil.getCell(sheet, rowNo, columnNo, style);

		try {
			if (date != null) {
				cell.setCellValue(date);
			} else
				cell.setCellValue("");

		} catch (Exception e) {
		}
		return cell;
	}

	private boolean calisan(VardiyaGun vardiyaGun) {
		boolean calisan = vardiyaGun != null;
		if (calisan) {
			if (vardiyaGun.getVardiya() != null) {

				calisan = vardiyaGun.isKullaniciYetkili() || (vardiyaGun.getIzin() != null && !helpPersonel(vardiyaGun.getPersonel()));
			}
		}
		return calisan;
	}

	private boolean helpPersonel(Personel personel) {
		return false;

	}

	public void tesisDoldur(boolean bolumDoldurDurum) throws Exception {
		sirket = null;
		bolumleriTemizle();
		tesisDurum = false;
		if (pdksSirketList == null || pdksSirketList.isEmpty())
			setTesisList(new ArrayList<SelectItem>());
		else {
			if (sirketId != null) {
				sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);
				if (!sirket.isTesisDurumu())
					tesisId = null;
			}
			List<SelectItem> list = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, denklestirmeAy != null ? getFazlaMesaiAylikPuantaj() : null, true, session);
			if (list.size() > 1 && denklestirmeAyDurum) {
				List<Personel> personeList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, null, null, null, denklestirmeAy != null ? getFazlaMesaiAylikPuantaj() : null, true, session);
				tesisDurum = personeList == null || personeList.size() > 200;
				if (tesisDurum == false)
					tesisId = null;
				personeList = null;
			}
			setTesisList(list);
			Long onceki = tesisId;
			if (list != null && !list.isEmpty()) {
				if (list.size() == 1 || onceki == null) {
					if (tesisDurum)
						tesisId = (Long) list.get(0).getValue();

				} else if (onceki != null) {
					tesisId = null;
					for (SelectItem st : list) {
						if (st.getValue().equals(onceki))
							tesisId = onceki;
					}
				}
			}
			if (!bolumDoldurDurum)
				if (sirket != null && sirket.isTesisDurumu() == false)
					bolumDoldurDurum = true;
			onceki = tesisId;

			if (tesisId != null || (bolumDoldurDurum)) {
				bolumDoldur();
				setTesisId(onceki);

			}
			if (denklestirmeAyDurum == false)
				hataliPuantajGoster = Boolean.FALSE;
		}
		aylikPuantajList.clear();
	}

	public String bolumDoldur() {
		fazlaMesaiVardiyaGun = null;
		linkAdres = null;
		stajerSirket = Boolean.FALSE;
		bolumleriTemizle();
		if (pdksSirketList == null || pdksSirketList.isEmpty())
			setGorevYeriList(new ArrayList<SelectItem>());
		else {

			if (personelDenklestirmeList != null)
				personelDenklestirmeList.clear();

			denklestirmeAy = ortakIslemler.getSQLDenklestirmeAy(yil, ay, session);
			if (authenticatedUser.getSuperVisorHemsirePersonelNoList() != null) {
				if (hastaneSuperVisor == null) {
					String calistigiSayfa = authenticatedUser.getCalistigiSayfa();
					String superVisorHemsireSayfalari = ortakIslemler.getParameterKey("superVisorHemsireSayfalari");
					List<String> sayfalar = PdksUtil.hasStringValue(superVisorHemsireSayfalari) ? PdksUtil.getListByString(superVisorHemsireSayfalari, null) : null;
					hastaneSuperVisor = sayfalar != null && sayfalar.contains(calistigiSayfa);
				}

			} else
				hastaneSuperVisor = Boolean.FALSE;
			Sirket sirket = null;
			if (sirketId != null) {
				sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);
			}
			setSirket(sirket);

			if (sirket != null) {
				setDepartman(sirket.getDepartman());
				if (departman.isAdminMi() && sirket.isTesisDurumu()) {
					gorevYeriList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, tesisId != null ? String.valueOf(tesisId) : null, denklestirmeAy != null ? getFazlaMesaiAylikPuantaj() : null, true, session);
				} else {
					gorevYeriList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(sirket, null, denklestirmeAy != null ? getFazlaMesaiAylikPuantaj() : null, true, session);
				}

				if (gorevYeriList.size() == 1) {
					seciliEkSaha3Id = (Long) gorevYeriList.get(0).getValue();
				}

			}
		}

		aylikPuantajList.clear();

		return "";
	}

	public void vardiyaGoster(VardiyaGun vg) {
		setVardiyaGun(vg);
		fazlaMesaiVardiyaGun = vg;
		toplamFazlamMesai = 0D;
		Long key = vg.getId();
		fmtList = fmtMap.containsKey(key) ? fmtMap.get(key) : null;

		if (vg.getIzin() == null && vg.getIzinler() != null) {
			for (Iterator iterator = vg.getIzinler().iterator(); iterator.hasNext();) {
				PersonelIzin personelIzin = (PersonelIzin) iterator.next();
				if (personelIzin.isGunlukOldu())
					iterator.remove();
			}
		}
		if (vg.getOrjinalHareketler() != null) {
			for (HareketKGS hareket : vg.getOrjinalHareketler()) {
				if (hareket.getPersonelFazlaMesai() != null && hareket.getPersonelFazlaMesai().isOnaylandi()) {
					if (hareket.getPersonelFazlaMesai().getFazlaMesaiSaati() != null)
						toplamFazlamMesai += hareket.getPersonelFazlaMesai().getFazlaMesaiSaati();
				}
			}
		}
	}

	// Haftalık çalışma planlarından giriş çıkış hareketi, izinler ve fazla
	// mesailerden haftalık toplam çalışma durumu hesaplanır

	public List<PersonelDenklestirme> getPersonelDenklestirmeList() {
		return personelDenklestirmeList;
	}

	public void setPersonelDenklestirmeList(List<PersonelDenklestirme> personelDenklestirmeList) {
		this.personelDenklestirmeList = personelDenklestirmeList;
	}

	public List<DepartmanDenklestirmeDonemi> getDenklestirmeDonemiList() {
		return denklestirmeDonemiList;
	}

	public void setDenklestirmeDonemiList(List<DepartmanDenklestirmeDonemi> denklestirmeDonemiList) {
		this.denklestirmeDonemiList = denklestirmeDonemiList;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket value) {
		this.sirket = value;
	}

	public List<PersonelDenklestirme> getBaslikDenklestirmeDonemiList() {
		return baslikDenklestirmeDonemiList;
	}

	public void setBaslikDenklestirmeDonemiList(List<PersonelDenklestirme> baslikDenklestirmeDonemiList) {
		this.baslikDenklestirmeDonemiList = baslikDenklestirmeDonemiList;
	}

	public Boolean getHataYok() {
		return hataYok;
	}

	public void setHataYok(Boolean hataYok) {
		this.hataYok = hataYok;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public List<YemekIzin> getYemekAraliklari() {
		return yemekAraliklari;
	}

	public void setYemekAraliklari(List<YemekIzin> yemekAraliklari) {
		this.yemekAraliklari = yemekAraliklari;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public int getAy() {
		return ay;
	}

	public void setAy(int ay) {
		this.ay = ay;
	}

	public List<SelectItem> getAylar() {
		return aylar;
	}

	public void setAylar(List<SelectItem> aylar) {
		this.aylar = aylar;
	}

	public int getMaxYil() {
		return maxYil;
	}

	public void setMaxYil(int maxYil) {
		this.maxYil = maxYil;
	}

	public Boolean getYetkili() {
		return yetkili;
	}

	public void setYetkili(Boolean yetkili) {
		this.yetkili = yetkili;
	}

	public void setAylikPuantajList(List<AylikPuantaj> aylikPuantajList) {
		this.aylikPuantajList = aylikPuantajList;
	}

	public List<AylikPuantaj> getAylikPuantajList() {
		return aylikPuantajList;
	}

	public AylikPuantaj getAylikPuantajDefault() {
		return aylikPuantajDefault;
	}

	public void setAylikPuantajDefault(AylikPuantaj aylikPuantajDefault) {
		this.aylikPuantajDefault = aylikPuantajDefault;
	}

	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		this.vardiyaGun = vardiyaGun;
	}

	public List<SelectItem> getGorevYeriList() {
		return gorevYeriList;
	}

	public void setGorevYeriList(List<SelectItem> value) {
		this.gorevYeriList = value;
	}

	public Long getSeciliEkSaha3Id() {
		return seciliEkSaha3Id;
	}

	public void setSeciliEkSaha3Id(Long seciliEkSaha3Id) {
		this.seciliEkSaha3Id = seciliEkSaha3Id;
	}

	public Tanim getGorevYeri() {
		return gorevYeri;
	}

	public void setGorevYeri(Tanim gorevYeri) {
		this.gorevYeri = gorevYeri;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public Boolean getResmiTatilVar() {
		return resmiTatilVar;
	}

	public void setResmiTatilVar(Boolean resmiTatilVar) {
		this.resmiTatilVar = resmiTatilVar;
	}

	public Boolean getKaydetDurum() {
		return kaydetDurum;
	}

	public void setKaydetDurum(Boolean kaydetDurum) {
		this.kaydetDurum = kaydetDurum;
	}

	public Long getGorevTipiId() {
		return gorevTipiId;
	}

	public void setGorevTipiId(Long gorevTipiId) {
		this.gorevTipiId = gorevTipiId;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public byte[] getExcelData() {
		return excelData;
	}

	public void setExcelData(byte[] excelData) {
		this.excelData = excelData;
	}

	public String getExcelDosyaAdi() {
		return excelDosyaAdi;
	}

	public void setExcelDosyaAdi(String excelDosyaAdi) {
		this.excelDosyaAdi = excelDosyaAdi;
	}

	public String getMailKonu() {
		return mailKonu;
	}

	public void setMailKonu(String mailKonu) {
		this.mailKonu = mailKonu;
	}

	public String getMailIcerik() {
		return mailIcerik;
	}

	public void setMailIcerik(String mailIcerik) {
		this.mailIcerik = mailIcerik;
	}

	public List<User> getToList() {
		return toList;
	}

	public void setToList(List<User> toList) {
		this.toList = toList;
	}

	public List<User> getCcList() {
		return ccList;
	}

	public void setCcList(List<User> ccList) {
		this.ccList = ccList;
	}

	public List<User> getBccList() {
		return bccList;
	}

	public void setBccList(List<User> bccList) {
		this.bccList = bccList;
	}

	public boolean isMailGonder() {
		return mailGonder;
	}

	public void setMailGonder(boolean mailGonder) {
		this.mailGonder = mailGonder;
	}

	public Boolean getOnayla() {
		return onayla;
	}

	public void setOnayla(Boolean onayla) {
		this.onayla = onayla;
	}

	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

	public List<SelectItem> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<SelectItem> departmanList) {
		this.departmanList = departmanList;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman value) {
		this.departman = value;
	}

	public Tanim getSeciliBolum() {
		return seciliBolum;
	}

	public void setSeciliBolum(Tanim seciliBolum) {
		this.seciliBolum = seciliBolum;
	}

	public Boolean getHastaneSuperVisor() {
		return hastaneSuperVisor;
	}

	public void setHastaneSuperVisor(Boolean hastaneSuperVisor) {
		this.hastaneSuperVisor = hastaneSuperVisor;
	}

	public Double getToplamFazlamMesai() {
		return toplamFazlamMesai;
	}

	public void setToplamFazlamMesai(Double toplamFazlamMesai) {
		this.toplamFazlamMesai = toplamFazlamMesai;
	}

	public Vardiya getSabahVardiya() {
		return sabahVardiya;
	}

	public void setSabahVardiya(Vardiya sabahVardiya) {
		this.sabahVardiya = sabahVardiya;
	}

	public List<SelectItem> getBolumDepartmanlari() {
		return bolumDepartmanlari;
	}

	public void setBolumDepartmanlari(List<SelectItem> bolumDepartmanlari) {
		this.bolumDepartmanlari = bolumDepartmanlari;
	}

	public Boolean getAksamGun() {
		return aksamGun;
	}

	public void setAksamGun(Boolean aksamGun) {
		this.aksamGun = aksamGun;
	}

	public Boolean getAksamSaat() {
		return aksamSaat;
	}

	public void setAksamSaat(Boolean aksamSaat) {
		this.aksamSaat = aksamSaat;
	}

	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public String getPersonelIzinGirisiStr() {
		return personelIzinGirisiStr;
	}

	public void setPersonelIzinGirisiStr(String personelIzinGirisiStr) {
		this.personelIzinGirisiStr = personelIzinGirisiStr;
	}

	public String getPersonelHareketStr() {
		return personelHareketStr;
	}

	public void setPersonelHareketStr(String personelHareketStr) {
		this.personelHareketStr = personelHareketStr;
	}

	public String getPersonelFazlaMesaiOrjStr() {
		return personelFazlaMesaiOrjStr;
	}

	public void setPersonelFazlaMesaiOrjStr(String personelFazlaMesaiOrjStr) {
		this.personelFazlaMesaiOrjStr = personelFazlaMesaiOrjStr;
	}

	public String getPersonelFazlaMesaiStr() {
		return personelFazlaMesaiStr;
	}

	public void setPersonelFazlaMesaiStr(String personelFazlaMesaiStr) {
		this.personelFazlaMesaiStr = personelFazlaMesaiStr;
	}

	public List<String> getSabahVardiyalar() {
		return sabahVardiyalar;
	}

	public void setSabahVardiyalar(List<String> sabahVardiyalar) {
		this.sabahVardiyalar = sabahVardiyalar;
	}

	public String getVardiyaPlaniStr() {
		return vardiyaPlaniStr;
	}

	public void setVardiyaPlaniStr(String vardiyaPlaniStr) {
		this.vardiyaPlaniStr = vardiyaPlaniStr;
	}

	public Boolean getStajerSirket() {
		return stajerSirket;
	}

	public void setStajerSirket(Boolean stajerSirket) {
		this.stajerSirket = stajerSirket;
	}

	public Boolean getBakiyeGuncelle() {
		return bakiyeGuncelle;
	}

	public void setBakiyeGuncelle(Boolean bakiyeGuncelle) {
		this.bakiyeGuncelle = bakiyeGuncelle;
	}

	public List<SelectItem> getPdksSirketList() {
		return pdksSirketList;
	}

	public void setPdksSirketList(List<SelectItem> value) {
		this.pdksSirketList = value;
	}

	public Boolean getHaftaTatilVar() {
		return haftaTatilVar;
	}

	public void setHaftaTatilVar(Boolean haftaTatilVar) {
		this.haftaTatilVar = haftaTatilVar;
	}

	public List<SelectItem> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<SelectItem> tesisList) {
		this.tesisList = tesisList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public Boolean getDepartmanBolumAyni() {
		return departmanBolumAyni;
	}

	public void setDepartmanBolumAyni(Boolean departmanBolumAyni) {
		this.departmanBolumAyni = departmanBolumAyni;
	}

	public boolean isTekSirket() {
		return tekSirket;
	}

	public void setTekSirket(boolean tekSirket) {
		this.tekSirket = tekSirket;
	}

	public Boolean getModelGoster() {
		return modelGoster;
	}

	public void setModelGoster(Boolean modelGoster) {
		this.modelGoster = modelGoster;
	}

	public String getMsgError() {
		return msgError;
	}

	public void setMsgError(String msgError) {
		this.msgError = msgError;
	}

	public String getMsgFazlaMesaiError() {
		return msgFazlaMesaiError;
	}

	public void setMsgFazlaMesaiError(String msgFazlaMesaiError) {
		this.msgFazlaMesaiError = msgFazlaMesaiError;
	}

	public TreeMap<String, Tanim> getFazlaMesaiMap() {
		return fazlaMesaiMap;
	}

	public void setFazlaMesaiMap(TreeMap<String, Tanim> fazlaMesaiMap) {
		this.fazlaMesaiMap = fazlaMesaiMap;
	}

	public Integer getAksamVardiyaBasSaat() {
		return aksamVardiyaBasSaat;
	}

	public void setAksamVardiyaBasSaat(Integer aksamVardiyaBasSaat) {
		this.aksamVardiyaBasSaat = aksamVardiyaBasSaat;
	}

	public Integer getAksamVardiyaBasDakika() {
		return aksamVardiyaBasDakika;
	}

	public void setAksamVardiyaBasDakika(Integer aksamVardiyaBasDakika) {
		this.aksamVardiyaBasDakika = aksamVardiyaBasDakika;
	}

	public Integer getAksamVardiyaBitDakika() {
		return aksamVardiyaBitDakika;
	}

	public void setAksamVardiyaBitDakika(Integer aksamVardiyaBitDakika) {
		this.aksamVardiyaBitDakika = aksamVardiyaBitDakika;
	}

	public TreeMap<Long, List<FazlaMesaiTalep>> getFmtMap() {
		return fmtMap;
	}

	public void setFmtMap(TreeMap<Long, List<FazlaMesaiTalep>> fmtMap) {
		this.fmtMap = fmtMap;
	}

	public List<FazlaMesaiTalep> getFmtList() {
		return fmtList;
	}

	public void setFmtList(List<FazlaMesaiTalep> fmtList) {
		this.fmtList = fmtList;
	}

	public Boolean getAyrikHareketVar() {
		return ayrikHareketVar;
	}

	public void setAyrikHareketVar(Boolean ayrikHareketVar) {
		this.ayrikHareketVar = ayrikHareketVar;
	}

	public String getSanalPersonelAciklama() {
		return sanalPersonelAciklama;
	}

	public void setSanalPersonelAciklama(String sanalPersonelAciklama) {
		this.sanalPersonelAciklama = sanalPersonelAciklama;
	}

	public Boolean getHataliPuantajGoster() {
		return hataliPuantajGoster;
	}

	public void setHataliPuantajGoster(Boolean hataliPuantajGoster) {
		this.hataliPuantajGoster = hataliPuantajGoster;
	}

	public Boolean getFazlaMesaiIzinKullan() {
		return fazlaMesaiIzinKullan;
	}

	public void setFazlaMesaiIzinKullan(Boolean fazlaMesaiIzinKullan) {
		this.fazlaMesaiIzinKullan = fazlaMesaiIzinKullan;
	}

	public Boolean getSirketIzinGirisDurum() {
		return sirketIzinGirisDurum;
	}

	public void setSirketIzinGirisDurum(Boolean sirketIzinGirisDurum) {
		this.sirketIzinGirisDurum = sirketIzinGirisDurum;
	}

	public CalismaModeli getPerCalismaModeli() {
		return perCalismaModeli;
	}

	public void setPerCalismaModeli(CalismaModeli perCalismaModeli) {
		this.perCalismaModeli = perCalismaModeli;
	}

	public Boolean getKullaniciPersonel() {
		return kullaniciPersonel;
	}

	public void setKullaniciPersonel(Boolean kullaniciPersonel) {
		this.kullaniciPersonel = kullaniciPersonel;
	}

	public Boolean getDenklestirmeAyDurum() {
		return denklestirmeAyDurum;
	}

	public void setDenklestirmeAyDurum(Boolean denklestirmeAyDurum) {
		this.denklestirmeAyDurum = denklestirmeAyDurum;
	}

	public boolean isAdminRole() {
		return adminRole;
	}

	public void setAdminRole(boolean adminRole) {
		this.adminRole = adminRole;
	}

	public boolean isIkRole() {
		return ikRole;
	}

	public void setIkRole(boolean ikRole) {
		this.ikRole = ikRole;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Boolean getMaasKesintiGoster() {
		return maasKesintiGoster;
	}

	public void setMaasKesintiGoster(Boolean maasKesintiGoster) {
		this.maasKesintiGoster = maasKesintiGoster;
	}

	/**
	 * @return the yoneticiERP1Kontrol
	 */
	public Boolean getYoneticiERP1Kontrol() {
		return yoneticiERP1Kontrol;
	}

	/**
	 * @param yoneticiERP1Kontrol
	 *            the yoneticiERP1Kontrol to set
	 */
	public void setYoneticiERP1Kontrol(Boolean yoneticiERP1Kontrol) {
		this.yoneticiERP1Kontrol = yoneticiERP1Kontrol;
	}

	public Boolean getFazlaMesaiOde() {
		return fazlaMesaiOde;
	}

	public void setFazlaMesaiOde(Boolean fazlaMesaiOde) {
		this.fazlaMesaiOde = fazlaMesaiOde;
	}

	public Boolean getKimlikGoster() {
		return kimlikGoster;
	}

	public void setKimlikGoster(Boolean kimlikGoster) {
		this.kimlikGoster = kimlikGoster;
	}

	public Boolean getYasalFazlaCalismaAsanSaat() {
		return yasalFazlaCalismaAsanSaat;
	}

	public void setYasalFazlaCalismaAsanSaat(Boolean yasalFazlaCalismaAsanSaat) {
		this.yasalFazlaCalismaAsanSaat = yasalFazlaCalismaAsanSaat;
	}

	public Boolean getSirketGoster() {
		return sirketGoster;
	}

	public void setSirketGoster(Boolean sirketGoster) {
		this.sirketGoster = sirketGoster;
	}

	public boolean isBordroPuantajEkranindaGoster() {
		return bordroPuantajEkranindaGoster;
	}

	public void setBordroPuantajEkranindaGoster(boolean bordroPuantajEkranindaGoster) {
		this.bordroPuantajEkranindaGoster = bordroPuantajEkranindaGoster;
	}

	public boolean isFazlaMesaiVar() {
		return fazlaMesaiVar;
	}

	public void setFazlaMesaiVar(boolean fazlaMesaiVar) {
		this.fazlaMesaiVar = fazlaMesaiVar;
	}

	public boolean isSaatlikMesaiVar() {
		return saatlikMesaiVar;
	}

	public void setSaatlikMesaiVar(boolean saatlikMesaiVar) {
		this.saatlikMesaiVar = saatlikMesaiVar;
	}

	public boolean isAylikMesaiVar() {
		return aylikMesaiVar;
	}

	public void setAylikMesaiVar(boolean aylikMesaiVar) {
		this.aylikMesaiVar = aylikMesaiVar;
	}

	public TreeMap<String, Tatil> getTatilGunleriMap() {
		return tatilGunleriMap;
	}

	public void setTatilGunleriMap(TreeMap<String, Tatil> tatilGunleriMap) {
		this.tatilGunleriMap = tatilGunleriMap;
	}

	public Boolean getGerceklesenMesaiKod() {
		return gerceklesenMesaiKod;
	}

	public void setGerceklesenMesaiKod(Boolean gerceklesenMesaiKod) {
		this.gerceklesenMesaiKod = gerceklesenMesaiKod;
	}

	public Boolean getDevredenBakiyeKod() {
		return devredenBakiyeKod;
	}

	public void setDevredenBakiyeKod(Boolean devredenBakiyeKod) {
		this.devredenBakiyeKod = devredenBakiyeKod;
	}

	public Boolean getNormalCalismaSaatKod() {
		return normalCalismaSaatKod;
	}

	public void setNormalCalismaSaatKod(Boolean normalCalismaSaatKod) {
		this.normalCalismaSaatKod = normalCalismaSaatKod;
	}

	public Boolean getHaftaTatilCalismaSaatKod() {
		return haftaTatilCalismaSaatKod;
	}

	public void setHaftaTatilCalismaSaatKod(Boolean haftaTatilCalismaSaatKod) {
		this.haftaTatilCalismaSaatKod = haftaTatilCalismaSaatKod;
	}

	public Boolean getResmiTatilCalismaSaatKod() {
		return resmiTatilCalismaSaatKod;
	}

	public void setResmiTatilCalismaSaatKod(Boolean resmiTatilCalismaSaatKod) {
		this.resmiTatilCalismaSaatKod = resmiTatilCalismaSaatKod;
	}

	public Boolean getIzinSureSaatKod() {
		return izinSureSaatKod;
	}

	public void setIzinSureSaatKod(Boolean izinSureSaatKod) {
		this.izinSureSaatKod = izinSureSaatKod;
	}

	public Boolean getNormalCalismaGunKod() {
		return normalCalismaGunKod;
	}

	public void setNormalCalismaGunKod(Boolean normalCalismaGunKod) {
		this.normalCalismaGunKod = normalCalismaGunKod;
	}

	public Boolean getHaftaTatilCalismaGunKod() {
		return haftaTatilCalismaGunKod;
	}

	public void setHaftaTatilCalismaGunKod(Boolean haftaTatilCalismaGunKod) {
		this.haftaTatilCalismaGunKod = haftaTatilCalismaGunKod;
	}

	public Boolean getResmiTatilCalismaGunKod() {
		return resmiTatilCalismaGunKod;
	}

	public void setResmiTatilCalismaGunKod(Boolean resmiTatilCalismaGunKod) {
		this.resmiTatilCalismaGunKod = resmiTatilCalismaGunKod;
	}

	public Boolean getIzinSureGunKod() {
		return izinSureGunKod;
	}

	public void setIzinSureGunKod(Boolean izinSureGunKod) {
		this.izinSureGunKod = izinSureGunKod;
	}

	public Boolean getUcretliIzinGunKod() {
		return ucretliIzinGunKod;
	}

	public void setUcretliIzinGunKod(Boolean ucretliIzinGunKod) {
		this.ucretliIzinGunKod = ucretliIzinGunKod;
	}

	public Boolean getUcretsizIzinGunKod() {
		return ucretsizIzinGunKod;
	}

	public void setUcretsizIzinGunKod(Boolean ucretsizIzinGunKod) {
		this.ucretsizIzinGunKod = ucretsizIzinGunKod;
	}

	public Boolean getHastalikIzinGunKod() {
		return hastalikIzinGunKod;
	}

	public void setHastalikIzinGunKod(Boolean hastalikIzinGunKod) {
		this.hastalikIzinGunKod = hastalikIzinGunKod;
	}

	public Boolean getNormalGunKod() {
		return normalGunKod;
	}

	public void setNormalGunKod(Boolean normalGunKod) {
		this.normalGunKod = normalGunKod;
	}

	public Boolean getHaftaTatilGunKod() {
		return haftaTatilGunKod;
	}

	public void setHaftaTatilGunKod(Boolean haftaTatilGunKod) {
		this.haftaTatilGunKod = haftaTatilGunKod;
	}

	public Boolean getResmiTatilGunKod() {
		return resmiTatilGunKod;
	}

	public void setResmiTatilGunKod(Boolean resmiTatilGunKod) {
		this.resmiTatilGunKod = resmiTatilGunKod;
	}

	public Boolean getArtikGunKod() {
		return artikGunKod;
	}

	public void setArtikGunKod(Boolean artikGunKod) {
		this.artikGunKod = artikGunKod;
	}

	public Boolean getBordroToplamGunKod() {
		return bordroToplamGunKod;
	}

	public void setBordroToplamGunKod(Boolean bordroToplamGunKod) {
		this.bordroToplamGunKod = bordroToplamGunKod;
	}

	public Boolean getDevredenMesaiKod() {
		return devredenMesaiKod;
	}

	public void setDevredenMesaiKod(Boolean devredenMesaiKod) {
		this.devredenMesaiKod = devredenMesaiKod;
	}

	public Boolean getUcretiOdenenKod() {
		return ucretiOdenenKod;
	}

	public void setUcretiOdenenKod(Boolean ucretiOdenenKod) {
		this.ucretiOdenenKod = ucretiOdenenKod;
	}

	public Tanim getTesis() {
		return tesis;
	}

	public void setTesis(Tanim tesis) {
		this.tesis = tesis;
	}

	public Double getAksamCalismaSaati() {
		return aksamCalismaSaati;
	}

	public void setAksamCalismaSaati(Double aksamCalismaSaati) {
		this.aksamCalismaSaati = aksamCalismaSaati;
	}

	public Double getAksamCalismaSaatiYuzde() {
		return aksamCalismaSaatiYuzde;
	}

	public void setAksamCalismaSaatiYuzde(Double aksamCalismaSaatiYuzde) {
		this.aksamCalismaSaatiYuzde = aksamCalismaSaatiYuzde;
	}

	public TreeMap<String, Boolean> getBaslikMap() {
		return baslikMap;
	}

	public void setBaslikMap(TreeMap<String, Boolean> baslikMap) {
		this.baslikMap = baslikMap;
	}

	public List<Tanim> getDenklestirmeDinamikAlanlar() {
		return denklestirmeDinamikAlanlar;
	}

	public void setDenklestirmeDinamikAlanlar(List<Tanim> denklestirmeDinamikAlanlar) {
		this.denklestirmeDinamikAlanlar = denklestirmeDinamikAlanlar;
	}

	public Boolean getHataliPuantajVar() {
		return hataliPuantajVar;
	}

	public void setHataliPuantajVar(Boolean hataliPuantajVar) {
		this.hataliPuantajVar = hataliPuantajVar;
	}

	public Boolean getHatalariDuzelt() {
		return hatalariDuzelt;
	}

	public void setHatalariDuzelt(Boolean hatalariDuzelt) {
		this.hatalariDuzelt = hatalariDuzelt;
	}

	public String getDuzeltTipi() {
		return duzeltTipi;
	}

	public void setDuzeltTipi(String duzeltTipi) {
		this.duzeltTipi = duzeltTipi;
	}

	public List<SelectItem> getDuzeltTipleri() {
		return duzeltTipleri;
	}

	public void setDuzeltTipleri(List<SelectItem> duzeltTipleri) {
		this.duzeltTipleri = duzeltTipleri;
	}

	public Tanim getEkSaha4Tanim() {
		return ekSaha4Tanim;
	}

	public void setEkSaha4Tanim(Tanim ekSaha4Tanim) {
		this.ekSaha4Tanim = ekSaha4Tanim;
	}

	public Boolean getSuaDurum() {
		return suaDurum;
	}

	public void setSuaDurum(Boolean suaDurum) {
		this.suaDurum = suaDurum;
	}

	public Boolean getSutIzniDurum() {
		return sutIzniDurum;
	}

	public void setSutIzniDurum(Boolean sutIzniDurum) {
		this.sutIzniDurum = sutIzniDurum;
	}

	public Boolean getGebeDurum() {
		return gebeDurum;
	}

	public void setGebeDurum(Boolean gebeDurum) {
		this.gebeDurum = gebeDurum;
	}

	public Boolean getPartTime() {
		return partTime;
	}

	public void setPartTime(Boolean partTime) {
		this.partTime = partTime;
	}

	public HashMap<String, Long> getVardiyaAdetMap() {
		return vardiyaAdetMap;
	}

	public void setVardiyaAdetMap(HashMap<String, Long> vardiyaAdetMap) {
		this.vardiyaAdetMap = vardiyaAdetMap;
	}

	public Long getVardiyaAdet() {
		return vardiyaAdet;
	}

	public void setVardiyaAdet(Long vardiyaAdet) {
		this.vardiyaAdet = vardiyaAdet;
	}

	public List<VardiyaGun> getTumVardiyaList() {
		return tumVardiyaList;
	}

	public void setTumVardiyaList(List<VardiyaGun> tumVardiyaList) {
		this.tumVardiyaList = tumVardiyaList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		FazlaMesaiArgeRaporHome.sayfaURL = sayfaURL;
	}

	public List<Tanim> getPersonelDinamikAlanlar() {
		return personelDinamikAlanlar;
	}

	public void setPersonelDinamikAlanlar(List<Tanim> personelDinamikAlanlar) {
		this.personelDinamikAlanlar = personelDinamikAlanlar;
	}

	public TreeMap<String, PersonelDinamikAlan> getPersonelDinamikAlanMap() {
		return personelDinamikAlanMap;
	}

	public void setPersonelDinamikAlanMap(TreeMap<String, PersonelDinamikAlan> personelDinamikAlanMap) {
		this.personelDinamikAlanMap = personelDinamikAlanMap;
	}

	public Boolean getIsAramaDurum() {
		return isAramaDurum;
	}

	public void setIsAramaDurum(Boolean isAramaDurum) {
		this.isAramaDurum = isAramaDurum;
	}

	public boolean isVardiyaPlanTopluAdet() {
		return vardiyaPlanTopluAdet;
	}

	public void setVardiyaPlanTopluAdet(boolean vardiyaPlanTopluAdet) {
		this.vardiyaPlanTopluAdet = vardiyaPlanTopluAdet;
	}

	public Boolean getPdfTopluAktarDurum() {
		return pdfTopluAktarDurum;
	}

	public void setPdfTopluAktarDurum(Boolean pdfTopluAktarDurum) {
		this.pdfTopluAktarDurum = pdfTopluAktarDurum;
	}

	public Boolean getSecimDurum() {
		return secimDurum;
	}

	public void setSecimDurum(Boolean secimDurum) {
		this.secimDurum = secimDurum;
	}

	public List<AylikPuantaj> getOnayList() {
		return onayList;
	}

	public void setOnayList(List<AylikPuantaj> onayList) {
		this.onayList = onayList;
	}

	public Boolean getResmiTatilKanunenEklenenSureGoster() {
		return resmiTatilKanunenEklenenSureGoster;
	}

	public void setResmiTatilKanunenEklenenSureGoster(Boolean resmiTatilKanunenEklenenSureGoster) {
		this.resmiTatilKanunenEklenenSureGoster = resmiTatilKanunenEklenenSureGoster;
	}

	public Boolean getIcapciSaatGoster() {
		return icapciSaatGoster;
	}

	public void setIcapciSaatGoster(Boolean icapciSaatGoster) {
		this.icapciSaatGoster = icapciSaatGoster;
	}

	public List<Vardiya> getIzinTipiVardiyaList() {
		return izinTipiVardiyaList;
	}

	public void setIzinTipiVardiyaList(List<Vardiya> izinTipiVardiyaList) {
		this.izinTipiVardiyaList = izinTipiVardiyaList;
	}

	public TreeMap<String, TreeMap<String, List<VardiyaGun>>> getIzinTipiPersonelVardiyaMap() {
		return izinTipiPersonelVardiyaMap;
	}

	public void setIzinTipiPersonelVardiyaMap(TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap) {
		this.izinTipiPersonelVardiyaMap = izinTipiPersonelVardiyaMap;
	}

	public Boolean getIzinGoster() {
		return izinGoster;
	}

	public void setIzinGoster(Boolean izinGoster) {
		this.izinGoster = izinGoster;
	}

	public Parameter getAylikVardiyaTabloHareketExcelParameter() {
		return aylikVardiyaTabloHareketExcelParameter;
	}

	public void setAylikVardiyaTabloHareketExcelParameter(Parameter aylikVardiyaTabloHareketExcelParameter) {
		this.aylikVardiyaTabloHareketExcelParameter = aylikVardiyaTabloHareketExcelParameter;
	}

	public Boolean getPdfBirlestirDurum() {
		return pdfBirlestirDurum;
	}

	public void setPdfBirlestirDurum(Boolean pdfBirlestirDurum) {
		this.pdfBirlestirDurum = pdfBirlestirDurum;
	}

	public Boolean getBrutUcretGoster() {
		return brutUcretGoster;
	}

	public void setBrutUcretGoster(Boolean brutUcretGoster) {
		this.brutUcretGoster = brutUcretGoster;
	}

	public Boolean getTesisDurum() {
		return tesisDurum;
	}

	public void setTesisDurum(Boolean tesisDurum) {
		this.tesisDurum = tesisDurum;
	}

	public Boolean getArgeGoster() {
		return argeGoster;
	}

	public void setArgeGoster(Boolean argeGoster) {
		this.argeGoster = argeGoster;
	}

	public Boolean getArgeTableGoster() {
		return argeTableGoster;
	}

	public void setArgeTableGoster(Boolean argeTableGoster) {
		this.argeTableGoster = argeTableGoster;
	}

	public AylikPuantaj getSeciliAylikPuantaj() {
		return seciliAylikPuantaj;
	}

	public void setSeciliAylikPuantaj(AylikPuantaj value) {
		CalismaModeli cm = null;
		if (value != null)
			cm = value.getCalismaModeli();
		this.seciliCalismaModeli = cm;
		this.seciliAylikPuantaj = value;
	}

	public CalismaModeli getSeciliCalismaModeli() {
		return seciliCalismaModeli;
	}

	public void setSeciliCalismaModeli(CalismaModeli seciliCalismaModeli) {
		this.seciliCalismaModeli = seciliCalismaModeli;
	}

	public int getColSpan() {
		return colSpan;
	}

	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}

	public int getRowSpan() {
		return rowSpan;
	}

	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	public Long getYukseklik() {
		return yukseklik;
	}

	public void setYukseklik(Long yukseklik) {
		this.yukseklik = yukseklik;
	}
}
