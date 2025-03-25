package org.pdks.dinamikRapor.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.dinamikRapor.entity.PdksDinamikRapor;
import org.pdks.dinamikRapor.entity.PdksDinamikRaporAlan;
import org.pdks.dinamikRapor.entity.PdksDinamikRaporParametre;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("dinamikRaporHome")
public class DinamikRaporHome extends EntityHome<PdksDinamikRapor> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7573955904085170923L;
	static Logger logger = Logger.getLogger(DinamikRaporHome.class);
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

	public static String sayfaURL = "dinamikRapor";
	private PdksDinamikRapor seciliPdksDinamikRapor;
	private List<PdksDinamikRapor> dinamikRaporList;
	private List<PdksDinamikRaporAlan> dinamikRaporAlanList;
	private List<Object[]> dinamikRaporAlanMapList;
	private List<PdksDinamikRaporParametre> dinamikRaporParametreList;

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
	public String sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		String sayfa = "";
		fillPdksDinamikRaporList();
		if (dinamikRaporAlanMapList == null)
			dinamikRaporAlanMapList = new ArrayList<Object[]>();
		else
			dinamikRaporAlanMapList.clear();
		seciliPdksDinamikRapor = null;
		if (dinamikRaporList.isEmpty()) {
			PdksUtil.addMessageAvailableWarn("Rapor alınacak tanımlanmış veri yoktur!");
			sayfa = MenuItemConstant.home;
		}

		return sayfa;
	}

	/**
	 * @param dinamikRapor
	 * @param tip
	 * @return
	 */
	public String dinamikRaporGuncelle(PdksDinamikRapor dinamikRapor) {
		seciliPdksDinamikRapor = dinamikRapor;
		fillDinamikRaporAlanList();
		filllDinamikRaporParametreList();
		dinamikRaporAlanMapList.clear();
		return "";

	}

	public String fillDinamikRaporList() {
		return "";
	}

	public String excelDinamikRaporList() {
		return "";
	}

	public Object getDinamikRaporAlan(Object[] veri, Integer index) {
		Object object = null;
		if (veri != null && veri != null && veri.length <= index)
			object = veri[index];
		return object;
	}

	private void fillPdksDinamikRaporList() {
		session.clear();
		HashMap fields = new HashMap();
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + PdksDinamikRapor.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
		dinamikRaporList = pdksEntityController.getSQLParamByFieldList(PdksDinamikRapor.TABLE_NAME, PdksDinamikRapor.COLUMN_NAME_DURUM, Boolean.TRUE, PdksDinamikRapor.class, session);
		if (authenticatedUser.isAdmin() == false) {
			for (Iterator iterator = dinamikRaporList.iterator(); iterator.hasNext();) {
				PdksDinamikRapor pr = (PdksDinamikRapor) iterator.next();
				if (pr.getGoruntulemeDurum().booleanValue() == false)
					iterator.remove();
			}
		}
	}

	private void fillDinamikRaporAlanList() {
		dinamikRaporAlanList = pdksEntityController.getSQLParamByAktifFieldList(PdksDinamikRaporAlan.TABLE_NAME, PdksDinamikRaporAlan.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporAlan.class, session);
		if (dinamikRaporAlanList.size() > 1)
			dinamikRaporAlanList = PdksUtil.sortListByAlanAdi(dinamikRaporAlanList, "sira", Boolean.FALSE);
	}

	private void filllDinamikRaporParametreList() {
		dinamikRaporParametreList = pdksEntityController.getSQLParamByAktifFieldList(PdksDinamikRaporParametre.TABLE_NAME, PdksDinamikRaporParametre.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporParametre.class, session);
		if (dinamikRaporParametreList.size() > 1)
			dinamikRaporParametreList = PdksUtil.sortListByAlanAdi(dinamikRaporParametreList, "sira", Boolean.FALSE);

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
		DinamikRaporHome.sayfaURL = sayfaURL;
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

	public PdksDinamikRapor getSeciliPdksDinamikRapor() {
		return seciliPdksDinamikRapor;
	}

	public void setSeciliPdksDinamikRapor(PdksDinamikRapor seciliPdksDinamikRapor) {
		this.seciliPdksDinamikRapor = seciliPdksDinamikRapor;
	}

	public List<Object[]> getDinamikRaporAlanMapList() {
		return dinamikRaporAlanMapList;
	}

	public void setDinamikRaporAlanMapList(List<Object[]> dinamikRaporAlanMapList) {
		this.dinamikRaporAlanMapList = dinamikRaporAlanMapList;
	}

}
