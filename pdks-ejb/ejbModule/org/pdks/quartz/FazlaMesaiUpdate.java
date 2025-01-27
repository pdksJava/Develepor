package org.pdks.quartz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
import org.jboss.seam.faces.Renderer;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Departman;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;
import org.pdks.session.FazlaMesaiHesaplaHome;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.pdks.session.VardiyaGunHome;

@Name("fazlaMesaiUpdate")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class FazlaMesaiUpdate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7638125510272694759L;

	static Logger logger = Logger.getLogger(FazlaMesaiUpdate.class);

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	public Zamanlayici zamanlayici;
	@In
	EntityManager entityManager;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;
	@In(required = false, create = true)
	FazlaMesaiHesaplaHome fazlaMesaiHesaplaHome;
	@In(required = false, create = true)
	VardiyaGunHome vardiyaGunHome;

	private static boolean calisiyor = Boolean.FALSE;
	private static final String PARAMETER_KEY = "fazlaMesaiUpdate";
	private String hataKonum;
	private Long tesisId;
	private Sirket sirket;
	private DenklestirmeAy denklestirmeAy;
	private User loginUser;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle fazlaMesaiUpdateTimer(@Expiration Date when, @IntervalCron String interval) {
		hataKonum = "fazlaMesaiUpdateTimer başladı ";
		hataKonum = "fazlaMesaiUpdateTimer kontrol ediliyor ";
		if (pdksEntityController != null && !isCalisiyor()) {
			Session session = null;
			try {
				setCalisiyor(Boolean.TRUE);
				// logger.error("Ise gelme durumu " + new Date());
				session = PdksUtil.getSession(entityManager, Boolean.TRUE);
				hataKonum = "Paramatre okunuyor ";
				Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
				String value = (parameter != null) ? parameter.getValue() : "";
				String[] saatler = new String[] { "06:30", "18:30" };
				for (int i = 0; i < saatler.length; i++) {
					String saat = saatler[i];
					if (value.indexOf(saat) < 0)
						value += (value.length() > 0 ? "," : "") + saat;
				}
				hataKonum = "Paramatre okundu ";
				if (value != null) {
					hataKonum = "Zaman kontrolu yapılıyor ";
					Date time = ortakIslemler.getBugun();
					boolean zamanDurum = PdksUtil.zamanKontrol(PARAMETER_KEY, value, time);
					if (zamanDurum)
						fazlaMesaiUpdateBul(session);

				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.error("fazlaMesaiUpdateTimer : " + e.getMessage());

			} finally {
				if (session != null)
					session.close();
				setCalisiyor(Boolean.FALSE);
			}

		}
		return null;
	}

	@Transactional
	public String sirketFazlaMesaiGuncelleme(Session session) {
		loginUser = ortakIslemler != null ? ortakIslemler.getSistemAdminUser(session) : null;
		loginUser.setAdmin(Boolean.TRUE);
		int yil = denklestirmeAy.getYil(), ay = denklestirmeAy.getAy();
		DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
		AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(ay, yil, denklestirmeDonemi, session);
		aylikPuantaj.setLoginUser(loginUser);
		aylikPuantaj.setDenklestirmeAy(denklestirmeAy);
		denklestirmeDonemi.setDenklestirmeAy(denklestirmeAy);
		denklestirmeDonemi.setLoginUser(loginUser);
		Departman departman = sirket.getDepartman();
		fazlaMesaiHesaplaHome.setYil(yil);
		fazlaMesaiHesaplaHome.setAy(ay);
		fazlaMesaiHesaplaHome.setSicilNo("");
		fazlaMesaiHesaplaHome.setDepartman(departman);
		fazlaMesaiHesaplaHome.setSirket(sirket);
		fazlaMesaiHesaplaHome.setSirketId(sirket.getId());
		fazlaMesaiHesaplaHome.setDenklestirmeAy(denklestirmeAy);
		fazlaMesaiHesaplaHome.setStajerSirket(false);
		fazlaMesaiHesaplaHome.setSession(session);
		fazlaMesaiHesaplaHome.setHataliPuantajGoster(false);
		fazlaMesaiHesaplaHome.setSicilNo("");
		fazlaMesaiHesaplaHome.setSeciliEkSaha4Id(null);
		fazlaMesaiHesaplaHome.setBakiyeGuncelle(denklestirmeAy.getDurum() == false);
		fazlaMesaiHesaplaHome.setDenklestirmeAyDurum(true);
		if (!denklestirmeAy.getDurum())
			fazlaMesaiHesaplaHome.setBakiyeGuncelle(true);

		vardiyaGunHome.setDenklestirmeAy(denklestirmeAy);
		vardiyaGunHome.setYil(denklestirmeAy.getYil());
		vardiyaGunHome.setAy(denklestirmeAy.getAy());

		boolean denklestirme = loginUser.isAdmin() == false;
		try {
			LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
			paramMap.put("loginUser", loginUser);
			paramMap.put("denklestirmeDonemi", denklestirmeDonemi);
			paramMap.put("aylikPuantaj", aylikPuantaj);
			paramMap.put("seciliSirket", sirket);
			paramMap.put("denklestirme", denklestirme);
			if (sirket.isTesisDurumu()) {
				if (tesisId != null) {
					paramMap.put("seciliTesisId", tesisId);
					denklestirme = bolumFazlaMesai(paramMap, session);
				} else {
					List<SelectItem> tesisDetayList = fazlaMesaiOrtakIslemler.getFazlaMesaiTesisList(sirket, aylikPuantaj, loginUser.isAdmin() == false, session);
					for (SelectItem selectItem3 : tesisDetayList) {
						Long tesis1Id = (Long) selectItem3.getValue();
						paramMap.put("seciliTesisId", tesis1Id);
						denklestirme = bolumFazlaMesai(paramMap, session);
						if (!denklestirme)
							break;
					}
				}

			} else
				denklestirme = bolumFazlaMesai(paramMap, session);

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * @param paramMap
	 * @return
	 */
	@Transactional
	private boolean bolumFazlaMesai(LinkedHashMap<String, Object> paramMap, Session session) {
		DepartmanDenklestirmeDonemi denklestirmeDonemi = (DepartmanDenklestirmeDonemi) paramMap.get("denklestirmeDonemi");
		int yil = denklestirmeAy.getYil(), ay = denklestirmeAy.getAy();
		AylikPuantaj aylikPuantaj = (AylikPuantaj) paramMap.get("aylikPuantaj");
		Sirket seciliSirket = (Sirket) paramMap.get("seciliSirket");
		Long seciliTesisId = paramMap.containsKey("seciliTesisId") ? (Long) paramMap.get("seciliTesisId") : null;
		List<SelectItem> bolumList = fazlaMesaiOrtakIslemler.getFazlaMesaiBolumList(seciliSirket, seciliTesisId != null ? String.valueOf(seciliTesisId) : "", aylikPuantaj, loginUser.isAdmin() == false, session);
		fazlaMesaiHesaplaHome.setTesisId(seciliTesisId);
		fazlaMesaiHesaplaHome.setTopluGuncelle(true);

		Tanim tesis = null;
		if (seciliTesisId != null && seciliSirket.isTesisDurumu()) {

			tesis = (Tanim) pdksEntityController.getSQLParamByFieldObject(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, seciliTesisId, Tanim.class, session);
		}
		String baslik = denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " " + (seciliSirket.getSirketGrup() == null ? seciliSirket.getAd() : seciliSirket.getSirketGrup().getAciklama()) + (tesis != null ? " " + tesis.getAciklama() : "");
		boolean hataYok = true;

		AramaSecenekleri as = new AramaSecenekleri();
		if (loginUser.isAdmin()) {
			as.setSicilNo("");
			as.setDepartman(sirket.getDepartman());
			as.setDepartmanId(as.getDepartman().getId());
			as.setSirket(sirket);
			as.setSirketId(sirket.getId());
			as.setTesisId(tesisId);
			as.setLoginUser(loginUser);
		}
		Date basGun = PdksUtil.convertToJavaDate(String.valueOf(yil * 100 + ay) + "01", "yyyyMMdd"), bugun = new Date();
		boolean gelecekTarih = basGun.after(bugun);
		for (SelectItem selectItem : bolumList) {
			Long seciliEkSaha3Id = (Long) selectItem.getValue();
			fazlaMesaiHesaplaHome.setSeciliEkSaha3Id(seciliEkSaha3Id);
			try {

				Tanim bolum = (Tanim) pdksEntityController.getSQLParamByFieldObject(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, seciliEkSaha3Id, Tanim.class, session);
				String str = baslik + (bolum != null ? " " + bolum.getAciklama() : "");
				List<Personel> donemPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(sirket, tesisId != null ? String.valueOf(tesisId) : null, seciliEkSaha3Id, null, aylikPuantaj, false, session);
				int kayitAdet = donemPerList != null ? donemPerList.size() : 0;
				if (loginUser.isAdmin() || gelecekTarih) {
					as.setEkSaha3Id(seciliEkSaha3Id);
					boolean devam = kayitAdet > 0;
					int adet = 0;
					while (devam && adet < 2) {
						session.clear();
						List<Personel> donemCPPerList = fazlaMesaiOrtakIslemler.getFazlaMesaiPersonelList(denklestirmeAy, donemPerList, session);
						try {
							devam = donemCPPerList != null && kayitAdet != donemCPPerList.size();
							if (devam) {
								logger.info(str + " aylikPuantajOlusturuluyor in " + PdksUtil.getCurrentTimeStampStr());
								vardiyaGunHome.setSession(session);
								vardiyaGunHome.setAramaSecenekleri(as);
								vardiyaGunHome.aylikPuantajOlusturuluyor();
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
				logger.info(str + " [ " + donemPerList.size() + " ] in " + PdksUtil.getCurrentTimeStampStr());
				donemPerList = null;
				loginUser.setAdmin(Boolean.TRUE);
				List<AylikPuantaj> puantajList = null;
				if (kayitAdet > 0 && gelecekTarih == false)
					puantajList = fazlaMesaiHesaplaHome.fillPersonelDenklestirmeDevam(null, aylikPuantaj, denklestirmeDonemi);
				if (puantajList != null && !puantajList.isEmpty()) {
					session.flush();
				}
				logger.info(str + (puantajList != null ? " [ " + puantajList.size() + " ]" : "") + " out " + PdksUtil.getCurrentTimeStampStr());
			} catch (Exception e) {
				logger.error(seciliEkSaha3Id + " " + e);
				e.printStackTrace();
				hataYok = false;
			}
			if (hataYok == false)
				break;

		}
		logger.info(baslik + " OK " + PdksUtil.getCurrentTimeStampStr());

		return hataYok;
	}

	/**
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public String fazlaMesaiUpdateBul(Session session) throws Exception {
		Date bugun = ortakIslemler.getBugun();
		Date oncekiDonem = PdksUtil.tariheAyEkleCikar(bugun, -2);
		HashMap fields = new HashMap();
		int d1 = Integer.parseInt(PdksUtil.convertToDateString(oncekiDonem, "yyyyMM")), d2 = Integer.parseInt(PdksUtil.convertToDateString(bugun, "yyyyMM"));
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + DenklestirmeAy.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
		sb.append(" where " + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1 and ( ( " + DenklestirmeAy.COLUMN_NAME_YIL + " * 100 + " + DenklestirmeAy.COLUMN_NAME_AY + " ) between " + d1 + " and " + d2 + " )");
		sb.append(" order by " + DenklestirmeAy.COLUMN_NAME_YIL + ", " + DenklestirmeAy.COLUMN_NAME_AY);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<DenklestirmeAy> list = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
		if (fazlaMesaiOrtakIslemler != null && fazlaMesaiHesaplaHome != null && vardiyaGunHome != null) {
			List<Long> grupSirketList = new ArrayList<Long>();
			for (DenklestirmeAy dm : list) {
				denklestirmeAy = dm;
				AylikPuantaj aylikPuantajDonem = new AylikPuantaj(dm);
				List<SelectItem> departmanListe = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(denklestirmeAy != null ? aylikPuantajDonem : null, false, session);
				for (SelectItem selectItem : departmanListe) {
					Long departmanId = (Long) selectItem.getValue();
					List<SelectItem> sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, denklestirmeAy != null ? aylikPuantajDonem : null, false, session);
					for (SelectItem selectItem2 : sirketler) {
						session.clear();
						sirket = (Sirket) pdksEntityController.getSQLParamByAktifFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, selectItem2.getValue(), Sirket.class, session);
						if (sirket != null) {
							if (sirket.getSirketGrup() != null) {
								if (grupSirketList.contains(sirket.getSirketGrup().getId()))
									continue;
								grupSirketList.add(sirket.getSirketGrup().getId());

							}
							sirketFazlaMesaiGuncelleme(session);

						}

					}
				}
			}
			grupSirketList = null;
		}
		return "";
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		FazlaMesaiUpdate.calisiyor = calisiyor;
	}

	public String getHataKonum() {
		return hataKonum;
	}

	public void setHataKonum(String hataKonum) {
		this.hataKonum = hataKonum;
	}

}