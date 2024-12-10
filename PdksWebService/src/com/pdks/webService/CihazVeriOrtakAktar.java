package com.pdks.webService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.pdks.dao.PdksDAO;
import org.pdks.dao.impl.BaseDAOHibernate;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.PdksUtil;
import org.pdks.kgs.model.Cihaz;
import org.pdks.kgs.model.CihazGecis;
import org.pdks.kgs.model.CihazPersonel;
import org.pdks.kgs.model.CihazTipi;
import org.pdks.kgs.model.CihazUser;
import org.pdks.kgs.model.Durum;
import org.pdks.kgs.model.Sonuc;

import com.google.gson.Gson;

public class CihazVeriOrtakAktar implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5158008675024700146L;
	private String fonksiyon;
	private Gson gson;
	PdksDAO pdksDAO;

	public CihazVeriOrtakAktar() {
		super();

	};

	public CihazVeriOrtakAktar(String fonksiyon) {
		super();
		this.fonksiyon = fonksiyon;
		gson = new Gson();
		pdksDAO = Constants.pdksDAO;
	};

	/**
	 * @param cihazList
	 * @param user
	 * @return
	 */
	public Sonuc saveCihaz(List<Cihaz> cihazList, CihazUser user) {
		Sonuc sonuc = null;
		if (cihazList != null && cihazList.isEmpty() == false) {
			List<Cihaz> cihazlar = new ArrayList<Cihaz>();
			for (Iterator iterator = cihazList.iterator(); iterator.hasNext();) {
				Cihaz cihaz = (Cihaz) iterator.next();
				boolean devam = true;
				CihazTipi cihazTipi = null;
				if (cihaz.getTipi() != null) {
					cihazTipi = CihazTipi.fromValue(cihaz.getTipi());
					if (cihazTipi == null) {
						sonuc = getKullaniciHatali("Cihaz tipi hatalı [1:Giriş/2:Çıkış]");
						devam = false;
					}
				}
				if (devam)
					if (cihaz.getId() == null || PdksUtil.hasStringValue(cihaz.getAdi()) == false)
						devam = false;
				if (devam) {
					if (cihaz.getDurum() == null)
						cihaz.setDurum(Durum.PASIF.value());
					if (cihazTipi != null) {
						cihazlar.add(cihaz);
					} else {
						Cihaz cihazGiris = (Cihaz) cihaz.clone(), cihazCikis = (Cihaz) cihaz.clone();
						cihazlar.add(cihazGiris);
						cihazlar.add(cihazCikis);
						cihazGiris.setAdi(cihaz.getAdi() + " GİRİŞ");
						cihazGiris.setTipi(CihazTipi.GIRIS.value());
						cihazCikis.setAdi(cihaz.getAdi() + " ÇIKIŞ");
						cihazCikis.setTipi(CihazTipi.CIKIS.value());
					}
				}
			}
			if (!cihazlar.isEmpty()) {
				LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
				LinkedHashMap<String, Object> headers = getHeaders(user);
				headers.put("cihazlar", gson.toJson(cihazlar));
				veriMap.put("jsonData", gson.toJson(headers));
				veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_UPDATE_CIHAZ");
				String mesaj = null;
				List sonucList = null;
				try {
					sonucList = pdksDAO.execSPList(veriMap, null);
				} catch (Exception e) {
					sonucList = new ArrayList();
				}
				if (!sonucList.isEmpty() && sonucList.size() == 1) {
					Object object = (Object) sonucList.get(0);
					if (object instanceof String)
						mesaj = (String) object;
				}
				sonuc = getKullaniciHatali(mesaj);
			} else
				sonuc = getKullaniciHatali("Cihaz bilgileri eksik!");
			cihazlar = null;
		} else
			sonuc = getKullaniciHatali("Cihaz yok!");
		return sonuc;
	}

	/**
	 * @param personelList
	 * @param cihazUser
	 * @return
	 */
	public Sonuc savePersonel(List<CihazPersonel> personelList, CihazUser cihazUser) {
		Sonuc sonuc = null;
		if (personelList != null && personelList.isEmpty() == false) {
			List<CihazPersonel> personeller = new ArrayList<CihazPersonel>();
			for (Iterator iterator = personelList.iterator(); iterator.hasNext();) {
				CihazPersonel cihazPersonel = (CihazPersonel) iterator.next();
				if (cihazPersonel.getId() != null && PdksUtil.hasStringValue(cihazPersonel.getPersonelNo()))
					personeller.add(cihazPersonel);
			}
			if (!personeller.isEmpty()) {
				LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
				LinkedHashMap<String, Object> headers = getHeaders(cihazUser);
				headers.put("personeller", gson.toJson(personeller));
				veriMap.put("jsonData", gson.toJson(headers));
				veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_UPDATE_CIHAZ_PERSONEL");
				String mesaj = null;
				List sonucList = null;
				try {
					sonucList = pdksDAO.execSPList(veriMap, null);
				} catch (Exception e) {
					sonucList = new ArrayList();
				}
				if (!sonucList.isEmpty() && sonucList.size() == 1) {
					Object object = (Object) sonucList.get(0);
					if (object instanceof String)
						mesaj = (String) object;
				}
				sonuc = getKullaniciHatali(mesaj);
			} else
				sonuc = getKullaniciHatali("Personel bilgileri eksik!");
			personeller = null;
		} else
			sonuc = getKullaniciHatali("Personel yok!");
		return sonuc;
	}

	/**
	 * @param gecisList
	 * @param cihazUser
	 * @return
	 */
	public Sonuc saveCihazGecis(List<CihazGecis> gecisList, CihazUser cihazUser) {
		Sonuc sonuc = null;
		if (gecisList != null && gecisList.isEmpty() == false) {
			List<CihazGecis> gecisler = new ArrayList<CihazGecis>();
			for (CihazGecis cihazGecis : gecisList) {
				CihazTipi cihazTipi = CihazTipi.fromValue(cihazGecis.getTipi());
				if (cihazGecis.getPersonelId() == null || cihazGecis.getCihazId() == null || cihazTipi == null)
					continue;
				if (cihazGecis.getId() != null && PdksUtil.hasStringValue(cihazGecis.getTarih()) && PdksUtil.hasStringValue(cihazGecis.getSaat()))
					gecisler.add(cihazGecis);
			}
			if (!gecisler.isEmpty()) {
				LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
				LinkedHashMap<String, Object> headers = getHeaders(cihazUser);
				headers.put("gecisler", gson.toJson(gecisler));
				veriMap.put("jsonData", gson.toJson(headers));
				veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_UPDATE_CIHAZ_GECIS");
				String mesaj = null;
				List sonucList = null;
				try {
					sonucList = pdksDAO.execSPList(veriMap, null);
				} catch (Exception e) {
					sonucList = new ArrayList();
				}
				if (!sonucList.isEmpty() && sonucList.size() == 1) {
					Object object = (Object) sonucList.get(0);
					if (object instanceof String)
						mesaj = (String) object;
				}
				sonuc = getKullaniciHatali(mesaj);
			} else
				sonuc = getKullaniciHatali("Cihaz geçiş bilgileri eksik!");
			gecisler = null;
		} else
			sonuc = getKullaniciHatali("Personel yok!");
		return sonuc;
	}

	public LinkedHashMap<String, Object> getHeaders(CihazUser cihazUser) {
		LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
		if (cihazUser != null) {
			veriMap.put("username", cihazUser.getUsername());
			veriMap.put("password", cihazUser.getPassword());
		}
		return veriMap;

	}

	/**
	 * @param mesaj
	 * @return
	 */
	private Sonuc getKullaniciHatali(String mesaj) {
		Sonuc string = new Sonuc(fonksiyon, mesaj == null, mesaj);
		return string;
	}
}
