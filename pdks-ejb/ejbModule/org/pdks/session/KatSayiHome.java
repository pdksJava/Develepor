package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.KatSayi;
import org.pdks.entity.Vardiya;
import org.pdks.enums.PuantajKatSayiTipi;
import org.pdks.security.entity.User;

@Name("katSayiHome")
public class KatSayiHome extends EntityHome<KatSayi> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5255379761639851498L;
	static Logger logger = Logger.getLogger(KatSayiHome.class);
	@RequestParameter
	Long katSayiId;

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
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	public static String sayfaURL = "katSayiTanimlama";

	private List<KatSayi> katSayiList;
	private List<Vardiya> vardiyaList;

	private KatSayi seciliKatSayi;

	private AramaSecenekleri as = new AramaSecenekleri();

	private List<SelectItem> puantajKatSayiTipiList;

	private Boolean sirketGoster = Boolean.FALSE, tesisGoster = Boolean.FALSE, vardiyaGetir = Boolean.FALSE, vardiyaGoster = Boolean.FALSE, pasifGoster = Boolean.FALSE;

	private Session session;

	@Override
	public Object getId() {
		if (katSayiId == null) {
			return super.getId();
		} else {
			return katSayiId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	public void instanceRefresh() {
		if (seciliKatSayi.getId() != null)
			session.refresh(seciliKatSayi);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		pasifGoster = false;
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		fillKatSayiList();
	}

	/**
	 * @param xKatSayi
	 * @return
	 */
	public String katSayiEkle(KatSayi xKatSayi) {
		as.setLoginUser(authenticatedUser);
		vardiyaGetir = Boolean.FALSE;
		puantajKatSayiTipiList.clear();
		if (xKatSayi == null) {
			xKatSayi = new KatSayi();
			for (PuantajKatSayiTipi c : PuantajKatSayiTipi.values()) {
				xKatSayi.setTipi(c);
				puantajKatSayiTipiList.add(new SelectItem(c.value(), xKatSayi.getTipAciklama()));
			}
			xKatSayi.setTipi(null);
			xKatSayi.setBasTarih(PdksUtil.getDate(new Date()));
			xKatSayi.setBitTarih(PdksUtil.convertToJavaDate("29991231", "yyyyMMdd"));
			xKatSayi.setDurum(Boolean.TRUE);

		}
		as.setSirket(xKatSayi.getSirket());
		as.setSirketId(xKatSayi.getSirketId());
		as.setTesisId(xKatSayi.getTesisId());
		seciliKatSayi = xKatSayi;
		sirketDoldur();

		return "";
	}

	public String sirketDoldur() {
		ortakIslemler.setAramaSecenekSirketVeTesisData(as, seciliKatSayi.getBasTarih(), seciliKatSayi.getBitTarih(), false, session);
		if (vardiyaGetir)
			fillVardiyalar();
		return "";
	}

	/**
	 * @return
	 */
	public String tesisDoldur() {
		if (as.getSirketId() != null)
			ortakIslemler.setAramaSecenekTesisData(as, seciliKatSayi.getBasTarih(), seciliKatSayi.getBitTarih(), false, session);
		else {
			as.setTesisId(null);
			as.setSirket(null);
		}
		if (vardiyaGetir)
			fillVardiyalar();
		return "";

	}

	/**
	 * @return
	 */
	public String fillVardiyalar() {
		if (vardiyaGetir) {
			vardiyaList = pdksEntityController.getSQLParamByFieldList(Vardiya.TABLE_NAME, Vardiya.COLUMN_NAME_DURUM, Boolean.TRUE, Vardiya.class, session);
			Long sirketId = as.getSirketId(), tesisId = as.getTesisId(), departmanId = as.getSirket() != null ? as.getSirket().getDepartman().getId() : null;
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				Vardiya vardiya = (Vardiya) iterator.next();
				if (vardiya.isCalisma() == false || vardiya.isIzinVardiya() || vardiya.getGenel().booleanValue() == false)
					iterator.remove();
				else if (departmanId != null && vardiya.getDepartmanId() != null && departmanId.equals(vardiya.getDepartmanId()) == false)
					iterator.remove();
				else if (sirketId != null && vardiya.getSirketId() != null && sirketId.equals(vardiya.getSirketId()) == false)
					iterator.remove();
				else if (tesisId != null && vardiya.getTesisId() != null && tesisId.equals(vardiya.getTesisId()) == false)
					iterator.remove();
			}
			if (vardiyaList.size() > 1)
				vardiyaList = PdksUtil.sortObjectStringAlanList(vardiyaList, "getAdi", null);
		} else {
			if (vardiyaList != null)
				vardiyaList.clear();
			else
				vardiyaList = new ArrayList<Vardiya>();

		}
		return "";
	}

	@Transactional
	public String kaydet() {
		try {
			if (seciliKatSayi.getId() == null) {
				seciliKatSayi.setSirketId(as.getSirketId());
				seciliKatSayi.setTesisId(as.getTesisId());
				if (vardiyaGetir.booleanValue() == false)
					seciliKatSayi.setVardiya(null);
			}

			pdksEntityController.saveOrUpdate(session, entityManager, seciliKatSayi);

			session.flush();

			fillKatSayiList();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public void fillKatSayiList() {
		sirketGoster = Boolean.FALSE;
		tesisGoster = Boolean.FALSE;
		vardiyaGoster = Boolean.FALSE;
		if (puantajKatSayiTipiList != null)
			puantajKatSayiTipiList.clear();
		else
			puantajKatSayiTipiList = new ArrayList<SelectItem>();
		katSayiList = pdksEntityController.getSQLParamByFieldList(KatSayi.TABLE_NAME, pasifGoster == false ? KatSayi.COLUMN_NAME_DURUM : null, Boolean.TRUE, KatSayi.class, session);
		if (katSayiList.size() > 1 || pasifGoster == false) {
			katSayiList = PdksUtil.sortListByAlanAdi(katSayiList, "basTarih", true);
			TreeMap<Integer, List<KatSayi>> map1 = new TreeMap<Integer, List<KatSayi>>();
			Date bugun = PdksUtil.getDate(new Date());
			for (KatSayi ks : katSayiList) {
				if (pasifGoster == false && bugun.before(ks.getBitTarih()))
					continue;
				List<KatSayi> list = map1.containsKey(ks.getTipNo()) ? map1.get(ks.getTipNo()) : new ArrayList<KatSayi>();
				if (list.isEmpty())
					map1.put(ks.getTipNo(), list);
				list.add(ks);
			}
			katSayiList.clear();
			for (Integer key : map1.keySet()) {
				List<KatSayi> list = map1.get(key);
				katSayiList.addAll(list);
				list = null;
			}
			map1 = null;

		}
		for (KatSayi ks : katSayiList) {
			if (sirketGoster == false)
				sirketGoster = ks.getSirketId() != null;
			if (tesisGoster == false)
				tesisGoster = ks.getTesisId() != null;
			if (vardiyaGoster == false)
				vardiyaGoster = ks.getVardiya() != null;
		}
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
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
		KatSayiHome.sayfaURL = sayfaURL;
	}

	public Boolean getPasifGoster() {
		return pasifGoster;
	}

	public void setPasifGoster(Boolean pasifGoster) {
		this.pasifGoster = pasifGoster;
	}

	public Boolean getSirketGoster() {
		return sirketGoster;
	}

	public void setSirketGoster(Boolean sirketGoster) {
		this.sirketGoster = sirketGoster;
	}

	public Boolean getTesisGoster() {
		return tesisGoster;
	}

	public void setTesisGoster(Boolean tesisGoster) {
		this.tesisGoster = tesisGoster;
	}

	public KatSayi getSeciliKatSayi() {
		return seciliKatSayi;
	}

	public void setSeciliKatSayi(KatSayi seciliKatSayi) {
		this.seciliKatSayi = seciliKatSayi;
	}

	public Boolean getVardiyaGoster() {
		return vardiyaGoster;
	}

	public void setVardiyaGoster(Boolean vardiyaGoster) {
		this.vardiyaGoster = vardiyaGoster;
	}

	public List<KatSayi> getKatSayiList() {
		return katSayiList;
	}

	public void setKatSayiList(List<KatSayi> katSayiList) {
		this.katSayiList = katSayiList;
	}

	public Boolean getVardiyaGetir() {
		return vardiyaGetir;
	}

	public void setVardiyaGetir(Boolean vardiyaGetir) {
		this.vardiyaGetir = vardiyaGetir;
	}

	public AramaSecenekleri getAs() {
		return as;
	}

	public void setAs(AramaSecenekleri as) {
		this.as = as;
	}

	public List<SelectItem> getPuantajKatSayiTipiList() {
		return puantajKatSayiTipiList;
	}

	public void setPuantajKatSayiTipiList(List<SelectItem> puantajKatSayiTipiList) {
		this.puantajKatSayiTipiList = puantajKatSayiTipiList;
	}

}
