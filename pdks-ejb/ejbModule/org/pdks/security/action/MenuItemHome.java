package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
import org.pdks.entity.AccountPermission;
import org.pdks.entity.MenuIliski;
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
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeNodeImpl;

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

	public static String sayfaURL = "menuItemTanimlama";
	private String iconLeaf = "/img/plus.gif";

	private MenuItem currentItem;
	private MenuItem nodeTitle;
	private TreeNode<MenuItem> rootNode;

	private Boolean selectAll = Boolean.FALSE;
	private Map<Long, Boolean> selectedIdsFromTreeMap = new HashMap<Long, Boolean>();
	private Map<Long, Boolean> selectedIdsFromDataTableMap = new HashMap<Long, Boolean>();
	private ArrayList<MenuItem> selectedNodeChildren = new ArrayList<MenuItem>();

	private ArrayList<MenuItem> allTreeMenuItemList;
	private List<MenuItem> freeMenuItemList;

	private boolean tesisYetki = false, paramDurum = false;
	private String bolumAciklama;

	private List<UserMenuItemTime> userMenuItemTimeList;

	private Session session;

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
	public void create() {
		super.create();
	}

	public TreeNode<MenuItem> getTreeNode() {
		if (rootNode == null) {
			loadTree();
			fillAllDataTableMenuItemList();
		}

		return rootNode;
	}

	/**
	 * data table verilerini(agaca yerlestirilmemis menuitemleri) getirir.
	 */

	public void fillAllDataTableMenuItemList() {
		List<MenuItem> menuItemList = pdksEntityController.getSQLParamByFieldList(MenuItem.TABLE_NAME, MenuItem.COLUMN_NAME_DURUM, Boolean.FALSE, MenuItem.class, session);
		if (menuItemList.size() > 1)
			menuItemList = PdksUtil.sortObjectStringAlanList(menuItemList, "getItemAciklama", null);
		setFreeMenuItemList(menuItemList);

	}

	/**
	 * data table ve tree den secilen menu itemleri listeleri olusuturlur ve bu listelerden hareketle yeni menu item ve account permissionlar ayarlanir.
	 */
	@Transactional
	public String moveMenuItemsFromDataTable2Tree() {
		ArrayList<String> targetListForDataTable = new ArrayList<String>();
		MenuItem selectedMenuItemFromTree = null;
		FacesMessage facesMessage = new FacesMessage();
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSession(entityManager, false);

		// treeden secili olanlari alir.Bu islem sirasinda agactan bir tane menu
		// bileseni secilmeli fakat biz hepsini aliyoruz. 1den fazlaysa uyari
		// veriyoruz.

		List<MenuItem> selectedMenuItemFromTreeList = new ArrayList<MenuItem>(), selectedMenuItemFromDataTableList = new ArrayList<MenuItem>();
		List<Long> idTreeList = new ArrayList<Long>(), idDataList = new ArrayList<Long>();
		for (MenuItem dataItem : allTreeMenuItemList) {
			if (selectedIdsFromTreeMap.containsKey(dataItem.getId()) && selectedIdsFromTreeMap.get(dataItem.getId()).booleanValue()) {
				idTreeList.add(dataItem.getId());
			}
		}
		if (idTreeList.isEmpty() == false) {
			selectedMenuItemFromTreeList = pdksEntityController.getSQLParamByFieldList(MenuItem.TABLE_NAME, MenuItem.COLUMN_NAME_ID, idTreeList, MenuItem.class, session);
			// datatabledan secili olanlar

			for (MenuItem menuItem : freeMenuItemList) {
				if (selectAll || (selectedIdsFromDataTableMap.containsKey(menuItem.getId()) && selectedIdsFromDataTableMap.get(menuItem.getId()).booleanValue())) {
					idDataList.add(menuItem.getId());
				}

			}
			if (idDataList.isEmpty() == false)
				selectedMenuItemFromDataTableList = pdksEntityController.getSQLParamByFieldList(MenuItem.TABLE_NAME, MenuItem.COLUMN_NAME_ID, idDataList, MenuItem.class, session);
		}
	
		idTreeList = null;
		idDataList = null;
		// Kontroller
		if (selectedMenuItemFromTreeList.isEmpty()) {

			facesMessage.setSummary("Ağaçtan Menü Bileşeni seçilmediğinden Data tabledan seçilen Menü Bileşeni üst bileşen olrak ayarlandı......");
			facesMessage.setDetail("Ağaçtan Menü Bileşeni seçilmediğinden Data tabledan seçilen Menü Bileşeni üst bileşen olrak ayarlandı...");
			facesMessage.setSeverity(FacesMessage.SEVERITY_INFO);
			FacesContext.getCurrentInstance().addMessage("", facesMessage);
		} else if (selectedMenuItemFromDataTableList.size() > 1) {
			facesMessage.setSummary("#{messages['pages.menuItemList.mesaj.birdenFazlaSecim']}");
			facesMessage.setDetail("Birden fazla menü seçilemez");
			facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage("", facesMessage);
		} else
			selectedMenuItemFromTree = selectedMenuItemFromTreeList.get(0);

		if (selectedMenuItemFromDataTableList.isEmpty()) {
			facesMessage.setSummary("Data tabledan Menü Bileşeni seçiniz...");
			facesMessage.setDetail("Data tabledan Menü Bileşeni seçiniz...");
			facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
			FacesContext.getCurrentInstance().addMessage("", facesMessage);
		}

		if (FacesContext.getCurrentInstance().getMaximumSeverity() == FacesMessage.SEVERITY_INFO || FacesContext.getCurrentInstance().getMaximumSeverity() == null) {
			try {
				// Data tabledan alınan menu Itemlerin ayarlanması
				for (Iterator<MenuItem> iterator4DataTable = selectedMenuItemFromDataTableList.iterator(); iterator4DataTable.hasNext();) {
					MenuItem tempMenuItemFromDataTable = (MenuItem) iterator4DataTable.next();
					tempMenuItemFromDataTable.setStatus(Boolean.TRUE);
					if (selectedMenuItemFromDataTableList.isEmpty())
						tempMenuItemFromDataTable.setTopMenu(Boolean.TRUE);
					else {// CHILD MENU
						targetListForDataTable.add(tempMenuItemFromDataTable.getName());
						List<MenuItem> tempppp = new ArrayList<MenuItem>(selectedMenuItemFromTree.getChildMenuItemList());
						tempppp.add(tempMenuItemFromDataTable);
						selectedMenuItemFromTree.setChildMenuItemList(tempppp);

					}
					pdksEntityController.saveOrUpdate(session, entityManager, tempMenuItemFromDataTable);
				}
				if (selectedMenuItemFromTree != null)
					pdksEntityController.saveOrUpdate(session, entityManager, selectedMenuItemFromTree);
				ortakIslemler.sessionFlush(session);
			} catch (Exception e) {
				e.printStackTrace();
			}

			session.clear();
			rootNode = null;
			startupAction.fillMenuItemList(session);
			selectedIdsFromTreeMap.clear();
			selectedIdsFromDataTableMap.clear();

		}
		targetListForDataTable = null;
		selectedMenuItemFromTreeList = null;
		selectedMenuItemFromDataTableList = null;
		return "";
	}

	/**
 	 */
	@Transactional
	public String moveMenuItemsFromTree2DataTable() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSession(entityManager, false);
		ArrayList<MenuItem> deleteMenuItemList = new ArrayList<MenuItem>();
		ArrayList<String> menuItemNameList = new ArrayList<String>();
		List<AccountPermission> deleteAccountPermissionList = new ArrayList<AccountPermission>();
		List<MenuItem> selectedMenuItemFromTreeList = new ArrayList<MenuItem>();
		List<Long> idTreeList = new ArrayList<Long>();

		// treeden secili olanlari alir
		for (MenuItem dataItem : allTreeMenuItemList) {
			try {
				Long key = dataItem.getId();
				if (selectedIdsFromTreeMap.containsKey(key) && selectedIdsFromTreeMap.get(key).booleanValue()) {

					idTreeList.add(dataItem.getId());
					selectedIdsFromTreeMap.remove(dataItem.getId()); // Reset.
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
			}

		}
		if (idTreeList.isEmpty() == false)
			selectedMenuItemFromTreeList = pdksEntityController.getSQLParamByFieldList(MenuItem.TABLE_NAME, MenuItem.COLUMN_NAME_ID, idTreeList, MenuItem.class, session);

		idTreeList = null;

		for (MenuItem tempMenuItemFromTree : selectedMenuItemFromTreeList) {
			tempMenuItemFromTree.setStatus(Boolean.FALSE);
			tempMenuItemFromTree.setTopMenu(Boolean.FALSE);
			// Secili menuItemin parenti bulunur. Parentin child listesinden
			// Secili menuItem çıkarılır.
			if (tempMenuItemFromTree.getParentMenuItem() != null) {
				MenuItem dataItem = tempMenuItemFromTree.getParentMenuItem();
				logger.debug(dataItem.getName() + " " + dataItem.getChildMenuItemList().size());
				for (Iterator iterator = dataItem.getChildMenuItemList().iterator(); iterator.hasNext();) {
					MenuItem tempMenuItem = (MenuItem) iterator.next();
					if (tempMenuItem.getId().equals(tempMenuItemFromTree.getId())) {
						// iterator.remove();
						dataItem.getChildMenuItemList().remove(tempMenuItem);
						break;
					}
				}
				pdksEntityController.saveOrUpdate(session, entityManager, dataItem);

			}

			deleteMenuItemList.add(tempMenuItemFromTree);
			menuItemNameList.add(tempMenuItemFromTree.getName());
			altBilesenleriCikart(menuItemNameList, deleteMenuItemList, tempMenuItemFromTree);
			tempMenuItemFromTree.getChildMenuItemList().clear();
		}
		if (!menuItemNameList.isEmpty()) {
			deleteAccountPermissionList = pdksEntityController.getSQLParamByFieldList(AccountPermission.TABLE_NAME, AccountPermission.COLUMN_NAME_TARGET, menuItemNameList, AccountPermission.class, session);

			for (Iterator iterator = deleteAccountPermissionList.iterator(); iterator.hasNext();) {
				AccountPermission accountPermission = (AccountPermission) iterator.next();
				accountPermission.setStatus(Boolean.FALSE);
				pdksEntityController.saveOrUpdate(session, entityManager, accountPermission);
			}
			for (MenuItem menuItem : deleteMenuItemList)
				pdksEntityController.saveOrUpdate(session, entityManager, menuItem);

			// pdksEntityController.update(deleteAccountPermissionList);
			// pdksEntityController.update(deleteMenuItemList);
			ortakIslemler.sessionFlush(session);
			session.clear();
			rootNode = null;
			startupAction.fillMenuItemList(session);
		}
		selectedMenuItemFromTreeList = null;
		deleteMenuItemList = null;
		menuItemNameList = null;
		deleteAccountPermissionList = null;

		return "";
	}

	/**
	 * @param deleteMenuItemList
	 * @param tempMenuItemList
	 * @param tempMenuItemFromTree
	 */
	private void altBilesenleriCikart(ArrayList<String> deleteMenuItemNameList, ArrayList<MenuItem> deleteMenuItemList, MenuItem menuItem) {
		if (!menuItem.getChildMenuItemList().isEmpty()) {
			for (Iterator iterator = menuItem.getChildMenuItemListSirali().iterator(); iterator.hasNext();) {
				MenuItem tempMenuItem = (MenuItem) iterator.next();
				tempMenuItem.setStatus(Boolean.FALSE);
				tempMenuItem.setTopMenu(Boolean.FALSE);
				deleteMenuItemList.add(tempMenuItem);
				deleteMenuItemNameList.add(tempMenuItem.getName());

				if (!tempMenuItem.getChildMenuItemList().isEmpty()) {
					altBilesenleriCikart(deleteMenuItemNameList, deleteMenuItemList, tempMenuItem);
					tempMenuItem.getChildMenuItemList().clear();
				}
			}
		}
	}

	/**
	 * @param containerList
	 * @param containedMenuItem
	 * @return
	 */
	public boolean contains(ArrayList<MenuItem> containerList, MenuItem containedMenuItem) {
		boolean booleanValue = Boolean.FALSE;
		for (Iterator<MenuItem> iterator = containerList.iterator(); iterator.hasNext();) {
			MenuItem menuItem = (MenuItem) iterator.next();
			if (menuItem.getId().equals(containedMenuItem.getId())) {
				booleanValue = Boolean.TRUE;
				break;
			}
		}
		return booleanValue;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSession(entityManager, false);
		ortakIslemler.setUserMenuItemTime(entityManager, session, sayfaURL);
		menuItemGiris();

		return "";
	}

	private void menuItemGiris() {
		rootNode = null;
		selectedIdsFromTreeMap.clear();
		selectedIdsFromDataTableMap.clear();
		selectedNodeChildren.clear();
		loadTree();
	}

	/**
	 * Tree olusturulurken loadTree metodu tarafindan cagirilarak nodlarin eklenmesini saglar.
	 * 
	 * @param menuItemList
	 * @param tempNode
	 * @param node2RootMenuItem
	 */
	private void addChildNodes(MenuItem parentMenuItem, TreeNode<MenuItem> tempNode) {

		if (!parentMenuItem.getChildMenuItemList().isEmpty()) {
			for (Iterator<MenuItem> menuIterator = parentMenuItem.getChildMenuItemListSirali().iterator(); menuIterator.hasNext();) {
				MenuItem tempMenuItem = (MenuItem) menuIterator.next();
				tempMenuItem.setParentMenuItem(parentMenuItem);
				if (tempMenuItem.getStatus()) {
					TreeNodeImpl<MenuItem> nodeImpl = new TreeNodeImpl<MenuItem>();
					tempNode.addChild(tempMenuItem.getId(), nodeImpl);
					nodeImpl.setData(tempMenuItem);
					addChildNodes(tempMenuItem, nodeImpl);
				}
			}
		}
	}

	/**
	 * Treenin oluşturuldugu Metod
	 * 
	 * @throws Exception
	 */

	private void loadTree() {
		List<MenuItem> list = pdksEntityController.getSQLParamByFieldList(MenuItem.TABLE_NAME, MenuItem.COLUMN_NAME_DURUM, Boolean.TRUE, MenuItem.class, session);
		if (list.size() > 1)
			list = PdksUtil.sortListByAlanAdi(list, "orderNo", Boolean.FALSE);
		if (allTreeMenuItemList == null)
			allTreeMenuItemList = new ArrayList<MenuItem>();
		else
			allTreeMenuItemList.clear();
		allTreeMenuItemList.addAll(list);
		list = null;
		HashMap<Long, MenuItem> map1 = new HashMap<Long, MenuItem>();
		for (MenuItem tempMenuItem : allTreeMenuItemList)
			map1.put(tempMenuItem.getId(), tempMenuItem);
		List<MenuIliski> menuIliskiList = pdksEntityController.getSQLTableList(MenuIliski.TABLE_NAME, MenuIliski.class, session);
		boolean flush = false, baglanti = false;
		for (MenuIliski menuIliski : menuIliskiList) {
			MenuItem menuItem = menuIliski.getMenuItem();
			MenuItem childMenuItem = menuIliski.getChildMenuItem();
			if (menuItem.getStatus().booleanValue() == false || childMenuItem.getStatus().booleanValue() == false) {
				session.delete(menuIliski);
				baglanti = true;
			}
			if (map1.containsKey(menuItem.getId()))
				map1.remove(menuItem.getId());
			if (map1.containsKey(childMenuItem.getId()))
				map1.remove(childMenuItem.getId());
		}
		menuIliskiList = null;
		if (map1.isEmpty() == false) {
			for (Iterator iterator = allTreeMenuItemList.iterator(); iterator.hasNext();) {
				MenuItem mi = (MenuItem) iterator.next();
				if (map1.containsKey(mi.getId())) {
					mi.setStatus(Boolean.FALSE);
					pdksEntityController.saveOrUpdate(session, entityManager, mi);
					iterator.remove();
					flush = true;
				}

			}

		}
		if (flush || baglanti) {
			ortakIslemler.sessionFlush(session);
			if (baglanti) {
				try {
					pdksEntityController.savePrepareTableID(true, null, MenuIliski.class, session);
					ortakIslemler.sessionFlush(session);
				} catch (Exception e) {
				}
			}
		}

		map1 = null;
		try {
			rootNode = new TreeNodeImpl<MenuItem>();
			for (MenuItem tempMenuItem : allTreeMenuItemList) {
				if (tempMenuItem.getTopMenu()) {
					tempMenuItem.setParentMenuItem(null);
					TreeNodeImpl<MenuItem> nodeImpl = new TreeNodeImpl<MenuItem>();
					rootNode.addChild(tempMenuItem.getId(), nodeImpl);
					nodeImpl.setData(tempMenuItem);
					addChildNodes(tempMenuItem, nodeImpl);
				}
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			throw new FacesException(e.getMessage(), e);
		}
	}

	/**
 	 */

	public void processSelection(NodeSelectedEvent event) {
		HtmlTree tree = (HtmlTree) event.getComponent();
		nodeTitle = (MenuItem) tree.getRowData();
		selectedNodeChildren.clear();
		TreeNode<MenuItem> currentNode = tree.getModelTreeNode(tree.getRowKey());
		if (currentNode.isLeaf()) {
			selectedNodeChildren.add((MenuItem) currentNode.getData());
		} else {
			Iterator<Entry<Object, TreeNode<MenuItem>>> it = currentNode.getChildren();
			while (it != null && it.hasNext()) {
				Entry<Object, TreeNode<MenuItem>> entry = it.next();
				selectedNodeChildren.add((MenuItem) entry.getValue().getData());
			}
		}

	}

	/**
	 * @param menuItem
	 * @return
	 */
	public String userList(MenuItem menuItem) {
		if (menuItem != null && menuItem.getMenuItemTimeList() == null) {
			if (menuItem.getId() != null) {

				List<UserMenuItemTime> list = pdksEntityController.getSQLParamByFieldList(UserMenuItemTime.TABLE_NAME, UserMenuItemTime.COLUMN_NAME_MENU, menuItem.getId(), UserMenuItemTime.class, session);
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					UserMenuItemTime userMenuItemTime = (UserMenuItemTime) iterator.next();
					if (!userMenuItemTime.getUser().isDurum())
						iterator.remove();

				}
				if (!list.isEmpty()) {
					list = PdksUtil.sortListByAlanAdi(list, "lastTime", Boolean.TRUE);
					menuItem.setMenuItemTimeList(list);
				}

			}
		}

		return "";
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
				userMenuItemTimeList = pdksEntityController.getSQLParamByFieldList(UserMenuItemTime.TABLE_NAME, UserMenuItemTime.COLUMN_NAME_MENU, item.getId(), UserMenuItemTime.class, session);
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
		List<UserMenuItemTime> list = pdksEntityController.getSQLParamByFieldList(UserMenuItemTime.TABLE_NAME, UserMenuItemTime.COLUMN_NAME_MENU, item.getId(), UserMenuItemTime.class, session);
		for (UserMenuItemTime userMenuItemTime : list)
			pdksEntityController.deleteObject(session, entityManager, userMenuItemTime);
		Tanim description = item.getDescription();
		pdksEntityController.deleteObject(session, entityManager, item);
		pdksEntityController.deleteObject(session, entityManager, description);
		ortakIslemler.sessionFlush(session);
		fillAllDataTableMenuItemList();
		// startupAction.fillMenuItemList(session);
		return "";
	}

	@Transactional
	public String itemGuncelle() {
		MenuItem item = getInstance();
		Tanim description = item.getDescription();
		if (!description.getTipi().equals(Tanim.TIPI_MENU_BILESENI) || description.getKodu() == null || !description.getKodu().equals(item.getName())) {
			description.setKodu(item.getName());
			description.setErpKodu(item.getName());
			description.setTipi(Tanim.TIPI_MENU_BILESENI);
			description.setIslemTarihi(new Date());
			description.setIslemYapan(authenticatedUser);
			pdksEntityController.saveOrUpdate(session, entityManager, description);
		}

		pdksEntityController.saveOrUpdate(session, entityManager, item);
		ortakIslemler.sessionFlush(session);
		PdksUtil.addMessageInfo("İşlem Başarı ile gerçekleştirildi.");
		startupAction.fillMenuItemList(session);

		return "";

	}

	@Transactional
	public String itemEkle() {
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
			Tanim description = item.getDescription();
			description.setDurum(Boolean.TRUE);
			description.setTipi(Tanim.TIPI_MENU_BILESENI);
			description.setKodu(item.getName());
			description.setErpKodu(item.getName());
			pdksEntityController.saveOrUpdate(session, entityManager, description);
			pdksEntityController.saveOrUpdate(session, entityManager, instance);
			ortakIslemler.sessionFlush(session);
			startupAction.fillMenuItemList(session);
			PdksUtil.addMessageInfo("İşlem Başarı ile gerçekleştirildi.");
			cikis = "";
		}
		fillAllDataTableMenuItemList();
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

	public String getIconLeaf() {
		return iconLeaf;
	}

	public void setIconLeaf(String iconLeaf) {
		this.iconLeaf = iconLeaf;
	}

	public MenuItem getCurrentItem() {
		if (currentItem == null)
			currentItem = new MenuItem();
		return currentItem;
	}

	public void setCurrentItem(MenuItem currentItem) {
		this.currentItem = currentItem;
	}

	public MenuItem getNodeTitle() {
		return nodeTitle;
	}

	public void setNodeTitle(MenuItem nodeTitle) {
		this.nodeTitle = nodeTitle;
	}

	/**
	 * @return the selectAll
	 */
	public Boolean getSelectAll() {
		return selectAll;
	}

	/**
	 * @param selectAll
	 *            the selectAll to set
	 */
	public void setSelectAll(Boolean selectAll) {
		this.selectAll = selectAll;
	}

	public Map<Long, Boolean> getSelectedIdsFromTree() {
		return selectedIdsFromTreeMap;
	}

	public Map<Long, Boolean> getSelectedIdsFromDataTable() {
		return selectedIdsFromDataTableMap;
	}

	public Session getSession() {

		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<MenuItem> getFreeMenuItemList() {
		return freeMenuItemList;
	}

	public void setFreeMenuItemList(List<MenuItem> freeMenuItemList) {
		this.freeMenuItemList = freeMenuItemList;
	}

	public Map<Long, Boolean> getSelectedIdsFromTreeMap() {
		return selectedIdsFromTreeMap;
	}

	public void setSelectedIdsFromTreeMap(Map<Long, Boolean> selectedIdsFromTreeMap) {
		this.selectedIdsFromTreeMap = selectedIdsFromTreeMap;
	}

	public Map<Long, Boolean> getSelectedIdsFromDataTableMap() {
		return selectedIdsFromDataTableMap;
	}

	public void setSelectedIdsFromDataTableMap(Map<Long, Boolean> selectedIdsFromDataTableMap) {
		this.selectedIdsFromDataTableMap = selectedIdsFromDataTableMap;
	}

	public ArrayList<MenuItem> getSelectedNodeChildren() {
		return selectedNodeChildren;
	}

	public void setSelectedNodeChildren(ArrayList<MenuItem> selectedNodeChildren) {
		this.selectedNodeChildren = selectedNodeChildren;
	}

}
