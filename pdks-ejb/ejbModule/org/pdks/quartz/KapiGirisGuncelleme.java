package org.pdks.quartz;

import java.io.Serializable;
import java.util.Date;

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
				boolean sistemDurum = PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum();
				if (sistemDurum) {
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					if (session != null) {
						kapiGirisGuncellemeBasla(false, session);
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
	public void kapiGirisGuncellemeBasla(boolean manuel, Session session) throws Exception {
		ortakIslemler.kapiGirisGuncelle(PdksUtil.getDate(PdksUtil.tariheGunEkleCikar(new Date(), -1)), null, session);
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		KapiGirisGuncelleme.calisiyor = calisiyor;
	}

}