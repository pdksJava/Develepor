package org.pdks.session;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.kgs.entity.MySQLPersonel;
import org.pdks.entity.Departman;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;

@Name("kgsPersonelHome")
public class KgsPersonelHome extends EntityHome<MySQLPersonel> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6244049958444869598L;

	static Logger logger = Logger.getLogger(KgsPersonelHome.class);
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

	public static String sayfaURL = "kgsPersonelTanimlama";
	private MySQLPersonel sqlPersonel;

	private List<MySQLPersonel> sqlPersonelList;

	private String sicilNo, adi, soyadi, spAdi;

	private LinkedHashMap<String, String> kgsPersonelSPMap;

	private boolean kimlikGoster, kartNoGoster, dTarihGoster, ekle, kayitEdilebilir;

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

	/**
	 * @param xCalismaModeli
	 * @return
	 */
	public String personelEkle(MySQLPersonel personel) {
		kayitEdilebilir = personel != null || spAdi != null;
		if (personel == null) {
			personel = new MySQLPersonel();
			personel.setIsGirisTarihi(PdksUtil.getDate(new Date()));
			personel.setIsCikisTarihi(PdksUtil.convertToJavaDate("99991231", "yyyyMMdd"));
		}

		setSqlPersonel(personel);
		return "";
	}

	public void instanceRefresh() {
		if (sqlPersonel.getId() != null)
			session.refresh(sqlPersonel);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(entityManager, session, sayfaURL);
		spAdi = null;
		ekle = authenticatedUser.isAdmin();
		List<Departman> depList = pdksEntityController.getSQLTableList(Departman.TABLE_NAME, Departman.class, session);
		for (Departman departman : depList) {
			if (ekle == false)
				ekle = departman.isAdminMi() == false;
		}
		kgsPersonelSPMap = null;
		fillPersonelList();
		List<Tanim> list = ortakIslemler.getTanimList(Tanim.TIPI_KGS_ENTEGRASYON_ALAN, session);
		if (list.isEmpty() == false) {
			list = PdksUtil.sortObjectStringAlanList(list, "getKodu", null);
			Tanim spTanim = null;
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Tanim tanim = (Tanim) iterator.next();
				if (tanim.getDurum()) {
					if (tanim.getKodu().equalsIgnoreCase("sp")) {
						spTanim = tanim;
						iterator.remove();
					}
				} else
					iterator.remove();

			}
			if (spTanim != null && list.isEmpty() == false) {
				if (pdksEntityController.isExisStoreProcedure(spTanim.getErpKodu(), session)) {
					spAdi = spTanim.getErpKodu();
					kgsPersonelSPMap = new LinkedHashMap<String, String>();
					for (Tanim tanim : list)
						kgsPersonelSPMap.put(tanim.getKodu(), tanim.getErpKodu());
				}
			}

		}

	}

	@Transactional
	public String kaydet() {
		try {
			if (sqlPersonel.getId() == null) {
				LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
				for (Iterator iterator = kgsPersonelSPMap.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					Object value = null;
					String alanAdi = kgsPersonelSPMap.get(key);
					if (alanAdi.equalsIgnoreCase("ADI"))
						value = sqlPersonel.getAdi();
					else if (alanAdi.equalsIgnoreCase("SOYADI"))
						value = sqlPersonel.getSoyadi();
					else if (alanAdi.equalsIgnoreCase("PERSONEL_NO"))
						value = sqlPersonel.getSicilNo();
					else if (alanAdi.equalsIgnoreCase("KIMLIK_NO"))
						value = sqlPersonel.getKimlikNo();
					else if (alanAdi.equalsIgnoreCase("ISE_GIRIS_TARIHI"))
						value = sqlPersonel.getIsGirisTarihi();
					else if (alanAdi.equalsIgnoreCase("ISTEN_AYRILMA_TARIHI"))
						value = sqlPersonel.getIsCikisTarihi();
					else if (alanAdi.equalsIgnoreCase("DOGUM_TARIHI"))
						value = sqlPersonel.getDogumTarihi();
					else if (alanAdi.equalsIgnoreCase("KART_NO"))
						value = sqlPersonel.getKartId();
					veriMap.put(key, value);
				}
				if (veriMap.isEmpty() == false) {
					List<PersonelKGS> list = pdksEntityController.execSPList(session, veriMap, spAdi, PersonelKGS.class);
					if (list != null) {
						if (list.size() == 1)
							sqlPersonel.setId(list.get(0).getKgsId());
						list = null;
					}
				}
				veriMap = null;
			} else {
				session.saveOrUpdate(sqlPersonel);
				session.flush();
			}

			fillPersonelList();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public String fillPersonelList() {
		session.clear();
		kimlikGoster = false;
		kartNoGoster = false;
		dTarihGoster = false;
		HashMap fields = new HashMap();
		StringBuilder sb = new StringBuilder();
		sb.append("select V.* from " + MySQLPersonel.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK() + " ");
		String str = " where ";
		if (PdksUtil.hasStringValue(adi)) {
			fields.put("ad1", "%" + adi.trim() + "%");
			sb.append(str + "  V." + MySQLPersonel.COLUMN_NAME_ADI + " like :ad1 ");
			str = " and ";
		}
		if (PdksUtil.hasStringValue(soyadi)) {
			fields.put("soyad1", "%" + soyadi.trim() + "%");
			sb.append(str + " V." + MySQLPersonel.COLUMN_NAME_SOYADI + " like :soyad1 ");
			str = " and ";
		}
		if (PdksUtil.hasStringValue(sicilNo)) {
			sicilNo = ortakIslemler.getSicilNo(sicilNo);
			String eqStr = "=";
			if (PdksUtil.getSicilNoUzunluk() != null) {
				fields.put("sicilNo1", sicilNo.trim());

			} else {
				eqStr = "like";
				Long sayi = null;
				try {
					sayi = Long.parseLong(sicilNo);
				} catch (Exception e) {
				}
				if (sayi != null && sayi.longValue() > 0) {
					fields.put("sicilNo1", "%" + sicilNo.trim());

				} else {
					fields.put("sicilNo1", sicilNo.trim() + "%");

				}
			}
			sb.append(str + " V." + MySQLPersonel.COLUMN_NAME_SICIL_NO + " " + eqStr + " :sicilNo1 ");

		}
		boolean bos = fields.isEmpty();

		if (bos)
			sb.append(str + " 1 = 1");
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		try {
			sqlPersonelList = pdksEntityController.getObjectBySQLList(sb, fields, MySQLPersonel.class);
			if (sqlPersonelList.size() > 1)
				sqlPersonelList = PdksUtil.sortObjectStringAlanList(sqlPersonelList, "getAdiSoyadi", null);
			for (MySQLPersonel per : sqlPersonelList) {
				if (kimlikGoster == false)
					kimlikGoster = PdksUtil.hasStringValue(per.getKimlikNo());
				if (kartNoGoster == false)
					kartNoGoster = PdksUtil.hasStringValue(per.getKartId());
				if (dTarihGoster == false)
					dTarihGoster = per.getDogumTarihi() != null;

			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return "";
	}

	public MySQLPersonel getSqlPersonel() {
		return sqlPersonel;
	}

	public void setSqlPersonel(MySQLPersonel sqlPersonel) {
		this.sqlPersonel = sqlPersonel;
	}

	public List<MySQLPersonel> getSqlPersonelList() {
		return sqlPersonelList;
	}

	public void setSqlPersonelList(List<MySQLPersonel> sqlPersonelList) {
		this.sqlPersonelList = sqlPersonelList;
	}

	public String getSicilNo() {
		return sicilNo;
	}

	public void setSicilNo(String sicilNo) {
		this.sicilNo = sicilNo;
	}

	public String getSoyadi() {
		return soyadi;
	}

	public void setSoyadi(String soyadi) {
		this.soyadi = soyadi;
	}

	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getSpAdi() {
		return spAdi;
	}

	public void setSpAdi(String spAdi) {
		this.spAdi = spAdi;
	}

	public boolean isKimlikGoster() {
		return kimlikGoster;
	}

	public void setKimlikGoster(boolean kimlikGoster) {
		this.kimlikGoster = kimlikGoster;
	}

	public boolean isKartNoGoster() {
		return kartNoGoster;
	}

	public void setKartNoGoster(boolean kartNoGoster) {
		this.kartNoGoster = kartNoGoster;
	}

	public boolean isdTarihGoster() {
		return dTarihGoster;
	}

	public void setdTarihGoster(boolean dTarihGoster) {
		this.dTarihGoster = dTarihGoster;
	}

	public boolean isEkle() {
		return ekle;
	}

	public void setEkle(boolean ekle) {
		this.ekle = ekle;
	}

	public boolean isKayitEdilebilir() {
		return kayitEdilebilir;
	}

	public void setKayitEdilebilir(boolean kayitEdilebilir) {
		this.kayitEdilebilir = kayitEdilebilir;
	}

}
