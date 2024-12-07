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
import org.pdks.kgs.model.CihazTipi;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.google.gson.Gson;

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

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/saveCihaz")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	/**	
	 {
	 "id": 1,
	 "adi": "Giriş",
	 "tipi": "1",
	 "durum": true
	 }
	  http://localhost:8080/PdksWebService/rest/servicesKGS/saveCihaz
	 **/
	public Response saveCihaz() throws Exception {
		Response response = null;
		String sonuc = null;
		try {
			LinkedHashMap<String, String> headers = getHeaders();
			if (headers.containsKey("username") && headers.containsKey("password")) {
				String json = PdksRestFulVeriAktarService.getBodyString(request);
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
				if (devam) {
					List<LinkedHashMap<String, Object>> list = new ArrayList<LinkedHashMap<String, Object>>();
					int durum = cihaz.getDurum() != null && cihaz.getDurum() ? 1 : 0;
					if (cihazTipi != null) {
						LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
						veriMap.put("id", cihaz.getId());
						veriMap.put("ad", cihaz.getAdi());
						veriMap.put("tipi", cihazTipi.value());
						veriMap.put("durum", durum);
						veriMap.putAll(headers);
						list.add(veriMap);
					} else {
						LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
						veriMap.put("id", cihaz.getId());
						veriMap.put("ad", cihaz.getAdi() + " GİRİŞ");
						veriMap.put("tipi", CihazTipi.GIRIS);
						veriMap.put("durum", durum);
						veriMap.putAll(headers);
						list.add(veriMap);
						veriMap = new LinkedHashMap<String, Object>();
						veriMap.put("id", cihaz.getId());
						veriMap.put("ad", cihaz.getAdi() + " ÇIKIŞ");
						veriMap.put("tipi", CihazTipi.CIKIS);
						veriMap.put("durum", durum);
						veriMap.putAll(headers);
						list.add(veriMap);
					}
					PdksDAO pdksDAO = Constants.pdksDAO;
					int adet = 0;
					String mesaj = "";
					for (LinkedHashMap<String, Object> veriMap : list) {
						veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_UPDATE_CIHAZ");
						List<Object[]> sonucList = pdksDAO.execSPList(veriMap, null);
						if (!sonucList.isEmpty()) {
							Object[] objects = sonucList.get(0);
							mesaj = (String) objects[0];
						}
						adet += (sonucList != null && sonucList.size() == 1 ? 1 : 0);
					}
					if (adet == list.size() && PdksUtil.hasStringValue(mesaj) == false) {
						LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
						veriMap.put("id", cihaz.getId());
						veriMap.put("durum", Boolean.TRUE);
						sonuc = gson.toJson(veriMap);
					} else
						sonuc = getKullaniciHatali(mesaj);

				}

			} else
				sonuc = getKullaniciHatali("Kullanıcı bilgi eksik!");

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
	public Response savePersonel(@RequestHeader(value = "User-Agent") String userAgent) throws Exception {
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
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/saveCihazGecis")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveCihazGecis(@RequestHeader(value = "User-Agent") String userAgent) throws Exception {

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
		HashMap<String, String> map = new HashMap<String, String>();
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
