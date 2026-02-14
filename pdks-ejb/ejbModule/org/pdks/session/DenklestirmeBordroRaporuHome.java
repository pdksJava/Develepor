package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.Dosya;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeBordro;
import org.pdks.entity.PersonelDenklestirmeBordroDetay;
import org.pdks.entity.PersonelDenklestirmeDinamikAlan;
import org.pdks.entity.PersonelDinamikAlan;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.enums.BordroDetayTipi;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

/**
 * @author Hasan Sayar
 * 
 */
@Name("denklestirmeBordroRaporuHome")
public class DenklestirmeBordroRaporuHome extends EntityHome<DenklestirmeAy> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9211132861369205688L;

	public static String sayfaURL = "denklestirmeBordroRaporu";

	static Logger logger = Logger.getLogger(DenklestirmeBordroRaporuHome.class);

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
	FazlaMesaiHesaplaHome fazlaMesaiHesaplaHome;
	@In(required = false, create = true)
	VardiyaGunHome vardiyaGunHome;

	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	@Out(scope = ScopeType.SESSION, required = false)
	String bordroAdres;

	private User pdksUser;

	private List<AylikPuantaj> personelDenklestirmeList;
	private TreeMap<String, PersonelDinamikAlan> personelDinamikAlanMap;

	private Boolean secimDurum = Boolean.FALSE, sureDurum, fazlaMesaiDurum, haftaTatilDurum, artikGunDurum, resmiTatilGunDurum, resmiTatilDurum, durumERP, onaylanmayanDurum, personelERP, modelGoster = Boolean.FALSE;
	private Boolean normalGunSaatDurum = Boolean.FALSE, haftaTatilSaatDurum = Boolean.FALSE, resmiTatilSaatDurum = Boolean.FALSE, izinSaatDurum = Boolean.FALSE;
	private Boolean denklestirmeAyDurum, fazlaMesaiHesaplaDurum = false, sadeceHataliGetir = false, login = false;
	private int ay, yil, maxYil, minYil;

	private List<SelectItem> aylar;

	private String sicilNo = "", bolumAciklama, linkAdresKey, fazlaMesaiHesaplaMenuAdi;

	private DenklestirmeAy denklestirmeAy;

	private Tanim ekSaha4Tanim;

	public String COL_SIRA = "sira";
	public String COL_YIL = "yil";
	public String COL_AY = "ay";
	public String COL_AY_ADI = "ayAdi";
	public String COL_PERSONEL_NO = "personelNo";
	public String COL_AD = "ad";
	public String COL_SOYAD = "soyad";
	public String COL_AD_SOYAD = "adSoyad";
	public String COL_KART_NO = "kartNo";
	public String COL_KIMLIK_NO = "kimlikNo";
	public String COL_SIRKET = "sirket";
	public String COL_TESIS = "tesis";
	public String COL_BOLUM = "bolumAdi";
	public String COL_ALT_BOLUM = "altBolumAdi";
	public String COL_NORMAL_GUN_ADET = "normalGunAdet";
	public String COL_HAFTA_TATIL_ADET = "haftaTatilAdet";
	public String COL_RESMI_TATIL_ADET = "resmiTatilAdet";
	public String COL_ARTIK_ADET = "artikAdet";
	public String COL_TOPLAM_ADET = "toplamAdet";
	public String COL_NORMAL_GUN_SAAT = "normalGunSaat";
	public String COL_HAFTA_TATIL_SAAT = "haftaTatilSaat";
	public String COL_RESMI_TATIL_SAAT = "resmiTatilSaat";
	public String COL_IZIN_SAAT = "izinSaat";
	public String COL_UCRETLI_IZIN = "ucretliIzin";
	public String COL_RAPORLU_IZIN = "raporluIzin";
	public String COL_UCRETSIZ_IZIN = "ucretsizIzin";
	public String COL_YILLIK_IZIN = "yillikIzin";
	public String COL_RESMI_TATIL_MESAI = "resmiTatilMesai";
	public String COL_UCRETI_ODENEN_MESAI = "ucretiOdenenMesai";
	public String COL_HAFTA_TATIL_MESAI = "haftaTatilMesai";
	public String COL_AKSAM_SAAT_MESAI = "aksamSaatMesai";
	public String COL_AKSAM_GUN_MESAI = "aksamGunMesai";
	public String COL_EKSIK_CALISMA = "eksikCalisma";
	public String COL_DEVAMLILIK_PRIMI = PersonelDenklestirmeDinamikAlan.TIPI_DEVAMLILIK_PRIMI;
	public String COL_CALISMA_MODELI = "calismaModeli";

	public String COL_ISE_BASLAMA_TARIHI = "iseBaslamaTarihi";
	public String COL_SSK_CIKIS_TARIHI = "istenAyrilisTarihi";

	private Date basGun, bitGun;

	private Sirket sirket;

	private Long sirketId, departmanId, tesisId;

	private List<SelectItem> sirketler, departmanList, tesisList;

	private Departman departman;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private Dosya fazlaMesaiDosya = new Dosya();
	private Boolean aksamGun = Boolean.FALSE, haftaCalisma = Boolean.FALSE, calismaModeliDurum = Boolean.FALSE, aksamSaat = Boolean.FALSE, erpAktarimDurum = Boolean.FALSE, maasKesintiGoster = Boolean.FALSE;
	private Boolean hataliVeriGetir, eksikCalisanVeriGetir, adminRole, ikRole, devamlikPrimGoster;
	private List<Vardiya> izinTipiVardiyaList;
	private TreeMap<String, TreeMap<String, List<VardiyaGun>>> izinTipiPersonelVardiyaMap;
	private TreeMap<String, Tanim> baslikMap;
	private TreeMap<Long, Personel> izinTipiPersonelMap;
	private List<Tanim> personelDinamikAlanlar;
	private Date sonGun, ilkGun;
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
	@Begin(join = true)
	public void create() {
		super.create();
	}

	/**
	 * 
	 */
	private void adminRoleDurum() {
		adminRole = getPdksUser().isAdmin() || getPdksUser().isSistemYoneticisi();
		ikRole = getPdksUser().isAdmin() || getPdksUser().isSistemYoneticisi() || getPdksUser().isIK();
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	public String getFazlaMesaiGuncellemeAciklama() {
		String str = "";
		if (tesisId != null && tesisId.longValue() > 0L)
			str = ortakIslemler.tesisAciklama();
		else if (sirket != null && sirket.isTesisDurumu() == false)
			str = ortakIslemler.sirketAciklama();
		return str;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	@Transactional
	public String sayfaFazlaMesaiGuncellemeAction() throws Exception {
		if (session == null)
			session = PdksUtil.getSession(entityManager, true);
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String id = (String) req.getParameter("id");
		if (session != null && id != null) {
			String decodeStr = OrtakIslemler.getDecodeStringByBase64(id);
			StringTokenizer st = new StringTokenizer(decodeStr, "&");
			HashMap<String, String> param = new HashMap<String, String>();
			while (st.hasMoreTokens()) {
				String tk = st.nextToken();
				String[] parStrings = tk.split("=");
				param.put(parStrings[0], parStrings[1]);
			}
			ekSaha4Tanim = null;
			if (param.containsKey("donemId") && param.containsKey("sirketId")) {
				session.setFlushMode(FlushMode.MANUAL);
				session.clear();
				try {
					Long donemId = Long.parseLong(param.get("donemId"));
					denklestirmeAy = (DenklestirmeAy) pdksEntityController.getSQLParamByFieldObject(DenklestirmeAy.TABLE_NAME, DenklestirmeAy.COLUMN_NAME_ID, donemId, DenklestirmeAy.class, session);
					if (denklestirmeAy != null && denklestirmeAy.getDurum()) {
						login = param.containsKey("login");
						if (baslikMap == null)
							baslikMap = new TreeMap<String, Tanim>();
						if (personelDenklestirmeList == null)
							personelDenklestirmeList = new ArrayList<AylikPuantaj>();
						yil = denklestirmeAy.getYil();
						ay = denklestirmeAy.getAy();
						List<Long> userIkIdList = ortakIslemler.getIKUserIdList(session);
						fazlaMesaiHesaplaHome.setUserIkIdList(userIkIdList);
						sirketId = Long.parseLong(param.get("sirketId"));
						sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);
						if (ekSaha4Tanim == null)
							ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
						departman = sirket.getDepartman();
						departmanId = departman.getId();
						tesisId = null;
						boolean sirketCalistir = true;
						Long pdksUserId = param.containsKey("pdksUserId") ? Long.parseLong(param.get("pdksUserId")) : null;
						if (pdksUserId != null)
							pdksUser = (User) pdksEntityController.getSQLParamByFieldObject(User.TABLE_NAME, User.COLUMN_NAME_ID, pdksUserId, User.class, session);
						else
							pdksUser = ortakIslemler.getSistemAdminUser(session);
						pdksUser.setAdmin(true);
						pdksUser.setLogin(false);
						departmanId = departman.getId();
						String donemStr = denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " : " + sirket.getAd();
						if (sirket.isTesisDurumu()) {
							String tesisIdStr = param.get("tesisId");
							List<Long> idList = new ArrayList<Long>();
							if (tesisIdStr.equals("*")) {
								sirketCalistir = false;
								DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
								AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(denklestirmeAy.getAy(), denklestirmeAy.getYil(), denklestirmeDonemi, session);
								aylikPuantaj.setLoginUser(pdksUser);
								aylikPuantaj.setDenklestirmeAy(denklestirmeAy);
								List<SelectItem> tesisDetayList = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, aylikPuantaj, false, session);
								for (SelectItem selectItem : tesisDetayList)
									idList.add((Long) selectItem.getValue());
								tesisDetayList = null;
							} else {

								Long paramId = param.containsKey("tesisId") ? Long.parseLong(tesisIdStr) : null;
								if (paramId != null)
									idList.add(paramId);

							}
							if (!idList.isEmpty()) {
								sirketCalistir = false;
								List<Tanim> tesisList = pdksEntityController.getSQLParamByFieldList(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, idList, Tanim.class, session);
								if (tesisList.size() > 1)
									tesisList = PdksUtil.sortTanimList(Constants.TR_LOCALE, tesisList);
								for (Tanim tesis : tesisList) {
									tesisId = tesis.getId();
									String str = PdksUtil.replaceAll(donemStr + " " + tesis.getAciklama(), "  ", " ").trim();
									if (login == false)
										logger.info(str + " in " + PdksUtil.getCurrentTimeStampStr());
									sirketFazlaMesaiGuncelleme();
									if (login == false)
										logger.info(str + " out " + PdksUtil.getCurrentTimeStampStr());

								}
								tesisList = null;
							}
							idList = null;
						}
						if (sirketCalistir) {
							String str = PdksUtil.replaceAll(donemStr, "  ", " ").trim();

							tesisId = null;
							if (login == false)
								logger.info(str + " in " + PdksUtil.getCurrentTimeStampStr());
							sirketFazlaMesaiGuncelleme();
							if (login == false)
								logger.info(str + " out " + PdksUtil.getCurrentTimeStampStr());

						}
					}

				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		}
		session.close();
		return MenuItemConstant.login;
	}

	@Transactional
	public String sirketFazlaMesaiGuncelleme() {
		HashMap fields = new HashMap();
		fields.put("id", sirketId);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		session.clear();
		Sirket sirketSecili = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);
		if (sirketSecili != null) {
			fazlaMesaiHesaplaHome.setSession(session);
			vardiyaGunHome.setSession(session);
			User loginUser = authenticatedUser != null ? ortakIslemler.getSistemAdminUser(session) : pdksUser;
			loginUser.setAdmin(Boolean.TRUE);
			loginUser.setLogin(Boolean.FALSE);
			DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
			denklestirmeDonemi.setLoginUser(loginUser);
			AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
			aylikPuantaj.setLoginUser(loginUser);
			if (getPdksUser() != null && getPdksUser().isIK())
				loginUser = getPdksUser();
			loginUser.setAdmin(Boolean.TRUE);

			aylikPuantaj.setDenklestirmeAy(denklestirmeAy);
			denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);

			boolean denklestirme = loginUser.isAdmin() == false;
			try {
				LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
				paramMap.put("loginUser", loginUser);
				paramMap.put("denklestirmeDonemi", denklestirmeDonemi);
				paramMap.put("aylikPuantaj", aylikPuantaj);
				paramMap.put("seciliSirket", sirketSecili);
				paramMap.put("denklestirme", denklestirme);
				if (sirketSecili.isTesisDurumu()) {
					if (tesisId != null) {
						paramMap.put("seciliTesisId", tesisId);
						denklestirme = bolumFazlaMesai(paramMap);
					} else {
						List<SelectItem> tesisDetayList = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirketSecili, aylikPuantaj, loginUser.isAdmin() == false, session);
						for (SelectItem selectItem3 : tesisDetayList) {
							Long tesis1Id = (Long) selectItem3.getValue();
							paramMap.put("seciliTesisId", tesis1Id);
							denklestirme = bolumFazlaMesai(paramMap);
							if (!denklestirme)
								break;
						}
					}

				} else
					denklestirme = bolumFazlaMesai(paramMap);
				FacesMessages facesMessages = (FacesMessages) Component.getInstance("facesMessages");
				facesMessages.clear();
				List<String> mesajlar = new ArrayList<String>();
				List list = facesMessages.getCurrentGlobalMessages();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					FacesMessage fm = (FacesMessage) iterator.next();
					if (!mesajlar.contains(fm.getDetail()))
						mesajlar.add(fm.getDetail());
					else
						iterator.remove();
				}

				personelDenklestirmeList.clear();
				if (denklestirme) {
					session.clear();
					fillPersonelDenklestirmeList();
					Tanim tesis = null;
					if (tesisId != null) 
 						tesis = (Tanim) pdksEntityController.getSQLParamByFieldObject(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, tesisId, Tanim.class, session);
					 
					if (authenticatedUser != null)
						PdksUtil.addMessageInfo(sirket.getAd() + (tesis != null ? " " + tesis.getAciklama() : "") + " fazla mesai hesaplaması tamamlandı.");
				}

			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		return "";
	}

	/**
	 * @param paramMap
	 * @return
	 */
	@Transactional
	private boolean bolumFazlaMesai(LinkedHashMap<String, Object> paramMap) {
		Long donemKodu = Long.parseLong(PdksUtil.convertToDateString(new Date(), "yyyyMM")), islemDonemKodu = denklestirmeAy.getDonemKodu();
		AylikPuantaj aylikPuantaj = (AylikPuantaj) paramMap.get("aylikPuantaj");
		boolean logYaz = authenticatedUser != null || login;
		if (logYaz == false)
			logYaz = ortakIslemler.getCanliDurum() == false && ortakIslemler.getTestSunucuDurum() == false;
		User loginUser = (User) paramMap.get("loginUser");
		Sirket seciliSirket = (Sirket) paramMap.get("seciliSirket");
		Long seciliTesisId = paramMap.containsKey("seciliTesisId") ? (Long) paramMap.get("seciliTesisId") : null;
		List<SelectItem> bolumList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(seciliSirket, seciliTesisId != null ? String.valueOf(seciliTesisId) : "", aylikPuantaj, loginUser.isAdmin() == false, session);
		AylikPuantaj ap = aylikPuantaj != null ? (AylikPuantaj) aylikPuantaj.clone() : new AylikPuantaj(denklestirmeAy);
		if (ap.getLoginUser() == null)
			ap.setLoginUser(loginUser);
		Tanim tesis = null;
		if (seciliTesisId != null && seciliSirket.isTesisDurumu())
			tesis = (Tanim) pdksEntityController.getSQLParamByFieldObject(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, seciliTesisId, Tanim.class, session);

		String baslik = denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " " + (seciliSirket.getSirketGrup() == null ? seciliSirket.getAd() : seciliSirket.getSirketGrup().getAciklama()) + (tesis != null ? " " + tesis.getAciklama() : "");
		boolean hataYok = true;
		Date basGun = PdksUtil.convertToJavaDate(String.valueOf(yil * 100 + ay) + "01", "yyyyMMdd"), bugun = new Date();
		boolean gelecekTarih = basGun.after(bugun);
		DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
		AylikPuantaj aylikPuantajDefault = fazlaMesaiOrtakIslemler.getAylikPuantaj(denklestirmeAy.getAy(), denklestirmeAy.getYil(), denklestirmeDonemi, session);
		vardiyaGunHome.setAylikPuantajDefault(aylikPuantajDefault);
		if (ekSaha4Tanim == null)
			ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
		for (SelectItem selectItem : bolumList) {
			Long seciliEkSaha3Id = (Long) selectItem.getValue();
			String linkStr = "donemId=" + denklestirmeAy.getId() + "&sirketId=" + sirketId + (seciliTesisId != null ? "&tesisId=" + seciliTesisId : "") + "&seciliEkSaha3Id=" + seciliEkSaha3Id;
			try {
				Tanim bolum = (Tanim) pdksEntityController.getSQLParamByFieldObject(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, seciliEkSaha3Id, Tanim.class, session);
				String str = baslik + (bolum != null ? " " + bolum.getAciklama() : "");
				List<Personel> donemPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, seciliTesisId != null ? String.valueOf(seciliTesisId) : null, seciliEkSaha3Id, null, aylikPuantaj, false, session);
				int kayitAdet = donemPerList != null ? donemPerList.size() : 0;
				if (kayitAdet > 50 && ekSaha4Tanim != null) {
					List<SelectItem> altBolumList = fazlaMesaiOrtakIslemler.getFazlaMesaiAltBolumList(sirket, seciliTesisId != null ? String.valueOf(seciliTesisId) : null, seciliEkSaha3Id, denklestirmeAy != null ? ap : null, false, session);
					if (altBolumList != null && altBolumList.size() > 1) {
						for (SelectItem altBolumSt : altBolumList) {
							Long altBolumId = (Long) altBolumSt.getValue();
							String altBolumStr = str + " : " + altBolumSt.getLabel();
							donemPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, seciliTesisId != null ? String.valueOf(seciliTesisId) : null, seciliEkSaha3Id, altBolumId, aylikPuantaj, false, session);
							kayitAdet = donemPerList != null ? donemPerList.size() : 0;
							int adet = 0;
							boolean devam = kayitAdet > 0;
							while (devam && adet < 2) {
								session.clear();
								List<Personel> donemCPPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(denklestirmeAy, donemPerList, session);
								try {
									devam = donemCPPerList != null && kayitAdet != donemCPPerList.size();
									if (devam && donemKodu.longValue() > denklestirmeAy.getDonem()) {
										logger.info(altBolumStr + " aylikPuantajOlusturuluyor in " + PdksUtil.getCurrentTimeStampStr());
										String idStr = ortakIslemler.getEncodeStringByBase64(linkStr + "&seciliEkSaha4Id=" + altBolumId);
										vardiyaGunHome.sayfaCalismaPlanOlustur(idStr, loginUser);
										logger.info(altBolumStr + " aylikPuantajOlusturuluyor out " + PdksUtil.getCurrentTimeStampStr());
									}
								} catch (Exception e) {
									logger.error(seciliEkSaha3Id + " " + e);
									e.printStackTrace();
								}

								++adet;
								donemCPPerList = null;
							}
							List<AylikPuantaj> puantajList = null;
							if (donemKodu.longValue() >= islemDonemKodu.longValue()) {
								if (logYaz)
									logger.info(altBolumStr + " [ " + donemPerList.size() + " ] in " + PdksUtil.getCurrentTimeStampStr());
								if (kayitAdet > 0 && gelecekTarih == false) {
									String idStr = ortakIslemler.getEncodeStringByBase64(linkStr + "&seciliEkSaha4Id=" + altBolumId);
									fazlaMesaiHesaplaHome.sayfaFazlaMesaiGuncelle(idStr, loginUser);
									puantajList = fazlaMesaiHesaplaHome.getAylikPuantajList();

								}
								if (puantajList != null && !puantajList.isEmpty()) {
									session.flush();
									if (logYaz)
										logger.info(altBolumStr + " [ " + donemPerList.size() + " ] out " + PdksUtil.getCurrentTimeStampStr());
								}
							}
							donemPerList = null;
						}
						continue;
					}
				}

				if (loginUser.isAdmin() || gelecekTarih) {
					boolean devam = kayitAdet > 0;
					int adet = 0;
					while (devam && adet < 2) {
						session.clear();
						List<Personel> donemCPPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(denklestirmeAy, donemPerList, session);
						try {
							devam = donemCPPerList != null && kayitAdet != donemCPPerList.size();
							if (devam && donemKodu.longValue() > denklestirmeAy.getDonem()) {
								logger.info(str + " aylikPuantajOlusturuluyor in " + PdksUtil.getCurrentTimeStampStr());
								String idStr = ortakIslemler.getEncodeStringByBase64(linkStr);
								vardiyaGunHome.sayfaCalismaPlanOlustur(idStr, loginUser);
								logger.info(str + " aylikPuantajOlusturuluyor out " + PdksUtil.getCurrentTimeStampStr());
							}
						} catch (Exception e) {
							logger.error(seciliEkSaha3Id + " " + e);
							e.printStackTrace();
						}
						++adet;
						donemCPPerList = null;
					}

				}

				List<AylikPuantaj> puantajList = null;
				if (donemKodu.longValue() >= islemDonemKodu.longValue()) {
					if (logYaz)
						logger.info(str + " [ " + donemPerList.size() + " ] in " + PdksUtil.getCurrentTimeStampStr());
					if (kayitAdet > 0 && gelecekTarih == false) {
						String idStr = ortakIslemler.getEncodeStringByBase64(linkStr);
						fazlaMesaiHesaplaHome.sayfaFazlaMesaiGuncelle(idStr, loginUser);
						puantajList = fazlaMesaiHesaplaHome.getAylikPuantajList();

					}
					if (puantajList != null && !puantajList.isEmpty()) {
						session.flush();
						if (logYaz)
							logger.info(str + (puantajList != null ? " [ " + puantajList.size() + " ]" : "") + " out " + PdksUtil.getCurrentTimeStampStr());
					}
				}
				donemPerList = null;

			} catch (Exception e) {
				logger.error(seciliEkSaha3Id + " " + e);
				e.printStackTrace();
				hataYok = false;
			}
			if (hataYok == false)
				break;

		}
		if (authenticatedUser != null)
			logger.info(baslik + " OK " + PdksUtil.getCurrentTimeStampStr());

		return hataYok;
	}

	/**
	 * @param aylikPuantaj
	 * @return
	 */
	@Transactional
	public String saveLastParameter(AylikPuantaj aylikPuantaj) {
		Map<String, String> map1 = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
		PersonelDenklestirme personelDenklestirme = aylikPuantaj.getPersonelDenklestirme();
		String adres = map1.containsKey("host") ? map1.get("host") : "";
		Personel personel = aylikPuantaj.getPdksPersonel();
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("yil", "" + yil);
		lastMap.put("ay", "" + ay);
		if (departmanId != null)
			lastMap.put("departmanId", "" + departmanId);
		if (sirketId != null)
			lastMap.put("sirketId", "" + sirketId);
		if (tesisId != null)
			lastMap.put("tesisId", "" + tesisId);
		else if (tesisList != null && !tesisList.isEmpty() && personel.getTesis() != null)
			lastMap.put("tesisId", "" + personel.getTesis().getId());
		if (personel.getEkSaha3() != null)
			lastMap.put("bolumId", "" + personel.getEkSaha3().getId());
		if (ekSaha4Tanim != null)
			lastMap.put("altBolumId", "" + (personel.getEkSaha4() != null ? personel.getEkSaha4().getId() : "-1"));
		lastMap.put("sicilNo", personel.getPdksSicilNo());
		String sayfa = MenuItemConstant.fazlaMesaiHesapla;
		CalismaModeliAy cma = personelDenklestirme.getCalismaModeliAy();
		if (personelDenklestirme.getDurum().equals(Boolean.TRUE) || personelDenklestirme.isOnaylandi() || (cma == null || cma.isHareketKaydiVardiyaBulsunmu())) {
			lastMap.put("sayfaURL", FazlaMesaiHesaplaHome.sayfaURL);
			lastMap.put("calistir", Boolean.TRUE);
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

	private void aylikPuantajListClear() {
		if (personelDenklestirmeList != null)
			personelDenklestirmeList.clear();
		else
			personelDenklestirmeList = ortakIslemler.getSelectItemList("aylikPuantaj", authenticatedUser);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		try {
			if (session == null)
				session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
			setPdksUser(authenticatedUser);
			ortakIslemler.setUserMenuItemTime(session, sayfaURL);
			aylikPuantajListClear();
			adminRoleDurum();
			login = true;
			fazlaMesaiHesaplaMenuAdi = "";
			String str = ortakIslemler.getParameterKey("bordroVeriOlustur");
			boolean ayniSayfa = getPdksUser().getCalistigiSayfa() != null && getPdksUser().getCalistigiSayfa().equals(sayfaURL);
			if (!ayniSayfa)
				getPdksUser().setCalistigiSayfa(sayfaURL);

			Calendar cal = Calendar.getInstance();
			maxYil = cal.get(Calendar.YEAR);
			ortakIslemler.gunCikar(cal, 2);
			modelGoster = Boolean.FALSE;
			ay = cal.get(Calendar.MONTH) + 1;
			yil = cal.get(Calendar.YEAR);
			fillEkSahaTanim();
			try {
				minYil = PdksUtil.getSistemBaslangicYili();
				if (str.length() > 5) {
					int yil = Integer.parseInt(str.substring(0, 4));
					if (yil > minYil && maxYil >= yil)
						minYil = yil;
				}

			} catch (Exception e) {

			}
			if (baslikMap == null)
				baslikMap = new TreeMap<String, Tanim>();

			sicilNo = "";

			setDepartmanId(null);
			setDepartman(null);
			setInstance(new DenklestirmeAy());
			setPersonelDenklestirmeList(new ArrayList<AylikPuantaj>());

			durumERP = Boolean.FALSE;
			personelERP = Boolean.FALSE;
			onaylanmayanDurum = null;
			sirket = null;
			sirketId = null;
			sirketler = null;
			fazlaMesaiHesaplaDurum = getPdksUser().isAdmin() || getPdksUser().isSistemYoneticisi() || getPdksUser().isAnahtarKullanici();
			if (!fazlaMesaiHesaplaDurum)
				fazlaMesaiHesaplaDurum = ortakIslemler.getParameterKey("sirketFazlaMesaiGuncelleme").equals("1");

			LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter(sayfaURL, session);
			String yilStr = null;
			String ayStr = null;
			String sirketIdStr = null;
			String tesisIdStr = null;
			String departmanIdStr = null;
			String hataliVeriGetirStr = null;
			String eksikCalisanVeriGetirStr = null;
			String sadeceHataliGetirStr = null;
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			linkAdresKey = (String) req.getParameter("linkAdresKey");
			departmanId = null;
			if (veriLastMap != null) {
				if (veriLastMap.containsKey("yil"))
					yilStr = (String) veriLastMap.get("yil");
				if (veriLastMap.containsKey("ay"))
					ayStr = (String) veriLastMap.get("ay");
				if (veriLastMap.containsKey("sirketId"))
					sirketIdStr = (String) veriLastMap.get("sirketId");
				if (veriLastMap.containsKey("tesisId"))
					tesisIdStr = (String) veriLastMap.get("tesisId");
				if (veriLastMap.containsKey("departmanId"))
					departmanIdStr = (String) veriLastMap.get("departmanId");
				if (veriLastMap.containsKey("sicilNo"))
					sicilNo = (String) veriLastMap.get("sicilNo");
				if (veriLastMap.containsKey("hataliVeriGetir"))
					hataliVeriGetirStr = (String) veriLastMap.get("hataliVeriGetir");
				if (veriLastMap.containsKey("eksikCalisanVeriGetir"))
					eksikCalisanVeriGetirStr = (String) veriLastMap.get("eksikCalisanVeriGetir");
				if (veriLastMap.containsKey("sadeceHataliGetir"))
					sadeceHataliGetirStr = (String) veriLastMap.get("sadeceHataliGetir");

				if (yilStr != null && ayStr != null && sirketIdStr != null) {
					yil = Integer.parseInt(yilStr);
					ay = Integer.parseInt(ayStr);
					sirketId = Long.parseLong(sirketIdStr);
					if (tesisIdStr != null)
						tesisId = Long.parseLong(tesisIdStr);
					if (sirketId != null) {

						sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);

						if (sirket != null)
							departmanId = sirket.getDepartman().getId();
					}
					if (departmanId == null && departmanIdStr != null)
						departmanId = Long.parseLong(departmanIdStr);
					yilDegisti();

				}
			}
			if (!getPdksUser().isAdmin()) {
				if (departmanId == null)
					setDepartmanId(getPdksUser().getDepartman().getId());
				if (getPdksUser().isIK())
					yilDegisti();
			}
			if (aylar == null)
				yilDegisti();

			// return ortakIslemler.yetkiIKAdmin(Boolean.FALSE);

			if (hataliVeriGetirStr != null)
				hataliVeriGetir = new Boolean(hataliVeriGetirStr);
			if (eksikCalisanVeriGetirStr != null)
				eksikCalisanVeriGetir = new Boolean(eksikCalisanVeriGetirStr);
			if (sadeceHataliGetirStr != null)
				sadeceHataliGetir = new Boolean(sadeceHataliGetirStr);

			bordroAdres = null;
			if (linkAdresKey != null)
				fillPersonelDenklestirmeList();

		} catch (Exception exx) {
			exx.printStackTrace();
		}

		return "";

	}

	public String personelNoDegisti() throws Exception {

		personelDenklestirmeList.clear();
		if (PdksUtil.hasStringValue(sicilNo))
			fillPersonelDenklestirmeList();
		return "";
	}

	/**
	 * 
	 */
	@Transactional
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		lastMap.put("yil", "" + yil);
		lastMap.put("ay", "" + ay);
		if (departmanId != null)
			lastMap.put("departmanId", "" + departmanId);
		if (sirketId != null)
			lastMap.put("sirketId", "" + sirketId);
		if (tesisId != null)
			lastMap.put("tesisId", "" + tesisId);
		if (PdksUtil.hasStringValue(sicilNo))
			lastMap.put("sicilNo", sicilNo.trim());
		if (denklestirmeAy.getDurum()) {
			if (hataliVeriGetir != null)
				lastMap.put("hataliVeriGetir", "" + hataliVeriGetir);
			if (eksikCalisanVeriGetir != null)
				lastMap.put("eksikCalisanVeriGetir", "" + eksikCalisanVeriGetir);
			if (sadeceHataliGetir != null)
				lastMap.put("sadeceHataliGetir", "" + sadeceHataliGetir);

		}
		lastMap.put("sayfaURL", sayfaURL);
		try {
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {
		}

	}

	public void filDepartmanList() {
		denklestirmeAy = ortakIslemler.getSQLDenklestirmeAy(yil, ay, session);
		// List<SelectItem> departmanListe = ortakIslemler.getSelectItemList("departman", getPdksUser());
		List<SelectItem> departmanListe = denklestirmeAy != null ? fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, true, session) : new ArrayList<SelectItem>();
		departman = null;

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
			if (departmanId != null)
				departman = (Departman) pdksEntityController.getSQLParamByFieldObject(Departman.TABLE_NAME, Departman.COLUMN_NAME_ID, departmanId, Departman.class, session);

		} else
			departmanId = null;

		setDepartmanList(departmanListe);
	}

	public void fillTesisList() {
		personelDenklestirmeList.clear();
		List<SelectItem> selectItems = null;
		Long onceki = null;
		if (sirketId != null) {
			onceki = tesisId;
			sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);
			if (sirket != null) {
				if (sirket.isTesisDurumu()) {
					DenklestirmeAy denklestirmeAy = ortakIslemler.getSQLDenklestirmeAy(yil, ay, session);
					denklestirmeAyDurum = fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy);
					selectItems = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, getPdksUser().isAdmin() == false, session);
					if (fazlaMesaiHesaplaDurum && denklestirmeAyDurum) {
						if (selectItems.isEmpty())
							selectItems = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, false, session);

					}

					if (!selectItems.isEmpty()) {
						if (selectItems.size() == 1)
							onceki = (Long) selectItems.get(0).getValue();
						else {
							onceki = null;
							for (SelectItem selectItem : selectItems) {
								if (selectItem.getValue().equals(tesisId))
									onceki = tesisId;
							}
						}
					}
				} else
					onceki = null;
			}

		} else
			tesisId = null;
		setTesisId(onceki);
		setTesisList(selectItems);
	}

	public void yilDegisti() {
		if (yil > 0) {
			aylar = ortakIslemler.getSelectItemList("ay", getPdksUser());
			ay = fazlaMesaiOrtakIslemler.aylariDoldurDurum(yil, ay, aylar, fazlaMesaiHesaplaDurum == false, session);
			filDepartmanList();
			if (!aylar.isEmpty())
				fillSirketList();
		}

	}

	public void fillSirketList() {
		personelDenklestirmeList.clear();

		if (departmanId != null)
			departman = (Departman) pdksEntityController.getSQLParamByFieldObject(Departman.TABLE_NAME, Departman.COLUMN_NAME_ID, departmanId, Departman.class, session);

		else
			departman = null;

		if (ay <= 0)
			ay = (Integer) aylar.get(aylar.size() - 1).getValue();

		denklestirmeAyDurum = fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy);
		List<SelectItem> sirketList = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, getPdksUser().isAdmin() == false, session);
		if (fazlaMesaiHesaplaDurum && denklestirmeAyDurum) {
			if (sirketList.isEmpty())
				sirketList = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, denklestirmeAy != null ? new AylikPuantaj(denklestirmeAy) : null, false, session);

		}
		Long onceki = null;
		sirket = null;
		if (!sirketList.isEmpty()) {
			onceki = sirketId;
			if (sirketList.size() == 1) {
				sirketId = (Long) sirketList.get(0).getValue();
			} else if (sirketId != null) {
				sirketId = null;
				for (SelectItem selectItem : sirketList) {
					if (selectItem.getValue().equals(onceki))
						sirketId = onceki;

				}
			}

		} else
			sirketId = onceki;
		setSirketler(sirketList);

		if (sirketId != null)
			fillTesisList();
		else {
			tesisId = null;
			tesisList = null;
		}

		setPersonelDenklestirmeList(new ArrayList<AylikPuantaj>());

	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	/**
	 * @param kod
	 * @return
	 */
	public String getBaslikAciklama(String kod) {
		String aciklama = "";
		if (baslikMap != null && kod != null) {
			if (kod.equals(COL_SIRKET))
				logger.debug("");
			if (baslikMap.containsKey(kod)) {
				aciklama = baslikMap.get(kod).getAciklama();
			}
		}
		return aciklama;
	}

	public String fillPersonelDenklestirmeList() throws Exception {
		fazlaMesaiHesaplaMenuAdi = "";
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.clear();
		Calendar cal = Calendar.getInstance();
		bordroAdres = null;
		aksamGun = Boolean.FALSE;
		aksamSaat = Boolean.FALSE;
		calismaModeliDurum = Boolean.FALSE;
		haftaCalisma = Boolean.FALSE;
		resmiTatilDurum = Boolean.FALSE;
		maasKesintiGoster = Boolean.FALSE;
		artikGunDurum = Boolean.FALSE;
		resmiTatilGunDurum = Boolean.FALSE;
		normalGunSaatDurum = Boolean.FALSE;
		haftaTatilSaatDurum = Boolean.FALSE;
		resmiTatilSaatDurum = Boolean.FALSE;
		izinSaatDurum = Boolean.FALSE;
		if (sadeceHataliGetir)
			eksikCalisanVeriGetir = sadeceHataliGetir;

		ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
		personelDenklestirmeList.clear();
		denklestirmeAy = ortakIslemler.getSQLDenklestirmeAy(yil, ay, session);
		denklestirmeAyDurum = fazlaMesaiOrtakIslemler.getDurum(denklestirmeAy);
		if (denklestirmeAyDurum.equals(Boolean.FALSE)) {
			eksikCalisanVeriGetir = null;
			hataliVeriGetir = null;
		}
		Date tarih = PdksUtil.getDateFromString((yil * 100 + ay) + "01");
		ilkGun = ortakIslemler.tariheGunEkleCikar(cal, tarih, -1);
		sonGun = ortakIslemler.tariheAyEkleCikar(cal, tarih, 1);
		basGun = null;
		bitGun = null;

		durumERP = Boolean.FALSE;
		onaylanmayanDurum = null;
		personelERP = Boolean.FALSE;
		boolean tekSicilGiris = PdksUtil.hasStringValue(sicilNo);
		personelDenklestirmeList.clear();

		baslikMap.clear();
		if (denklestirmeAy != null) {
			saveLastParameter();
			try {
				String str = ortakIslemler.getParameterKey("bordroVeriOlustur");
				if (yil * 100 + ay >= Integer.parseInt(str)) {
					AramaSecenekleri as = new AramaSecenekleri();
					as.setSicilNo(sicilNo);
					as.setSirket(sirket);
					as.setTesisId(tesisId);
					as.setLoginUser(getPdksUser());
					List<Personel> donemPerList = new ArrayList<Personel>();
					try {
						if (denklestirmeAyDurum == false)
							denklestirmeAyDurum = getPdksUser().isAdmin() == false && getPdksUser().isIK() == false;
						personelDenklestirmeList = fazlaMesaiOrtakIslemler.getBordoDenklestirmeList(denklestirmeAy, as, tekSicilGiris == false && (denklestirmeAyDurum == false || (hataliVeriGetir != null && hataliVeriGetir)), tekSicilGiris == false
								&& (denklestirmeAyDurum == false || (eksikCalisanVeriGetir != null && eksikCalisanVeriGetir)), session);
						if (personelDenklestirmeList != null) {
							if (tekSicilGiris == false && sadeceHataliGetir) {
								for (Iterator iterator = personelDenklestirmeList.iterator(); iterator.hasNext();) {
									AylikPuantaj ap = (AylikPuantaj) iterator.next();
									PersonelDenklestirme pd = ap.getPersonelDenklestirme();
									if (pd.getDurum() && ap.getEksikCalismaSure() == null)
										iterator.remove();
								}
							}
							if (!personelDenklestirmeList.isEmpty()) {
								List<Tanim> bordroAlanlari = ortakIslemler.getTanimList(Tanim.TIPI_BORDRDO_ALANLARI, session);
								if (bordroAlanlari.isEmpty()) {
									boolean kimlikNoGoster = false;
									String kartNoAciklama = ortakIslemler.getParameterKey("kartNoAciklama");
									Boolean kartNoAciklamaGoster = null;
									if (PdksUtil.hasStringValue(kartNoAciklama))
										kartNoAciklamaGoster = false;
									for (AylikPuantaj ap : personelDenklestirmeList) {
										Personel personel = ap.getPdksPersonel();
										PersonelKGS personelKGS = personel.getPersonelKGS();
										if (personelKGS != null) {
											if (kartNoAciklamaGoster != null && kartNoAciklamaGoster.booleanValue() == false) {
												kartNoAciklamaGoster = PdksUtil.hasStringValue(personelKGS.getKartNo());
												if (kartNoAciklamaGoster && kimlikNoGoster)
													break;
											}

											if (!kimlikNoGoster) {
												kimlikNoGoster = PdksUtil.hasStringValue(personelKGS.getKimlikNo());
												if (kimlikNoGoster && (kartNoAciklamaGoster == null || kartNoAciklamaGoster))
													break;
											}
										}
									}
									if (kartNoAciklamaGoster == null)
										kartNoAciklamaGoster = false;
									bordroBilgiAciklamaOlustur(kimlikNoGoster, kartNoAciklama, kartNoAciklamaGoster, bordroAlanlari);
								}
								baslikMap.clear();
								for (Tanim tanim : bordroAlanlari)
									if (tanim.getDurum())
										baslikMap.put(tanim.getKodu(), tanim);
								boolean saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
								boolean haftaTatilBaslik = PdksUtil.hasStringValue(getBaslikAciklama(COL_HAFTA_TATIL_MESAI));
								boolean aksamGunBaslik = PdksUtil.hasStringValue(getBaslikAciklama(COL_AKSAM_GUN_MESAI));
								boolean aksamSaatBaslik = PdksUtil.hasStringValue(getBaslikAciklama(COL_AKSAM_SAAT_MESAI));
								boolean eksikCalismaBaslik = PdksUtil.hasStringValue(getBaslikAciklama(COL_EKSIK_CALISMA));
								boolean devamlikDurumBaslik = PdksUtil.hasStringValue(getBaslikAciklama(COL_DEVAMLILIK_PRIMI));
								Date bugun = ortakIslemler.getBugun();
								for (AylikPuantaj ap : personelDenklestirmeList) {
									PersonelDenklestirme pd = ap.getPersonelDenklestirme();
									donemPerList.add(pd.getPdksPersonel());
									PersonelDenklestirmeBordro personelDenklestirmeBordro = ap.getDenklestirmeBordro();
									if (personelDenklestirmeBordro == null) {
										personelDenklestirmeBordro = new PersonelDenklestirmeBordro();
										personelDenklestirmeBordro.setDetayMap(new HashMap<BordroDetayTipi, PersonelDenklestirmeBordroDetay>());
										ap.setDenklestirmeBordro(personelDenklestirmeBordro);
									}
									if (pd.isOnaylandi() && pd.getDurum().booleanValue() == false && ap.getVardiyalar() != null) {
										List<VardiyaGun> vardiyalar = ap.getVardiyalar();
										int adet = vardiyalar.size();
										for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
											VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
											if (vardiyaGun.getVardiya() != null && vardiyaGun.getVardiya().isCalisma()) {
												Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
												if (adet > 1 && islemVardiya.getVardiyaTelorans2BitZaman().after(bugun))
													iterator.remove();
											}
											if (vardiyalar.isEmpty())
												ap.setVardiyalar(null);
										}

									}
									if (saatlikCalismaVar) {
										if (!normalGunSaatDurum)
											normalGunSaatDurum = personelDenklestirmeBordro.getSaatNormal() != null && personelDenklestirmeBordro.getSaatNormal().doubleValue() > 0.0d;
										if (!haftaTatilSaatDurum)
											haftaTatilSaatDurum = personelDenklestirmeBordro.getSaatHaftaTatil() != null && personelDenklestirmeBordro.getSaatHaftaTatil().doubleValue() > 0.0d;
										if (!resmiTatilSaatDurum)
											resmiTatilSaatDurum = personelDenklestirmeBordro.getSaatResmiTatil() != null && personelDenklestirmeBordro.getSaatResmiTatil().doubleValue() > 0.0d;
										if (!izinSaatDurum)
											izinSaatDurum = personelDenklestirmeBordro.getSaatIzin() != null && personelDenklestirmeBordro.getSaatIzin().doubleValue() > 0.0d;
									}
									if (!artikGunDurum)
										artikGunDurum = personelDenklestirmeBordro.getArtikAdet() != null && personelDenklestirmeBordro.getArtikAdet().doubleValue() > 0.0d;
									if (!resmiTatilGunDurum)
										resmiTatilGunDurum = personelDenklestirmeBordro.getResmiTatilAdet() != null && personelDenklestirmeBordro.getResmiTatilAdet().doubleValue() > 0.0d;
									if (!artikGunDurum)
										artikGunDurum = personelDenklestirmeBordro.getArtikAdet() != null && personelDenklestirmeBordro.getArtikAdet().doubleValue() > 0.0d;
									if (!haftaCalisma && haftaTatilBaslik)
										haftaCalisma = pd.getHaftaCalismaSuresi() != null && pd.getHaftaCalismaSuresi().doubleValue() > 0.0d;
									if (!resmiTatilDurum)
										resmiTatilDurum = pd.getResmiTatilSure() != null && pd.getResmiTatilSure().doubleValue() > 0.0d;
									if (!aksamGun && aksamGunBaslik)
										setAksamGun(pd.getAksamVardiyaSayisi() != null && pd.getAksamVardiyaSayisi().doubleValue() > 0.0d);
									if (!aksamSaat && aksamSaatBaslik)
										setAksamSaat(pd.getAksamVardiyaSaatSayisi() != null && pd.getAksamVardiyaSaatSayisi().doubleValue() > 0.0d);
									if (!maasKesintiGoster && eksikCalismaBaslik)
										setMaasKesintiGoster(pd.getEksikCalismaSure() != null && pd.getEksikCalismaSure().doubleValue() > 0.0d);

									if (!normalGunSaatDurum)
										normalGunSaatDurum = personelDenklestirmeBordro.getSaatNormal() != null && personelDenklestirmeBordro.getSaatNormal().doubleValue() > 0.0d;
									if (!haftaTatilSaatDurum)
										haftaTatilSaatDurum = personelDenklestirmeBordro.getSaatHaftaTatil() != null && personelDenklestirmeBordro.getSaatHaftaTatil().doubleValue() > 0.0d;
									if (!resmiTatilSaatDurum)
										resmiTatilSaatDurum = personelDenklestirmeBordro.getSaatResmiTatil() != null && personelDenklestirmeBordro.getSaatResmiTatil().doubleValue() > 0.0d;
									if (!izinSaatDurum)
										izinSaatDurum = personelDenklestirmeBordro.getSaatIzin() != null && personelDenklestirmeBordro.getSaatIzin().doubleValue() > 0.0d;
									if (!devamlikDurumBaslik)
										devamlikDurumBaslik = personelDenklestirmeBordro.getDevamlilikPrimi() != null && personelDenklestirmeBordro.getDevamlilikPrimi();

								}
								if (baslikMap.containsKey(COL_CALISMA_MODELI)) {
									personelDinamikAlanlar = PdksUtil.getAktifList(ortakIslemler.getSQLTanimListByTipKodu(Tanim.TIPI_PERSONEL_DINAMIK_TANIM, null, session));
									personelDinamikAlanMap = ortakIslemler.getPersonelDinamikAlanMap(donemPerList, personelDinamikAlanlar, session);
									TreeMap<Long, Tanim> tanimMap = new TreeMap<Long, Tanim>();
									personelDinamikAlanlar.clear();
									for (String key : personelDinamikAlanMap.keySet()) {
										PersonelDinamikAlan personelDinamikAlan = personelDinamikAlanMap.get(key);
										if (personelDinamikAlan.getId() != null) {
											Tanim alan = personelDinamikAlan.getAlan();
											if (baslikMap.containsKey(alan.getKodu()) && !tanimMap.containsKey(alan.getId()))
												tanimMap.put(alan.getId(), alan);

										}
									}
									if (!tanimMap.isEmpty()) {
										personelDinamikAlanlar.addAll(PdksUtil.sortTanimList(null, new ArrayList(tanimMap.values())));
									}
								} else {
									if (personelDinamikAlanlar == null)
										personelDinamikAlanlar = new ArrayList<Tanim>();
									else
										personelDinamikAlanlar.clear();
									if (personelDinamikAlanMap == null)
										personelDinamikAlanMap = new TreeMap<String, PersonelDinamikAlan>();
									else
										personelDinamikAlanMap.clear();
								}

								setDevamlikPrimGoster(devamlikDurumBaslik);
							}
						}
					} catch (Exception es) {
						ortakIslemler.setExceptionLog(null, es);
					}
					as = null;
				}
			} catch (Exception ex) {
				ortakIslemler.loggerErrorYaz(sayfaURL, ex);
				throw new Exception(ex);
			}

		}

		if (personelDenklestirmeList.isEmpty()) {
			if (authenticatedUser != null) {
				if (fazlaMesaiHesaplaDurum == false)
					PdksUtil.addMessageWarn("İlgili döneme ait fazla mesai bulunamadı!");
				else
					PdksUtil.addMessageWarn((tesisId != null ? ortakIslemler.tesisAciklama() : ortakIslemler.sirketAciklama()) + " Fazla Mesai Hesapla çalıştırın.");
			}
		}

		else
			fazlaMesaiHesaplaMenuAdi = ortakIslemler.getMenuAdi("fazlaMesaiHesapla");

		setInstance(denklestirmeAy);

		return "";
	}

	public String denklestirmeExcelAktar() {
		ByteArrayOutputStream baosDosya = null;
		try {
			baosDosya = denklestirmeExcelAktarDevam();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		if (baosDosya != null) {
			String dosyaAdi = "bordroVeri";
			if (sirket != null)
				dosyaAdi += "_" + sirket.getAd();
			if (tesisId != null) {

				Tanim tesis = (Tanim) pdksEntityController.getSQLParamByFieldObject(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, tesisId, Tanim.class, session);

				if (tesis != null)
					dosyaAdi += "_" + tesis.getAciklama();
			}
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi + PdksUtil.convertToDateString(basGun, "_MMMMM_yyyy") + ".xlsx");
		} else if (authenticatedUser != null)
			PdksUtil.addMessageAvailableWarn("Aktarılacak veri bulunamadı!");

		return "";
	}

	/**
	 * @param sira
	 * @param adi
	 * @param aciklama
	 * @return
	 */
	private Tanim getBordroAlani(int sira, String adi, String aciklama) {
		Tanim tanim = new Tanim();
		tanim.setKodu(adi);
		tanim.setAciklamatr(aciklama);
		tanim.setAciklamaen(aciklama);
		tanim.setErpKodu(PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(sira * 50), '0', 4));
		return tanim;

	}

	/**
	 * @return
	 * @throws IOException
	 */
	private ByteArrayOutputStream denklestirmeExcelAktarDevam() throws IOException {
		ByteArrayOutputStream baos = null;
		HashMap<String, Object> veriMap = new HashMap<String, Object>();
		veriMap.put("denklestirmeAy", denklestirmeAy);
		veriMap.put("personelDenklestirmeList", personelDenklestirmeList);
		if (ekSaha4Tanim != null)
			veriMap.put("ekSaha4Tanim", ekSaha4Tanim);
		if (personelDinamikAlanlar != null && !personelDinamikAlanlar.isEmpty())
			veriMap.put("personelDinamikAlanlar", personelDinamikAlanlar);
		if (personelDinamikAlanMap != null && !personelDinamikAlanMap.isEmpty())
			veriMap.put("personelDinamikAlanMap", personelDinamikAlanMap);
		try {
			baos = fazlaMesaiOrtakIslemler.denklestirmeExcelAktarDevam(veriMap, session);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return baos;
	}

	/**
	 * @param kimlikNoGoster
	 * @param kartNoAciklama
	 * @param kartNoAciklamaGoster
	 * @param bordroAlanlari
	 */
	@Transactional
	private void bordroBilgiAciklamaOlustur(boolean kimlikNoGoster, String kartNoAciklama, Boolean kartNoAciklamaGoster, List<Tanim> bordroAlanlari) {
		int sira = 0;
		Tanim ekSaha4Tanim = ortakIslemler.getEkSaha4(sirket, sirketId, session);
		bordroAlanlari.add(getBordroAlani(++sira, COL_SIRA, "Sıra"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_YIL, ortakIslemler.yilAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_AY, ortakIslemler.aylarAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_PERSONEL_NO, ortakIslemler.personelNoAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_AD_SOYAD, "Personel"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_SIRKET + "Kodu", ortakIslemler.sirketAciklama() + " Kodu"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_SIRKET, ortakIslemler.sirketAciklama()));
		if (ortakIslemler.getTesisDurumu()) {
			bordroAlanlari.add(getBordroAlani(++sira, COL_TESIS + "Kodu", ortakIslemler.tesisAciklama() + " Kodu"));
			bordroAlanlari.add(getBordroAlani(++sira, COL_TESIS, ortakIslemler.tesisAciklama()));
		}
		if (kartNoAciklamaGoster)
			bordroAlanlari.add(getBordroAlani(++sira, COL_KART_NO, kartNoAciklama));
		if (kimlikNoGoster)
			bordroAlanlari.add(getBordroAlani(++sira, COL_KIMLIK_NO, ortakIslemler.kimlikNoAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_BOLUM, bolumAciklama));
		if (ekSaha4Tanim != null)
			bordroAlanlari.add(getBordroAlani(++sira, COL_ALT_BOLUM, ekSaha4Tanim.getAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_NORMAL_GUN_ADET, "Normal Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_HAFTA_TATIL_ADET, "H.Tatil Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_RESMI_TATIL_ADET, "R.Tatil Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_ARTIK_ADET, "Artık Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_UCRETLI_IZIN, "Ücretli İzin Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_RAPORLU_IZIN, "Raporlu (Hasta)"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_UCRETSIZ_IZIN, "Ücretsiz İzin Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_YILLIK_IZIN, "Yıllık İzin Gün"));
		bordroAlanlari.add(getBordroAlani(++sira, COL_UCRETI_ODENEN_MESAI, "Ücreti Ödenen Mesai"));
		if (maasKesintiGoster)
			bordroAlanlari.add(getBordroAlani(++sira, COL_EKSIK_CALISMA, ortakIslemler.eksikCalismaAciklama()));
		bordroAlanlari.add(getBordroAlani(++sira, COL_RESMI_TATIL_MESAI, "Resmi Tatil Mesai"));
		if (haftaCalisma)
			bordroAlanlari.add(getBordroAlani(++sira, COL_HAFTA_TATIL_MESAI, "Hafta Tatil Mesai"));
		if (aksamSaat)
			bordroAlanlari.add(getBordroAlani(++sira, COL_AKSAM_SAAT_MESAI, "Gece Saat"));
		if (aksamGun)
			bordroAlanlari.add(getBordroAlani(++sira, COL_AKSAM_SAAT_MESAI, "Gece Adet"));
		Date islemTarihi = new Date();
		for (Tanim tanim : bordroAlanlari) {
			tanim.setTipi(Tanim.TIPI_BORDRDO_ALANLARI);
			tanim.setIslemYapan(getPdksUser());
			tanim.setIslemTarihi(islemTarihi);
			pdksEntityController.saveOrUpdate(session, entityManager, tanim);
		}
		session.flush();
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
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

	public Boolean getSecimDurum() {
		return secimDurum;
	}

	public void setSecimDurum(Boolean secimDurum) {
		this.secimDurum = secimDurum;
	}

	public Boolean getSureDurum() {
		return sureDurum;
	}

	public void setSureDurum(Boolean sureDurum) {
		this.sureDurum = sureDurum;
	}

	public Boolean getFazlaMesaiDurum() {

		return fazlaMesaiDurum;
	}

	public void setFazlaMesaiDurum(Boolean fazlaMesaiDurum) {
		this.fazlaMesaiDurum = fazlaMesaiDurum;
	}

	public Boolean getResmiTatilDurum() {
		return resmiTatilDurum;
	}

	public void setResmiTatilDurum(Boolean resmiTatilDurum) {
		this.resmiTatilDurum = resmiTatilDurum;
	}

	public Date getBasGun() {
		return basGun;
	}

	public void setBasGun(Date basGun) {
		this.basGun = basGun;
	}

	public Date getBitGun() {
		return bitGun;
	}

	public void setBitGun(Date bitGun) {
		this.bitGun = bitGun;
	}

	public Boolean getOnaylanmayanDurum() {
		return onaylanmayanDurum;
	}

	public void setOnaylanmayanDurum(Boolean onaylanmayanDurum) {
		this.onaylanmayanDurum = onaylanmayanDurum;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public List<SelectItem> getSirketler() {
		return sirketler;
	}

	public void setSirketler(List<SelectItem> sirketler) {
		this.sirketler = sirketler;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public List<SelectItem> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<SelectItem> departmanList) {
		this.departmanList = departmanList;
	}

	public Long getDepartmanId() {
		return departmanId;
	}

	public void setDepartmanId(Long departmanId) {
		this.departmanId = departmanId;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
	}

	public Dosya getFazlaMesaiDosya() {
		return fazlaMesaiDosya;
	}

	public void setFazlaMesaiDosya(Dosya fazlaMesaiDosya) {
		this.fazlaMesaiDosya = fazlaMesaiDosya;
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

	public Boolean getHaftaCalisma() {
		return haftaCalisma;
	}

	public void setHaftaCalisma(Boolean haftaCalisma) {
		this.haftaCalisma = haftaCalisma;
	}

	public Boolean getPersonelERP() {
		return personelERP;
	}

	public void setPersonelERP(Boolean personelERP) {
		this.personelERP = personelERP;
	}

	public Boolean getDurumERP() {
		return durumERP;
	}

	public void setDurumERP(Boolean durumERP) {
		this.durumERP = durumERP;
	}

	public Boolean getErpAktarimDurum() {
		return erpAktarimDurum;
	}

	public void setErpAktarimDurum(Boolean erpAktarimDurum) {
		this.erpAktarimDurum = erpAktarimDurum;
	}

	public Boolean getHaftaTatilDurum() {
		return haftaTatilDurum;
	}

	public void setHaftaTatilDurum(Boolean haftaTatilDurum) {
		this.haftaTatilDurum = haftaTatilDurum;
	}

	public Boolean getModelGoster() {
		return modelGoster;
	}

	public void setModelGoster(Boolean modelGoster) {
		this.modelGoster = modelGoster;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
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

	public TreeMap<Long, Personel> getIzinTipiPersonelMap() {
		return izinTipiPersonelMap;
	}

	public void setIzinTipiPersonelMap(TreeMap<Long, Personel> izinTipiPersonelMap) {
		this.izinTipiPersonelMap = izinTipiPersonelMap;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public int getMinYil() {
		return minYil;
	}

	public void setMinYil(int minYil) {
		this.minYil = minYil;
	}

	public List<AylikPuantaj> getPersonelDenklestirmeList() {
		return personelDenklestirmeList;
	}

	public void setPersonelDenklestirmeList(List<AylikPuantaj> personelDenklestirmeList) {
		this.personelDenklestirmeList = personelDenklestirmeList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public List<SelectItem> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<SelectItem> tesisList) {
		this.tesisList = tesisList;
	}

	public String getCOL_SIRA() {
		return COL_SIRA;
	}

	public void setCOL_SIRA(String cOL_SIRA) {
		COL_SIRA = cOL_SIRA;
	}

	public String getCOL_YIL() {
		return COL_YIL;
	}

	public void setCOL_YIL(String cOL_YIL) {
		COL_YIL = cOL_YIL;
	}

	public String getCOL_AY() {
		return COL_AY;
	}

	public void setCOL_AY(String cOL_AY) {
		COL_AY = cOL_AY;
	}

	public String getCOL_AY_ADI() {
		return COL_AY_ADI;
	}

	public void setCOL_AY_ADI(String cOL_AY_ADI) {
		COL_AY_ADI = cOL_AY_ADI;
	}

	public String getCOL_PERSONEL_NO() {
		return COL_PERSONEL_NO;
	}

	public void setCOL_PERSONEL_NO(String cOL_PERSONEL_NO) {
		COL_PERSONEL_NO = cOL_PERSONEL_NO;
	}

	public String getCOL_AD() {
		return COL_AD;
	}

	public void setCOL_AD(String cOL_AD) {
		COL_AD = cOL_AD;
	}

	public String getCOL_SOYAD() {
		return COL_SOYAD;
	}

	public void setCOL_SOYAD(String cOL_SOYAD) {
		COL_SOYAD = cOL_SOYAD;
	}

	public String getCOL_AD_SOYAD() {
		return COL_AD_SOYAD;
	}

	public void setCOL_AD_SOYAD(String cOL_AD_SOYAD) {
		COL_AD_SOYAD = cOL_AD_SOYAD;
	}

	public String getCOL_KART_NO() {
		return COL_KART_NO;
	}

	public void setCOL_KART_NO(String cOL_KART_NO) {
		COL_KART_NO = cOL_KART_NO;
	}

	public String getCOL_KIMLIK_NO() {
		return COL_KIMLIK_NO;
	}

	public void setCOL_KIMLIK_NO(String cOL_KIMLIK_NO) {
		COL_KIMLIK_NO = cOL_KIMLIK_NO;
	}

	public String getCOL_SIRKET() {
		return COL_SIRKET;
	}

	public void setCOL_SIRKET(String cOL_SIRKET) {
		COL_SIRKET = cOL_SIRKET;
	}

	public String getCOL_TESIS() {
		return COL_TESIS;
	}

	public void setCOL_TESIS(String cOL_TESIS) {
		COL_TESIS = cOL_TESIS;
	}

	public String getCOL_BOLUM() {
		return COL_BOLUM;
	}

	public void setCOL_BOLUM(String cOL_BOLUM) {
		COL_BOLUM = cOL_BOLUM;
	}

	public String getCOL_ALT_BOLUM() {
		return COL_ALT_BOLUM;
	}

	public void setCOL_ALT_BOLUM(String cOL_ALT_BOLUM) {
		COL_ALT_BOLUM = cOL_ALT_BOLUM;
	}

	public String getCOL_NORMAL_GUN_ADET() {
		return COL_NORMAL_GUN_ADET;
	}

	public void setCOL_NORMAL_GUN_ADET(String cOL_NORMAL_GUN_ADET) {
		COL_NORMAL_GUN_ADET = cOL_NORMAL_GUN_ADET;
	}

	public String getCOL_HAFTA_TATIL_ADET() {
		return COL_HAFTA_TATIL_ADET;
	}

	public void setCOL_HAFTA_TATIL_ADET(String cOL_HAFTA_TATIL_ADET) {
		COL_HAFTA_TATIL_ADET = cOL_HAFTA_TATIL_ADET;
	}

	public String getCOL_UCRETLI_IZIN() {
		return COL_UCRETLI_IZIN;
	}

	public void setCOL_UCRETLI_IZIN(String cOL_UCRETLI_IZIN) {
		COL_UCRETLI_IZIN = cOL_UCRETLI_IZIN;
	}

	public String getCOL_RAPORLU_IZIN() {
		return COL_RAPORLU_IZIN;
	}

	public void setCOL_RAPORLU_IZIN(String cOL_RAPORLU_IZIN) {
		COL_RAPORLU_IZIN = cOL_RAPORLU_IZIN;
	}

	public String getCOL_UCRETSIZ_IZIN() {
		return COL_UCRETSIZ_IZIN;
	}

	public void setCOL_UCRETSIZ_IZIN(String cOL_UCRETSIZ_IZIN) {
		COL_UCRETSIZ_IZIN = cOL_UCRETSIZ_IZIN;
	}

	public String getCOL_RESMI_TATIL_MESAI() {
		return COL_RESMI_TATIL_MESAI;
	}

	public void setCOL_RESMI_TATIL_MESAI(String cOL_RESMI_TATIL_MESAI) {
		COL_RESMI_TATIL_MESAI = cOL_RESMI_TATIL_MESAI;
	}

	public String getCOL_UCRETI_ODENEN_MESAI() {
		return COL_UCRETI_ODENEN_MESAI;
	}

	public void setCOL_UCRETI_ODENEN_MESAI(String cOL_UCRETI_ODENEN_MESAI) {
		COL_UCRETI_ODENEN_MESAI = cOL_UCRETI_ODENEN_MESAI;
	}

	public String getCOL_HAFTA_TATIL_MESAI() {
		return COL_HAFTA_TATIL_MESAI;
	}

	public void setCOL_HAFTA_TATIL_MESAI(String cOL_HAFTA_TATIL_MESAI) {
		COL_HAFTA_TATIL_MESAI = cOL_HAFTA_TATIL_MESAI;
	}

	public String getCOL_AKSAM_SAAT_MESAI() {
		return COL_AKSAM_SAAT_MESAI;
	}

	public void setCOL_AKSAM_SAAT_MESAI(String cOL_AKSAM_SAAT_MESAI) {
		COL_AKSAM_SAAT_MESAI = cOL_AKSAM_SAAT_MESAI;
	}

	public String getCOL_AKSAM_GUN_MESAI() {
		return COL_AKSAM_GUN_MESAI;
	}

	public void setCOL_AKSAM_GUN_MESAI(String cOL_AKSAM_GUN_MESAI) {
		COL_AKSAM_GUN_MESAI = cOL_AKSAM_GUN_MESAI;
	}

	public TreeMap<String, Tanim> getBaslikMap() {
		return baslikMap;
	}

	public void setBaslikMap(TreeMap<String, Tanim> baslikMap) {
		this.baslikMap = baslikMap;
	}

	public Boolean getMaasKesintiGoster() {
		return maasKesintiGoster;
	}

	public void setMaasKesintiGoster(Boolean maasKesintiGoster) {
		this.maasKesintiGoster = maasKesintiGoster;
	}

	public String getCOL_EKSIK_CALISMA() {
		return COL_EKSIK_CALISMA;
	}

	public void setCOL_EKSIK_CALISMA(String cOL_EKSIK_CALISMA) {
		COL_EKSIK_CALISMA = cOL_EKSIK_CALISMA;
	}

	public String getCOL_RESMI_TATIL_ADET() {
		return COL_RESMI_TATIL_ADET;
	}

	public void setCOL_RESMI_TATIL_ADET(String cOL_RESMI_TATIL_ADET) {
		COL_RESMI_TATIL_ADET = cOL_RESMI_TATIL_ADET;
	}

	public String getCOL_ARTIK_ADET() {
		return COL_ARTIK_ADET;
	}

	public void setCOL_ARTIK_ADET(String cOL_ARTIK_ADET) {
		COL_ARTIK_ADET = cOL_ARTIK_ADET;
	}

	public Boolean getArtikGunDurum() {
		return artikGunDurum;
	}

	public void setArtikGunDurum(Boolean artikGunDurum) {
		this.artikGunDurum = artikGunDurum;
	}

	public Boolean getResmiTatilGunDurum() {
		return resmiTatilGunDurum;
	}

	public void setResmiTatilGunDurum(Boolean resmiTatilGunDurum) {
		this.resmiTatilGunDurum = resmiTatilGunDurum;
	}

	public String getCOL_NORMAL_GUN_SAAT() {
		return COL_NORMAL_GUN_SAAT;
	}

	public void setCOL_NORMAL_GUN_SAAT(String cOL_NORMAL_GUN_SAAT) {
		COL_NORMAL_GUN_SAAT = cOL_NORMAL_GUN_SAAT;
	}

	public String getCOL_HAFTA_TATIL_SAAT() {
		return COL_HAFTA_TATIL_SAAT;
	}

	public void setCOL_HAFTA_TATIL_SAAT(String cOL_HAFTA_TATIL_SAAT) {
		COL_HAFTA_TATIL_SAAT = cOL_HAFTA_TATIL_SAAT;
	}

	public String getCOL_RESMI_TATIL_SAAT() {
		return COL_RESMI_TATIL_SAAT;
	}

	public void setCOL_RESMI_TATIL_SAAT(String cOL_RESMI_TATIL_SAAT) {
		COL_RESMI_TATIL_SAAT = cOL_RESMI_TATIL_SAAT;
	}

	public String getCOL_IZIN_SAAT() {
		return COL_IZIN_SAAT;
	}

	public void setCOL_IZIN_SAAT(String cOL_IZIN_SAAT) {
		COL_IZIN_SAAT = cOL_IZIN_SAAT;
	}

	public Boolean getNormalGunSaatDurum() {
		return normalGunSaatDurum;
	}

	public void setNormalGunSaatDurum(Boolean normalGunSaatDurum) {
		this.normalGunSaatDurum = normalGunSaatDurum;
	}

	public Boolean getHaftaTatilSaatDurum() {
		return haftaTatilSaatDurum;
	}

	public void setHaftaTatilSaatDurum(Boolean haftaTatilSaatDurum) {
		this.haftaTatilSaatDurum = haftaTatilSaatDurum;
	}

	public Boolean getResmiTatilSaatDurum() {
		return resmiTatilSaatDurum;
	}

	public void setResmiTatilSaatDurum(Boolean resmiTatilSaatDurum) {
		this.resmiTatilSaatDurum = resmiTatilSaatDurum;
	}

	public Boolean getIzinSaatDurum() {
		return izinSaatDurum;
	}

	public void setIzinSaatDurum(Boolean izinSaatDurum) {
		this.izinSaatDurum = izinSaatDurum;
	}

	/**
	 * @return the hataliVeriGetir
	 */
	public Boolean getHataliVeriGetir() {
		return hataliVeriGetir;
	}

	/**
	 * @param hataliVeriGetir
	 *            the hataliVeriGetir to set
	 */
	public void setHataliVeriGetir(Boolean hataliVeriGetir) {
		this.hataliVeriGetir = hataliVeriGetir;
	}

	/**
	 * @return the denklestirmeAy
	 */
	public DenklestirmeAy getDenklestirmeAy() {
		return denklestirmeAy;
	}

	/**
	 * @param denklestirmeAy
	 *            the denklestirmeAy to set
	 */
	public void setDenklestirmeAy(DenklestirmeAy denklestirmeAy) {
		this.denklestirmeAy = denklestirmeAy;
	}

	/**
	 * @return the cOL_CALISMA_MODELI
	 */
	public String getCOL_CALISMA_MODELI() {
		return COL_CALISMA_MODELI;
	}

	/**
	 * @param cOL_CALISMA_MODELI
	 *            the cOL_CALISMA_MODELI to set
	 */
	public void setCOL_CALISMA_MODELI(String cOL_CALISMA_MODELI) {
		COL_CALISMA_MODELI = cOL_CALISMA_MODELI;
	}

	/**
	 * @return the calismaModeliDurum
	 */
	public Boolean getCalismaModeliDurum() {
		return calismaModeliDurum;
	}

	/**
	 * @param calismaModeliDurum
	 *            the calismaModeliDurum to set
	 */
	public void setCalismaModeliDurum(Boolean calismaModeliDurum) {
		this.calismaModeliDurum = calismaModeliDurum;
	}

	public Tanim getEkSaha4Tanim() {
		return ekSaha4Tanim;
	}

	public void setEkSaha4Tanim(Tanim ekSaha4Tanim) {
		this.ekSaha4Tanim = ekSaha4Tanim;
	}

	/**
	 * @return the linkAdresKey
	 */
	public String getLinkAdresKey() {
		return linkAdresKey;
	}

	/**
	 * @param linkAdresKey
	 *            the linkAdresKey to set
	 */
	public void setLinkAdresKey(String linkAdresKey) {
		this.linkAdresKey = linkAdresKey;
	}

	public Boolean getEksikCalisanVeriGetir() {
		return eksikCalisanVeriGetir;
	}

	public void setEksikCalisanVeriGetir(Boolean eksikCalisanVeriGetir) {
		this.eksikCalisanVeriGetir = eksikCalisanVeriGetir;
	}

	/**
	 * @return the cOL_ISE_BASLAMA_TARIHI
	 */
	public String getCOL_ISE_BASLAMA_TARIHI() {
		return COL_ISE_BASLAMA_TARIHI;
	}

	/**
	 * @param cOL_ISE_BASLAMA_TARIHI
	 *            the cOL_ISE_BASLAMA_TARIHI to set
	 */
	public void setCOL_ISE_BASLAMA_TARIHI(String cOL_ISE_BASLAMA_TARIHI) {
		COL_ISE_BASLAMA_TARIHI = cOL_ISE_BASLAMA_TARIHI;
	}

	/**
	 * @return the cOL_SSK_CIKIS_TARIHI
	 */
	public String getCOL_SSK_CIKIS_TARIHI() {
		return COL_SSK_CIKIS_TARIHI;
	}

	/**
	 * @param cOL_SSK_CIKIS_TARIHI
	 *            the cOL_SSK_CIKIS_TARIHI to set
	 */
	public void setCOL_SSK_CIKIS_TARIHI(String cOL_SSK_CIKIS_TARIHI) {
		COL_SSK_CIKIS_TARIHI = cOL_SSK_CIKIS_TARIHI;
	}

	/**
	 * @return the sonGun
	 */
	public Date getSonGun() {
		return sonGun;
	}

	/**
	 * @param sonGun
	 *            the sonGun to set
	 */
	public void setSonGun(Date sonGun) {
		this.sonGun = sonGun;
	}

	/**
	 * @return the ilkGun
	 */
	public Date getIlkGun() {
		return ilkGun;
	}

	/**
	 * @param ilkGun
	 *            the ilkGun to set
	 */
	public void setIlkGun(Date ilkGun) {
		this.ilkGun = ilkGun;
	}

	public String getCOL_TOPLAM_ADET() {
		return COL_TOPLAM_ADET;
	}

	public void setCOL_TOPLAM_ADET(String cOL_TOPLAM_ADET) {
		COL_TOPLAM_ADET = cOL_TOPLAM_ADET;
	}

	public String getCOL_YILLIK_IZIN() {
		return COL_YILLIK_IZIN;
	}

	public void setCOL_YILLIK_IZIN(String cOL_YILLIK_IZIN) {
		COL_YILLIK_IZIN = cOL_YILLIK_IZIN;
	}

	/**
	 * @return the adminRole
	 */
	public Boolean getAdminRole() {
		return adminRole;
	}

	/**
	 * @param adminRole
	 *            the adminRole to set
	 */
	public void setAdminRole(Boolean adminRole) {
		this.adminRole = adminRole;
	}

	/**
	 * @return the ikRole
	 */
	public Boolean getIkRole() {
		return ikRole;
	}

	/**
	 * @param ikRole
	 *            the ikRole to set
	 */
	public void setIkRole(Boolean ikRole) {
		this.ikRole = ikRole;
	}

	public String getFazlaMesaiHesaplaMenuAdi() {
		return fazlaMesaiHesaplaMenuAdi;
	}

	public void setFazlaMesaiHesaplaMenuAdi(String fazlaMesaiHesaplaMenuAdi) {
		this.fazlaMesaiHesaplaMenuAdi = fazlaMesaiHesaplaMenuAdi;
	}

	public String getCOL_DEVAMLILIK_PRIMI() {
		return COL_DEVAMLILIK_PRIMI;
	}

	public void setCOL_DEVAMLILIK_PRIMI(String cOL_DEVAMLILIK_PRIMI) {
		COL_DEVAMLILIK_PRIMI = cOL_DEVAMLILIK_PRIMI;
	}

	public boolean booleanValue() {
		return devamlikPrimGoster.booleanValue();
	}

	public String toString() {
		return devamlikPrimGoster.toString();
	}

	public int hashCode() {
		return devamlikPrimGoster.hashCode();
	}

	public boolean equals(Object obj) {
		return devamlikPrimGoster.equals(obj);
	}

	public int compareTo(Boolean b) {
		return devamlikPrimGoster.compareTo(b);
	}

	public Boolean getDevamlikPrimGoster() {
		return devamlikPrimGoster;
	}

	public void setDevamlikPrimGoster(Boolean devamlikPrimGoster) {
		this.devamlikPrimGoster = devamlikPrimGoster;
	}

	public Boolean getDenklestirmeAyDurum() {
		return denklestirmeAyDurum;
	}

	public void setDenklestirmeAyDurum(Boolean denklestirmeAyDurum) {
		this.denklestirmeAyDurum = denklestirmeAyDurum;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		DenklestirmeBordroRaporuHome.sayfaURL = sayfaURL;
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

	public Boolean getFazlaMesaiHesaplaDurum() {
		return fazlaMesaiHesaplaDurum;
	}

	public void setFazlaMesaiHesaplaDurum(Boolean fazlaMesaiHesaplaDurum) {
		this.fazlaMesaiHesaplaDurum = fazlaMesaiHesaplaDurum;
	}

	public Boolean getSadeceHataliGetir() {
		return sadeceHataliGetir;
	}

	public void setSadeceHataliGetir(Boolean sadeceHataliGetir) {
		this.sadeceHataliGetir = sadeceHataliGetir;
	}

	public User getPdksUser() {
		if (pdksUser == null)
			pdksUser = authenticatedUser;
		return pdksUser;
	}

	public void setPdksUser(User pdksUser) {
		this.pdksUser = pdksUser;
	}

	public Boolean getLogin() {
		return login;
	}

	public void setLogin(Boolean login) {
		this.login = login;
	}
}
