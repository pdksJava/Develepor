package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.Departman;
import org.pdks.entity.Sirket;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.security.entity.User;

@Name("vardiyaSablonuHome")
public class VardiyaSablonuHome extends EntityHome<VardiyaSablonu> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6175559101056709773L;
	static Logger logger = Logger.getLogger(VardiyaSablonuHome.class);
	@RequestParameter
	Long pdksVardiyaSablonId;

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false)
	FacesMessages facesMessages;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	public static String sayfaURL = "vardiyaSablonTanimlama";
	private List<VardiyaSablonu> vardiyaSablonList = new ArrayList<VardiyaSablonu>();
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>(), vardiyaCalisanList = new ArrayList<Vardiya>(), arifeVardiyaList = new ArrayList<Vardiya>();
	private boolean vardiyaVar = Boolean.FALSE;
	private Vardiya lastVardiya;
	private List<Sirket> sirketList, pdksSirketList;
	private List<Departman> departmanList = new ArrayList<Departman>();
	private List<CalismaModeli> modelList;
	private Sirket seciliSirket;
	private Session session;

	@Override
	public Object getId() {
		if (pdksVardiyaSablonId == null) {
			return super.getId();
		} else {
			return pdksVardiyaSablonId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void vardiyaDegistir(Vardiya pdksVardiya) {
		setLastVardiya(pdksVardiya);
	}

	/**
	 * @param sablonu
	 */
	public void kayitGuncelle(VardiyaSablonu sablonu) {
		if (sablonu == null) {
			fillBagliOlduguDepartmanTanimList();
			sablonu = new VardiyaSablonu();
			if (seciliSirket != null) {
				sablonu.setSirket(seciliSirket);
				sablonu.setDepartman(seciliSirket.getDepartman());
			} else if (departmanList.size() == 1)
				sablonu.setDepartman(departmanList.get(0));
		}
		setInstance(sablonu);
		fillPdksVardiyaList();
		sirketList = ortakIslemler.getDepartmanPDKSSirketList(sablonu.getDepartman(), session);
		setVardiyaVar(Boolean.TRUE);

	}

	public void fillBagliOlduguDepartmanTanimList() {

		List tanimList = null;

		try {
			tanimList = ortakIslemler.fillDepartmanTanimList(session);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		} finally {

		}

		setDepartmanList(tanimList);
	}

	@Override
	@Transactional
	public String update() {
		String cikis = "";
		VardiyaSablonu pdksVardiyaSablon = getInstance();

		boolean hata = Boolean.FALSE;
		try {
			if (pdksVardiyaSablon.getDurum()) {
				pdksVardiyaSablon.vardiyaBul();
				int vardiyaGunAdet = 0, haftaTatilGunAdet = 0, offGunAdet = 0;
				for (Iterator<Vardiya> iterator = pdksVardiyaSablon.getVardiyaList().iterator(); iterator.hasNext();) {
					Vardiya pdksVardiya = iterator.next();
					if (pdksVardiya.isCalisma() || pdksVardiya.isIzin())
						++vardiyaGunAdet;
					else if (pdksVardiya.isHaftaTatil())
						++haftaTatilGunAdet;
					else if (PdksUtil.getPlanOffAdet() > 0 && pdksVardiya.isOffGun())
						++offGunAdet;
				}
				if (offGunAdet > 0 && offGunAdet > PdksUtil.getPlanOffAdet()) {
					PdksUtil.addMessageWarn("Off gün sayısı en fazla " + PdksUtil.getPlanOffAdet() + " adet seçiniz");
					hata = Boolean.TRUE;
				}
				if (vardiyaGunAdet != pdksVardiyaSablon.getCalismaGunSayisi()) {
					PdksUtil.addMessageWarn("Çalışma gün sayısı " + pdksVardiyaSablon.getCalismaGunSayisi() + " adet seçiniz");
					hata = Boolean.TRUE;
				}
				if (haftaTatilGunAdet != 1) {
					PdksUtil.addMessageWarn("Hafta tatili sayısı toplam 1 gün seçiniz");
					hata = Boolean.TRUE;
				}
			}
			if (!hata) {
				cikis = "persisted";
				if (pdksVardiyaSablon.getId() == null)
					pdksVardiyaSablon.setOlusturanUser(authenticatedUser);
				else {
					pdksVardiyaSablon.setGuncelleyenUser(authenticatedUser);
					pdksVardiyaSablon.setGuncellemeTarihi(new Date());
				}
				if (pdksVardiyaSablon.getId() == null && !authenticatedUser.isAdmin())
					pdksVardiyaSablon.setDepartman(authenticatedUser.getDepartman());
				if (pdksVardiyaSablon.getSirket() != null)
					pdksVardiyaSablon.setDepartman(pdksVardiyaSablon.getSirket().getDepartman());
				pdksEntityController.saveOrUpdate(session, entityManager, pdksVardiyaSablon);
				session.flush();
				fillPdksVardiyaSablonList();
			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		return cikis;

	}

	public void fillPdksVardiyaSablonList() {
		List<VardiyaSablonu> sablonList = new ArrayList<VardiyaSablonu>();
		try {
			HashMap parametreMap = new HashMap();
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			sablonList = pdksEntityController.getObjectByInnerObjectList(parametreMap, VardiyaSablonu.class);
			if (!authenticatedUser.isAdmin()) {
				for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
					VardiyaSablonu pdksVardiyaSablonu = (VardiyaSablonu) iterator.next();
					if (pdksVardiyaSablonu.getDepartman() != null && !authenticatedUser.getDepartman().getId().equals(pdksVardiyaSablonu.getDepartman().getId()))
						iterator.remove();
				}
			}
			vardiyaSablonList.clear();

			if (seciliSirket != null) {
				Departman seciliDepartman = seciliSirket.getDepartman();
				for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
					VardiyaSablonu vardiyaSablonu = (VardiyaSablonu) iterator.next();
					Departman departman = vardiyaSablonu.getDepartman();
					Sirket sirket = vardiyaSablonu.getSirket();
					boolean sil = false;
					if (departman != null && !seciliDepartman.getId().equals(departman.getId()))
						sil = true;
					if (sirket != null && !seciliSirket.getId().equals(sirket.getId()))
						sil = true;
					if (sil)
						iterator.remove();
				}

			}
			if (!sablonList.isEmpty()) {
				sablonList = PdksUtil.sortListByAlanAdi(sablonList, "sonIslemTarihi", Boolean.TRUE);
				for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
					VardiyaSablonu pdksVardiyaSablonu = (VardiyaSablonu) iterator.next();
					if (pdksVardiyaSablonu.getDurum()) {

						vardiyaSablonList.add(pdksVardiyaSablonu);
						iterator.remove();
					}
				}
				if (!sablonList.isEmpty())
					vardiyaSablonList.addAll(sablonList);
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillPdksVardiyaSablonList Hata : " + e.getMessage());
		}

	}

	private void fillCalismaModelList(VardiyaSablonu pdksVardiyaSablonu) {
		List<CalismaModeli> list = null;
		Long pdksDepartmanId = pdksVardiyaSablonu.getDepartman() != null ? pdksVardiyaSablonu.getDepartman().getId() : null;
		try {

			list = ortakIslemler.getCalismaModeliList(pdksVardiyaSablonu.getSirket(), pdksDepartmanId, false, session);
			if (list.size() > 1)
				list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);

			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				CalismaModeli calismaModeli = (CalismaModeli) iterator.next();
				if (calismaModeli.getBagliVardiyaSablonu() != null) {
					iterator.remove();
					continue;
				}

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillPdksVardiyaList Hata : " + e.getMessage());
		}
		setModelList(list);

	}

	public void fillPdksVardiyaList() {
		VardiyaSablonu pdksVardiyaSablonu = getInstance();
		fillCalismaModelList(pdksVardiyaSablonu);
		List<Vardiya> pdksList = new ArrayList<Vardiya>();
		Long depLong = pdksVardiyaSablonu.getDepartman() != null ? pdksVardiyaSablonu.getDepartman().getId() : null;
		Sirket sirket = pdksVardiyaSablonu.getSirket();
		boolean durum = Boolean.FALSE;
		try {
			pdksList = ortakIslemler.getVardiyaList(sirket, depLong, session);

			if (pdksList.size() > 1)
				pdksList = PdksUtil.sortListByAlanAdi(pdksList, "vardiyaNumeric", false);
			if (arifeVardiyaList == null)
				arifeVardiyaList = new ArrayList<Vardiya>();
			else
				arifeVardiyaList.clear();
			if (vardiyaCalisanList == null)
				vardiyaCalisanList = new ArrayList<Vardiya>();
			else
				vardiyaCalisanList.clear();

			for (Iterator iterator = pdksList.iterator(); iterator.hasNext();) {
				Vardiya pdksVardiya = (Vardiya) iterator.next();
				if (pdksVardiyaSablonu.getDepartman() != null && pdksVardiya.getDepartman() != null && !pdksVardiya.getDepartman().getId().equals(pdksVardiyaSablonu.getDepartman().getId())) {
					iterator.remove();
					continue;
				}
				if (pdksVardiya.isCalisma()) {
					if (pdksVardiya.getCalismaSaati() != pdksVardiyaSablonu.getToplamSaat() || pdksVardiya.getCalismaGun() != pdksVardiyaSablonu.getCalismaGunSayisi())
						iterator.remove();
					else if (pdksVardiya.getDurum() && pdksVardiya.getNetCalismaSuresi() <= 4.5d)
						arifeVardiyaList.add(pdksVardiya);
				}

			}
			if (!pdksList.isEmpty())
				vardiyaCalisanList.addAll(pdksList);

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.error("fillPdksVardiyaList Hata : " + e.getMessage());
		}
		setVardiyaList(pdksList);
		setVardiyaVar(durum);
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		pdksSirketList = ortakIslemler.getDepartmanPDKSSirketList(null, session);
		fillPdksVardiyaSablonList();
		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
	}

	public void sablonOlustur() {

	}

	public boolean isVardiyaVar() {
		return vardiyaVar;
	}

	public void setVardiyaVar(boolean vardiyaVar) {
		this.vardiyaVar = vardiyaVar;
	}

	public Vardiya getLastVardiya() {
		return lastVardiya;
	}

	public void setLastVardiya(Vardiya lastVardiya) {
		this.lastVardiya = lastVardiya;
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<VardiyaSablonu> getVardiyaSablonList() {
		return vardiyaSablonList;
	}

	public void setVardiyaSablonList(List<VardiyaSablonu> vardiyaSablonList) {
		this.vardiyaSablonList = vardiyaSablonList;
	}

	public List<CalismaModeli> getModelList() {
		return modelList;
	}

	public void setModelList(List<CalismaModeli> modelList) {
		this.modelList = modelList;
	}

	public List<Vardiya> getVardiyaCalisanList() {
		return vardiyaCalisanList;
	}

	public void setVardiyaCalisanList(List<Vardiya> vardiyaCalisanList) {
		this.vardiyaCalisanList = vardiyaCalisanList;
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
		VardiyaSablonuHome.sayfaURL = sayfaURL;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public List<Sirket> getPdksSirketList() {
		return pdksSirketList;
	}

	public void setPdksSirketList(List<Sirket> pdksSirketList) {
		this.pdksSirketList = pdksSirketList;
	}

	public Sirket getSeciliSirket() {
		return seciliSirket;
	}

	public void setSeciliSirket(Sirket seciliSirket) {
		this.seciliSirket = seciliSirket;
	}

	public List<Vardiya> getArifeVardiyaList() {
		return arifeVardiyaList;
	}

	public void setArifeVardiyaList(List<Vardiya> arifeVardiyaList) {
		this.arifeVardiyaList = arifeVardiyaList;
	}
}
