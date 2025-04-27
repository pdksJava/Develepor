package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

//@Name("threadAgent")
//@Stateful
public class ThreadAgent extends Thread implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5256821611417250968L;

	static Logger logger = Logger.getLogger(ThreadAgent.class);

	private PdksAgent agent;

	private PdksEntityController pdksEntityController;
	private EntityManager entityManager;
	private Session session = null;

	public ThreadAgent(PdksAgent agent, PdksEntityController pdksEntityController, EntityManager entityManager, Session sessionx) {
		super();
		this.agent = agent;
		this.pdksEntityController = pdksEntityController;
		this.entityManager = entityManager;
		this.session = sessionx;
		try {
			if (sessionx == null && entityManager != null)
				this.session = PdksUtil.getSession(entityManager, Boolean.TRUE);
		} catch (Exception e) {

		}

	}

	// @Remove
	public void remove() {
		if (session != null) {
			session.close();
			session = null;
		}
	}

	@Override
	public void run() {
		// synchronized (ThreadAgent.class) {

		if (pdksEntityController != null && session != null) {
			session.clear();
			StringBuffer sp = new StringBuffer();
			if (agent != null && PdksUtil.hasStringValue(agent.getStoreProcedureAdi())) {
				logger.info(agent.getStoreProcedureAdi() + " " + PdksUtil.convertToDateString(new Date(), "HH:mm"));
				LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
				sp.append(agent.getStoreProcedureAdi());
				try {
					veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					pdksEntityController.execSP(veriMap, sp);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// } else {
				// HashMap fields = new HashMap();
				// sp.append("select * from " + PdksAgent.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
				// fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				// List<PdksAgent> list = pdksEntityController.getObjectBySQLList(sp, fields, PdksAgent.class);
				// if (!list.isEmpty()) {
				// Calendar cal = Calendar.getInstance();
				// int dakika = cal.get(Calendar.MINUTE);
				// int saat = cal.get(Calendar.HOUR_OF_DAY);
				// int gun = cal.get(Calendar.DATE);
				// int hafta = cal.get(Calendar.DAY_OF_WEEK);
				// for (PdksAgent pa : list) {
				// if (pa.getDurum() != null && pa.getDurum() && PdksUtil.hasStringValue(pa.getStoreProcedureAdi())) {
				// boolean dakikaCalistir = true, saatCalistir = true, gunCalistir = true, haftaCalistir = true;
				// String dakikaStr = PdksUtil.hasStringValue(pa.getDakikaBilgi()) ? pa.getDakikaBilgi() : "*";
				// String saatStr = PdksUtil.hasStringValue(pa.getSaatBilgi()) ? pa.getSaatBilgi() : "*";
				// String gunStr = PdksUtil.hasStringValue(pa.getGunBilgi()) ? pa.getGunBilgi() : "*";
				// String haftaStr = PdksUtil.hasStringValue(pa.getHaftaBilgi()) ? pa.getHaftaBilgi() : "*";
				// if (!dakikaStr.equals("*"))
				// dakikaCalistir = kontrol(dakika, dakikaStr);
				//
				// if (!saatStr.equals("*"))
				// saatCalistir = kontrol(saat, saatStr);
				//
				// if (!gunStr.equals("*")) {
				// gunStr = gunStr.toUpperCase(Locale.ENGLISH);
				// if (gunStr.indexOf("L") >= 0) {
				// int sonGun = cal.getActualMaximum(Calendar.DATE);
				// if (gunStr.indexOf(",") > 0) {
				// String[] str = gunStr.split(",");
				// for (int i = 0; i < str.length; i++) {
				// String str1 = str[i];
				// if (str1.equals("L"))
				// gunCalistir = gun == sonGun;
				// else
				// gunCalistir = kontrol(saat, str1);
				// if (gunCalistir)
				// break;
				// }
				//
				// } else {
				//
				// gunCalistir = gun == sonGun;
				// }
				//
				// } else
				// gunCalistir = kontrol(gun, gunStr);
				// }
				//
				// if (!haftaStr.equals("*"))
				// haftaCalistir = kontrol(hafta, haftaStr);
				//
				// if (dakikaCalistir && saatCalistir && gunCalistir && haftaCalistir) {
				// try {
				// ThreadAgent ta = new ThreadAgent(pa, pdksEntityController, entityManager, null);
				// ta.start();
				// } catch (Exception e) {
				// logger.error(e);
				// }
				// }
				// }
				// }
				// }
				// list = null;
			}

		}

		if (agent != null)
			remove();

	}

	/**
	 * @param sayi
	 * @param inputStr
	 * @return
	 */
	public static boolean kontrol(int sayi, String inputStr) {
		boolean calistir = false;
		if (PdksUtil.hasStringValue(inputStr)) {
			if (inputStr.indexOf(",") >= 0) {
				String[] str = inputStr.split(",");
				for (int i = 0; i < str.length; i++) {
					if (kontrol(sayi, str[i])) {
						calistir = true;
						break;
					}
				}
			} else if (inputStr.indexOf("/") >= 0) {
				String[] str = inputStr.split("/");
				int bas = -1;
				int mod = -2;
				if (str.length == 2) {
					try {
						bas = Integer.parseInt(str[0]);
					} catch (Exception e) {
						// TODO: handle exception
					}
					try {
						mod = Integer.parseInt(str[1]);
					} catch (Exception e) {
						// TODO: handle exception
					}
					if (bas == 0 && mod > 0 && sayi >= mod) {
						calistir = sayi % mod == 0;
					}
				}
			} else if (inputStr.indexOf("-") >= 0) {
				int bas = -1;
				int bit = -2;
				String[] str = inputStr.split("-");
				if (str.length == 2) {
					try {
						bas = Integer.parseInt(str[0]);
					} catch (Exception e) {
						// TODO: handle exception
					}
					try {
						bit = Integer.parseInt(str[1]);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				calistir = sayi >= bas && sayi <= bit;

			} else {
				try {
					int inputSayi = Integer.parseInt(inputStr);
					calistir = inputSayi == sayi;
				} catch (Exception e) {
				}
			}
		}
		return calistir;
	}

	public PdksAgent getAgent() {
		return agent;
	}

	public void setAgent(PdksAgent agent) {
		this.agent = agent;
	}

	public PdksEntityController getPdksEntityController() {
		return pdksEntityController;
	}

	public void setPdksEntityController(PdksEntityController pdksEntityController) {
		this.pdksEntityController = pdksEntityController;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

}
