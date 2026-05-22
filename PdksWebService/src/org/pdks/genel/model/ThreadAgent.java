package org.pdks.genel.model;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.pdks.dao.PdksDAO;
import org.pdks.dao.impl.BaseDAOHibernate;
import org.pdks.entity.PdksAgent;

public class ThreadAgent extends Thread implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5256821611417250968L;

	static Logger logger = Logger.getLogger(ThreadAgent.class);

	private PdksAgent agent;

	private PdksDAO pdksDAO = null;

	public ThreadAgent(PdksAgent agent, PdksDAO dAO) {
		super();
		this.agent = agent;
		this.pdksDAO = dAO != null ? dAO : Constants.pdksDAO;
	}

	// @Remove
	public void remove() {
	}

	@Override
	public void run() {
		if (pdksDAO != null) {
			String programAdi = agent != null ? agent.getStoreProcedureAdi() : null;
			if (PdksUtil.hasStringValue(programAdi)) {
				int index = programAdi.indexOf(".page.xml");
				if (index < 0) {
					String str = agent.getAciklama() + " --> " + agent.getStoreProcedureAdi() + " : " + PdksUtil.convertToDateString(new Date(), "HH:mm");
					logger.info(str);
					LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
					try {
						veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, agent.getStoreProcedureAdi());
						if (agent.getUpdateSP())
							pdksDAO.execSP(veriMap);
						else
							pdksDAO.execSPList(veriMap, null);

					} catch (Exception e) {
						logger.error(agent.getStoreProcedureAdi() + "\n" + str + "\nHata : " + e);
						e.printStackTrace();
					}
					mailGonder();
				} else {

					File file = new File("/opt/sertifika/web.txt");
					if (file.exists()) {
						List<String> dosyaList = null;
						try {
							dosyaList = PdksUtil.getStringListFromFile(file);
						} catch (Exception e) {
						}
						if (dosyaList != null && dosyaList.isEmpty() == false) {
							for (String string : dosyaList) {
								if (string.startsWith("http") && string.indexOf("login") > 1) {
									String adres = PdksUtil.replaceAllManuel(string, "login", programAdi.substring(0, index));
									logger.info(agent.getAciklama() + " --> " + adres + " " + PdksUtil.getCurrentTimeStampStr());
									PdksUtil.adresKontrol(adres);

								}

							}
						}
					}

				}

			}
		}
	}

	private void mailGonder() {
		String loginAdres = PdksUtil.getLoginAdres();
		if (PdksUtil.hasStringValue(loginAdres)) {
			String adres = PdksUtil.replaceAllManuel(loginAdres, "login", "pdksAgent");
			PdksUtil.adresKontrol(adres);
		}
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
					}
					try {
						mod = Integer.parseInt(str[1]);
					} catch (Exception e) {
					}
					if (bas == 0 && mod > 0 && (sayi == 0 || sayi >= mod)) {
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
					}
					try {
						bit = Integer.parseInt(str[1]);
					} catch (Exception e) {
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

	public PdksDAO getPdksDAO() {
		return pdksDAO;
	}

	public void setPdksDAO(PdksDAO pdksDAO) {
		this.pdksDAO = pdksDAO;
	}

}
