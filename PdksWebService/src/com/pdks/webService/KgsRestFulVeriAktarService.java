package com.pdks.webService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.pdks.dao.PdksDAO;
import org.pdks.dao.impl.BaseDAOHibernate;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.PdksUtil;
import org.pdks.kgs.model.Cihaz;
import org.pdks.kgs.model.CihazPersonel;
import org.pdks.kgs.model.CihazTipi;
import org.pdks.kgs.model.Durum;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@Service
@Path("/servicesKGS")
public class KgsRestFulVeriAktarService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7707560744586116520L;

	public Logger logger = Logger.getLogger(KgsRestFulVeriAktarService.class);

	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;

	private Gson gson = new Gson();

	private String fonksiyon;

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/saveCihaz")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	/**	
	[
	{
		"id": 1,
		"adi": "Personel",
		"tipi": "1",
		"durum": 1
	},
	{
		"id": 2,
		"adi": "Muhasebe",
		"tipi": null,
		"durum": 1
	}
	]
	  http://localhost:8080/PdksWebService/rest/servicesKGS/saveCihaz
	 **/
	public Response saveCihaz() throws Exception {
		fonksiyon = "saveCihaz";
		Response response = null;
		String sonuc = null;
		try {
			LinkedHashMap<String, String> headers = getHeaders();
			if (headers.containsKey("username") && headers.containsKey("password")) {
				String json = PdksRestFulVeriAktarService.getBodyString(request);
				List<LinkedTreeMap> dataList = gson.fromJson(json, List.class);
				List<Cihaz> cihazlar = new ArrayList<Cihaz>();
				if (!dataList.isEmpty()) {
					for (LinkedTreeMap linkedTreeMap : dataList) {
						json = gson.toJson(linkedTreeMap);
						Cihaz cihaz = gson.fromJson(json, Cihaz.class);
						boolean devam = true;
						CihazTipi cihazTipi = null;
						if (PdksUtil.hasStringValue(cihaz.getTipi())) {
							cihazTipi = cihaz.getCihazTipi();
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
						headers.put("cihazlar", gson.toJson(cihazlar));
						veriMap.put("jsonData", gson.toJson(headers));
						PdksDAO pdksDAO = Constants.pdksDAO;
						veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_UPDATE_CIHAZ");
						String mesaj = null;
						List sonucList = pdksDAO.execSPList(veriMap, null);
						if (!sonucList.isEmpty() && sonucList.size() == 1) {
							Object object = (Object) sonucList.get(0);
							if (object instanceof String)
								mesaj = (String) object;
						}
						sonuc = getKullaniciHatali(mesaj);
					} else
						sonuc = getKullaniciHatali("Cihaz bilgileri eksik!");

				} else
					sonuc = getKullaniciHatali("Cihaz yok!");

			} else
				sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");
		} catch (Exception e) {
			sonuc = getKullaniciHatali("Hata oluştu!-->" + e);
		}
		response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		return response;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/savePersonel")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	/**	
	[
	{
		"id": 1,
		"adi": "AHMET",
		"soyadi": "CİNGÖZ",
		"personelNo": "00121",
		"kimlikNo": "3233300121",
		"durum": 1
	},
	{
		"id": 2,
		"adi": "MERYEM",
		"soyadi": "DEMİR",
		"personelNo": "00123",
		"kimlikNo": "3233222300121",
		"durum": 1
	}
	]
	  http://localhost:8080/PdksWebService/rest/servicesKGS/savePersonel
	 **/
	public Response savePersonel() throws Exception {
		fonksiyon = "savePersonel";
		Response response = null;
		String sonuc = null;
		try {
			LinkedHashMap<String, String> headers = getHeaders();
			if (headers.containsKey("username") && headers.containsKey("password")) {
				String json = PdksRestFulVeriAktarService.getBodyString(request);
				List<LinkedTreeMap> dataList = gson.fromJson(json, List.class);
				List<CihazPersonel> personeller = new ArrayList<CihazPersonel>();
				if (!dataList.isEmpty()) {
					for (LinkedTreeMap linkedTreeMap : dataList) {
						json = gson.toJson(linkedTreeMap);
						CihazPersonel cihazPersonel = gson.fromJson(json, CihazPersonel.class);
						if (cihazPersonel.getId() != null && PdksUtil.hasStringValue(cihazPersonel.getPersonelNo()))
							personeller.add(cihazPersonel);
					}
					if (!personeller.isEmpty()) {
						LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
						headers.put("personeller", gson.toJson(personeller));
						veriMap.put("jsonData", gson.toJson(headers));
						PdksDAO pdksDAO = Constants.pdksDAO;
						veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_UPDATE_CIHAZ_PERSONEL");
						String mesaj = null;
						List sonucList = pdksDAO.execSPList(veriMap, null);
						if (!sonucList.isEmpty() && sonucList.size() == 1) {
							Object object = (Object) sonucList.get(0);
							if (object instanceof String)
								mesaj = (String) object;
						}
						sonuc = getKullaniciHatali(mesaj);
					} else
						sonuc = getKullaniciHatali("Personel bilgileri eksik!");
				} else
					sonuc = getKullaniciHatali("Personel yok!");

			} else
				sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");

			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/saveCihazGecis")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveCihazGecis() throws Exception {
		fonksiyon = "saveCihazGecis";
		HashMap<String, Object> durumMap = new HashMap<String, Object>();

		Response response = null;
		try {
			String sonuc = gson.toJson(durumMap);
			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @param mesaj
	 * @return
	 */
	private String getKullaniciHatali(String mesaj) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("fonksiyon", fonksiyon);
		map.put("durum", mesaj == null);
		if (mesaj != null)
			map.put("hata", mesaj);
		String sonuc = gson.toJson(map);
		map = null;
		return sonuc;

	}

	/**
	 * @return
	 */
	private LinkedHashMap<String, String> getHeaders() {
		LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
		String username = null, password = null;
		for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
			String nextHeaderName = (String) e.nextElement();
			String headerValue = request.getHeader(nextHeaderName);
			if (nextHeaderName.equals("username"))
				username = headerValue;
			else if (nextHeaderName.equals("password"))
				password = headerValue;
		}
		if (username != null)
			headers.put("username", username);
		if (password != null)
			headers.put("password", password);
		return headers;
	}
}
