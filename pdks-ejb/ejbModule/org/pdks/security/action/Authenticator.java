package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.permission.PermissionManager;
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.erp.action.SapRfcManager;
import org.pdks.erp.entity.SAPSunucu;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.session.LDAPUserManager;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Stateless
@Name("authenticator")
public class Authenticator implements IAuthenticator, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8380953982616334438L;

	static Logger logger = Logger.getLogger(Authenticator.class);
	@In
	Identity identity;
	@In
	Credentials credentials;
	@In
	EntityManager entityManager;
	@In
	IdentityManager identityManager;
	@In
	PermissionManager permissionManager;
	@In
	PasswordSecurity passwordSecurity;

	@In(create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false, create = true)
	HashMap<String, String> parameterMap;

	/*
	 * @In LDAPUserManager ldapUserManager;
	 */
	@Out(scope = ScopeType.SESSION, required = false)
	User authenticatedUser;
	@Out(scope = ScopeType.SESSION, required = false)
	String kisaKullaniciAdi;

	@In(create = true)
	PdksEntityController pdksEntityController;

	private String adres;
	private List<Liste> mesajList = null;
	private Session session;

	/**
	 * @param str
	 */
	private void addMessageAvailableWarn(String str) {
		PdksUtil.addMessageAvailableWarn(mesajList, str);
	}

	/**
	 * @param str
	 */
	private void addMessageAvailableError(String str) {
		PdksUtil.addMessageAvailableError(mesajList, str);
	}

	@Transactional
	public boolean authenticate() {
		session = PdksUtil.getSession(entityManager, Boolean.FALSE);
		session.clear();
		if (mesajList == null)
			mesajList = new ArrayList<Liste>();
		else
			mesajList.clear();
		authenticatedUser = null;
		String username = credentials.getUsername();
		String userName = username.trim();
		boolean sonuc = Boolean.FALSE;
		User loginUser = null;
		if (passwordSecurity != null && passwordSecurity.isForgot()) {
			ortakIslemler.sifremiUnuttum(mesajList, userName, session);

		} else {
			String password = credentials.getPassword();
			Map<String, String> map = null;
			try {
				map = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
				loginUser = new User();
				loginUser.setUsername(userName);
				loginUser.setPasswordHash(password);
				return false;
			}
			User ldapUser = null;
			adres = map.containsKey("host") ? map.get("host") : "";
			boolean test = adres.indexOf("localhost:8080") >= 0;
			if (!test)
				test = ortakIslemler.testDurum(password);

			kisaKullaniciAdi = "";
			if (userName.indexOf("@") < 0)
				ldapUser = ortakIslemler.kullaniciBul(userName, LDAPUserManager.USER_ATTRIBUTES_SAM_ACCOUNT_NAME);
			else {
				ldapUser = ortakIslemler.kullaniciBul(userName, LDAPUserManager.USER_ATTRIBUTES_PRINCIPAL_NAME);
				if (ldapUser == null)
					ldapUser = ortakIslemler.kullaniciBul(userName, LDAPUserManager.USER_ATTRIBUTES_MAIL);
			}
			if (ldapUser != null && !ldapUser.isDurum())
				ldapUser = null;
			if (ldapUser != null) {
				kisaKullaniciAdi = ldapUser.getStaffId();
				userName = ldapUser.getUsername();
			}

			try {
				loginUser = getKullanici(userName, User.COLUMN_NAME_USERNAME);
				List<String> adminIPList = PdksUtil.getListByString(ortakIslemler.getParameterKey("adminIP"), null);
				String remoteAddr = PdksUtil.getRemoteAddr();
				if (!test)
					test = adminIPList.contains(remoteAddr);
				if (loginUser == null) {
					if (ldapUser != null) {
						loginUser = getKullanici(ldapUser.getShortUsername(), User.COLUMN_NAME_SHORT_USER_NAME);
						if (loginUser != null) {
							loginUser.setUsername(ldapUser.getUsername());
							if (!parameterMap.containsKey("emailBozuk"))
								loginUser.setEmail(ldapUser.getEmail());
							pdksEntityController.saveOrUpdate(session, entityManager, loginUser);
							session.flush();
							userName = ldapUser.getUsername();
						}
					}
				} else if (ldapUser != null && loginUser != null) {
					if (loginUser.getShortUsername() == null || !loginUser.getShortUsername().equals(ldapUser.getShortUsername())) {
						loginUser.setShortUsername(ldapUser.getShortUsername());
						pdksEntityController.saveOrUpdate(session, entityManager, loginUser);
						session.flush();
					}
				}

				if (loginUser == null && ldapUser != null) {
					String sicilNo = "900" + ldapUser.getStaffId().substring(3).trim();
					HashMap parametreMap = new HashMap();
					StringBuilder sb = new StringBuilder();
					sb.append("select U.* from " + Personel.TABLE_NAME + " P " + PdksEntityController.getSelectLOCK());
					sb.append("inner join " + User.TABLE_NAME + " U " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = U." + User.COLUMN_NAME_PERSONEL + " and U." + User.COLUMN_NAME_DURUM + " = 1 and U." + User.COLUMN_NAME_DEPARTMAN + " is not null ");
					sb.append(" where P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " = :sicilNo and P." + Personel.COLUMN_NAME_DURUM + " = 1  ");
					sb.append(" and P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= convert(date,GETDATE()) and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= convert(date,GETDATE())");
					sb.append(" and P." + Personel.COLUMN_NAME_DURUM + " = 1 ");
					parametreMap.put("sicilNo", sicilNo);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					loginUser = (User) pdksEntityController.getObjectBySQL(sb, parametreMap, User.class);
					if (loginUser != null) {
						logger.info(loginUser.getUsername() + " kullanıcı bilgisi okundu.");
						loginUser.setUsername(ldapUser.getUsername());
						if (!parametreMap.containsKey("emailBozuk"))
							loginUser.setEmail(ldapUser.getEmail());
						pdksEntityController.saveOrUpdate(session, entityManager, loginUser);
						session.flush();
					}
				}
				if (!test && parameterMap != null && parameterMap.containsKey("sifreKontrol"))
					test = !parameterMap.get("sifreKontrol").equals("1");
				sonuc = loginUser != null && test;
				String encodePassword = PdksUtil.encodePassword(password);
				if (loginUser != null) {
					loginUser.setLogin(Boolean.FALSE);
					userName = loginUser.getUsername();
					if (!PdksUtil.hasStringValue(kisaKullaniciAdi))
						kisaKullaniciAdi = loginUser.getUsername();
					else
						kisaKullaniciAdi = kisaKullaniciAdi + " <---> " + loginUser.getUsername();

					loginUser.setDurum(loginUser.getPdksPersonel().isCalisiyor());
					if (!loginUser.isDurum())
						addMessageAvailableWarn(loginUser.getAdSoyad() + " ait işe giriş çıkış tarihinde uyumsuz");
					else {
						if (loginUser.isLdapUse() || sonuc) {
							// ldap kullanıyorsa
							try {
								if (!sonuc)
									sonuc = LDAPUserManager.authenticate(userName, password);
								if (!PdksUtil.hasStringValue(kisaKullaniciAdi)) {
									ldapUser = LDAPUserManager.getLDAPUserAttributes(userName);
									if (ldapUser != null)
										kisaKullaniciAdi = ldapUser.getStaffId();
								}
							} catch (Exception e) {
								logger.error("PDKS hata in : \n");
								e.printStackTrace();
								logger.error("PDKS hata out : " + e.getMessage());
								try {
									String ldapHataDonusKodu = LDAPUserManager.ldapHatasininDetayAciklamasiniGetir(e);
									if (ldapHataDonusKodu != null) {
										String mesaj = PdksUtil.getMessageBundleMessage(LDAPUserManager.LDAP_HATA_KODU_KEY_ON_EK + ldapHataDonusKodu);
										logger.info(mesaj + " ( " + userName + " )");
										addMessageAvailableError(mesaj + " ( " + userName + " )");
									}
								} catch (Exception e2) {
									logger.error(e2.getLocalizedMessage());
								}
							}
						} else {
							sonuc = (loginUser.getPasswordHash().equals(encodePassword));
							if (sonuc == false)
								addMessageAvailableError(credentials.getUsername().trim() + " kullanıcısının şifre hatalıdır!");
						}
						if (sonuc) {
							username = loginUser.getUsername();
							FacesContext context = FacesContext.getCurrentInstance();
							try {
								if (loginUser.getPdksPersonel().getSirket() != null && loginUser.getPdksPersonel().getSirket().isLdap()) {
									String email = PdksUtil.getMailAdres(userName);
									if (email != null && (loginUser.getEmail() == null || !email.equals(loginUser.getEmail()))) {
										if (!parameterMap.containsKey("emailBozuk"))
											loginUser.setEmail(email);
										pdksEntityController.saveOrUpdate(session, entityManager, loginUser);
										session.flush();
									}
								}
							} catch (Exception e) {
								logger.error("PDKS hata in : \n");
								e.printStackTrace();
								logger.error("PDKS hata out : " + e.getMessage());
							}
							credentials.setUsername(userName);
							boolean browserIE = PdksUtil.isInternetExplorer((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
							authenticatedUser = loginUser;
							ortakIslemler.setUserRoller(loginUser, session);
							ortakIslemler.setUserTesisler(loginUser,true, session);
							ortakIslemler.setUserBolumler(loginUser, session);
							loginUser.setBrowserIE(browserIE);
							if (loginUser.getYetkiliRollerim() != null) {
								for (Role role : loginUser.getYetkiliRollerim())
									identity.addRole(role.getRolename());
							}
							try {
								List<SAPSunucu> sapSunucular = pdksEntityController.getSQLParamByFieldList(SAPSunucu.TABLE_NAME, SAPSunucu.COLUMN_NAME_DURUM, SAPSunucu.DURUM_AKTIF, SAPSunucu.class, session);
								if (!sapSunucular.isEmpty())
									logger.info("ERP sunucuları okundu.");
								SapRfcManager.setSapSunucular(sapSunucular);
								if (loginUser.getYetkiliRollerim().isEmpty())
									loginUser = ortakIslemler.personelPdksRolAta(loginUser, Boolean.TRUE, session);
								if (PdksUtil.getBundleName() == null) {
									try {
										PdksUtil.setBundleName(context.getApplication().getMessageBundle());
									} catch (Exception e) {
										logger.error("PDKS hata in : \n");
										e.printStackTrace();
										logger.error("PDKS hata out : " + e.getMessage());
										PdksUtil.setBundleName(null);
									}
								}
								loginUser.setTestLogin(test);
								loginUser.setCalistigiSayfa("anasayfa");
								ortakIslemler.sistemeGirisIslemleri(loginUser, Boolean.TRUE, null, null, session);
								logger.info(loginUser.getUsername() + " " + loginUser.getAdSoyad() + " " + (loginUser.getEmail() != null && !loginUser.getEmail().equals(loginUser.getUsername()) ? loginUser.getEmail() + " e-postalı" : "") + " kullanıcısı PDKS sistemine login oldu. "
										+ PdksUtil.getCurrentTimeStampStr());
								loginUser.setSessionSQL(session);
								loginUser.setLogin(Boolean.TRUE);
							} catch (Exception e) {
								logger.error("PDKS hata in : \n");
								e.printStackTrace();
								logger.error("PDKS hata out : " + e.getMessage());
								logger.debug("Hata : " + e.getMessage());
							}
							if (!test || adres.startsWith("surum")) {
								try {
									loginUser.setLastLogin(new Date());
									pdksEntityController.saveOrUpdate(session, entityManager, loginUser);
									session.flush();
								} catch (Exception e) {
								}
							}

						}
					}
				} else if (mesajList.isEmpty())
					addMessageAvailableError(credentials.getUsername().trim() + " kullanıcı adı sistemde kayıtlı değildir!");

				// sonuc = getSonDurum(sonuc, userName, loginUser);
				// return sonuc;
			} catch (Exception ex) {
				mesajList.clear();
				String str = (ex.getMessage() != null ? ex.getMessage() : "Hata oluştu! ") + " " + ex.getClass().getName();
				addMessageAvailableError(str + " [ " + userName + " ]");
				sonuc = false;
			}
		}
		sonuc = getSonDurum(sonuc, userName, loginUser);
		return sonuc;

	}

	/**
	 * @param sonuc
	 * @param userName
	 * @param loginUser
	 * @return
	 */
	private boolean getSonDurum(boolean sonuc, String userName, User loginUser) {
		if (loginUser == null || loginUser.getId() == null)
			sonuc = false;
		authenticatedUser = sonuc ? loginUser : null;
		if (sonuc == false && mesajList.isEmpty() == false) {
			authenticatedUser = new User();
			authenticatedUser.setUsername(userName);
			List list = ortakIslemler.getSelectItemList("hataMesajList", authenticatedUser);
			list.clear();
			list.addAll(mesajList);
		}
		return sonuc;
	}

	/**
	 * @param userName
	 * @param fieldName
	 * @return
	 */
	private User getKullanici(String userName, String fieldName) {
		User user = null;
		try {
			if (userName.indexOf("%") > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("select S.* from " + User.TABLE_NAME + " S " + PdksEntityController.getSelectLOCK());
				sb.append(" where S." + fieldName + " like :userName");
				HashMap fields = new HashMap();
				fields.put("userName", userName);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				user = (User) pdksEntityController.getObjectBySQL(sb, fields, User.class);
			} else
				user = (User) pdksEntityController.getSQLParamByFieldObject(User.TABLE_NAME, fieldName, userName, User.class, session);

		} catch (Exception ex) {
			mesajList.clear();
			String str = (ex.getMessage() != null ? ex.getMessage() : "Hata oluştu! ") + " " + ex.getClass().getName();
			addMessageAvailableError(str + " [ " + userName + " ]");
		}

		if (user != null) {
			Personel personel = user.getPdksPersonel();
			if (user.isDurum() == false || user.getDepartman() == null || personel == null || personel.getDurum().equals(Boolean.FALSE) || personel.isCalisiyor() == false) {
				if (personel.isCalisiyor() == false)
					addMessageAvailableError(personel.getAdSoyad() + " personel işten ayrılmıştır!");
				else if (personel.getDurum().booleanValue() == false)
					addMessageAvailableWarn(personel.getAdSoyad() + " personel aktif değildir!");
				else if (user.isDurum() == false) {
					addMessageAvailableError((personel != null ? personel.getAdSoyad() + " personelin " : "") + user.getUsername() + " kullanıcısı aktif değildir!");
				} else if (user.getDepartman() == null)
					addMessageAvailableWarn((personel != null ? personel.getAdSoyad() + " personelin " : "") + user.getUsername() + " kullanıcısı departmanı tanımlı değildir!");
				user = null;
			}
		}
		return user;
	}

	public String logout() {
		identity.logout();
		return "login";
	}

	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
