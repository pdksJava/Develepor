package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.PdksAgent;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Tanim;
import org.pdks.entity.Tatil;
import org.pdks.quartz.Zamanlayici;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserRoles;

import com.ibm.icu.util.IslamicCalendar;
import com.ibm.icu.util.ULocale;
import com.pdks.webservice.MailObject;

@Name("tatilHome")
public class TatilHome extends EntityHome<Tatil> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3927468770176440280L;
	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(TatilHome.class);
	@RequestParameter
	Long pdksTatilId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(create = true)
	Renderer renderer;
	@In(required = false, create = true)
	HashMap parameterMap;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	Zamanlayici zamanlayici;

	public static String sayfaURL = "tatilTanimlama";
	private List<String> mesajList = new ArrayList<String>();
	private List<Tanim> tatilTanimList = new ArrayList<Tanim>();
	private List<Tatil> tatilList = new ArrayList<Tatil>(), diniList = new ArrayList<Tatil>();
	private List<SelectItem> ayList;
	private List<SelectItem> basGunList, bitisGunList;
	private List<User> userList = new ArrayList<User>();
	private ArrayList<PersonelIzin> izinListesi;
	private Date tarih = Calendar.getInstance().getTime();
	private Boolean kaydetHatali = Boolean.FALSE, kopyala = Boolean.FALSE;
	private int yilSayisi = 1;
	private Tatil oldPdksTatil;
	private User islemYapan;
	private Long agentId;
	private Session session;

	@Override
	public Object getId() {
		if (pdksTatilId == null) {
			return super.getId();
		} else {
			return pdksTatilId;
		}
	}

	@Override
	public void create() {
		super.create();
	}

	@Transactional
	public String diniBayramBasla() {
		try {
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			agentId = req != null ? Long.parseLong(req.getParameter("agentId")) : null;
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			if (PdksUtil.isSessionKapali(session)) {
				if (authenticatedUser != null)
					session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
				else
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
			}

			if ((PdksUtil.getTestSunucuDurum() == false && PdksUtil.getCanliSunucuDurum() == false) || PdksUtil.isSistemDestekVar())
				diniBayramEkle();
		} catch (Exception e) {
		}
		pdksEntityController.sessionClose(session);
		return MenuItemConstant.home;

	}

	/**
	 * @param year
	 * @param tatilTipi
	 * @return
	 */
	private List<Tatil> diniBayramlarGuncelle(int year, Tanim tatilTipi) {
		List<Tatil> tatiller = new ArrayList<Tatil>();
		Date buYilBasi = PdksUtil.convertToJavaDate(year + "0101", "yyyyMMdd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(buYilBasi);
		cal.add(Calendar.DATE, -4);
		Date date = cal.getTime();
		cal.setTime(buYilBasi);
		cal.add(Calendar.YEAR, 1);
		cal.set(Calendar.DATE, 4);
		Date stopDate = cal.getTime();
		cal.setTime(date);
		ULocale locale = new ULocale("@calendar=islamic-umalqura");
		IslamicCalendar calIs = new IslamicCalendar(locale);
		Tatil holiday = null;
		while (date.after(stopDate) == false && tatilTipi != null) {
			cal.setTime(date);
			calIs.setTime(date);
			int hijriMonth = calIs.get(IslamicCalendar.MONTH);
			int hijriDay = calIs.get(IslamicCalendar.DAY_OF_MONTH);
			if (hijriMonth == 9) {// 1. Ramazan Bayramı Hesaplama (Şevval Ayı 1, 2, 3. Günler)
				if (hijriDay == 1) {
					Date tarih = PdksUtil.tariheGunEkleCikar(date, -1);
					cal.setTime(tarih);
					if (cal.get(Calendar.YEAR) == year) {
						holiday = new Tatil("R", tarih, 3);
						tatiller.add(holiday);
					}

				} else if (hijriDay == 2) {
					// createHolidayMap(holidayMap, "RB2", date);
				} else if (hijriDay == 3) {
					if (holiday != null)
						holiday.setBitTarih(date);
					// createHolidayMap(holidayMap, "RB3", date);
				}
			}

			if (hijriMonth == 11) {// 2. Kurban Bayramı Hesaplama (Zilhicce Ayı 9, 10, 11, 12, 13. Günler)
				if (hijriDay == 9) {
					Date tarih = date;
					cal.setTime(tarih);
					if (cal.get(Calendar.YEAR) == year) {
						holiday = new Tatil("K", tarih, 4);
						tatiller.add(holiday);
					}
					// createHolidayMap(holidayMap, "KB0", date);
				} else if (hijriDay == 10) {
					// createHolidayMap(holidayMap, "KB1", date);
				} else if (hijriDay == 11) {
					// createHolidayMap(holidayMap, "KB2", date);
				} else if (hijriDay == 12) {
					// createHolidayMap(holidayMap, "KB3", date);
				} else if (hijriDay == 13) {
					if (holiday != null)
						holiday.setBitTarih(date);
					// createHolidayMap(holidayMap, "KB4", date);
				}
			}

			date = PdksUtil.tariheGunEkleCikar(date, 1);
		}
		if (holiday != null && holiday.getBitGun() == null) {
			cal.setTime(holiday.getBasTarih());
			cal.add(Calendar.DATE, holiday.getGunAdet());
			holiday.setBitTarih(cal.getTime());
		}
		if (tatiller.isEmpty() == false) {
			List<Tatil> list = new ArrayList<Tatil>(tatiller);
			tatiller.clear();
			for (Tatil tatil : list)
				ortakIslemler.updateTatilGunleri(year, tatil.getBasTarih(), tatil.getBitTarih(), tatil.getAd(), tatilTipi, tatiller, session);
			list = null;
		}

		return tatiller;
	}

	/**
	 * @return
	 */
	public String diniBayramEkle() {
		diniList.clear();
		Calendar cal = Calendar.getInstance();
		int basYil = cal.get(Calendar.YEAR), sonYil = cal.get(Calendar.YEAR) + (authenticatedUser == null ? 1 : 5);
		List<Tanim> tatilTipList = pdksEntityController.getSQLParamByAktifFieldList(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_TIPI, Tanim.TIPI_TATIL_TIPI, Tanim.class, session);
		Tanim tatilTipi = null;
		for (Tanim tanim : tatilTipList) {
			if (tanim.getKodu().equals(Tatil.TATIL_TIPI_TEK_SEFER))
				tatilTipi = tanim;
		}

		for (int yil = basYil; yil <= sonYil; yil++) {
			try {
				// <Tatil> list = ortakIslemler.diniBayramlarGuncelle(yil, session);
				List<Tatil> list = diniBayramlarGuncelle(yil, tatilTipi);
				if (list.isEmpty() == false)
					diniList.addAll(list);
				list = null;
			} catch (Exception e) {
			}

		}
		try {
			if (authenticatedUser == null)
				if (PdksUtil.getTestSunucuDurum() || PdksUtil.getCanliSunucuDurum()) {
					String konu = "Tatil günleri kontrol";
					if (agentId != null) {
						PdksAgent agent = (PdksAgent) pdksEntityController.getSQLParamByFieldObject(PdksAgent.TABLE_NAME, PdksAgent.COLUMN_NAME_ID, agentId, PdksAgent.class, session);
						if (agent != null)
							konu = agent.getAciklama();
					}
					zamanlayici.mailGonder(session, null, konu, diniList.isEmpty() ? "Tatil günleri güncelleme olmadı" : "Tatil günlerine " + diniList.size() + " adet tatil eklendi", null, false);

				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		tatilList = null;
		return "";

	}

	@Transactional
	public String diniBayramKaydet() {
		boolean flush = false;
		for (Tatil tatil : diniList) {
			if (tatil.isCheckBoxDurum()) {
				pdksEntityController.saveOrUpdate(session, entityManager, tatil);
				tatil.setCheckBoxDurum(false);
				flush = true;
			}

		}
		if (flush) {
			ortakIslemler.sessionFlush(session);
			fillPdksTatilList();
		} else
			PdksUtil.addMessageAvailableWarn("Ekleme yapılacak dini bayram seçili değildir!");
		return "";
	}

	public void tatilEkle() {
		Tatil tatil = new Tatil();
		tatil.setArifeSonraVardiyaDenklestirmeVar(!ortakIslemler.getParameterKeyHasStringValue("arifeSonraVardiyaDenklestirmeVar"));
		setInstance(tatil);
		setBasGunList(new ArrayList<SelectItem>());
		setBitisGunList(new ArrayList<SelectItem>());
		setOldPdksTatil(null);
		setKaydetHatali(Boolean.FALSE);
	}

	public void tipDegisti() {
		setKaydetHatali(Boolean.FALSE);
	}

	public String tarihDegisti() {
		Tatil pdksTatil = getInstance();
		Calendar cal = Calendar.getInstance();
		if (pdksTatil.getBitTarih() == null) {
			if (pdksTatil.getBasTarih() != null)
				pdksTatil.setBitTarih(ortakIslemler.tariheGunEkleCikar(cal, pdksTatil.getBasTarih(), pdksTatil.isYarimGunMu() ? 1 : 0));
		} else if (pdksTatil.getBasTarih() == null) {
			if (pdksTatil.getBitTarih() != null)
				pdksTatil.setBasTarih(ortakIslemler.tariheGunEkleCikar(cal, pdksTatil.getBitTarih(), pdksTatil.isYarimGunMu() ? -1 : 0));

		}

		return "";
	}

	@Transactional
	public String save() {
		setKaydetHatali(Boolean.FALSE);
		Tatil pdksTatil = getInstance();
		Calendar cal1 = Calendar.getInstance();
		Date bugun = cal1.getTime();
		long buGunLong = Long.parseLong(PdksUtil.convertToDateString(bugun, "yyyyMMdd"));
		int buYil = cal1.get(Calendar.YEAR);
		Calendar cal2 = Calendar.getInstance();
		String cikis = "";
		ArrayList<String> buffer = new ArrayList<String>();
		int bitYil = 2999, basYil = buYil;
		boolean iptalEdildi = false;
		String bs = null;
		try {

			if (pdksTatil.isPeriyodik()) {
				try {
					int basDonem = Integer.parseInt((String) pdksTatil.getBasAy()) * 100 + 100 + Integer.parseInt((String) pdksTatil.getBasGun());
					int bitDonem = Integer.parseInt((String) pdksTatil.getBitAy()) * 100 + 100 + Integer.parseInt((String) pdksTatil.getBitGun());
					if (bitDonem < basDonem)
						buffer.add("Başlangıç ay/gün bitiş ay/günden büyük olamaz");
					else {
						int yil = pdksTatil.getId() != null ? PdksUtil.getDateField(pdksTatil.getBasTarih(), Calendar.YEAR) : buYil;
						basYil = yil;
						cal1.set(yil, Integer.parseInt((String) pdksTatil.getBasAy()), Integer.parseInt((String) pdksTatil.getBasGun()), 0, 0);
						pdksTatil.setBasTarih(PdksUtil.getDate((Date) cal1.getTime()));
						if (pdksTatil.getId() == null && pdksTatil.getBasTarih().before(bugun)) {
							cal1.set(yil + 1, Integer.parseInt((String) pdksTatil.getBasAy()), Integer.parseInt((String) pdksTatil.getBasGun()), 0, 0);
							pdksTatil.setBasTarih(PdksUtil.getDate((Date) cal1.getTime()));
						}
						bitYil = 2999;
						if (pdksTatil.getDurum().equals(Boolean.FALSE)) {
							int bitisYil = PdksUtil.getDateField(pdksTatil.getBitTarih(), Calendar.YEAR);
							if (bitisYil != bitYil) {
								bitYil = bitisYil;
							} else {
								iptalEdildi = true;
								bitYil = buYil;
								long bitisTarihi = (bitYil * 10000) + (Long.parseLong(pdksTatil.getBitAy().toString()) * 100) + Long.parseLong(pdksTatil.getBitGun().toString());
								if (buGunLong < bitisTarihi)
									--bitYil;

							}
						}
						cal2.set(bitYil, Integer.parseInt((String) pdksTatil.getBitAy()), Integer.parseInt((String) pdksTatil.getBitGun()));
						String pattern = "yyyyMMdd";
						bs = PdksUtil.convertToDateString(cal2.getTime(), pattern) + " 23:59:59";
						pdksTatil.setBitTarih(PdksUtil.convertToJavaDate(bs, pattern + " HH:mm:ss"));

					}
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					buffer.add("Tarihleri seçiniz");
					pdksTatil.setBitTarih(null);
					pdksTatil.setBasTarih(null);
				}
				if (pdksTatil.getId() != null && !pdksTatil.getDurum()) {
					cal1.setTime(pdksTatil.getBitTarih());
					cal1.set(Calendar.YEAR, buYil);
					if (cal1.getTime().after(bugun)) {
						--buYil;
						cal1.set(Calendar.YEAR, buYil);
					}
					Date bitisTarih = cal1.getTime();
					pdksTatil.setBitTarih(bitisTarih);
				}
			}
			Date basTarih = null;
			Date bitTarih = null;

			if (buffer.isEmpty()) {
				if ((pdksTatil.getId() == null || pdksTatil.isTekSefer()) && PdksUtil.tarihKarsilastirNumeric(pdksTatil.getBasTarih(), pdksTatil.getBitTarih()) == 1)
					buffer.add("Başlangıç tarihi bitiş tarihinden büyük olamaz");
				else if (authenticatedUser.isAdmin() == false && pdksTatil.getDurum() && PdksUtil.tarihKarsilastirNumeric(pdksTatil.getBasTarih(), Calendar.getInstance().getTime()) != 1)
					buffer.add("Geçmişe ait tatil giremezsiniz");
				else {
					cal1 = Calendar.getInstance();
					basTarih = PdksUtil.getDate((Date) pdksTatil.getBasTarih().clone());
					bitTarih = PdksUtil.getGunSonu((Date) pdksTatil.getBitTarih().clone());
					if (pdksTatil.isPeriyodik()) {
						int yil = cal1.get(Calendar.YEAR);
						basTarih = PdksUtil.setTarih(basTarih, Calendar.YEAR, yil);
						bitTarih = PdksUtil.setTarih(bitTarih, Calendar.YEAR, yil);
					}
					if (pdksTatil.isYarimGunMu()) {
						if (pdksTatil.getId() == null && pdksTatil.getOrjTatil() == null)
							pdksTatil.setArifeSonraVardiyaDenklestirmeVar(pdksTatil.isYarimGunMu());
						int saat = 13, dakika = 0;
						String yarimGunStr = (parameterMap.containsKey("yarimGunSaati") ? (String) parameterMap.get("yarimGunSaati") : "");
						if (yarimGunStr.indexOf(":") > 0) {
							StringTokenizer st = new StringTokenizer(yarimGunStr, ":");
							if (st.countTokens() == 2) {
								try {
									saat = Integer.parseInt(st.nextToken().trim());
								} catch (Exception e) {
									logger.error("PDKS hata in : \n");
									e.printStackTrace();
									logger.error("PDKS hata out : " + e.getMessage());
									saat = 13;
								}
								try {
									dakika = Integer.parseInt(st.nextToken().trim());
								} catch (Exception e) {
									logger.error("PDKS hata in : \n");
									e.printStackTrace();
									logger.error("PDKS hata out : " + e.getMessage());
									saat = 13;
									dakika = 0;
								}
							}
						}
						basTarih = PdksUtil.setTarih(basTarih, Calendar.HOUR_OF_DAY, saat);
						basTarih = PdksUtil.setTarih(basTarih, Calendar.MINUTE, dakika);
					}
					bs = PdksUtil.convertToDateString(bitTarih, "yyyyMMdd HH:mm:ss");
					if (oldPdksTatil == null || !oldPdksTatil.getDurum().equals(pdksTatil.getDurum()) || (pdksTatil.isTekSefer() && (basTarih.getTime() != oldPdksTatil.getBasTarih().getTime() || !bs.equals(PdksUtil.convertToDateString(oldPdksTatil.getBitTarih(), "yyyyMMdd HH:mm:ss"))))) {
						HashMap parametreMap = new HashMap();
						parametreMap.put("baslangicZamani <= ", bitTarih);
						parametreMap.put("bitisZamani >= ", basTarih);
						ArrayList durumList = new ArrayList();
						durumList.add(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
						durumList.add(PersonelIzin.IZIN_DURUMU_REDEDILDI);
						durumList.add(PersonelIzin.IZIN_DURUMU_ERP_GONDERILDI);
						parametreMap.put("izinDurumu not", durumList);
						parametreMap.put("izinTipi.personelGirisTipi <> ", IzinTipi.GIRIS_TIPI_YOK);
						parametreMap.put("izinTipi.takvimGunumu <> ", Boolean.TRUE);
						parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						List<PersonelIzin> list = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
						boolean izinERPUpdate = ortakIslemler.getParameterKey("izinERPUpdate").equals("1");
						if (!izinERPUpdate)
							list.clear();
						if (!list.isEmpty()) {
							HashMap<Long, ArrayList<PersonelIzin>> izinDepartmanMap = new HashMap<Long, ArrayList<PersonelIzin>>();
							for (PersonelIzin personelIzin : list) {
								long departmanId = personelIzin.getIzinSahibi().getSirket().getDepartman().getId();
								ArrayList<PersonelIzin> izinList = izinDepartmanMap.containsKey(departmanId) ? izinDepartmanMap.get(departmanId) : new ArrayList<PersonelIzin>();
								izinList.add(personelIzin);
								izinDepartmanMap.put(departmanId, izinList);
							}
							List idList = new ArrayList(izinDepartmanMap.keySet());
							String fieldName = "user.departman.id";
							parametreMap.clear();
							parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "user");
							parametreMap.put("role.rolename", Role.TIPI_IK);
							parametreMap.put(fieldName, idList);
							parametreMap.put("user.durum", Boolean.TRUE);
							parametreMap.put("user.pdksPersonel.durum", Boolean.TRUE);
							if (session != null)
								parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
							// List ikList = pdksEntityController.getObjectByInnerObjectList(parametreMap, UserRoles.class);
							List ikList = ortakIslemler.getParamList(false, idList, fieldName, parametreMap, UserRoles.class, session);
							for (Iterator<Long> iterator = izinDepartmanMap.keySet().iterator(); iterator.hasNext();) {
								Long departmanId = iterator.next();
								ArrayList<PersonelIzin> izinList = izinDepartmanMap.get(departmanId);
								setIzinListesi(izinList);
								userList.clear();
								for (Iterator<User> iterator2 = ikList.iterator(); iterator2.hasNext();) {
									User ik = iterator2.next();
									if (departmanId.equals(ik.getDepartman().getId())) {
										userList.add(ik);
										iterator2.remove();
									}

								}
								for (Iterator<PersonelIzin> iterator2 = list.iterator(); iterator2.hasNext();) {
									PersonelIzin personelIzin = iterator2.next();
									if (departmanId.equals(personelIzin.getIzinSahibi().getSirket().getDepartman().getId())) {
										if (personelIzin.getOlusturanUser() != null && !personelIzin.getOlusturanUser().isIK())
											userList.add(personelIzin.getOlusturanUser());
										iterator2.remove();
									}

								}

								try {
									MailObject mail = new MailObject();
									mail.setSubject("Tatil Tanımlama");
									StringBuilder body = new StringBuilder("<p>Girdiğiniz izin ile aynı tarihe resmi yada genel tatil tanımlaması yapılmıştır. İzni silip, tekrardan yaratınız.</p><p></p>");
									body.append("<table><thead><tr>");
									body.append("<th><b>" + ortakIslemler.personelNoAciklama() + "</b></th>");
									body.append("<th><b>İzin Sahibi</b></th>");
									body.append("<th><b>Tipi</b></th>");
									body.append("<th><b>Başlangıç Zamanı</b></th>");
									body.append("<th><b>Bitiş Zamanı</b></th></tr></thead><tbody>");
									for (PersonelIzin izin : izinListesi) {
										Personel isahibi = izin.getIzinSahibi();
										body.append("<tr>");
										body.append("<td align='center'>" + isahibi.getPdksSicilNo() + "</th>");
										body.append("<td>" + isahibi.getAdSoyad() + "</th>");
										body.append("<td>" + izin.getIzinTipiAciklama() + "</th>");
										body.append("<td align='center'>" + authenticatedUser.dateTimeFormatla(izin.getBaslangicZamani()) + "</th>");
										body.append("<td align='center'>" + authenticatedUser.dateTimeFormatla(izin.getBitisZamani()) + "</th>");
										body.append("</tr>");
									}
									body.append("</tbody></table>");
									mail.setBody(body.toString());

									ortakIslemler.addMailPersonelUserList(userList, mail.getToList());
									// ortakIslemler.mailSoapServisGonder(true, mail, renderer, "/email/tatilUyariMail.xhtml", session);
									HashMap<String, Object> veriMap = new HashMap<String, Object>();
									veriMap.put("temizleTOCCList", true);
									veriMap.put("mailObject", mail);
									veriMap.put("homeRenderer", renderer);
									veriMap.put("sayfaAdi", "/email/tatilUyariMail.xhtml");
									ortakIslemler.mailSoapServisGonder(veriMap, session);
									veriMap = null;
								} catch (Exception e) {
									logger.error("PDKS hata in : \n");
									e.printStackTrace();
									logger.error("PDKS hata out : " + e.getMessage());
									PdksUtil.addMessageError(e.getMessage());
								}

							}
						}
					}

				}

			}
			if (!pdksTatil.isYarimGunMu())
				pdksTatil.setArifeSonraVardiyaDenklestirmeVar(null);
			if (!buffer.isEmpty()) {
				for (String string : buffer)
					PdksUtil.addMessageWarn(string);
				setKaydetHatali(Boolean.TRUE);
				cikis = "";
			} else {
				if (pdksTatil.getId() == null || pdksTatil.isTekSefer() || iptalEdildi) {
					if (pdksTatil.isPeriyodik()) {
						basTarih = PdksUtil.setTarih(basTarih, Calendar.YEAR, basYil);
						bitTarih = PdksUtil.setTarih(bitTarih, Calendar.YEAR, bitYil);
					}
					pdksTatil.setBasTarih(basTarih);
					pdksTatil.setBitTarih(bitTarih);
				}
				if (pdksTatil.getId() == null) {
					pdksTatil.setOlusturanUser(authenticatedUser);
					pdksTatil.setOlusturmaTarihi(new Date());
				} else {
					pdksTatil.setGuncelleyenUser(authenticatedUser);
					pdksTatil.setGuncellemeTarihi(new Date());
				}
				if (pdksTatil.getId() == null || pdksTatil.getBasTarih().before(pdksTatil.getBitTarih())) {
					if (pdksTatil.getId() != null && pdksTatil.isPeriyodik())
						pdksTatil.setDurum(Boolean.TRUE);
					pdksEntityController.saveOrUpdate(session, entityManager, pdksTatil);
				} else {
					pdksEntityController.deleteObject(session, entityManager, pdksTatil);
				}

				ortakIslemler.sessionFlush(session);
				fillPdksTatilList();
				cikis = "persist";

			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return cikis;
	}

	public void fillPdksTatilList() {
		session.clear();
		if (diniList == null)
			diniList = new ArrayList<Tatil>();
		else
			diniList.clear();
		HashMap parametreMap = new HashMap();
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tatil> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tatil.class);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tarih);
		Integer seciliYil = cal.get(Calendar.YEAR);
		for (Iterator<Tatil> iterator = list.iterator(); iterator.hasNext();) {
			Tatil pdksTatil = iterator.next();
			if (PdksUtil.tarihKarsilastirNumeric(tarih, pdksTatil.getBitTarih()) == 1) {
				iterator.remove();
			} else {
				if (pdksTatil.isPeriyodik()) {
					int yil = seciliYil;
					cal.setTime(pdksTatil.getBasTarih());
					cal.set(Calendar.YEAR, yil);
					Date basTarih = cal.getTime();
					if (basTarih.before(tarih)) {
						cal.set(Calendar.YEAR, ++yil);
						basTarih = cal.getTime();
					}
					pdksTatil.setBasGun(basTarih);
					cal.setTime(pdksTatil.getBitTarih());
					cal.set(Calendar.YEAR, yil);
					Date bitTarih = cal.getTime();
					if (bitTarih.before(tarih) || bitTarih.before(basTarih)) {
						cal.set(Calendar.YEAR, ++yil);
						bitTarih = cal.getTime();
					}
					pdksTatil.setBitGun(bitTarih);
				} else {
					pdksTatil.setBasGun(pdksTatil.getBasTarih());
					pdksTatil.setBitGun(pdksTatil.getBitTarih());
				}
			}

		}
		if (list.size() > 1)
			list = PdksUtil.sortListByAlanAdi(list, "bitGun", false);
		for (Tatil pdksTatil : list) {
			pdksTatil.setBasGun(null);
			pdksTatil.setBitGun(null);
		}
		setTatilList(list);
	}

	public void fillTatilTipiTanimList() {
		List<Tanim> tanimList = null;
		HashMap parametreMap = new HashMap();
		parametreMap.put("tipi", Tanim.TIPI_TATIL_TIPI);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			tanimList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tanim.class);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setTatilTanimList(tanimList);
	}

	public void fillAyList() {
		List<SelectItem> list = ortakIslemler.getAyListesi(Boolean.FALSE);
		setAyList(list);
	}

	public void fillGunBasList() {
		Tatil pdksTatil = getInstance();
		try {
			String ay = (String) pdksTatil.getBasAy();
			setBasGunList(fillGunList("basGun", new Integer(ay)));

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			setBasGunList(new ArrayList<SelectItem>());
		}
	}

	public void fillGunBitisList() {
		Tatil pdksTatil = getInstance();
		try {
			String ay = (String) pdksTatil.getBitAy();
			setBitisGunList(fillGunList("bitGun", new Integer(ay)));

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			setBitisGunList(new ArrayList<SelectItem>());
		}
	}

	public List<SelectItem> fillGunList(String key, Integer deger) {
		List<SelectItem> list = ortakIslemler.getSelectItemList(key, authenticatedUser);
		if (deger != null) {
			int bitis = 31;
			int ay = deger.intValue() + 1;
			if (ay == 4 || ay == 6 || ay == 9 || ay == 11)
				bitis = 30;
			else if (ay == 2)
				bitis = 29;
			for (int i = 1; i <= bitis; i++)
				list.add(new SelectItem(String.valueOf(i)));

		}
		return list;

	}

	/**
	 * @param pdksTatil
	 * @return
	 */
	public String kayitKopyala(Tatil pdksTatil) {
		setInstance(pdksTatil);
		yilSayisi = 1;
		kopyala = Boolean.TRUE;
		if (!ortakIslemler.getParameterKeyHasStringValue("cokluTatilKopyala"))
			kayitKopyalaDevam();
		return "";
	}

	@Transactional
	public String kayitKopyalaDevam() {
		Tatil pdksTatil = getInstance();
		kopyala = yilSayisi > 1;
		boolean flush = false;
		Date olusturmaTarihi = kopyala ? new Date() : null;
		session.clear();
		for (int i = 0; i < yilSayisi; i++) {
			pdksTatil = periyodikOlmayanTatilKopyala(pdksTatil);
			if (kopyala && pdksTatil.getId() == null) {
				flush = true;
				pdksTatil.setOlusturmaTarihi(olusturmaTarihi);
				pdksTatil.setOlusturanUser(authenticatedUser);
				pdksTatil.setDurum(Boolean.TRUE);
				pdksEntityController.saveOrUpdate(session, entityManager, pdksTatil);
			}
		}
		if (!kopyala) {
			pdksTatil.setOrjTatil(getInstance());
			kayitGuncelle(pdksTatil);
		} else {
			if (flush)
				ortakIslemler.sessionFlush(session);
			fillPdksTatilList();
		}

		return "";
	}

	/**
	 * @param pdksTatil
	 * @return
	 */
	private Tatil periyodikOlmayanTatilKopyala(Tatil tatil) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(tatil.getBasTarih());
		int arti = 354;
		cal.add(Calendar.DATE, arti);
		int yil = cal.get(Calendar.YEAR);
		if (yil % 4 == 0) {
			++arti;
			cal.setTime(tatil.getBasTarih());
			cal.add(Calendar.DATE, arti);
		}
		// logger.info(yil + " " + arti);
		Date basTarih = (Date) cal.getTime().clone();
		HashMap parametreMap = new HashMap();
		parametreMap.put("basTarih", basTarih);
		parametreMap.put("tatilTipi.id", tatil.getTatilTipi().getId());
		parametreMap.put("durum", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<Tatil> tatilList = pdksEntityController.getObjectByInnerObjectList(parametreMap, Tatil.class);
		Tatil tatilYeni = null;
		if (tatilList.isEmpty()) {
			tatilYeni = (Tatil) tatil.clone();
			tatilYeni.setId(null);
			tatilYeni.setBasTarih(basTarih);
			cal.setTime(tatil.getBitTarih());
			cal.add(Calendar.DATE, arti);
			tatilYeni.setYarimGun(Boolean.TRUE);
			tatilYeni.setBitTarih((Date) cal.getTime().clone());
			tatilYeni.setOlusturanUser(null);
			tatilYeni.setOlusturmaTarihi(null);
			tatilYeni.setGuncellemeTarihi(null);
			tatilYeni.setGuncelleyenUser(null);
			tatilYeni.setAciklama(yil + " Yılı " + tatil.getAd());
			tatilYeni.setDurum(Boolean.FALSE);
		} else
			tatilYeni = tatilList.get(0);

		return tatilYeni;
	}

	/**
	 * @param pdksTatil
	 */
	public void kayitGuncelle(Tatil pdksTatil) {
		fillTatilTipiTanimList();
		kopyala = Boolean.FALSE;
		if (pdksTatil == null) {
			pdksTatil = new Tatil();
			for (Tanim tatilTipi : tatilTanimList) {
				pdksTatil.setTatilTipi(tatilTipi);
				if (pdksTatil.isTekSefer())
					break;
			}
			setBasGunList(new ArrayList<SelectItem>());
			setBitisGunList(new ArrayList<SelectItem>());
			setOldPdksTatil(null);
			setKaydetHatali(Boolean.FALSE);
		} else
			setOldPdksTatil((Tatil) pdksTatil.clone());

		if (pdksTatil.getTatilTipi() != null && pdksTatil.isPeriyodik()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(pdksTatil.getBasTarih());
			pdksTatil.setBasAy(String.valueOf(cal.get(Calendar.MONTH)));
			pdksTatil.setBasGun(String.valueOf(cal.get(Calendar.DATE)));
			cal.setTime(pdksTatil.getBitTarih());
			pdksTatil.setBitAy(String.valueOf(cal.get(Calendar.MONTH)));
			pdksTatil.setBitGun(String.valueOf(cal.get(Calendar.DATE)));
			fillGunBasList();
			fillGunBitisList();
		}

		if (pdksTatil.getTatilTipi() != null && tatilTanimList != null) {
			Long id = pdksTatil.getTatilTipi().getId();
			for (Tanim tatilTipi : tatilTanimList) {
				if (tatilTipi.getId().equals(id))
					pdksTatil.setTatilTipi(tatilTipi);

			}
		}
		setInstance(pdksTatil);
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUserCalistiSayfa(entityManager, authenticatedUser, sayfaURL);
		ortakIslemler.setUserMenuItemTime(entityManager, session, sayfaURL);
		setIslemYapan(authenticatedUser);
		fillPdksTatilList();
		fillAyList();
	}

	public List<SelectItem> getAyList() {
		return ayList;
	}

	public void setAyList(List<SelectItem> ayList) {
		this.ayList = ayList;
	}

	public List<SelectItem> getBasGunList() {
		return basGunList;
	}

	public void setBasGunList(List<SelectItem> basGunList) {
		this.basGunList = basGunList;
	}

	public List<SelectItem> getBitisGunList() {
		return bitisGunList;
	}

	public void setBitisGunList(List<SelectItem> bitisGunList) {
		this.bitisGunList = bitisGunList;
	}

	public Boolean getKaydetHatali() {
		return kaydetHatali;
	}

	public void setKaydetHatali(Boolean kaydetHatali) {
		this.kaydetHatali = kaydetHatali;
	}

	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	public Tatil getOldPdksTatil() {
		return oldPdksTatil;
	}

	public void setOldPdksTatil(Tatil oldPdksTatil) {
		this.oldPdksTatil = oldPdksTatil;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public List<String> getMesajList() {
		return mesajList;
	}

	public void setMesajList(List<String> mesajList) {
		this.mesajList = mesajList;
	}

	public User getIslemYapan() {
		return islemYapan;
	}

	public void setIslemYapan(User islemYapan) {
		this.islemYapan = islemYapan;
	}

	public ArrayList<PersonelIzin> getIzinListesi() {
		return izinListesi;
	}

	public void setIzinListesi(ArrayList<PersonelIzin> izinListesi) {
		this.izinListesi = izinListesi;
	}

	public Boolean getKopyala() {
		return kopyala;
	}

	public void setKopyala(Boolean kopyala) {
		this.kopyala = kopyala;
	}

	public int getYilSayisi() {
		return yilSayisi;
	}

	public void setYilSayisi(int yilSayisi) {
		this.yilSayisi = yilSayisi;
	}

	public List<Tanim> getTatilTanimList() {
		return tatilTanimList;
	}

	public void setTatilTanimList(List<Tanim> tatilTanimList) {
		this.tatilTanimList = tatilTanimList;
	}

	public List<Tatil> getTatilList() {
		return tatilList;
	}

	public void setTatilList(List<Tatil> value) {
		this.tatilList = value;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<Tatil> getDiniList() {
		return diniList;
	}

	public void setDiniList(List<Tatil> diniList) {
		this.diniList = diniList;
	}
}
