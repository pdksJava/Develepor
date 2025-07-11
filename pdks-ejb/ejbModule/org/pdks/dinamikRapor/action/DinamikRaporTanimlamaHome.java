package org.pdks.dinamikRapor.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import org.pdks.dinamikRapor.entity.PdksDinamikRaporRole;
import org.pdks.dinamikRapor.enums.ENumAlanHizalaTipi;
import org.pdks.dinamikRapor.enums.ENumDinamikRaporTipi;
import org.pdks.dinamikRapor.enums.ENumEsitlik;
import org.pdks.dinamikRapor.enums.ENumRaporAlanTipi;
import org.pdks.security.action.StartupAction;
import org.pdks.security.entity.Role;
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
	@In(required = false, create = true)
	StartupAction startupAction;

	public static String sayfaURL = "dinamikRaporTanimlama";
	private List<PdksDinamikRapor> dinamikRaporList;
	private PdksDinamikRapor seciliPdksDinamikRapor;
	private PdksDinamikRaporAlan seciliPdksDinamikRaporAlan;
	private PdksDinamikRaporParametre seciliPdksDinamikRaporParametre;
	private List<PdksDinamikRaporAlan> dinamikRaporAlanList, dinamikRaporBagliAlanList;
	private List<Role> raporRoleList, roleList;
	private List<PdksDinamikRaporParametre> dinamikRaporParametreList;
	private List<SelectItem> parametreList, alanAdiList, raporTipiList, alanHizalamaList, esitlikList;
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
		for (ENumDinamikRaporTipi c : ENumDinamikRaporTipi.values())
			raporTipiList.add(new SelectItem(c.value(), PdksDinamikRapor.getPdksDinamikRaporTipiAciklama(c.value())));

		if (alanHizalamaList == null)
			alanHizalamaList = new ArrayList<SelectItem>();
		else
			alanHizalamaList.clear();
		for (ENumAlanHizalaTipi c : ENumAlanHizalaTipi.values())
			alanHizalamaList.add(new SelectItem(c.value(), PdksDinamikRapor.getPdksDinamikRaporAlanhHizalaAciklama(c.value())));

		if (alanAdiList == null)
			alanAdiList = new ArrayList<SelectItem>();
		else
			alanAdiList.clear();
		for (ENumRaporAlanTipi c : ENumRaporAlanTipi.values())
			alanAdiList.add(new SelectItem(c.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(c.value())));

		if (parametreList == null)
			parametreList = new ArrayList<SelectItem>();
		else
			parametreList.clear();
		for (ENumRaporAlanTipi c : ENumRaporAlanTipi.values())
			if (c.equals(ENumRaporAlanTipi.SAAT) == false && c.equals(ENumRaporAlanTipi.TARIH_SAAT) == false)
				parametreList.add(new SelectItem(c.value(), PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(c.value())));

		if (esitlikList == null)
			esitlikList = new ArrayList<SelectItem>();
		else
			esitlikList.clear();
		for (ENumEsitlik c : ENumEsitlik.values())
			if (c.equals(ENumEsitlik.ESIT) == false)
				esitlikList.add(new SelectItem(c.value(), PdksDinamikRapor.getEsitlikAciklama(c.value())));

		fillDinamikRaporList();
	}

	/**
	 * @param dinamikRapor
	 * @param tip
	 * @return
	 */
	public String dinamikRaporGuncelle(PdksDinamikRapor dinamikRapor, String tip) {
		if (dinamikRapor == null) {
			if (dinamikRaporList == null)
				dinamikRaporList = new ArrayList<PdksDinamikRapor>();
			dinamikRapor = new PdksDinamikRapor();
			if (dinamikRaporAlanList == null)
				dinamikRaporAlanList = new ArrayList<PdksDinamikRaporAlan>();
			if (dinamikRaporParametreList == null)
				dinamikRaporParametreList = new ArrayList<PdksDinamikRaporParametre>();
			if (raporRoleList == null)
				raporRoleList = new ArrayList<Role>();
			if (roleList == null)
				roleList = new ArrayList<Role>();
			dinamikRapor.setSira((dinamikRaporList.size() + 1) * 100);
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
			raporRoleList.clear();
			roleList.clear();
		}
		if (tip != null) {
			if (tip.equals("A"))
				dinamikRaporAlanGuncelle(null);
			else if (tip.equals("P"))
				dinamikRaporParametreGuncelle(null);
			else
				fillDinamikRapoRoleList();
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
		dinamikRaporBagliAlanList = seciliPdksDinamikRapor.getId() != null ? pdksEntityController.getSQLParamByFieldList(PdksDinamikRaporAlan.TABLE_NAME, PdksDinamikRaporAlan.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporAlan.class, session)
				: new ArrayList<PdksDinamikRaporAlan>();
		// boolean ekle = parametre == null && parametre.getBagliDinamikAlan() != null;
		if (parametre == null) {
			parametre = new PdksDinamikRaporParametre();
			parametre.setPdksDinamikRapor(seciliPdksDinamikRapor);
			int sonSira = -10;
			for (PdksDinamikRaporParametre pr : dinamikRaporParametreList) {
				sonSira = pr.getSira();
			}
			parametre.setSira(sonSira + 10);

		}
		PdksDinamikRaporAlan bagliDinamikAlan = parametre.getBagliDinamikAlan();
		for (Iterator iterator = dinamikRaporBagliAlanList.iterator(); iterator.hasNext();) {
			PdksDinamikRaporAlan dra = (PdksDinamikRaporAlan) iterator.next();
			if (dra.isKarakter() == false || dra.getBaslik() != null)
				iterator.remove();
			else if (bagliDinamikAlan != null) {
				if (bagliDinamikAlan.getId().equals(dra.getId()))
					bagliDinamikAlan = null;
			}

		}
		if (bagliDinamikAlan != null && bagliDinamikAlan.isKarakter())
			dinamikRaporBagliAlanList.add(bagliDinamikAlan);
		if (dinamikRaporBagliAlanList.size() > 1)
			dinamikRaporBagliAlanList = PdksUtil.sortListByAlanAdi(dinamikRaporBagliAlanList, "sira", Boolean.FALSE);

		seciliPdksDinamikRaporParametre = parametre;
		return "";

	}

	private void fillDinamikRaporList() {
		session.clear();
		HashMap fields = new HashMap();
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + PdksDinamikRapor.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
		dinamikRaporList = pdksEntityController.getObjectBySQLList(sb, fields, PdksDinamikRapor.class);
		startupAction.fillRaporRole(session);
	}

	@Transactional
	public String saveDinamikRapor() {
		if (seciliPdksDinamikRapor.isStoreProcedure()) {
			seciliPdksDinamikRapor.setWhereSQL("");
			seciliPdksDinamikRapor.setOrderSQL("");
		}
		List<PdksDinamikRaporRole> list = null;
		if (seciliPdksDinamikRapor.getId() != null)
			list = pdksEntityController.getSQLParamByFieldList(PdksDinamikRaporRole.TABLE_NAME, PdksDinamikRaporRole.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporRole.class, session);
		else
			list = new ArrayList<PdksDinamikRaporRole>();
		pdksEntityController.saveOrUpdate(session, entityManager, seciliPdksDinamikRapor);
		for (Role role : raporRoleList) {
			boolean kaydet = true;
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				PdksDinamikRaporRole raporRole = (PdksDinamikRaporRole) iterator.next();
				if (raporRole.getRole().getId().equals(role.getId())) {
					kaydet = false;
					iterator.remove();
					break;
				}
			}
			if (kaydet) {
				PdksDinamikRaporRole raporRole = new PdksDinamikRaporRole(seciliPdksDinamikRapor, role);
				pdksEntityController.saveOrUpdate(session, entityManager, raporRole);
			}
		}
		for (PdksDinamikRaporRole raporRole : list)
			session.delete(raporRole);

		session.flush();
		fillDinamikRaporList();
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
				sonSira += 1;
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
				sonSira += 10;
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
					seciliPdksDinamikRaporAlan.setHizala(ENumAlanHizalaTipi.SAGA.value());
				else
					seciliPdksDinamikRaporAlan.setHizala(ENumAlanHizalaTipi.ORTALA.value());
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

	private void fillDinamikRapoRoleList() {
		if (raporRoleList != null)
			raporRoleList.clear();
		else
			raporRoleList = new ArrayList<Role>();
		roleList = pdksEntityController.getSQLParamByFieldList(Role.TABLE_NAME, Role.COLUMN_NAME_STATUS, Boolean.TRUE, Role.class, session);
		for (Iterator iterator = roleList.iterator(); iterator.hasNext();) {
			Role role = (Role) iterator.next();
			if (role.getRolename().equals(Role.TIPI_ADMIN) || role.getRolename().equals(Role.TIPI_SISTEM_YONETICI))
				iterator.remove();
		}
		if (seciliPdksDinamikRapor.getId() != null) {
			List<PdksDinamikRaporRole> list = pdksEntityController.getSQLParamByFieldList(PdksDinamikRaporRole.TABLE_NAME, PdksDinamikRaporRole.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporRole.class, session);
			if (!list.isEmpty()) {
				if (list.size() > 1)
					list = PdksUtil.sortObjectStringAlanList(list, "getRoleAdi", null);
				for (PdksDinamikRaporRole raporRole : list) {
					Role role1 = raporRole.getRole();
					raporRoleList.add(role1);
					for (Iterator iterator = roleList.iterator(); iterator.hasNext();) {
						Role role = (Role) iterator.next();
						if (role1.getId().equals(role.getId())) {
							iterator.remove();
							break;
						}

					}
				}
			}
			list = null;
		}
		if (roleList.size() > 1)
			roleList = PdksUtil.sortObjectStringAlanList(roleList, "getAciklama", null);

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
			pdksEntityController.savePrepareTableID(true, PdksDinamikRaporParametre.class, entityManager, session);
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
			pdksEntityController.savePrepareTableID(true, PdksDinamikRaporAlan.class, entityManager, session);
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
				if (seciliPdksDinamikRapor.isStoreProcedure()) {
					seciliPdksDinamikRaporParametre.setParametreDurum(Boolean.TRUE);
					seciliPdksDinamikRaporParametre.setEsitlik("");
				}
			}
			if (seciliPdksDinamikRapor.isStoreProcedure() == false) {
				if (PdksUtil.hasStringValue(seciliPdksDinamikRaporParametre.getEsitlik())) {
					String esitlik = seciliPdksDinamikRaporParametre.getEsitlik().trim().toLowerCase(Locale.ENGLISH);
					List<String> veriler = Arrays.asList(new String[] { "<=", ">=", "like" });
					if (!veriler.contains(esitlik))
						seciliPdksDinamikRaporParametre.setEsitlik("");
					else
						seciliPdksDinamikRaporParametre.setEsitlik(esitlik);
					veriler = null;
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

	public List<SelectItem> getEsitlikList() {
		return esitlikList;
	}

	public void setEsitlikList(List<SelectItem> esitlikList) {
		this.esitlikList = esitlikList;
	}

	public List<Role> getRaporRoleList() {
		return raporRoleList;
	}

	public void setRaporRoleList(List<Role> raporRoleList) {
		this.raporRoleList = raporRoleList;
	}

	public List<Role> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<Role> roleList) {
		this.roleList = roleList;
	}

	public List<PdksDinamikRaporAlan> getDinamikRaporBagliAlanList() {
		return dinamikRaporBagliAlanList;
	}

	public void setDinamikRaporBagliAlanList(List<PdksDinamikRaporAlan> dinamikRaporBagliAlanList) {
		this.dinamikRaporBagliAlanList = dinamikRaporBagliAlanList;
	}
}
