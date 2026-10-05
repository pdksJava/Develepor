package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
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
	static Logger logger = Logger.getLogger(MenuLoaderActionBean.class);
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
		menuIslemleri = createMenu(MenuItemConstant.menuIslemleri, false);
		return menuIslemleri;
	}

	public void setMenuIslemleri(HtmlDropDownMenu menuIslemleri) {
		this.menuIslemleri = menuIslemleri;
	}

	public HtmlDropDownMenu getKullaniciIslemleri() {
		kullaniciIslemleri = createMenu(MenuItemConstant.kullaniciIslemleri, false);
		return kullaniciIslemleri;
	}

	public void setKullaniciIslemleri(HtmlDropDownMenu kullaniciIslemleri) {
		this.kullaniciIslemleri = kullaniciIslemleri;
	}

	public HtmlDropDownMenu getIzinIslemleri() {
		izinIslemleri = createMenu(MenuItemConstant.izinIslemleri, false);
		return izinIslemleri;
	}

	public void setIzinIslemleri(HtmlDropDownMenu izinIslemleri) {
		this.izinIslemleri = izinIslemleri;
	}

	public HtmlDropDownMenu getPuantajIslemleri() {
		puantajIslemleri = createMenu(MenuItemConstant.puantajIslemleri, false);
		return puantajIslemleri;
	}

	public HtmlDropDownMenu getRaporIslemleri() {
		raporIslemleri = createMenu(MenuItemConstant.raporIslemleri, true);
		String menuAdi = "dinamikRapor";
		if (raporIslemleri != null && authenticatedUser != null && userHome != null && userHome.hasPermission(menuAdi, "view")) {
			if (PdksUtil.isSessionKapali(session)) {
				session = authenticatedUser.getSessionSQL();
				if (PdksUtil.isSessionKapali(session))
					session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
			}

			session.clear();
			try {
				dinamikRaporUpdate(menuAdi);
			} catch (Exception e) {
			}
		}
		return raporIslemleri;
	}

	/**
	 * @param menuAdi
	 */
	private void dinamikRaporUpdate(String menuAdi) {
		HashMap fields = new HashMap();

		MenuItem dinamikRaporMenu = new MenuItem();
		dinamikRaporMenu.setName(menuAdi);

		String menuBaslik = ortakIslemler.getMenuAdi(menuAdi);
		if (PdksUtil.hasStringValue(menuBaslik) == false)
			menuBaslik = "Dinamik Raporlar";
		StringBuilder sb = new StringBuilder();
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
			HashMap<String, List<PdksDinamikRapor>> bagliRaporMap = new HashMap<String, List<PdksDinamikRapor>>();
			for (Iterator iterator = raporlar.iterator(); iterator.hasNext();) {
				PdksDinamikRapor pdksDinamikRapor = (PdksDinamikRapor) iterator.next();
				if (pdksDinamikRapor.getUstMenu() != null) {
					String key = pdksDinamikRapor.getUstMenu().getName();
					List<PdksDinamikRapor> list1 = bagliRaporMap.containsKey(key) ? bagliRaporMap.get(key) : new ArrayList<PdksDinamikRapor>();
					if (list1.isEmpty())
						bagliRaporMap.put(key, list1);
					list1.add(pdksDinamikRapor);
					iterator.remove();
				}

			}
			List list = new ArrayList();
			for (Iterator iterator = raporIslemleri.getChildren().iterator(); iterator.hasNext();) {
				UIComponent component = (UIComponent) iterator.next();
				boolean ekle = true;
				String id = component.getId();
				if (component instanceof HtmlMenuGroup) {
					HtmlMenuGroup raporGrup = (HtmlMenuGroup) component;

					if (id.equals(menuAdi))
						ekle = false;
					else {
						if (bagliRaporMap.containsKey(id)) {
							List<UIComponent> childrenList = raporGrup.getChildren();
							bagliRaporMap.remove(id);
							List<PdksDinamikRapor> list1 = bagliRaporMap.get(id);
							for (PdksDinamikRapor pdksDinamikRapor : list1) {
								MenuItem dinamikMenu = (MenuItem) dinamikRaporMenu.cloneEmpty();
								dinamikMenu.setName(dinamikRaporMenu.getName());
								// dinamikMenu.setTopMenu(true);
								HtmlMenuItem rapor = new HtmlMenuItem();
								rapor.setData(pdksDinamikRapor.getSira());
								dinamikMenu.setParametre("id=" + PdksUtil.getEncodeStringByBase64("id=" + pdksDinamikRapor.getId() + "&userId=" + authenticatedUser.getId() + "&time=" + new Date().getTime()));
								rapor.setValue(pdksDinamikRapor.getAciklama() + (authenticatedUser.isAdmin() == false ? "" : " [ Dinamik ]"));
								rapor.setId(id + "_" + menuAdi + pdksDinamikRapor.getId());
								MethodExpression me = null;
								try {
									me = startAction(dinamikMenu);
								} catch (Exception e) {
									System.err.println(e);
								}

								if (me != null) {
									rapor.setActionExpression(me);

								}
								childrenList.add(rapor);
							}
							if (childrenList.size() > 1) {
								TreeMap<Long, HtmlMenuItem> map = new TreeMap<Long, HtmlMenuItem>();
								for (UIComponent child : childrenList) {
									if (child instanceof HtmlMenuItem) {
										HtmlMenuItem item = (HtmlMenuItem) child;
										Long key = Long.parseLong(item.getData() == null ? "0" : item.getData().toString());
										map.put(key, item);

									}
								}

								childrenList.clear();
								for (Long key : map.keySet()) {
									raporGrup.getChildren().add(map.get(key));
								}

							}

						}
					}

				} else if (component instanceof HtmlMenuItem) {
					if (id.equals(menuAdi))
						ekle = false;
				}
				if (ekle)
					list.add(component);
			}
			if (bagliRaporMap.isEmpty() == false) {
				for (String key : bagliRaporMap.keySet()) {
					List<PdksDinamikRapor> list1 = bagliRaporMap.get(key);
					raporlar.addAll(list1);
				}
				if (raporlar.size() > 1)
					raporlar = PdksUtil.sortListByAlanAdi(raporlar, "sira", false);
			}
			if (raporlar.isEmpty() == false) {
				HtmlMenuGroup raporGrup = new HtmlMenuGroup();
				raporGrup.setId(menuAdi);
				raporGrup.setValue(menuBaslik);
				for (Iterator iterator = raporlar.iterator(); iterator.hasNext();) {
					PdksDinamikRapor pdksDinamikRapor = (PdksDinamikRapor) iterator.next();
 					if (ortakIslemler.isRaporYetkili(pdksDinamikRapor) == false) {
						iterator.remove();
						continue;
					}
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
		if (raporlar.isEmpty()) {
			for (Iterator iterator = raporIslemleri.getChildren().iterator(); iterator.hasNext();) {
				UIComponent component = (UIComponent) iterator.next();
				if (component.getId().endsWith(menuAdi))
					iterator.remove();

			}
		}
		menuAltKontrol(raporIslemleri);

	}

	/**
	 * @param dropDownMenu
	 */
	private boolean menuAltKontrol(UIComponent dropDownMenu) {
		boolean detayVar = false;
		if (dropDownMenu != null) {
			if (dropDownMenu.getId().equals("dinamikRapor"))
				logger.debug("");
			List<UIComponent> list = dropDownMenu.getChildren();
			if (list != null) {
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					UIComponent component = (UIComponent) iterator.next();
					if (component.getId().equals(dropDownMenu.getId()))
						continue;
					if (component instanceof HtmlMenuGroup || component instanceof HtmlDropDownMenu) {
						boolean altDetayVar = false;
						for (UIComponent child : component.getChildren()) {
							if (child instanceof HtmlMenuItem) {
								altDetayVar = true;
								break;
							} else if (child instanceof HtmlMenuGroup || component instanceof HtmlDropDownMenu)
								altDetayVar = menuAltKontrol(child);
						}
						if (altDetayVar == false) {
							logger.info(component.getId());
							iterator.remove();
						} else {
							detayVar = true;
						}

					} else if (component instanceof HtmlMenuItem) {
						detayVar = true;
						break;
					}

				}
			}
		}
		return detayVar;
	}

	/**
	 * @param menuItemName
	 * @param raporMenu
	 * @return
	 */
	public HtmlDropDownMenu createMenu(String menuItemName, boolean raporMenu) {
		MenuItem topMenu = menuItemName != null ? topActiveMenuItemMap.get(menuItemName) : null;
		HtmlDropDownMenu menu = null;
		if (topMenu != null) {
			Application app = FacesContext.getCurrentInstance().getApplication();
			menu = (HtmlDropDownMenu) app.createComponent(HtmlDropDownMenu.COMPONENT_TYPE);
			menu.setRendered(Boolean.TRUE);
			String aciklama = ortakIslemler.getMenuAciklamaERP(topMenu);
			menu.setValue(aciklama);
			menu.setId(topMenu.getName());
			menu.getChildren().add(menu);
			HtmlMenuItem hmiHomePage;
			HtmlMenuGroup htmlMenuGroup = new HtmlMenuGroup();
			for (MenuItem subMenu : topMenu.getChildMenuItemListSirali()) {
				try {
					if (subMenu.getChildMenuItemList().isEmpty() && subMenu.istUstMenu() == false) {

						if (subMenu.getStatus() && userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
							hmiHomePage = new HtmlMenuItem();
							String subAciklama = ortakIslemler.getMenuAciklamaERP(subMenu);
							hmiHomePage.setValue(subAciklama);
							hmiHomePage.setData(subMenu.getOrderNo());
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
							String subAciklama = ortakIslemler.getMenuAciklamaERP(subMenu);
							htmlMenuGroup.setValue(subAciklama);
							htmlMenuGroup.setId(subMenu.getName());
							// htmlMenuGroup.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
							menu.getChildren().add(htmlMenuGroup);
							addChildNodes(subMenu, htmlMenuGroup);// sub menüleri doldur
						}
					}
				} catch (Exception e) {
				}
			}
			if (raporMenu == false)
				menuAltKontrol(menu);
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
	private boolean addChildNodes(MenuItem menuItem, HtmlMenuGroup menuGroup) {
		HtmlMenuItem hmiHomePage;
		HtmlMenuGroup htmlMenuGroup = new HtmlMenuGroup();
		boolean eklendi = false;
		for (MenuItem subMenu : menuItem.getChildMenuItemListSirali()) {
			eklendi = true;
			if (subMenu.getChildMenuItemList().isEmpty() && subMenu.istUstMenu() == false) {// demekki sub menuleri yok
				if (subMenu.getStatus() && userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
					hmiHomePage = new HtmlMenuItem();
					String aciklama = ortakIslemler.getMenuAciklamaERP(subMenu);
					hmiHomePage.setValue(aciklama);
					hmiHomePage.setData(subMenu.getOrderNo());
					hmiHomePage.setActionExpression(startAction(subMenu));
					// hmiHomePage.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
					if (hmiHomePage.getActionExpression() != null)
						menuGroup.getChildren().add(hmiHomePage);
				}
			} else {// sub menuleri de var
				if (userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW)) {// gormeye yetkisi yoksa menüyü yaratmasın
					htmlMenuGroup = new HtmlMenuGroup();
					String aciklama = ortakIslemler.getMenuAciklamaERP(subMenu);
					htmlMenuGroup.setValue(aciklama);
					// htmlMenuGroup.setRendered(userHome.hasPermission(subMenu.getName(), AccountPermission.ACTION_VIEW));
					menuGroup.getChildren().add(htmlMenuGroup);
					if (subMenu.getChildMenuItemList().isEmpty() == false)
						addChildNodes(subMenu, htmlMenuGroup);// sub menüleri doldur
				}
			}
		}
		return eklendi;
	}

	/**
	 * @param menuItem
	 * @return
	 */
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

	public void setPuantajIslemleri(HtmlDropDownMenu puantajIslemleri) {
		this.puantajIslemleri = puantajIslemleri;
	}

	public void setRaporIslemleri(HtmlDropDownMenu raporIslemleri) {
		this.raporIslemleri = raporIslemleri;
	}

	public HtmlDropDownMenu getAdminMenu() {
		adminMenu = createMenu(MenuItemConstant.admin, false);
		return adminMenu;
	}

	public void setAdminMenu(HtmlDropDownMenu adminMenu) {
		this.adminMenu = adminMenu;
	}

	public HtmlDropDownMenu getGuestMenu() {
		guestMenu = createMenu(MenuItemConstant.guest, false);
		return guestMenu;
	}

	public void setGuestMenu(HtmlDropDownMenu guestMenu) {
		this.guestMenu = guestMenu;
	}

}
