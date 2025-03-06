package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.security.Identity;
import org.pdks.entity.AccountPermission;
import org.pdks.entity.PersonelIzinDetay;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

import com.Ostermiller.util.RandPass;

@Name("userHome")
public class UserHome extends EntityHome<User> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2931248138867368415L;
	static Logger logger = Logger.getLogger(UserHome.class);

	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false)
	Map accountPermissionMap;
	@RequestParameter
	Long userId;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	FacesMessages facesMessages;
	@In(required = false)
	Identity identity;
	@In(required = false, create = true)
	EntityManager entityManager;

	private static boolean menuKapali = false;
	private List<String> izinRaporlari = Arrays.asList("aylikIzinRapor", "bakiyeIzin", "fazlaMesaiIzin", "holdingKalanIzin", "iseGelmeyenPersonelDagilimi", "izinKagidi", "izinOnay", "personelKalanIzin");
	private List<String> izinIslemler = Arrays.asList("izinIslemleri", "izinERPAktarim", "izinHakedisHakkiTanimlama", "onayimaGelenIzinler", "personelIzinKopyala", "sskIzinGirisi", "personelIzinGirisi");

	private User currentUser;
	private String newPassword1, newPassword2, passwordHash, oldUserName;
	private String changeUserName = "";
	private List<User> allUserList = new ArrayList<User>();
	private Session session;

	@Override
	public Object getId() {
		if (userId == null) {
			return super.getId();
		} else {
			return userId;
		}
	}

	public void userGuncelle(User currentUser) {
		setInstance(currentUser);
	}

	@Transactional
	public String save() {
		User currentUser = getInstance();
		User user = ortakIslemler.digerKullanici(currentUser, getOldUserName(), session);
		String sonuc = "";
		if (user == null) {
			String passwordEncoded = null;
			boolean ldapUse = currentUser.isLdapUse();
			String randPassword = "";
			if (!ldapUse && currentUser.getId() == null) {
				RandPass sifreYaratici = new RandPass();
				randPassword = sifreYaratici.getPass(10);

				// kullaniciya mail atilir.
				getInstance().setNewPassword(randPassword);

				/*
				 * try { passwordEncoded = Base64Encoder.encode(randPassword); } catch (IOException e) { }
				 */
				passwordEncoded = randPassword;
				currentUser.setPasswordHash(passwordEncoded);

			}
			pdksEntityController.saveOrUpdate(session, entityManager, currentUser);
			session.flush();
			assignId(PersistenceProvider.instance().getId(getInstance(), entityManager));
			// createdMessage();
			raiseAfterTransactionSuccessEvent();
			sonuc = "ok";
		} else
			PdksUtil.addMessageWarn(user.getUsername() + " kullanıcısı " + user.getAdSoyad() + " personel'de tanımlıdır");
		return sonuc;

	}

	@Transactional
	public String changePassword() {
		User user = getInstance();
		String oldPasswordData = authenticatedUser != null ? user.getPasswordHash() : null;

		String ekran = "";
		// ilk olarak eski sifre dogru mu kontrol edelim
		try {
			String oldPassword = passwordHash != null ? PdksUtil.encodePassword(passwordHash) : null;
			if (oldPasswordData != null && !oldPassword.equals(oldPasswordData))
				facesMessages.add("Eski şifre doğru girilmelidir.", "");

			else if (!newPassword1.equals(newPassword2))
				facesMessages.add("Yeni şifre onayı farklı doğru girilmelidir.", "");
			else if (passwordHash != null && newPassword1.equals(passwordHash))
				facesMessages.add("Yeni şifreyi farklı girmelisiniz.", "");

			else {
				String newPassword = PdksUtil.encodePassword(newPassword1);
				user.setPasswordHash(newPassword);
				pdksEntityController.saveOrUpdate(session, entityManager, user);
				session.flush();
				facesMessages.add("Yeni şifre değiştirilmiştir.", "");
				if (authenticatedUser != null)
					ekran = "anaSayfa";
				else
					ekran = MenuItemConstant.login;
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return ekran;
	}

	@Override
	protected User createInstance() {
		User user = new User();

		return user;
	}

	public void wire() {
		getInstance();
	}

	public void changeUser() {

	}

	public boolean isWired() {
		return true;
	}

	public User getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public void setDefinedInstance(User user) {
	}

	public List<Role> getDistinctRoles() {
		HashMap parametreMap = new HashMap();
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		parametreMap.put("status=", Boolean.TRUE);
		if (!authenticatedUser.isAdmin())
			parametreMap.put("rolename<>", Role.TIPI_ADMIN);
		parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List allRoles = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, Role.class);
		ortakIslemler.setUserRoller(getInstance(), session);
		if (getInstance() != null && getInstance().getYetkiliRollerim() != null)
			for (Role role : getInstance().getYetkiliRollerim()) {
				if (allRoles.contains(role))
					allRoles.remove(role);
			}

		return allRoles;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sifreUnuttumAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		String str = MenuItemConstant.login;
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String username = (String) req.getParameter("username");
		str = ortakIslemler.sifremiUnuttum(username, session);
		return str;
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();

		fillAllUserList();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sifreDegistirAction() {
		User user = authenticatedUser;
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, user);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		String sayfa = "";
		if (user != null) {
			authenticatedUser.setNewPassword("");
		} else {
			user = null;
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String id = (String) req.getParameter("id");
			if (id != null) {
				String decodeStr = OrtakIslemler.getDecodeStringByBase64(id);
				StringTokenizer st = new StringTokenizer(decodeStr, "&");
				HashMap<String, String> param = new HashMap<String, String>();
				while (st.hasMoreTokens()) {
					String tk = st.nextToken();
					String[] parStrings = tk.split("=");
					param.put(parStrings[0], parStrings[1]);
				}
				Date tarih = null;
				if (param.containsKey("tarih"))
					tarih = new Date(Long.parseLong(param.get("tarih")));
				String sifreUnuttum = ortakIslemler.getParameterKey("sifreUnuttum");
				Integer dakika = null;
				try {
					dakika = Integer.parseInt(sifreUnuttum);
				} catch (Exception e) {
					dakika = -1;
				}
				if (dakika < 1)
					dakika = 5;
				if (tarih == null || PdksUtil.addTarih(tarih, Calendar.MINUTE, dakika).before(new Date())) {
					PdksUtil.addMessageAvailableWarn("Link geçersizdir");
					sayfa = MenuItemConstant.login;
				} else if (param.containsKey("userId")) {
					user = (User) pdksEntityController.getSQLParamByFieldObject(User.TABLE_NAME, User.COLUMN_NAME_ID, new Long(param.get("userId")), User.class, session);
				}
			} else
				sayfa = MenuItemConstant.login;

		}
		setInstance(user);
		return sayfa;
	}

	public void fillAllUserList() {
		HashMap parametreMap = new HashMap();
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List userList = pdksEntityController.getObjectByInnerObjectList(parametreMap, User.class);
		setAllUserList(userList);
	}

	public void setDistinctRoles(List<Role> distinctRoles) {
		logger.debug("sadfsf");
	}

	public List<Role> getRoles() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);

		if (getInstance().getYetkiliRollerim() == null)
			ortakIslemler.setUserRoller(getInstance(), session);
		return getInstance().getYetkiliRollerim();
	}

	public void setRoles(List<Role> roller) {
		getInstance().setYetkiliRollerim(roller);
	}

	/**
	 * @param target
	 * @param action
	 * @return
	 */
	public boolean hasPermission(Object target, String action) {
		boolean sonuc = Boolean.FALSE;
		try {
			if (identity != null && identity.isLoggedIn() && authenticatedUser != null) {
				HashMap<String, Boolean> menuYetkiMap = authenticatedUser.getMenuYetkiMap();
				if (menuYetkiMap == null) {
					menuYetkiMap = new HashMap<String, Boolean>();
					authenticatedUser.setMenuYetkiMap(menuYetkiMap);
				}
				String startKey = action + "-" + target;
				String key = startKey + authenticatedUser.getUsername() + "-" + AccountPermission.DISCRIMINATOR_USER;
				if (menuYetkiMap.containsKey(startKey)) {
					sonuc = menuYetkiMap.get(startKey);
				} else {
					boolean adminRole = authenticatedUser.isAdmin();
					if (accountPermissionMap.containsKey(key)) {
						sonuc = getSonuc(target);
					} else {
						List<Role> yetkiliRollerim = new ArrayList<Role>();
						if (session == null)
							session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
						if (authenticatedUser.getYetkiliRollerim() == null || authenticatedUser.getYetkiliRollerim().isEmpty())
							ortakIslemler.setUserRoller(authenticatedUser, session);
						if (authenticatedUser.getYetkiliRollerim() != null)
							yetkiliRollerim.addAll(authenticatedUser.getYetkiliRollerim());

						if (authenticatedUser.getBagliRoller() == null) {
							List<Role> bagliRoller = PdksUtil.setUserYetki(authenticatedUser);
							authenticatedUser.setBagliRoller(bagliRoller);
						}
						List<Role> digerRoller = authenticatedUser.getBagliRoller();
						if (digerRoller != null && !digerRoller.isEmpty())
							yetkiliRollerim.addAll(digerRoller);

						if (adminRole)
							sonuc = getSonuc(target);
						else if (yetkiliRollerim != null) {
							for (Role role : yetkiliRollerim) {
								String roleName = role.getRolename();
								if (roleName.equals(AccountPermission.ADMIN_ROLE)) {// admin
									// herşeye
									// yetkilidir
									sonuc = getSonuc(target);
									break;
								}
								key = startKey + "-" + roleName + "-" + AccountPermission.DISCRIMINATOR_ROLE;
								if (accountPermissionMap.containsKey(key)) {
									sonuc = getSonuc(target);
									break;
								}
							}
						}
						yetkiliRollerim = null;
					}
					if (sonuc && adminRole == false && menuKapali) {
						String menuKapaliStr = ortakIslemler.getParameterKey("menuKapali");
						if (!(menuKapaliStr.equalsIgnoreCase("ik") && (authenticatedUser.isIK() || authenticatedUser.isSistemYoneticisi())))
							sonuc = !menuKapali;
					}
					menuYetkiMap.put(startKey, sonuc);
				}

			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		return sonuc;
	}

	/**
	 * @param target
	 * @return
	 */
	private boolean getSonuc(Object target) {
		boolean sonuc = Boolean.TRUE;
		if (target != null) {
			boolean sistemYoneticisi = authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin() || authenticatedUser.isSistemYoneticisi();
			if (izinRaporlari.contains(target) || izinIslemler.contains(target)) {
				sonuc = authenticatedUser.isIzinGirebilir();
				if (target.equals("personelKalanIzin") || target.equals("izinRaporlari")) {
					sonuc = authenticatedUser.isIzinGirebilir() || ortakIslemler.getParameterKeyHasStringValue(ortakIslemler.getParametreIzinERPTableView());
				} else if (target.equals("holdingKalanIzin") || target.equals("izinRaporlari")) {
					sonuc = authenticatedUser.isIzinGirebilir() || PersonelIzinDetay.isIzinHakedisGuncelle();
				} else if (target.equals("onayimaGelenIzinler") || target.equals("izinIslemleri"))
					sonuc = authenticatedUser.isIzinOnaylayabilir() || (target.equals("izinIslemleri") && sistemYoneticisi);
				else {
					if (target.equals("sskIzinGirisi"))
						sonuc = authenticatedUser.isIzinSSKGirebilir();
					else if (target.equals("personelIzinGirisi") || target.equals("izinIslemleri"))
						sonuc = sistemYoneticisi;
				}

			} else if (target.equals("izinRaporlari") || target.equals("sskIstirahatIzinleri")) {
				sonuc = authenticatedUser.isIzinGirebilir() || !ortakIslemler.getParameterKey("izinERPUpdate").equals("1");
				if (sonuc == false && target.equals("izinRaporlari") && sistemYoneticisi) {
					sonuc = true;
				}
			}

			else if (target.equals("sifreDegistirme")) {
				sonuc = authenticatedUser.isLdapUse() == false;
			} else if (target.equals("puantajRaporlari")) {
				sonuc = !ortakIslemler.getParameterKey("kartOkuyucuDurum").equals("0");
			} else if (target.equals("sapSunucuTanimlama")) {
				String sapController = ortakIslemler.getParameterKey("sapController");
				sonuc = sapController.equals("2") || sapController.equals("3");
			} else if (target.equals("mesaiTalepListesi")) {
				String fazlaMesaiTalepDurum = ortakIslemler.getParameterKey("fazlaMesaiTalepDurum");
				sonuc = fazlaMesaiTalepDurum.equals("1");
			}
			if (sonuc && ortakIslemler.getSistemDestekVar() && authenticatedUser.isAdmin() == false && authenticatedUser.isSistemYoneticisi() == false) {
				if (target.equals("vardiyaTanimlama") || target.equals("vardiyaSablonTanimlama") || target.equals("calismaModeliTanimlama") || target.equals("kapiTanimlama"))
					sonuc = false;
			}

		}

		return sonuc;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getNewPassword1() {
		return newPassword1;
	}

	public void setNewPassword1(String newPassword1) {
		this.newPassword1 = newPassword1;
	}

	public String getNewPassword2() {
		return newPassword2;
	}

	public void setNewPassword2(String newPassword2) {
		this.newPassword2 = newPassword2;
	}

	public String getOldUserName() {
		return oldUserName;
	}

	public void setOldUserName(String oldUserName) {
		this.oldUserName = oldUserName;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getChangeUserName() {
		return changeUserName;
	}

	public void setChangeUserName(String changeUserName) {
		this.changeUserName = changeUserName;
	}

	public List<User> getAllUserList() {
		return allUserList;
	}

	public void setAllUserList(List<User> allUserList) {
		this.allUserList = allUserList;
	}

	public User getCurrentUser() {
		if (currentUser == null)
			currentUser = new User();
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		setOldUserName(currentUser != null ? currentUser.getUsername() : null);
		this.currentUser = currentUser;
	}

	public static boolean isMenuKapali() {
		return menuKapali;
	}

	public static void setMenuKapali(boolean menuKapali) {
		UserHome.menuKapali = menuKapali;
	}
}
