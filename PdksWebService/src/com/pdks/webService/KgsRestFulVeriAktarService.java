package com.pdks.webService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
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
import org.pdks.kgs.model.Cihaz;
import org.pdks.kgs.model.CihazGecis;
import org.pdks.kgs.model.CihazPersonel;
import org.pdks.kgs.model.Sonuc;
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
				List<Cihaz> cihazList = new ArrayList<Cihaz>();
				if (!dataList.isEmpty()) {
					for (LinkedTreeMap linkedTreeMap : dataList) {
						json = gson.toJson(linkedTreeMap);
						Cihaz cihaz = gson.fromJson(json, Cihaz.class);
						cihazList.add(cihaz);
					}

				}
				CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
				sonuc = getKullaniciHatali(cihazVeriOrtakAktar.saveCihaz(cihazList, headers).getHata());
				cihazList = null;
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
				List<CihazPersonel> personelList = new ArrayList<CihazPersonel>();
				if (!dataList.isEmpty()) {
					for (LinkedTreeMap linkedTreeMap : dataList) {
						json = gson.toJson(linkedTreeMap);
						CihazPersonel cihazPersonel = gson.fromJson(json, CihazPersonel.class);
						personelList.add(cihazPersonel);
					}
					CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
					sonuc = getKullaniciHatali(cihazVeriOrtakAktar.savePersonel(personelList, headers).getHata());
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
	/**
	
	 [
	 {
	 "id": 1,
	 "cihazId": 1,
	 "personelId": 1,
	 "tarih": "20241124",
	 "saat": "1145",
	 "tipi": "1",
	 "durum": 1
	 },
	 {
	 "id": 2,
	 "cihazId": 2,
	 "personelId": 2,
	 "tarih": "20241124",
	 "saat": "1135",
	 "tipi": "1",
	 "durum": 1
	 },
	 {
	 "id": 3,
	 "cihazId": 2,
	 "personelId": 2,
	 "tarih": "20241124",
	 "saat": "1645",
	 "tipi": "2",
	 "durum": 1
	 }	
	
	 ]
	
	 *  http://localhost:8080/PdksWebService/rest/servicesKGS/saveCihazGecis
	 */
	public Response saveCihazGecis() throws Exception {
		fonksiyon = "saveCihazGecis";
		String sonuc = null;
		Response response = null;
		try {
			LinkedHashMap<String, String> headers = getHeaders();
			if (headers.containsKey("username") && headers.containsKey("password")) {
				String json = PdksRestFulVeriAktarService.getBodyString(request);
				List<LinkedTreeMap> dataList = gson.fromJson(json, List.class);
				List<CihazGecis> gecisList = new ArrayList<CihazGecis>();
				if (!dataList.isEmpty()) {
					for (LinkedTreeMap linkedTreeMap : dataList) {
						json = gson.toJson(linkedTreeMap);
						CihazGecis cihazGecis = gson.fromJson(json, CihazGecis.class);
						gecisList.add(cihazGecis);
					}
					CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
					sonuc = getKullaniciHatali(cihazVeriOrtakAktar.saveCihazGecis(gecisList, headers).getHata());
				} else
					sonuc = getKullaniciHatali("Cihaz geçiş yok!");

			} else
				sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");

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
		String string = gson.toJson(new Sonuc(fonksiyon, mesaj == null, mesaj));
		return string;
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
