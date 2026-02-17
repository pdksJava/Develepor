package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.pdks.entity.ERPSistem;
import org.pdks.entity.FazlaMesaiERP;
import org.pdks.entity.FazlaMesaiERPDetay;
import org.pdks.enums.MethodAPI;
import org.pdks.enums.MethodAlanAPI;
import org.pdks.enums.VeriTipiAPI;
import org.pdks.security.entity.User;

@Name("fazlaMesaiERPHome")
public class FazlaMesaiERPHome extends EntityHome<FazlaMesaiERP> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8254204570949087604L;
	/**
	 * 
	 */
	static Logger logger = Logger.getLogger(FazlaMesaiERPHome.class);
	@RequestParameter
	Long fazlaMesaiERPId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;

	public static String sayfaURL = "fazlaMesaiERPTanimlama";
	private List<SelectItem> methodAlanList, methodList, veriTipiList;
	private List<FazlaMesaiERP> fazlaMesaiERPList = new ArrayList<FazlaMesaiERP>();
	private List<FazlaMesaiERPDetay> fazlaMesaiERPDetayList;
	private FazlaMesaiERP seciliFazlaMesaiERP;
	private FazlaMesaiERPDetay seciliFazlaMesaiERPDetay;
	private boolean veriVar = false;
	private Session session;

	@Override
	public Object getId() {
		if (fazlaMesaiERPId == null) {
			return super.getId();
		} else {
			return fazlaMesaiERPId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String baslikKaydet() {
		try {
			pdksEntityController.saveOrUpdate(session, entityManager, seciliFazlaMesaiERP);
			session.flush();
			fillFazlaMesaiERPList();
			seciliFazlaMesaiERP = null;
		} catch (Exception e) {
		}

		return "";

	}

	public Boolean getFazlaMesaiERPUrlZorunlu() {
		boolean zorunlu = false;
		if (seciliFazlaMesaiERP != null && seciliFazlaMesaiERP.getMethodAdi() != null) {
			MethodAPI methodAPI = seciliFazlaMesaiERP.getMethodAPI();
			if (methodAPI == null)
				methodAPI = MethodAPI.fromValue(seciliFazlaMesaiERP.getMethodAdi());
			if (methodAPI != null)
				zorunlu = methodAPI.equals(MethodAPI.GET) == false;

		}
		return zorunlu;
	}

	@Transactional
	public String detaySil() {
		try {
			session.delete(seciliFazlaMesaiERPDetay);
			session.flush();
			pdksEntityController.savePrepareTableID(true, FazlaMesaiERPDetay.class, entityManager, session);

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		fillFazlaMesaiERPDetayList();
		fillMethodAlanList();
		seciliFazlaMesaiERPDetay = null;
		return "";
	}

	@Transactional
	public String detayKaydet() {
		boolean devam = true;
		if (seciliFazlaMesaiERPDetay.getId() != null) {
			for (FazlaMesaiERPDetay dt : fazlaMesaiERPDetayList) {
				if (dt.getId().equals(seciliFazlaMesaiERPDetay.getId()) == false && seciliFazlaMesaiERPDetay.getAlanTipi().equals(dt.getAlanTipi())) {
					PdksUtil.addMessageWarn(getAlanAciklama(seciliFazlaMesaiERPDetay.getMethodAlanAPI()) + " alan tipi kayıtlıdır!");
					devam = false;
					break;
				}
			}
		}
		if (devam) {
			try {
				boolean disable = getSeciliAlanAdiDisabled();
				if (disable)
					seciliFazlaMesaiERPDetay.setAlanAdi("");
				if (seciliFazlaMesaiERPDetay.isGuvenlikAlani()) {
					seciliFazlaMesaiERPDetay.setBaslikAlan(false);
				} else
					seciliFazlaMesaiERPDetay.setAlanDeger("");
				pdksEntityController.saveOrUpdate(session, entityManager, seciliFazlaMesaiERPDetay);
				session.flush();
				fillFazlaMesaiERPDetayList();
				fillMethodAlanList();
				seciliFazlaMesaiERPDetay = null;
			} catch (Exception e) {
			}
		}

		return "";

	}

	/**
	 * @param fmd
	 * @return
	 */
	public boolean asagiKaydirabilir(FazlaMesaiERPDetay fmd) {
		boolean kaydir = false;
		MethodAlanAPI alanAPI = fmd.getMethodAlanAPI();
		if (alanAPI != null) {
			if (fmd.isGuvenlikAlani()) {
				kaydir = fmd.getIndis().intValue() == 0;
			} else
				kaydir = fmd.getIndis().intValue() + 1 < fazlaMesaiERPDetayList.size();

		}
		return kaydir;
	}

	/**
	 * @param fmd
	 * @return
	 */
	public boolean yukariKaydirabilir(FazlaMesaiERPDetay fmd) {
		boolean kaydir = false;
		MethodAlanAPI alanAPI = fmd.getMethodAlanAPI();
		if (alanAPI != null) {
			if (fmd.isGuvenlikAlani()) {
				kaydir = fmd.getIndis().intValue() == 1;
			} else if (fmd.getIndis().intValue() > 0) {
				int index = fmd.getIndis().intValue() - 1;
				FazlaMesaiERPDetay detay1 = fazlaMesaiERPDetayList.get(index);
				alanAPI = detay1.getMethodAlanAPI();
				kaydir = alanAPI != null && (alanAPI.equals(MethodAlanAPI.USER_NAME) || alanAPI.equals(MethodAlanAPI.PASSWORD)) == false;
			}

		}
		return kaydir;
	}

	/**
	 * @param fmd
	 * @return
	 */
	@Transactional
	public String asagiKaydir(FazlaMesaiERPDetay fmd) {
		int index = fmd.getIndis().intValue() + 1;
		FazlaMesaiERPDetay fmd1 = fazlaMesaiERPDetayList.get(index);
		int sira1 = fmd.getSira(), sira2 = fmd1.getSira();
		if (sira2 > sira1) {
			fmd1.setSira(sira1);
			fmd.setSira(sira2);
			pdksEntityController.saveOrUpdate(session, entityManager, fmd);
			pdksEntityController.saveOrUpdate(session, entityManager, fmd1);
			session.flush();
			fillFazlaMesaiERPDetayList();
		}
		return "";
	}

	/**
	 * @param fmd
	 * @return
	 */
	@Transactional
	public String yukariKaydir(FazlaMesaiERPDetay fmd) {
		int index = fmd.getIndis().intValue() - 1;
		FazlaMesaiERPDetay fmd0 = fazlaMesaiERPDetayList.get(index);
		int sira2 = fmd.getSira(), sira1 = fmd0.getSira();
		if (sira2 > sira1) {
			fmd0.setSira(sira2);
			fmd.setSira(sira1);
			pdksEntityController.saveOrUpdate(session, entityManager, fmd);
			pdksEntityController.saveOrUpdate(session, entityManager, fmd0);
			session.flush();
			fillFazlaMesaiERPDetayList();
		}
		return "";
	}

	/**
	 * yukariLink
	 * 
	 * @param detay
	 * @return
	 */
	public String fazlaMesaiERPDetayGuncelle(FazlaMesaiERPDetay detay) {
		fillMethodAlanList();

		if (detay == null) {
			detay = new FazlaMesaiERPDetay();
			detay.setFazlaMesaiERP(seciliFazlaMesaiERP);
			detay.setSira(fazlaMesaiERPDetayList.size() + 1);
			for (FazlaMesaiERPDetay dt : fazlaMesaiERPDetayList) {
				for (Iterator iterator = methodAlanList.iterator(); iterator.hasNext();) {
					SelectItem st = (SelectItem) iterator.next();
					if (st.getValue().equals(dt.getAlanTipi())) {
						iterator.remove();
						break;
					}

				}
			}
		}
		seciliFazlaMesaiERPDetay = detay;

		return "";
	}

	private void fillMethodAlanList() {
		if (methodAlanList == null)
			methodAlanList = new ArrayList<SelectItem>();
		else
			methodAlanList.clear();
		for (MethodAlanAPI methodAlanAPI : MethodAlanAPI.values()) {
			String key = methodAlanAPI.value();
			String aciklama = getAlanAciklama(methodAlanAPI);
			methodAlanList.add(new SelectItem(key, aciklama));
		}
	}

	/**
	 * @param methodAlanAPI
	 * @return
	 */
	public String getAlanAciklama(MethodAlanAPI methodAlanAPI) {
		String aciklama = "";
		if (methodAlanAPI != null) {
			String key = methodAlanAPI.value();
			if (methodAlanAPI.equals(MethodAlanAPI.SIRKET))
				aciklama = ortakIslemler.sirketAciklama() + " No";
			else if (methodAlanAPI.equals(MethodAlanAPI.TESIS))
				aciklama = ortakIslemler.tesisAciklama() + " No";
			else if (methodAlanAPI.equals(MethodAlanAPI.PERSONEL))
				aciklama = ortakIslemler.personelNoAciklama();
			else
				aciklama = MethodAlanAPI.getAlanAciklama(key);
		}
		return aciklama;
	}

	/**
	 * @param fazlaMesaiERP
	 * @return
	 */
	public String fazlaMesaiERPGuncelle(FazlaMesaiERP fazlaMesaiERP) {
		if (fazlaMesaiERP == null) {
			String sirketAdi = ortakIslemler.getParameterKey("uygulamaBordro");
			ERPSistem sistem = (ERPSistem) pdksEntityController.getSQLParamByFieldObject(ERPSistem.TABLE_NAME, ERPSistem.COLUMN_NAME_ERP_SIRKET, sirketAdi, ERPSistem.class, session);
			fazlaMesaiERP = new FazlaMesaiERP();
			fazlaMesaiERP.setErpSistem(sistem);
			if (sistem == null)
				fazlaMesaiERP.setSirketAdi(sirketAdi);
		}

		if (methodList == null)
			methodList = new ArrayList<SelectItem>();
		else
			methodList.clear();

		if (veriTipiList == null)
			veriTipiList = new ArrayList<SelectItem>();
		else
			veriTipiList.clear();

		for (MethodAPI methodAPI : MethodAPI.values()) {
			String key = methodAPI.value();
			methodList.add(new SelectItem(key, MethodAPI.getMethodAciklama(key)));
		}
		for (VeriTipiAPI veriTipiAPI : VeriTipiAPI.values()) {
			Integer key = veriTipiAPI.value();
			String value = VeriTipiAPI.getServisTipiAciklama(key);
			veriTipiList.add(new SelectItem(key, value));
		}
		setSeciliFazlaMesaiERP(fazlaMesaiERP);
		fillMethodAlanList();
		fillFazlaMesaiERPDetayList();

		setInstance(fazlaMesaiERP);

		return "";
	}

	/**
	 * @return
	 */
	@Transactional
	public String fillFazlaMesaiERPDetayList() {
		seciliFazlaMesaiERPDetay = null;
		List<FazlaMesaiERPDetay> list = null;
		if (seciliFazlaMesaiERP.getId() != null) {
			list = pdksEntityController.getSQLParamByFieldList(FazlaMesaiERPDetay.TABLE_NAME, FazlaMesaiERPDetay.COLUMN_NAME_FAZLA_MESAI_ERP, seciliFazlaMesaiERP.getId(), FazlaMesaiERPDetay.class, session);
			if (list.size() > 1)
				list = PdksUtil.sortListByAlanAdi(list, "sira", false);
			int indis = 0;
			boolean flush = false;
			for (FazlaMesaiERPDetay fazlaMesaiERPDetay : list) {
				fazlaMesaiERPDetay.setIndis(indis++);
				if (fazlaMesaiERPDetay.getSira().intValue() != indis) {
					try {
						fazlaMesaiERPDetay.setSira(indis);
						pdksEntityController.saveOrUpdate(session, entityManager, fazlaMesaiERPDetay);
						flush = true;
					} catch (Exception e) {

					}

				}
			}
			if (flush)
				try {
					session.flush();
				} catch (Exception e) {

				}

		} else
			list = new ArrayList<FazlaMesaiERPDetay>();
		setFazlaMesaiERPDetayList(list);
		return "";
	}

	/**
	 * @return
	 */
	public String fillFazlaMesaiERPList() {
		String uygulamaBordro = ortakIslemler.getParameterKey("uygulamaBordro");
		List<FazlaMesaiERP> list = pdksEntityController.getSQLTableList(FazlaMesaiERP.TABLE_NAME, FazlaMesaiERP.class, session);
		veriVar = PdksUtil.hasStringValue(uygulamaBordro) == false;
		seciliFazlaMesaiERP = null;
		seciliFazlaMesaiERPDetay = null;
		for (FazlaMesaiERP fazlaMesaiERP : list) {
			if (fazlaMesaiERP.getSirketAdi().equals(uygulamaBordro))
				veriVar = true;

		}
		if (veriVar == false) {
			seciliFazlaMesaiERP = new FazlaMesaiERP();
			seciliFazlaMesaiERP.setSirketAdi(uygulamaBordro);
			fazlaMesaiERPGuncelle(seciliFazlaMesaiERP);
		}
		setFazlaMesaiERPList(list);
		return "";
	}

	public void instanceRefresh() {
		if (seciliFazlaMesaiERPDetay != null && seciliFazlaMesaiERPDetay.getId() != null)
			session.refresh(seciliFazlaMesaiERPDetay);

	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		fillFazlaMesaiERPList();
	}

	/**
	 * @return
	 */
	public Boolean getSeciliAlanAdiDisabled() {
		Boolean disabled = false;
		if (seciliFazlaMesaiERPDetay != null) {
			MethodAlanAPI alan = seciliFazlaMesaiERPDetay.getMethodAlanAPI();
			// boolean alanDolu = PdksUtil.hasStringValue(seciliFazlaMesaiERPDetay.getAlanAdi());
			if (alan == null)
				alan = MethodAlanAPI.fromValue(seciliFazlaMesaiERPDetay.getAlanTipi());
			if (alan != null) {
				if (alan.equals(MethodAlanAPI.UOM))
					disabled = PdksUtil.hasStringValue(seciliFazlaMesaiERP.getUomAlanAdi());
				else if (alan.equals(MethodAlanAPI.RT))
					disabled = PdksUtil.hasStringValue(seciliFazlaMesaiERP.getRtAlanAdi());
				else if (alan.equals(MethodAlanAPI.HT))
					disabled = PdksUtil.hasStringValue(seciliFazlaMesaiERP.getHtAlanAdi());
			}
		}
		return disabled;
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
		FazlaMesaiERPHome.sayfaURL = sayfaURL;
	}

	public List<SelectItem> getMethodAlanList() {
		return methodAlanList;
	}

	public void setMethodAlanList(List<SelectItem> methodAlanList) {
		this.methodAlanList = methodAlanList;
	}

	public List<SelectItem> getMethodList() {
		return methodList;
	}

	public void setMethodList(List<SelectItem> methodList) {
		this.methodList = methodList;
	}

	public List<FazlaMesaiERP> getFazlaMesaiERPList() {
		return fazlaMesaiERPList;
	}

	public void setFazlaMesaiERPList(List<FazlaMesaiERP> fazlaMesaiERPList) {
		this.fazlaMesaiERPList = fazlaMesaiERPList;
	}

	public List<FazlaMesaiERPDetay> getFazlaMesaiERPDetayList() {
		return fazlaMesaiERPDetayList;
	}

	public void setFazlaMesaiERPDetayList(List<FazlaMesaiERPDetay> fazlaMesaiERPDetayList) {
		this.fazlaMesaiERPDetayList = fazlaMesaiERPDetayList;
	}

	public FazlaMesaiERP getSeciliFazlaMesaiERP() {
		return seciliFazlaMesaiERP;
	}

	public void setSeciliFazlaMesaiERP(FazlaMesaiERP seciliFazlaMesaiERP) {
		this.seciliFazlaMesaiERP = seciliFazlaMesaiERP;
	}

	public FazlaMesaiERPDetay getSeciliFazlaMesaiERPDetay() {
		return seciliFazlaMesaiERPDetay;
	}

	public void setSeciliFazlaMesaiERPDetay(FazlaMesaiERPDetay seciliFazlaMesaiERPDetay) {
		this.seciliFazlaMesaiERPDetay = seciliFazlaMesaiERPDetay;
	}

	public boolean isVeriVar() {
		return veriVar;
	}

	public void setVeriVar(boolean veriVar) {
		this.veriVar = veriVar;
	}

	public List<SelectItem> getVeriTipiList() {
		return veriTipiList;
	}

	public void setVeriTipiList(List<SelectItem> veriTipiList) {
		this.veriTipiList = veriTipiList;
	}
}
