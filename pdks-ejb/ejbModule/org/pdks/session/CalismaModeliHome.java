package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

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
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliGun;
import org.pdks.entity.CalismaModeliVardiya;
import org.pdks.entity.Departman;
import org.pdks.entity.Sirket;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.security.entity.User;

@Name("calismaModeliHome")
public class CalismaModeliHome extends EntityHome<CalismaModeli> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1146930923797257560L;

	static Logger logger = Logger.getLogger(CalismaModeliHome.class);
	@RequestParameter
	Long calismaModeliId;

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

	public static String sayfaURL = "calismaModeliTanimlama";
	private CalismaModeli calismaModeli;

	private List<CalismaModeli> calismaModeliList;
	private List<VardiyaSablonu> sablonList;
	private List<Sirket> sirketList, pdksSirketList;
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>(), kayitliVardiyaList = new ArrayList<Vardiya>();
	private List<CalismaModeliGun> cmGunList;
	private List<Departman> departmanList;
	private List<SelectItem> haftaTatilGunleri;
	private HashMap<Integer, List<CalismaModeliGun>> cmGunMap;
	private Sirket seciliSirket;
	private CalismaModeliGun cmgPage = new CalismaModeliGun();

	private Boolean pasifGoster = Boolean.FALSE, hareketKaydiVardiyaBul = Boolean.FALSE, saatlikCalismaVar = false, otomatikFazlaCalismaOnaylansinVar = false, izinGoster = false;

	private Session session;

	@Override
	public Object getId() {
		if (calismaModeliId == null) {
			return super.getId();
		} else {
			return calismaModeliId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public String calismaModeliEkle(CalismaModeli xCalismaModeli) {

		if (xCalismaModeli == null) {

			xCalismaModeli = new CalismaModeli();
			if (!saatlikCalismaVar)
				xCalismaModeli.setAylikMaas(Boolean.TRUE);
			if (seciliSirket != null) {
				xCalismaModeli.setSirket(seciliSirket);
				xCalismaModeli.setDepartman(seciliSirket.getDepartman());
			}
		}

		setCalismaModeli(xCalismaModeli);

		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
		if (xCalismaModeli.getId() == null && departmanList.size() > 0)
			xCalismaModeli.setDepartman(departmanList.get(0));

		fillVardiyalar(xCalismaModeli);

		return "";
	}

	private void gunleriSifirla() {
		if (cmGunMap == null)
			cmGunMap = new HashMap<Integer, List<CalismaModeliGun>>();
		else
			cmGunMap.clear();
		if (cmGunList == null)
			cmGunList = new ArrayList<CalismaModeliGun>();
		else
			cmGunList.clear();
	}

	public String calismaModeliKopyala(CalismaModeli xCalismaModeli) {
		CalismaModeli calismaModeliYeni = (CalismaModeli) xCalismaModeli.cloneEmpty();

		calismaModeliYeni.setId(null);
		if (calismaModeliYeni.getAciklama() != null)
			calismaModeliYeni.setAciklama(xCalismaModeli.getAciklama() + " kopya");
		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
		setCalismaModeli(calismaModeliYeni);
		fillVardiyalar(xCalismaModeli);
		return "";

	}

	/**
	 * @param gunTipi
	 * @return
	 */
	public String fillGunList(int gunTipi) {
		cmgPage.setGunTipi(gunTipi);
		List<CalismaModeliGun> list = null;
		if (cmGunMap.containsKey(gunTipi))
			list = cmGunMap.get(gunTipi);
		else {
			TreeMap<String, CalismaModeliGun> map = null;
			if (calismaModeli.getId() != null) {
				HashMap fields = new HashMap();
				fields.put("calismaModeli.id", calismaModeli.getId());
				fields.put("gunTipi", gunTipi);
				fields.put(PdksEntityController.MAP_KEY_MAP, "getKey");
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				map = pdksEntityController.getObjectByInnerObjectMap(fields, CalismaModeliGun.class, false);
			} else
				map = new TreeMap<String, CalismaModeliGun>();
			list = new ArrayList<CalismaModeliGun>();
			Double sure = gunTipi == CalismaModeliGun.GUN_SAAT ? calismaModeli.getHaftaIci() : calismaModeli.getHaftaIciSutIzniSure();
			for (int i = Calendar.MONDAY; i < Calendar.SATURDAY; i++) {
				String key = CalismaModeliGun.getKey(calismaModeli, gunTipi, i);
				if (!map.containsKey(key)) {
					CalismaModeliGun cmg = new CalismaModeliGun(calismaModeli, gunTipi, i);
					cmg.setSure(sure);
					map.put(key, cmg);
				}
				CalismaModeliGun cmg = map.get(key);
				cmg.setGuncellendi(false);
				list.add(cmg);
			}
			cmGunMap.put(gunTipi, list);
		}
		cmGunList = list;
		return "";
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

	/**
	 * @param list
	 * @return
	 */
	private List<Vardiya> vardiyaAyir(List<Vardiya> list) {
		list = PdksUtil.sortObjectStringAlanList(list, "getAdi", null);
		List<Vardiya> ozelList = new ArrayList<Vardiya>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Vardiya vardiya = (Vardiya) iterator.next();
			if (vardiya.getGenel().equals(Boolean.FALSE)) {
				ozelList.add(vardiya);
				iterator.remove();
			}
		}
		if (!ozelList.isEmpty())
			list.addAll(ozelList);
		ozelList = null;
		return list;

	}

	public String fillVardiyalar(CalismaModeli cm) {
		Sirket sirket = cm.getSirket();
		haftaTatilGunleri.clear();
		Calendar cal = Calendar.getInstance();
		haftaTatilGunleri.add(new SelectItem(null, "Sabit Gün Değil"));
		for (int i = 1; i <= 7; i++) {
			cal.set(Calendar.DAY_OF_WEEK, i);
			haftaTatilGunleri.add(new SelectItem(i, PdksUtil.convertToDateString(cal.getTime(), "EEEEE")));
		}
		gunleriSifirla();
		Long departmanId = cm.getDepartman() != null ? cm.getDepartman().getId() : null;

		sablonList = ortakIslemler.getVardiyaSablonuList(sirket, departmanId, session);

		for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
			VardiyaSablonu sablonu = (VardiyaSablonu) iterator.next();
			if (sablonu.getCalismaModeli() != null)
				iterator.remove();

		}
		if (cm.getBagliVardiyaSablonu() != null) {
			boolean ekle = true;
			Long id = cm.getBagliVardiyaSablonu().getId();
			for (VardiyaSablonu sablonu : sablonList) {
				if (sablonu.getId().equals(id)) {
					ekle = false;
					break;
				}
			}
			if (ekle)
				sablonList.add(cm.getBagliVardiyaSablonu());
		}
		Long cmaDepartmanId = cm.getDepartman() != null ? cm.getDepartman().getId() : null;
		HashMap parametreMap = new HashMap();
		vardiyaList = ortakIslemler.getVardiyaList(sirket, cmaDepartmanId, session);
		for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
			Vardiya vardiya = (Vardiya) iterator.next();
			if (vardiya.getKisaAdi().equals("TA") || vardiya.getKisaAdi().equals("TG"))
				logger.debug(vardiya.getId() + " " + vardiya.getKisaAdi());
			if (vardiya.isCalisma() == false)
				iterator.remove();

		}
		if (vardiyaList.size() > 1)
			vardiyaList = vardiyaAyir(vardiyaList);
		if (cm.getId() != null) {
			parametreMap.clear();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "vardiya");
			parametreMap.put("calismaModeli.id", cm.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			kayitliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				Vardiya vardiya = (Vardiya) iterator.next();
				for (Vardiya vardiyaCalisma : kayitliVardiyaList) {
					if (vardiyaCalisma.getId().equals(vardiya.getId())) {
						iterator.remove();
						break;
					}

				}
			}
			if (kayitliVardiyaList.size() > 1)
				kayitliVardiyaList = vardiyaAyir(kayitliVardiyaList);

		} else
			kayitliVardiyaList = new ArrayList<Vardiya>();
		sirketList = ortakIslemler.getDepartmanPDKSSirketList(cm.getDepartman(), session);

		return "";
	}

	public void instanceRefresh() {
		if (calismaModeli.getId() != null)
			session.refresh(calismaModeli);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		pasifGoster = false;
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		pdksSirketList = ortakIslemler.getDepartmanPDKSSirketList(null, session);
		fillCalismaModeliList();
	}

	@Transactional
	public String kaydet() {
		try {
			boolean devam = true;
			if (calismaModeli.getHaftaTatilGun() != null) {
				double saat = calismaModeli.getSaat(calismaModeli.getHaftaTatilGun());
				if (saat != 0) {
					devam = false;
					PdksUtil.addMessageWarn("Hafta tatil günü çalışma saati tanımlıdır!");
				}
			}
			if (devam) {

				if (calismaModeli.getId() != null) {
					calismaModeli.setGuncellemeTarihi(new Date());
					calismaModeli.setGuncelleyenUser(authenticatedUser);
				} else {
					calismaModeli.setOlusturmaTarihi(new Date());
					calismaModeli.setOlusturanUser(authenticatedUser);
				}
				List<CalismaModeliVardiya> kayitliCalismaModeliVardiyaList = null;
				if (calismaModeli.getId() != null && calismaModeli.getGenelVardiya().equals(Boolean.FALSE)) {
					HashMap parametreMap = new HashMap();
					parametreMap.put("calismaModeli.id", calismaModeli.getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					kayitliCalismaModeliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
				} else
					kayitliCalismaModeliVardiyaList = new ArrayList<CalismaModeliVardiya>();
				String haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum");
				if (!haftaTatilDurum.equals("1"))
					calismaModeli.setHaftaTatilMesaiOde(Boolean.FALSE);
				if (calismaModeli.getHaftaTatilMesaiOde().equals(Boolean.FALSE))
					calismaModeli.setGeceHaftaTatilMesaiParcala(Boolean.FALSE);
				pdksEntityController.saveOrUpdate(session, entityManager, calismaModeli);
				if (calismaModeli.getGenelVardiya() || calismaModeli.isOrtakVardiyadir())
					kayitliVardiyaList.clear();
				for (Iterator iterator = kayitliVardiyaList.iterator(); iterator.hasNext();) {
					Vardiya kayitliVardiya = (Vardiya) iterator.next();
					boolean ekle = true;
					for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
						CalismaModeliVardiya cmv = (CalismaModeliVardiya) iterator2.next();
						if (cmv.getVardiya().getId().equals(kayitliVardiya.getId())) {
							ekle = false;
							iterator2.remove();
							break;
						}

					}
					if (ekle) {
						CalismaModeliVardiya cmv = new CalismaModeliVardiya(kayitliVardiya, calismaModeli);
						pdksEntityController.saveOrUpdate(session, entityManager, cmv);
					}
				}
				for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
					CalismaModeliVardiya cmv = (CalismaModeliVardiya) iterator2.next();
					pdksEntityController.deleteObject(session, entityManager, cmv);
				}
				if (cmGunMap != null && !cmGunMap.isEmpty()) {
					for (Integer gunTipi : cmGunMap.keySet()) {
						double sure = gunTipi.equals(CalismaModeliGun.GUN_SAAT) ? calismaModeli.getHaftaIci() : calismaModeli.getHaftaIciSutIzniSure();
						List<CalismaModeliGun> list = cmGunMap.get(gunTipi);
						for (CalismaModeliGun calismaModeliGun : list) {
							if (calismaModeliGun.getSure() == sure) {
								if (calismaModeliGun.getId() != null)
									session.delete(calismaModeliGun);
							} else if (calismaModeliGun.isGuncellendi())
								pdksEntityController.saveOrUpdate(session, entityManager, calismaModeliGun);
						}
					}
				}
				session.flush();
				fillCalismaModeliList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * @param d
	 * @return
	 */
	private boolean veriVar(Double d) {
		boolean v = d != null && d.doubleValue() > 0.0d;
		return v;
	}

	public void fillCalismaModeliList() {
		if (haftaTatilGunleri == null)
			haftaTatilGunleri = new ArrayList<SelectItem>();

		izinGoster = false;
		hareketKaydiVardiyaBul = ortakIslemler.getParameterKey("hareketKaydiVardiyaBul").equals("1");
		saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
		otomatikFazlaCalismaOnaylansinVar = ortakIslemler.getParameterKey("otomatikFazlaCalismaOnaylansin").equals("1");
		calismaModeli = new CalismaModeli();

		calismaModeliList = pdksEntityController.getSQLParamByFieldList(CalismaModeli.TABLE_NAME, pasifGoster == false ? CalismaModeli.COLUMN_NAME_DURUM : null, Boolean.TRUE, CalismaModeli.class, session);
		if (seciliSirket != null) {
			Departman seciliDepartman = seciliSirket.getDepartman();
			for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
				CalismaModeli cmd = (CalismaModeli) iterator.next();
				Departman departman = cmd.getDepartman();
				Sirket sirket = cmd.getSirket();
				boolean sil = false;
				if (departman != null && !seciliDepartman.getId().equals(departman.getId()))
					sil = true;
				if (sirket != null && !seciliSirket.getId().equals(sirket.getId()))
					sil = true;
				if (sil)
					iterator.remove();
			}

		}
		if (!hareketKaydiVardiyaBul || !otomatikFazlaCalismaOnaylansinVar) {
			List<CalismaModeli> pasifList = new ArrayList<CalismaModeli>();
			for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
				CalismaModeli cm = (CalismaModeli) iterator.next();
				if (cm.getDurum().booleanValue() == false) {
					pasifList.add(cm);
					iterator.remove();
				}

			}
			if (authenticatedUser.isAdmin() && !pasifList.isEmpty())
				calismaModeliList.addAll(pasifList);
			pasifList = null;
			for (CalismaModeli cm : calismaModeliList) {
				if (cm.getDurum()) {
					if (!izinGoster)
						izinGoster = veriVar(cm.getIzin()) || veriVar(cm.getCumartesiIzinSaat()) || veriVar(cm.getPazarIzinSaat());
					if (!otomatikFazlaCalismaOnaylansinVar)
						otomatikFazlaCalismaOnaylansinVar = cm.isOtomatikFazlaCalismaOnaylansinmi();
					if (!hareketKaydiVardiyaBul)
						hareketKaydiVardiyaBul = cm.isHareketKaydiVardiyaBulsunmu();
				}

			}
		}
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<CalismaModeli> getCalismaModeliList() {
		return calismaModeliList;
	}

	public void setCalismaModeliList(List<CalismaModeli> calismaModeliList) {
		this.calismaModeliList = calismaModeliList;
	}

	public List<Vardiya> getKayitliVardiyaList() {
		return kayitliVardiyaList;
	}

	public void setKayitliVardiyaList(List<Vardiya> kayitliVardiyaList) {
		this.kayitliVardiyaList = kayitliVardiyaList;
	}

	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	public List<VardiyaSablonu> getSablonList() {
		return sablonList;
	}

	public void setSablonList(List<VardiyaSablonu> sablonList) {
		this.sablonList = sablonList;
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public Boolean getHareketKaydiVardiyaBul() {
		return hareketKaydiVardiyaBul;
	}

	public void setHareketKaydiVardiyaBul(Boolean hareketKaydiVardiyaBul) {
		this.hareketKaydiVardiyaBul = hareketKaydiVardiyaBul;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * @return the saatlikCalismaVar
	 */
	public Boolean getSaatlikCalismaVar() {
		return saatlikCalismaVar;
	}

	/**
	 * @param saatlikCalismaVar
	 *            the saatlikCalismaVar to set
	 */
	public void setSaatlikCalismaVar(Boolean saatlikCalismaVar) {
		this.saatlikCalismaVar = saatlikCalismaVar;
	}

	/**
	 * @return the otomatikFazlaCalismaOnaylansinVar
	 */
	public Boolean getOtomatikFazlaCalismaOnaylansinVar() {
		return otomatikFazlaCalismaOnaylansinVar;
	}

	/**
	 * @param otomatikFazlaCalismaOnaylansinVar
	 *            the otomatikFazlaCalismaOnaylansinVar to set
	 */
	public void setOtomatikFazlaCalismaOnaylansinVar(Boolean otomatikFazlaCalismaOnaylansinVar) {
		this.otomatikFazlaCalismaOnaylansinVar = otomatikFazlaCalismaOnaylansinVar;
	}

	public Boolean getIzinGoster() {
		return izinGoster;
	}

	public void setIzinGoster(Boolean izinGoster) {
		this.izinGoster = izinGoster;
	}

	public List<CalismaModeliGun> getCmGunList() {
		return cmGunList;
	}

	public void setCmGunList(List<CalismaModeliGun> cmGunList) {
		this.cmGunList = cmGunList;
	}

	public HashMap<Integer, List<CalismaModeliGun>> getCmGunMap() {
		return cmGunMap;
	}

	public void setCmGunMap(HashMap<Integer, List<CalismaModeliGun>> cmGunMap) {
		this.cmGunMap = cmGunMap;
	}

	public CalismaModeliGun getCmgPage() {
		return cmgPage;
	}

	public void setCmgPage(CalismaModeliGun cmgPage) {
		this.cmgPage = cmgPage;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		CalismaModeliHome.sayfaURL = sayfaURL;
	}

	public List<SelectItem> getHaftaTatilGunleri() {
		return haftaTatilGunleri;
	}

	public void setHaftaTatilGunleri(List<SelectItem> haftaTatilGunleri) {
		this.haftaTatilGunleri = haftaTatilGunleri;
	}

	public Boolean getPasifGoster() {
		return pasifGoster;
	}

	public void setPasifGoster(Boolean pasifGoster) {
		this.pasifGoster = pasifGoster;
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

}
