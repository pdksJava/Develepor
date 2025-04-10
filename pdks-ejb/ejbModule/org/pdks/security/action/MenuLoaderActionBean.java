package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.pdks.dinamikRapor.entity.PdksDinamikRapor;
import org.pdks.entity.AccountPermission;
import org.pdks.entity.MenuItem;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;
import org.richfaces.component.html.HtmlDropDownMenu;
import org.richfaces.component.html.HtmlMenuGroup;
import org.richfaces.component.html.HtmlMenuItem;

@Name("menuLoaderAction")
public class MenuLoaderActionBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3857102837520938592L;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(create = true)
	Map<String, MenuItem> topActiveMenuItemMap;

	@In(required = false, create = true)
	StatusMessages statusMessages;
	@In(create = true)
	MenuItemConstant menuItemConstant;
	@In(required = false)
	Identity identity;
	@In(required = false, create = true)
	UserHome userHome;
	@In(required = false)
	List<MenuItem> menuItemList;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	User authenticatedUser;

	private Session session;

	private HtmlDropDownMenu adminMenu;
	private HtmlDropDownMenu guestMenu;
	private HtmlDropDownMenu kullaniciIslemleri;
	private HtmlDropDownMenu menuIslemleri;
	private HtmlDropDownMenu izinIslemleri;
	private HtmlDropDownMenu puantajIslemleri;
	private HtmlDropDownMenu raporIslemleri;

	public HtmlDropDownMenu getMenuIslemleri() {
		menuIslemleri = createMenu(MenuItemConstant.menuIslemleri);
		return menuIslemleri;
	}

	public void setMenuIslemleri(HtmlDropDownMenu menuIslemleri) {
		this.menuIslemleri = menuIslemleri;
	}

	public HtmlDropDownMenu getKullaniciIslemleri() {
		kullaniciIslemleri = createMenu(MenuItemConstant.kullaniciIslemleri);
		return kullaniciIslemleri;
	}

	public void setKullaniciIslemleri(HtmlDropDownMenu kullaniciIslemleri) {
		this.kullaniciIslemleri = kullaniciIslemleri;
	}

	public HtmlDropDownMenu getIzinIslemleri() {
		izinIslemleri = createMenu(MenuItemConstant.izinIslemleri);
		return izinIslemleri;
	}

	public void setIzinIslemleri(HtmlDropDownMenu izinIslemleri) {
		this.izinIslemleri = izinIslemleri;
	}

	public HtmlDropDownMenu getPuantajIslemleri() {
		puantajIslemleri = createMenu(MenuItemConstant.puantajIslemleri);
		return puantajIslemleri;
	}

	public HtmlDropDownMenu getRaporIslemleri() {
		raporIslemleri = createMenu(MenuItemConstant.raporIslemleri);
		String menuAdi = "dinamikRapor";
		if (raporIslemleri != null && authenticatedUser != null && userHome != null && userHome.hasPermission(menuAdi, "view")) {
			if (session == null)
				session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
			session.clear();
			HashMap fields = new HashMap();
			fields.put("name", menuAdi);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			MenuItem dinamikRaporMenu = (MenuItem) pdksEntityController.getObjectByInnerObject(fields, MenuItem.class);
			// MenuItem dinamikRaporMenu = (MenuItem) pdksEntityController.getSQLParamByFieldObject(MenuItem.TABLE_NAME, MenuItem.COLUMN_NAME_MENU_ADI, menuAdi, MenuItem.class, session);
			if (dinamikRaporMenu != null) {
				StringBuffer sb = new StringBuffer();
				sb.append("select * from " + PdksDinamikRapor.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
				sb.append(" where " + PdksDinamikRapor.COLUMN_NAME_DURUM + " = 1 ");
				if (authenticatedUser.isAdmin() == false)
					sb.append(" and " + PdksDinamikRapor.COLUMN_NAME_GORUNTULENSIN + " = 1 ");
				sb.append(" order by " + PdksDinamikRapor.COLUMN_NAME_SIRA);
				fields.clear();
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				List<PdksDinamikRapor> raporlar = pdksEntityController.getObjectBySQLList(sb, fields, PdksDinamikRapor.class);
				if (!raporlar.isEmpty() && raporIslemleri.getChildren() != null) {
					List list = new ArrayList();
					for (Iterator iterator = raporIslemleri.getChildren().iterator(); iterator.hasNext();) {
						Object object = (Object) iterator.next();
						boolean ekle = true;
						if (object instanceof HtmlMenuGroup) {
							HtmlMenuGroup raporGrup = (HtmlMenuGroup) object;
							if (raporGrup.getId().equals(menuAdi))
								ekle = false;

						} else if (object instanceof HtmlMenuItem) {
							HtmlMenuItem rapor = (HtmlMenuItem) object;
							if (rapor.getId().equals(menuAdi))
								ekle = false;

						}
						if (ekle)
							list.add(object);
					}
					HtmlMenuGroup raporGrup = new HtmlMenuGroup();
					raporGrup.setId(menuAdi);
					raporGrup.setValue(dinamikRaporMenu.getDescription().getAciklama());
					for (PdksDinamikRapor pdksDinamikRapor : raporlar) {
						HtmlMenuItem rapor = new HtmlMenuItem();
						dinamikRaporMenu.setParametre("id=" + PdksUtil.getEncodeStringByBase64("id=" + pdksDinamikRapor.getId() + "&userId=" + authenticatedUser.getId() + "&time=" + new Date().getTime()));
						rapor.setValue(pdksDinamikRapor.getAciklama());
						rapor.setId(menuAdi + pdksDinamikRapor.getId());
						MethodExpression me = startAction(dinamikRaporMenu);
						if (me != null) {
							rapor.setActionExpression(me);
							raporGrup.getChildren().add(rapor);
						}
					}
					if (raporGrup.getChildren().isEmpty() == false) {
						raporIslemleri.getChildren().clear();
						raporIslemleri.getChildren().add(raporGrup);
						raporIslemleri.getChildren().addAll(list);
						list = null;
					} else
						raporGrup = null;
				}
			}
		}
		return raporIslemleri;
	}

	public void setPuantajIslemleri(HtmlDropDownMenu puantajIslemleri) {
		this.puantajIslemleri = puantajIslemleri;
	}

	public void setRaporIslemleri(HtmlDropDownMenu raporIslemleri) {
		this.raporIslemleri = raporIslemleri;
	}

	public HtmlDropDownMenu getAdminMenu() {
		adminMenu = createMenu(MenuItemConstant.admin);
		return adminMenu;
	}

	public void setAdminMenu(HtmlDropDownMenu adminMenu) {
		this.adminMenu = adminMenu;
	}

	public HtmlDropDownMenu getGuestMenu() {
		guestMenu = createMenu(MenuItemConstant.guest);
		return guestMenu;
	}

	public void setGuestMenu(HtmlDropDownMenu guestMenu) {
		this.guestMenu = guestMenu;
	}

	public HtmlDropDownMenu createMenu(String menuItemName) {
		MenuItem topMenu = menuItemName != null ? topActiveMenuItemMap.get(menuItemName) : null;
		HtmlDropDownMenu menu = null;
		if (topMenu != null) {
			Application app = FacesContext.getCurrentInstance().getApplication();
			menu = (HtmlDropDownMenu) app.createComponent(HtmlDropDownMenu.COMPONENT_TYPE);
			menu.setRendered(Boolean.TRUE);
			menu.setValue(topMenu.getDescription().getAciklama());
			menu.setId(topMenu.getName());
			menu.getChildren().add(menu);
			HtmlMenuItem hmiHomePage;
			HtmlMenuGroup htmlMenuGroup = new HtmlMenuGroup();
			for (MenuItem subMenu : topMenu.getChildMenuItemListSirali()) {
				try {
					if (subMenu.getChildMenuItemList().isEmpty()) {

						if (subMenu.getStatus() && userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
							hmiHomePage = new HtmlMenuItem();
							hmiHomePage.setValue(subMenu.getDescription().getAciklama());
							hmiHomePage.setId(subMenu.getName());
							hmiHomePage.setActionExpression(this.startAction(subMenu));
							// hmiHomePage.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
							if (hmiHomePage.getActionExpression() != null)
								menu.getChildren().add(hmiHomePage);
						}

						// demekki sub menuleri yok

					} else {// sub menuleri de var
						if (userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
							htmlMenuGroup = new HtmlMenuGroup();
							htmlMenuGroup.setValue(subMenu.getDescription().getAciklama());
							htmlMenuGroup.setId(subMenu.getName());
							// htmlMenuGroup.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
							menu.getChildren().add(htmlMenuGroup);
							addChildNodes(subMenu, htmlMenuGroup);// sub menüleri doldur
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return menu;

	}

	/**
	 * Tree olusturulurken loadTree metodu tarafindan cagirilarak nodlarin eklenmesini saglar.
	 * 
	 * @param menuItemList
	 * @param tempNode
	 * @param node2RootMenuItem
	 */
	private void addChildNodes(MenuItem menuItem, HtmlMenuGroup menuGroup) {
		HtmlMenuItem hmiHomePage;
		HtmlMenuGroup htmlMenuGroup = new HtmlMenuGroup();
		for (MenuItem subMenu : menuItem.getChildMenuItemListSirali()) {
			if (subMenu.getChildMenuItemList().isEmpty()) {// demekki sub menuleri yok
				if (subMenu.getStatus() && userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
					hmiHomePage = new HtmlMenuItem();
					hmiHomePage.setValue(subMenu.getDescription().getAciklama());
					hmiHomePage.setActionExpression(startAction(subMenu));
					// hmiHomePage.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
					if (hmiHomePage.getActionExpression() != null)
						menuGroup.getChildren().add(hmiHomePage);
				}
			} else {// sub menuleri de var
				if (userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
					htmlMenuGroup = new HtmlMenuGroup();
					htmlMenuGroup.setValue(subMenu.getDescription().getAciklama());
					// htmlMenuGroup.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
					menuGroup.getChildren().add(htmlMenuGroup);
					addChildNodes(subMenu, htmlMenuGroup);// sub menüleri doldur
				}
			}
		}
	}

	private MethodExpression startAction(MenuItem menuItem) {
		String menuName = "", firstCharMenuName = "", action = "";
		menuName = menuItem.getName().trim();
		firstCharMenuName = menuName.substring(0, 1);
		menuName = menuName.substring(1);
		menuName = "get" + firstCharMenuName.toUpperCase() + menuName;
		try {
			action = (String) PdksUtil.getMethodObject(new MenuItemConstant(), menuName, null);
		} catch (Exception e) {
			action = "/home.seam";
		}
		if (action == null)
			return null;
		FacesContext ctx = FacesContext.getCurrentInstance();
		Application app = ctx.getApplication();
		if (PdksUtil.hasStringValue(menuItem.getParametre()))
			action = action + "?" + menuItem.getParametre();
		Class[] params = {};
		MethodExpression actionExpression = app.getExpressionFactory().createMethodExpression(ctx.getELContext(), action, String.class, params);
		return actionExpression;
	}

}
