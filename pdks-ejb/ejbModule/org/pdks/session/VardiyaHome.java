package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliVardiya;
import org.pdks.entity.CalismaSekli;
import org.pdks.entity.Departman;
import org.pdks.entity.Liste;
import org.pdks.entity.Sirket;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.entity.VardiyaYemekIzin;
import org.pdks.entity.YemekIzin;
import org.pdks.enums.BordroDetayTipi;
import org.pdks.enums.KatSayiTipi;
import org.pdks.security.entity.User;

@Name("vardiyaHome")
public class VardiyaHome extends EntityHome<Vardiya> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5118807122083827640L;
	static Logger logger = Logger.getLogger(VardiyaHome.class);
	@RequestParameter
	Long pdksVardiyaId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	HashMap parameterMap;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false)
	FacesMessages facesMessages;

	public static String sayfaURL = "vardiyaTanimlama";
	private List<String> saatList = new ArrayList<String>();
	private List<String> dakikaList = new ArrayList<String>();
	private List<String> toleransDakikaList = new ArrayList<String>();
	private List<SelectItem> vardiyaTipiList, departmanIdList, calismaSekliIdList, sirketIdList, pdksSirketIdList, izinTipiList;
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>(), izinCalismaVardiyaList = new ArrayList<Vardiya>();
	private List<VardiyaSablonu> sablonList = new ArrayList<VardiyaSablonu>();

	private List<CalismaModeli> calismaModeliList = new ArrayList<CalismaModeli>(), calismaModeliKayitliList = new ArrayList<CalismaModeli>();
	private List<YemekIzin> yemekIzinList = new ArrayList<YemekIzin>(), yemekIzinKayitliList = new ArrayList<YemekIzin>();

	private List<YemekIzin> yemekList;
	private Long seciliSirketId;
	private int saat = 13, dakika = 0;
	private Boolean sirketGoster = Boolean.FALSE, icapVardiyaGoster = Boolean.FALSE, sutIzniGoster = Boolean.FALSE, gebelikGoster = Boolean.FALSE, suaGoster = Boolean.FALSE;

	private boolean pasifGoster = Boolean.FALSE, manuelVardiyaIzinGir = false;

	private boolean yemekMolaKontrolEt = false, erkenGirisKontrolEt = false, erkenCikisKontrolEt = false, gecCikisKontrolEt = false, gecGirisKontrolEt = false;
	private Session session;

	@Override
	public Object getId() {
		if (pdksVardiyaId == null) {
			return super.getId();
		} else {
			return pdksVardiyaId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public String izinVardiyalariGetir(Vardiya pdksVardiya) {
		if (sirketIdList == null)
			sirketIdList = new ArrayList<SelectItem>();
		else
			sirketIdList.clear();
		Departman departman = null;
		if (pdksVardiya.getDepartmanId() != null) {
			departman = new Departman();
			departman.setId(pdksVardiya.getDepartmanId());
		}
		List<Sirket> sirketList = ortakIslemler.getDepartmanPDKSSirketList(departman, session);
		for (Sirket sirket : sirketList) {
			sirketIdList.add(new SelectItem(sirket.getId(), sirket.getAd()));
		}
		fillCalismaModeliList(pdksVardiya);
		return "";
	}

	public void fillYemekList(Vardiya pdksVardiya) {
		if (pdksVardiya.getId() == null || pdksVardiya.isCalisma()) {
			yemekIzinList = ortakIslemler.getYemekList(new Date(), null, session);
			if (pdksVardiya.getId() != null) {
				HashMap parametreMap = new HashMap();
				parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "yemekIzin");
				parametreMap.put("vardiya.id", pdksVardiya.getId());
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				yemekIzinKayitliList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaYemekIzin.class);
				if (yemekIzinKayitliList.size() > 1)
					yemekIzinKayitliList = PdksUtil.sortListByAlanAdi(yemekIzinKayitliList, "yemekNumeric", false);
			} else
				yemekIzinKayitliList = new ArrayList<YemekIzin>();
			for (Iterator iterator = yemekIzinList.iterator(); iterator.hasNext();) {
				YemekIzin veri = (YemekIzin) iterator.next();
				if (veri.getOzelMola() == null || veri.getOzelMola().equals(Boolean.FALSE))
					iterator.remove();
				else {
					for (Iterator iterator2 = yemekIzinKayitliList.iterator(); iterator2.hasNext();) {
						YemekIzin kayit = (YemekIzin) iterator2.next();
						if (kayit.getId().equals(veri.getId())) {
							iterator.remove();
							break;
						}
					}
				}
			}
		} else {
			yemekIzinList = new ArrayList<YemekIzin>();
			yemekIzinKayitliList = new ArrayList<YemekIzin>();
		}

	}

	/**
	 * @param pdksVardiya
	 */
	public void fillCalismaModeliList(Vardiya pdksVardiya) {
		if (pdksVardiya.getId() == null || pdksVardiya.isCalisma()) {
			Long pdksDepartmanId = pdksVardiya.getDepartmanId() != null ? pdksVardiya.getDepartmanId() : null;
			Sirket sirket = pdksVardiya.getSirketId() != null ? new Sirket(pdksVardiya.getSirketId()) : null;
			calismaModeliList = ortakIslemler.getCalismaModeliList(sirket, pdksDepartmanId, false, session);

			if (pdksDepartmanId != null) {
				for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
					CalismaModeli cm = (CalismaModeli) iterator.next();
					if (cm.getDepartman() != null && !cm.getDepartman().getId().equals(pdksDepartmanId))
						iterator.remove();
				}
			}
			if (pdksVardiya.getId() != null) {
				calismaModeliKayitliList = ortakIslemler.fillCalismaModeliVardiyaList(pdksVardiya, session);
				if (calismaModeliKayitliList.size() > 1)
					calismaModeliKayitliList = PdksUtil.sortObjectStringAlanList(calismaModeliKayitliList, "getAciklama", null);
			} else
				calismaModeliKayitliList = new ArrayList<CalismaModeli>();
			for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
				CalismaModeli veri = (CalismaModeli) iterator.next();
				for (Iterator iterator2 = calismaModeliKayitliList.iterator(); iterator2.hasNext();) {
					CalismaModeli kayit = (CalismaModeli) iterator2.next();
					if (kayit.getId().equals(veri.getId())) {
						iterator.remove();
						break;
					}
				}
			}
			if (calismaModeliList.size() > 1)
				calismaModeliList = PdksUtil.sortObjectStringAlanList(calismaModeliList, "getAciklama", null);
		} else {
			calismaModeliList = new ArrayList<CalismaModeli>();
			calismaModeliKayitliList = new ArrayList<CalismaModeli>();
		}
	}

	public void fillBagliOlduguDepartmanTanimList() {
		List<Departman> tanimList = null;
		if (departmanIdList == null)
			departmanIdList = new ArrayList<SelectItem>();
		else
			departmanIdList.clear();
		HashMap parametreMap = new HashMap();
		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			tanimList = ortakIslemler.fillDepartmanTanimList(session);
			for (Departman departman : tanimList) {
				departmanIdList.add(new SelectItem(departman.getId(), departman.getAciklama()));
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		} finally {
			parametreMap = null;
		}

	}

	/**
	 * @param vardiya
	 * @return
	 */
	public String getArifeMesaiAciklama(Vardiya vardiya) {
		String title = "";
		if (vardiya != null) {
			try {
				Date tarih = PdksUtil.getDate(new Date());
				Calendar cal = Calendar.getInstance();
				cal.setTime(tarih);
				VardiyaGun tmp = new VardiyaGun(authenticatedUser.getPdksPersonel(), vardiya, tarih);
				tmp.setVardiyaZamani();
				Vardiya islemVardiya = tmp.getIslemVardiya();
				cal.set(Calendar.HOUR_OF_DAY, saat);
				cal.set(Calendar.MINUTE, dakika);
				Date arifeBaslangicTarihi = cal.getTime();
				Date bitisTarihi = null;
				CalismaSekli calismaSekli = vardiya.getCalismaSekli();
				Double arifeNormalCalismaDakika = null;
				if (vardiya.isCalisma() && (calismaSekli != null || (vardiya.getArifeNormalCalismaDakika() != null && vardiya.getArifeNormalCalismaDakika() > 0.0d))) {
					arifeNormalCalismaDakika = calismaSekli != null ? calismaSekli.getArifeNormalCalismaDakika() : null;
					if (vardiya.getArifeNormalCalismaDakika() != null && vardiya.getArifeNormalCalismaDakika() > 0.0d)
						arifeNormalCalismaDakika = vardiya.getArifeNormalCalismaDakika();
					if (arifeNormalCalismaDakika != null) {
						cal.setTime(islemVardiya.getVardiyaBasZaman());
						cal.add(Calendar.MINUTE, arifeNormalCalismaDakika.intValue());
						arifeBaslangicTarihi = cal.getTime();
					}
					bitisTarihi = islemVardiya.getVardiyaBitZaman();
				} else {
					if (vardiya.isCalisma()) {
						if (arifeBaslangicTarihi.before(islemVardiya.getVardiyaBasZaman()))
							arifeBaslangicTarihi = islemVardiya.getVardiyaBasZaman();
						bitisTarihi = islemVardiya.getVardiyaBitZaman();
					}

					else
						bitisTarihi = ortakIslemler.tariheGunEkleCikar(cal, tarih, 1);
				}
				List yemekArifeList = new ArrayList();
				islemVardiya.setArifeBaslangicTarihi(arifeBaslangicTarihi);
				Double sure = ortakIslemler.getSaatSure(arifeBaslangicTarihi, bitisTarihi, yemekArifeList, tmp, session), netSure = vardiya.isCalisma() ? islemVardiya.getNetCalismaSuresi() : 0;
				if (netSure > 0 && sure > netSure)
					sure = netSure;
				tmp = null;
				title = "Tatil başlama saati : " + authenticatedUser.timeFormatla(arifeBaslangicTarihi) + (sure > 0 ? "<br></br>Tatil süresi : " + authenticatedUser.sayiFormatliGoster(sure) : "");
				if (calismaSekli != null || (arifeNormalCalismaDakika != null && arifeNormalCalismaDakika > 0.0d)) {
					if (vardiya.isCalisma())
						sure = ortakIslemler.getSaatSure(islemVardiya.getVardiyaBasZaman(), arifeBaslangicTarihi, yemekList, tmp, session);
					else
						sure = 0.0d;
					title += (sure > 0 ? "<br></br>Çalışma süresi : " + authenticatedUser.sayiFormatliGoster(sure) : "");

					title += (sure > 0 ? "<br></br>Eklenen süre : " + authenticatedUser.sayiFormatliGoster(arifeNormalCalismaDakika / 60d) : "");
				}
				if (netSure > 0.0d)
					title += (sure > 0 ? "<br></br><b>Net süre : " + authenticatedUser.sayiFormatliGoster(netSure) + "</b>" : "");
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		return title;
	}

	/**
	 * @param pdksVardiya
	 * @return
	 */
	public String kayitKopyala(Vardiya pdksVardiya) {
		vardiyaAlanlariDoldur(pdksVardiya);
		Vardiya pdksVardiyaYeni = (Vardiya) pdksVardiya.clone();
		if (authenticatedUser.isIK())
			pdksVardiyaYeni.setDepartman(authenticatedUser.getDepartman());
		pdksVardiyaYeni.setId(null);
		if (pdksVardiya.getKisaAdi() != null)
			pdksVardiyaYeni.setKisaAdi(pdksVardiya.getKisaAdi() + " kopya");
		pdksVardiyaYeni.setAdi(pdksVardiya.getAdi() + " kopya");

		setInstance(pdksVardiyaYeni);
		return "";

	}

	/**
	 * @param pdksVardiya
	 */
	public void kayitGuncelle(Vardiya pdksVardiya) {
		if (pdksVardiya == null) {
			pdksVardiya = new Vardiya();
			if (seciliSirketId != null) {
				pdksVardiya.setSirketId(seciliSirketId);
				Sirket seciliSirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, seciliSirketId, Sirket.class, session);
				pdksVardiya.setDepartmanId(seciliSirket.getDepartman().getId());
			}

			pdksVardiya.setTipi(String.valueOf(Vardiya.TIPI_CALISMA));
			pdksVardiya.setBasDakika((short) 0);
			pdksVardiya.setBitDakika((short) 0);
			pdksVardiya.setGirisErkenToleransDakika((short) 0);
			pdksVardiya.setGirisGecikmeToleransDakika((short) 0);
			pdksVardiya.setCikisErkenToleransDakika((short) 0);
			pdksVardiya.setCikisGecikmeToleransDakika((short) 0);
		}
		vardiyaAlanlariDoldur(pdksVardiya);

		setInstance(pdksVardiya);
	}

	/**
	 * @param pdksVardiya
	 */
	private void vardiyaAlanlariDoldur(Vardiya pdksVardiya) {
		setInstance(pdksVardiya);
		fillSaatler();
		fillSablonlar();
		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
		fillYemekList(pdksVardiya);
		fillCalismaModeliList(pdksVardiya);
		pdksVardiya.setTipi(String.valueOf(pdksVardiya.getVardiyaTipi()));
		fillCalismaSekilleri();
		fillVardiyaTipiList(pdksVardiya);
		if (sirketIdList == null)
			sirketIdList = new ArrayList<SelectItem>();
		else
			sirketIdList.clear();
		List<Sirket> sirketList = ortakIslemler.getDepartmanPDKSSirketList(pdksVardiya.getDepartman(), session);
		for (Sirket sirket : sirketList) {
			sirketIdList.add(new SelectItem(sirket.getId(), sirket.getAd()));
		}
	}

	@Transactional
	public String save() {
		String cikis = "";
		Vardiya pdksVardiya = getInstance();
		if (pdksVardiya.getId() == null && !authenticatedUser.isAdmin())
			pdksVardiya.setDepartman(authenticatedUser.getDepartman());
		boolean yaz = Boolean.TRUE;
		try {
			if (!pdksVardiya.isCalisma()) {
				pdksVardiya.setBasDakika((short) 0);
				pdksVardiya.setBasSaat((short) 0);
				pdksVardiya.setBitDakika((short) 0);
				pdksVardiya.setBitSaat((short) 0);
				pdksVardiya.setGirisErkenToleransDakika((short) 0);
				pdksVardiya.setGirisGecikmeToleransDakika((short) 0);
				pdksVardiya.setCikisErkenToleransDakika((short) 0);
				pdksVardiya.setCikisGecikmeToleransDakika((short) 0);
				if (!pdksVardiya.isIzin()) {
					pdksVardiya.setCalismaGun(0);
					pdksVardiya.setCalismaSaati(0);
				}
			}
			if (yaz) {
				HashMap parametreMap = new HashMap();
				User islemYapan = (User) pdksEntityController.getSQLParamByFieldObject(User.TABLE_NAME, User.COLUMN_NAME_ID, authenticatedUser.getId(), User.class, session);

				if (pdksVardiya.getId() == null) {
					pdksVardiya.setOlusturmaTarihi(new Date());
					pdksVardiya.setOlusturanUser(islemYapan);
				}

				else {
					pdksVardiya.setGuncelleyenUser(islemYapan);
					pdksVardiya.setGuncellemeTarihi(new Date());
				}
				if (!pdksVardiya.isCalisma())
					pdksVardiya.setMesaiOde(null);
				else if (pdksVardiya.getAksamVardiya() != null && pdksVardiya.getAksamVardiya().booleanValue())
					pdksVardiya.setAksamVardiya(pdksVardiya.getBasDonem() >= pdksVardiya.getBitDonem());
				if (pdksVardiya.getGenel() != null && pdksVardiya.getGenel()) {
					pdksVardiya.setIcapVardiya(Boolean.FALSE);
					pdksVardiya.setSua(Boolean.FALSE);
					pdksVardiya.setGebelik(Boolean.FALSE);
					pdksVardiya.setSutIzni(Boolean.FALSE);
				}
				if (pdksVardiya.getSirket() != null)
					pdksVardiya.setDepartman(pdksVardiya.getSirket().getDepartman());
				pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiya);
				if (calismaModeliList.size() + calismaModeliKayitliList.size() > 0) {
					parametreMap.clear();
					parametreMap.put("vardiya.id", pdksVardiya.getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<CalismaModeliVardiya> kayitliCalismaModeliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
					for (Iterator iterator = calismaModeliKayitliList.iterator(); iterator.hasNext();) {
						CalismaModeli kayitli = (CalismaModeli) iterator.next();
						boolean ekle = true;
						for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
							CalismaModeliVardiya vyi = (CalismaModeliVardiya) iterator2.next();
							if (vyi.getCalismaModeli().getId().equals(kayitli.getId())) {
								ekle = false;
								iterator2.remove();
								break;
							}
						}
						if (ekle) {
							CalismaModeliVardiya vyi = new CalismaModeliVardiya(pdksVardiya, kayitli);
							pdksEntityController.saveOrUpdate(session, entityManager, vyi);
						}
					}
					for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
						CalismaModeliVardiya vyi = (CalismaModeliVardiya) iterator2.next();
						pdksEntityController.deleteObject(session, entityManager, vyi);
					}
					kayitliCalismaModeliVardiyaList = null;
				}
				if (yemekIzinList.size() + yemekIzinKayitliList.size() > 0) {
					parametreMap.clear();
					parametreMap.put("vardiya.id", pdksVardiya.getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<VardiyaYemekIzin> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaYemekIzin.class);
					for (Iterator iterator = yemekIzinKayitliList.iterator(); iterator.hasNext();) {
						YemekIzin kayitli = (YemekIzin) iterator.next();
						boolean ekle = true;
						for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
							VardiyaYemekIzin vyi = (VardiyaYemekIzin) iterator2.next();
							if (vyi.getYemekIzin().getId().equals(kayitli.getId())) {
								ekle = false;
								iterator2.remove();
								break;
							}

						}
						if (ekle) {
							VardiyaYemekIzin vyi = new VardiyaYemekIzin(pdksVardiya, kayitli);
							pdksEntityController.saveOrUpdate(session, entityManager, vyi);
						}
					}
					for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
						VardiyaYemekIzin vyi = (VardiyaYemekIzin) iterator2.next();
						pdksEntityController.deleteObject(session, entityManager, vyi);
					}
					list = null;
				}
				session.flush();
				fillVardiyalar();
				cikis = "persisted";
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			PdksUtil.addMessageWarn(e.getMessage());
		}

		return cikis;

	}

	public void fillSaatler() {

		List<String> saatListesi = PdksUtil.getSayilar(0, 24, 1);
		List<String> dakikaListesi = PdksUtil.getSayilar(0, 60, 5);
		List<String> toleransDakikaList = PdksUtil.getSayilar(0, 20, 5);

		setSaatList(saatListesi);
		setDakikaList(dakikaListesi);
		setToleransDakikaList(toleransDakikaList);
	}

	public void fillVardiyalar() {
		icapVardiyaGoster = Boolean.FALSE;
		sutIzniGoster = Boolean.FALSE;
		gebelikGoster = Boolean.FALSE;
		suaGoster = Boolean.FALSE;
		sirketGoster = Boolean.FALSE;
		HashMap parametreMap = new HashMap();
		try {
			manuelVardiyaIzinGir = ortakIslemler.getVardiyaIzinGir(session, authenticatedUser.getDepartman());
			List<Vardiya> vardiyalar = pdksEntityController.getSQLParamByFieldList(Vardiya.TABLE_NAME, pasifGoster == false ? Vardiya.COLUMN_NAME_DURUM : null, Boolean.TRUE, Vardiya.class, session);
			if (seciliSirketId != null) {
				Sirket seciliSirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, seciliSirketId, Sirket.class, session);
				Departman seciliDepartman = seciliSirket.getDepartman();
				for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
					Vardiya vardiya = (Vardiya) iterator.next();
					Sirket sirket = vardiya.getSirket();
					Departman departman = vardiya.getDepartman();
					boolean sil = false;
					if (departman != null && !seciliDepartman.getId().equals(departman.getId()))
						sil = true;
					if (sirket != null && !seciliSirket.getId().equals(sirket.getId()))
						sil = true;

					if (sil)
						iterator.remove();
				}

			}
			erkenGirisKontrolEt = false;
			erkenCikisKontrolEt = false;
			gecCikisKontrolEt = false;
			gecGirisKontrolEt = false;
			if (vardiyalar.isEmpty() == false) {
				Date bugun = PdksUtil.buGun();
				HashMap<KatSayiTipi, TreeMap<String, BigDecimal>> allMap = ortakIslemler.getVardiyaKatSayiAllMap(bugun, session);
				TreeMap<String, BigDecimal> yemekMolaMap = allMap.containsKey(KatSayiTipi.VARDIYA_MOLA) ? allMap.get(KatSayiTipi.VARDIYA_MOLA) : null;
				TreeMap<String, BigDecimal> erkenGirisMap = allMap.containsKey(KatSayiTipi.ERKEN_GIRIS_TIPI) ? allMap.get(KatSayiTipi.ERKEN_GIRIS_TIPI) : null;
				TreeMap<String, BigDecimal> erkenCikisMap = allMap.containsKey(KatSayiTipi.ERKEN_CIKIS_TIPI) ? allMap.get(KatSayiTipi.ERKEN_CIKIS_TIPI) : null;
				TreeMap<String, BigDecimal> gecGirisMap = allMap.containsKey(KatSayiTipi.GEC_GIRIS_TIPI) ? allMap.get(KatSayiTipi.GEC_GIRIS_TIPI) : null;
				TreeMap<String, BigDecimal> gecCikisMap = allMap.containsKey(KatSayiTipi.GEC_CIKIS_TIPI) ? allMap.get(KatSayiTipi.GEC_CIKIS_TIPI) : null;
				yemekMolaKontrolEt = yemekMolaMap != null && !yemekMolaMap.isEmpty();
				erkenGirisKontrolEt = erkenGirisMap != null && !erkenGirisMap.isEmpty();
				erkenCikisKontrolEt = erkenCikisMap != null && !erkenCikisMap.isEmpty();
				gecCikisKontrolEt = gecCikisMap != null && !gecCikisMap.isEmpty();
				gecGirisKontrolEt = gecGirisMap != null && !gecGirisMap.isEmpty();
				String str = PdksUtil.convertToDateString(bugun, "yyyyMMdd");
				for (Vardiya vardiya : vardiyalar) {
					HashMap<Integer, BigDecimal> katSayiMap = null;
					if (vardiya.isCalisma()) {
						katSayiMap = new HashMap<Integer, BigDecimal>();
						Long sirketId = vardiya.getSirket() != null ? vardiya.getSirket().getId() : null, tesisId = null, vardiyaId = vardiya.getId();
						if (yemekMolaKontrolEt && ortakIslemler.veriKatSayiVar(yemekMolaMap, sirketId, tesisId, vardiyaId, str)) {
							BigDecimal deger = ortakIslemler.getKatSayiVeriMap(yemekMolaMap, sirketId, tesisId, vardiyaId, str);
							if (deger != null)
								katSayiMap.put(KatSayiTipi.VARDIYA_MOLA.value(), deger);
						}
						if (erkenGirisKontrolEt && ortakIslemler.veriKatSayiVar(erkenGirisMap, sirketId, tesisId, vardiyaId, str)) {
							BigDecimal deger = ortakIslemler.getKatSayiVeriMap(erkenGirisMap, sirketId, tesisId, vardiyaId, str);
							if (deger != null)
								katSayiMap.put(KatSayiTipi.ERKEN_GIRIS_TIPI.value(), deger);
						}
						if (erkenCikisKontrolEt && ortakIslemler.veriKatSayiVar(erkenCikisMap, sirketId, tesisId, vardiyaId, str)) {
							BigDecimal deger = ortakIslemler.getKatSayiVeriMap(erkenCikisMap, sirketId, tesisId, vardiyaId, str);
							if (deger != null)
								katSayiMap.put(KatSayiTipi.ERKEN_CIKIS_TIPI.value(), deger);
						}
						if (gecGirisKontrolEt && ortakIslemler.veriKatSayiVar(gecGirisMap, sirketId, tesisId, vardiyaId, str)) {
							BigDecimal deger = ortakIslemler.getKatSayiVeriMap(gecGirisMap, sirketId, tesisId, vardiyaId, str);
							if (deger != null)
								katSayiMap.put(KatSayiTipi.GEC_GIRIS_TIPI.value(), deger);
						}
						if (gecCikisKontrolEt && ortakIslemler.veriKatSayiVar(gecCikisMap, sirketId, tesisId, vardiyaId, str)) {
							BigDecimal deger = ortakIslemler.getKatSayiVeriMap(gecCikisMap, sirketId, tesisId, vardiyaId, str);
							if (deger != null)
								katSayiMap.put(KatSayiTipi.GEC_CIKIS_TIPI.value(), deger);
						}
						if (katSayiMap.isEmpty() == false)
							vardiya.setKatSayiMap(katSayiMap);
						else {
							katSayiMap = null;
						}

					}
					if (katSayiMap == null)
						logger.info(vardiya.getAdi() + " " + vardiya.getKisaAdi());

				}
			}
			yemekList = ortakIslemler.getYemekList(new Date(), null, session);
			HashMap<Long, Vardiya> vardiyaMap = new HashMap<Long, Vardiya>();
			Calendar cal = Calendar.getInstance();
			VardiyaGun vardiyaGun = new VardiyaGun();
			vardiyaGun.setVardiyaDate(PdksUtil.getDate(cal.getTime()));
			// boolean yemekTanimsiz = yemekList == null || yemekList.isEmpty();
			for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
				Vardiya vardiya = (Vardiya) iterator.next();
				Double yarimGunSure = vardiya.getArifeNormalCalismaDakika() != null && vardiya.getArifeNormalCalismaDakika().doubleValue() > 0.0 ? vardiya.getArifeNormalCalismaDakika() : null;
				if (yarimGunSure == null) {
					yarimGunSure = vardiya.getNetCalismaSuresi() * 30;
				}
				if (vardiya.getDurum() && vardiya.isCalisma()) {

					vardiyaGun.setVardiya(vardiya);
					vardiyaGun.setIslemVardiya(null);
					vardiyaGun.setVardiyaZamani();
					Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
					cal.setTime(islemVardiya.getVardiyaBasZaman());
					cal.add(Calendar.MINUTE, yarimGunSure.intValue());
					Date arifeBaslangicTarihi = cal.getTime();
					Double sure = ortakIslemler.getSaatSure(arifeBaslangicTarihi, islemVardiya.getVardiyaBitZaman(), yemekList, vardiyaGun, session) - (vardiya.getNetCalismaSuresi() * 0.5d);
					if (authenticatedUser.isAdmin() && sure.doubleValue() != 0.0d)
						logger.debug(vardiya.getId() + " --> " + vardiya.getKisaAdi() + " : " + yarimGunSure + " " + sure);
				}
				vardiya.setYemekIzinList(null);
				if (vardiya.getDurum() && vardiya.isCalisma())
					vardiyaMap.put(vardiya.getId(), vardiya);
			}
			if (!vardiyaMap.isEmpty()) {
				try {
					String fieldName = "vardiya.id";
					List idList = new ArrayList(vardiyaMap.keySet());
					parametreMap.clear();
					parametreMap.put(fieldName, idList);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					List<VardiyaYemekIzin> vardiyaYemekIzinList = ortakIslemler.getParamList(false, idList, fieldName, parametreMap, VardiyaYemekIzin.class, session);
					if (!vardiyaYemekIzinList.isEmpty()) {
						if (vardiyaYemekIzinList.size() > 1)
							vardiyaYemekIzinList = PdksUtil.sortListByAlanAdi(vardiyaYemekIzinList, "yemekNumeric", true);
						for (Iterator iterator = vardiyaYemekIzinList.iterator(); iterator.hasNext();) {
							VardiyaYemekIzin vardiyaYemekIzin = (VardiyaYemekIzin) iterator.next();
							YemekIzin yemekIzin = vardiyaYemekIzin.getYemekIzin();
							if (yemekIzin.getOzelMola() != null && yemekIzin.getOzelMola()) {
								Vardiya vardiya = vardiyaMap.get(vardiyaYemekIzin.getVardiya().getId());
								if (vardiya.getYemekIzinList() == null)
									vardiya.setYemekIzinList(new ArrayList<YemekIzin>());
								vardiya.getYemekIzinList().add(yemekIzin);
							}

						}
					}
					vardiyaYemekIzinList = null;
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}

			}
			if (!authenticatedUser.isAdmin()) {
				List<Vardiya> digerVardiyalar = new ArrayList<Vardiya>();
				if (!vardiyalar.isEmpty())
					vardiyalar = PdksUtil.sortListByAlanAdi(vardiyalar, "sonIslemTarihi", Boolean.TRUE);
				for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
					Vardiya pdksVardiya = (Vardiya) iterator.next();
					if (pdksVardiya.getDepartman() == null || !authenticatedUser.getDepartman().getId().equals(pdksVardiya.getDepartman().getId())) {
						iterator.remove();
						if (pdksVardiya.getDepartman() != null && authenticatedUser.isIKAdmin())
							digerVardiyalar.add(pdksVardiya);
					}

				}
				if (!vardiyalar.isEmpty())
					vardiyalar = PdksUtil.sortListByAlanAdi(vardiyalar, "sonIslemTarihi", Boolean.TRUE);
				if (!digerVardiyalar.isEmpty())
					vardiyalar.addAll(digerVardiyalar);
				digerVardiyalar = null;
			}
			vardiyaList.clear();
			if (!vardiyalar.isEmpty()) {
				vardiyalar = PdksUtil.sortObjectStringAlanList(vardiyalar, "getKisaAdiSort", null);
				TreeMap<String, List<Vardiya>> map = new TreeMap<String, List<Vardiya>>();
				for (Iterator iterator = vardiyalar.iterator(); iterator.hasNext();) {
					Vardiya vardiya = (Vardiya) iterator.next();
					if (authenticatedUser.isIK() && (!vardiya.isCalisma())) {
						iterator.remove();
						continue;
					}

					if (vardiya.getDurum()) {
						if (!vardiya.isCalisma() && vardiya.getDepartman() == null) {
							vardiyaList.add(vardiya);
						} else {
							String key = vardiya.getDepartman() != null ? vardiya.getDepartman().getId().toString() : "";
							List<Vardiya> list = map.containsKey(key) ? map.get(key) : new ArrayList<Vardiya>();
							if (list.isEmpty())
								map.put(key, list);
							list.add(vardiya);
						}
						iterator.remove();
					}

				}
				for (String key : map.keySet())
					vardiyaList.addAll(map.get(key));

				if (!vardiyalar.isEmpty())
					vardiyaList.addAll(vardiyalar);
			}
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				Vardiya vardiya = (Vardiya) iterator.next();
				if (icapVardiyaGoster.booleanValue() == false)
					icapVardiyaGoster = vardiya.isIcapVardiyasi();
				if (sutIzniGoster.booleanValue() == false)
					sutIzniGoster = vardiya.isSutIzniMi();
				if (gebelikGoster.booleanValue() == false)
					gebelikGoster = vardiya.isGebelikMi();
				if (suaGoster.booleanValue() == false)
					suaGoster = vardiya.isSuaMi();
				if (sirketGoster.booleanValue() == false)
					sirketGoster = vardiya.getSirket() != null;

			}

			saat = 13;
			dakika = 0;
			String yarimGunStr = (parameterMap.containsKey("yarimGunSaati") ? (String) parameterMap.get("yarimGunSaati") : "");
			if (yarimGunStr.indexOf(":") > 0) {
				StringTokenizer st = new StringTokenizer(yarimGunStr, ":");
				if (st.countTokens() == 2) {
					try {
						saat = Integer.parseInt(st.nextToken().trim());
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						saat = 13;
					}
					try {
						dakika = Integer.parseInt(st.nextToken().trim());
					} catch (Exception e) {
						logger.error("Pdks hata in : \n");
						e.printStackTrace();
						logger.error("Pdks hata out : " + e.getMessage());
						saat = 13;
						dakika = 0;
					}
				}
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("Hata : fillVardiyalar " + e.getMessage() + " " + PdksUtil.getCurrentTimeStampStr());
		} finally {
			parametreMap = null;
		}

	}

	public String excelAktar() {
		try {

			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, ortakIslemler.vardiyaAciklama() + (pasifGoster == false ? "Aktif" : "") + "Listesi.xlsx");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "";
	}

	private ByteArrayOutputStream excelDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		String vardiyaAciklama = ortakIslemler.vardiyaAciklama();
		try {

			Sheet sheet = ExcelUtil.createSheet(wb, vardiyaAciklama + (pasifGoster == false ? "Aktif" : "") + " Listesi", false);
			// Drawing drawing = sheet.createDrawingPatriarch();
			// CreationHelper helper = wb.getCreationHelper();
			// ClientAnchor anchor = helper.createClientAnchor();
			CellStyle header = ExcelUtil.getStyleHeader(wb);
			CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
			CellStyle styleOddRed = ExcelUtil.getStyleOdd(null, wb);
			ExcelUtil.setFontColor(styleOddRed, Color.RED);
			CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
			CellStyle styleOddSayi = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATA_NUMBER, wb);
			CellStyle styleOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATA_TUTAR, wb);
			CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
			CellStyle styleEvenRed = ExcelUtil.getStyleOdd(null, wb);
			ExcelUtil.setFontColor(styleEvenRed, Color.RED);
			CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
			CellStyle styleEvenSayi = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATA_NUMBER, wb);
			CellStyle styleEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATA_TUTAR, wb);
			int row = 0;
			int col = 0;
			boolean admin = authenticatedUser.isAdmin();
			boolean ikAdmin = admin || authenticatedUser.isIKAdmin() || authenticatedUser.isSistemYoneticisi();
			// boolean hastane = getParameterKey("uygulamaTipi").equalsIgnoreCase("H");
			if (admin)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(vardiyaAciklama + " Id");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(vardiyaAciklama + " Adı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(vardiyaAciklama + " Kısa Adı");
			if (sirketGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Ekran Sıra");

			if (admin)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(vardiyaAciklama + " Sınıf Adı");
			if (ikAdmin)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.firmaKaynagiAciklama());

			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(vardiyaAciklama + " Tipi");
			if (ikAdmin)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Net Çalışma Süresi");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Toplam Saat");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çalışma Şekli");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Arife Normal Çalışma Süresi");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Gün Sayısı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çalışma Aralığı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Yemek Süresi (Dakika)");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tolerans erken giriş dakikası");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tolerans gecikme giriş dakikası");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tolerans erken çıkış dakikası");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tolerans gecikme çıkış dakikası");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çıkış Mola Süresi(Dakika)");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Akşam " + vardiyaAciklama);
			if (ikAdmin)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Genel");
			if (ikAdmin && icapVardiyaGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İcap " + vardiyaAciklama);
			if (ikAdmin && suaGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Şua " + vardiyaAciklama);
			if (ikAdmin && gebelikGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Gebe " + vardiyaAciklama);
			if (ikAdmin && sutIzniGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süt İzini " + vardiyaAciklama);
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("FÇS Ödenir");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Mesai Ödenir");
			TreeMap<Long, List<CalismaModeli>> cMap = new TreeMap<Long, List<CalismaModeli>>();
			if (admin) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.calismaModeliAciklama());
				List<Long> idList = new ArrayList<Long>();
				for (Vardiya vardiya : vardiyaList) {
					if (vardiya.isCalisma() && vardiya.getGenel())
						idList.add(vardiya.getId());

				}
				if (idList.isEmpty() == false) {
					List<CalismaModeliVardiya> list = pdksEntityController.getSQLParamByFieldList(CalismaModeliVardiya.TABLE_NAME, CalismaModeliVardiya.COLUMN_NAME_VARDIYA, idList, CalismaModeliVardiya.class, session);
					if (list.isEmpty() == false) {
						List<Liste> list2 = new ArrayList<Liste>();
						for (CalismaModeliVardiya cmv : list) {
							CalismaModeli calismaModeli = cmv.getCalismaModeli();
							if (calismaModeli != null && calismaModeli.getDurum()) {
								list2.add(new Liste(cmv.getVardiya().getId() + "_" + calismaModeli.getAciklama(), cmv));
							}

						}
						if (list2.isEmpty() == false) {
							list2 = PdksUtil.sortObjectStringAlanList(list2, "getId", null);
							for (Liste liste : list2) {
								CalismaModeliVardiya cmv = (CalismaModeliVardiya) liste.getValue();
								Long key = cmv.getVardiya().getId();
								List<CalismaModeli> calismaModeliList = cMap.containsKey(key) ? cMap.get(key) : new ArrayList<CalismaModeli>();
								if (calismaModeliList.isEmpty())
									cMap.put(key, calismaModeliList);
								calismaModeliList.add(cmv.getCalismaModeli());
							}
						}
						list2 = null;
					}
					list = null;
				}
				idList = null;
			}
			if (pasifGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Aktif");
			boolean renk = true;

			for (Vardiya vardiya : vardiyaList) {
				col = 0;
				row++;
				CellStyle style = null, styleCenter = null, cellStyleTutar = null, cellStyleSayi = null;
				if (renk) {
					cellStyleTutar = styleOddTutar;
					cellStyleSayi = styleOddSayi;
					style = styleOdd;
					styleCenter = styleOddCenter;

				} else {
					cellStyleTutar = styleEvenTutar;
					cellStyleSayi = styleEvenSayi;
					style = styleEven;
					styleCenter = styleEvenCenter;

				}
				renk = !renk;
				boolean calisma = vardiya.isCalisma();
				if (admin)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getId());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiya.getAdi());
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(vardiya.getKisaAdi());
				if (sirketGoster)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiya.getSirket() != null ? vardiya.getSirket().getAd() : "");
				ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getEkranSira());

				if (admin)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiya.getStyleClass() != null ? vardiya.getStyleClass().trim() : "");
				if (ikAdmin)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiya.getDepartman() != null ? vardiya.getDepartman().getDepartmanTanim().getAciklama() : "");

				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiya.getVardiyaTipiAciklama());
				if (ikAdmin) {
					if (calisma)
						ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(vardiya.getNetCalismaSuresi());
					else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

				}
				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(vardiya.getCalismaSaati());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiya.getCalismaSekli() != null ? vardiya.getCalismaSekli().getAdi() : "");
				if (calisma && vardiya.getArifeNormalCalismaDakika() != null && vardiya.getArifeNormalCalismaDakika().doubleValue() > 0.0d)
					ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(vardiya.getArifeNormalCalismaDakika());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getCalismaGun());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.timeFormatla(vardiya.getBasZaman()) + " - " + authenticatedUser.timeFormatla(vardiya.getBitZaman()));
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getYemekSuresi());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getGirisErkenToleransDakika());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getGirisGecikmeToleransDakika());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getCikisErkenToleransDakika());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getCikisGecikmeToleransDakika());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (calisma)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(vardiya.getCikisMolaSaat());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.getAksamVardiya()));
				if (ikAdmin)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.getGenel()));
				if (ikAdmin && icapVardiyaGoster)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.isIcapVardiyasi()));

				if (ikAdmin && suaGoster)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.isSuaMi()));

				if (ikAdmin && gebelikGoster)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.isGebelikMi()));

				if (ikAdmin && sutIzniGoster)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.isSutIzniMi()));
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.isFcsDahil()));
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.isMesaiOdenir()));

				if (admin) {
					StringBuffer cmAciklama = new StringBuffer();
					if (calisma && cMap.containsKey(vardiya.getId())) {
						List<CalismaModeli> list = cMap.get(vardiya.getId());
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							CalismaModeli cm = (CalismaModeli) iterator.next();
							cmAciklama.append(cm.getAciklama());
							if (iterator.hasNext())
								cmAciklama.append(", ");
						}
						list = null;
					}
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(cmAciklama.toString());
					cmAciklama = null;
				}
				if (pasifGoster)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(vardiya.getDurum()));
			}
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return baos;
	}

	public void fillCalismaSekilleri() {
		HashMap parametreMap = new HashMap();
		if (calismaSekliIdList == null)
			calismaSekliIdList = new ArrayList<SelectItem>();
		else
			calismaSekliIdList.clear();
		try {
			parametreMap.put("durum", Boolean.TRUE);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<CalismaSekli> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaSekli.class);
			for (CalismaSekli calismaSekli : list) {
				calismaSekliIdList.add(new SelectItem(calismaSekli.getId(), calismaSekli.getAdi()));
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("Hata : fillSablonlar " + e.getMessage() + " " + PdksUtil.getCurrentTimeStampStr());
		} finally {
			parametreMap = null;
		}

	}

	public void fillSablonlar() {
		HashMap parametreMap = new HashMap();
		try {
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<VardiyaSablonu> sablonList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaSablonu.class);
			setSablonList(sablonList);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("Hata : fillSablonlar " + e.getMessage() + " " + PdksUtil.getCurrentTimeStampStr());
		} finally {
			parametreMap = null;
		}

	}

	public void instanceRefresh() {
		Vardiya pdksVardiya = getInstance();
		if (pdksVardiya.getId() != null)
			try {
				session.refresh(pdksVardiya);
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

			}

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		pasifGoster = false;
		List<Sirket> pdksSirketList = ortakIslemler.getDepartmanPDKSSirketList(null, session);
		if (pdksSirketIdList == null)
			pdksSirketIdList = new ArrayList<SelectItem>();
		else
			pdksSirketIdList.clear();

		for (Sirket sirket : pdksSirketList) {
			pdksSirketIdList.add(new SelectItem(sirket.getId(), sirket.getAd()));
		}
		fillVardiyalar();

	}

	/**
	 * @param vardiya
	 */
	public void fillVardiyaTipiList(Vardiya vardiya) {
		List<SelectItem> list = ortakIslemler.getSelectItemList("vardiyaTipi", authenticatedUser);
		list.add(new SelectItem(String.valueOf(Vardiya.TIPI_CALISMA), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_CALISMA, null)));
		list.add(new SelectItem(String.valueOf(Vardiya.TIPI_HAFTA_TATIL), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_HAFTA_TATIL, "HT")));
		list.add(new SelectItem(String.valueOf(Vardiya.TIPI_OFF), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_OFF, "OFF")));
		boolean adminUser = ortakIslemler.getAdminRole(authenticatedUser);
		if (adminUser) {
			String vardiyaTipi = vardiya.getId() != null ? String.valueOf(vardiya.getVardiyaTipi()) : "";
			if (ortakIslemler.getParameterKey("uygulamaTipi").equals("H"))
				list.add(new SelectItem(String.valueOf(Vardiya.TIPI_RADYASYON_IZNI), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_RADYASYON_IZNI, null)));
			if (authenticatedUser.isAdmin()) {
				if (ortakIslemler.getParameterKey("fazlaMesaiIzinKullan").equals("1") || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_FMI)))
					list.add(new SelectItem(String.valueOf(Vardiya.TIPI_FMI), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_FMI, "Fazla Mesai İzin")));
				boolean izinGiris = manuelVardiyaIzinGir || PdksUtil.getCanliSunucuDurum() == false;
				if (izinGiris || (vardiyaTipi.equals("") && vardiya.getId() == null) || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_IZIN)) || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_HASTALIK_RAPOR))) {
					if (izinGiris || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_IZIN)))
						list.add(new SelectItem(String.valueOf(Vardiya.TIPI_IZIN), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_IZIN, "İzin Tatil Hariç")));
					if (izinGiris || vardiyaTipi.equals(String.valueOf(Vardiya.TIPI_HASTALIK_RAPOR)))
						list.add(new SelectItem(String.valueOf(Vardiya.TIPI_HASTALIK_RAPOR), Vardiya.getVardiyaTipiAciklama(Vardiya.TIPI_HASTALIK_RAPOR, "İzin Tatil Dahil")));

				}
			}

		}
		if (izinTipiList == null)
			izinTipiList = new ArrayList<SelectItem>();
		izinTipiList.clear();
		if (vardiya.getId() == null || vardiya.isIzin()) {
			izinTipiList.add(new SelectItem(BordroDetayTipi.UCRETLI_IZIN.value(), "Ücretli İzin"));
			izinTipiList.add(new SelectItem(BordroDetayTipi.UCRETSIZ_IZIN.value(), "Ücretsiz İzin"));
			izinTipiList.add(new SelectItem(BordroDetayTipi.RAPORLU_IZIN.value(), "Raporlu İzin"));
		}
		setVardiyaTipiList(list);
	}

	public List<SelectItem> getVardiyaTipiList() {
		return vardiyaTipiList;
	}

	public void setVardiyaTipiList(List<SelectItem> vardiyaTipiList) {
		this.vardiyaTipiList = vardiyaTipiList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<VardiyaSablonu> getSablonList() {
		return sablonList;
	}

	public void setSablonList(List<VardiyaSablonu> sablonList) {
		this.sablonList = sablonList;
	}

	public List<String> getSaatList() {
		return saatList;
	}

	public void setSaatList(List<String> saatList) {
		this.saatList = saatList;
	}

	public List<String> getDakikaList() {
		return dakikaList;
	}

	public void setDakikaList(List<String> dakikaList) {
		this.dakikaList = dakikaList;
	}

	public List<String> getToleransDakikaList() {
		return toleransDakikaList;
	}

	public void setToleransDakikaList(List<String> toleransDakikaList) {
		this.toleransDakikaList = toleransDakikaList;
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<CalismaModeli> getCalismaModeliList() {
		return calismaModeliList;
	}

	public void setCalismaModeliList(List<CalismaModeli> calismaModeliList) {
		this.calismaModeliList = calismaModeliList;
	}

	public List<YemekIzin> getYemekIzinList() {
		return yemekIzinList;
	}

	public void setYemekIzinList(List<YemekIzin> yemekIzinList) {
		this.yemekIzinList = yemekIzinList;
	}

	public List<YemekIzin> getYemekList() {
		return yemekList;
	}

	public void setYemekList(List<YemekIzin> yemekList) {
		this.yemekList = yemekList;
	}

	public int getSaat() {
		return saat;
	}

	public void setSaat(int saat) {
		this.saat = saat;
	}

	public int getDakika() {
		return dakika;
	}

	public void setDakika(int dakika) {
		this.dakika = dakika;
	}

	public List<CalismaModeli> getCalismaModeliKayitliList() {
		return calismaModeliKayitliList;
	}

	public void setCalismaModeliKayitliList(List<CalismaModeli> calismaModeliKayitliList) {
		this.calismaModeliKayitliList = calismaModeliKayitliList;
	}

	public List<YemekIzin> getYemekIzinKayitliList() {
		return yemekIzinKayitliList;
	}

	public void setYemekIzinKayitliList(List<YemekIzin> yemekIzinKayitliList) {
		this.yemekIzinKayitliList = yemekIzinKayitliList;
	}

	public boolean isManuelVardiyaIzinGir() {
		return manuelVardiyaIzinGir;
	}

	public void setManuelVardiyaIzinGir(boolean manuelVardiyaIzinGir) {
		this.manuelVardiyaIzinGir = manuelVardiyaIzinGir;
	}

	public List<Vardiya> getIzinCalismaVardiyaList() {
		return izinCalismaVardiyaList;
	}

	public void setIzinCalismaVardiyaList(List<Vardiya> izinCalismaVardiyaList) {
		this.izinCalismaVardiyaList = izinCalismaVardiyaList;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		VardiyaHome.sayfaURL = sayfaURL;
	}

	public boolean isPasifGoster() {
		return pasifGoster;
	}

	public void setPasifGoster(boolean pasifGoster) {
		this.pasifGoster = pasifGoster;
	}

	public List<SelectItem> getDepartmanIdList() {
		return departmanIdList;
	}

	public void setDepartmanIdList(List<SelectItem> departmanIdList) {
		this.departmanIdList = departmanIdList;
	}

	public List<SelectItem> getCalismaSekliIdList() {
		return calismaSekliIdList;
	}

	public void setCalismaSekliIdList(List<SelectItem> calismaSekliIdList) {
		this.calismaSekliIdList = calismaSekliIdList;
	}

	public List<SelectItem> getSirketIdList() {
		return sirketIdList;
	}

	public void setSirketIdList(List<SelectItem> sirketIdList) {
		this.sirketIdList = sirketIdList;
	}

	public List<SelectItem> getPdksSirketIdList() {
		return pdksSirketIdList;
	}

	public void setPdksSirketIdList(List<SelectItem> pdksSirketIdList) {
		this.pdksSirketIdList = pdksSirketIdList;
	}

	public Long getSeciliSirketId() {
		return seciliSirketId;
	}

	public void setSeciliSirketId(Long seciliSirketId) {
		this.seciliSirketId = seciliSirketId;
	}

	public List<SelectItem> getIzinTipiList() {
		return izinTipiList;
	}

	public void setIzinTipiList(List<SelectItem> izinTipiList) {
		this.izinTipiList = izinTipiList;
	}

	public Boolean getIcapVardiyaGoster() {
		return icapVardiyaGoster;
	}

	public void setIcapVardiyaGoster(Boolean icapVardiyaGoster) {
		this.icapVardiyaGoster = icapVardiyaGoster;
	}

	public Boolean getSutIzniGoster() {
		return sutIzniGoster;
	}

	public void setSutIzniGoster(Boolean sutIzniGoster) {
		this.sutIzniGoster = sutIzniGoster;
	}

	public Boolean getGebelikGoster() {
		return gebelikGoster;
	}

	public void setGebelikGoster(Boolean gebelikGoster) {
		this.gebelikGoster = gebelikGoster;
	}

	public Boolean getSuaGoster() {
		return suaGoster;
	}

	public void setSuaGoster(Boolean suaGoster) {
		this.suaGoster = suaGoster;
	}

	public Boolean getSirketGoster() {
		return sirketGoster;
	}

	public void setSirketGoster(Boolean sirketGoster) {
		this.sirketGoster = sirketGoster;
	}

	public boolean isErkenGirisKontrolEt() {
		return erkenGirisKontrolEt;
	}

	public void setErkenGirisKontrolEt(boolean erkenGirisKontrolEt) {
		this.erkenGirisKontrolEt = erkenGirisKontrolEt;
	}

	public boolean isErkenCikisKontrolEt() {
		return erkenCikisKontrolEt;
	}

	public void setErkenCikisKontrolEt(boolean erkenCikisKontrolEt) {
		this.erkenCikisKontrolEt = erkenCikisKontrolEt;
	}

	public boolean isGecCikisKontrolEt() {
		return gecCikisKontrolEt;
	}

	public void setGecCikisKontrolEt(boolean gecCikisKontrolEt) {
		this.gecCikisKontrolEt = gecCikisKontrolEt;
	}

	public boolean isGecGirisKontrolEt() {
		return gecGirisKontrolEt;
	}

	public void setGecGirisKontrolEt(boolean gecGirisKontrolEt) {
		this.gecGirisKontrolEt = gecGirisKontrolEt;
	}
}
