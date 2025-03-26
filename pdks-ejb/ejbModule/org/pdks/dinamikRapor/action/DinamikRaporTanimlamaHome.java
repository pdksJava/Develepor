package org.pdks.dinamikRapor.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.dinamikRapor.entity.PdksDinamikRapor;
import org.pdks.dinamikRapor.entity.PdksDinamikRaporAlan;
import org.pdks.dinamikRapor.entity.PdksDinamikRaporParametre;
import org.pdks.dinamikRapor.entity.PdksDinamikRaporTipi;
import org.pdks.dinamikRapor.entity.PdksRaporAlanHizalaTipi;
import org.pdks.dinamikRapor.entity.PdksRaporAlanTipi;
import org.pdks.security.entity.User;
import org.pdks.session.ComponentState;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("dinamikRaporTanimlamaHome")
public class DinamikRaporTanimlamaHome extends EntityHome<PdksDinamikRapor> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4829402964583814743L;
	static Logger logger = Logger.getLogger(DinamikRaporTanimlamaHome.class);
	@RequestParameter
	Long pdksDepartmanId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	ComponentState componentState;

	public static String sayfaURL = "dinamikRaporTanimlama";
	private List<PdksDinamikRapor> dinamikRaporList;
	private PdksDinamikRapor seciliPdksDinamikRapor;
	private PdksDinamikRaporAlan seciliPdksDinamikRaporAlan;
	private PdksDinamikRaporParametre seciliPdksDinamikRaporParametre;
	private List<PdksDinamikRaporAlan> dinamikRaporAlanList;
	private List<PdksDinamikRaporParametre> dinamikRaporParametreList;
	private List<SelectItem> parametreList, alanAdiList, raporTipiList, alanHizalamaList;
	private Session session;

	@Override
	public Object getId() {
		if (pdksDepartmanId == null) {
			return super.getId();
		} else {
			return pdksDepartmanId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		if (raporTipiList == null)
			raporTipiList = new ArrayList<SelectItem>();
		else
			raporTipiList.clear();

		raporTipiList.add(new SelectItem(PdksDinamikRaporTipi.VIEW.value(), PdksDinamikRapor.getPdksDinamikRaporTipiAciklama(PdksDinamikRaporTipi.VIEW.value())));
		raporTipiList.add(new SelectItem(PdksDinamikRaporTipi.FUNCTION.value(), PdksDinamikRapor.getPdksDinamikRaporTipiAciklama(PdksDinamikRaporTipi.FUNCTION.value())));
		raporTipiList.add(new SelectItem(PdksDinamikRaporTipi.STORE_PROCEDURE.value(), PdksDinamikRapor.getPdksDinamikRaporTipiAciklama(PdksDinamikRaporTipi.STORE_PROCEDURE.value())));

		if (alanHizalamaList == null)
			alanHizalamaList = new ArrayList<SelectItem>();
		else
			alanHizalamaList.clear();
		alanHizalamaList.add(new SelectItem(PdksRaporAlanHizalaTipi.SOLA.value(), PdksDinamikRapor.getPdksDinamikRaporAlanhHizalaAciklama(PdksRaporAlanHizalaTipi.SOLA.value())));
		alanHizalamaList.add(new SelectItem(PdksRaporAlanHizalaTipi.ORTALA.value(), PdksDinamikRapor.getPdksDinamikRaporAlanhHizalaAciklama(PdksRaporAlanHizalaTipi.ORTALA.value())));
		alanHizalamaList.add(new SelectItem(PdksRaporAlanHizalaTipi.SAGA.value(), PdksDinamikRapor.getPdksDinamikRaporAlanhHizalaAciklama(PdksRaporAlanHizalaTipi.SAGA.value())));

		if (alanAdiList == null)
			alanAdiList = new ArrayList<SelectItem>();
		else
			alanAdiList.clear();
		alanAdiList.add(new SelectItem(PdksRaporAlanTipi.KARAKTER.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(PdksRaporAlanTipi.KARAKTER.value())));
		alanAdiList.add(new SelectItem(PdksRaporAlanTipi.SAYISAL.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(PdksRaporAlanTipi.SAYISAL.value())));
		alanAdiList.add(new SelectItem(PdksRaporAlanTipi.TARIH.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(PdksRaporAlanTipi.TARIH.value())));
		alanAdiList.add(new SelectItem(PdksRaporAlanTipi.TARIH_SAAT.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(PdksRaporAlanTipi.TARIH_SAAT.value())));
		alanAdiList.add(new SelectItem(PdksRaporAlanTipi.SAAT.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(PdksRaporAlanTipi.SAAT.value())));

		if (parametreList == null)
			parametreList = new ArrayList<SelectItem>();
		else
			parametreList.clear();
		parametreList.add(new SelectItem(PdksRaporAlanTipi.KARAKTER.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(PdksRaporAlanTipi.KARAKTER.value())));
		parametreList.add(new SelectItem(PdksRaporAlanTipi.SAYISAL.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(PdksRaporAlanTipi.SAYISAL.value())));
		parametreList.add(new SelectItem(PdksRaporAlanTipi.TARIH.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(PdksRaporAlanTipi.TARIH.value())));
		fillPdksDinamikRaporList();
	}

	/**
	 * @param dinamikRapor
	 * @param tip
	 * @return
	 */
	public String dinamikRaporGuncelle(PdksDinamikRapor dinamikRapor, String tip) {
		if (dinamikRapor == null) {
			dinamikRapor = new PdksDinamikRapor();
			if (dinamikRaporAlanList == null)
				dinamikRaporAlanList = new ArrayList<PdksDinamikRaporAlan>();
			if (dinamikRaporParametreList == null)
				dinamikRaporParametreList = new ArrayList<PdksDinamikRaporParametre>();
		}
		seciliPdksDinamikRapor = dinamikRapor;
		seciliPdksDinamikRaporAlan = null;
		seciliPdksDinamikRaporParametre = null;
		componentState.setSeciliTab("");
		if (dinamikRapor.getId() != null) {
			fillDinamikRaporAlanList();
			filllDinamikRaporParametreList();
			if (!dinamikRaporParametreList.isEmpty())
				componentState.setSeciliTab("parametreTab");
			else if (!dinamikRaporAlanList.isEmpty())
				componentState.setSeciliTab("alanTab");
		} else {
			dinamikRaporAlanList.clear();
			dinamikRaporParametreList.clear();
		}
		if (tip != null) {
			if (tip.equals("A"))
				dinamikRaporAlanGuncelle(null);
			else if (tip.equals("P"))
				dinamikRaporParametreGuncelle(null);
		}
		return "";

	}

	public String instanceRefresh() {
		if (seciliPdksDinamikRapor.getId() != null)
			session.refresh(seciliPdksDinamikRapor);
		return "";
	}

	public String dinamikRaporAlanGuncelle(PdksDinamikRaporAlan alan) {
		if (alan == null) {
			alan = new PdksDinamikRaporAlan();
			alan.setPdksDinamikRapor(seciliPdksDinamikRapor);
			int sonSira = -1;
			for (PdksDinamikRaporAlan pr : dinamikRaporAlanList) {
				sonSira = pr.getSira();
			}
			alan.setSira(sonSira + 1);
		}
		seciliPdksDinamikRaporAlan = alan;
		return "";

	}

	public String dinamikRaporParametreGuncelle(PdksDinamikRaporParametre parametre) {
		if (parametre == null) {
			parametre = new PdksDinamikRaporParametre();
			parametre.setPdksDinamikRapor(seciliPdksDinamikRapor);
			int sonSira = -1;
			for (PdksDinamikRaporParametre pr : dinamikRaporParametreList) {
				sonSira = pr.getSira();
			}
			parametre.setSira(sonSira + 1);
		}
		seciliPdksDinamikRaporParametre = parametre;
		return "";

	}

	private void fillPdksDinamikRaporList() {
		session.clear();
		HashMap fields = new HashMap();
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + PdksDinamikRapor.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
		dinamikRaporList = pdksEntityController.getObjectBySQLList(sb, fields, PdksDinamikRapor.class);
	}

	@Transactional
	public String savePdksDinamikRapor() {
		if (seciliPdksDinamikRapor.isStoreProcedure()) {
			seciliPdksDinamikRapor.setWhereSQL("");
			seciliPdksDinamikRapor.setOrderSQL("");
		}
		pdksEntityController.saveOrUpdate(session, entityManager, seciliPdksDinamikRapor);
		session.flush();
		fillPdksDinamikRaporList();
		return "";

	}

	@Transactional
	public String updateDinamikRaporAlanSira() {
		Integer sonSira = 0;
		boolean flush = false;
		for (PdksDinamikRaporAlan pr : dinamikRaporAlanList) {
			if (pr.getDurum()) {
				if (!sonSira.equals(pr.getSira())) {
					pr.setSira(sonSira);
					pdksEntityController.saveOrUpdate(session, entityManager, pr);
					flush = true;
				}
				++sonSira;
			}

		}
		if (flush)
			session.flush();
		return "";

	}

	@Transactional
	public String updateDinamikRaporParametreSira() {
		Integer sonSira = 0;
		boolean flush = false;
		for (PdksDinamikRaporParametre pr : dinamikRaporParametreList) {
			if (pr.getDurum()) {
				if (!sonSira.equals(pr.getSira())) {
					pr.setSira(sonSira);
					pdksEntityController.saveOrUpdate(session, entityManager, pr);
					flush = true;
				}
				++sonSira;
			}
		}
		if (flush)
			session.flush();
		return "";

	}

	@Transactional
	public String saveDinamikRaporAlan() {
		try {
			if (seciliPdksDinamikRaporAlan.isKarakter() == false) {
				if (seciliPdksDinamikRaporAlan.isSayisal())
					seciliPdksDinamikRaporAlan.setHizala(PdksRaporAlanHizalaTipi.SAGA.value());
				else
					seciliPdksDinamikRaporAlan.setHizala(PdksRaporAlanHizalaTipi.ORTALA.value());
			}
			pdksEntityController.saveOrUpdate(session, entityManager, seciliPdksDinamikRaporAlan);
			session.flush();
			fillDinamikRaporAlanList();
			if (dinamikRaporAlanList.isEmpty() == false)
				updateDinamikRaporAlanSira();
			dinamikRaporAlanGuncelle(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";

	}

	private void fillDinamikRaporAlanList() {
		dinamikRaporAlanList = pdksEntityController.getSQLParamByFieldList(PdksDinamikRaporAlan.TABLE_NAME, PdksDinamikRaporAlan.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporAlan.class, session);
		if (dinamikRaporAlanList.size() > 1)
			dinamikRaporAlanList = PdksUtil.sortListByAlanAdi(dinamikRaporAlanList, "sira", Boolean.FALSE);
		List<PdksDinamikRaporAlan> list = new ArrayList<PdksDinamikRaporAlan>();
		for (Iterator iterator = dinamikRaporAlanList.iterator(); iterator.hasNext();) {
			PdksDinamikRaporAlan dr = (PdksDinamikRaporAlan) iterator.next();
			if (dr.getDurum().booleanValue() == false) {
				list.add(dr);
				iterator.remove();
			}

		}
		if (!list.isEmpty())
			dinamikRaporAlanList.addAll(list);
		list = null;
		seciliPdksDinamikRaporAlan = null;

	}

	@Transactional
	public String deleteDinamikRaporParametre(PdksDinamikRaporParametre deleteValue) {
		session.delete(deleteValue);
		session.flush();
		try {
			pdksEntityController.savePrepareTableID(PdksDinamikRaporParametre.class, entityManager, session);
		} catch (Exception e) {
			e.printStackTrace();
		}
		session.flush();
		filllDinamikRaporParametreList();
		if (dinamikRaporParametreList.isEmpty() == false)
			updateDinamikRaporParametreSira();
		dinamikRaporParametreGuncelle(null);
		return "";
	}

	@Transactional
	public String deleteDinamikRaporAlan(PdksDinamikRaporAlan deleteValue) {
		session.delete(deleteValue);
		session.flush();
		try {
			pdksEntityController.savePrepareTableID(PdksDinamikRaporAlan.class, entityManager, session);
		} catch (Exception e) {
			e.printStackTrace();
		}
		session.flush();
		fillDinamikRaporAlanList();
		if (dinamikRaporAlanList.isEmpty() == false)
			updateDinamikRaporAlanSira();
		dinamikRaporAlanGuncelle(null);
		return "";

	}

	private void filllDinamikRaporParametreList() {
		dinamikRaporParametreList = pdksEntityController.getSQLParamByFieldList(PdksDinamikRaporParametre.TABLE_NAME, PdksDinamikRaporParametre.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporParametre.class, session);
		if (dinamikRaporParametreList.size() > 1)
			dinamikRaporParametreList = PdksUtil.sortListByAlanAdi(dinamikRaporParametreList, "sira", Boolean.FALSE);
		List<PdksDinamikRaporParametre> list = new ArrayList<PdksDinamikRaporParametre>();
		for (Iterator iterator = dinamikRaporParametreList.iterator(); iterator.hasNext();) {
			PdksDinamikRaporParametre dr = (PdksDinamikRaporParametre) iterator.next();
			if (dr.getDurum().booleanValue() == false) {
				list.add(dr);
				iterator.remove();
			}
		}
		if (!list.isEmpty())
			dinamikRaporParametreList.addAll(list);
		list = null;
		seciliPdksDinamikRaporParametre = null;
	}

	@Transactional
	public String saveDinamikRaporParametre() {
		try {
			if (seciliPdksDinamikRapor.isView()) {
				seciliPdksDinamikRaporParametre.setParametreDurum(Boolean.TRUE);
			} else {
				if (seciliPdksDinamikRapor.isStoreProcedure())
					seciliPdksDinamikRaporParametre.setParametreDurum(Boolean.TRUE);
				seciliPdksDinamikRaporParametre.setEsitlik("");
			}
			if (seciliPdksDinamikRapor.isStoreProcedure() == false) {
				if (PdksUtil.hasStringValue(seciliPdksDinamikRaporParametre.getEsitlik())) {
					List<String> veriler = Arrays.asList(new String[] { "=", "<=", ">=", "like" });
					if (!veriler.contains(seciliPdksDinamikRaporParametre.getEsitlik()))
						seciliPdksDinamikRaporParametre.setEsitlik("");
				}
			}

			pdksEntityController.saveOrUpdate(session, entityManager, seciliPdksDinamikRaporParametre);
			session.flush();
			filllDinamikRaporParametreList();
			if (dinamikRaporParametreList.isEmpty() == false)
				updateDinamikRaporParametreSira();
			dinamikRaporParametreGuncelle(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";

	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		DinamikRaporTanimlamaHome.sayfaURL = sayfaURL;
	}

	public List<PdksDinamikRapor> getDinamikRaporList() {
		return dinamikRaporList;
	}

	public void setDinamikRaporList(List<PdksDinamikRapor> dinamikRaporList) {
		this.dinamikRaporList = dinamikRaporList;
	}

	public List<PdksDinamikRaporAlan> getDinamikRaporAlanList() {
		return dinamikRaporAlanList;
	}

	public void setDinamikRaporAlanList(List<PdksDinamikRaporAlan> dinamikRaporAlanList) {
		this.dinamikRaporAlanList = dinamikRaporAlanList;
	}

	public List<PdksDinamikRaporParametre> getDinamikRaporParametreList() {
		return dinamikRaporParametreList;
	}

	public void setDinamikRaporParametreList(List<PdksDinamikRaporParametre> dinamikRaporParametreList) {
		this.dinamikRaporParametreList = dinamikRaporParametreList;
	}

	public List<SelectItem> getParametreList() {
		return parametreList;
	}

	public void setParametreList(List<SelectItem> parametreList) {
		this.parametreList = parametreList;
	}

	public List<SelectItem> getAlanAdiList() {
		return alanAdiList;
	}

	public void setAlanAdiList(List<SelectItem> alanAdiList) {
		this.alanAdiList = alanAdiList;
	}

	public PdksDinamikRapor getSeciliPdksDinamikRapor() {
		return seciliPdksDinamikRapor;
	}

	public void setSeciliPdksDinamikRapor(PdksDinamikRapor seciliPdksDinamikRapor) {
		this.seciliPdksDinamikRapor = seciliPdksDinamikRapor;
	}

	public List<SelectItem> getRaporTipiList() {
		return raporTipiList;
	}

	public void setRaporTipiList(List<SelectItem> raporTipiList) {
		this.raporTipiList = raporTipiList;
	}

	public PdksDinamikRaporAlan getSeciliPdksDinamikRaporAlan() {
		return seciliPdksDinamikRaporAlan;
	}

	public void setSeciliPdksDinamikRaporAlan(PdksDinamikRaporAlan seciliPdksDinamikRaporAlan) {
		this.seciliPdksDinamikRaporAlan = seciliPdksDinamikRaporAlan;
	}

	public PdksDinamikRaporParametre getSeciliPdksDinamikRaporParametre() {
		return seciliPdksDinamikRaporParametre;
	}

	public void setSeciliPdksDinamikRaporParametre(PdksDinamikRaporParametre seciliPdksDinamikRaporParametre) {
		this.seciliPdksDinamikRaporParametre = seciliPdksDinamikRaporParametre;
	}

	public List<SelectItem> getAlanHizalamaList() {
		return alanHizalamaList;
	}

	public void setAlanHizalamaList(List<SelectItem> alanHizalamaList) {
		this.alanHizalamaList = alanHizalamaList;
	}
}
