package org.pdks.quartz;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.CalismaModeliAy;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.DepartmanDenklestirmeDonemi;
import org.pdks.entity.FazlaMesaiTalep;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.KatSayi;
import org.pdks.entity.Liste;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelDenklestirmeTasiyici;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.ServiceData;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.enums.PuantajKatSayiTipi;
import org.pdks.security.entity.User;
import org.pdks.session.ExcelUtil;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.pdks.webservice.MailFile;
import com.pdks.webservice.MailObject;
import com.pdks.webservice.MailPersonel;
import com.pdks.webservice.MailStatu;

@Name("planVardiyaHareketGuncelleme")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class PlanVardiyaHareketGuncelleme implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8751158477032830258L;

	static Logger logger = Logger.getLogger(PlanVardiyaHareketGuncelleme.class);

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

	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	public static final String PARAMETER_HAREKET_KEY = "hareketVardiyaZamani";
	public static final String PARAMETER_FAZLA_MESAI_KEY = "fazlaMesaiHesaplamaZamani";
	public static final String PATTERN = "yyyyMMdd";
	public static final String PATTERN_DONEM = "yyyyMM";

	private static boolean calisiyor = Boolean.FALSE;

	private Date bugun;

	public void mailGonder(List<ServiceData> mailList, Session session) {
		if (mailList == null)
			mailList = pdksEntityController.getSQLParamByFieldList(ServiceData.TABLE_NAME, ServiceData.COLUMN_NAME_FONKSIYON_ADI, "mailDosyaGonder", ServiceData.class, session);
		if (mailList.isEmpty() == false) {
			Gson gson = new Gson();
			boolean flush = false;
			for (ServiceData serviceData : mailList) {
				List<LinkedTreeMap<String, Object>> paramList = null, veriler = null;
				String parametreJSON = serviceData.getInputData();
				String dataJSON = serviceData.getOutputData();
				try {
					paramList = gson.fromJson(parametreJSON, List.class);
				} catch (Exception e) {
					// TODO: handle exception
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
					// TODO: handle exception
				}
				if (paramList != null && veriler != null) {
					try {
						LinkedTreeMap<String, Object> params = paramList.get(0);
						String konu = (String) params.get("konu");
						boolean tabloYazDurum = false;
						List<String> toList = new ArrayList<String>(), ccList = new ArrayList<String>(), bccList = new ArrayList<String>();
						List<String> mailAdres = new ArrayList<String>();
						if (params.containsKey("toAdres")) {
							List<String> list = PdksUtil.getListFromString((String) params.get("toAdres"), null);
							for (String string : list) {
								if (mailAdres.contains(string))
									continue;
								toList.add(string);
								mailAdres.add(string);
							}
							list = null;
						}
						if (params.containsKey("cc")) {
							List<String> list = PdksUtil.getListFromString((String) params.get("cc"), null);
							for (String string : list) {
								if (mailAdres.contains(string))
									continue;
								ccList.add(string);
								mailAdres.add(string);
							}
							list = null;
						}
						if (params.containsKey("bcc")) {
							List<String> list = PdksUtil.getListFromString((String) params.get("bcc"), null);
							for (String string : list) {
								if (mailAdres.contains(string))
									continue;
								bccList.add(string);
								mailAdres.add(string);
							}
							list = null;
						}

						if (mailAdres.isEmpty() == false) {
							List<User> userList = pdksEntityController.getSQLParamByAktifFieldList(User.TABLE_NAME, User.COLUMN_NAME_EMAIL, mailAdres, User.class, session);
							HashMap<String, String> userMap = new HashMap<String, String>();
							for (User user : userList) {
								Personel personel = user.getPdksPersonel();
								userMap.put(user.getEmail(), personel.getAdSoyad());
							}
							userList = null;
							if (params.containsKey("tabloYaz")) {
								Double tabloYaz = (Double) params.get("tabloYaz");
								tabloYazDurum = tabloYaz.intValue() == 1;
							}
							LinkedTreeMap<String, String> alanDurum = null;
							if (params.containsKey("parametre")) {
								List list = (ArrayList) params.get("parametre");
								alanDurum = (LinkedTreeMap<String, String>) list.get(0);
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
												// TODO: handle exception
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
										if (veri != null) {
											if (veri instanceof String == false) {
												try {
													Object value = PdksUtil.numericValueFormatStr(veri, null);
													if (value != null)
														veri = value;
												} catch (Exception e) {
													// TODO: handle exception
												}

											}

										}
										String alignStr = "";
										if (alanDurum.containsKey(key)) {
											String str = alanDurum.get(key);
											if (str.equalsIgnoreCase("c") || str.equalsIgnoreCase("d") || str.equalsIgnoreCase("t") || str.equalsIgnoreCase("dt"))
												alignStr = " align='center'";
											else if (str.equalsIgnoreCase("r"))
												alignStr = " align='rigth'";
										}
										sb.append("<td" + alignStr + ">" + (veri != null ? veri : "") + "</td>");
									}
									sb.append("</tr>");
									renk = !renk;
								}
								sb.append("</tbody></table>");

							}
							sb.append("</DIV>");
							MailObject mail = new MailObject();
							mail.setSubject(konu);
							mail.setBody(sb.toString());
							if (toList.isEmpty() == false) {
								if (PdksUtil.getCanliSunucuDurum() == true || PdksUtil.getTestSunucuDurum() == true) {
									for (String key : toList) {
										MailPersonel mp = new MailPersonel();
										mp.setEPosta(key);
										if (userMap.containsKey(key))
											mp.setAdiSoyadi(userMap.get(key));
										mail.getToList().add(mp);
									}
								}
							}
							if (ccList.isEmpty() == false) {
								for (String key : ccList) {
									MailPersonel mp = new MailPersonel();
									mp.setEPosta(key);
									if (userMap.containsKey(key))
										mp.setAdiSoyadi(userMap.get(key));
									mail.getCcList().add(mp);
								}
							}
							if (bccList.isEmpty() == false) {
								for (String key : bccList) {
									MailPersonel mp = new MailPersonel();
									mp.setEPosta(key);
									if (userMap.containsKey(key))
										mp.setAdiSoyadi(userMap.get(key));
									mail.getBccList().add(mp);
								}
							}
							toList = null;
							ccList = null;
							bccList = null;
							if (params.containsKey("dosyaAdi")) {
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
								String dosyaAdi = (String) params.get("dosyaAdi");
								MailFile mf = new MailFile();
								mf.setDisplayName(dosyaAdi);
								mf.setIcerik(baos.toByteArray());
								if (mf.getFile() != null || mf.getIcerik() != null)
									mail.getAttachmentFiles().add(mf);
							}
							HashMap<String, Object> veriMap = new HashMap<String, Object>();
							veriMap.put("temizleTOCCList", true);
							veriMap.put("mailObject", mail);
							veriMap.put("homeRenderer", null);
							veriMap.put("sayfaAdi", null);
							MailStatu mailStatu = ortakIslemler.mailSoapServisGonder(veriMap, session);
							if (mailStatu != null && mailStatu.getDurum())
								logger.info(mail.getSubject() + " mail gönderildi. ");
						} else {

						}
						serviceData.setFonksiyonAdi("mailDosyaGonderildi");
						serviceData.setOlusturmaTarihi(new Date());
						session.saveOrUpdate(serviceData);
						flush = true;
					} catch (Exception e) {
						logger.error(e);
					}

				}

			}
			if (flush)
				session.flush();
		}
		mailList = null;
	}

	@Asynchronous
	@SuppressWarnings("unchecked")
	// @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public QuartzTriggerHandle planVardiyaHareketGuncellemeTimer(@Expiration Date when, @IntervalCron String interval) {
		if (!isCalisiyor()) {
			setCalisiyor(Boolean.TRUE);
			logger.debug("planVardiyaHareketGuncelleme in " + PdksUtil.getCurrentTimeStampStr());
			Session session = null;
			int saniye = 5;
			try {
				List<ServiceData> mailList = pdksEntityController.getSQLParamByFieldList(ServiceData.TABLE_NAME, ServiceData.COLUMN_NAME_FONKSIYON_ADI, "mailDosyaGonder", ServiceData.class, session);
				if (PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum()) {
					Calendar cal = Calendar.getInstance();
					bugun = cal.getTime();
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					Parameter parameterHareket = getParameter(PARAMETER_HAREKET_KEY, session);
					Parameter parameterFazlaMesaiHesaplama = getParameter(PARAMETER_FAZLA_MESAI_KEY, session);
					if (parameterHareket != null || parameterFazlaMesaiHesaplama != null) {
						boolean fazlaMesaiHesaplaDurum = false;
						Date tarih = PdksUtil.getDate(bugun);
						Date basTarih = bugun;
						String konu = null, aciklama = null;
						if (parameterHareket != null) {
							fazlaMesaiHesaplaDurum = ortakIslemler.getParameterKey("sirketFazlaMesaiGuncelleme").equals("1");
							if (parameterFazlaMesaiHesaplama == null && fazlaMesaiHesaplaDurum == false) {
								boolean guncellemeHareketDurum = vardiyaHareketGuncelleme(tarih, session);
								if (guncellemeHareketDurum) {
									konu = parameterHareket.getDescription();
									aciklama = "Plan Vardiya Hareket Güncelleme güncellenmiştir.";
								}
							}
						}
						if (parameterFazlaMesaiHesaplama != null || (parameterHareket != null && fazlaMesaiHesaplaDurum)) {
							basTarih = ortakIslemler.getBugun();
							if (fazlaMesaiGuncelleme(tarih, session) != null) {
								konu = parameterFazlaMesaiHesaplama != null ? parameterFazlaMesaiHesaplama.getDescription() : "Fazla Mesai Toplu Güncelleme";
								aciklama = "Fazla Mesai Toplu güncellenmiştir.";
							}
						}
						if (PdksUtil.getCanliSunucuDurum() && PdksUtil.hasStringValue(konu)) {
							if (PdksUtil.isSessionKapali(session))
								session = PdksUtil.getSession(entityManager, Boolean.TRUE);
							boolean mailGonder = getMailGonder(session);
							if (mailGonder) {
								List<User> userList = null;
								if (ortakIslemler.getParameterKey("fazlaMesaiGuncelleMail").equals("1"))
									userList = ortakIslemler.getIKUserList(session);
								if (userList == null || userList.isEmpty()) {
									aciklama = aciklama + "<br></br><br></br><b>Start Time : </b>" + PdksUtil.convertToDateString(basTarih, PdksUtil.getDateTimeLongFormat());
									aciklama = aciklama + "<br></br><b>Stop Time  : </b>" + PdksUtil.convertToDateString(ortakIslemler.getBugun(), PdksUtil.getDateTimeLongFormat()) + "<br></br>";
								}
								zamanlayici.mailGonder(session, null, konu, aciklama, userList, Boolean.TRUE);
							}

						}
					}

				} else
					saniye = 15;
				if (mailList.isEmpty() == false) {
					if (PdksUtil.getCanliSunucuDurum() || PdksUtil.getTestSunucuDurum()) {
						Thread.sleep(saniye * 1000);
						mailGonder(mailList, session);
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
			logger.debug("planVardiyaHareketGuncelleme out " + PdksUtil.getCurrentTimeStampStr());
		}

		return null;
	}

	/**
	 * @param tarih
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public String fazlaMesaiGuncelleme(Date tarih, Session session) throws Exception {
		Date bugun = PdksUtil.getDate(ortakIslemler.getBugun());
		if (tarih == null)
			tarih = bugun;
		Calendar cal = Calendar.getInstance();
		cal.setTime(bugun);
		int haftaGun = cal.get(Calendar.DAY_OF_WEEK);
		int artiGun = haftaGun != Calendar.SATURDAY && haftaGun != Calendar.SUNDAY ? 0 : 6;
		String adresStr = null;
		long buAy = Long.parseLong(PdksUtil.convertToDateString(tarih, PATTERN_DONEM));
		long oncekiAy = Long.parseLong(PdksUtil.convertToDateString(PdksUtil.tariheAyEkleCikar(PdksUtil.convertToJavaDate(buAy + "01", PATTERN), -1), PATTERN_DONEM));
		long sonrakiAy = Long.parseLong(PdksUtil.convertToDateString(PdksUtil.tariheGunEkleCikar(tarih, artiGun), PATTERN_DONEM));
		List<DenklestirmeAy> aylar = pdksEntityController.getSQLParamByFieldList(DenklestirmeAy.TABLE_NAME, DenklestirmeAy.COLUMN_NAME_DONEM_KODU, Arrays.asList(new Long[] { oncekiAy, buAy, sonrakiAy }), DenklestirmeAy.class, session);
		TreeMap<Long, DenklestirmeAy> ayMap = new TreeMap<Long, DenklestirmeAy>();
		for (DenklestirmeAy denklestirmeAy : aylar)
			ayMap.put(denklestirmeAy.getDonem(), denklestirmeAy);
		if (ayMap.containsKey(sonrakiAy) == false)
			ayMap.put(sonrakiAy, ayMap.get(buAy));
		aylar = null;
		Date tarihBas = PdksUtil.convertToJavaDate(oncekiAy + "01", PATTERN);
		Date tarih2 = PdksUtil.convertToJavaDate(sonrakiAy + "01", PATTERN);
		String str = "";
		StringBuffer sb = new StringBuffer(), sb1 = new StringBuffer(), sb2 = new StringBuffer();
		HashMap fields = new HashMap();
		int sayac = 0;
		while (tarih2.getTime() >= tarihBas.getTime()) {
			++sayac;
			Date tarihBit = PdksUtil.getAyinSonGunu(tarihBas);
			DenklestirmeAy da = ayMap.get(Long.parseLong(PdksUtil.convertToDateString(tarihBas, PATTERN_DONEM)));
			long donemId = da.getId();
			int durum = da.getDurum() ? 1 : 0;
			sb2.append(" PERSONEL" + sayac + " as ( ");
			sb2.append(" select P." + Personel.COLUMN_NAME_ID + " as " + VardiyaGun.COLUMN_NAME_PERSONEL + ", count(*) ADET from " + Personel.TABLE_NAME + " P " + PdksEntityController.getSelectLOCK());
			if (da.getDurum()) {
				fields.put("pbas" + sayac, tarihBas);
				fields.put("pbit" + sayac, tarihBit);
				fields.put("vbas" + sayac, tarihBas);
				fields.put("vbit" + sayac, tarihBit);
				sb2.append(" left join " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
				sb2.append(" and ( V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between :vbas" + sayac + " and :vbit" + sayac + ")");
				sb2.append(" where P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :pbas" + sayac + " and P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :pbit" + sayac);
				sb2.append(" and P." + Personel.COLUMN_NAME_CALISMA_MODELI + " is not null and P." + Personel.COLUMN_NAME_SABLON + " is not null");
				sb2.append(" and (P." + Personel.COLUMN_NAME_PDKS_DURUM + " = 1 or P." + Personel.COLUMN_NAME_MAIL_TAKIP + " = 1) and P." + Personel.COLUMN_NAME_EK_SAHA3 + " is not null");
				sb2.append(" and P." + Personel.COLUMN_NAME_DURUM + " = 1 and coalesce(V." + VardiyaGun.COLUMN_NAME_DURUM + ", 0) = 0 ");
			} else
				sb2.append(" where 1 = 2 ");
			sb2.append(" group by P." + Personel.COLUMN_NAME_ID);
			sb2.append("), ");
			boolean eski = sayac < 3 || buAy == sonrakiAy;
			// String str2 = eski ? "( coalesce(PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + ", 0) = 0 or " + donem + " < " + buAy + " )" : "PD." + PersonelDenklestirme.COLUMN_NAME_ID + " is null";
			String str2 = eski ? "" : " and PD." + PersonelDenklestirme.COLUMN_NAME_ID + " is null";
			sb1.append(str + " select distinct D.* from " + DenklestirmeAy.TABLE_NAME + " D " + PdksEntityController.getSelectLOCK());
			sb1.append(" inner join PERSONEL" + sayac + " V " + PdksEntityController.getJoinLOCK() + " on  1 = " + durum);
			sb1.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
			if (eski == false) {
				long buDonemId = ayMap.get(buAy).getId();
				sb1.append(" inner join " + PersonelDenklestirme.TABLE_NAME + " G " + PdksEntityController.getJoinLOCK() + " on G." + Personel.COLUMN_NAME_ID + " = " + buDonemId);
				sb1.append(" and G." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
				sb1.append(" inner join " + PersonelKGS.TABLE_NAME + " K " + PdksEntityController.getJoinLOCK() + " on K." + PersonelKGS.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_KGS_PERSONEL + " and K." + PersonelKGS.COLUMN_NAME_DURUM + " = 1");

			}
			sb1.append(" inner join " + Sirket.TABLE_NAME + " S " + PdksEntityController.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET + " and S." + Sirket.COLUMN_NAME_FAZLA_MESAI + " = 1");
			sb1.append(" left join " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getJoinLOCK() + " on PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
			sb1.append(" and PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = P." + Personel.COLUMN_NAME_ID);
			sb1.append(" where D." + DenklestirmeAy.COLUMN_NAME_ID + " = " + donemId + " and " + durum + " = 1" + str2);
			str = " union";

			tarihBas = PdksUtil.tariheAyEkleCikar(tarihBas, 1);

		}
		ayMap = null;
		sb.append("with " + sb2.toString() + " VERI as (" + sb1.toString());
		sb.append(" ) select distinct D.* from VERI D " + PdksEntityController.getSelectLOCK());
		sb.append(" order by D." + DenklestirmeAy.COLUMN_NAME_DONEM_KODU);
		sb2 = null;
		sb1 = null;
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			aylar = pdksEntityController.getObjectBySQLList(sb.toString(), fields, DenklestirmeAy.class);
		} catch (Exception e) {
			logger.error(e);
		}

		if (aylar != null && aylar.isEmpty() == false) {
			adresStr = ortakIslemler.getLoginAdres();
			if (PdksUtil.hasStringValue(adresStr)) {
				String adres = PdksUtil.replaceAllManuel(adresStr, "login", "denklestirmeBordroGuncelleme");
				logger.info(adres + " in " + PdksUtil.getCurrentTimeStampStr());
				User guncelleyenUser = ortakIslemler.getSistemAdminUser(session);
				guncelleyenUser.setAdmin(true);
				List<Liste> islemList = new ArrayList<Liste>();
				boolean talepVar = getSirketTalepGirmeDurum(session);
				for (DenklestirmeAy da : aylar) {
					try {
						vardiyaVersiyonGuncelle(da, talepVar, bugun, guncelleyenUser, session);
					} catch (Exception e) {

					}

					DepartmanDenklestirmeDonemi denklestirmeDonemi = new DepartmanDenklestirmeDonemi();
					AylikPuantaj aylikPuantaj = fazlaMesaiOrtakIslemler.getAylikPuantaj(da.getAy(), da.getYil(), denklestirmeDonemi, session);
					aylikPuantaj.setLoginUser(guncelleyenUser);
					aylikPuantaj.setDenklestirmeAy(da);
					List<SelectItem> departmanIdList = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(aylikPuantaj, false, session);
					for (SelectItem siDepartman : departmanIdList) {
						Long departmanId = (Long) siDepartman.getValue();
						List<SelectItem> sirketIdList = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, aylikPuantaj, false, session);
						if (sirketIdList.isEmpty() == false) {
							List<Long> idList = new ArrayList<Long>();
							for (SelectItem sirketSelectItem : sirketIdList)
								idList.add((Long) sirketSelectItem.getValue());
							List<Sirket> sirketList = pdksEntityController.getSQLParamByFieldList(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, idList, Sirket.class, session);
							if (sirketList.size() > 1)
								sirketList = PdksUtil.sortObjectStringAlanList(sirketList, "getAd", null);
							idList = null;
							for (Sirket sirket : sirketList) {
								String linkStr = "pdksUserId=" + guncelleyenUser.getId() + "&donemId=" + da.getId() + "&sirketId=" + sirket.getId();
								if (sirket.isTesisDurumu())
									linkStr = linkStr + "&tesisId=*";
								String id = ortakIslemler.getEncodeStringByBase64(linkStr);
								islemList.add(new Liste(da.getAyAdi() + " " + da.getYil() + " " + sirket.getAd(), adres + "?id=" + id));
							}
							sirketList = null;
						}
						sirketIdList = null;
					}
				}
				for (Liste liste : islemList) {
					String id = (String) liste.getValue();
					String sonuc = ortakIslemler.adresKontrol(id);
					if (sonuc != null)
						logger.error(liste.getId() + " hata =" + sonuc + " out " + PdksUtil.getCurrentTimeStampStr());
				}
				islemList = null;
				logger.info(adres + " out " + PdksUtil.getCurrentTimeStampStr());
			}
		}
		aylar = null;
		if (adresStr != null)
			adresStr = "";
		return adresStr;

	}

	/**
	 * @param da
	 * @param talepVar
	 * @param bugun
	 * @param guncelleyenUser
	 * @param session
	 */
	@Transactional
	private void vardiyaVersiyonGuncelle(DenklestirmeAy da, boolean talepVar, Date bugun, User guncelleyenUser, Session session) {
		HashMap fields = new HashMap();
		Date tarihBas = PdksUtil.convertToJavaDate(da.getDonem() + "01", PATTERN);
		Date tarihBit = PdksUtil.getAyinSonGunu(tarihBas);
		if (tarihBit.after(bugun))
			tarihBit = bugun;
		if (talepVar) {
			StringBuffer sb = new StringBuffer();
			sb.append("select K.* from " + KatSayi.TABLE_NAME + " K " + PdksEntityController.getSelectLOCK());
			sb.append(" where K." + KatSayi.COLUMN_NAME_BAS_TARIH + " <= :t2 and K." + KatSayi.COLUMN_NAME_BIT_TARIH + " >= :t1");
			sb.append(" and K." + KatSayi.COLUMN_NAME_TIPI + " = " + PuantajKatSayiTipi.GUN_FMT_DURUM.value());
			fields.put("t1", tarihBas);
			fields.put("t2", tarihBit);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<KatSayi> katsayiList = pdksEntityController.getObjectBySQLList(sb.toString(), fields, KatSayi.class);
			talepVar = katsayiList.isEmpty() == false;
			katsayiList = null;
		}
		fields.clear();
		StringBuffer sb1 = new StringBuffer();
		sb1.append("select V.* from " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getSelectLOCK());
		sb1.append(" inner join " + CalismaModeliAy.TABLE_NAME + " CA " + PdksEntityController.getJoinLOCK() + " on CA." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY);
		sb1.append(" and CA." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1");
		sb1.append(" inner join " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getJoinLOCK() + " on V." + VardiyaGun.COLUMN_NAME_PERSONEL + " = PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		sb1.append(" and (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between :t1 and :t2) ");
		sb1.append(" and ( (V." + VardiyaGun.COLUMN_NAME_ONAYLI + " = 1 and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 0 ) ");
		sb1.append(" or (V." + VardiyaGun.COLUMN_NAME_ONAYLI + " = 0 and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 1 ) ) ");
		sb1.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		sb1.append(" and (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ") ");
		if (talepVar) {
			sb1.append(" left join " + FazlaMesaiTalep.TABLE_NAME + " F " + PdksEntityController.getJoinLOCK() + " on F." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + " = V." + VardiyaGun.COLUMN_NAME_ID);
			sb1.append(" and F." + FazlaMesaiTalep.COLUMN_NAME_DURUM + " = 1");
		}
		sb1.append(" where PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = " + da.getId() + " and PD." + PersonelDenklestirme.COLUMN_NAME_DURUM + " = 1");
		if (talepVar)
			sb1.append(" and F." + FazlaMesaiTalep.COLUMN_NAME_ID + " is null");
		sb1.append(" order by P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + ", V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI);
		fields.put("t1", tarihBas);
		fields.put("t2", tarihBit);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<VardiyaGun> vGunList = pdksEntityController.getObjectBySQLList(sb1.toString(), fields, VardiyaGun.class);
		if (vGunList.isEmpty() == false) {
			List<Long> perIdList = new ArrayList<Long>();
			for (VardiyaGun vg : vGunList) {
				if (vg.getVardiya().isHaftaTatil()) {
					Long perId = vg.getPdksPersonel().getId();
					if (!perIdList.contains(perId))
						perIdList.add(perId);

				}
			}
			if (perIdList.isEmpty() == false) {
				fields.clear();
				sb1 = new StringBuffer();
				sb1.append("select PD.* from " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getSelectLOCK());
				sb1.append(" inner join " + CalismaModeliAy.TABLE_NAME + " CA " + PdksEntityController.getJoinLOCK() + " on CA." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY);
				sb1.append(" and CA." + CalismaModeliAy.COLUMN_NAME_HAFTA_TATIL_HAREKET_GUNCELLE + " = 0");
				sb1.append(" where PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = " + da.getId() + " and PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :p");
				fields.put("p", perIdList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PersonelDenklestirme> pdList = pdksEntityController.getObjectBySQLList(sb1.toString(), fields, PersonelDenklestirme.class);
				perIdList.clear();
				for (PersonelDenklestirme pd : pdList)
					perIdList.add(pd.getPersonelId());
				pdList = null;
			}
			Date guncellemeTarihi = null;
			boolean flush = false;
			for (VardiyaGun vg : vGunList) {
				Long perId = vg.getPdksPersonel().getId();
				Boolean vardiyaOnayli = vg.getDurum();
				if (vg.getVardiya().isHaftaTatil()) {
					if (perIdList.contains(perId))
						vardiyaOnayli = true;
				}
				if (vg.getVardiyaOnayli().equals(vardiyaOnayli) == false) {
					if (guncellemeTarihi == null)
						guncellemeTarihi = new Date();
					vg.setGuncellemeTarihi(guncellemeTarihi);
					vg.setGuncelleyenUser(guncelleyenUser);
					vg.setVardiyaOnayli(vardiyaOnayli);
					pdksEntityController.saveOrUpdate(session, entityManager, vg);
					flush = true;
				}

			}
			perIdList = null;
			if (flush)
				session.flush();
		}
		fields.clear();
		sb1 = new StringBuffer();
		sb1.append("with DATA as (");
		sb1.append(" select PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + ", CA." + CalismaModeliAy.COLUMN_NAME_HAFTA_TATIL_HAREKET_GUNCELLE + " from " + CalismaModeliAy.TABLE_NAME + " CA " + PdksEntityController.getSelectLOCK());
		sb1.append(" inner join " + PersonelDenklestirme.TABLE_NAME + " PD " + PdksEntityController.getJoinLOCK() + " on CA." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY);
		sb1.append(" where CA. " + CalismaModeliAy.COLUMN_NAME_DONEM + " = " + da.getId() + " and CA." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1 )");
		sb1.append(" select V.* from " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK());
		sb1.append(" inner join DATA D " + PdksEntityController.getJoinLOCK() + " on D." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb1.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL);
		sb1.append(" and (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + ") ");
		if (talepVar)
			sb1.append(" left join " + FazlaMesaiTalep.TABLE_NAME + " T " + PdksEntityController.getJoinLOCK() + " on T." + FazlaMesaiTalep.COLUMN_NAME_VARDIYA_GUN + " = V." + VardiyaGun.COLUMN_NAME_ID);
		sb1.append(" where (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between :t1 and :t2) and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 0 and V." + VardiyaGun.COLUMN_NAME_ONAYLI + " = 1");
		sb1.append("  and (V.VARDIYA_ACIKLAMA <> 'HT' OR D." + CalismaModeliAy.COLUMN_NAME_HAFTA_TATIL_HAREKET_GUNCELLE + " = 1)");
		sb1.append("  and  V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " <= " + PdksEntityController.getSqlBuGun());
		if (talepVar)
			sb1.append(" and T." + FazlaMesaiTalep.COLUMN_NAME_ID + " is null");
		fields.put("t1", tarihBas);
		fields.put("t2", tarihBit);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		vGunList = pdksEntityController.getObjectBySQLList(sb1.toString(), fields, VardiyaGun.class);
		Date guncellemeTarihi = null;
		boolean flush = false;
		for (VardiyaGun vg : vGunList) {
			Vardiya v = vg.getVardiya();
			if (v.isIzinVardiya() || v.isOffGun())
				continue;
			if (guncellemeTarihi == null)
				guncellemeTarihi = new Date();
			vg.setGuncellemeTarihi(guncellemeTarihi);
			vg.setGuncelleyenUser(guncelleyenUser);
			vg.setVardiyaOnayli(Boolean.FALSE);
			pdksEntityController.saveOrUpdate(session, entityManager, vg);
			flush = true;
		}
		if (flush)
			session.flush();
		vGunList = null;
		sb1 = null;
	}

	/**
	 * @param session
	 * @return
	 */
	private boolean getSirketTalepGirmeDurum(Session session) {
		boolean talepVar = false;
		List<Sirket> sirketList = pdksEntityController.getSQLParamByAktifFieldList(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_PDKS, Boolean.TRUE, Sirket.class, session);
		for (Sirket sirket : sirketList) {
			if (sirket.getFazlaMesai() && talepVar == false)
				talepVar = sirket.getFazlaMesaiTalepGirilebilir();

		}
		sirketList = null;
		return talepVar;
	}

	/**
	 * @param tarih
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public boolean vardiyaHareketGuncelleme(Date tarih, Session session) throws Exception {
		if (tarih == null)
			tarih = ortakIslemler.getBugun();
		Calendar cal = Calendar.getInstance();
		String dateStr = PdksUtil.convertToDateString(cal.getTime(), PATTERN);
		int dayOffWeek = cal.get(Calendar.DAY_OF_WEEK);
		cal.setTime(tarih);
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.MONTH, -1);
		Date basTarih = PdksUtil.getDate(cal.getTime());
		Date bitTarih = PdksUtil.getDate(tarih);

		HashMap fields = new HashMap();
		fields.put("t1", basTarih);
		fields.put("t2", bitTarih);
		StringBuffer sb = new StringBuffer();
		sb.append(" with VERI as (");
		sb.append(" select distinct year(V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ") " + DenklestirmeAy.COLUMN_NAME_YIL + ", month(V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + ") " + DenklestirmeAy.COLUMN_NAME_AY + ",");
		sb.append(" V." + VardiyaGun.COLUMN_NAME_PERSONEL + " from " + VardiyaGun.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK());
		sb.append(" inner join PERSONEL P " + PdksEntityController.getJoinLOCK() + " on P.ID = V." + VardiyaGun.COLUMN_NAME_PERSONEL + " and  V.VARDIYA_TARIHI between P.ISE_BASLAMA_TARIHI and P.SSK_CIKIS_TARIHI");
		sb.append(" where (V." + VardiyaGun.COLUMN_NAME_VARDIYA_TARIHI + " between :t1 and :t2 ) and V." + VardiyaGun.COLUMN_NAME_DURUM + " = 0 and V." + VardiyaGun.COLUMN_NAME_ONAYLI + " < 0");
		sb.append(" )");
		sb.append(" select PD." + PersonelDenklestirme.COLUMN_NAME_ID + " from VERI V " + PdksEntityController.getSelectLOCK());
		sb.append(" inner join " + DenklestirmeAy.TABLE_NAME + " D on D." + DenklestirmeAy.COLUMN_NAME_YIL + " = V." + DenklestirmeAy.COLUMN_NAME_YIL + " and D." + DenklestirmeAy.COLUMN_NAME_AY + " = V." + DenklestirmeAy.COLUMN_NAME_AY + " and D." + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1");
		sb.append(" inner join " + PersonelDenklestirme.TABLE_NAME + " PD on PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " = V." + VardiyaGun.COLUMN_NAME_PERSONEL + "  and PD." + PersonelDenklestirme.COLUMN_NAME_DONEM + " = D." + DenklestirmeAy.COLUMN_NAME_ID);
		sb.append(" and coalesce(PD." + PersonelDenklestirme.COLUMN_NAME_SUA_DURUM + ", 0) = 0");
		sb.append(" inner join " + CalismaModeliAy.TABLE_NAME + " C on C." + CalismaModeliAy.COLUMN_NAME_ID + " = PD." + PersonelDenklestirme.COLUMN_NAME_CALISMA_MODELI_AY + " and C." + CalismaModeliAy.COLUMN_NAME_HAREKET_KAYDI_VARDIYA_BUL + " = 1");
		sb.append(" order by D." + DenklestirmeAy.COLUMN_NAME_YIL + " desc, D." + DenklestirmeAy.COLUMN_NAME_AY + " desc, PD." + PersonelDenklestirme.COLUMN_NAME_PERSONEL);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Long> idList = PdksUtil.getLongListFromBigDecimal(null, pdksEntityController.getObjectBySQLList(sb.toString(), fields, null));
		List<PersonelDenklestirme> denklestirmeList = null;
		if (idList.isEmpty() == false)
			denklestirmeList = pdksEntityController.getSQLParamByFieldList(PersonelDenklestirme.TABLE_NAME, PersonelDenklestirme.COLUMN_NAME_ID, idList, PersonelDenklestirme.class, session);
		idList = null;
		Boolean islemYapildi = null;
		if (denklestirmeList != null && denklestirmeList.isEmpty() == false) {
			TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(null, basTarih, bitTarih, session);
			LinkedHashMap<String, List<PersonelDenklestirme>> linkedHashMap = new LinkedHashMap<String, List<PersonelDenklestirme>>();
			HashMap<Long, Boolean> hareketKaydiVardiyaMap = new HashMap<Long, Boolean>();
			for (PersonelDenklestirme pd : denklestirmeList) {
				Long cmId = pd.getCalismaModeli().getId();
				if (hareketKaydiVardiyaMap.containsKey(cmId) == false)
					hareketKaydiVardiyaMap.put(cmId, true);
				DenklestirmeAy dm = pd.getDenklestirmeAy();
				String key = "" + dm.getDonemKodu();
				List<PersonelDenklestirme> list = linkedHashMap.containsKey(key) ? linkedHashMap.get(key) : new ArrayList<PersonelDenklestirme>();
				if (list.isEmpty())
					linkedHashMap.put(key, list);
				list.add(pd);
			}
			for (String key : linkedHashMap.keySet()) {
				tarih = PdksUtil.convertToJavaDate(key + "01", PATTERN);
				basTarih = PdksUtil.tariheGunEkleCikar(tarih, -6);
				bitTarih = PdksUtil.tariheGunEkleCikar(PdksUtil.tariheAyEkleCikar(tarih, 1), 5);
				List<PersonelDenklestirme> list1 = linkedHashMap.get(key);
				TreeMap<String, Liste> pdMap = new TreeMap<String, Liste>();
				DenklestirmeAy da = null;
				for (PersonelDenklestirme pd : list1) {
					da = pd.getDenklestirmeAy();
					Personel personel = pd.getPdksPersonel();
					String key1 = personel.getSirket().getAd() + "_" + (personel.getTesis() != null ? personel.getTesis().getAciklama() : "") + "_" + (personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "") + "_"
							+ (personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");
					Liste liste = pdMap.containsKey(key1) ? pdMap.get(key1) : new Liste(key1, new ArrayList<PersonelDenklestirme>());
					List<PersonelDenklestirme> list2 = (List<PersonelDenklestirme>) liste.getValue();
					if (list2.isEmpty())
						pdMap.put(key1, liste);
					list2.add(pd);

				}
				list1 = null;
				List<Liste> listeList = PdksUtil.sortObjectStringAlanList(new ArrayList(pdMap.values()), "getId", null);
				pdMap = null;
				for (Liste liste : listeList) {
					HashMap<Long, PersonelView> kgsPerMap = new HashMap<Long, PersonelView>();
					List<PersonelDenklestirme> list = (List<PersonelDenklestirme>) liste.getValue();
					TreeMap<Long, PersonelDenklestirmeTasiyici> personelDenklestirmeMap = new TreeMap<Long, PersonelDenklestirmeTasiyici>();
					List<Long> personelIdList = new ArrayList<Long>();
					Sirket sirket = null;
					Tanim tesis = null, bolum = null, altBolum = null;
					for (PersonelDenklestirme pd : list) {
						Personel personel = pd.getPdksPersonel();
						sirket = personel.getSirket();
						tesis = personel.getTesis();
						bolum = personel.getEkSaha3();
						altBolum = personel.getEkSaha4();
						PersonelKGS personelKGS = personel.getPersonelKGS();
						kgsPerMap.put(personelKGS.getId(), personelKGS.getPersonelView());
						Long perId = pd.getPersonelId();
						AylikPuantaj ap = new AylikPuantaj(da);
						ap.setPersonelDenklestirme(pd);
						ap.setPdksPersonel(ap.getPdksPersonel());
						PersonelDenklestirmeTasiyici pdt = new PersonelDenklestirmeTasiyici(ap);
						pdt.setVardiyaGunleriMap(new TreeMap<String, VardiyaGun>());

						personelDenklestirmeMap.put(perId, pdt);
						personelIdList.add(perId);
					}
					String str = da.getAyAdi() + " " + da.getYil() + " : "
							+ PdksUtil.replaceAllManuel(sirket.getAd() + " " + (tesis != null ? tesis.getAciklama() + " " : "") + (bolum != null ? bolum.getAciklama() + " " : "") + (altBolum != null ? altBolum.getAciklama() + " " : "") + " [ " + list.size() + " ]", "  ", " ");
					if (authenticatedUser != null)
						logger.info(str + " in " + PdksUtil.getCurrentTimeStampStr());
					HashMap<Long, ArrayList<HareketKGS>> personelHareketMap = ortakIslemler.personelHareketleriGetir(kgsPerMap, ortakIslemler.tariheGunEkleCikar(cal, basTarih, -1), ortakIslemler.tariheGunEkleCikar(cal, bitTarih, 1), session);
					if (personelHareketMap.isEmpty() == false) {
						List<VardiyaGun> gunList = ortakIslemler.getAllPersonelIdVardiyalar(personelIdList, tatilMap, basTarih, bitTarih, true, session);
						TreeMap<Long, List<VardiyaGun>> vGunMap = new TreeMap<Long, List<VardiyaGun>>();
						HashMap<Long, ArrayList<VardiyaGun>> calismaPlaniMap = new HashMap<Long, ArrayList<VardiyaGun>>();
						for (VardiyaGun vg : gunList) {
							if (vg.getPdksPersonel() == null)
								continue;
							vg.setAyinGunu(vg.getVardiyaDateStr().startsWith(key));
							String vKey = PdksUtil.convertToDateString(vg.getVardiyaDate(), PATTERN);
							Long perId = vg.getPdksPersonel().getId();
							TreeMap<String, VardiyaGun> vardiyaGunleriMap = personelDenklestirmeMap.get(perId).getVardiyaGunleriMap();
							vardiyaGunleriMap.put(vKey, vg);
							List<VardiyaGun> vardiyaGunList = vGunMap.containsKey(perId) ? vGunMap.get(perId) : new ArrayList<VardiyaGun>();
							ArrayList<VardiyaGun> vardiyaGun1List = calismaPlaniMap.containsKey(perId) ? calismaPlaniMap.get(perId) : new ArrayList<VardiyaGun>();
							if (vardiyaGunList.isEmpty()) {
								vGunMap.put(perId, vardiyaGunList);
								calismaPlaniMap.put(perId, vardiyaGun1List);
							}
							vardiyaGun1List.add(vg);
							vardiyaGunList.add(vg);
						}
						if (personelHareketMap != null && personelHareketMap.isEmpty() == false)
							ortakIslemler.vardiyaHareketlerdenGuncelle(personelDenklestirmeMap, vGunMap, calismaPlaniMap, hareketKaydiVardiyaMap, personelHareketMap, null, session);
						if (islemYapildi == null)
							islemYapildi = dayOffWeek != Calendar.SUNDAY && tatilMap.containsKey(dateStr) == false;

						gunList = null;
						calismaPlaniMap = null;
						vGunMap = null;
					}
					if (authenticatedUser != null)
						logger.info(str + " out " + PdksUtil.getCurrentTimeStampStr());
					list = null;
					personelDenklestirmeMap = null;
					personelHareketMap = null;

					list = null;
					personelIdList = null;
				}
				if (authenticatedUser != null && listeList.isEmpty() == false)
					logger.info("VardiyaHareketGuncelleme bitti. " + PdksUtil.getCurrentTimeStampStr());
				listeList = null;
			}
			linkedHashMap = null;
			tatilMap = null;
		}
		denklestirmeList = null;
		sb = null;
		if (islemYapildi == null)
			islemYapildi = false;
		return islemYapildi;
	}

	/**
	 * @param session
	 * @return
	 */
	private boolean getMailGonder(Session session) {
		boolean mailGonder = false;
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			Date basTarih = PdksUtil.getDate(cal.getTime());
			String key = PdksUtil.convertToDateString(basTarih, PATTERN);
			Date bitTarih = PdksUtil.tariheGunEkleCikar(basTarih, 1);
			TreeMap<String, Tatil> tatilMap = ortakIslemler.getTatilGunleri(null, basTarih, bitTarih, session);
			Tatil tatil = tatilMap != null && tatilMap.containsKey(key) ? tatilMap.get(key) : null;
			mailGonder = tatil == null || tatil.isYarimGunMu();
			tatilMap = null;
		}
		return mailGonder;
	}

	/**
	 * @param adi
	 * @param session
	 * @return
	 */
	private Parameter getParameter(String adi, Session session) {
		Parameter parameter = null;
		if (ortakIslemler.getParameterKeyHasStringValue(adi))
			parameter = ortakIslemler.getParameter(session, adi);
		if (parameter != null) {
			boolean guncelleme = false;
			String value = parameter.getValue();
			if (PdksUtil.hasStringValue(value))
				try {
					guncelleme = PdksUtil.zamanKontrol(adi, value, bugun);
				} catch (Exception e) {
				}

			if (guncelleme == false)
				parameter = null;
		}
		return parameter;

	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		PlanVardiyaHareketGuncelleme.calisiyor = calisiyor;
	}

}