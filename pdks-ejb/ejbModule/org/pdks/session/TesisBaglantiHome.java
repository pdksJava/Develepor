package org.pdks.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.pdks.entity.Tanim;
import org.pdks.entity.TesisBaglanti;
import org.pdks.enums.PersonelTipi;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;

@Name("tesisBaglantiHome")
public class TesisBaglantiHome extends EntityHome<TesisBaglanti> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8030902958844278816L;
	static Logger logger = Logger.getLogger(TesisBaglantiHome.class);
	/**
	 * 
	 */
	@RequestParameter
	Long tesisId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;

	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	public static String sayfaURL = "tesisBaglantiTanimlama";
	private List<TesisBaglanti> tesisBaglantiList = new ArrayList<TesisBaglanti>();
	private List<Tanim> tesisList;

	private List<SelectItem> personelTipiList;
	private Tanim tesis;

	private Session session;

	@Override
	public Object getId() {
		if (tesisId == null) {
			return super.getId();
		} else {
			return tesisId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Transactional
	public String kaydet() {
		int sirala = 0, kayit = 0;
		for (Iterator iterator = tesisBaglantiList.iterator(); iterator.hasNext();) {
			TesisBaglanti tb = (TesisBaglanti) iterator.next();
			if (tb.isCheckBoxDurum()) {
				if (tb.getId() == null || tb.isDegisti()) {
					pdksEntityController.saveOrUpdate(session, entityManager, tb);
					++kayit;
				}
			} else if (tb.getId() != null) {
				pdksEntityController.deleteObject(session, entityManager, tb);
				++sirala;
			}
			iterator.remove();
		}
		if (kayit + sirala > 0)
			try {
				session.flush();
				if (sirala > 0)
					pdksEntityController.savePrepareTableID(true, TesisBaglanti.class, entityManager, session);
				if (kayit > 0)
					PdksUtil.addMessageInfo("Kayıt" + (kayit == 1 ? "" : "lar") + " başarılı.");
				else
					PdksUtil.addMessageInfo("Kayıt" + (sirala == 1 ? "" : "lar") + " silindi.");
			} catch (Exception e) {
			}
		tesis = null;
		return "persisted";

	}

	private void fillTesisList() {
		session.clear();

		List<SelectItem> tesisIdList = new ArrayList<SelectItem>();
		fazlaMesaiOrtakIslemler.tesisDoldur(authenticatedUser, null, tesisIdList, session);
		if (tesisList == null)
			tesisList = new ArrayList<Tanim>();
		else
			tesisList.clear();

		if (tesisIdList.size() > 1) {
			List<Long> idList = new ArrayList<Long>();
			for (SelectItem st : tesisIdList)
				idList.add((Long) st.getValue());
			tesisList = PdksUtil.sortTanimList(null, pdksEntityController.getSQLParamByAktifFieldList(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_ID, idList, Tanim.class, session));

		}
		tesisIdList = null;

		tesis = null;
		tesisGuncelle();
	}

	/**
	 * @return
	 */
	public String tesisGuncelle() {
		tesisBaglantiList.clear();
		if (tesis != null) {
			List<TesisBaglanti> list = pdksEntityController.getSQLParamByFieldList(TesisBaglanti.TABLE_NAME, TesisBaglanti.COLUMN_NAME_TESIS, tesis.getId(), TesisBaglanti.class, session);
			HashMap<Long, TesisBaglanti> map = new HashMap<Long, TesisBaglanti>();
			List<TesisBaglanti> list2 = new ArrayList<TesisBaglanti>();
			for (TesisBaglanti tb : list) {
				tb.setCheckBoxDurum(true);
				tb.setDegisti(false);
				map.put(tb.getTesisBaglanti().getId(), tb);
			}
			for (Tanim tesisBaglanti : tesisList) {
				if (tesisBaglanti.getId().equals(tesis.getId()) == false) {
					if (map.containsKey(tesisBaglanti.getId()))
						tesisBaglantiList.add(map.get(tesisBaglanti.getId()));
					else
						list2.add(new TesisBaglanti(tesis, tesisBaglanti));
				}
			}
			if (list2.isEmpty() == false)
				tesisBaglantiList.addAll(list2);
			list2 = null;
			list = null;
			map = null;
		}
		return "";
	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		String sayfa = "";
		if (ortakIslemler.getTesisDurumu()) {
			if (personelTipiList == null)
				personelTipiList = new ArrayList<SelectItem>();
			else
				personelTipiList.clear();
			personelTipiList.add(new SelectItem(PersonelTipi.TUM.value(), TesisBaglanti.getPersonelTipiAciklama(PersonelTipi.TUM)));
			personelTipiList.add(new SelectItem(PersonelTipi.IK.value(), TesisBaglanti.getPersonelTipiAciklama(PersonelTipi.IK)));
			personelTipiList.add(new SelectItem(PersonelTipi.SUPERVISOR.value(), TesisBaglanti.getPersonelTipiAciklama(PersonelTipi.SUPERVISOR)));
			fillTesisList();
		} else
			sayfa = MenuItemConstant.home;

		return sayfa;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public List<TesisBaglanti> getTesisBaglantiList() {
		return tesisBaglantiList;
	}

	public void setTesisBaglantiList(List<TesisBaglanti> tesisBaglantiList) {
		this.tesisBaglantiList = tesisBaglantiList;
	}

	public List<Tanim> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<Tanim> value) {
		this.tesisList = value;
	}

	public Tanim getTesis() {
		return tesis;
	}

	public void setTesis(Tanim tesis) {
		this.tesis = tesis;
	}

	public List<SelectItem> getPersonelTipiList() {
		return personelTipiList;
	}

	public void setPersonelTipiList(List<SelectItem> personelTipiList) {
		this.personelTipiList = personelTipiList;
	}
}
