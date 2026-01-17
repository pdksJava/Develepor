package org.pdks.quartz;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Liste;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("planVardiyaHareketGuncelleme")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class PlanVardiyaHareketGuncelleme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8751158477032830258L;

	static Logger logger = Logger.getLogger(PlanVardiyaHareketGuncelleme.class);

	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = false, create = true)
	Zamanlayici zamanlayici;

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;

	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	public static final String PARAMETER_HAREKET_KEY = "hareketVardiyaZamani";
	public static final String PARAMETER_FAZLA_MESAI_KEY = "fazlaMesaiHesaplamaZamani";
	public static final String PATTERN = "yyyyMMdd";

	private static boolean calisiyor = Boolean.FALSE;

	private Date bugun;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle planVardiyaHareketGuncellemeTimer(@Expiration Date when, @IntervalCron String interval) {
		if (!isCalisiyor()) {
			setCalisiyor(Boolean.TRUE);
			logger.debug("planVardiyaHareketGuncelleme in " + PdksUtil.getCurrentTimeStampStr());
			Session session = null;
			try {
				if (PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum()) {
					Calendar cal = Calendar.getInstance();
					bugun = cal.getTime();
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					boolean fazlaMesaiHesaplaDurum = ortakIslemler.getParameterKey("sirketFazlaMesaiGuncelleme").equals("1");
					Parameter parameterHareket = getParameter(PARAMETER_HAREKET_KEY, session);
					Parameter parameterFazlaMesaiHesaplama = getParameter(PARAMETER_FAZLA_MESAI_KEY, session);
					if (parameterHareket != null || parameterFazlaMesaiHesaplama != null || fazlaMesaiHesaplaDurum) {
						Date tarih = PdksUtil.getDate(bugun);
						Date basTarih = bugun;
						String konu = null, aciklama = null;
						if (parameterHareket != null && parameterFazlaMesaiHesaplama == null) {
							boolean guncellemeHareketDurum = vardiyaHareketGuncelleme(tarih, session);
							if (guncellemeHareketDurum) {
								konu = parameterHareket.getDescription();
								aciklama = "Plan Vardiya Hareket Güncelleme güncellenmiştir.";
							}
						}
						if (parameterFazlaMesaiHesaplama != null || (parameterHareket != null && fazlaMesaiHesaplaDurum)) {
							basTarih = ortakIslemler.getBugun();
							if (fazlaMesaiGuncelleme(tarih, session) != null) {
								konu = parameterFazlaMesaiHesaplama != null ? parameterFazlaMesaiHesaplama.getDescription() : "Fazla Mesai Toplu Güncelleme";
								aciklama = "Fazla Mesai güncellenmiştir.";
							}
						}
						if (PdksUtil.hasStringValue(konu)) {
							boolean mailGonder = true;
							if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
								String key = PdksUtil.convertToDateString(cal.getTime(), PATTERN);
								Date bitTarih = PdksUtil.tariheGunEkleCikar(cal.getTime(), 1);
								TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(null, basTarih, bitTarih, session);
								mailGonder = tatilMap.containsKey(key) == false;
							}
							if (mailGonder) {
								aciklama = aciklama + "<br></br><br></br><b>Start Time : </b>" + PdksUtil.convertToDateString(basTarih, PdksUtil.getDateTimeLongFormat());
								aciklama = aciklama + "<br></br><b>Stop Time  : </b>" + PdksUtil.convertToDateString(ortakIslemler.getBugun(), PdksUtil.getDateTimeLongFormat()) + "<br></br>";
								zamanlayici.mailGonder(session, null, konu, aciklama, null, Boolean.TRUE);
							}

						}
					}
				}
			} catch (Exception e) {
				logger.error("PDKS hata in : \n" + e.getMessage() + " " + PdksUtil.getCurrentTimeStampStr());
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
			} finally {
				if (session != null)
					session.close();
				setCalisiyor(Boolean.FALSE);

			}
			logger.debug("planVardiyaHareketGuncelleme out " + PdksUtil.getCurrentTimeStampStr());
		}

		return null;
	}

	@Transactional
	private void vardiyaBaslatGuncelleme(Date basTarih, Session session) throws Exception {
		if (basTarih == null)
			basTarih = ortakIslemler.getBugun();
		Date bitTarih = PdksUtil.tariheAyEkleCikar(basTarih, 2);
		HashMap fields = new HashMap();
		fields.put("t1", basTarih);
		fields.put("t2", bitTarih);
		StringBuffer sb = new StringBuffer();

		sb.append(" with PLAN_VERI as ( ");
		sb.append(" select V.* from " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK());
		sb.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb.append(" and (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ")");
		sb.append(" where V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + "  between :t1 and :t2");
		sb.append(" and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 0 and V." + VardiyaGun.COLUMN_NAME_VERSION + " = 0");
		sb.append(" )");
		sb.append(" select P.* FROM PLAN_VERI P");
		sb.append(" inner join " + DenklestirmeAy.TABLE_NAME + " D  " + PdksEntityController.getJoinLOCK() + " on D." + DenklestirmeAy.COLUMN_NAME_YIL + " = year(P." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ")");
		sb.append(" and D." + DenklestirmeAy.COLUMN_NAME_AY + " = month(P." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ") and D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1");
		sb.append(" inner join " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getJoinLOCK() + " on PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = P." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb.append(" and PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" inner join " + CalismaModeliAy.TABLE_NAME + " C " + PdksEntityController.getJoinLOCK() + " on C." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY + " and C." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1");
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<VardiyaGun> vardiyaGunList = pdksEntityController.getObjectBySQLList(sb.toString(), fields, VardiyaGun.class);
		if (vardiyaGunList.isEmpty() == false) {
			boolean flush = false;
			User guncelleyenUser = ortakIslemler.getSistemAdminUser(session);
			Date guncellemeTarihi = new Date();
			HashMap<Long, List<VardiyaGun>> vMap = new HashMap<Long, List<VardiyaGun>>();
			for (VardiyaGun vg : vardiyaGunList) {
				Long perId = vg.getPdksPersonel().getId();
				List<VardiyaGun> list = vMap.containsKey(perId) ? vMap.get(perId) : new ArrayList<VardiyaGun>();
				if (list.isEmpty())
					vMap.put(perId, list);
				list.add(vg);

			}
			HashMap<Long, List<PersonelIzin>> izinMap = ortakIslemler.getPersonelIzinMap(new ArrayList<Long>(vMap.keySet()), basTarih, bitTarih, session);
			if (izinMap.isEmpty() == false) {
				for (Long key : izinMap.keySet()) {
					if (vMap.containsKey(key))
						ortakIslemler.vardiyaIzinleriGuncelle(izinMap.get(key), vMap.get(key));

				}
			}
			izinMap = null;
			for (VardiyaGun vardiyaGun : vardiyaGunList) {
				if (vardiyaGun.isIzinli())
					continue;
				int version = vardiyaGun.getVersion();
				Vardiya vardiya = vardiyaGun.getVardiya();
				if (version == 0) {
					if (vardiya.isHaftaTatil() == false) {
						vardiyaGun.setVersion(-1);
						vardiyaGun.setGuncellemeTarihi(guncellemeTarihi);
						vardiyaGun.setGuncelleyenUser(guncelleyenUser);
						pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
						flush = true;
					}
				} else if (version < 0) {
					if (vardiya.isHaftaTatil()) {
						vardiyaGun.setVersion(-1);
						vardiyaGun.setGuncellemeTarihi(guncellemeTarihi);
						vardiyaGun.setGuncelleyenUser(guncelleyenUser);
						pdksEntityController.saveOrUpdate(session, entityManager, vardiyaGun);
						flush = true;
					}

				}

			}
			if (flush)
				session.flush();
		}
		vardiyaGunList = null;
	}

	/**
	 * @param tarih
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public String fazlaMesaiGuncelleme(Date tarih, Session session) throws Exception {
		if (tarih == null)
			tarih = ortakIslemler.getBugun();
		String adresStr = null;
		Long buAy = Long.parseLong(PdksUtil.convertToDateString(tarih, "yyyyMM"));
		Long oncekiAy = Long.parseLong(PdksUtil.convertToDateString(PdksUtil.tariheAyEkleCikar(tarih, -1), "yyyyMM"));
		StringBuffer sb = new StringBuffer();
		HashMap fields = new HashMap();
		sb.append(" select distinct D." + DenklestirmeAy.COLUMN_NAME_ID + ", P." + Personel.COLUMN_NAME_SIRKET + ", S.AD, D.DONEM_KODU from " + DenklestirmeAy.TABLE_NAME + " D " + PdksEntityController.getSelectLOCK());
		sb.append(" left join " + PersonelDenklestirme.TABLE_NAME + " PD on PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" left join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		sb.append(" left join " + Sirket.TABLE_NAME + " S " + PdksEntityController.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET + " AND S." + Sirket.COLUMN_NAME_PDKS + " = 1");
		sb.append(" where (D." + DenklestirmeAy.COLUMN_NAME_DONEM_KODU + "  between :d1 and :d2 ) and D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1");
		sb.append(" order by D." + DenklestirmeAy.COLUMN_NAME_DONEM_KODU + ", S." + Sirket.COLUMN_NAME_AD);
		fields.put("d1", oncekiAy);
		fields.put("d2", buAy);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Object[]> veriler = pdksEntityController.getObjectBySQLList(sb.toString(), fields, null);
		if (veriler.isEmpty() == false) {
			LinkedHashMap<Long, List<Long>> veriMap = new LinkedHashMap<Long, List<Long>>();
			for (Object[] objects : veriler) {
				Long key = ((BigDecimal) objects[0]).longValue(), sirketId = objects[1] != null ? ((BigDecimal) objects[1]).longValue() : null;
				List<Long> sirketIdList = veriMap.containsKey(key) ? veriMap.get(key) : new ArrayList<Long>();
				if (sirketIdList.isEmpty())
					veriMap.put(key, sirketIdList);
				if (sirketId != null)
					sirketIdList.add(sirketId);
			}
			adresStr = ortakIslemler.getLoginAdres();
			if (PdksUtil.hasStringValue(adresStr)) {
				String adres = PdksUtil.replaceAllManuel(adresStr, "login", "denklestirmeBordroGuncelleme");
				logger.info(adres + " in " + PdksUtil.getCurrentTimeStampStr());
				User loginUser = ortakIslemler.getSistemAdminUser(session);
				loginUser.setAdmin(true);
				for (Long donemId : veriMap.keySet()) {
					DenklestirmeAy da = (DenklestirmeAy) pdksEntityController.getSQLParamByFieldObject(DenklestirmeAy.TABLE_NAME, DenklestirmeAy.COLUMN_NAME_ID, donemId, DenklestirmeAy.class, session);
					if (veriMap.size() > 1)
						logger.info(da.getAyAdi() + " " + da.getYil() + " in " + PdksUtil.getCurrentTimeStampStr());
					DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
					AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(da.getAy(), da.getYil(), denklestirmeDonemi, session);
					aylikPuantaj.setLoginUser(loginUser);
					aylikPuantaj.setDenklestirmeAy(da);
					List<SelectItem> departmanIdList = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(aylikPuantaj, false, session);
					for (SelectItem siDepartman : departmanIdList) {
						Long departmanId = (Long) siDepartman.getValue();
						List<SelectItem> sirketIdList = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, aylikPuantaj, false, session);
						if (sirketIdList.isEmpty() == false) {
							List<Long> idList = new ArrayList<Long>();
							for (SelectItem sirketSelectItem : sirketIdList)
								idList.add((Long) sirketSelectItem.getValue());
							List<Sirket> sirketList = pdksEntityController.getSQLParamByFieldList(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, idList, Sirket.class, session);
							if (sirketList.size() > 1)
								sirketList = PdksUtil.sortObjectStringAlanList(sirketList, "getAd", null);
							idList = null;
							for (Sirket sirket : sirketList) {
								String linkStr = "loginAdres=" + ortakIslemler.getEncodeStringByBase64(adresStr) + "&pdksUserId=" + loginUser.getId() + "&donemId=" + donemId + "&sirketId=" + sirket.getId();
								if (sirket.isTesisDurumu())
									linkStr = linkStr + "&tesisId=*";
								String id = ortakIslemler.getEncodeStringByBase64(linkStr);
								String sonuc = ortakIslemler.adresKontrol(adres + "?id=" + id);
								if (sonuc != null)
									logger.error(da.getAyAdi() + " " + da.getYil() + " " + sirket.getAd() + " hata =" + sonuc + " out " + PdksUtil.getCurrentTimeStampStr());
							}
							sirketList = null;
						}
						sirketIdList = null;
					}
					if (veriMap.size() > 1)
						logger.info(da.getAyAdi() + " " + da.getYil() + " out " + PdksUtil.getCurrentTimeStampStr());
				}
				logger.info(adres + " out " + PdksUtil.getCurrentTimeStampStr());
			}

		}
		return adresStr;

	}

	/**
	 * @param tarih
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public boolean vardiyaHareketGuncelleme(Date tarih, Session session) throws Exception {
		if (tarih == null)
			tarih = ortakIslemler.getBugun();
		Calendar cal = Calendar.getInstance();
		String dateStr = PdksUtil.convertToDateString(cal.getTime(), PATTERN);
		int dayOffWeek = cal.get(Calendar.DAY_OF_WEEK);
		cal.setTime(tarih);
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.MONTH, -1);
		Date basTarih = PdksUtil.getDate(cal.getTime());
		Date bitTarih = PdksUtil.getDate(tarih);
		vardiyaBaslatGuncelleme(basTarih, session);
		HashMap fields = new HashMap();
		fields.put("t1", basTarih);
		fields.put("t2", bitTarih);
		StringBuffer sb = new StringBuffer();
		sb.append(" with VERI as (");
		sb.append(" select distinct year(V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ") " + DenklestirmeAy.COLUMN_NAME_YIL + ", month(V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ") " + DenklestirmeAy.COLUMN_NAME_AY + ",");
		sb.append(" V." + VardiyaGun.COLUMN_NAME_PERSONEL + " from " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK());
		sb.append(" inner join PERSONEL P " + PdksEntityController.getJoinLOCK() + " on P.ID = V." + VardiyaGun.COLUMN_NAME_PERSONEL + " and  V.VARDIYA_TARIHI between P.ISE_BASLAMA_TARIHI and P.SSK_CIKIS_TARIHI");
		sb.append(" where (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between :t1 and :t2 ) and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 0 AND V." + VardiyaGun.COLUMN_NAME_VERSION + " < 0");
		sb.append(" )");
		sb.append(" select PD." + PersonelDenklestirme.COLUMN_NAME_ID + " from VERI V " + PdksEntityController.getSelectLOCK());
		sb.append(" inner join " + DenklestirmeAy.TABLE_NAME + " D on D." + DenklestirmeAy.COLUMN_NAME_YIL + " = V." + DenklestirmeAy.COLUMN_NAME_YIL + " AND D." + DenklestirmeAy.COLUMN_NAME_AY + " = V." + DenklestirmeAy.COLUMN_NAME_AY + " AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1");
		sb.append(" inner join " + PersonelDenklestirme.TABLE_NAME + " PD on PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL + "  and PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" and coalesce(PD." + PersonelDenklestirme.COLUMN_NAME_SUA_DURUM + ", 0) = 0");
		sb.append(" inner join " + CalismaModeliAy.TABLE_NAME + " C on C." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY + " and C." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1");
		sb.append(" order by D." + DenklestirmeAy.COLUMN_NAME_YIL + " desc, D." + DenklestirmeAy.COLUMN_NAME_AY + " desc, PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Long> idList = PdksUtil.getLongListFromBigDecimal(null, pdksEntityController.getObjectBySQLList(sb.toString(), fields, null));
		List<PersonelDenklestirme> denklestirmeList = null;
		if (idList.isEmpty() == false)
			denklestirmeList = pdksEntityController.getSQLParamByFieldList(PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.COLUMN_NAME_ID, idList, PersonelDenklestirme.class, session);
		idList = null;
		Boolean islemYapildi = null;
		if (denklestirmeList != null && denklestirmeList.isEmpty() == false) {
			TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(null, basTarih, bitTarih, session);
			LinkedHashMap<String, List<PersonelDenklestirme>> linkedHashMap = new LinkedHashMap<String, List<PersonelDenklestirme>>();
			HashMap<Long, Boolean> hareketKaydiVardiyaMap = new HashMap<Long, Boolean>();
			for (PersonelDenklestirme pd : denklestirmeList) {
				Long cmId = pd.getCalismaModeli().getId();
				if (hareketKaydiVardiyaMap.containsKey(cmId) == false)
					hareketKaydiVardiyaMap.put(cmId, true);
				DenklestirmeAy dm = pd.getDenklestirmeAy();
				String key = "" + dm.getDonemKodu();
				List<PersonelDenklestirme> list = linkedHashMap.containsKey(key) ? linkedHashMap.get(key) : new ArrayList<PersonelDenklestirme>();
				if (list.isEmpty())
					linkedHashMap.put(key, list);
				list.add(pd);
			}
			for (String key : linkedHashMap.keySet()) {
				tarih = PdksUtil.convertToJavaDate(key + "01", PATTERN);
				basTarih = PdksUtil.tariheGunEkleCikar(tarih, -6);
				bitTarih = PdksUtil.tariheGunEkleCikar(PdksUtil.tariheAyEkleCikar(tarih, 1), 5);
				List<PersonelDenklestirme> list1 = linkedHashMap.get(key);
				TreeMap<String, Liste> pdMap = new TreeMap<String, Liste>();
				DenklestirmeAy da = null;
				for (PersonelDenklestirme pd : list1) {
					da = pd.getDenklestirmeAy();
					Personel personel = pd.getPdksPersonel();
					String key1 = personel.getSirket().getAd() + "_" + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "") + "_" + (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "") + "_"
							+ (personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");
					Liste liste = pdMap.containsKey(key1) ? pdMap.get(key1) : new Liste(key1, new ArrayList<PersonelDenklestirme>());
					List<PersonelDenklestirme> list2 = (List<PersonelDenklestirme>) liste.getValue();
					if (list2.isEmpty())
						pdMap.put(key1, liste);
					list2.add(pd);

				}
				list1 = null;
				List<Liste> listeList = PdksUtil.sortObjectStringAlanList(new ArrayList(pdMap.values()), "getId", null);
				pdMap = null;
				for (Liste liste : listeList) {
					HashMap<Long, PersonelView> kgsPerMap = new HashMap<Long, PersonelView>();
					List<PersonelDenklestirme> list = (List<PersonelDenklestirme>) liste.getValue();
					TreeMap<Long, PersonelDenklestirmeTasiyici> personelDenklestirmeMap = new TreeMap<Long, PersonelDenklestirmeTasiyici>();
					List<Long> personelIdList = new ArrayList<Long>();
					Sirket sirket = null;
					Tanim tesis = null, bolum = null, altBolum = null;
					for (PersonelDenklestirme pd : list) {
						Personel personel = pd.getPdksPersonel();
						sirket = personel.getSirket();
						tesis = personel.getTesis();
						bolum = personel.getEkSaha3();
						altBolum = personel.getEkSaha4();
						PersonelKGS personelKGS = personel.getPersonelKGS();
						kgsPerMap.put(personelKGS.getId(), personelKGS.getPersonelView());
						Long perId = pd.getPersonelId();
						AylikPuantaj ap = new AylikPuantaj(da);
						ap.setPersonelDenklestirme(pd);
						ap.setPdksPersonel(ap.getPdksPersonel());
						PersonelDenklestirmeTasiyici pdt = new PersonelDenklestirmeTasiyici(ap);
						pdt.setVardiyaGunleriMap(new TreeMap<String, VardiyaGun>());

						personelDenklestirmeMap.put(perId, pdt);
						personelIdList.add(perId);
					}
					String str = da.getAyAdi() + " " + da.getYil() + " : "
							+ PdksUtil.replaceAllManuel(sirket.getAd() + " " + (tesis != null ? tesis.getAciklama() + " " : "") + (bolum != null ? bolum.getAciklama() + " " : "") + (altBolum != null ? altBolum.getAciklama() + " " : "") + " [ " + list.size() + " ]", "  ", " ");
					if (authenticatedUser != null)
						logger.info(str + " in " + PdksUtil.getCurrentTimeStampStr());
					HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = ortakIslemler.personelHareketleriGetir(kgsPerMap, ortakIslemler.tariheGunEkleCikar(cal, basTarih, -1), ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 1), session);
					if (personelHareketMap.isEmpty() == false) {
						List<VardiyaGun> gunList = ortakIslemler.getAllPersonelIdVardiyalar(personelIdList, tatilMap, basTarih, bitTarih, true, session);
						TreeMap<Long, List<VardiyaGun>> vGunMap = new TreeMap<Long, List<VardiyaGun>>();
						HashMap<Long, ArrayList<VardiyaGun>> calismaPlaniMap = new HashMap<Long, ArrayList<VardiyaGun>>();
						for (VardiyaGun vg : gunList) {
							if (vg.getPdksPersonel() == null)
								continue;
							vg.setAyinGunu(vg.getVardiyaDateStr().startsWith(key));
							String vKey = PdksUtil.convertToDateString(vg.getVardiyaDate(), PATTERN);
							Long perId = vg.getPdksPersonel().getId();
							TreeMap<String, VardiyaGun> vardiyaGunleriMap = personelDenklestirmeMap.get(perId).getVardiyaGunleriMap();
							vardiyaGunleriMap.put(vKey, vg);
							List<VardiyaGun> vardiyaGunList = vGunMap.containsKey(perId) ? vGunMap.get(perId) : new ArrayList<VardiyaGun>();
							ArrayList<VardiyaGun> vardiyaGun1List = calismaPlaniMap.containsKey(perId) ? calismaPlaniMap.get(perId) : new ArrayList<VardiyaGun>();
							if (vardiyaGunList.isEmpty()) {
								vGunMap.put(perId, vardiyaGunList);
								calismaPlaniMap.put(perId, vardiyaGun1List);
							}
							vardiyaGun1List.add(vg);
							vardiyaGunList.add(vg);
						}
						ortakIslemler.vardiyaHareketlerdenGuncelle(personelDenklestirmeMap, vGunMap, calismaPlaniMap, hareketKaydiVardiyaMap, personelHareketMap, null, session);
						if (islemYapildi == null)
							islemYapildi = dayOffWeek != Calendar.SUNDAY && tatilMap.containsKey(dateStr) == false;

						gunList = null;
						calismaPlaniMap = null;
						vGunMap = null;
					}
					if (authenticatedUser != null)
						logger.info(str + " out " + PdksUtil.getCurrentTimeStampStr());
					list = null;
					personelDenklestirmeMap = null;
					personelHareketMap = null;

					list = null;
					personelIdList = null;
				}
				if (authenticatedUser != null && listeList.isEmpty() == false)
					logger.info("VardiyaHareketGuncelleme bitti. " + PdksUtil.getCurrentTimeStampStr());
				listeList = null;
			}
			linkedHashMap = null;
			tatilMap = null;
		}
		denklestirmeList = null;
		sb = null;
		if (islemYapildi == null)
			islemYapildi = false;
		if (authenticatedUser != null)
			fazlaMesaiGuncelleme(tarih, session);
		return islemYapildi;
	}

	/**
	 * @param adi
	 * @param session
	 * @return
	 */
	private Parameter getParameter(String adi, Session session) {
		Parameter parameter = null;
		if (ortakIslemler.getParameterKeyHasStringValue(adi))
			parameter = ortakIslemler.getParameter(session, adi);
		if (parameter != null) {
			boolean guncelleme = false;
			String value = parameter.getValue();
			if (PdksUtil.hasStringValue(value))
				try {
					guncelleme = PdksUtil.zamanKontrol(adi, value, bugun);
				} catch (Exception e) {
				}

			if (guncelleme == false)
				parameter = null;
		}
		return parameter;

	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		PlanVardiyaHareketGuncelleme.calisiyor = calisiyor;
	}

}