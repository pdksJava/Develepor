package org.pdks.session;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelFazlaMesai;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.YemekIzin;
import org.pdks.enums.KatSayiPuantajTipi;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

@Name("personelFazlaMesaiHome")
public class PersonelFazlaMesaiHome extends EntityHome<PersonelFazlaMesai> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6296894631826661965L;
	static Logger logger = Logger.getLogger(PersonelFazlaMesaiHome.class);

	public static String sayfaURL = "personelFazlaMesai";

	@RequestParameter
	Long fazlaMesaiId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	List<User> userList;
	@In(required = false, create = true)
	HashMap parameterMap;
	@In(required = false, create = true)
	String linkAdres;
	@In(required = false, create = true)
	VardiyaGun fazlaMesaiVardiyaGun;
	@In(required = false)
	FacesMessages facesMessages;

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	private List<Tanim> fazlaMesaiList = new ArrayList<Tanim>(), bolumList, onaylamamaNedeniList = new ArrayList<Tanim>();
	private VardiyaGun vardiyaGun;
	private List<Sirket> sirketList = new ArrayList<Sirket>();
	private Sirket sirket;
	private Tanim seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4;
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private KapiView manuelGiris = null, manuelCikis = null;
	private Tatil tatil, tatilMesai;
	private DenklestirmeAy pdksDenklestirmeAy = null;
	private String donusAdres = "";
	private TreeMap<String, Tatil> tatilMap;
	private Departman departman;
	private HareketKGS seciliHareket;
	private Sirket pdksSirket;
	private int yuvarmaTipi = 1;

	private String islemTipi, bolumAciklama;
	private Date date, mesaiSaati;
	private Tanim fazlaMesaiSistemOnayDurum;
	private User sistemAdminUser;
	private List<SelectItem> departmanList, pdksSirketList, bolumDepartmanlari;
	private Long departmanId, sirketId, seciliEkSaha3Id, seciliEkSaha4Id;
	private boolean denklestirmeAyDurum = Boolean.FALSE, adminRole, ikRole, fazlaMesaiGirisDurum = false;
	private AramaSecenekleri aramaSecenekleri = null;
	private Session session;

	@Override
	public Object getId() {
		if (fazlaMesaiId == null) {
			return super.getId();
		} else {
			return fazlaMesaiId;
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
	private void sessionClear() {
		session.clear();
	}

	/**
	 * @param object
	 */
	@Transactional
	private void saveOrUpdate(Object object) {
		if (object != null)
			pdksEntityController.saveOrUpdate(session, entityManager, object);
	}

	/**
	 * 
	 */
	@Transactional
	private void sessionFlush() {
		session.flush();
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	private void fillEkSahaTanim() {
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
		if (aramaSecenekleri.getSirketIdList().size() == 1) {
			aramaSecenekleri.setSirketId((Long) aramaSecenekleri.getSirketIdList().get(0).getValue());
			ortakIslemler.getTesisList(aramaSecenekleri.getTesisList(), null, aramaSecenekleri.getSirketId(), true, session);
			if (aramaSecenekleri.getTesisList() != null && aramaSecenekleri.getTesisList().size() == 1)
				aramaSecenekleri.setTesisId((Long) aramaSecenekleri.getTesisList().get(0).getValue());
		}
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() throws Exception {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		fazlaMesaiGirisDurum = false;
		adminRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIKAdmin();
		ikRole = authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || (PdksUtil.isSistemDestekVar() && authenticatedUser.isIK());
		if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);

		aramaSecenekleri.setSessionClear(Boolean.FALSE);
		aramaSecenekleri.setStajyerOlmayanSirket(Boolean.TRUE);
		boolean ayniSayfa = authenticatedUser.getCalistigiSayfa() != null && authenticatedUser.getCalistigiSayfa().equals(sayfaURL);
		if (!ayniSayfa)
			authenticatedUser.setCalistigiSayfa(sayfaURL);
		try {
			fillEkSahaTanim();
			pdksDenklestirmeAy = null;
			tatil = null;
			setHareketList(new ArrayList<HareketKGS>());
			PersonelFazlaMesai mesai = new PersonelFazlaMesai();
			HareketKGS hareket = new HareketKGS();
			hareket.setPersonel(new PersonelView());
			hareket.setKapiView(new KapiView());
			hareket.setIslem(new PersonelHareketIslem());
			mesai.setHareketId(hareket.getId());
			// mesai.setHareket(hareket);
			double fazlaMesaiSaati = 0;
			if (fazlaMesaiVardiyaGun != null) {
				if (hareket.getFazlaMesai() != null)
					fazlaMesaiSaati = PdksUtil.setSureDoubleTypeRounded(hareket.getFazlaMesai(), fazlaMesaiVardiyaGun.getFazlaMesaiYuvarla());
				hareket.setVardiyaGun(fazlaMesaiVardiyaGun);
			}
			mesai.setVardiyaGun(hareket.getVardiyaGun());

			mesai.setFazlaMesaiSaati(fazlaMesaiSaati);
			hareket.setPersonelFazlaMesai(mesai);
			setDate(new Date());
			setInstance(new PersonelFazlaMesai());
			setSirket(null);
			sirketId = null;
			setDepartman(authenticatedUser.getDepartman());
			departmanId = departman != null ? departman.getId() : null;

			if (fazlaMesaiVardiyaGun != null) {
				setSirket(authenticatedUser.getPdksPersonel().getSirket());
				sirketId = sirket != null && sirket.getDepartman().getId().equals(departmanId) ? sirket.getId() : null;

			}

			aramaSecenekleri.setSirketId(sirketId);
			if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin())
				filDepartmanList();
			if (departmanList != null && departmanList.size() == 1)
				setDepartmanId((Long) departmanList.get(0).getValue());
			if (departmanId != null)
				fillSirketList();

			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			Long perKGSId = null;
			String dateStr = null;
			String planKey = (String) req.getParameter("planKey");
			LinkedHashMap<String, Object> veriLastMap = ortakIslemler.getLastParameter("personelFazlaMesai", session);
			if (planKey != null) {
				if (fazlaMesaiVardiyaGun != null) {
					fazlaMesaiGirisDurum = true;
					dateStr = PdksUtil.convertToDateString(fazlaMesaiVardiyaGun.getVardiyaDate(), "yyyyMMdd");
					perKGSId = fazlaMesaiVardiyaGun.getPersonel().getPersonelKGS().getId();
				}

			} else {
				if (veriLastMap != null) {
					if (fazlaMesaiVardiyaGun == null && ikRole) {
						ortakIslemler.setAramaSecenekleriFromVeriLast(aramaSecenekleri, veriLastMap);
						if (veriLastMap.containsKey("date"))
							date = PdksUtil.convertToJavaDate((String) veriLastMap.get("date"), "yyyy-MM-dd");
					}

				}
			}

			donusAdres = null;
			if (dateStr != null) {
				donusAdres = linkAdres;
				Date vardiyaDate = PdksUtil.convertToJavaDate(dateStr, "yyyyMMdd");
				setDate(vardiyaDate);
				if (perKGSId != null) {

					PersonelKGS personelKGS = (PersonelKGS) pdksEntityController.getSQLParamByFieldObject(PersonelKGS.TABLE_NAME, PersonelKGS.COLUMN_NAME_ID, perKGSId, PersonelKGS.class, session);
					PersonelView personelView = personelKGS != null ? personelKGS.getPersonelView() : null;
					if (personelView != null && personelView.getPdksPersonel() != null) {
						Personel pdksPersonel = personelView.getPdksPersonel();
						Sirket pdksSirket = pdksPersonel.getSirket();
						if (pdksPersonel.getTesis() != null)
							aramaSecenekleri.setTesisId(pdksPersonel.getTesis().getId());
						if (pdksSirket != null) {
							departmanId = pdksSirket.getDepartman().getId();
							aramaSecenekleri.setDepartmanId(departmanId);
							sirketId = pdksSirket.getId();
							aramaSecenekleri.setSirketId(sirketId);
							if (authenticatedUser.isIK() || authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi())
								fillSirketList();
							aramaSecenekleri.setSirketId(sirketId);
						}
						if (pdksPersonel.getEkSaha1() != null) {
							aramaSecenekleri.setEkSaha1Id(pdksPersonel.getEkSaha1().getId());
						}
						if (pdksPersonel.getEkSaha2() != null) {
							aramaSecenekleri.setEkSaha2Id(pdksPersonel.getEkSaha2().getId());
						}
						if (pdksPersonel.getEkSaha3() != null) {
							seciliEkSaha3Id = pdksPersonel.getEkSaha3().getId();
							aramaSecenekleri.setEkSaha3Id(seciliEkSaha3Id);
						}

						if (pdksPersonel.getEkSaha4() != null) {
							seciliEkSaha4Id = pdksPersonel.getEkSaha4().getId();
							aramaSecenekleri.setEkSaha4Id(seciliEkSaha4Id);
						}

						if (pdksSirket != null)
							sirket = pdksSirket;
						aramaSecenekleri.setSicilNo(pdksPersonel.getPdksSicilNo());
						aramaSecenekleri.setAd(pdksPersonel.getAd());
						aramaSecenekleri.setSoyad(pdksPersonel.getSoyad());
					}
					fillHareketMesaiList();
				}

			} else {
				if (aramaSecenekleri.getSirketId() != null)
					fillTesisList();
				aramaSecenekleri.setSicilNo("");
				aramaSecenekleri.setAd("");
				aramaSecenekleri.setSoyad("");
			}
			if (!ayniSayfa)
				authenticatedUser.setCalistigiSayfa("");
			Boolean kullaniciPersonel = ortakIslemler.getKullaniciPersonel(authenticatedUser);
			if (kullaniciPersonel) {
				PdksUtil.addMessageAvailableWarn("'" + ortakIslemler.getMenuAdi("personelFazlaMesai") + "' sayfasına giriş yetkiniz yoktur!");
				return MenuItemConstant.home;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public void filDepartmanList() {

		List<SelectItem> departmanListe = ortakIslemler.getSelectItemList("departman", authenticatedUser);

		List<Departman> list = ortakIslemler.fillDepartmanTanimList(session);
		if (list.size() == 1) {
			departmanId = list.get(0).getId();
			fillSirketList();
		}

		for (Departman pdksDepartman : list)
			departmanListe.add(new SelectItem(pdksDepartman.getId(), pdksDepartman.getAciklama()));

		setDepartmanList(departmanListe);
	}

	public void fillSirketList() {
		Date bugun = PdksUtil.getDate(date);
		try {
			aramaSecenekleri.setDepartmanId(departmanId);
			ortakIslemler.setAramaSecenekSirketVeTesisData(aramaSecenekleri, bugun, bugun, true, session);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String fillTesisList() {
		Date bugun = PdksUtil.getDate(new Date());
		ortakIslemler.setAramaSecenekTesisData(aramaSecenekleri, bugun, bugun, true, session);
		return "";
	}

	public String fillEkSahaList() {
		Date bugun = PdksUtil.getDate(new Date());
		ortakIslemler.setAramaSecenekEkDataDoldur(aramaSecenekleri, bugun, bugun, session);
		return "";
	}

	public String mesaiSec(FazlaMesaiTalep fmt) {
		PersonelFazlaMesai fazlaMesai = getInstance();
		fazlaMesai.setFazlaMesaiTalep(fmt);
		fazlaMesai.setFazlaMesaiOnayDurum(fmt.getMesaiNeden());
		return "";
	}

	/**
	 * @param hareket
	 * @param onayDurum
	 */
	public void ekle(HareketKGS hareket, boolean onayDurum) {
		String tipi = null, sort = null;
		if (onayDurum) {
			tipi = Tanim.TIPI_FAZLA_MESAI_NEDEN;
			sort = "getAciklama";
		} else {
			tipi = Tanim.TIPI_ONAYLAMAMA_NEDEN;
			sort = "getKodu";
		}
		List<Tanim> list = ortakIslemler.getTanimAlanList(tipi, sort, "S", session);
		PersonelFazlaMesai fazlaMesai = getInstance();
		fazlaMesai.setId(null);
		VardiyaGun vg = hareket.getVardiyaGun();
		Date basZaman = ortakIslemler.getSaniyeSifirla(hareket.getGirisZaman(), vg);
		Date bitZaman = ortakIslemler.getSaniyeSifirla(hareket.getCikisZaman(), vg);
		fazlaMesai.setBasZaman(basZaman);
		fazlaMesai.setBitZaman(bitZaman);
		vg.setFazlaMesaiTalepler(null);
		fazlaMesai.setFazlaMesaiTalep(null);
		fazlaMesai.setVardiyaGun(vg);
		Double fazlaMesaiMaxSaati = null;
		if (onayDurum) {
			HashMap fields = new HashMap();
			fields.put("vardiyaGun.id=", vg.getId());
			fields.put("onayDurumu=", FazlaMesaiTalep.ONAY_DURUM_ONAYLANDI);
			fields.put("durum=", Boolean.TRUE);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<FazlaMesaiTalep> fazlaMesaiTalepler = pdksEntityController.getObjectByInnerObjectListInLogic(fields, FazlaMesaiTalep.class);
			if (!fazlaMesaiTalepler.isEmpty()) {
				for (Iterator iterator = fazlaMesaiTalepler.iterator(); iterator.hasNext();) {
					FazlaMesaiTalep fazlaMesaiTalep = (FazlaMesaiTalep) iterator.next();
					if (!(fazlaMesaiTalep.getBitisZamani().getTime() > fazlaMesai.getBasZaman().getTime() && fazlaMesaiTalep.getBaslangicZamani().getTime() < fazlaMesai.getBitZaman().getTime())) {
						iterator.remove();
					}
				}
				if (fazlaMesaiTalepler.size() > 1)
					fazlaMesaiTalepler = PdksUtil.sortListByAlanAdi(fazlaMesaiTalepler, "id", Boolean.TRUE);
				vg.setFazlaMesaiTalepler(fazlaMesaiTalepler);
			}
			setFazlaMesaiList(list);
			Long fazlaMesaiMaxSaatiDurum = null;
			try {
				String fazlaMesaiMaxSaatiDurumStr = ortakIslemler.getParameterKey("fazlaMesaiMaxSaatiDurum");
				if (PdksUtil.hasStringValue(fazlaMesaiMaxSaatiDurumStr))
					fazlaMesaiMaxSaatiDurum = Long.parseLong(fazlaMesaiMaxSaatiDurumStr);
			} catch (Exception e) {
				fazlaMesaiMaxSaatiDurum = null;
			}
			if (fazlaMesaiMaxSaatiDurum != null && fazlaMesaiMaxSaatiDurum.longValue() > 0L) {
				Double sure = PdksUtil.getSaatFarki(fazlaMesai.getBitZaman(), fazlaMesai.getBasZaman()).doubleValue();
				fazlaMesaiMaxSaati = sure.longValue() + 0.0d;
				if (sure.doubleValue() > fazlaMesaiMaxSaati.doubleValue())
					fazlaMesaiMaxSaati += fazlaMesaiMaxSaatiDurum.longValue();
			}

		} else
			setOnaylamamaNedeniList(list);
		fazlaMesai.setFazlaMesaiMaxSaati(fazlaMesaiMaxSaati);
		fazlaMesai.setHareket(hareket);
		fazlaMesai.setHareketId(hareket.getId());
		double fazlaMesaiSaati = PdksUtil.setSureDoubleTypeRounded(hareket.getFazlaMesai(), vg.getFazlaMesaiYuvarla());

		fazlaMesai.setFazlaMesaiSaati(fazlaMesaiSaati);

		hareket.setPersonelFazlaMesai(fazlaMesai);

	}

	@Transactional
	public String onayla() {
		PersonelFazlaMesai fazlaMesai = getInstance();
		VardiyaGun vg = null;
		try {
			vg = getVardiyaPlan(fazlaMesai);
		} catch (Exception e1) {
			logger.error(e1);
			e1.printStackTrace();
		}
		double fazlaMesaiSaati = PdksUtil.setSureDoubleTypeRounded(fazlaMesai.getHareket().getFazlaMesai(), vg.getFazlaMesaiYuvarla());
		if (fazlaMesai.getFazlaMesaiMaxSaati() != null && fazlaMesai.getFazlaMesaiMaxSaati().doubleValue() < fazlaMesaiSaati) {
			fazlaMesai.setOnayDurum(null);
			PdksUtil.addMessageWarn("Mesai saati " + fazlaMesai.getFazlaMesaiMaxSaati().longValue() + " büyük olamaz!");
		} else {
			fazlaMesai.setOnayDurum(PersonelFazlaMesai.DURUM_ONAYLANMADI);
			boolean yeni = fazlaMesai.getId() == null;
			try {
				List<HareketKGS> list = ortakIslemler.getHareketIdBilgileri(null, fazlaMesai.getHareket(), date, date, session);
				boolean tatil = fazlaMesai.getHareket().isTatil();

				HareketKGS hareket = !list.isEmpty() ? list.get(0) : null;
				if (hareket == null && fazlaMesai.getHareketId() != null) {
					hareket = new HareketKGS();
					hareket.setId(fazlaMesai.getHareketId());
				}
				fazlaMesai.setHareketId(hareket.getId());
				fazlaMesai.setHareket(hareket);
				// fazlaMesai.setHareket(hareket);
				fazlaMesai.setVardiyaGun(vg);
				fazlaMesai.setFazlaMesaiSaati(fazlaMesaiSaati);
				if (!tatil)
					fazlaMesai.setTatilDurum(null);
				else
					fazlaMesai.setTatilDurum(PersonelFazlaMesai.BAYRAM);
				fazlaMesai.setOnayDurum(PersonelFazlaMesai.DURUM_ONAYLANDI);
				if (yeni) {
					fazlaMesai.setOlusturanUser(authenticatedUser);

				} else {
					fazlaMesai.setGuncelleyenUser(authenticatedUser);
					fazlaMesai.setGuncellemeTarihi(new Date());
				}

				try {
					saveOrUpdate(fazlaMesai);
					sessionFlush();
				} catch (Exception e) {
					logger.error("Pdks hata in : \n");
					e.printStackTrace();
					logger.error("Pdks hata out : " + e.getMessage());

				}

				setInstance(new PersonelFazlaMesai());

				// session.refresh(this.getInstance());
				fillHareketMesaiList();

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}
		}
		return "";
	}

	/**
	 * @param fazlaMesai
	 * @return
	 * @throws Exception
	 */
	private VardiyaGun getVardiyaPlan(PersonelFazlaMesai fazlaMesai) throws Exception {
		VardiyaGun pdksVardiyaGun = null;
		if (fazlaMesai.getVardiyaGun() != null) {
			if (fazlaMesai.getVardiyaGun().getId() == null) {
				pdksVardiyaGun = fazlaMesai.getVardiyaGun();
				String key = pdksVardiyaGun.getVardiyaKey();
				List<Personel> personeller = new ArrayList<Personel>();
				personeller.add(pdksVardiyaGun.getPersonel());
				TreeMap<String, VardiyaGun> vardiyalar = ortakIslemler.getVardiyalar(personeller, null, pdksVardiyaGun.getVardiyaDate(), pdksVardiyaGun.getVardiyaDate(), null, Boolean.TRUE, session, Boolean.FALSE);
				if (vardiyalar.containsKey(key))
					pdksVardiyaGun = vardiyalar.get(key);
				fazlaMesai.setVardiyaGun(pdksVardiyaGun);
			} else {

				pdksVardiyaGun = (VardiyaGun) pdksEntityController.getSQLParamByFieldObject(VardiyaGun.TABLE_NAME, VardiyaGun.COLUMN_NAME_ID, fazlaMesai.getVardiyaGun().getId(), VardiyaGun.class, session);

			}

		}
		return pdksVardiyaGun;
	}

	@Transactional
	public String onaylama() {
		PersonelFazlaMesai fazlaMesai = getInstance();
		String islem = "";
		fazlaMesai.setOnayDurum(null);
		if (fazlaMesai.getFazlaMesaiOnayDurum() != null) {
			boolean yeni = fazlaMesai.getId() == null;
			try {
				VardiyaGun pdksVardiyaGun = getVardiyaPlan(fazlaMesai);
				List<HareketKGS> list = ortakIslemler.getHareketIdBilgileri(null, fazlaMesai.getHareket(), date, date, session);
				HareketKGS hareket = !list.isEmpty() ? list.get(0) : null;

				if (hareket != null) {
					fazlaMesai.setHareketId(hareket.getId());
					fazlaMesai.setHareket(hareket);
					// fazlaMesai.setHareket(hareket);
					fazlaMesai.setVardiyaGun(pdksVardiyaGun);
					fazlaMesai.setFazlaMesaiSaati(0.0d);
					fazlaMesai.setOnayDurum(PersonelFazlaMesai.DURUM_ONAYLANMADI);

					if (yeni) {
						fazlaMesai.setOlusturanUser(authenticatedUser);

					} else {
						fazlaMesai.setGuncelleyenUser(authenticatedUser);
						fazlaMesai.setGuncellemeTarihi(new Date());
					}

					try {
						saveOrUpdate(fazlaMesai);
						sessionFlush();
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					session.refresh(this.getInstance());
					setInstance(new PersonelFazlaMesai());
					fillHareketMesaiList();
					islem = "persisted";
				}

			} catch (Exception e) {
				logger.error("Pdks hata in : \n");
				e.printStackTrace();
				logger.error("Pdks hata out : " + e.getMessage());

			}
		} else {
			fazlaMesai.setOnayDurum(PersonelFazlaMesai.DURUM_ONAYLANMADI);
			PdksUtil.addMessageAvailableWarn("Neden seçiniz!");
		}

		return islem;

	}

	public String fillDenklestirmeHareketList() {
		return "";
	}

	public void fillHareketMesaiList() throws Exception {
		sessionClear();
		seciliEkSaha3 = null;
		if (seciliEkSaha3Id != null) {

			seciliEkSaha3 = (Tanim) pdksEntityController.getSQLParamByFieldObject(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, seciliEkSaha3Id, Tanim.class, session);

		}
		if (seciliEkSaha4Id != null) {

			seciliEkSaha4 = (Tanim) pdksEntityController.getSQLParamByFieldObject(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, seciliEkSaha4Id, Tanim.class, session);
		}

		ArrayList<String> sicilNoList = null;
		if (fazlaMesaiGirisDurum)
			sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.FALSE, session);
		else {
			List<Personel> perList = ortakIslemler.getAramaSecenekleriPersonelList(authenticatedUser, date, aramaSecenekleri, session);
			if (!perList.isEmpty()) {
				sicilNoList = new ArrayList<String>();
				for (Personel personel : perList) {
					sicilNoList.add(personel.getPdksSicilNo());
				}
			} else
				sicilNoList = new ArrayList<String>();
		}

		String sicilNo = ortakIslemler.getSicilNo(aramaSecenekleri.getSicilNo());
		if (linkAdres != null && PdksUtil.hasStringValue(sicilNo) && !sicilNoList.contains(sicilNo))
			sicilNoList.add(sicilNo);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		pdksDenklestirmeAy = ortakIslemler.getSQLDenklestirmeAy(cal, session);

		if (ikRole == false && authenticatedUser.isDirektorSuperVisor() == false) {
			Long sirketId = aramaSecenekleri.getSirketId(), tesisId = aramaSecenekleri.getTesisId(), ekSaha3Id = aramaSecenekleri.getEkSaha3Id(), ekSaha4Id = aramaSecenekleri.getEkSaha4Id();
			fazlaMesaiOrtakIslemler.setFazlaMesaiPersonel(date, sicilNoList, sirketId, tesisId, ekSaha3Id, ekSaha4Id, session);
		}
		denklestirmeAyDurum = fazlaMesaiOrtakIslemler.getDurum(pdksDenklestirmeAy);
		ArrayList<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>();
		List<PersonelFazlaMesai> mesaiList = new ArrayList<PersonelFazlaMesai>();
		List<HareketKGS> kgsList1 = new ArrayList<HareketKGS>();
		ArrayList<Personel> personeller = null;
		if (sicilNoList != null && !sicilNoList.isEmpty()) {
			personeller = (ArrayList<Personel>) pdksEntityController.getSQLParamByFieldList(Personel.TABLE_NAME, Personel.COLUMN_NAME_PDKS_SICIL_NO, sicilNoList.clone(), Personel.class, session);
		}
		tatilMap = ortakIslemler.getTatilGunleri(personeller, ortakIslemler.tariheGunEkleCikar(cal, date, -1), ortakIslemler.tariheGunEkleCikar(cal, date, 1), session);
		tatil = null;
		fazlaMesaiSistemOnayDurum = null;
		if (!tatilMap.isEmpty()) {
			if (tatilMap.containsKey(PdksUtil.convertToDateString(date, "yyyyMMdd"))) {
				tatil = (Tatil) tatilMap.get((PdksUtil.convertToDateString(date, "yyyyMMdd")));
				if (tatil.isTekSefer() && tatil.getBitGun() != null)
					tatil.setBitTarih((Date) tatil.getBitGun());
			}
			fazlaMesaiSistemOnayDurum = ortakIslemler.getOtomatikKapGirisiNeden(session);
		}

		if (personeller != null && !personeller.isEmpty()) {
			saveLastParameter();
			try {
				boolean fazlaMesaiYemekHesapla = ortakIslemler.getParameterKey("fazlaMesaiYemekHesapla").equals("1");
				List<YemekIzin> yemekGenelList = null;
				if (fazlaMesaiYemekHesapla)
					yemekGenelList = ortakIslemler.getYemekList(date, null, session);
				else
					yemekGenelList = new ArrayList<YemekIzin>();

				boolean giris = Boolean.TRUE;
				boolean yaz = Boolean.FALSE;
				while (giris) {
					List<Long> personelIdList = new ArrayList<Long>();
					for (Iterator iterator = personeller.iterator(); iterator.hasNext();) {
						Personel personel = (Personel) iterator.next();
						personelIdList.add(personel.getId());
					}

					List<HareketKGS> kgsList = new ArrayList<HareketKGS>();

					Date tarih1 = null;
					Date tarih2 = null;
					Date tarih3 = null;
					Date tarih4 = null;
					TreeMap<String, VardiyaGun> vardiyaMap = null;
					Date basTarih = ortakIslemler.tariheGunEkleCikar(cal, date, -2), bitTarih = ortakIslemler.tariheGunEkleCikar(cal, date, 3);
					try {
						vardiyaMap = ortakIslemler.getIslemVardiyalar((List<Personel>) personeller.clone(), basTarih, bitTarih, yaz, session, Boolean.FALSE);
					} catch (Exception e) {
						vardiyaMap = new TreeMap<String, VardiyaGun>();
						e.printStackTrace();
					}
					boolean yuvarlamaKatSayiOku = ortakIslemler.getParameterKey("yuvarlamaKatSayiOku").equals("1");
					TreeMap<String, BigDecimal> yuvarlamaMap = null, fazlaMesaiYuvarlamaMap = null;
					if (yuvarlamaKatSayiOku) {
						List<Long> perIdList = new ArrayList<Long>();
						for (Personel personel : personeller)
							perIdList.add(personel.getId());
						yuvarlamaMap = ortakIslemler.getPlanKatSayiMap(perIdList, basTarih, bitTarih, KatSayiPuantajTipi.YUVARLAMA_TIPI, session);
						fazlaMesaiYuvarlamaMap = ortakIslemler.getPlanKatSayiMap(perIdList, basTarih, bitTarih, KatSayiPuantajTipi.FAZLA_MESAI_YUVARLAMA, session);
						perIdList = null;
					}
					vardiyaList = new ArrayList<VardiyaGun>(vardiyaMap.values());
					ortakIslemler.setVardiyaYemekList(vardiyaList, yemekGenelList);
					Date bugun = Calendar.getInstance().getTime();
					TreeMap<String, List<VardiyaGun>> perVardiyaMap = new TreeMap<String, List<VardiyaGun>>();
					for (VardiyaGun vg : vardiyaList) {
						if (vg.getVardiya() == null)
							continue;
						Vardiya vardiya = vg.getVardiya();
						Long sirketId = null, tesisId = null, vardiyaId = null;
						try {
							if (vg.getPdksPersonel() != null) {
								Sirket sirket = vg.getPdksPersonel().getSirket();
								if (sirket != null) {
									if (sirket.getTesisDurum())
										tesisId = vg.getPdksPersonel().getTesis() != null ? vg.getPdksPersonel().getTesis().getId() : null;
									sirketId = sirket.getId();
								}
							}
						} catch (Exception e) {
							sirketId = null;
						}
						try {
							vardiyaId = vardiya != null ? vardiya.getId() : null;
						} catch (Exception e) {
							vardiyaId = null;
						}
						String key = vg.getPersonel().getPdksSicilNo();
						if (yuvarlamaMap != null) {
							String str = vg.getVardiyaDateStr();
							if (ortakIslemler.veriKatSayiVar(yuvarlamaMap, sirketId, tesisId, vardiyaId, str)) {
								BigDecimal deger = ortakIslemler.getKatSayiVeriMap(yuvarlamaMap, sirketId, tesisId, vardiyaId, str);
								if (deger != null)
									vg.setYarimYuvarla(deger.intValue());
							}
							if (ortakIslemler.veriKatSayiVar(fazlaMesaiYuvarlamaMap, sirketId, tesisId, vardiyaId, str)) {
								BigDecimal deger = ortakIslemler.getKatSayiVeriMap(fazlaMesaiYuvarlamaMap, sirketId, tesisId, vardiyaId, str);
								if (deger != null)
									vg.setFazlaMesaiYuvarla(deger.intValue());
							}
						}

						List<VardiyaGun> list = perVardiyaMap.containsKey(key) ? perVardiyaMap.get(key) : new ArrayList<VardiyaGun>();
						if (list.isEmpty())
							perVardiyaMap.put(key, list);
						list.add(vg);
					}
					TreeMap<String, VardiyaGun> vardiyalarMap = new TreeMap<String, VardiyaGun>();
					for (String key : perVardiyaMap.keySet()) {
						List<VardiyaGun> list = perVardiyaMap.get(key);
						for (VardiyaGun vardiyaGun : list)
							vardiyalarMap.put(vardiyaGun.getVardiyaKeyStr(), vardiyaGun);
						ortakIslemler.fazlaMesaiSaatiAyarla(vardiyalarMap);
						vardiyalarMap.clear();
						list = null;
					}
					vardiyalarMap = null;
					perVardiyaMap = null;
					for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
						VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
						// logger.info(pdksVardiyaGun.getVardiyaKeyStr());
						if (PdksUtil.tarihKarsilastirNumeric(pdksVardiyaGun.getVardiyaDate(), date) != 0) {
							iterator.remove();
							continue;

						}

					}
					HashMap parametreMap2 = new HashMap();
					List idler = new ArrayList();
					StringBuilder sb = new StringBuilder();
					String fieldName = "p";
					sb.append("select * from " + Personel.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
					sb.append(" where " + Personel.COLUMN_NAME_ID + " :" + fieldName + " and " + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :s ");
					for (Personel personel : personeller)
						idler.add(personel.getId());

					if (!Personel.getGrubaGirisTarihiAlanAdi().equalsIgnoreCase(Personel.COLUMN_NAME_GRUBA_GIRIS_TARIHI))
						sb.append(" and " + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :b");
					else
						sb.append(" and " + Personel.COLUMN_NAME_GRUBA_GIRIS_TARIHI + " <= :b");
					parametreMap2.put("b", date);

					parametreMap2.put("s", date);
					parametreMap2.put(fieldName, idler);
					if (session != null)
						parametreMap2.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<Personel> personelList = pdksEntityController.getSQLParamList(idler, sb, fieldName, parametreMap2, Personel.class, session);
					if (!personelList.isEmpty()) {
						idler.clear();
						for (Personel personel : personelList) {
							idler.add(personel.getId());
						}
						fieldName = "p";
						sb = new StringBuilder();
						sb.append("select F.* from " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK());
						sb.append(" inner join " + PersonelFazlaMesai.TABLE_NAME + " F " + PdksEntityController.getJoinLOCK() + " on F." + PersonelFazlaMesai.COLUMN_NAME_VARDIYA_GUN + " = V." + VardiyaGun.COLUMN_NAME_ID);
						sb.append(" and F." + PersonelFazlaMesai.COLUMN_NAME_DURUM + " = 1 ");
						sb.append(" where V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " = :t and V." + VardiyaGun.COLUMN_NAME_PERSONEL + " :" + fieldName);
						parametreMap2.clear();
						parametreMap2.put("t", date);
						parametreMap2.put(fieldName, idler);
						if (session != null)
							parametreMap2.put(PdksEntityController.MAP_KEY_SESSION, session);
						// mesaiList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap2, PersonelFazlaMesai.class);
						mesaiList = pdksEntityController.getSQLParamList(idler, sb, fieldName, parametreMap2, PersonelFazlaMesai.class, session);

					} else
						mesaiList = new ArrayList<PersonelFazlaMesai>();
					idler = null;
					for (VardiyaGun pdksVardiyaGun : vardiyaList) {
						pdksVardiyaGun.setHareketler(null);
						pdksVardiyaGun.setGirisHareketleri(null);
						pdksVardiyaGun.setCikisHareketleri(null);
						pdksVardiyaGun.setGecersizHareketler(null);
						Vardiya islemVardiya = pdksVardiyaGun.getIslemVardiya();
						if ((tarih1 == null && tarih3 == null) || islemVardiya.getVardiyaBasZaman().getTime() < tarih3.getTime()) {
							tarih3 = islemVardiya.getVardiyaBasZaman();
							tarih1 = islemVardiya.getVardiyaFazlaMesaiBasZaman();
						}
						if (tarih2 == null || islemVardiya.getVardiyaBitZaman().getTime() > tarih4.getTime()) {
							tarih4 = islemVardiya.getVardiyaBitZaman();
							tarih2 = islemVardiya.getVardiyaFazlaMesaiBitZaman();
							if (pdksVardiyaGun.getSonrakiVardiyaGun() == null)
								tarih2 = ortakIslemler.tariheGunEkleCikar(cal, tarih1, 1);

						}

					}
					List<Long> kapiIdler = ortakIslemler.getPdksDonemselKapiIdler(tarih1, tarih2, session);
					if (!personelList.isEmpty()) {
						List<Long> perIdler = new ArrayList<Long>();
						for (Iterator iterator2 = personelList.iterator(); iterator2.hasNext();) {
							Personel pdksPersonel = (Personel) iterator2.next();
							perIdler.add(pdksPersonel.getPersonelKGS().getId());
						}

						try {
							kgsList = ortakIslemler.getHareketBilgileri(kapiIdler, perIdler, tarih1, PdksUtil.addTarih(tarih2, Calendar.SECOND, 1), HareketKGS.class, session);
							for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
								HareketKGS hareket = (HareketKGS) iterator.next();
								if (hareket.getAktif() == false)
									iterator.remove();

							}
						} catch (Exception e) {
							kgsList = new ArrayList<HareketKGS>();
							e.printStackTrace();
						}
						perIdler = null;
					} else
						kgsList = new ArrayList<HareketKGS>();
					if (kgsList.size() > 1)
						kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);

					Long bitZaman2 = 0L, bitFazlaMesai = 0L;

					try {
						HashMap<Long, HareketKGS> girisMap = new HashMap<Long, HareketKGS>(), cikisMap = new HashMap<Long, HareketKGS>();
						long perId = 0;
						for (Iterator iterator1 = kgsList.iterator(); iterator1.hasNext();) {
							HareketKGS kgsHareket = (HareketKGS) iterator1.next();
							if (kgsHareket.getIslem() != null && kgsHareket.getIslem().getIslemTipi().equals("D")) {
								iterator1.remove();
								continue;
							}
							kgsHareket.setTatil(Boolean.FALSE);
							setTatilDurum(kgsHareket, kgsHareket.getZaman());
							kgsHareket.setGirisHareket(null);
							kgsHareket.setCikisHareket(null);
						}
						for (Iterator iterator1 = kgsList.iterator(); iterator1.hasNext();) {
							HareketKGS kgsHareket = (HareketKGS) iterator1.next();
							perId = kgsHareket.getPersonel().getPdksPersonel().getId();
							boolean durum = false;
							for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
								VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
								if (vardiyaGun.getPersonel().getId().equals(perId)) {
									if (vardiyaGun.addHareket(kgsHareket, Boolean.FALSE)) {
										durum = true;
									}

								}

							}
							if (!durum) {
								iterator1.remove();
								continue;
							}
							if (kgsHareket.getKapiView().getKapi().isGirisKapi()) {
								if (!girisMap.containsKey(perId)) {
									kgsHareket.setGirisZaman(kgsHareket.getZaman());
									girisMap.put(perId, kgsHareket);
								}

							} else if (kgsHareket.getKapiView().getKapi().isCikisKapi()) {
								kgsHareket.setCikisZaman(kgsHareket.getZaman());
								cikisMap.put(perId, kgsHareket);

							}

						}
						ortakIslemler.otomatikHareketEkle(new ArrayList<VardiyaGun>(vardiyaMap.values()), session);
						for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
							VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
							if (vardiyaGun.getHareketler() == null)
								continue;
							List<YemekIzin> yemekList = vardiyaGun.getYemekList();
							yuvarmaTipi = vardiyaGun.getFazlaMesaiYuvarla();
							List<HareketKGS> girisHareketleri = vardiyaGun.getGirisHareketleri(), cikisHareketleri = vardiyaGun.getCikisHareketleri();
							if (girisHareketleri == null || cikisHareketleri == null || cikisHareketleri.size() != girisHareketleri.size())
								continue;
							Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
							double molaSaat = islemVardiya.getCikisMolaSaat() != null && islemVardiya.getCikisMolaSaat().intValue() > 0 ? (double) islemVardiya.getCikisMolaSaat() / 60.0d : 0.0d;
							if (vardiyaGun.getHareketDurum() || bugun.getTime() < islemVardiya.getVardiyaTelorans2BitZaman().getTime()) {
								PersonelIzin izin = vardiyaGun.getIzin();
								if (vardiyaGun.getVardiya().isCalisma() && !vardiyaGun.getVardiya().isIcapVardiyasi()) {
									bitZaman2 = islemVardiya.getVardiyaTelorans2BitZaman().getTime();
									bitFazlaMesai = islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime();
									if (vardiyaGun.getHareketDurum() && izin == null && cikisHareketleri.size() > 0) {
										Date vardiyaFazlaMesaiBasZaman = islemVardiya.getVardiyaFazlaMesaiBasZaman();
										Date vardiyaTelorans1BasZaman = islemVardiya.getVardiyaTelorans1BasZaman();
										Date vardiyaTelorans2BitZaman = islemVardiya.getVardiyaTelorans2BitZaman();
										Date vardiyaFazlaMesaiBitZaman = islemVardiya.getVardiyaFazlaMesaiBitZaman();
										for (int j = 0; j < cikisHareketleri.size(); j++) {
											HareketKGS hareketGiris = girisHareketleri.get(j);
											HareketKGS hareketCikis = cikisHareketleri.get(j);
											// if (hareketGiris.getOrjinalZaman() != null)
											// hareketGiris.setZaman(hareketGiris.getOrjinalZaman());
											// if (hareketCikis.getOrjinalZaman() != null)
											// hareketCikis.setZaman(hareketCikis.getOrjinalZaman());
											HareketKGS kgsHareketGiris = (HareketKGS) hareketGiris.clone();
											HareketKGS kgsHareketCikis = (HareketKGS) hareketCikis.clone();

											try {
												Long kgsZaman = kgsHareketCikis.getZaman().getTime();
												Date girisZaman = kgsHareketGiris != null && kgsHareketGiris.getOrjinalZaman() != null ? (Date) kgsHareketGiris.getZaman().clone() : null;
												Date cikisZaman = kgsHareketCikis != null && kgsHareketCikis.getOrjinalZaman() != null ? (Date) kgsHareketCikis.getZaman().clone() : null;

												if ((cikisZaman.getTime() > vardiyaTelorans2BitZaman.getTime()) && (cikisZaman.getTime() <= vardiyaFazlaMesaiBitZaman.getTime())) {
													kgsHareketCikis.setVardiyaGun(vardiyaGun);

													kgsHareketCikis.setGirisZaman(girisZaman.before(islemVardiya.getVardiyaBitZaman()) ? islemVardiya.getVardiyaBitZaman() : girisZaman);
													double saat = 0;

													if (cikisZaman.getTime() > bitZaman2) {

														if (girisZaman.getTime() < bitZaman2)
															girisZaman = islemVardiya.getVardiyaBitZaman();

														saat += ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vardiyaGun, session);

														if (girisZaman.getTime() <= cikisZaman.getTime()) {
															if (molaSaat > saat)
																molaSaat = saat;
															kgsHareketCikis.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat - molaSaat, vardiyaGun.getFazlaMesaiYuvarla()));
															kgsHareketCikis.setCikisZaman(cikisZaman);
															molaSaat = 0;

															kgsHareketCikis.setGirisHareket(kgsHareketGiris);
															fazlaMesaiEkle(kgsList1, kgsHareketCikis);
															// logger.info("bb " + kgsList1.size());
														}
													}
												}
												kgsHareketGiris = (HareketKGS) girisHareketleri.get(j).clone();
												kgsHareketCikis = (HareketKGS) cikisHareketleri.get(j).clone();
												girisZaman = kgsHareketGiris != null && kgsHareketGiris.getZaman() != null ? (Date) kgsHareketGiris.getZaman().clone() : null;
												cikisZaman = kgsHareketCikis != null && kgsHareketCikis.getZaman() != null ? (Date) kgsHareketCikis.getZaman().clone() : null;

												kgsZaman = kgsHareketGiris.getZaman().getTime();
												long basZaman = kgsHareketGiris == null ? 0L : vardiyaTelorans1BasZaman.getTime();
												long basZaman2 = kgsHareketGiris == null ? 0L : vardiyaFazlaMesaiBasZaman.getTime();
												if (kgsZaman < basZaman && kgsZaman >= basZaman2) {
													double saat = 0.0d;
													kgsHareketGiris.setVardiyaGun(vardiyaGun);

													// for (int i = 0; i < girisHareketleri.size(); i++) {
													// girisZaman = girisHareketleri.get(i).getZaman();

													if (girisZaman.getTime() < basZaman && girisZaman.getTime() >= basZaman2) {
														if (kgsHareketGiris.getGirisZaman() == null)
															kgsHareketGiris.setGirisZaman(girisZaman);
														cikisZaman = kgsHareketCikis.getZaman();
														if (cikisZaman.getTime() > basZaman)
															cikisZaman = islemVardiya.getVardiyaBasZaman();
														kgsHareketGiris.setCikisZaman(cikisZaman);
														saat += ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vardiyaGun, session);
														if (girisZaman.getTime() <= cikisZaman.getTime()) {
															kgsHareketGiris.setCikisZaman(cikisZaman);
															kgsHareketGiris.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat, vardiyaGun.getFazlaMesaiYuvarla()));
															kgsHareketGiris.setVardiyaGun(vardiyaGun);
															kgsHareketGiris.setGirisZaman(girisZaman);
															fazlaMesaiEkle(kgsList1, kgsHareketGiris);
															// logger.info("aa1 " + kgsList1.size());
														}
													}

												}

												// }
											} catch (Exception ee) {
												ee.printStackTrace();
											}
										}

									}

									if (izin != null) {

										for (int i = 0; i < cikisHareketleri.size(); i++) {
											HareketKGS kgsHareketGiris = girisHareketleri.get(i);
											HareketKGS kgsHareketCikis = cikisHareketleri.get(i);
											Date girisDate = kgsHareketGiris.getZaman();
											Date cikisDate = kgsHareketCikis.getZaman();
											kgsHareketGiris.setVardiyaGun(vardiyaGun);
											kgsHareketGiris.setCikisZaman(cikisDate);
											kgsHareketGiris.setGirisZaman(girisDate);
											double saat = ortakIslemler.getSaatSure(girisDate, cikisDate, yemekList, vardiyaGun, session);
											if (girisDate.getTime() <= cikisDate.getTime()) {
												kgsHareketGiris.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat, vardiyaGun.getFazlaMesaiYuvarla()));
												fazlaMesaiEkle(kgsList1, kgsHareketGiris);
											}
										}

									}

								}

								else {

									if (tatil != null) {
										for (int i = 0; i < cikisHareketleri.size(); i++) {
											HareketKGS kgsHareketGiris = girisHareketleri.get(i);
											HareketKGS kgsHareketCikis = cikisHareketleri.get(i);
											if (tatil.isYarimGunMu() && kgsHareketGiris != null) {
												if (kgsHareketGiris.getGirisZaman() == null)
													kgsHareketGiris.setGirisZaman(kgsHareketGiris.getZaman());
												if (kgsHareketGiris.getGirisZaman().getTime() < tatil.getBasTarih().getTime()) {
													Date kgsGirisDate = kgsHareketGiris.getGirisZaman();
													kgsHareketGiris.setVardiyaGun(vardiyaGun);
													kgsHareketGiris.setCikisZaman(tatil.getBasTarih());
													if (kgsHareketCikis != null) {
														if (kgsHareketCikis.getCikisZaman() == null)
															kgsHareketCikis.setCikisZaman(kgsHareketCikis.getZaman());
														if (kgsHareketGiris.getCikisZaman().after(kgsHareketCikis.getCikisZaman()))
															kgsHareketGiris.setCikisZaman(kgsHareketCikis.getCikisZaman());
													}
													Date kgsCikisDate = kgsHareketGiris.getCikisZaman();
													double saat = ortakIslemler.getSaatSure(kgsGirisDate, kgsCikisDate, yemekList, vardiyaGun, session);
													if (kgsGirisDate.getTime() <= kgsCikisDate.getTime()) {
														kgsHareketGiris.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat, vardiyaGun.getFazlaMesaiYuvarla()));
														fazlaMesaiEkle(kgsList1, kgsHareketGiris);
													}
												}
												if (kgsHareketCikis != null) {
													if (kgsHareketCikis.getCikisZaman() == null)
														kgsHareketCikis.setCikisZaman(kgsHareketCikis.getZaman());
													Date kgsCikisDate = kgsHareketCikis.getCikisZaman();
													kgsHareketCikis.setVardiyaGun(vardiyaGun);
													kgsHareketCikis.setGirisZaman(tatil.getBasTarih());
													double saat = ortakIslemler.getSaatSure(tatil.getBasTarih(), kgsCikisDate, yemekList, vardiyaGun, session);
													if (tatil.getBasTarih().getTime() <= kgsCikisDate.getTime()) {
														if (molaSaat > saat)
															molaSaat = saat;
														kgsHareketCikis.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat - molaSaat, vardiyaGun.getFazlaMesaiYuvarla()));
														molaSaat = 0;
														fazlaMesaiEkle(kgsList1, kgsHareketCikis);
													}

												}

											}

											else {
												if (kgsHareketGiris != null && kgsHareketCikis != null) {
													Date girisDate = kgsHareketGiris.getZaman();
													Date cikisDate = kgsHareketCikis.getZaman();
													if (vardiyaGun.getVardiya().isCalisma() == false || kgsHareketGiris.getGirisZaman() == null)
														kgsHareketGiris.setGirisZaman(girisDate);
													kgsHareketGiris.setVardiyaGun(vardiyaGun);
													kgsHareketGiris.setCikisZaman(cikisDate);
													double saat = ortakIslemler.getSaatSure(girisDate, cikisDate, yemekList, vardiyaGun, session);
													if (girisDate.getTime() <= cikisDate.getTime()) {
														kgsHareketGiris.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat, vardiyaGun.getFazlaMesaiYuvarla()));
														fazlaMesaiEkle(kgsList1, kgsHareketGiris);
													}

												}
											}

										}
									}

									else {
										// Date girisDate = null, cikisDate = null;

										islemVardiya = vardiyaGun.getIslemVardiya();
										for (int j = 0; j < cikisHareketleri.size(); j++) {
											HareketKGS kgsHareketGiris = girisHareketleri.get(j);
											HareketKGS kgsHareketCikis = cikisHareketleri.get(j);
											if (!vardiyaGun.getVardiya().isIcapVardiyasi() && vardiyaGun.getVardiya().isCalisma()
													&& !(kgsHareketGiris.getZaman().getTime() >= islemVardiya.getVardiyaTelorans1BasZaman().getTime() && islemVardiya.getVardiyaTelorans2BitZaman().getTime() >= kgsHareketCikis.getZaman().getTime()))
												continue;

											if (!vardiyaGun.getVardiya().isIcapVardiyasi()) {
												try {
													bitZaman2 = islemVardiya.getVardiyaFazlaMesaiBasZaman().getTime();
													bitFazlaMesai = islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime();

													Long kgsZaman = kgsHareketCikis.getZaman().getTime();
													if ((kgsHareketCikis.getZaman().getTime() > bitZaman2) && (kgsHareketGiris.getZaman().getTime() <= bitFazlaMesai)) {
														kgsHareketGiris.setVardiyaGun(vardiyaGun);
														Date girisZaman = (Date) kgsHareketGiris.getZaman().clone();

														kgsHareketGiris.setGirisZaman(girisZaman);
														double saat = 0;

														Date cikisZaman = (Date) kgsHareketCikis.getZaman().clone();
														if (cikisZaman.getTime() > bitZaman2) {

															if (girisZaman.getTime() < bitZaman2)
																girisZaman = islemVardiya.getVardiyaBitZaman();

															saat += ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vardiyaGun, session);

															if (girisZaman.getTime() <= cikisZaman.getTime()) {
																if (molaSaat > saat)
																	molaSaat = saat;
																kgsHareketGiris.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat, vardiyaGun.getFazlaMesaiYuvarla()));
																kgsHareketGiris.setCikisZaman(cikisZaman);
																molaSaat = 0;
																kgsHareketGiris.setCikisHareket(kgsHareketCikis);
																fazlaMesaiEkle(kgsList1, kgsHareketGiris);
															}
														}
													}
													kgsZaman = kgsHareketGiris.getZaman().getTime();
													long basZaman = kgsHareketGiris == null ? 0L : islemVardiya.getVardiyaTelorans1BasZaman().getTime();
													long basZaman2 = kgsHareketGiris == null ? 0L : islemVardiya.getVardiyaFazlaMesaiBasZaman().getTime();
													if (kgsZaman < basZaman && kgsZaman >= basZaman2) {
														double saat = 0.0d;
														kgsHareketGiris.setVardiyaGun(vardiyaGun);

														for (int i = 0; i < girisHareketleri.size(); i++) {
															Date girisZaman = girisHareketleri.get(i).getZaman();

															if (girisZaman.getTime() < basZaman && girisZaman.getTime() >= basZaman2) {
																if (kgsHareketGiris.getGirisZaman() == null)
																	kgsHareketGiris.setGirisZaman(girisZaman);
																Date cikisZaman = cikisHareketleri.get(i).getZaman();

																kgsHareketGiris.setCikisZaman(cikisZaman);

																saat += ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vardiyaGun, session);
																if (girisZaman.getTime() <= cikisZaman.getTime()) {
																	kgsHareketGiris.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat, vardiyaGun.getFazlaMesaiYuvarla()));
																	fazlaMesaiEkle(kgsList1, kgsHareketGiris);
																}
															}

														}

													}
												} catch (Exception ee) {
													ee.printStackTrace();
												}
											} else {
												Date girisZaman = (Date) kgsHareketGiris.getZaman().clone();
												Date cikisZaman = (Date) kgsHareketCikis.getZaman().clone();
												kgsHareketGiris.setVardiyaGun(vardiyaGun);
												kgsHareketGiris.setCikisZaman(cikisZaman);
												kgsHareketGiris.setGirisZaman(girisZaman);
												double saat = ortakIslemler.getSaatSure(girisZaman, cikisZaman, yemekList, vardiyaGun, session);
												kgsHareketGiris.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat, vardiyaGun.getFazlaMesaiYuvarla()));
												fazlaMesaiEkle(kgsList1, kgsHareketGiris);

											}

										}

									}

								}
							} else {
								HareketKGS kgsHareket = vardiyaGun.getHareketler().get(0);

								Date girisZaman = girisHareketleri != null ? vardiyaGun.getHareketler().get(0).getZaman() : kgsHareket.getZaman();
								Date cikisZaman = cikisHareketleri != null ? cikisHareketleri.get(cikisHareketleri.size() - 1).getZaman() : vardiyaGun.getHareketler().get(vardiyaGun.getHareketler().size() - 1).getZaman();
								kgsHareket.setVardiyaGun(vardiyaGun);
								kgsHareket.setGirisZaman(girisZaman);
								kgsHareket.setCikisZaman(cikisZaman);
								kgsHareket.setFazlaMesai((double) 0);
								fazlaMesaiEkle(kgsList1, kgsHareket);
							}

						}
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());

					}
					HashMap<String, String> hareketMap = new HashMap<String, String>();
					List<HareketKGS> idList = new ArrayList<HareketKGS>();
					for (Iterator iterator2 = kgsList1.iterator(); iterator2.hasNext();) {
						HareketKGS kgsHareket = (HareketKGS) iterator2.next();
						if (hareketMap.containsKey(kgsHareket.getId())) {
							iterator2.remove();
							continue;
						}
						hareketMap.put(kgsHareket.getId(), kgsHareket.getId());
						kgsHareket.setPersonelFazlaMesai(null);
						for (Iterator iterator = mesaiList.iterator(); iterator.hasNext();) {
							PersonelFazlaMesai mesai = (PersonelFazlaMesai) iterator.next();

							if (mesai.getHareketId().equals(kgsHareket.getId())) {
								mesai.setHareket(kgsHareket);
								kgsHareket.setPersonelFazlaMesai(mesai);
								idList.add(kgsHareket);
								iterator.remove();
								break;
							}
						}

					}
					if (!mesaiList.isEmpty()) {

						try {
							List<HareketKGS> hareketler = ortakIslemler.getHareketIdBilgileri(idList, null, date, date, session);
							for (HareketKGS kgsHareket : hareketler) {
								for (Iterator iterator = mesaiList.iterator(); iterator.hasNext();) {
									PersonelFazlaMesai mesai = (PersonelFazlaMesai) iterator.next();

									if (mesai.getHareketId().equals(kgsHareket.getId())) {
										mesai.setHareket(kgsHareket);
										kgsHareket.setGirisZaman(mesai.getBasZaman());
										kgsHareket.setCikisZaman(mesai.getBitZaman());
										kgsHareket.setPersonelFazlaMesai(mesai);
										fazlaMesaiEkle(kgsList1, kgsHareket);
										iterator.remove();
										break;
									}
								}
							}
						} catch (Exception e) {

						}

						for (Iterator iterator = mesaiList.iterator(); iterator.hasNext();) {
							PersonelFazlaMesai mesai = (PersonelFazlaMesai) iterator.next();
							HareketKGS kgsHareket = new HareketKGS();
							kgsHareket.setVardiyaGun(mesai.getVardiyaGun());
							kgsHareket.setId(mesai.getHareketId());
							kgsHareket.setGirisZaman(mesai.getBasZaman());
							kgsHareket.setCikisZaman(mesai.getBitZaman());
							kgsHareket.setPersonelId(mesai.getVardiyaGun().getPersonel().getId());
							PersonelView personel = new PersonelView();
							personel.setPdksPersonel(mesai.getVardiyaGun().getPersonel());
							kgsHareket.setPersonel(personel);
							mesai.setHareket(kgsHareket);
							kgsHareket.setPersonelFazlaMesai(mesai);
							fazlaMesaiEkle(kgsList1, kgsHareket);

						}
					}

					hareketMap = null;
					giris = Boolean.FALSE;
					HashMap<Long, Personel> perMap = new HashMap<Long, Personel>();
					for (HareketKGS kgsHareket : kgsList1) {
						VardiyaGun vg = kgsHareket.getVardiyaGun();
						if (vg != null) {
							if (kgsHareket.getPersonelFazlaMesai() == null && vg.getVardiya() != null) {
								Vardiya vardiya = vg.getVardiya();
								double yemekVardiyaSuresi = vardiya.isCalisma() ? vardiya.getYemekSuresi().doubleValue() / 60.0d : 1.0d, yemekMolasi = 0;
								if (yemekVardiyaSuresi > 0 && kgsHareket.getFazlaMesai() > 2.0d)
									yemekMolasi = ortakIslemler.getToplamYemekSuresi(yemekVardiyaSuresi, 0.0d, kgsHareket.getFazlaMesai());
								kgsHareket.setYemekMolasi(yemekMolasi);
							}
							if (vg.getId() == null) {
								giris = Boolean.TRUE;
								yaz = Boolean.TRUE;
							}
						}
						perMap.put(kgsHareket.getPersonelId(), kgsHareket.getPersonel().getPdksPersonel());

					}
					if (giris)
						personeller = new ArrayList<Personel>(perMap.values());

				}
			} catch (Exception ex) {
				ortakIslemler.loggerErrorYaz(sayfaURL, ex);
				throw new Exception(ex);
			}

		}
		manuelGiris = null;
		manuelCikis = null;
		if (!kgsList1.isEmpty()) {
			HashMap<String, KapiView> manuelKapiMap = ortakIslemler.getManuelKapiMap(null, session);
			manuelGiris = manuelKapiMap.get(Kapi.TIPI_KODU_GIRIS);
			manuelCikis = manuelKapiMap.get(Kapi.TIPI_KODU_CIKIS);
			manuelKapiMap = null;
		}
		setHareketList(kgsList1);

	}

	@Transactional
	private void saveLastParameter() {
		LinkedHashMap<String, Object> lastMap = new LinkedHashMap<String, Object>();
		ortakIslemler.saveAramaSecenekleri(aramaSecenekleri, lastMap);
		if (date != null)
			lastMap.put("date", PdksUtil.convertToDateString(date, "yyyy-MM-dd"));

		try {
			lastMap.put("sayfaURL", sayfaURL);
			ortakIslemler.saveLastParameter(lastMap, session);
		} catch (Exception e) {

		}
		lastMap = null;
	}

	/**
	 * @param kapiView
	 * @return
	 */
	private KapiView manuelKapiKontrol(KapiView kapiView) {
		if (kapiView != null && manuelGiris != null && manuelCikis != null) {
			Kapi pdksKapi = kapiView.getKapi();
			if (pdksKapi != null) {
				try {
					if (pdksKapi.isGirisKapi())
						kapiView = manuelGiris;
					else if (pdksKapi.isCikisKapi())
						kapiView = manuelCikis;
				} catch (Exception e) {
				}

			}
		}
		return kapiView;
	}

	/**
	 * @param kgsHareket
	 * @param tarih
	 */
	private boolean setTatilDurum(HareketKGS kgsHareket, Date zaman) {
		boolean tatilMi = false;
		if (zaman != null && !tatilMap.isEmpty() && tatilMap.containsKey(PdksUtil.convertToDateString(zaman, "yyyyMMdd"))) {
			Tatil hareketTatil = tatilMap.get(PdksUtil.convertToDateString(zaman, "yyyyMMdd"));
			Date basTarih = hareketTatil.getBasTarih(), bitTarih = hareketTatil.getBitTarih();
			if (hareketTatil.getOrjTatil() != null && hareketTatil.getOrjTatil().getBitTarih() != null)
				bitTarih = hareketTatil.getOrjTatil().getBitTarih();
			tatilMi = zaman != null && zaman.getTime() >= basTarih.getTime() && zaman.getTime() <= bitTarih.getTime();
			if (kgsHareket != null)
				kgsHareket.setTatil(tatilMi);
			else if (tatilMi)
				tatilMesai = hareketTatil;

		}
		return tatilMi;
	}

	/**
	 * @param kgsList
	 * @param kgsHareket
	 */
	private void fazlaMesaiEkle(List<HareketKGS> kgsList, HareketKGS hareket) {
		try {
			if (fazlaMesaiSistemOnayDurum != null && !tatilMap.isEmpty() && hareket.getGirisHareket() != null && hareket.getCikisHareket() == null) {
				tatilMesai = null;
				boolean girisTatilDurum = setTatilDurum(null, hareket.getGirisZaman()), cikisTatilDurum = setTatilDurum(null, hareket.getCikisZaman());
				if (tatilMesai != null) {
					if (!cikisTatilDurum) {
						if (girisTatilDurum)
							bayramMesaiAyir(kgsList, hareket, girisTatilDurum);

					} else if (!girisTatilDurum)
						bayramMesaiAyir(kgsList, hareket, girisTatilDurum);
				}

			} else if (hareket.isTatil()) {
				Date zaman = hareket.getZaman();
				if (zaman != null && !tatilMap.isEmpty()) {
					hareket.setTatil(tatilMap.containsKey(PdksUtil.convertToDateString(zaman, "yyyyMMdd")));
					if (hareket.isTatil() && hareket.getCikisHareket() != null) {
						boolean girisTatilDurum = setTatilDurum(null, hareket.getGirisZaman()), cikisTatilDurum = setTatilDurum(null, hareket.getCikisZaman());
						if (!cikisTatilDurum) {
							hareket.setGirisHareket(hareket);
							if (girisTatilDurum)
								bayramMesaiAyir(kgsList, hareket, girisTatilDurum);

						} else if (!girisTatilDurum)
							bayramMesaiAyir(kgsList, hareket, girisTatilDurum);

					}

				}
			}

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		kgsList.add(hareket);

	}

	/**
	 * @param kgsList
	 * @param hareket
	 * @param bayramBitti
	 */
	private void bayramMesaiAyir(List<HareketKGS> kgsList, HareketKGS hareket, Boolean bayramBitti) {
		Calendar cal = Calendar.getInstance();
		double fazlaMesai = hareket.getFazlaMesai();
		if (fazlaMesai > 0) {
			Date orjCikisZaman = (Date) hareket.getCikisZaman().clone();
			Date orjGirisZaman = (Date) hareket.getGirisZaman().clone();
			Date girisZaman = PdksUtil.getDate(orjCikisZaman);
			Date cikisZaman = PdksUtil.convertToJavaDate(PdksUtil.convertToDateString(ortakIslemler.tariheGunEkleCikar(cal, orjCikisZaman, -1), "yyyyMMdd") + " 23:59:00", "yyyyMMdd HH:mm:ss");
			if (!bayramBitti) {
				girisZaman = tatilMesai.getBasTarih();
				cikisZaman = PdksUtil.convertToJavaDate(PdksUtil.convertToDateString(ortakIslemler.tariheGunEkleCikar(cal, orjCikisZaman, -1), "yyyyMMdd") + " 23:59:00", "yyyyMMdd HH:mm:ss");
				if (tatilMesai.isYarimGunMu() && PdksUtil.tarihKarsilastirNumeric(orjGirisZaman, tatilMesai.getBasTarih()) == 0) {
					cikisZaman = PdksUtil.addTarih(girisZaman, Calendar.MINUTE, -1);
				}

			}
			sistemAdminUser = ortakIslemler.getSistemAdminUser(session);
			HareketKGS cikis = hareket.getCikisHareket() != null ? hareket.getCikisHareket() : hareket;
			Long hareketTableId = pdksEntityController.hareketEkle(manuelKapiKontrol(cikis.getKapiView()), hareket.getPersonel(), cikisZaman, sistemAdminUser, fazlaMesaiSistemOnayDurum.getId(), "", session);
			if (hareketTableId != null) {
				HashMap parametreMap = new HashMap();

				parametreMap.put("hareketTableId", hareketTableId);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

				List<HareketKGS> list = null;
				try {
					HareketKGS hareketKGS = new HareketKGS();
					hareketKGS.setId(HareketKGS.GIRIS_ISLEM_YAPAN_SIRKET_PDKS + hareketTableId);
					hareketKGS.setHareketTableId(hareketTableId);
					list = ortakIslemler.getHareketIdBilgileri(null, hareketKGS, date, date, session);
				} catch (Exception e) {
					list = new ArrayList<HareketKGS>();
					e.printStackTrace();
				}
				HareketKGS kgsHareketCikis = !list.isEmpty() ? list.get(0) : null;
				List<YemekIzin> yemekList = hareket.getVardiyaGun().getYemekList();
				if (kgsHareketCikis != null) {
					HareketKGS kgsHareketGiris = hareket.getGirisHareket() != null ? hareket.getGirisHareket() : hareket;
					pdksEntityController.hareketEkle(manuelKapiKontrol(kgsHareketGiris.getKapiView()), hareket.getPersonel(), girisZaman, sistemAdminUser, fazlaMesaiSistemOnayDurum.getId(), "", session);
					kgsHareketCikis.setVardiyaGun(hareket.getVardiyaGun());
					double saat = ortakIslemler.getSaatSure(girisZaman, orjCikisZaman, yemekList, hareket.getVardiyaGun(), session);
					kgsHareketCikis.setGirisZaman(orjGirisZaman);
					kgsHareketCikis.setCikisZaman(cikisZaman);
					kgsHareketCikis.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(fazlaMesai - saat, yuvarmaTipi));
					kgsHareketCikis.setTatil(bayramBitti);
					kgsList.add(kgsHareketCikis);
					hareket.setGirisZaman(girisZaman);
					hareket.setFazlaMesai(PdksUtil.setSureDoubleTypeRounded(saat, yuvarmaTipi));
					hareket.setTatil(!bayramBitti);
				}
			}

		}
	}

	public String mesaiSec(HareketKGS hareket) {
		setSeciliHareket(hareket);
		return "";
	}

	@Transactional
	public String mesaiSil() {

		PersonelFazlaMesai mesai = (PersonelFazlaMesai) pdksEntityController.getSQLParamByFieldObject(PersonelFazlaMesai.TABLE_NAME, PersonelFazlaMesai.COLUMN_NAME_ID, seciliHareket.getPersonelFazlaMesai().getId(), PersonelFazlaMesai.class, session);
		if (mesai != null) {
			try {
				mesai.setGuncelleyenUser(authenticatedUser);
				mesai.setGuncellemeTarihi(new Date());
				mesai.setDurum(Boolean.FALSE);
				saveOrUpdate(mesai);
				sessionFlush();
				fillHareketMesaiList();
			} catch (Exception e) {
			}

		}
		return "";
	}

	public void vardiyaGoster(VardiyaGun gun) {
		this.vardiyaGun = gun;
	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
	}

	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getMesaiSaati() {
		return mesaiSaati;
	}

	public void setMesaiSaati(Date mesaiSaati) {
		this.mesaiSaati = mesaiSaati;
	}

	public List<Tanim> getFazlaMesaiList() {
		return fazlaMesaiList;
	}

	public void setFazlaMesaiList(List<Tanim> fazlaMesaiList) {
		this.fazlaMesaiList = fazlaMesaiList;
	}

	public List<Tanim> getOnaylamamaNedeniList() {
		return onaylamamaNedeniList;
	}

	public void setOnaylamamaNedeniList(List<Tanim> onaylamamaNedeniList) {
		this.onaylamamaNedeniList = onaylamamaNedeniList;
	}

	public VardiyaGun getVardiyaGun() {
		return vardiyaGun;
	}

	public void setVardiyaGun(VardiyaGun vardiyaGun) {
		this.vardiyaGun = vardiyaGun;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public Tanim getSeciliEkSaha1() {
		return seciliEkSaha1;
	}

	public void setSeciliEkSaha1(Tanim seciliEkSaha1) {
		this.seciliEkSaha1 = seciliEkSaha1;
	}

	public Tanim getSeciliEkSaha2() {
		return seciliEkSaha2;
	}

	public void setSeciliEkSaha2(Tanim seciliEkSaha2) {
		this.seciliEkSaha2 = seciliEkSaha2;
	}

	public Tanim getSeciliEkSaha3() {
		return seciliEkSaha3;
	}

	public void setSeciliEkSaha3(Tanim seciliEkSaha3) {
		this.seciliEkSaha3 = seciliEkSaha3;
	}

	public Tanim getSeciliEkSaha4() {
		return seciliEkSaha4;
	}

	public void setSeciliEkSaha4(Tanim seciliEkSaha4) {
		this.seciliEkSaha4 = seciliEkSaha4;
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

	public String getDonusAdres() {
		return donusAdres;
	}

	public void setDonusAdres(String donusAdres) {
		this.donusAdres = donusAdres;
	}

	public Tatil getTatil() {
		return tatil;
	}

	public void setTatil(Tatil tatil) {
		this.tatil = tatil;
	}

	public DenklestirmeAy getDenklestirmeAy() {
		return pdksDenklestirmeAy;
	}

	public void setDenklestirmeAy(DenklestirmeAy pdksDenklestirmeAy) {
		this.pdksDenklestirmeAy = pdksDenklestirmeAy;
	}

	public HareketKGS getSeciliHareket() {
		return seciliHareket;
	}

	public void setSeciliHareket(HareketKGS seciliHareket) {
		this.seciliHareket = seciliHareket;
	}

	public TreeMap<String, Tatil> getTatilMap() {
		return tatilMap;
	}

	public void setTatilMap(TreeMap<String, Tatil> tatilMap) {
		this.tatilMap = tatilMap;
	}

	public Tanim getFazlaMesaiSistemOnayDurum() {
		return fazlaMesaiSistemOnayDurum;
	}

	public void setFazlaMesaiSistemOnayDurum(Tanim fazlaMesaiSistemOnayDurum) {
		this.fazlaMesaiSistemOnayDurum = fazlaMesaiSistemOnayDurum;
	}

	public User getSistemAdminUser() {
		return sistemAdminUser;
	}

	public void setSistemAdminUser(User sistemAdminUser) {
		this.sistemAdminUser = sistemAdminUser;
	}

	public List<Tanim> getBolumList() {
		return bolumList;
	}

	public void setBolumList(List<Tanim> bolumList) {
		this.bolumList = bolumList;
	}

	public Departman getDepartman() {
		return departman;
	}

	public void setDepartman(Departman departman) {
		this.departman = departman;
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

	public Sirket getPdksSirket() {
		return pdksSirket;
	}

	public void setPdksSirket(Sirket pdksSirket) {
		this.pdksSirket = pdksSirket;
	}

	public List<SelectItem> getPdksSirketList() {
		return pdksSirketList;
	}

	public void setPdksSirketList(List<SelectItem> pdksSirketList) {
		this.pdksSirketList = pdksSirketList;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public List<SelectItem> getBolumDepartmanlari() {
		return bolumDepartmanlari;
	}

	public void setBolumDepartmanlari(List<SelectItem> bolumDepartmanlari) {
		this.bolumDepartmanlari = bolumDepartmanlari;
	}

	public Long getSeciliEkSaha3Id() {
		return seciliEkSaha3Id;
	}

	public void setSeciliEkSaha3Id(Long seciliEkSaha3Id) {
		this.seciliEkSaha3Id = seciliEkSaha3Id;
	}

	public Tatil getTatilMesai() {
		return tatilMesai;
	}

	public void setTatilMesai(Tatil tatilMesai) {
		this.tatilMesai = tatilMesai;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public boolean isDenklestirmeAyDurum() {
		return denklestirmeAyDurum;
	}

	public void setDenklestirmeAyDurum(boolean denklestirmeAyDurum) {
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

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Long getSeciliEkSaha4Id() {
		return seciliEkSaha4Id;
	}

	public void setSeciliEkSaha4Id(Long seciliEkSaha4Id) {
		this.seciliEkSaha4Id = seciliEkSaha4Id;
	}

	public boolean isFazlaMesaiGirisDurum() {
		return fazlaMesaiGirisDurum;
	}

	public void setFazlaMesaiGirisDurum(boolean fazlaMesaiGirisDurum) {
		this.fazlaMesaiGirisDurum = fazlaMesaiGirisDurum;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		PersonelFazlaMesaiHome.sayfaURL = sayfaURL;
	}

}
