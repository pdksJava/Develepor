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
import org.jboss.seam.international.StatusMessage.Severity;
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
import org.pdks.session.IAuthenticator;
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
	private static final long serialVersionUID = -5011682752102859161L;
	static Logger logger = Logger.getLogger(Authenticator.class);

	@In
	Identity identity;
	@In
	PdksCredentials pdksCredentials;
	@In
	Credentials credentials;
	@In
	EntityManager entityManager;
	@In
	IdentityManager identityManager;
	@In
	PermissionManager permissionManager;

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

	@Out(scope = ScopeType.SESSION, required = false)
	List<Liste> mesajList;

	@In(create = true)
	PdksEntityController pdksEntityController;

	private String adres;

	private Session session;

	/**
	 * @param list
	 * @param str
	 */
	public static void addMessageAvailableInfo(List<Liste> list, String str) {
		if (list != null) {
			Liste liste = new Liste(Severity.INFO, str);
			liste.setSelected("blue");
			liste.setChecked("msginfo.png");
			list.add(liste);
		}
		// PdksUtil.addMessageAvailableInfo(str);
	}

	/**
	 * @param list
	 * @param str
	 */
	public static void addMessageAvailableError(List<Liste> list, String str) {
		if (list != null) {
			Liste liste = new Liste(Severity.ERROR, str);
			liste.setSelected("red");
			liste.setChecked("msgerror.png");
			list.add(liste);
		}
		// PdksUtil.addMessageAvailableError(str);
	}

	/**
	 * @param list
	 * @param str
	 */
	public static void addMessageAvailableWarn(List<Liste> list, String str) {
		if (list != null) {
			Liste liste = new Liste(Severity.WARN, str);
			liste.setSelected("black");
			liste.setChecked("msgwarn.png");
			list.add(liste);
		}
		// PdksUtil.addMessageAvailableWarn(str);
	}

	/**
	 * @param str
	 */
	private void addMessageAvailableWarn(String str) {
		addMessageAvailableWarn(mesajList, str);
	}

	/**
	 * @param str
	 */
	private void addMessageAvailableError(String str) {
		addMessageAvailableError(mesajList, str);
	}

	@Transactional
	public boolean authenticate() {
		session = PdksUtil.getSession(entityManager, Boolean.FALSE);
		session.clear();
		if (mesajList == null)
			mesajList = new ArrayList<Liste>();
		else
			mesajList.clear();
		String username = credentials.getUsername();
		String userName = username.trim();
		boolean sonuc = Boolean.FALSE;
		if (pdksCredentials.isForgetPassword()) {
			pdksCredentials.setForgetPassword(false);
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
				authenticatedUser = new User();
				authenticatedUser.setUsername(userName);
				authenticatedUser.setPasswordHash(password);
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
				authenticatedUser = getKullanici(userName, User.COLUMN_NAME_USERNAME);
				List<String> adminIPList = PdksUtil.getListByString(ortakIslemler.getParameterKey("adminIP"), null);
				String remoteAddr = PdksUtil.getRemoteAddr();
				if (!test)
					test = adminIPList.contains(remoteAddr);
				if (authenticatedUser == null) {
					if (ldapUser != null) {
						authenticatedUser = getKullanici(ldapUser.getShortUsername(), User.COLUMN_NAME_SHORT_USER_NAME);
						if (authenticatedUser != null) {
							authenticatedUser.setUsername(ldapUser.getUsername());
							if (!parameterMap.containsKey("emailBozuk"))
								authenticatedUser.setEmail(ldapUser.getEmail());
							pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
							session.flush();
							// authenticatedUser = entityManager.merge(authenticatedUser);
							// entityManager.flush();
							userName = ldapUser.getUsername();
						}
					}
				} else if (ldapUser != null && authenticatedUser != null) {
					if (authenticatedUser.getShortUsername() == null || !authenticatedUser.getShortUsername().equals(ldapUser.getShortUsername())) {
						authenticatedUser.setShortUsername(ldapUser.getShortUsername());

						pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
						session.flush();
					}
				}

				if (authenticatedUser == null && ldapUser != null) {
					String sicilNo = "900" + ldapUser.getStaffId().substring(3).trim();
					HashMap parametreMap = new HashMap();
					StringBuffer sb = new StringBuffer();
					sb.append("select U.* from " + Personel.TABLE_NAME + " P " + PdksEntityController.getSelectLOCK());
					sb.append("inner join " + User.TABLE_NAME + " U " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = U." + User.COLUMN_NAME_PERSONEL + " and U." + User.COLUMN_NAME_DURUM + " = 1 and U." + User.COLUMN_NAME_DEPARTMAN + " is not null ");
					sb.append(" where P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " = :sicilNo and P." + Personel.COLUMN_NAME_DURUM + " = 1  ");
					sb.append(" and P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= convert(date,GETDATE()) and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= convert(date,GETDATE())");
					sb.append(" and P." + Personel.COLUMN_NAME_DURUM + " = 1 ");
					parametreMap.put("sicilNo", sicilNo);
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					authenticatedUser = (User) pdksEntityController.getObjectBySQL(sb, parametreMap, User.class);

					if (authenticatedUser != null) {
						logger.info(authenticatedUser.getUsername() + " kullanıcı bilgisi okundu.");
						authenticatedUser.setUsername(ldapUser.getUsername());
						if (!parametreMap.containsKey("emailBozuk"))
							authenticatedUser.setEmail(ldapUser.getEmail());

						pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
						session.flush();
					}

				}
				if (!test && parameterMap != null && parameterMap.containsKey("sifreKontrol"))
					test = !parameterMap.get("sifreKontrol").equals("1");
				sonuc = authenticatedUser != null && test;
				String encodePassword = PdksUtil.encodePassword(password);
				if (authenticatedUser != null) {
					authenticatedUser.setLogin(Boolean.FALSE);
					userName = authenticatedUser.getUsername();
					if (!PdksUtil.hasStringValue(kisaKullaniciAdi))
						kisaKullaniciAdi = authenticatedUser.getUsername();
					else
						kisaKullaniciAdi = kisaKullaniciAdi + " <---> " + authenticatedUser.getUsername();

					authenticatedUser.setDurum(authenticatedUser.getPdksPersonel().isCalisiyor());
					if (!authenticatedUser.isDurum())
						addMessageAvailableWarn(authenticatedUser.getAdSoyad() + " ait işe giriş çıkış tarihinde uyumsuz");
					else {
						if (authenticatedUser.isLdapUse() || sonuc) {
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
									}

								} catch (Exception e2) {
									logger.error(e2.getLocalizedMessage());
								}
							}

						} else {

							sonuc = (authenticatedUser.getPasswordHash().equals(encodePassword));
						}
						if (sonuc) {
							username = authenticatedUser.getUsername();
							FacesContext context = FacesContext.getCurrentInstance();
							try {
								if (authenticatedUser.getPdksPersonel().getSirket() != null && authenticatedUser.getPdksPersonel().getSirket().isLdap()) {
									String email = PdksUtil.getMailAdres(userName);
									if (email != null && (authenticatedUser.getEmail() == null || !email.equals(authenticatedUser.getEmail()))) {
										if (!parameterMap.containsKey("emailBozuk"))
											authenticatedUser.setEmail(email);
										pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
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
							ortakIslemler.setUserRoller(authenticatedUser, session);
							ortakIslemler.setUserTesisler(authenticatedUser, session);
							ortakIslemler.setUserBolumler(authenticatedUser, session);
							authenticatedUser.setBrowserIE(browserIE);
							if (authenticatedUser.getYetkiliRollerim() != null) {
								for (Role role : authenticatedUser.getYetkiliRollerim())
									identity.addRole(role.getRolename());
							}
							try {

								List<SAPSunucu> sapSunucular = pdksEntityController.getSQLParamByFieldList(SAPSunucu.TABLE_NAME, SAPSunucu.COLUMN_NAME_DURUM, SAPSunucu.DURUM_AKTIF, SAPSunucu.class, session);

								if (!sapSunucular.isEmpty())
									logger.info("ERP sunucuları okundu.");

								SapRfcManager.setSapSunucular(sapSunucular);

								if (authenticatedUser.getYetkiliRollerim().isEmpty())
									authenticatedUser = ortakIslemler.personelPdksRolAta(authenticatedUser, Boolean.TRUE, session);
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
								authenticatedUser.setTestLogin(test);
								authenticatedUser.setCalistigiSayfa("anasayfa");
								ortakIslemler.sistemeGirisIslemleri(authenticatedUser, Boolean.TRUE, null, null, session);
								logger.info(authenticatedUser.getUsername() + " " + authenticatedUser.getAdSoyad() + " " + (authenticatedUser.getEmail() != null && !authenticatedUser.getEmail().equals(authenticatedUser.getUsername()) ? authenticatedUser.getEmail() + " E-postali" : "")
										+ " kullanıcısı PDKS sistemine login oldu. " + PdksUtil.getCurrentTimeStampStr());
								authenticatedUser.setSessionSQL(session);
							} catch (Exception e) {
								logger.error("PDKS hata in : \n");
								e.printStackTrace();
								logger.error("PDKS hata out : " + e.getMessage());
								logger.debug("Hata : " + e.getMessage());
							}
							if (!test || adres.startsWith("surum")) {
								try {
									authenticatedUser.setLastLogin(new Date());
									pdksEntityController.saveOrUpdate(session, entityManager, authenticatedUser);
									session.flush();
								} catch (Exception e) {
								}
							}
							authenticatedUser.setLogin(Boolean.TRUE);
						}
					}
				} else if (mesajList.isEmpty())
					addMessageAvailableError(credentials.getUsername().trim() + " kullanıcı adı Zaman Yönetimi-PDKS Sistemi'nde kayıtlı değildir!");

			} catch (Exception ex) {
				logger.debug("Hata : " + ex.getMessage());

				try {

					List perList = pdksEntityController.getSQLParamByFieldList(Personel.TABLE_NAME, Personel.COLUMN_NAME_ID, 1L, Personel.class, session);
					logger.info(authenticatedUser.getUsername() + " kullanıcı bilgisi okundu.");

					if (!perList.isEmpty())
						logger.error(perList.size() + " " + PdksUtil.getCurrentTimeStampStr());

				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());
					logger.debug("Hata : " + e.getMessage());
				}
				logger.debug("authenticating " + username);
			}
		}
		if (sonuc == false)
			authenticatedUser = null;
		return sonuc;

	}

	/**
	 * @param userName
	 * @param fieldName
	 * @return
	 */
	private User getKullanici(String userName, String fieldName) {
		User authenticated = null;
		if (userName.indexOf("%") > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("select S.* from " + User.TABLE_NAME + " S " + PdksEntityController.getSelectLOCK());
			// sb.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = S." + User.COLUMN_NAME_PERSONEL);
			// sb.append(" and P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= convert(date,GETDATE()) and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= convert(date,GETDATE())");
			// if (userName.indexOf("%") > 0)
			sb.append(" where S." + fieldName + " like :userName");
			HashMap fields = new HashMap();
			fields.put("userName", userName);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			authenticated = (User) pdksEntityController.getObjectBySQL(sb, fields, User.class);
		} else
			authenticated = (User) pdksEntityController.getSQLParamByFieldObject(User.TABLE_NAME, fieldName, userName, User.class, session);

		if (authenticated != null) {
			Personel personel = authenticated.getPdksPersonel();
			if (authenticated.isDurum() == false || authenticated.getDepartman() == null || personel == null || personel.getDurum().equals(Boolean.FALSE) || personel.isCalisiyor() == false) {
				if (authenticated.isDurum() == false) {
					addMessageAvailableError(authenticated.getUsername() + " kullanıcısı aktif değildir!");
				} else if (authenticated.getDepartman() == null)
					addMessageAvailableError(authenticated.getUsername() + " kullanıcısı departmanı tanımlı değildir!");
				else if (personel != null) {
					if (personel.isCalisiyor() == false)
						addMessageAvailableError(personel.getAdSoyad() + " personel işten ayrılmıştır!");
					else if (personel.getDurum().booleanValue() == false)
						addMessageAvailableError(personel.getAdSoyad() + " personel aktif değildir!");
				}
				authenticated = null;
			}

		}
		return authenticated;
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

	public String getAciklamaIslem() {
		String str = pdksCredentials.isForgetPassword() == false ? "Sisteme Giriş" : "Yeni Şifre Gönder";
		return str;
	}

}
