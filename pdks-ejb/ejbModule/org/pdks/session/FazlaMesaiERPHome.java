package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.pdks.entity.FazlaMesaiERP;
import org.pdks.entity.FazlaMesaiERPDetay;
import org.pdks.enums.MethodAPI;
import org.pdks.enums.MethodAlanAPI;
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
	private List<SelectItem> methodAlanList, methodList;
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
		fillFazlaMesaiERPList();
		return "";

	}

	@Transactional
	public String datayKaydet() {
		fillFazlaMesaiERPDetayList();
		return "";

	}

	/**
	 * @param fmd
	 * @return
	 */
	public boolean asagiKaydirabilir(FazlaMesaiERPDetay fmd) {
		boolean kaydir = false;
		return kaydir;
	}

	/**
	 * @param fmd
	 * @return
	 */
	@Transactional
	public String asagiKaydir(FazlaMesaiERPDetay fmd) {

		return "";
	}

	/**
	 * @param fmd
	 * @return
	 */
	@Transactional
	public String yukariKaydir(FazlaMesaiERPDetay fmd) {

		return "";
	}

	/**
	 * @param fmd
	 * @return
	 */
	public boolean yukariKaydirabilir(FazlaMesaiERPDetay fmd) {
		boolean kaydir = false;
		return kaydir;
	}

	/**
	 * @param detay
	 * @return
	 */
	public String fazlaMesaiERPDetayGuncelle(FazlaMesaiERPDetay detay) {
		if (detay == null) {
			detay = new FazlaMesaiERPDetay();
			detay.setFazlaMesaiERP(seciliFazlaMesaiERP);
		}
		seciliFazlaMesaiERPDetay = detay;
		if (methodAlanList == null)
			methodAlanList = new ArrayList<SelectItem>();
		else
			methodAlanList.clear();
		for (MethodAlanAPI methodAlanAPI : MethodAlanAPI.values()) {
			String key = methodAlanAPI.value();
			String aciklama = FazlaMesaiERPDetay.getAlanAciklama(key);
			methodAlanList.add(new SelectItem(key, aciklama));
		}
		return "";
	}

	/**
	 * @param fazlaMesaiERP
	 * @return
	 */
	public String fazlaMesaiERPGuncelle(FazlaMesaiERP fazlaMesaiERP) {
		if (fazlaMesaiERP == null) {
			String sirketAdi = ortakIslemler.getParameterKey("uygulamaBordro");
			fazlaMesaiERP = new FazlaMesaiERP();
			fazlaMesaiERP.setSirketAdi(sirketAdi);
		}

		if (methodList == null)
			methodList = new ArrayList<SelectItem>();
		else
			methodList.clear();
		for (MethodAPI methodAPI : MethodAPI.values()) {
			String key = methodAPI.value();
			methodList.add(new SelectItem(key, FazlaMesaiERP.getMethodAciklama(key)));
		}
		setSeciliFazlaMesaiERP(fazlaMesaiERP);
		fillFazlaMesaiERPDetayList();

		setInstance(fazlaMesaiERP);

		return "";
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public String fillFazlaMesaiERPDetayList() {
		seciliFazlaMesaiERPDetay = null;
		List<FazlaMesaiERPDetay> list = null;
		if (seciliFazlaMesaiERP.getId() != null) {
			list = pdksEntityController.getSQLParamByFieldList(FazlaMesaiERPDetay.TABLE_NAME, FazlaMesaiERPDetay.COLUMN_NAME_FAZLA_MESAI_ERP, seciliFazlaMesaiERP.getId(), FazlaMesaiERPDetay.class, session);
			if (list.size() > 1)
				list = PdksUtil.sortListByAlanAdi(list, "sira", false);
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
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		fillFazlaMesaiERPList();
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
}
