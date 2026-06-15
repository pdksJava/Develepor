package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.PdksAgent;
import org.pdks.entity.Personel;
import org.pdks.entity.ServiceData;
import org.pdks.quartz.ThreadAgent;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailPersonel;
import com.pdks.webservice.MailStatu;

@Name("pdksAgentTanimlamaHome")
public class PdksAgentTanimlamaHome extends EntityHome<PdksAgent> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2910343234370602034L;

	static Logger logger = Logger.getLogger(PdksAgentTanimlamaHome.class);

	@RequestParameter
	Long parameterId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	EntityManager entityManager;

	@In(required = true, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	public static String sayfaURL = "pdksAgentTanimlama";
	private PdksAgent currentAgent;
	private List<PdksAgent> pdksAgentList;

	private Boolean helpDesk, pasifGoster, admin;
	private Long mailId = null;
	private String konu;
	private Session session;

	@Override
	public Object getId() {
		if (parameterId == null) {
			return super.getId();
		} else {
			return parameterId;
		}
	}

	@Override
	public void create() {
		super.create();
	}

	@Transactional
	public String mailDataGonderBasla() {

		try {
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			mailId = req != null ? Long.parseLong(req.getParameter("mailId")) : null;
		} catch (Exception e) {
		}
		try {

			if (mailId != null || PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum()) {

				if (PdksUtil.isSessionKapali(session)) {
					if (authenticatedUser != null)
						session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
					else
						session = PdksUtil.getSession(entityManager, Boolean.TRUE);
				}
				List<ServiceData> mailList = null;
				if (mailId != null)
					mailList = pdksEntityController.getSQLParamByFieldList(ServiceData.TABLE_NAME, ServiceData.COLUMN_NAME_ID, mailId, ServiceData.class, session);

				mailDataGonder(mailList, session);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		pdksEntityController.sessionClose(session);
		return mailId == null ? MenuItemConstant.home : "";
	}

	/**
	 * @param mailList
	 * @param session
	 */
	@Transactional
	public void mailDataGonder(List<ServiceData> mailList, Session session) {
		if (mailList == null)
			mailList = getMailList(session);
		if (mailList.isEmpty() == false) {

			for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
				ServiceData serviceData = (ServiceData) iterator.next();
				mailGonderServisData(serviceData);
			}

			ortakIslemler.sessionFlush(session);
		}
		mailList = null;
	}

	/**
	 * @param sd
	 */
	public void mailGonderServisData(ServiceData sd) {
		Gson gson = new Gson();
		MailStatu mailStatu = null;
		int adet = 0;
		boolean sil = true;
		HashMap<String, Object> veriMap = new HashMap<String, Object>();
		MailObject mail = new MailObject();
		List<LinkedTreeMap<String, Object>> paramList = null, veriler = null;
		String parametreJSON = sd.getInputData();
		String dataJSON = sd.getOutputData();
		try {
			paramList = gson.fromJson(parametreJSON, List.class);
		} catch (Exception e) {
		}
		if (paramList == null && parametreJSON != null) {

			paramList = new ArrayList<LinkedTreeMap<String, Object>>();
			LinkedHashMap<String, Object> paramMap = gson.fromJson(parametreJSON, LinkedHashMap.class);
			LinkedTreeMap<String, Object> params = new LinkedTreeMap<String, Object>();
			params.putAll(paramMap);
			paramList.add(params);
			paramMap = null;

		}
		String baslik = "";
		try {
			if (dataJSON.startsWith("{") == false)
				veriler = gson.fromJson(dataJSON, List.class);
			else {
				LinkedHashMap<String, Object> verilerMap = gson.fromJson(dataJSON, LinkedHashMap.class);
				for (String key : verilerMap.keySet()) {
					baslik = key;
					veriler = (List<LinkedTreeMap<String, Object>>) verilerMap.get(key);
				}
			}
		} catch (Exception e) {
		}
		if (paramList != null && veriler != null) {
			if (sd.getId() != null)
				session.delete(sd);
			try {
				LinkedTreeMap<String, Object> params = paramList.get(0);
				konu = (String) params.get("konu");
				boolean tabloYazDurum = false;
				List<String> toList = new ArrayList<String>(), ccList = new ArrayList<String>(), bccList = new ArrayList<String>();
				List<String> mailAdres = new ArrayList<String>();
				if (params.containsKey("toAdres")) {
					List<String> list = PdksUtil.getListFromString((String) params.get("toAdres"), null);
					if (list != null && list.isEmpty() == false) {
						for (String string : list) {
							if (mailAdres.contains(string))
								continue;
							toList.add(string);
							mailAdres.add(string);
						}
					}
					list = null;
				}
				if (params.containsKey("cc")) {
					List<String> list = PdksUtil.getListFromString((String) params.get("cc"), null);
					if (list != null && list.isEmpty() == false) {
						for (String string : list) {
							if (mailAdres.contains(string))
								continue;
							ccList.add(string);
							mailAdres.add(string);
						}
					}
					list = null;
				}
				if (params.containsKey("bcc")) {
					List<String> list = PdksUtil.getListFromString((String) params.get("bcc"), null);
					if (list != null && list.isEmpty() == false) {
						for (String string : list) {
							if (mailAdres.contains(string))
								continue;
							bccList.add(string);
							mailAdres.add(string);
						}
					}
					list = null;
				}

				if (mailAdres.isEmpty() == false) {
					List<User> userList = pdksEntityController.getSQLParamByFieldList(User.TABLE_NAME, User.COLUMN_NAME_EMAIL, mailAdres, User.class, session);
					HashMap<String, User> userMap = new HashMap<String, User>();
					for (User user : userList)
						userMap.put(user.getEmail(), user);
					userList = null;
					if (params.containsKey("tabloYaz")) {
						Double tabloYaz = (Double) params.get("tabloYaz");
						tabloYazDurum = tabloYaz.intValue() == 1;
					}
					LinkedTreeMap<String, String> alanDurum = null;
					if (params.containsKey("parametre")) {
						Object parametre = params.get("parametre");
						if (parametre instanceof List) {
							List list = (ArrayList) parametre;
							alanDurum = (LinkedTreeMap<String, String>) list.get(0);
						} else
							alanDurum = (LinkedTreeMap<String, String>) parametre;

					} else
						alanDurum = new LinkedTreeMap<String, String>();
					if (params.containsKey("tabloYaz")) {
						Double tabloYaz = (Double) params.get("tabloYaz");
						tabloYazDurum = tabloYaz.intValue() == 1;
					}

					LinkedHashMap<String, String> baslikMap = new LinkedHashMap<String, String>();
					for (LinkedTreeMap<String, Object> linkedHashMap : veriler) {
						for (String key : linkedHashMap.keySet()) {
							if (baslikMap.containsKey(key) == false) {
								String baslikStr = key;
								if (baslikStr.indexOf(" ") < 0) {
									try {
										String method = key + (key.indexOf("Aciklama") > 0 ? "" : "Aciklama");
										String str = (String) PdksUtil.getMethodObject(ortakIslemler, method, null);
										if (PdksUtil.hasStringValue(str))
											baslikStr = str;
									} catch (Exception e) {
									}
								}
								baslikMap.put(key, baslikStr);
							}
						}
					}
					StringBuffer sb = new StringBuffer();
					sb.append("<DIV>");
					if (PdksUtil.hasStringValue(baslik))
						sb.append("<P style='font-size: 20px; font-weight: bold;' align='center'>" + baslik + "</P>");

					if (tabloYazDurum) {
						sb.append("<table class=\"mars\" style=\"border-collapse: collapse;\" border=\"1\"><thead><tr>");
						for (String key : baslikMap.keySet()) {
							sb.append("<th>" + baslikMap.get(key) + "</th>");
						}
						sb.append("</tr></thead><tbody>");
						boolean renk = true;
						for (LinkedTreeMap<String, Object> linkedHashMap : veriler) {
							sb.append("<tr class='" + (renk ? "odd" : "even") + "'>");
							for (String key : baslikMap.keySet()) {
								Object veri = linkedHashMap.containsKey(key) ? linkedHashMap.get(key) : null;
								String alignStr = "", str = "";
								if (veri != null) {
									if (alanDurum.containsKey(key)) {
										str = alanDurum.get(key);
										if (str.equalsIgnoreCase("c") || str.equalsIgnoreCase("d") || str.equalsIgnoreCase("t") || str.equalsIgnoreCase("dt"))
											alignStr = " align='center'";
										else if (str.equalsIgnoreCase("r"))
											alignStr = " align='rigth'";
									}
									if (veri instanceof String == false) {
										try {
											Object value = PdksUtil.numericValueFormatStr(veri, null);
											if (value != null)
												veri = value;
										} catch (Exception e) {
										}

									} else if (str.equalsIgnoreCase("d") || str.equalsIgnoreCase("t") || str.equalsIgnoreCase("dt")) {
										Date tarih = PdksUtil.convertToJavaDate((String) veri, Constants.JSON_TARIH);
										if (tarih != null) {
											if (str.equalsIgnoreCase("dt"))
												veri = PdksUtil.convertToDateString(tarih, PdksUtil.getDateTimeFormat());
											else if (str.equalsIgnoreCase("d"))
												veri = PdksUtil.convertToDateString(tarih, PdksUtil.getDateFormat());
											else
												veri = PdksUtil.convertToDateString(tarih, PdksUtil.getSaatFormat());
										}
									}
								}
								sb.append("<td" + alignStr + ">" + (veri != null ? veri : "") + "</td>");
							}
							sb.append("</tr>");
							renk = !renk;
						}
						sb.append("</tbody></table>");

					}
					sb.append("</DIV>");

					mail.setSubject(konu);
					mail.setBody(sb.toString());

					if (toList.size() + ccList.size() + bccList.size() > 0) {
						if (mailId != null || PdksUtil.getCanliSunucuDurum() == true || PdksUtil.getTestSunucuDurum() == true) {
							adet += mailListKontrol(userMap, toList, mail.getToList());
							adet += mailListKontrol(userMap, ccList, mail.getCcList());
							adet += mailListKontrol(userMap, bccList, mail.getBccList());
						}
					}
					toList = null;
					ccList = null;
					bccList = null;
					if (adet > 0 && params.containsKey("dosyaAdi")) {
						byte[] icerik = null;
						try {
							icerik = getExcelDosya(veriler, alanDurum, baslikMap);
						} catch (Exception e) {
						}
						if (icerik != null) {
							MailFile mf = new MailFile();
							String dosyaAdi = (String) params.get("dosyaAdi");
							mf.setDisplayName(dosyaAdi);
							mf.setIcerik(icerik);
							mail.getAttachmentFiles().add(mf);
						}
					}

					veriMap.put("temizleTOCCList", true);
					veriMap.put("mailObject", mail);

				}

			} catch (Exception e) {
				logger.error(e);
			}

		} else if (sd.getId() != null) {
			sil = false;
			sd.setFonksiyonAdi("mailDosyaGonderilmedi");
			sd.setOlusturmaTarihi(new Date());
			pdksEntityController.saveOrUpdate(session, entityManager, sd);
		}

		if (sil) {
			try {
				mailStatu = adet > 0 ? ortakIslemler.mailSoapServisGonder(veriMap, session) : null;
			} catch (Exception e) {

			}
			if (adet == 0 || (mailStatu != null && mailStatu.getDurum())) {
				logger.info(mail.getSubject() + " mail gönderildi. ");

			}
		}
	}

	/**
	 * @param userMap
	 * @param list
	 * @param mailList
	 */
	private int mailListKontrol(HashMap<String, User> userMap, List<String> list, List<MailPersonel> mailList) {
		if (list != null && list.isEmpty() == false && mailList != null) {
			for (String key : list) {
				MailPersonel mp = new MailPersonel();
				mp.setEPosta(key);
				boolean ekle = true;
				if (userMap != null && userMap.containsKey(key)) {
					User user = userMap.get(key);
					Personel per = user.getPdksPersonel();
					if (user.isDurum() && per.isCalisiyor())
						mp.setAdiSoyadi(per.getAdSoyad());
					else
						ekle = false;
				}
				if (ekle)
					mailList.add(mp);
			}
		}
		return mailList != null ? mailList.size() : 0;
	}

	/**
	 * @param session
	 * @return
	 */
	private List<ServiceData> getMailList(Session session) {
		List<ServiceData> mailList = pdksEntityController.getSQLParamByFieldList(ServiceData.TABLE_NAME, ServiceData.COLUMN_NAME_FONKSIYON_ADI, "mailDosyaGonder", ServiceData.class, session);
		Date bugun = ortakIslemler.getBugun();
		boolean flush = false;
		for (Iterator iterator = mailList.iterator(); iterator.hasNext();) {
			ServiceData sd = (ServiceData) iterator.next();
			if (sd.getOlusturmaTarihi() == null)
				continue;
			Double sure = PdksUtil.getSaatFarki(bugun, sd.getOlusturmaTarihi()).doubleValue() * 60.0d;
			if (sure.intValue() > 5) {
				sd.setFonksiyonAdi("mailDosyaGonderilmedi");
				pdksEntityController.saveOrUpdate(session, entityManager, sd);
				flush = true;
				iterator.remove();
			}

		}
		if (flush)
			ortakIslemler.sessionFlush(session);
		return mailList;
	}

	/**
	 * @param veriler
	 * @param alanDurum
	 * @param baslikMap
	 * @return
	 * @throws Exception
	 */
	private byte[] getExcelDosya(List<LinkedTreeMap<String, Object>> veriler, LinkedTreeMap<String, String> alanDurum, LinkedHashMap<String, String> baslikMap) throws Exception {
		int col = 0, row = 0;
		Workbook wb = new XSSFWorkbook();
		CellStyle header = null;
		CellStyle styleOdd = null, styleOddCenter = null, styleOddDate = null, styleOddDateTime = null, styleOddTime = null, styleOddRight = null, styleOddTutar = null;
		CellStyle styleEven = null, styleEvenCenter = null, styleEvenDate = null, styleEvenDateTime = null, styleEvenTime = null, styleEvenRight = null, styleEvenTutar = null;
		header = ExcelUtil.getStyleHeader(wb);
		styleOdd = ExcelUtil.getStyleOdd(null, wb);
		styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		styleOddRight = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_RIGHT, wb);
		styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		styleOddTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TIME, wb);
		styleOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TUTAR, wb);
		styleEven = ExcelUtil.getStyleEven(null, wb);
		styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		styleEvenRight = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_RIGHT, wb);
		styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
		styleEvenTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TIME, wb);
		styleEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TUTAR, wb);
		Sheet sheet = ExcelUtil.createSheet(wb, konu, false);
		for (String key : baslikMap.keySet()) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(baslikMap.get(key));

		}
		boolean renk = true;
		for (LinkedTreeMap<String, Object> linkedHashMap : veriler) {
			col = 0;
			++row;

			for (String key : baslikMap.keySet()) {
				String alignStr = "";
				String strOrj = "";
				if (alanDurum.containsKey(key)) {
					strOrj = alanDurum.get(key);
					if (strOrj.equalsIgnoreCase("c") || strOrj.equalsIgnoreCase("d") || strOrj.equalsIgnoreCase("t") || strOrj.equalsIgnoreCase("dt"))
						alignStr = "c";
					else if (strOrj.equalsIgnoreCase("r"))
						alignStr = "r";
				}
				Object veri = linkedHashMap.containsKey(key) ? linkedHashMap.get(key) : null;

				if (veri == null)
					veri = "";
				if (strOrj.equalsIgnoreCase("dt") || strOrj.equalsIgnoreCase("t") || strOrj.equalsIgnoreCase("d")) {
					Date tarih = PdksUtil.convertToJavaDate((String) veri, Constants.JSON_TARIH);
					if (tarih != null)
						veri = tarih;

				}
				if (veri instanceof String) {
					String str = (String) veri;
					if (alignStr.equals("c"))
						ExcelUtil.getCell(sheet, row, col++, renk ? styleOddCenter : styleEvenCenter).setCellValue(str);
					else if (alignStr.equals("r"))
						ExcelUtil.getCell(sheet, row, col++, renk ? styleOddRight : styleEvenRight).setCellValue(str);
					else
						ExcelUtil.getCell(sheet, row, col++, renk ? styleOdd : styleEven).setCellValue(str);
				} else if (veri instanceof Date) {
					Date tarih = (Date) veri;
					if (strOrj.equals("dt"))
						ExcelUtil.getCell(sheet, row, col++, renk ? styleOddDateTime : styleEvenDateTime).setCellValue(tarih);
					else if (alignStr.equals("d"))
						ExcelUtil.getCell(sheet, row, col++, renk ? styleOddDate : styleEvenDate).setCellValue(tarih);
					else
						ExcelUtil.getCell(sheet, row, col++, renk ? styleOddTime : styleEvenTime).setCellValue(tarih);
				} else {
					try {
						Double d = new Double(veri.toString());
						Long l = d.longValue();
						if (d.doubleValue() > l.longValue())
							ExcelUtil.getCell(sheet, row, col++, renk ? styleOddTutar : styleEvenTutar).setCellValue(d);
						else
							ExcelUtil.getCell(sheet, row, col++, renk ? styleOddRight : styleEvenRight).setCellValue(l);
					} catch (Exception e) {
						ExcelUtil.getCell(sheet, row, col++, renk ? styleOddRight : styleEvenRight).setCellValue(PdksUtil.numericValueFormatStr(veri, null));
					}

				}

			}
			renk = !renk;
		}
		for (int i = 0; i < col; i++)
			sheet.autoSizeColumn(i);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		byte[] icerik = baos.toByteArray();
		return icerik;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUserCalistiSayfa(entityManager, authenticatedUser, sayfaURL);
		ortakIslemler.setUserMenuItemTime(entityManager, session, sayfaURL);
		admin = authenticatedUser.isAdmin();
		fillPdksAgentList();
	}

	@Transactional
	public String deleteAgent() {
		PdksAgent agent = getInstance();
		pdksEntityController.deleteObject(session, entityManager, agent);
		ortakIslemler.sessionFlush(session);
		try {
			pdksEntityController.savePrepareTableID(true, agent, PdksAgent.class, session);
		} catch (Exception e) {
		}
		ortakIslemler.sessionFlush(session);
		session.clear();

		fillPdksAgentList();

		return "";
	}

	public String agentRun(PdksAgent agent) {
		if (agent.getStart().booleanValue() == false) {
			ThreadAgent threadAgent = new ThreadAgent(agent, pdksEntityController, session);
			threadAgent.start();
			PdksUtil.addMessageAvailableInfo(agent.getAciklama() + " çalışmaya başladı.");
		} else
			PdksUtil.addMessageAvailableWarn(agent.getAciklama() + " çalışıyor!");

		return "";
	}

	public String guncelle(PdksAgent agent) {
		if (agent == null)
			agent = new PdksAgent();

		currentAgent = agent;
		setInstance(agent);
		return "";
	}

	@Transactional
	public String kaydet() {
		PdksAgent agent = getInstance();
		pdksEntityController.saveOrUpdate(session, entityManager, agent);
		ortakIslemler.sessionFlush(session);
		session.clear();
		fillPdksAgentList();
		return "persisted";

	}

	public void instanceRefresh() {
		if (currentAgent.getId() != null)
			session.refresh(currentAgent);
	}

	public String fillPdksAgentList() {

		HashMap parametreMap = new HashMap();
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);

		helpDesk = false;
		StringBuilder sb = new StringBuilder();
		sb.append("select T.* from " + PdksAgent.TABLE_NAME + " T " + PdksEntityController.getSelectLOCK() + " ");
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<PdksAgent> list = pdksEntityController.getObjectBySQLList(sb, parametreMap, PdksAgent.class);

		list = PdksUtil.sortListByAlanAdi(list, "id", admin);

		List<PdksAgent> pasifList = new ArrayList<PdksAgent>();
		try {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				PdksAgent parameter = (PdksAgent) iterator.next();
				parameter.setStart(Boolean.FALSE);
				if (parameter.getDurum().equals(Boolean.FALSE)) {
					pasifList.add(parameter);
					iterator.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!pasifList.isEmpty())
			list.addAll(pasifList);

		pasifList = null;

		setPdksAgentList(list);
		return "";
	}

	public void refreshInstance() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Boolean getHelpDesk() {
		return helpDesk;
	}

	public void setHelpDesk(Boolean helpDesk) {
		this.helpDesk = helpDesk;
	}

	public Boolean getPasifGoster() {
		return pasifGoster;
	}

	public void setPasifGoster(Boolean pasifGoster) {
		this.pasifGoster = pasifGoster;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		PdksAgentTanimlamaHome.sayfaURL = sayfaURL;
	}

	public List<PdksAgent> getPdksAgentList() {
		return pdksAgentList;
	}

	public void setPdksAgentList(List<PdksAgent> pdksAgentList) {
		this.pdksAgentList = pdksAgentList;
	}

	public PdksAgent getCurrentAgent() {
		return currentAgent;
	}

	public void setCurrentAgent(PdksAgent currentAgent) {
		this.currentAgent = currentAgent;
	}

}
