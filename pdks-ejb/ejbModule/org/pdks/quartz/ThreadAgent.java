package org.pdks.quartz;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.pdks.entity.PdksAgent;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

public class ThreadAgent extends Thread implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6307633959371742262L;

	static Logger logger = Logger.getLogger(ThreadAgent.class);

	private PdksAgent agent;

	private Session session = null;

	private PdksEntityController pdksEntityController;

	public ThreadAgent(PdksAgent agent, PdksEntityController controller, Session ses) {
		super();
		this.agent = agent;
		this.session = ses;
		this.agent.setStart(true);
		this.pdksEntityController = controller;
	}

	@Override
	public void run() {
		if (session != null) {
			if (agent != null) {
				String programAdi = agent.getStoreProcedureAdi();
				if (PdksUtil.hasStringValue(programAdi)) {
					int index = programAdi.indexOf(".page.xml");
					if (index < 0) {
						logger.info(agent.getAciklama() + " --> " + programAdi + (agent.getStart() ? " (manuel)" : " " + PdksUtil.convertToDateString(new Date(), "HH:mm")) + " " + PdksUtil.getCurrentTimeStampStr());
						LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
						try {

							if (agent.getUpdateSP())
								pdksEntityController.execSP(session, veriMap, programAdi);
							else
								pdksEntityController.execSPList(session, veriMap, programAdi, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
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
										logger.info(agent.getAciklama() + " --> " + adres + (agent.getStart() ? " (manuel)" : " " + PdksUtil.convertToDateString(new Date(), "HH:mm")) + " " + PdksUtil.getCurrentTimeStampStr());
										PdksUtil.adresKontrol(adres);

									}

								}
							}
						}

					}

				}
				agent.setStart(false);
			}
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

}
