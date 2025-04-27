package org.pdks.quartz;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
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
import org.jboss.seam.faces.Renderer;
import org.pdks.entity.Departman;
import org.pdks.entity.IzinHakedisHakki;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Parameter;
import org.pdks.entity.PdksAgent;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.ThreadAgent;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("izinBakiyeGuncelleme")
@AutoCreate
// @Synchronized(timeout=15000)
@Scope(ScopeType.APPLICATION)
public class IzinBakiyeGuncelleme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7758680001001702451L;

	static Logger logger = Logger.getLogger(IzinBakiyeGuncelleme.class);

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	Zamanlayici zamanlayici;
	@In(required = false, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	StartupAction startupAction;
	private static boolean calisiyor = Boolean.FALSE;
	private int yil;
	private String hataKonum;
	private List<User> toList;
	private List<PersonelIzin> izinList;
	public static final String PARAMETER_KEY = "izinBakiyeGuncelleme";
	private static boolean ozelKontrol = Boolean.FALSE;
	private boolean guncellemeDBDurum = Boolean.FALSE, zamanDurum = false;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle izinBakiyeGuncellemeTimer(@Expiration Date when, @IntervalCron String interval) {
		hataKonum = "izinBakiyeGuncellemeTimer başladı ";
		if (pdksEntityController != null && !isCalisiyor()) {
			Session session = null;

			zamanDurum = false;

			izinGuncelemeCalistir(false, session);
		}
		return null;
	}

	/**
	 * @param session
	 * @return
	 */
	public String agentCalistir(Session session) {
		if (entityManager != null)
			getAgentCalistirTime(session);
		return "";
	}

	/**
	 * @param session
	 * @return
	 */
	private Calendar getAgentCalistirTime(Session session) {
		session.clear();
		Calendar cal = Calendar.getInstance();
		HashMap fields = new HashMap();
		StringBuffer sp = new StringBuffer();
		sp.append("select * from " + PdksAgent.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
		sp.append(" where 1 = 2 ");
		fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PdksAgent> list = pdksEntityController.getObjectBySQLList(sp, fields, PdksAgent.class);
		if (!list.isEmpty()) {
			int dakika = cal.get(Calendar.MINUTE);
			int saat = cal.get(Calendar.HOUR_OF_DAY);
			int gun = cal.get(Calendar.DATE);
			int hafta = cal.get(Calendar.DAY_OF_WEEK);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				PdksAgent pa = (PdksAgent) iterator.next();
				if (pa.getDurum() != null && pa.getDurum() && PdksUtil.hasStringValue(pa.getStoreProcedureAdi())) {
					boolean dakikaCalistir = true, saatCalistir = true, gunCalistir = true, haftaCalistir = true;
					String dakikaStr = PdksUtil.hasStringValue(pa.getDakikaBilgi()) ? pa.getDakikaBilgi() : "*";
					String saatStr = PdksUtil.hasStringValue(pa.getSaatBilgi()) ? pa.getSaatBilgi() : "*";
					String gunStr = PdksUtil.hasStringValue(pa.getGunBilgi()) ? pa.getGunBilgi() : "*";
					String haftaStr = PdksUtil.hasStringValue(pa.getHaftaBilgi()) ? pa.getHaftaBilgi() : "*";
					if (!dakikaStr.equals("*"))
						dakikaCalistir = ThreadAgent.kontrol(dakika, dakikaStr);

					if (!saatStr.equals("*"))
						saatCalistir = ThreadAgent.kontrol(saat, saatStr);

					if (!gunStr.equals("*")) {
						gunStr = gunStr.toUpperCase(Locale.ENGLISH);
						if (gunStr.indexOf("L") >= 0) {
							int sonGun = cal.getActualMaximum(Calendar.DATE);
							if (gunStr.indexOf(",") > 0) {
								String[] str = gunStr.split(",");
								for (int i = 0; i < str.length; i++) {
									String str1 = str[i];
									if (str1.equals("L"))
										gunCalistir = gun == sonGun;
									else
										gunCalistir = ThreadAgent.kontrol(saat, str1);
									if (gunCalistir)
										break;
								}
							} else {
								gunCalistir = gun == sonGun;
							}
						} else
							gunCalistir = ThreadAgent.kontrol(gun, gunStr);
					}

					if (!haftaStr.equals("*"))
						haftaCalistir = ThreadAgent.kontrol(hafta, haftaStr);

					if (dakikaCalistir && saatCalistir && gunCalistir && haftaCalistir) {
						try {
							ThreadAgent ta = new ThreadAgent(pa, pdksEntityController, entityManager, null);
							ta.start();
						} catch (Exception e) {
							logger.error(e);
						}
					}
				}
			}
		}
		list = null;
		return cal;
	}

	/**
	 * @param manuel
	 * @param session
	 */
	public boolean izinGuncelemeCalistir(boolean manuel, Session session) {
		setCalisiyor(Boolean.TRUE);
		ozelKontrol = Boolean.FALSE;
		boolean hataGonder = Boolean.FALSE;
		guncellemeDBDurum = false;
		StringBuffer sb = new StringBuffer();
		try {
			if (session == null)
				session = PdksUtil.getSession(entityManager, Boolean.TRUE);
//			Calendar cal = getAgentCalistirTime(session);
			Calendar cal = Calendar.getInstance();
			Date time = cal.getTime();
			hataGonder = Boolean.TRUE;
			hataKonum = "Paramatre okunuyor ";
			Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
			String value = (parameter != null) ? parameter.getValue() : null;
			String izinERPTableViewAdi = ortakIslemler.getParameterKey(ortakIslemler.getParametreIzinERPTableView());
			boolean izinBakiye = value != null && (manuel || PdksUtil.zamanKontrol(PARAMETER_KEY, value, time));
			boolean sunucuDurum = manuel || PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum();

			zamanDurum = sunucuDurum && (manuel || PdksUtil.zamanKontrol(PARAMETER_KEY, value, time));
			// zamanDurum = true;
			// sunucuDurum = true;
			boolean tableERPOku = PdksUtil.hasStringValue(izinERPTableViewAdi);
			if (!zamanDurum && tableERPOku) {
				String parameterUpdateKey = PARAMETER_KEY + "Update";
				value = ortakIslemler.getParameterKey(parameterUpdateKey);
				if (PdksUtil.hasStringValue(value)) {
					guncellemeDBDurum = PdksUtil.zamanKontrol(parameterUpdateKey, value, time);
					if (guncellemeDBDurum == false && PdksUtil.isSistemDestekVar()) {
						// Calendar cal = Calendar.getInstance();
						int gun = cal.get(Calendar.DATE), dakika = cal.get(Calendar.MINUTE), saat = cal.get(Calendar.HOUR_OF_DAY), dayOffWeek = cal.get(Calendar.DAY_OF_WEEK);
						guncellemeDBDurum = dayOffWeek != Calendar.SATURDAY && dayOffWeek != Calendar.SUNDAY && (gun > 25 || gun < 6) && (saat > 7 && saat < 20) && dakika % 15 == 0;
					}
				}
			}
			hataKonum = "Paramatre okundu ";
			if (zamanDurum || guncellemeDBDurum) {
				if (ortakIslemler.getGuncellemeDurum(PersonelIzin.TABLE_NAME, session)) {
					hataKonum = "İzin durum kontrolu yapılıyor ";
					if (tableERPOku && sunucuDurum) {
						String uygulamaBordro = ortakIslemler.getParameterKey("uygulamaBordro");
						try {
							if (manuel == false) {
								logger.info(uygulamaBordro + " izin bilgileri güncelleniyor in " + PdksUtil.getCurrentTimeStampStr());
								ortakIslemler.izinERPDBGuncelle(true, null, session);
								logger.info(uygulamaBordro + " izin bilgileri güncelleniyor out " + PdksUtil.getCurrentTimeStampStr());
							}
						} catch (Exception e) {
							logger.error(e);
							e.printStackTrace();
						}

						String parameterName = ortakIslemler.getParametreHakEdisIzinERPTableView();
						if (izinBakiye && ortakIslemler.getParameterKeyHasStringValue(parameterName)) {
							if (izinBakiye) {
								// TODO Hakedis geçmiş oluştur
							}
							logger.info(uygulamaBordro + " izin hakediş bilgileri güncelleniyor in " + PdksUtil.getCurrentTimeStampStr());
							ortakIslemler.hakEdisIzinERPDBGuncelle(session);
							logger.info(uygulamaBordro + " izin hakediş bilgileri güncelleniyor out " + PdksUtil.getCurrentTimeStampStr());
						}

					}
					if (zamanDurum) {
						boolean spDurum = ortakIslemler.isExisStoreProcedure(PersonelIzinDetay.SP_NAME, session);
						HashMap fields = new HashMap();
						sb.append(" select distinct  IT." + IzinTipi.COLUMN_NAME_GIRIS_TIPI + ", IT." + IzinTipi.COLUMN_NAME_ID + " from " + IzinTipi.TABLE_NAME + " IT " + PdksEntityController.getSelectLOCK() + " ");
						sb.append(" inner join " + Departman.TABLE_NAME + " D " + PdksEntityController.getJoinLOCK() + " on D." + Departman.COLUMN_NAME_ID + " = IT." + IzinTipi.COLUMN_NAME_DEPARTMAN);
						String parameterName = ortakIslemler.getParametreHakEdisIzinERPTableView();
						if (spDurum == false && !ortakIslemler.getParameterKeyHasStringValue(parameterName))
							sb.append(" and D." + Departman.COLUMN_NAME_IZIN_GIRILEBILIR + " = 1 ");
						sb.append(" inner join " + IzinTipi.TABLE_NAME + " B " + PdksEntityController.getJoinLOCK() + " on B." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " = IT." + IzinTipi.COLUMN_NAME_ID);
						sb.append(" and B." + IzinTipi.COLUMN_NAME_DURUM + " = 1 ");
						sb.append(" where IT." + IzinTipi.COLUMN_NAME_DURUM + " = 1 ");
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<Object[]> list = pdksEntityController.getObjectBySQLList(sb, fields, null);
						if (list != null && !list.isEmpty()) {
							izinBakiyeGuncellemeCalistir(session, true, manuel);
							Object[] objects = list.get(0);
							if (objects[0] != null && spDurum) {
								String girisTipi = (String) objects[0];
								if (girisTipi.equals(IzinTipi.GIRIS_TIPI_YOK)) {
									fields.clear();
									sb = new StringBuffer();
									sb.append(" select distinct  P." + PersonelIzin.COLUMN_NAME_ID + " from " + PersonelIzin.TABLE_NAME + " P " + PdksEntityController.getSelectLOCK() + " ");
									sb.append(" left join " + PersonelIzinDetay.TABLE_NAME + " D " + PdksEntityController.getJoinLOCK() + " on D." + PersonelIzinDetay.COLUMN_NAME_IZIN + " = P." + PersonelIzin.COLUMN_NAME_ID);
									sb.append(" where P." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " =  " + objects[1] + " and P." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " =  " + PersonelIzin.IZIN_DURUMU_ONAYLANDI + " and D." + PersonelIzinDetay.COLUMN_NAME_ID + " is null");
									if (session != null)
										fields.put(PdksEntityController.MAP_KEY_SESSION, session);
									List<BigDecimal> izinList = pdksEntityController.getObjectBySQLList(sb, fields, null);
									if (!izinList.isEmpty()) {
										LinkedHashMap veriMap = new LinkedHashMap();
										sb = new StringBuffer(PersonelIzinDetay.SP_NAME);
										for (BigDecimal bigDecimal : izinList) {
											veriMap.put("id", bigDecimal.longValue());
											if (session != null)
												veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
											pdksEntityController.execSP(veriMap, sb);
										}
										session.flush();
									}
									izinList = null;
								}
							}

						} else
							zamanDurum = false;
						list = null;
					}
				}
			}

		} catch (Exception e) {
			logger.error(sb.toString() + " " + e);
			e.printStackTrace();
			if (hataGonder && zamanDurum)
				try {
					zamanlayici.mailGonder(session, null, "İzin Bakiye Güncellemesi", "İzin bakiyeleri güncellenmemiştir." + e.getMessage() + " ( " + hataKonum + " )", null, Boolean.TRUE);

				} catch (Exception e2) {
					logger.error("izinBakiyeGuncellemeTimer 2 : " + e2.getMessage());
				}
		} finally {
			if (manuel == false && session != null)
				session.close();
			setCalisiyor(Boolean.FALSE);

		}
		return zamanDurum;
	}

	/**
	 * @param session
	 * @param mailGonder
	 * @param manuel
	 * @throws Exception
	 */
	public void izinBakiyeGuncellemeCalistir(Session session, Boolean mailGonder, Boolean manuel) throws Exception {
		logger.info("izinleriHesapla basladi " + PdksUtil.getCurrentTimeStampStr());
		ozelKontrol = zamanlayici.getOzelKontrol(session);

		User sistemAdminUser = ortakIslemler.getSistemAdminUser(session);
		hataKonum = "izinleriHesapla başladı ";
		if (sistemAdminUser != null)
			sistemAdminUser.setLogin(Boolean.FALSE);
		izinleriHesapla(sistemAdminUser, session, manuel);
		if (mailGonder && zamanDurum)
			zamanlayici.mailGonder(session, null, "İzin Bakiye Güncellemesi", "İzin bakiyeleri güncellenmiştir.", null, Boolean.TRUE);
		logger.info("izinleriHesapla bitti " + PdksUtil.getCurrentTimeStampStr());
	}

	/**
	 * @param session
	 * @throws Exception
	 */
	@Transactional
	public void ciftBakiyeIzinKontrol(Session session) throws Exception {
		HashMap parametreMap = new HashMap();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(" select distinct  I.* from " + Personel.TABLE_NAME + " P " + PdksEntityController.getSelectLOCK() + " ");
			sb.append(" inner join " + PersonelIzin.TABLE_NAME + " I " + PdksEntityController.getJoinLOCK() + " on I." + PersonelIzin.COLUMN_NAME_PERSONEL + " = P." + Personel.COLUMN_NAME_ID);
			sb.append(" and I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " not in (8,9) and I." + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI + " >= P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI);
			sb.append(" inner join " + IzinTipi.TABLE_NAME + "  T " + PdksEntityController.getJoinLOCK() + " on T." + IzinTipi.COLUMN_NAME_ID + " = I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " and T." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " is not null");
			sb.append(" left join " + PersonelIzinDetay.TABLE_NAME + " D " + PdksEntityController.getJoinLOCK() + " on D." + PersonelIzinDetay.COLUMN_NAME_HAKEDIS_IZIN + " = I." + IzinTipi.COLUMN_NAME_ID);
			sb.append(" where P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " <= convert(date,GETDATE()) and D." + PersonelIzinDetay.COLUMN_NAME_ID + " is null");

			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<PersonelIzin> izinList = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelIzin.class);
			if (!izinList.isEmpty()) {
				User guncelleyenUser = ortakIslemler.getSistemAdminUser(session);
				Date guncellemeTarihi = Calendar.getInstance().getTime();
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();
					personelIzin.setIzinDurumu(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
					personelIzin.setGuncellemeTarihi(guncellemeTarihi);
					if (guncelleyenUser != null)
						personelIzin.setGuncelleyenUser(guncelleyenUser);
					pdksEntityController.saveOrUpdate(session, entityManager, personelIzin);
				}
				session.flush();
			}
		} catch (Exception e) {
			izinList = new ArrayList<PersonelIzin>();
		}
		parametreMap = null;
	}

	/**
	 * @param session
	 * @throws Exception
	 */
	private void senelikSuaIzinKontrol(Session session) throws Exception {

		try {
			Parameter parameter = ortakIslemler.getParameter(session, "suaSenelikKullan");
			boolean suaSenelikKullan = parameter != null && parameter.getValue().equals("1");

			HashMap parametreMap = new HashMap();
			StringBuffer sb = new StringBuffer();
			try {
				sb.append(" with IZIN_BAKIYELER as ( ");
				sb.append(" select  YEAR(I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ") as YIL,I." + PersonelIzin.COLUMN_NAME_PERSONEL + ",I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + ",max(I." + IzinTipi.COLUMN_NAME_ID + ") as IZIN_ID from " + PersonelIzin.TABLE_NAME + " I "
						+ PdksEntityController.getSelectLOCK() + " ");
				sb.append(" inner join " + Personel.TABLE_NAME + " P on P." + Personel.COLUMN_NAME_ID + " = I." + PersonelIzin.COLUMN_NAME_PERSONEL + " and P." + Personel.COLUMN_NAME_DURUM + " = 1 and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >=convert(date,GETDATE())");
				if (suaSenelikKullan)
					sb.append(" and (P.SUA_OLABILIR is null or P.SUA_OLABILIR<>1 ) ");
				sb.append(" inner join " + IzinTipi.TABLE_NAME + " IT " + PdksEntityController.getJoinLOCK() + " on IT." + IzinTipi.COLUMN_NAME_ID + " = I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " and IT." + IzinTipi.COLUMN_NAME_DEPARTMAN + " = 1 and IT." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI
						+ " is not null ");
				sb.append(" inner join " + Tanim.TABLE_NAME + " T " + PdksEntityController.getJoinLOCK() + " on T." + Tanim.COLUMN_NAME_ID + " = IT." + IzinTipi.COLUMN_NAME_IZIN_TIPI + " and T." + Tanim.COLUMN_NAME_KODU + " IN ('" + IzinTipi.YILLIK_UCRETLI_IZIN + "','" + IzinTipi.SUA_IZNI + "') ");
				sb.append(" where I." + PersonelIzin.COLUMN_NAME_IZIN_SURESI + " > 0 and I." + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + " not in (8,9) ");
				sb.append(" group by YEAR(I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ")  ,I." + PersonelIzin.COLUMN_NAME_PERSONEL + ",I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI);
				sb.append(" ), ");
				sb.append(" CIFT_IZIN as ( ");
				sb.append(" select " + PersonelIzin.COLUMN_NAME_PERSONEL + ",YIL,min(IZIN_ID) as IZIN_1,Max(IZIN_ID) as IZIN_2 from IZIN_BAKIYELER ");
				sb.append(" group by " + PersonelIzin.COLUMN_NAME_PERSONEL + ",YIL ");
				sb.append(" having count(*)=2 ");
				sb.append(" ), ");
				sb.append(" DATA as ( ");
				sb.append(" select P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + ",P." + Personel.COLUMN_NAME_AD + " + ' '+ P." + Personel.COLUMN_NAME_SOYAD + " as PERSONEL,V.* from CIFT_IZIN V ");
				sb.append(" inner join " + Personel.TABLE_NAME + " P on P." + Personel.COLUMN_NAME_ID + " = V." + PersonelIzin.COLUMN_NAME_PERSONEL);
				sb.append(" )  ");
				sb.append("select I.* from DATA V " + PdksEntityController.getSelectLOCK() + " ");
				sb.append(" inner join " + PersonelIzin.TABLE_NAME + " I " + PdksEntityController.getJoinLOCK() + " on I." + PersonelIzin.COLUMN_NAME_ID + " = V.IZIN_1 or I." + PersonelIzin.COLUMN_NAME_ID + " = V.IZIN_2");
				sb.append(" where YIL>=YEAR(GETDATE()) ");
				sb.append(" order by V.PERSONEL,V." + Personel.COLUMN_NAME_PDKS_SICIL_NO + ",V.YIL ");
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinList = pdksEntityController.getObjectBySQLList(sb, parametreMap, PersonelIzin.class);
			} catch (Exception e) {
				izinList = new ArrayList<PersonelIzin>();
			}
			sb = null;
			parametreMap = null;
			if (!izinList.isEmpty()) {
				toList = ortakIslemler.IKKullanicilariBul(toList, null, session);
				for (Iterator iterator = toList.iterator(); iterator.hasNext();) {
					User user = (User) iterator.next();
					if (!user.getDepartman().isAdminMi())
						iterator.remove();
				}
				if (!toList.isEmpty())
					ortakIslemler.mailGonder(renderer, "/email/suaSenelikIzinMail.xhtml");

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}

	}

	/**
	 * @param user
	 * @param session
	 * @param manuel
	 * @throws Exception
	 */
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void izinleriHesapla(User user, Session session, boolean manuel) throws Exception {
		logger.info("izinBakiyeGuncellemeTimer in " + PdksUtil.getCurrentTimeStampStr());

		Calendar cal = Calendar.getInstance();
		yil = cal.get(Calendar.YEAR);

		try {

			if (session == null)
				session = PdksUtil.getSession(entityManager, Boolean.TRUE);
			hataKonum = "senelikBakiyeIzinEkle basladı ";

			HashMap map = new HashMap();
			List<String> haricKodlar = new ArrayList<String>();
			haricKodlar.add(IzinTipi.SUA_IZNI);
			haricKodlar.add(IzinTipi.YURT_DISI_KONGRE);
			haricKodlar.add(IzinTipi.YURT_ICI_KONGRE);
			haricKodlar.add(IzinTipi.MOLA_IZNI);
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
			map.put(PdksEntityController.MAP_KEY_MAP, "getId");
			map.put(PdksEntityController.MAP_KEY_SELECT, "bakiyeIzinTipi.izinTipiTanim");
			map.put("bakiyeIzinTipi.izinTipiTanim.kodu not ", haricKodlar);
			map.put("bakiyeIzinTipi.durum=", Boolean.TRUE);
			map.put("bakiyeIzinTipi.bakiyeDevirTipi=", IzinTipi.BAKIYE_DEVIR_SENELIK);
			map.put("bakiyeIzinTipi.onaylayanTipi<>", IzinTipi.ONAYLAYAN_TIPI_YOK);
			map.put("bakiyeIzinTipi.personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
			map.put("kotaBakiye>=", 0D);
			hataKonum = "bakiyeIzinTipleri okunuyor ";
			TreeMap<Long, Tanim> senelikBakiyeIzinTipiMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, IzinTipi.class, Boolean.TRUE);
			boolean flush = false;
			for (Iterator iterator = senelikBakiyeIzinTipiMap.keySet().iterator(); iterator.hasNext();) {
				Long key = (Long) iterator.next();
				String izinTipi = senelikBakiyeIzinTipiMap.get(key).getKodu();
				if (izinTipi.equals(IzinTipi.SUA_IZNI) || izinTipi.equals(IzinTipi.YILLIK_UCRETLI_IZIN))
					continue;
				senelikBakiyeIzinEkle(session, user, null, izinTipi, null, yil);
				flush = true;
			}

			izinleriBakiyeleriniHesapla(session, null, null, user, Boolean.TRUE, Boolean.FALSE, manuel, Boolean.TRUE);
			flush = true;
			if (ozelKontrol) {
				Parameter parameter = ortakIslemler.getParameter(session, "suaSenelikKullan");
				boolean suaSenelikKullanma = parameter == null || parameter.getValue() == null || !parameter.getValue().equals("1");
				cal = Calendar.getInstance();
				int haftaGun = cal.get(Calendar.DAY_OF_WEEK);
				if (haftaGun == Calendar.SUNDAY)
					ciftBakiyeIzinKontrol(session);
				else if (suaSenelikKullanma == false && haftaGun != Calendar.SATURDAY)
					senelikSuaIzinKontrol(session);
			}
			if (flush)
				session.flush();
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		hataKonum = "izinleriBakiyeleriniHesapla basladi ";

		if (user != null)
			PdksUtil.addMessageInfo("İzin bakiyeleri güncellenmiştir");

		logger.info("izinBakiyeGuncellemeTimer out " + PdksUtil.getCurrentTimeStampStr());

	}

	/**
	 * @param userSession
	 * @param siciller
	 * @param sirket
	 * @param user
	 * @param yeni
	 * @param gecmisHesapla
	 * @param manuel
	 * @param calisanPersonel
	 */
	@Transactional
	public void izinleriBakiyeleriniHesapla(Session userSession, List<String> siciller, Sirket sirket, User user, boolean yeni, boolean gecmisHesapla, boolean manuel, boolean calisanPersonel) {
		if (userSession == null)
			userSession = PdksUtil.getSession(entityManager, yeni);
		logger.info("izinleriBakiyeleriniHesapla in " + PdksUtil.getCurrentTimeStampStr());
		Date bugun = PdksUtil.getDate(Calendar.getInstance().getTime());
		String parameterName = ortakIslemler.getParametreHakEdisIzinERPTableView();
		boolean izinTipiGiris = ortakIslemler.isExisStoreProcedure(PersonelIzinDetay.SP_NAME, userSession) == false && !ortakIslemler.getParameterKeyHasStringValue(parameterName);
		HashMap map = new HashMap();
		StringBuffer sb = new StringBuffer();
		sb.append("select P.* from " + Personel.TABLE_NAME + " P " + PdksEntityController.getSelectLOCK());
		sb.append(" inner join " + Sirket.TABLE_NAME + " S " + PdksEntityController.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
		if (siciller == null || siciller.isEmpty()) {
			if (sirket != null) {
				sb.append(" and S." + Sirket.COLUMN_NAME_ID + " = :s");
				map.put("s", sirket.getId());
			}

			else {
				sb.append(" and S." + Sirket.COLUMN_NAME_DURUM + " = 1");
				if (!yeni && user != null && !user.isAdmin())
					sb.append(" S." + Sirket.COLUMN_NAME_DEPARTMAN + " =" + user.getDepartman().getId());

			}

		}
		if (izinTipiGiris) {
			sb.append(" inner join " + Departman.TABLE_NAME + " D " + PdksEntityController.getJoinLOCK() + " on D." + Departman.COLUMN_NAME_ID + " = S." + Sirket.COLUMN_NAME_DEPARTMAN);
			sb.append(" and D." + Departman.COLUMN_NAME_IZIN_GIRILEBILIR + " = 1");

		}

		sb.append(" where P." + Personel.COLUMN_NAME_DOGUM_TARIHI + " is not null and P." + Personel.COLUMN_NAME_DURUM + " = 1 and P." + Personel.COLUMN_NAME_IZIN_KARTI_VAR + " = 1");
		map.put(PdksEntityController.MAP_KEY_SESSION, userSession);
		if (calisanPersonel) {
			sb.append(" and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :b1 and P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :b2");
			map.put("b1", bugun);
			map.put("b2", bugun);
		}
		List<Personel> list = null;
		String fieldName = "d";
		if (siciller != null && !siciller.isEmpty()) {
			map.put(fieldName, siciller);
			sb.append(" and P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :" + fieldName);
			list = pdksEntityController.getSQLParamList(siciller, sb, fieldName, map, Personel.class, userSession);
		} else
			list = pdksEntityController.getObjectBySQLList(sb, map, Personel.class);

		if (list.size() > 1)
			list = PdksUtil.sortListByAlanAdi(list, "iseBaslamaTarihi", false);
		if (list.size() > 0)
			logger.info("izinleriBakiyeleriniHesapla " + list.size() + " " + PdksUtil.getCurrentTimeStampStr());
		String sicilNo = null;
		HashMap dataMap = new HashMap();
		int sayac = 0;
		boolean logGoster = PdksUtil.getUrl().indexOf("localhost") >= 0;
		HashMap<String, IzinHakedisHakki> hakedisMap = ortakIslemler.getHakedisMap(userSession);
		User sistemYonetici = ortakIslemler.getSistemAdminUser(userSession);
		LinkedHashMap<String, Object> dataKidemMap = new LinkedHashMap<String, Object>();
		dataKidemMap.put("sistemYonetici", sistemYonetici);
		dataKidemMap.put("hakedisMap", hakedisMap);
		dataKidemMap.put("user", user);
		dataKidemMap.put("dataMap", dataMap);
		dataKidemMap.put("gecmis", gecmisHesapla);
		dataKidemMap.put("yeniBakiyeOlustur", Boolean.TRUE);
		while (!list.isEmpty() && sayac < 10) {
			sayac++;
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Personel pdksPersonel = (Personel) iterator.next();
				Sirket sirketPdks = pdksPersonel.getSirket();
				if (sirketPdks == null || !sirketPdks.getFazlaMesai() || !(izinTipiGiris == false || sirketPdks.isIzinGirer())) {
					iterator.remove();
					continue;
				}
				try {
					if (!(PdksUtil.hasStringValue(pdksPersonel.getSicilNo()) == false || pdksPersonel.getIzinHakEdisTarihi() == null || pdksPersonel.getDogumTarihi() == null)) {
						try {
							sicilNo = String.valueOf(Long.parseLong(pdksPersonel.getSicilNo().trim()));
						} catch (Exception e) {
							sicilNo = pdksPersonel.getSicilNo();
						}
						if (sicilNo != null) {
							if (logGoster)
								logger.debug(sicilNo);
							dataKidemMap.put("personel", pdksPersonel);
							ortakIslemler.getKidemHesabi(dataKidemMap, userSession);
						}

					}
					iterator.remove();
					// if (manuel)
					// list.clear();
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					logger.error(" izinBakiyeGuncelleme hata : " + sicilNo + " " + pdksPersonel.getAdSoyad() + " " + e.getMessage());

					break;
				}

			}
			if (!list.isEmpty()) {
				userSession.flush();
				if (yeni)
					userSession = PdksUtil.getSession(entityManager, yeni);

				if (list.size() > 1)
					Collections.shuffle(list);
			}
		}
		userSession.flush();
		logger.info("izinleriBakiyeleriniHesapla out " + PdksUtil.getCurrentTimeStampStr());
		if (yeni)
			logger.info("izinBakiyeGuncelleme tamam");

	}

	/**
	 * @param session
	 * @param user
	 * @param sql
	 * @param izinTipiKodu
	 * @param bakiye
	 * @param izinYil
	 * @throws Exception
	 */
	private void senelikBakiyeIzinEkle(Session session, User user, String sql, String izinTipiKodu, Integer bakiye, int izinYil) throws Exception {
		StringBuilder queryStr = new StringBuilder();
		String bakiyeTarih = izinYil + "0101";
		queryStr.append(" with IZIN_OZET as ( ");
		queryStr.append(" select TA.ACIKLAMATR as ACIKLAMA, convert(datetime,'" + bakiyeTarih + "', 112) as DONEM, ");
		queryStr.append((bakiye == null ? " T.KOTA_BAKIYE " : String.valueOf(bakiye)) + " as IZIN_SURESI,  B." + Personel.COLUMN_NAME_ID + " PERSONEL_ID,T." + IzinTipi.COLUMN_NAME_ID + " as IZIN_TIPI_ID from " + IzinTipi.TABLE_NAME + " T " + PdksEntityController.getSelectLOCK() + " ");
		queryStr.append(" inner join " + Tanim.TABLE_NAME + " TA " + PdksEntityController.getJoinLOCK() + " on TA." + Tanim.COLUMN_NAME_ID + " = T." + IzinTipi.COLUMN_NAME_IZIN_TIPI + " and TA.kodu='" + izinTipiKodu + "'");
		queryStr.append(" inner join " + Sirket.TABLE_NAME + " S " + PdksEntityController.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_DEPARTMAN + " = T." + IzinTipi.COLUMN_NAME_DEPARTMAN + " and S." + Sirket.COLUMN_NAME_FAZLA_MESAI + " = 1 and S." + Sirket.COLUMN_NAME_DURUM + " = 1 ");
		queryStr.append(" inner join " + Personel.TABLE_NAME + " B " + PdksEntityController.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_ID + " = B." + Personel.COLUMN_NAME_SIRKET + " and B." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= convert(date,GETDATE()) and B."
				+ Personel.COLUMN_NAME_DURUM + " = 1 " + (sql != null ? " and " + sql : ""));
		queryStr.append("  where T." + IzinTipi.COLUMN_NAME_DURUM + " = 1 and T." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " is not null ");
		queryStr.append("  )");
		queryStr.append(" insert into " + PersonelIzin.TABLE_NAME + " (" + PersonelIzin.COLUMN_NAME_DURUM + ", " + PersonelIzin.COLUMN_NAME_OLUSTURMA_TARIHI + ", " + PersonelIzin.COLUMN_NAME_ACIKLAMA + ", " + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + ", " + PersonelIzin.COLUMN_NAME_BITIS_ZAMANI
				+ ",");
		queryStr.append(PersonelIzin.COLUMN_NAME_IZIN_SURESI + ", " + PersonelIzin.COLUMN_NAME_IZIN_DURUMU + ", " + PersonelIzin.COLUMN_NAME_VERSION + ", " + PersonelIzin.COLUMN_NAME_OLUSTURAN + ", " + PersonelIzin.COLUMN_NAME_PERSONEL + ", " + PersonelIzin.COLUMN_NAME_IZIN_TIPI + ")");
		queryStr.append(" select 1 as DURUM, GETDATE() olusturmaTarihi, '" + izinYil + " YILI ' + O.ACIKLAMA as ACIKLAMA, O.DONEM as BASLANGIC_ZAMANI,");
		queryStr.append(" O.DONEM as BITIS_ZAMANI,O.IZIN_SURESI, 4 as IZIN_DURUMU, 0 as version," + user.getId() + " olusturanUser_id ,");
		queryStr.append(" O.PERSONEL_ID, O.IZIN_TIPI_ID from IZIN_OZET O ");
		queryStr.append(" left join " + PersonelIzin.TABLE_NAME + " I " + PdksEntityController.getJoinLOCK() + " on I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " = O.IZIN_TIPI_ID ");
		queryStr.append(" and I." + PersonelIzin.COLUMN_NAME_PERSONEL + " = O.PERSONEL_ID and I." + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + " = O.DONEM");
		queryStr.append(" where I." + PersonelIzin.COLUMN_NAME_ID + " is null");
		String str = queryStr.toString();
		try {
			SQLQuery query = session.createSQLQuery(str);
			query.executeUpdate();
		} catch (Exception e) {
			logger.error(e + "\n" + str);
		}
		queryStr = null;
	}

	public int getYil() {
		return yil;
	}

	public void setYil(int yil) {
		this.yil = yil;
	}

	public String getHataKonum() {
		return hataKonum;
	}

	public void setHataKonum(String hataKonum) {
		this.hataKonum = hataKonum;
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		IzinBakiyeGuncelleme.calisiyor = calisiyor;
	}

	public List<User> getToList() {
		return toList;
	}

	public void setToList(List<User> toList) {
		this.toList = toList;
	}

	public List<PersonelIzin> getIzinList() {
		return izinList;
	}

	public void setIzinList(List<PersonelIzin> izinList) {
		this.izinList = izinList;
	}

	public static boolean isOzelKontrol() {
		return ozelKontrol;
	}

	public static void setOzelKontrol(boolean ozelKontrol) {
		IzinBakiyeGuncelleme.ozelKontrol = ozelKontrol;
	}

}