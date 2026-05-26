package org.pdks.quartz;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
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
import org.pdks.session.PdksUtil;
import org.pdks.session.SSLImport;

@Name("sertifikaSSLKontrol")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class SertifikaSSLKontrol implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5287144140086345430L;

	static Logger logger = Logger.getLogger(SertifikaSSLKontrol.class);

	@In(required = false, create = true)
	HashMap<String, String> parameterMap;

	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = false, create = true)
	Zamanlayici zamanlayici;

	// @In(required = false, create = true)
	// FazlaMesaiGuncelleme fazlaMesaiGuncelleme;

	private static boolean calisiyor = Boolean.FALSE;

	@Asynchronous
	@SuppressWarnings("unchecked")
	@Transactional
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle sertifikaSSLKontrolTimer(@Expiration Date when, @IntervalCron String interval) {

		if (!isCalisiyor()) {
			setCalisiyor(Boolean.TRUE);
			logger.debug("Sertifika SSL Kontrol in " + PdksUtil.getCurrentTimeStampStr());
			try {
				SSLImport.setServisURLList(null);
				SSLImport.addCertToKeyStore(null, null, true);

				logger.debug("Sertifika SSL Kontrol out " + PdksUtil.getCurrentTimeStampStr());
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());

				logger.error("Sertifika SSL Kontrol hata " + e.getMessage() + " " + PdksUtil.getCurrentTimeStampStr());

			} finally {
				setCalisiyor(Boolean.FALSE);

			}

		}

		return null;
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		SertifikaSSLKontrol.calisiyor = calisiyor;
	}

}