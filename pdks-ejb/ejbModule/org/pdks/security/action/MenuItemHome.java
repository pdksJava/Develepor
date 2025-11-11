package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.MenuItem;
import org.pdks.entity.Personel;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserMenuItemTime;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("menuItemHome")
public class MenuItemHome extends EntityHome<MenuItem> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8362313398220211918L;
	static Logger logger = Logger.getLogger(MenuItemHome.class);

	@RequestParameter
	Long menuItemId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;
	@In(create = true)
	StartupAction startupAction;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	private Session session;
	private boolean tesisYetki = false, paramDurum = false;
	private String bolumAciklama;

	private List<UserMenuItemTime> userMenuItemTimeList;

	// SampleDAO sampleDAO=new SampleDAO();

	@Override
	public Object getId() {
		if (menuItemId == null) {
			return super.getId();
		} else {
			return menuItemId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	/**
	 * @param item
	 * @param userDurum
	 * @return
	 */
	public String guncelle(MenuItem item, boolean userDurum) {
		tesisYetki = false;
		paramDurum = false;
		bolumAciklama = null;
		if (authenticatedUser.isAdmin() == false || item == null)
			userDurum = false;
		if (userMenuItemTimeList == null)
			userMenuItemTimeList = new ArrayList<UserMenuItemTime>();
		else
			userMenuItemTimeList.clear();
		if (item == null) {
			item = new MenuItem();
		} else {
			if (item.getDurum().booleanValue() == false || item.getTopMenu() == null || item.getTopMenu().booleanValue())
				userDurum = false;
			if (userDurum) {
				userMenuItemTimeList = ortakIslemler.getUserMenuItemTimeList(item.getId(), null, getSession());
				for (Iterator iterator = userMenuItemTimeList.iterator(); iterator.hasNext();) {
					UserMenuItemTime userMenuItemTime = (UserMenuItemTime) iterator.next();
					User user = userMenuItemTime.getUser();
					Personel personel = user.getPdksPersonel();
					Sirket sirket = personel.getSirket();
					Tanim bolum = personel.getEkSaha3();
					if (bolumAciklama == null && sirket.getDepartman().isAdminMi() && bolum != null) {
						if (bolum.getParentTanim() != null)
							bolumAciklama = bolum.getParentTanim().getAciklama();
					}
					if (tesisYetki == false && personel.getTesis() != null)
						tesisYetki = sirket.isTesisDurumu();
					if (!paramDurum)
						paramDurum = userMenuItemTime.getParametreJSON() != null && userMenuItemTime.getParametreJSON().indexOf("}") > 3;

				}
				if (userMenuItemTimeList.isEmpty() == false) {
					if (bolumAciklama == null)
						bolumAciklama = "Bölüm";
				}

			}
		}

		setInstance(item);
		return "";
	}

	@Transactional
	public String deleteItem() {
		MenuItem item = getInstance();
		Session sessionx = getSession();

		HashMap fields = new HashMap();
		fields.put("menu.id", item.getId());
		if (sessionx != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, sessionx);
		List<UserMenuItemTime> list = pdksEntityController.getObjectByInnerObjectList(fields, UserMenuItemTime.class);
		for (UserMenuItemTime userMenuItemTime : list) {
			pdksEntityController.deleteObject(session, entityManager, userMenuItemTime);
		}
		pdksEntityController.deleteObject(sessionx, entityManager, item);

		sessionx.flush();
		sessionx.clear();
		startupAction.fillMenuItemList(sessionx);
		return "";
	}

	@Override
	@Begin(join = true)
	public String update() {
		Session sessionx = getSession();
		MenuItem item = getInstance();
		Tanim description = item.getDescription();
		if (!description.getTipi().equals(Tanim.TIPI_MENU_BILESENI) || description.getKodu() == null || !description.getKodu().equals(item.getName())) {
			description.setKodu(item.getName());
			description.setTipi(Tanim.TIPI_MENU_BILESENI);
			description.setIslemTarihi(new Date());
			description.setIslemYapan(authenticatedUser);
			pdksEntityController.saveOrUpdate(sessionx, entityManager, description);
		}

		pdksEntityController.saveOrUpdate(sessionx, entityManager, item);
		sessionx.flush();
		PdksUtil.addMessageInfo("İşlem Başarı ile gerçekleştirildi.");
		startupAction.fillMenuItemList(sessionx);
		return "";

	}

	@Override
	@Begin(join = true)
	@Transactional
	public String persist() {
		Session sessionx = getSession();
		String method = "";
		String adres;
		String cikis = "";
		try {
			method = "get" + instance.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + instance.getName().substring(1);
			MenuItemConstant menuItemConstant = new MenuItemConstant();
			adres = (String) PdksUtil.getMethodObject(menuItemConstant, method, null);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			adres = null;
		}

		if (adres == null || !isDefined(instance)) {
			FacesMessage facesMessage = new FacesMessage();
			facesMessage.setSummary("MenuItemConstant.java içerisine tanımlı değil.Lütfen önce tanımlayınız.");
			facesMessage.setDetail("MenuItemConstant.java içerisine tanımlı değil.Lütfen önce tanımlayınız.");
			facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
			this.getFacesContext();
			FacesContext.getCurrentInstance().addMessage("", facesMessage);
		} else {
			MenuItem item = getInstance();
			if (item.getTopMenu())
				item.setStatus(Boolean.TRUE);
			else
				item.setStatus(Boolean.FALSE);
			item.getDescription().setDurum(Boolean.TRUE);
			item.getDescription().setTipi(Tanim.TIPI_MENU_BILESENI);
			item.getDescription().setKodu(item.getName());

			pdksEntityController.saveOrUpdate(sessionx, entityManager, instance);
			sessionx.flush();
			startupAction.fillMenuItemList(sessionx);
			PdksUtil.addMessageInfo("İşlem Başarı ile gerçekleştirildi.");
			cikis = "ok";
		}

		return cikis;
	}

	private boolean isDefined(MenuItem menuItem) {
		boolean booleanValue = Boolean.FALSE;
		String menuName = "", firstCharMenuName = "";
		menuName = menuItem.getName();
		firstCharMenuName = menuName.substring(0, 1);
		menuName = menuName.substring(1);
		menuName = "get" + firstCharMenuName.toUpperCase() + menuName;
		try {
			@SuppressWarnings("unused")
			String action = (String) PdksUtil.getMethodObject(new MenuItemConstant(), menuName, null);
			booleanValue = Boolean.TRUE;
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			booleanValue = Boolean.FALSE;
		}

		return booleanValue;
	}

	public Session getSession() {
		if (session == null) {
			session = authenticatedUser.getSessionSQL();
			if (session == null)
				session = PdksUtil.getSession(entityManager, false);
			if (session != null)
				session.clear();
		}
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<UserMenuItemTime> getUserMenuItemTimeList() {
		return userMenuItemTimeList;
	}

	public void setUserMenuItemTimeList(List<UserMenuItemTime> userMenuItemTimeList) {
		this.userMenuItemTimeList = userMenuItemTimeList;
	}

	public boolean isTesisYetki() {
		return tesisYetki;
	}

	public void setTesisYetki(boolean tesisYetki) {
		this.tesisYetki = tesisYetki;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public boolean isParamDurum() {
		return paramDurum;
	}

	public void setParamDurum(boolean paramDurum) {
		this.paramDurum = paramDurum;
	}

}
