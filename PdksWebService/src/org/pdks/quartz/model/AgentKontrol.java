package org.pdks.quartz.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.pdks.dao.PdksDAO;
import org.pdks.entity.CalismaModeliGun;
import org.pdks.entity.Parameter;
import org.pdks.entity.PdksAgent;
import org.pdks.entity.PersonelDinamikAlan;
import org.pdks.entity.ServiceData;
import org.pdks.entity.Tatil;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.MailManager;
import org.pdks.genel.model.PdksUtil;
import org.pdks.genel.model.ThreadAgent;
import org.pdks.mail.model.MailObject;
import org.pdks.mail.model.MailPersonel;
import org.pdks.mail.model.MailStatu;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserDigerOrganizasyon;
import org.pdks.security.entity.UserRoles;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
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
		try {
			dbEPostaGonder(dAO);
		} catch (Exception e) {

		}

	}

	/**
	 * @param dAO
	 */
	private void dbEPostaGonder(PdksDAO dAO) {
		String paramName = "dbEPosta";
		HashMap fields = new HashMap();
		StringBuffer sp = new StringBuffer();
		sp.append("select S.* from " + ServiceData.TABLE_NAME + " S " + PdksVeriOrtakAktar.getSelectLOCK());
//		sp.append(" inner join " + ServiceData.TABLE_NAME + " S " + PdksVeriOrtakAktar.getJoinLOCK() + " on S." + ServiceData.COLUMN_NAME_FONKSIYON_ADI + " = P." + Parameter.COLUMN_NAME_ADI);
		sp.append(" where S." + ServiceData.COLUMN_NAME_FONKSIYON_ADI + " = :f");
		// sp.append(" and P." + Parameter.COLUMN_NAME_DURUM + " = 1 and P." + Parameter.COLUMN_NAME_DEGER + " = '1'");
		sp.append(" order by S." + ServiceData.COLUMN_NAME_ID);
		fields.put("f", paramName);
		List<ServiceData> mailDataList = dAO != null ? dAO.getNativeSQLList(fields, sp, ServiceData.class) : null;
		if (mailDataList != null) {
			if (!mailDataList.isEmpty()) {
				PdksVeriOrtakAktar pdksVeriOrtakAktar = new PdksVeriOrtakAktar();
				HashMap<String, Object> mailMap = pdksVeriOrtakAktar.sistemVerileriniYukle(dAO, false);
				MailStatu mailStatu = null;
				Gson gson = new Gson();
				List<String> mailStrList = new ArrayList<String>(), pasifList = new ArrayList<String>();
				List<ServiceData> mailDataDeleteList = new ArrayList<ServiceData>();
				for (ServiceData serviceData : mailDataList) {
					if (mailMap.containsKey("mailObject"))
						mailMap.remove("mailObject");
					MailObject mailObject = new MailObject();
					LinkedTreeMap<String, Object> dataMap = null;
					LinkedHashMap<String, Object> map = null;
					String jsonStr = serviceData.getOutputData();
					try {
						map = gson.fromJson(jsonStr, LinkedHashMap.class);
						if (map.containsKey("mail")) {
							List list = (List) map.get("mail");
							dataMap = (LinkedTreeMap<String, Object>) list.get(0);
							if (dataMap != null) {
								setMailList("toAdres", mailStrList, mailObject.getToList(), dataMap);
								setMailList("ccAdres", mailStrList, mailObject.getCcList(), dataMap);
								setMailList("bccAdres", mailStrList, mailObject.getBccList(), dataMap);
							}
							List<User> userList = null;
							TreeMap<String, User> userMap = new TreeMap<String, User>();
							if (mailStrList.isEmpty() == false) {
								fields.clear();
								fields.put("email", mailStrList.size() > 1 ? mailStrList : mailStrList.get(0));
								userList = dAO.getObjectByInnerObjectList(fields, User.class);
								for (User user : userList) {
									if (user.isDurum() && user.getPdksPersonel().isCalisiyor())
										userMap.put(user.getEmail(), user);
									else {
										mailStrList.remove(user.getEmail());
										pasifList.add(user.getEmail());
									}
								}
							}
							if (!userMap.isEmpty()) {
								PdksVeriOrtakAktar.mailUserListKontrol(mailObject.getToList(), userMap, pasifList);
								PdksVeriOrtakAktar.mailUserListKontrol(mailObject.getCcList(), userMap, pasifList);
								PdksVeriOrtakAktar.mailUserListKontrol(mailObject.getBccList(), userMap, pasifList);
							}
							userList = null;
							if (mailStrList.isEmpty() == false) {
								if (dataMap.containsKey("body"))
									mailObject.setBody((String) dataMap.get("body"));
								if (mailObject.getBody() != null) {
									// mailObject.getToList().clear();
									// MailPersonel mailPersonel = new MailPersonel();
									// mailPersonel.setePosta("hasansayar58@gmail.com");
									// mailObject.getToList().add(mailPersonel);
									mailObject.setSubject(serviceData.getInputData());
									mailMap.put("mailObject", mailObject);
									mailStatu = MailManager.ePostaGonder(mailMap);
								}
								if (mailStatu != null && mailStatu.isDurum())
									mailDataDeleteList.add(serviceData);
							}
						}
					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
					mailObject = null;
					mailStrList.clear();
					pasifList.clear();
				}
				if (mailDataDeleteList.isEmpty() == false)
					dAO.deleteObjectList(mailDataDeleteList);
				pasifList = null;
				mailStrList = null;
				mailDataDeleteList = null;
			}
			mailDataList = null;

		}
	}

	/**
	 * @param key
	 * @param mailStrList
	 * @param mailList
	 * @param dataMap
	 */
	private void setMailList(String key, List<String> mailStrList, List<MailPersonel> mailList, LinkedTreeMap<String, Object> dataMap) {
		if (dataMap.containsKey(key)) {
			List<String> list = PdksUtil.getListFromString((String) dataMap.get(key), null);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				String ePosta = (String) iterator.next();
				if (mailStrList.contains(ePosta)) {
					iterator.remove();
				} else {
					MailPersonel mailPersonel = new MailPersonel();
					mailPersonel.setePosta(ePosta);
					mailList.add(mailPersonel);
					mailStrList.add(ePosta);
				}

			}
			list = null;
		}

	}

}
