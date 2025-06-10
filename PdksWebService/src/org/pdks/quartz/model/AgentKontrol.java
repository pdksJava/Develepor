package org.pdks.quartz.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.pdks.dao.PdksDAO;
import org.pdks.entity.CalismaModeliGun;
import org.pdks.entity.Parameter;
import org.pdks.entity.PdksAgent;
import org.pdks.entity.PersonelDinamikAlan;
import org.pdks.entity.ServiceData;
import org.pdks.entity.Tatil;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.PdksUtil;
import org.pdks.genel.model.ThreadAgent;
import org.pdks.security.entity.UserDigerOrganizasyon;
import org.pdks.security.entity.UserRoles;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;

import com.pdks.webService.PdksVeriOrtakAktar;

public final class AgentKontrol extends QuartzJobBean {

	private Logger logger = Logger.getLogger(AgentKontrol.class);

	private static boolean calisiyor = false;

	@Transactional
	public void savePrepareAllTableID(PdksDAO pdksDAO) {
		List<Class> list = new ArrayList<Class>();
		long toplamAdet = 0L;
		try {
			list.add(CalismaModeliGun.class);
			list.add(Parameter.class);
			list.add(PersonelDinamikAlan.class);
			list.add(ServiceData.class);
			list.add(Tatil.class);
			list.add(UserDigerOrganizasyon.class);
			list.add(UserRoles.class);
			for (Class class1 : list)
				pdksDAO.savePrepareTableID(false, class1);

		} catch (Exception e) {
			logger.error(e);
		}
		if (toplamAdet > 0)
			logger.info(toplamAdet + " adet kayıt id güncellendi.");

		list = null;
	}

	// ERROR //
	protected void executeInternal(org.quartz.JobExecutionContext ctx) throws org.quartz.JobExecutionException {
		if (!calisiyor) {
			try {
				calisiyor = true;
				PdksDAO pdksDAO = Constants.pdksDAO;
				if (pdksDAO != null)
					runAgentService(pdksDAO);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			} finally {
				calisiyor = false;
			}

		}

	}

	/**
	 * @param dAO
	 */
	private void runAgentService(PdksDAO dAO) {
		Calendar cal = Calendar.getInstance();
		HashMap fields = new HashMap();
		StringBuffer sp = new StringBuffer();
		sp.append("select * from " + PdksAgent.TABLE_NAME + " " + PdksVeriOrtakAktar.getSelectLOCK());
		List<PdksAgent> list = dAO != null ? dAO.getNativeSQLList(fields, sp, PdksAgent.class) : null;
		if (list != null && !list.isEmpty()) {
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
							ThreadAgent ta = new ThreadAgent(pa, dAO);
							ta.start();
						} catch (Exception e) {
							logger.error(e);
						}
					}
				}
			}
		}
		list = null;
	}

}
