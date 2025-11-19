package org.pdks.quartz;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

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
				if (PdksUtil.hasStringValue(agent.getStoreProcedureAdi())) {
					logger.info(agent.getAciklama() + " --> " + agent.getStoreProcedureAdi() + (agent.getStart() ? " (manuel)" : " " + PdksUtil.convertToDateString(new Date(), "HH:mm")));
					LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
					try {
						veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);

						if (agent.getUpdateSP())
							pdksEntityController.execSP(veriMap, agent.getStoreProcedureAdi());
						else
							pdksEntityController.execSPList(veriMap, agent.getStoreProcedureAdi(), null);
					} catch (Exception e) {
						e.printStackTrace();
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
