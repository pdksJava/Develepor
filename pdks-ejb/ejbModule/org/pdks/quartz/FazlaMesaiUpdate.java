package org.pdks.quartz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
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
import org.jboss.seam.faces.Renderer;
import org.pdks.entity.AylikPuantaj;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.Parameter;
import org.pdks.entity.Sirket;
import org.pdks.session.DenklestirmeBordroRaporuHome;
import org.pdks.session.FazlaMesaiOrtakIslemler;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("fazlaMesaiUpdate")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class FazlaMesaiUpdate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7638125510272694759L;

	static Logger logger = Logger.getLogger(FazlaMesaiUpdate.class);

	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	Renderer renderer;
	@In(required = false, create = true)
	HashMap<String, String> parameterMap;
	@In(required = false, create = true)
	public Zamanlayici zamanlayici;
	@In
	EntityManager entityManager;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;
 	@In(required = false, create = true)
	DenklestirmeBordroRaporuHome denklestirmeBordroRaporuHome;

	private static boolean calisiyor = Boolean.FALSE;
	private static final String PARAMETER_KEY = "fazlaMesaiUpdate";
	private String hataKonum;
	private Sirket sirket;
	private DenklestirmeAy denklestirmeAy;

	// private User loginUser;
	// private Long tesisId;

	@Asynchronous
	public QuartzTriggerHandle fazlaMesaiUpdateTimer(@Expiration Date when, @IntervalCron String interval) {
		hataKonum = "fazlaMesaiUpdateTimer başladı ";
		hataKonum = "fazlaMesaiUpdateTimer kontrol ediliyor ";
		if (pdksEntityController != null && !isCalisiyor()) {
			Session session = null;
			try {
				setCalisiyor(Boolean.TRUE);
				// logger.error("Ise gelme durumu " + new Date());
				Date time = ortakIslemler.getBugun();
				int dayOfWeek = PdksUtil.getDateField(time, Calendar.DAY_OF_WEEK);
				if (dayOfWeek != Calendar.SUNDAY) {
					session = PdksUtil.getSession(entityManager, Boolean.TRUE);
					hataKonum = "Paramatre okunuyor ";
					Parameter parameter = ortakIslemler.getParameter(session, PARAMETER_KEY);
					String value = (parameter != null && parameter.getValue() != null) ? parameter.getValue() : "";
					String[] saatler = new String[] { "06:30", "18:30" };
					for (int i = 0; i < saatler.length; i++) {
						String saat = saatler[i];
						if (value.indexOf(saat) < 0)
							value += (value.length() > 0 ? "," : "") + saat;
					}
					hataKonum = "Paramatre okundu ";
					if (value != null) {
						hataKonum = "Zaman kontrolu yapılıyor ";
						boolean zamanDurum = PdksUtil.zamanKontrol(PARAMETER_KEY, value, time);
						if (zamanDurum)
							fazlaMesaiUpdateBul(session);

					}
				}

			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				logger.error("fazlaMesaiUpdateTimer : " + e.getMessage());

			} finally {
				if (session != null)
					session.close();
				setCalisiyor(Boolean.FALSE);
			}

		}
		return null;
	}

	/**
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public String fazlaMesaiUpdateBul(Session session) throws Exception {
		Date bugun = ortakIslemler.getBugun();
		Date oncekiDonem = PdksUtil.tariheAyEkleCikar(bugun, -2);
		HashMap fields = new HashMap();
		int d1 = Integer.parseInt(PdksUtil.convertToDateString(oncekiDonem, "yyyyMM")), d2 = Integer.parseInt(PdksUtil.convertToDateString(bugun, "yyyyMM"));
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + DenklestirmeAy.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
		sb.append(" where " + DenklestirmeAy.COLUMN_NAME_DURUM + " = 1 and ( ( " + DenklestirmeAy.COLUMN_NAME_YIL + " * 100 + " + DenklestirmeAy.COLUMN_NAME_AY + " ) between " + d1 + " and " + d2 + " )");
		sb.append(" order by " + DenklestirmeAy.COLUMN_NAME_YIL + ", " + DenklestirmeAy.COLUMN_NAME_AY);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<DenklestirmeAy> list = pdksEntityController.getObjectBySQLList(sb, fields, DenklestirmeAy.class);
		if (fazlaMesaiOrtakIslemler != null && denklestirmeBordroRaporuHome != null) {
			denklestirmeBordroRaporuHome.setSession(session);
			List<Long> grupSirketList = new ArrayList<Long>();
			for (DenklestirmeAy dm : list) {
				denklestirmeAy = dm;
				denklestirmeBordroRaporuHome.setDenklestirmeAy(dm);
				denklestirmeBordroRaporuHome.setYil(dm.getYil());
				denklestirmeBordroRaporuHome.setAy(dm.getAy());
				AylikPuantaj aylikPuantajDonem = new AylikPuantaj(dm);
				List<SelectItem> departmanListe = fazlaMesaiOrtakIslemler.getFazlaMesaiDepartmanList(denklestirmeAy != null ? aylikPuantajDonem : null, false, session);
				if (departmanListe != null) {
					for (SelectItem selectItem : departmanListe) {
						Long departmanId = (Long) selectItem.getValue();
						List<SelectItem> sirketler = fazlaMesaiOrtakIslemler.getFazlaMesaiSirketList(departmanId, denklestirmeAy != null ? aylikPuantajDonem : null, false, session);
						if (sirketler != null) {
							for (SelectItem selectItem2 : sirketler) {
								session.clear();
								try {
									sirket = (Sirket) pdksEntityController.getSQLParamByAktifFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, selectItem2.getValue(), Sirket.class, session);
									if (sirket != null) {
										if (sirket.getSirketGrup() != null) {
											if (grupSirketList.contains(sirket.getSirketGrup().getId()))
												continue;
											grupSirketList.add(sirket.getSirketGrup().getId());

										}
										denklestirmeBordroRaporuHome.setSirket(sirket);
										denklestirmeBordroRaporuHome.setSirketId(sirket.getId());
										denklestirmeBordroRaporuHome.sirketFazlaMesaiGuncelleme();
									}
								} catch (Exception e) {
									logger.error(e);
									e.printStackTrace();
								}
							}
							sirketler = null;
						}
					}
					departmanListe = null;
				}
			}
			list = null;
			grupSirketList = null;
		}
		return "";
	}

	public static boolean isCalisiyor() {
		return calisiyor;
	}

	public static void setCalisiyor(boolean calisiyor) {
		FazlaMesaiUpdate.calisiyor = calisiyor;
	}

	public String getHataKonum() {
		return hataKonum;
	}

	public void setHataKonum(String hataKonum) {
		this.hataKonum = hataKonum;
	}

}