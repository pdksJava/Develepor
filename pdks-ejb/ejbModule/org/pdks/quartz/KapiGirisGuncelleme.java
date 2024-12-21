package org.pdks.quartz;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("kapiGirisGuncelleme")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class KapiGirisGuncelleme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6776373506431071650L;

	static Logger logger = Logger.getLogger(KapiGirisGuncelleme.class);

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

	public static final String SP_NAME = "SP_START_KAPI_HAREKET_UPDATE";

	private static boolean calisiyor = Boolean.FALSE;

	private static Boolean kapiGirisGuncelleDurum;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle kapiGirisGuncellemeTimer(@Expiration Date when, @IntervalCron String interval) {
		if (!isCalisiyor()) {
			setCalisiyor(Boolean.TRUE);
			logger.debug("kapiGirisGuncelleme in " + PdksUtil.getCurrentTimeStampStr());
			Session session = null;
			try {
				String key = "kapiGirisGuncellemeTimer";
				boolean sistemDurum = ortakIslemler.getParameterKeyHasStringValue(key) && (PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum());
				// sistemDurum = true;
				boolean logYaz = false;
				if (sistemDurum) {
					String str = ortakIslemler.getParameterKey(key);
					Integer mesai = 2, mesaiDisi = 10;
					List<String> list = PdksUtil.getListByString(str, null);
					for (String parca : list) {
						String string = parca.trim();
						if (string.length() > 1) {
							if (string.startsWith("+")) {
								try {
									mesai = Integer.parseInt(string.substring(1));
								} catch (Exception e) {
									mesai = 0;
								}
							} else if (string.startsWith("-")) {
								try {
									mesaiDisi = Integer.parseInt(string.substring(1));
								} catch (Exception e) {
									mesaiDisi = 0;
								}

							}
						} else if (!logYaz)
							logYaz = string.equalsIgnoreCase("L");

					}
					if (mesai == null || mesai < 1)
						mesai = 2;
					if (mesaiDisi == null || mesaiDisi < 1)
						mesaiDisi = 10;

					Calendar cal = Calendar.getInstance();
					int dakika = cal.get(Calendar.MINUTE);
					if (dakika != 0) {
						int saat = cal.get(Calendar.HOUR_OF_DAY);
						if (saat >= 8 && saat <= 21)
							sistemDurum = dakika % mesai == 0;
						else
							sistemDurum = dakika % mesaiDisi == 0;
					}

				}
				if (sistemDurum) {
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					if (session != null) {
						if (logYaz)
							logger.info("kapiGirisGuncellemeTimer start " + PdksUtil.getCurrentTimeStampStr());
						kapiGirisGuncellemeBasla(false, session);
						if (logYaz)
							logger.info("kapiGirisGuncellemeTimer stop " + PdksUtil.getCurrentTimeStampStr());
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

		}

		return null;
	}

	/**
	 * @param manuel
	 * @param session
	 * @throws Exception
	 */
	@Transactional
	public void kapiGirisGuncellemeBasla(boolean manuel, Session session) throws Exception {
		ortakIslemler.kapiGirisGuncelle(PdksUtil.getDate(PdksUtil.tariheGunEkleCikar(new Date(), -61)), null, session);
	}

	public boolean kapiGirisGuncellemeKontrol(Session session) {
		if (kapiGirisGuncelleDurum == null)
			kapiGirisGuncelleDurum = ortakIslemler.isExisStoreProcedure(SP_NAME, session);

		return kapiGirisGuncelleDurum;
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		KapiGirisGuncelleme.calisiyor = calisiyor;
	}

	public static Boolean getKapiGirisGuncelleDurum() {
		return kapiGirisGuncelleDurum;
	}

	public static void setKapiGirisGuncelleDurum(Boolean kapiGirisGuncelleDurum) {
		KapiGirisGuncelleme.kapiGirisGuncelleDurum = kapiGirisGuncelleDurum;
	}

}