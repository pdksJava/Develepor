package org.pdks.quartz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

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
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.KatSayi;
import org.pdks.entity.Liste;
import org.pdks.entity.Parameter;
import org.pdks.entity.PdksAgent;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.enums.PuantajKatSayiTipi;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("fazlaMesaiGuncelleme")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class FazlaMesaiGuncelleme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8751158477032830258L;

	static Logger logger = Logger.getLogger(FazlaMesaiGuncelleme.class);

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

	public static final String PARAMETER_FAZLA_MESAI_KEY = "fazlaMesaiHesaplamaZamani";
	public static final String PATTERN = "yyyyMMdd";
	public static final String PATTERN_DONEM = "yyyyMM";
	private StringBuffer fazlaMesaiDetay;
	private String fazlaMesaiGuncelleMail, konu;

	private static boolean calisiyor = Boolean.FALSE;

	private Date bugun, basTarih;

	private Parameter parameterFazlaMesaiHesaplama;

	private Long agentId = null;

	private Session session = null;

	@Transactional
	public String fazlaMesaiHesaplamaBaslat() {

		try {
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			agentId = req != null ? Long.parseLong(req.getParameter("agentId")) : null;
		} catch (Exception e) {
		}
		try {
			if (PdksUtil.isSessionKapali(session)) {
				if (authenticatedUser != null)
					session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
				else
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
			}

			parameterFazlaMesaiHesaplama = ortakIslemler.getParameterAktif(session, PARAMETER_FAZLA_MESAI_KEY);
			if (parameterFazlaMesaiHesaplama == null) {
				parameterFazlaMesaiHesaplama = new Parameter();
				parameterFazlaMesaiHesaplama.setDescription("Fazla Mesai Toplu Hesaplama");
			}
			if (parameterFazlaMesaiHesaplama != null && ortakIslemler.hasStringValue(parameterFazlaMesaiHesaplama.getValue()) == false) {
				fazlaMesaiGuncelleme(ortakIslemler.getBugun(), session);

			}
		} catch (Exception e) {
		}
		if (fazlaMesaiDetay != null)
			mailGonder();
	
		pdksEntityController.sessionClose(session);
		return MenuItemConstant.home;
	}

	@Asynchronous
	@SuppressWarnings("unchecked")
	public QuartzTriggerHandle fazlaMesaiHesaplamaTimer(@Expiration Date when, @IntervalCron String interval) {
		if (!isCalisiyor()) {
			setCalisiyor(Boolean.TRUE);
			logger.debug("fazlaMesaiHesaplamaTimer in " + PdksUtil.getCurrentTimeStampStr());
			try {
				if (PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum()) {
					Calendar cal = Calendar.getInstance();
					bugun = cal.getTime();
					if (PdksUtil.isSessionKapali(session))
						session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					parameterFazlaMesaiHesaplama = getParameter(PARAMETER_FAZLA_MESAI_KEY, session);
					if (parameterFazlaMesaiHesaplama != null) {
						fazlaMesaiGuncelleme(PdksUtil.getDate(bugun), session);
					}
				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n" + e.getMessage() + " " + PdksUtil.getCurrentTimeStampStr());
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
			} finally {
				if (fazlaMesaiDetay != null)
					mailGonder();
				pdksEntityController.sessionClose(session);
				setCalisiyor(Boolean.FALSE);

			}
			logger.debug("fazlaMesaiHesaplamaTimer out " + PdksUtil.getCurrentTimeStampStr());
		}

		return null;
	}

	/**
	 * @param tarih
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public String fazlaMesaiGuncelleme(Date tarih, Session session) throws Exception {
		if (agentId != null) {
			PdksAgent agent = (PdksAgent) pdksEntityController.getSQLParamByFieldObject(PdksAgent.TABLE_NAME, PdksAgent.COLUMN_NAME_ID, agentId, PdksAgent.class, session);
			if (agent != null)
				parameterFazlaMesaiHesaplama.setDescription(agent.getAciklama());
		}
		konu = parameterFazlaMesaiHesaplama.getDescription();
		logger.info(konu + " in " + PdksUtil.getCurrentTimeStampStr());
		Date bugun = PdksUtil.getDate(ortakIslemler.getBugun());
		if (tarih == null)
			tarih = bugun;
		fazlaMesaiDetay = null;
		Calendar cal = Calendar.getInstance();
		Parameter parameter = ortakIslemler.getParameterAktif(session, "fazlaMesaiGuncelleMail");
		fazlaMesaiGuncelleMail = parameter != null && parameter.getValue() != null ? parameter.getValue() : "";
		cal.setTime(bugun);
		int haftaGun = cal.get(Calendar.DAY_OF_WEEK);
		int artiGun = haftaGun != Calendar.SATURDAY && haftaGun != Calendar.SUNDAY ? 0 : 6;
		String adresStr = null;
		long buAy = Long.parseLong(PdksUtil.convertToDateString(tarih, PATTERN_DONEM));
		long oncekiAy = Long.parseLong(PdksUtil.convertToDateString(PdksUtil.tariheAyEkleCikar(PdksUtil.convertToJavaDate(buAy + "01", PATTERN), -1), PATTERN_DONEM));
		long sonrakiAy = Long.parseLong(PdksUtil.convertToDateString(PdksUtil.tariheGunEkleCikar(tarih, artiGun), PATTERN_DONEM));
		List<DenklestirmeAy> aylar = pdksEntityController.getSQLParamByFieldList(DenklestirmeAy.TABLE_NAME, DenklestirmeAy.COLUMN_NAME_DONEM_KODU, Arrays.asList(new Long[] { oncekiAy, buAy, sonrakiAy }), DenklestirmeAy.class, session);
		TreeMap<Long, DenklestirmeAy> ayMap = new TreeMap<Long, DenklestirmeAy>();
		for (DenklestirmeAy denklestirmeAy : aylar)
			ayMap.put(denklestirmeAy.getDonem(), denklestirmeAy);
		if (ayMap.containsKey(sonrakiAy) == false)
			ayMap.put(sonrakiAy, ayMap.get(buAy));
		aylar = null;
		Date tarihBas = PdksUtil.convertToJavaDate(oncekiAy + "01", PATTERN);
		Date tarih2 = PdksUtil.convertToJavaDate(sonrakiAy + "01", PATTERN);
		String str = "";
		StringBuffer sb = new StringBuffer(), sb1 = new StringBuffer(), sb2 = new StringBuffer();
		HashMap fields = new HashMap();
		int sayac = 0;
		Boolean mailGonder = null;
		while (tarih2.getTime() >= tarihBas.getTime()) {
			++sayac;
			Date tarihBit = PdksUtil.getAyinSonGunu(tarihBas);
			DenklestirmeAy da = ayMap.get(Long.parseLong(PdksUtil.convertToDateString(tarihBas, PATTERN_DONEM)));
			long donemId = da.getId();
			int durum = da.getDurum() ? 1 : 0;
			sb2.append(" PERSONEL" + sayac + " as ( ");
			sb2.append(" select P." + Personel.COLUMN_NAME_ID + " as " + VardiyaGun.COLUMN_NAME_PERSONEL + ", count(*) ADET from " + Personel.TABLE_NAME + " P " + PdksEntityController.getSelectLOCK());
			if (da.getDurum()) {
				fields.put("pbas" + sayac, tarihBas);
				fields.put("pbit" + sayac, tarihBit);
				fields.put("vbas" + sayac, tarihBas);
				fields.put("vbit" + sayac, tarihBit);
				sb2.append(" left join " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
				sb2.append(" and ( V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between :vbas" + sayac + " and :vbit" + sayac + ")");
				sb2.append(" where P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :pbas" + sayac + " and P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :pbit" + sayac);
				sb2.append(" and P." + Personel.COLUMN_NAME_CALISMA_MODELI + " is not null and P." + Personel.COLUMN_NAME_SABLON + " is not null");
				sb2.append(" and (P." + Personel.COLUMN_NAME_PDKS_DURUM + " = 1 or P." + Personel.COLUMN_NAME_MAIL_TAKIP + " = 1) and P." + Personel.COLUMN_NAME_EK_SAHA3 + " is not null");
				sb2.append(" and P." + Personel.COLUMN_NAME_DURUM + " = 1 and coalesce(V." + VardiyaGun.COLUMN_NAME_DURUM + ", 0) = 0 ");
			} else
				sb2.append(" where 1 = 2 ");
			sb2.append(" group by P." + Personel.COLUMN_NAME_ID);
			sb2.append("), ");
			boolean eski = sayac < 3 || buAy == sonrakiAy;
			// String str2 = eski ? "( coalesce(PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + ", 0) = 0 or " + donem + " < " + buAy + " )" : "PD." + PersonelDenklestirme.COLUMN_NAME_ID + " is null";
			String str2 = eski ? "" : " and PD." + PersonelDenklestirme.COLUMN_NAME_ID + " is null";
			sb1.append(str + " select distinct D.* from " + DenklestirmeAy.TABLE_NAME + " D " + PdksEntityController.getSelectLOCK());
			sb1.append(" inner join PERSONEL" + sayac + " V " + PdksEntityController.getJoinLOCK() + " on  1 = " + durum);
			sb1.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
			if (eski == false) {
				long buDonemId = ayMap.get(buAy).getId();
				sb1.append(" inner join " + PersonelDenklestirme.TABLE_NAME + " G " + PdksEntityController.getJoinLOCK() + " on G." + Personel.COLUMN_NAME_ID + " = " + buDonemId);
				sb1.append(" and G." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
				sb1.append(" inner join " + PersonelKGS.TABLE_NAME + " K " + PdksEntityController.getJoinLOCK() + " on K." + PersonelKGS.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_KGS_PERSONEL + " and K." + PersonelKGS.COLUMN_NAME_DURUM + " = 1");

			}
			sb1.append(" inner join " + Sirket.TABLE_NAME + " S " + PdksEntityController.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET + " and S." + Sirket.COLUMN_NAME_FAZLA_MESAI + " = 1");
			sb1.append(" left join " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getJoinLOCK() + " on PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb1.append(" and PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = P." + Personel.COLUMN_NAME_ID);
			sb1.append(" where D." + DenklestirmeAy.COLUMN_NAME_ID + " = " + donemId + " and " + durum + " = 1" + str2);
			str = " union";

			tarihBas = PdksUtil.tariheAyEkleCikar(tarihBas, 1);

		}
		ayMap = null;
		sb.append("with " + sb2.toString() + " VERI as (" + sb1.toString());
		sb.append(" ) select distinct D.* from VERI D " + PdksEntityController.getSelectLOCK());
		sb.append(" order by D." + DenklestirmeAy.COLUMN_NAME_DONEM_KODU);
		sb2 = null;
		sb1 = null;
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			aylar = pdksEntityController.getObjectBySQLList(sb.toString(), fields, DenklestirmeAy.class);
		} catch (Exception e) {
			logger.error(e);
		}
		List<Sirket> sirketList = null;
		if (aylar != null && aylar.isEmpty() == false) {
			adresStr = ortakIslemler.getLoginAdres();
			if (PdksUtil.hasStringValue(adresStr)) {
				String adres = PdksUtil.replaceAllManuel(adresStr, "login", "denklestirmeBordroGuncelleme");
				logger.info(adres + " in " + PdksUtil.getCurrentTimeStampStr());
				User guncelleyenUser = ortakIslemler.getSistemAdminUser(session);
				guncelleyenUser.setAdmin(true);
				List<Liste> islemList = new ArrayList<Liste>();
				boolean talepVar = getSirketTalepGirmeDurum(session);
				List<String> donemler = new ArrayList<String>();
				for (DenklestirmeAy da : aylar) {
					try {
						vardiyaVersiyonGuncelle(da, talepVar, bugun, guncelleyenUser, session);
					} catch (Exception e) {

					}
					String donem = da.getAyAdi() + " " + da.getYil();
					donemler.add(donem);
					DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
					AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(da.getAy(), da.getYil(), denklestirmeDonemi, session);
					aylikPuantaj.setLoginUser(guncelleyenUser);
					aylikPuantaj.setDenklestirmeAy(da);
					List<SelectItem> departmanIdList = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(aylikPuantaj, false, session);
					for (SelectItem siDepartman : departmanIdList) {
						Long departmanId = (Long) siDepartman.getValue();
						List<SelectItem> sirketIdList = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, aylikPuantaj, false, session);
						if (sirketIdList.isEmpty() == false) {
							List<Long> idList = new ArrayList<Long>();
							for (SelectItem sirketSelectItem : sirketIdList)
								idList.add((Long) sirketSelectItem.getValue());
							sirketList = pdksEntityController.getSQLParamByFieldList(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, idList, Sirket.class, session);
							if (sirketList.size() > 1)
								sirketList = PdksUtil.sortObjectStringAlanList(sirketList, "getAd", null);
							idList = null;
							for (Sirket sirket : sirketList) {
								String linkStr = "pdksUserId=" + guncelleyenUser.getId() + "&donemId=" + da.getId() + "&sirketId=" + sirket.getId();
								if (sirket.isTesisDurumu())
									linkStr = linkStr + "&tesisId=*";
								String id = ortakIslemler.getEncodeStringByBase64(linkStr);

								islemList.add(new Liste(donem + " : " + sirket.getAd(), adres + "?id=" + id));
							}

						}
						sirketIdList = null;
					}
				}
				String uolStr = "";
				if (mailGonder == null && islemList.isEmpty() == false) {
					mailGonder = getMailGonder(session);
					if (mailGonder) {
						uolStr = aylar.size() > 1 && (sirketList != null && sirketList.size() > 1) ? "OL" : "UL";
						fazlaMesaiDetay = new StringBuffer();
						fazlaMesaiDetay.append("<p><" + uolStr + ">");
					}

				}
				boolean renkUyari = true;
				for (String donem : donemler) {

					for (Iterator iterator = islemList.iterator(); iterator.hasNext();) {
						Liste liste = (Liste) iterator.next();
						String value = (String) liste.getValue();
						String id = (String) liste.getId();
						if (id.startsWith(donem) || donemler.size() == 1) {
							String sonuc = ortakIslemler.adresKontrol(value);
							if (sonuc != null)
								logger.error(id + " hata =" + sonuc + " out " + PdksUtil.getCurrentTimeStampStr());
							else if (fazlaMesaiDetay != null) {
								fazlaMesaiDetay.append("<LI class=\"" + (renkUyari ? "odd" : "even") + "\" style=\"text-align: left;\">" + liste.getId() + (iterator.hasNext() ? " " + PdksUtil.getCurrentTimeStampStr() : "") + "</LI>");
								renkUyari = !renkUyari;
							}
							iterator.remove();
						}

					}
					if (uolStr != null && uolStr.equals("OL")) {
						if (fazlaMesaiDetay != null) {
							fazlaMesaiDetay.append("</" + uolStr + "></p>");
							fazlaMesaiDetay.append("<p><" + uolStr + ">");
						}
						renkUyari = true;
					}

				}

				logger.info(adres + " out " + PdksUtil.getCurrentTimeStampStr());
				if (fazlaMesaiDetay != null)
					fazlaMesaiDetay.append("</" + uolStr + "></p>");

				islemList = null;

			}
		}
		aylar = null;
		if (adresStr != null)
			adresStr = "";
		logger.info(konu + " out " + PdksUtil.getCurrentTimeStampStr());

		return adresStr;

	}

	/**
	 * @param da
	 * @param talepVar
	 * @param bugun
	 * @param guncelleyenUser
	 * @param session
	 */
	private void vardiyaVersiyonGuncelle(DenklestirmeAy da, boolean talepVar, Date bugun, User guncelleyenUser, Session session) {
		HashMap fields = new HashMap();
		Date tarihBas = PdksUtil.convertToJavaDate(da.getDonem() + "01", PATTERN);
		Date tarihBit = PdksUtil.getAyinSonGunu(tarihBas);
		if (tarihBit.after(bugun))
			tarihBit = bugun;
		if (talepVar) {
			StringBuffer sb = new StringBuffer();
			sb.append("select K.* from " + KatSayi.TABLE_NAME + " K " + PdksEntityController.getSelectLOCK());
			sb.append(" where K." + KatSayi.COLUMN_NAME_BAS_TARIH + " <= :t2 and K." + KatSayi.COLUMN_NAME_BIT_TARIH + " >= :t1");
			sb.append(" and K." + KatSayi.COLUMN_NAME_TIPI + " = " + PuantajKatSayiTipi.GUN_FMT_DURUM.value());
			fields.put("t1", tarihBas);
			fields.put("t2", tarihBit);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<KatSayi> katsayiList = pdksEntityController.getObjectBySQLList(sb.toString(), fields, KatSayi.class);
			talepVar = katsayiList.isEmpty() == false;
			katsayiList = null;
		}
		fields.clear();
		StringBuffer sb1 = new StringBuffer();
		sb1.append("select V.* from " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getSelectLOCK());
		sb1.append(" inner join " + CalismaModeliAy.TABLE_NAME + " CA " + PdksEntityController.getJoinLOCK() + " on CA." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY);
		sb1.append(" and CA." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1");
		sb1.append(" inner join " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getJoinLOCK() + " on V." + VardiyaGun.COLUMN_NAME_PERSONEL + " = PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		sb1.append(" and (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between :t1 and :t2) ");
		sb1.append(" and ( (V." + VardiyaGun.COLUMN_NAME_ONAYLI + " = 1 and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 0 ) ");
		sb1.append(" or (V." + VardiyaGun.COLUMN_NAME_ONAYLI + " = 0 and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 1 ) ) ");
		sb1.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		sb1.append(" and (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ") ");
		if (talepVar) {
			sb1.append(" left join " + FazlaMesaiTalep.TABLE_NAME + " F " + PdksEntityController.getJoinLOCK() + " on F." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + " = V." + VardiyaGun.COLUMN_NAME_ID);
			sb1.append(" and F." + FazlaMesaiTalep.COLUMN_NAME_DURUM + " = 1");
		}
		sb1.append(" where PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = " + da.getId() + " and PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + " = 1");
		if (talepVar)
			sb1.append(" and F." + FazlaMesaiTalep.COLUMN_NAME_ID + " is null");
		sb1.append(" order by P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + ", V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI);
		fields.put("t1", tarihBas);
		fields.put("t2", tarihBit);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<VardiyaGun> vGunList = pdksEntityController.getObjectBySQLList(sb1.toString(), fields, VardiyaGun.class);
		if (vGunList.isEmpty() == false) {
			List<Long> perIdList = new ArrayList<Long>();
			for (VardiyaGun vg : vGunList) {
				if (vg.getVardiya().isHaftaTatil()) {
					Long perId = vg.getPdksPersonel().getId();
					if (!perIdList.contains(perId))
						perIdList.add(perId);

				}
			}
			if (perIdList.isEmpty() == false) {
				fields.clear();
				sb1 = new StringBuffer();
				sb1.append("select PD.* from " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getSelectLOCK());
				sb1.append(" inner join " + CalismaModeliAy.TABLE_NAME + " CA " + PdksEntityController.getJoinLOCK() + " on CA." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY);
				sb1.append(" and CA." + CalismaModeliAy.COLUMN_NAME_HAFTA_TATIL_HAREKET_GUNCELLE + " = 0");
				sb1.append(" where PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = " + da.getId() + " and PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :p");
				fields.put("p", perIdList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PersonelDenklestirme> pdList = pdksEntityController.getObjectBySQLList(sb1.toString(), fields, PersonelDenklestirme.class);
				perIdList.clear();
				for (PersonelDenklestirme pd : pdList)
					perIdList.add(pd.getPersonelId());
				pdList = null;
			}
			Date guncellemeTarihi = null;
			boolean flush = false;
			// int adet = 0;
			for (VardiyaGun vg : vGunList) {
				Long perId = vg.getPdksPersonel().getId();
				Boolean vardiyaOnayli = vg.getDurum();
				if (vg.getVardiya().isHaftaTatil()) {
					if (perIdList.contains(perId))
						vardiyaOnayli = true;
				}
				if (vg.getVardiyaOnayli().equals(vardiyaOnayli) == false) {
					if (guncellemeTarihi == null)
						guncellemeTarihi = new Date();
					vg.setGuncellemeTarihi(guncellemeTarihi);
					vg.setGuncelleyenUser(guncelleyenUser);
					vg.setVardiyaOnayli(vardiyaOnayli);
					pdksEntityController.saveOrUpdate(session, entityManager, vg);
					flush = true;

				}

			}
			perIdList = null;
			if (flush)
				ortakIslemler.sessionFlush(session);

		}
		fields.clear();
		sb1 = new StringBuffer();
		sb1.append("with DATA as (");
		sb1.append(" select PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + ", CA." + CalismaModeliAy.COLUMN_NAME_HAFTA_TATIL_HAREKET_GUNCELLE + " from " + CalismaModeliAy.TABLE_NAME + " CA " + PdksEntityController.getSelectLOCK());
		sb1.append(" inner join " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getJoinLOCK() + " on CA." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY);
		sb1.append(" where CA. " + CalismaModeliAy.COLUMN_NAME_DONEM + " = " + da.getId() + " and CA." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1 )");
		sb1.append(" select V.* from " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK());
		sb1.append(" inner join DATA D " + PdksEntityController.getJoinLOCK() + " on D." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb1.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb1.append(" and (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ") ");
		if (talepVar)
			sb1.append(" left join " + FazlaMesaiTalep.TABLE_NAME + " T " + PdksEntityController.getJoinLOCK() + " on T." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + " = V." + VardiyaGun.COLUMN_NAME_ID);
		sb1.append(" where (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between :t1 and :t2) and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 0 and V." + VardiyaGun.COLUMN_NAME_ONAYLI + " = 1");
		sb1.append("  and (V.VARDIYA_ACIKLAMA <> 'HT' OR D." + CalismaModeliAy.COLUMN_NAME_HAFTA_TATIL_HAREKET_GUNCELLE + " = 1)");
		sb1.append("  and  V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= " + PdksEntityController.getSqlBuGun());
		if (talepVar)
			sb1.append(" and T." + FazlaMesaiTalep.COLUMN_NAME_ID + " is null");
		fields.put("t1", tarihBas);
		fields.put("t2", tarihBit);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		vGunList = pdksEntityController.getObjectBySQLList(sb1.toString(), fields, VardiyaGun.class);
		Date guncellemeTarihi = null;
		boolean flush = false;
		// int adet = 0;
		for (VardiyaGun vg : vGunList) {
			Vardiya v = vg.getVardiya();
			if (v.isIzinVardiya() || v.isOffGun() || vg.getVardiyaOnayli() || vg.getDurum())
				continue;
			if (guncellemeTarihi == null)
				guncellemeTarihi = new Date();
			vg.setGuncellemeTarihi(guncellemeTarihi);
			vg.setGuncelleyenUser(guncelleyenUser);
			vg.setVardiyaOnayli(Boolean.FALSE);
			pdksEntityController.saveOrUpdate(session, entityManager, vg);
			flush = true;

		}
		if (flush)
			ortakIslemler.sessionFlush(session);

		vGunList = null;
		sb1 = null;
	}

	/**
	 * @param session
	 * @return
	 */
	private boolean getSirketTalepGirmeDurum(Session session) {
		boolean talepVar = false;
		List<Sirket> sirketList = pdksEntityController.getSQLParamByAktifFieldList(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_PDKS, Boolean.TRUE, Sirket.class, session);
		for (Sirket sirket : sirketList) {
			if (sirket.getFazlaMesai() && talepVar == false)
				talepVar = sirket.getFazlaMesaiTalepGirilebilir();

		}
		sirketList = null;
		return talepVar;
	}

	/**
	 * @throws Exception
	 */
	private void mailGonder() {
	///	String konu = parameterFazlaMesaiHesaplama.getDescription();
		logger.info(konu + " mail gönderiliyor. " + PdksUtil.getCurrentTimeStampStr());
		String aciklama = "Fazla Mesai Toplu güncellenmiştir.<br></br>" + fazlaMesaiDetay.toString();
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSession(entityManager, Boolean.TRUE);
		fazlaMesaiDetay = null;
		List<User> userList = null;
 		if (fazlaMesaiGuncelleMail.equals("1"))
			try {
				userList = ortakIslemler.getIKUserList(session);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		if (userList == null || userList.isEmpty()) {
			aciklama = aciklama + "<br></br><br></br><b>Start Time : </b>" + PdksUtil.convertToDateString(basTarih, PdksUtil.getDateTimeLongFormat());
			aciklama = aciklama + "<br></br><b>Stop Time  : </b>" + PdksUtil.convertToDateString(ortakIslemler.getBugun(), PdksUtil.getDateTimeLongFormat()) + "<br></br>";
		}
		try {
			zamanlayici.mailGonder(session, null, konu, aciklama, userList, Boolean.TRUE);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	/**
	 * @param session
	 * @return
	 */
	private boolean getMailGonder(Session session) {
		boolean mailGonder = false;
		if (PdksUtil.getCanliSunucuDurum() || (PdksUtil.getTestSunucuDurum() == false && fazlaMesaiGuncelleMail.equals("1") == false)) {
			Calendar cal = Calendar.getInstance();
			if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				Date basTarih = PdksUtil.getDate(cal.getTime());
				String key = PdksUtil.convertToDateString(basTarih, PATTERN);
				Date bitTarih = PdksUtil.tariheGunEkleCikar(basTarih, 1);
				TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(null, basTarih, bitTarih, session);
				Tatil tatil = tatilMap != null && tatilMap.containsKey(key) ? tatilMap.get(key) : null;
				mailGonder = tatil == null || tatil.isYarimGunMu();
				tatilMap = null;
			}
		}
		return mailGonder;
	}

	/**
	 * @param adi
	 * @param session
	 * @return
	 */
	private Parameter getParameter(String adi, Session session) {
		Parameter parameter = null;
		if (ortakIslemler.getParameterKeyHasStringValue(adi))
			parameter = ortakIslemler.getParameterAktif(session, adi);
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
		FazlaMesaiGuncelleme.calisiyor = calisiyor;
	}

}