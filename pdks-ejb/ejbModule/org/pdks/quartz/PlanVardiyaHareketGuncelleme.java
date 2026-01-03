package org.pdks.quartz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

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
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Liste;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;
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

	public static final String PARAMETER_HAREKET_KEY = "hareketVardiyaZamani";

	private static boolean calisiyor = Boolean.FALSE;

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
				session = PdksUtil.getSession(entityManager, Boolean.TRUE);
				Parameter parameterHareket = ortakIslemler.getParameter(session, PARAMETER_HAREKET_KEY);
				String valueHareket = (parameterHareket != null) ? parameterHareket.getValue() : null;
				if (PdksUtil.hasStringValue(valueHareket)) {
					Date tarih = ortakIslemler.getBugun();
					boolean guncellemeHareketDurum = PdksUtil.zamanKontrol(PARAMETER_HAREKET_KEY, valueHareket, tarih);
					if (guncellemeHareketDurum) {
						guncellemeHareketDurum = vardiyaHareketGuncelleme(tarih, session);
						if (guncellemeHareketDurum)
							zamanlayici.mailGonder(session, null, parameterHareket.getDescription(), "Plan Vardiya Hareket Güncelleme güncellenmiştir.", null, Boolean.TRUE);

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
		String dateStr = PdksUtil.convertToDateString(cal.getTime(), "yyyyMMdd");
		int dayOffWeek = cal.get(Calendar.DAY_OF_WEEK);
		cal.setTime(tarih);
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.MONTH, -1);
		Date basTarih = PdksUtil.getDate(cal.getTime());
		Date bitTarih = PdksUtil.getDate(tarih);
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
		sb.append(" inner join " + DenklestirmeAy.TABLE_NAME + " D ON D." + DenklestirmeAy.COLUMN_NAME_YIL + " = V." + DenklestirmeAy.COLUMN_NAME_YIL + " AND D." + DenklestirmeAy.COLUMN_NAME_AY + " = V." + DenklestirmeAy.COLUMN_NAME_AY + " AND D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1");
		sb.append(" inner join " + PersonelDenklestirme.TABLE_NAME + " PD ON PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL + "  and PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" and coalesce(PD." + PersonelDenklestirme.COLUMN_NAME_SUA_DURUM + ", 0) = 0");
		sb.append(" inner join " + CalismaModeliAy.TABLE_NAME + " C ON C." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY + " and C." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1");
		sb.append(" order by D." + DenklestirmeAy.COLUMN_NAME_YIL + " desc, D." + DenklestirmeAy.COLUMN_NAME_AY + " desc, PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Long> idList = PdksUtil.getLongListFromBigDecimal(pdksEntityController.getObjectBySQLList(sb.toString(), fields, null));
		List<PersonelDenklestirme> denklestirmeList = null;
		if (idList.isEmpty() == false)
			denklestirmeList = pdksEntityController.getSQLParamByFieldList(PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.COLUMN_NAME_ID, idList, PersonelDenklestirme.class, session);
		idList = null;
		boolean islemYapildi = false;
		if (denklestirmeList != null && denklestirmeList.isEmpty() == false) {
			TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(null, basTarih, bitTarih, session);
			islemYapildi = dayOffWeek != Calendar.SUNDAY && tatilMap.containsKey(dateStr) == false;
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
				tarih = PdksUtil.convertToJavaDate(key + "01", "yyyyMMdd");
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
							String vKey = PdksUtil.convertToDateString(vg.getVardiyaDate(), "yyyyMMdd");
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
						gunList = null;
						calismaPlaniMap = null;
						vGunMap = null;
					}
					logger.info(str + " out " + PdksUtil.getCurrentTimeStampStr());
					list = null;
					personelDenklestirmeMap = null;
					personelHareketMap = null;

					list = null;
					personelIdList = null;
				}
				listeList = null;
			}
			linkedHashMap = null;
			tatilMap = null;
		}
		denklestirmeList = null;
		sb = null;
		return islemYapildi;
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		PlanVardiyaHareketGuncelleme.calisiyor = calisiyor;
	}

}