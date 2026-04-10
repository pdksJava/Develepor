package com.pdks.webService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.kgs.entity.MySQLPersonel;
import org.pdks.dao.PdksDAO;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.Sirket;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.enums.PuantajKatSayiTipi;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.PdksUtil;
import org.pdks.kgs.model.Cihaz;
import org.pdks.kgs.model.CihazGecis;
import org.pdks.kgs.model.CihazPersonel;
import org.pdks.kgs.model.CihazUser;
import org.pdks.kgs.model.Sonuc;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@Service
@Path("/servicesKGS")
// @WSDLDocumentation("http://localhost:8080/PdksWebService/rest/servicesKGS")
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
		"tipi": 1,
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
			CihazUser cihazUser = getCihazUser();
			if (cihazUser != null) {
				String json = PdksRestFulVeriAktarService.getBodyString(request);
				List<LinkedTreeMap> dataList = gson.fromJson(json, List.class);
				List<Cihaz> cihazList = new ArrayList<Cihaz>();
				if (!dataList.isEmpty()) {
					for (LinkedTreeMap linkedTreeMap : dataList) {
						json = gson.toJson(linkedTreeMap);
						Cihaz cihaz = gson.fromJson(json, Cihaz.class);
						cihazList.add(cihaz);
					}
				} else
					sonuc = getKullaniciHatali("Cihaz yok!");
				dataList = null;
				CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
				sonuc = getKullaniciHatali(cihazVeriOrtakAktar.saveCihaz(cihazList, cihazUser).getHata());
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
			CihazUser cihazUser = getCihazUser();
			if (cihazUser != null) {
				String json = PdksRestFulVeriAktarService.getBodyString(request);
				List<LinkedTreeMap> dataList = gson.fromJson(json, List.class);
				if (!dataList.isEmpty()) {
					List<CihazPersonel> personelList = new ArrayList<CihazPersonel>();
					for (LinkedTreeMap linkedTreeMap : dataList) {
						json = gson.toJson(linkedTreeMap);
						CihazPersonel cihazPersonel = gson.fromJson(json, CihazPersonel.class);
						personelList.add(cihazPersonel);
					}
					CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
					sonuc = getKullaniciHatali(cihazVeriOrtakAktar.savePersonel(personelList, cihazUser).getHata());
					personelList = null;
				} else
					sonuc = getKullaniciHatali("Personel yok!");
				dataList = null;
			} else
				sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");

			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	@POST
	@Path("/getPersonel")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response postPersonel(@QueryParam("sicilNo") String sicilNo, @QueryParam("kimlikNo") String kimlikNo, @QueryParam("tarih") String tarih) throws Exception {
		String data = PdksRestFulVeriAktarService.getBodyString(request);
		LinkedTreeMap<String, Object> jsonMap = data != null ? gson.fromJson(data, LinkedTreeMap.class) : new LinkedTreeMap<String, Object>();
		if (PdksUtil.hasStringValue(sicilNo) == false)
			sicilNo = jsonMap.containsKey("sicilNo") ? (String) jsonMap.get("sicilNo") : null;
		if (PdksUtil.hasStringValue(kimlikNo) == false)
			kimlikNo = jsonMap.containsKey("kimlikNo") ? (String) jsonMap.get("kimlikNo") : null;
		if (PdksUtil.hasStringValue(tarih) == false)
			tarih = jsonMap.containsKey("tarih") ? (String) jsonMap.get("tarih") : null;
		jsonMap = null;
		Response response = getPersonelResponse(sicilNo, kimlikNo, tarih);
		return response;
	}

	@GET
	@Path("/getPersonel")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getPersonel(@QueryParam("sicilNo") String sicilNo, @QueryParam("kimlikNo") String kimlikNo, @QueryParam("tarih") String tarih) throws Exception {
		Response response = getPersonelResponse(sicilNo, kimlikNo, tarih);
		return response;
	}

	/**
	 * @param sicilNo
	 * @param kimlikNo
	 * @param tarih
	 * @return
	 */
	private Response getPersonelResponse(String sicilNo, String kimlikNo, String tarih) {
		Response response;
		PdksDAO pdksDAO = Constants.pdksDAO;
		Date islemZamani = new Date();
		LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		fields.clear();
		sb = new StringBuffer();
		sb.append("select K.* from " + MySQLPersonel.TABLE_NAME + " K " + PdksVeriOrtakAktar.getSelectLOCK());
		sb.append(" where K." + MySQLPersonel.COLUMN_NAME_SICIL_NO + " = :s ");
		fields.put("s", sicilNo);
		if (PdksUtil.hasStringValue(kimlikNo)) {
			sb.append(" and K." + MySQLPersonel.COLUMN_NAME_KIMLIK_NO + " = :k");
			fields.put("k", kimlikNo);
		}
		String hata = null;
		List<MySQLPersonel> personelSQLList = pdksDAO.getNativeSQLList(fields, sb, MySQLPersonel.class);
		MySQLPersonel mySQLPersonel = null;
		if (personelSQLList != null && personelSQLList.isEmpty() == false) {
			mySQLPersonel = personelSQLList.get(0);
			if (mySQLPersonel.getIsCikisTarihi() != null && mySQLPersonel.getIsCikisTarihi().before(PdksUtil.getDate(islemZamani))) {
				hata = mySQLPersonel.getAdi() + " " + mySQLPersonel.getSoyadi() + " " + PdksUtil.convertToDateString(mySQLPersonel.getIsCikisTarihi(), "yyyy-MM-dd") + " tarihinde işten ayrıldı!";
				mySQLPersonel = null;
			}
		}

		sb = new StringBuffer();
		sb.append("select P.* from " + PersonelKGS.TABLE_NAME + " K " + PdksVeriOrtakAktar.getSelectLOCK());
		if (mySQLPersonel == null)
			sb.append(" where 1 = 2 ");
		else {
			sb.append(" inner join " + Personel.TABLE_NAME + " P " + PdksVeriOrtakAktar.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_KGS_PERSONEL + " = K." + PersonelKGS.COLUMN_NAME_ID);
			sb.append(" where K." + PersonelKGS.COLUMN_NAME_SICIL_NO + " = :s ");
			fields.put("s", sicilNo);
			if (PdksUtil.hasStringValue(kimlikNo)) {
				sb.append(" and K." + PersonelKGS.COLUMN_NAME_KIMLIK_NO + " = :k");
				fields.put("k", kimlikNo);
			}
		}

		Personel personel = null;
		List<Personel> personelList = pdksDAO.getNativeSQLList(fields, sb, Personel.class);
		if (personelList != null) {
			for (Personel per : personelList) {
				if (per.isCalisiyor())
					personel = per;
			}
		}

		Gson gson = new Gson();

		if (personel != null) {
			PersonelKGS kgs = personel.getPersonelKGS();
			veriMap.put("personelId", mySQLPersonel.getId());
			veriMap.put("sicilNo", sicilNo);
			if (PdksUtil.hasStringValue(kgs.getKimlikNo()))
				veriMap.put("kimlikNo", kgs.getKimlikNo());
			veriMap.put("adi", kgs.getAd());
			veriMap.put("soyadi", kgs.getSoyad());
			if (PdksUtil.hasStringValue(tarih)) {
				Date vardiyaTarih = PdksUtil.getDateFromString(tarih);
				if (vardiyaTarih != null) {
					PdksVeriOrtakAktar ortak = new PdksVeriOrtakAktar();
					List<Long> perIdList = new ArrayList<Long>();
					perIdList.add(personel.getId());
					Date basTarih = PdksUtil.tariheGunEkleCikar(vardiyaTarih, -1);
					List<VardiyaGun> vList = ortak.getVardiyalar(basTarih, vardiyaTarih, perIdList);
					perIdList = null;
					Vardiya islemVardiya = null;
					if (vList != null && vList.isEmpty() == false) {
						VardiyaGun vg = null;
						if (vList.size() > 1) {
							boolean calismaVar = false;
							for (VardiyaGun vardiyaGun : vList) {
								if (!calismaVar)
									calismaVar = vardiyaGun.getVardiya().isCalisma();

							}
							if (calismaVar) {
								List<Integer> list = Arrays.asList(new Integer[] { PuantajKatSayiTipi.GUN_ERKEN_GIRIS_TIPI.value(), PuantajKatSayiTipi.GUN_GEC_CIKIS_TIPI.value() });
								HashMap<PuantajKatSayiTipi, TreeMap<String, BigDecimal>> katSayilarMap = ortak.getYuvarlamaKatSayiMap(basTarih, vardiyaTarih, perIdList, list, pdksDAO);
								if (katSayilarMap != null && katSayilarMap.isEmpty() == false) {
									Long sirketId = null, tesisId = null;
									try {
										Sirket sirket = personel.getSirket();
										if (sirket != null) {
											if (sirket.getTesisDurum())
												tesisId = personel.getTesis() != null ? personel.getTesis().getId() : null;
											sirketId = sirket.getId();
										}

									} catch (Exception e) {
										sirketId = null;
									}
									TreeMap<String, BigDecimal> erkenGirisMap = katSayilarMap != null && katSayilarMap.containsKey(PuantajKatSayiTipi.GUN_ERKEN_GIRIS_TIPI) ? katSayilarMap.get(PuantajKatSayiTipi.GUN_ERKEN_GIRIS_TIPI) : null;
									TreeMap<String, BigDecimal> gecCikisMap = katSayilarMap != null && katSayilarMap.containsKey(PuantajKatSayiTipi.GUN_GEC_CIKIS_TIPI) ? katSayilarMap.get(PuantajKatSayiTipi.GUN_GEC_CIKIS_TIPI) : null;
									for (Iterator iterator = vList.iterator(); iterator.hasNext();) {
										VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
										Vardiya vardiya = vardiyaGun.getVardiya();
										if (vardiya.isCalisma()) {
											HashMap<Integer, BigDecimal> katSayiMap = new HashMap<Integer, BigDecimal>();
											String str = vardiyaGun.getVardiyaDateStr();
											Long vardiyaId = null;
											try {
												vardiyaId = vardiya != null ? vardiya.getId() : null;
											} catch (Exception e) {
												vardiyaId = null;
											}
											if (erkenGirisMap != null && ortak.veriKatSayiVar(erkenGirisMap, sirketId, tesisId, vardiyaId, str)) {
												BigDecimal deger = ortak.getKatSayiVeriMap(erkenGirisMap, sirketId, tesisId, vardiyaId, str);
												if (deger != null)
													katSayiMap.put(PuantajKatSayiTipi.GUN_ERKEN_GIRIS_TIPI.value(), deger);
											}
											if (gecCikisMap != null && ortak.veriKatSayiVar(gecCikisMap, sirketId, tesisId, vardiyaId, str)) {
												BigDecimal deger = ortak.getKatSayiVeriMap(gecCikisMap, sirketId, tesisId, vardiyaId, str);
												if (deger != null)
													katSayiMap.put(PuantajKatSayiTipi.GUN_ERKEN_GIRIS_TIPI.value(), deger);
											}
											if (!katSayiMap.isEmpty()) {
												vardiya.setKatSayiMap(katSayiMap);
												vardiya.setIslemVardiyaGun(vardiyaGun);
												vardiyaGun.setKatSayiMap(katSayiMap);
											} else
												katSayiMap = null;
										}
										gecCikisMap = null;
										erkenGirisMap = null;
									}
								}
								katSayilarMap = null;
							}

							for (Iterator iterator = vList.iterator(); iterator.hasNext();) {
								VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
								Vardiya vardiya = vardiyaGun.getIslemVardiya();
								if (vardiya.isCalisma() && PdksUtil.tarihKarsilastirNumeric(vardiyaTarih, islemZamani) == 0) {
									if (vardiya.getKatSayiMap() != null)
										vardiya.setIslemVardiyaGun(vardiyaGun);
									if (islemZamani.before(vardiya.getVardiyaTelorans1BasZaman()) == false && islemZamani.after(vardiya.getVardiyaTelorans2BitZaman()) == false)
										vg = vardiyaGun;

								}
							}
						}
						if (vg == null)
							vg = vList.get(0);
						if (vg != null)
							islemVardiya = vg.getIslemVardiya();

					}
					if (islemVardiya != null) {
						LinkedHashMap<String, Object> shiftMap = new LinkedHashMap<String, Object>();
						veriMap.put("shift", shiftMap);
						if (islemVardiya.isCalisma()) {
							islemVardiya = vList.get(0).getIslemVardiya();
							shiftMap.put("adi", islemVardiya.getKisaAdi());
							shiftMap.put("baslangicSaat", islemVardiya.getBasSaat());
							shiftMap.put("baslangicDakika", islemVardiya.getBasDakika());
							shiftMap.put("bitisSaat", islemVardiya.getBitSaat());
							shiftMap.put("bitisDakika", islemVardiya.getBitDakika());
							shiftMap.put("baslangicZamani", PdksUtil.convertToDateString(islemVardiya.getBasZaman(), "yyyy-MM-dd HH:ss"));
							shiftMap.put("bitisZamani", PdksUtil.convertToDateString(islemVardiya.getBitZaman(), "yyyy-MM-dd HH:ss"));
						} else
							shiftMap.put("hata", tarih + " çalışma planlı değildir ");
						shiftMap.put("durum", islemVardiya.isCalisma());
					} else
						veriMap.put("shiftHata", "Shift bilgisi bulunamadı " + tarih);

				} else
					veriMap.put("shiftHata", "Hatalı tarih format " + tarih);
			}
		} else {
			if (mySQLPersonel != null) {
				veriMap.put("sicilNo", sicilNo);
				if (PdksUtil.hasStringValue(mySQLPersonel.getKimlikNo()))
					veriMap.put("kimlikNo", mySQLPersonel.getKimlikNo());
				veriMap.put("adi", mySQLPersonel.getAdi());
				veriMap.put("soyadi", mySQLPersonel.getSoyadi());
				hata = "Personel tanımı bulunamadı!";
			} else {
				veriMap.put("sicilNo", sicilNo);
				if (PdksUtil.hasStringValue(kimlikNo))
					veriMap.put("kimlikNo", kimlikNo);
				if (hata == null)
					hata = "Kayıt bulunamadı!";
			}

		}
		veriMap.put("durum", hata == null);
		if (hata != null)
			veriMap.put("hata", hata);
		String sonuc = gson.toJson(veriMap);
		String mediaType = MediaType.APPLICATION_JSON;
		response = Response.ok(sonuc).type(mediaType + ";charset=utf-8").build();
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
	 "tipi": 1,
	 "durum": 1
	 },
	 {
	 "id": 2,
	 "cihazId": 2,
	 "personelId": 2,
	 "tarih": "20241124",
	 "saat": "1135",
	 "tipi": 1,
	 "durum": 1
	 },
	 {
	 "id": 3,
	 "cihazId": 2,
	 "personelId": 2,
	 "tarih": "20241124",
	 "saat": "1645",
	 "tipi": 2,
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
			CihazUser cihazUser = getCihazUser();
			if (cihazUser != null) {
				String json = PdksRestFulVeriAktarService.getBodyString(request);
				List<LinkedTreeMap> dataList = gson.fromJson(json, List.class);
				if (!dataList.isEmpty()) {
					List<CihazGecis> gecisList = new ArrayList<CihazGecis>();
					for (LinkedTreeMap linkedTreeMap : dataList) {
						json = gson.toJson(linkedTreeMap);
						CihazGecis cihazGecis = gson.fromJson(json, CihazGecis.class);
						gecisList.add(cihazGecis);
					}
					CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
					sonuc = getKullaniciHatali(cihazVeriOrtakAktar.saveCihazGecis(gecisList, cihazUser).getHata());
					gecisList = null;
				} else
					sonuc = getKullaniciHatali("Cihaz geçiş yok!");
				dataList = null;
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
	private CihazUser getCihazUser() {
		CihazUser user = null;
		String username = null, password = null;
		for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
			String nextHeaderName = (String) e.nextElement();
			String headerValue = request.getHeader(nextHeaderName);
			if (nextHeaderName.equals("username"))
				username = headerValue;
			else if (nextHeaderName.equals("password"))
				password = headerValue;
		}
		if (username != null && password != null)
			user = new CihazUser(username, password);

		return user;
	}
}
