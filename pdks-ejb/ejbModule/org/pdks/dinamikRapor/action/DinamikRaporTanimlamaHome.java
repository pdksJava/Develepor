package org.pdks.dinamikRapor.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.pdks.dinamikRapor.entity.PdksRaporAlanTipi;
import org.pdks.security.entity.User;
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

	public static String sayfaURL = "dinamikRaporTanimlama";
	private List<PdksDinamikRapor> dinamikRaporList;
	private PdksDinamikRapor seciliPdksDinamikRapor;
	private PdksDinamikRaporAlan seciliPdksDinamikRaporAlan;
	private PdksDinamikRaporParametre seciliPdksDinamikRaporParametre;
	private List<PdksDinamikRaporAlan> dinamikRaporAlanList;
	private List<PdksDinamikRaporParametre> dinamikRaporParametreList;
	private List<SelectItem> parametreList, alanAdiList, raporTipiList;
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
	 * @return
	 */
	public String dinamikRaporGuncelle(PdksDinamikRapor dinamikRapor) {
		if (dinamikRapor == null) {
			dinamikRapor = new PdksDinamikRapor();
			if (dinamikRaporAlanList == null)
				dinamikRaporAlanList = new ArrayList<PdksDinamikRaporAlan>();
			if (dinamikRaporParametreList == null)
				dinamikRaporParametreList = new ArrayList<PdksDinamikRaporParametre>();
		}
		if (dinamikRapor.getId() != null) {
			dinamikRaporAlanList = pdksEntityController.getSQLParamByFieldList(PdksDinamikRaporAlan.TABLE_NAME, PdksDinamikRaporAlan.COLUMN_NAME_DINAMIK_RAPOR, dinamikRapor.getId(), PdksDinamikRaporAlan.class, session);
			dinamikRaporParametreList = pdksEntityController.getSQLParamByFieldList(PdksDinamikRaporParametre.TABLE_NAME, PdksDinamikRaporParametre.COLUMN_NAME_DINAMIK_RAPOR, dinamikRapor.getId(), PdksDinamikRaporParametre.class, session);
		} else {
			dinamikRaporAlanList.clear();
			dinamikRaporParametreList.clear();
		}
		seciliPdksDinamikRapor = dinamikRapor;
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
		}
		seciliPdksDinamikRaporAlan = alan;
		return "";

	}

	public String dinamikRaporParametreGuncelle(PdksDinamikRaporParametre parametre) {
		if (parametre == null) {
			parametre = new PdksDinamikRaporParametre();
		}
		seciliPdksDinamikRaporParametre = parametre;
		return "";

	}

	private void fillPdksDinamikRaporList() {
		HashMap fields = new HashMap();
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + PdksDinamikRapor.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
		dinamikRaporList = pdksEntityController.getObjectBySQLList(sb, fields, PdksDinamikRapor.class);
	}

	@Transactional
	public String savePdksDinamikRapor() {
		pdksEntityController.saveOrUpdate(session, entityManager, seciliPdksDinamikRapor);
		session.flush();
		fillPdksDinamikRaporList();
		return "";

	}

	@Transactional
	public String saveDinamikRaporAlan() {
		return "";

	}

	@Transactional
	public String saveDinamikRaporParametre() {
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
}
