package com.pdks.webService;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.pdks.dao.PdksDAO;
import org.pdks.dao.impl.BaseDAOHibernate;
import org.pdks.entity.DenklestirmeAy;
import org.pdks.entity.ERPSistem;
import org.pdks.entity.FazlaMesaiERP;
import org.pdks.entity.FazlaMesaiERPDetay;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelMesai;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.TesisBaglanti;
import org.pdks.enums.MethodAPI;
import org.pdks.enums.MethodAlanAPI;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.MailManager;
import org.pdks.genel.model.PdksUtil;
import org.pdks.mail.model.MailFile;
import org.pdks.mail.model.MailObject;
import org.pdks.mail.model.MailPersonel;
import org.pdks.mail.model.MailStatu;
import org.pdks.security.entity.Role;
import org.pdks.security.entity.User;
import org.pdks.security.entity.UserDigerOrganizasyon;
import org.pdks.security.entity.UserRoles;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@Service
@Path("/servicesPDKS")
// @WSDLDocumentation("http://localhost:8080/PdksWebService/rest/servicesPDKS")
public class PdksRestFulVeriAktarService implements Serializable {

	/**
	 * 
	 */

	public static final long serialVersionUID = -2420146759483423027L;

	public Logger logger = Logger.getLogger(PdksRestFulVeriAktarService.class);

	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;

	private Gson gson = new Gson();

	private Integer year, month;

	private FazlaMesaiERP fazlaMesaiERP;

	private PdksDAO pdksDAO;

	/**
	 * @param sirketERPKodu
	 * @param baslikList
	 * @return
	 */
	private LinkedHashMap<String, Object> getBaslikHeaderMap(String sirketERPKodu, List<FazlaMesaiERPDetay> baslikList) {
		LinkedHashMap<String, Object> baslikAlanMap = new LinkedHashMap();
		for (FazlaMesaiERPDetay fmd : baslikList) {
			MethodAlanAPI methodAlanAPI = fmd.getMethodAlanAPI();
			if (methodAlanAPI != null) {
				String key = fmd.getAlanAdi();
				if (methodAlanAPI.equals(MethodAlanAPI.YIL))
					baslikAlanMap.put(key, year);
				else if (methodAlanAPI.equals(MethodAlanAPI.AY))
					baslikAlanMap.put(key, month);
				else if (methodAlanAPI.equals(MethodAlanAPI.SIRKET))
					baslikAlanMap.put(key, sirketERPKodu);
			}
		}
		return baslikAlanMap;
	}

	/**
	 * @param sirketKodu
	 * @param tesisKodu
	 * @param dosyaAdi
	 * @param mesajStr
	 * @throws Exception
	 */
	private void sendIKMail(String sirketKodu, String tesisKodu, String dosyaAdi, String mesajStr) throws Exception {
		HashMap fields = new HashMap();
		StringBuffer sb = new StringBuffer();
		String alanAdi = User.COLUMN_NAME_ID;
		List userFieldList = null;
		LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
		veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_IK_USERNAME_LIST");
		veriMap.put("alanAdi", alanAdi);
		try {
			userFieldList = pdksDAO.execSPList(veriMap, null);
		} catch (Exception e) {
			userFieldList = new ArrayList<Long>();
		}
		HashMap<String, List<User>> userMap = new HashMap<String, List<User>>();
		if (userFieldList.isEmpty() == false) {

			List<Long> longList = PdksUtil.getLongListFromBigDecimal(null, userFieldList);
			HashMap<Long, List<Tanim>> orgMap = new HashMap<Long, List<Tanim>>();
			Tanim tesis = null;
			Sirket sirket = null;
			HashMap<Long, List<Tanim>> tesisBaglantiMap = new HashMap<Long, List<Tanim>>();
			sb = new StringBuffer();
			sb.append("select * from " + UserDigerOrganizasyon.TABLE_NAME + " " + PdksVeriOrtakAktar.getSelectLOCK());
			sb.append(" where " + UserDigerOrganizasyon.COLUMN_NAME_USER + " :b ");
			fields.clear();
			fields.put("b", longList);
			List<UserDigerOrganizasyon> userDigerOrganizasyonList = pdksDAO.getNativeSQLList(fields, sb, UserDigerOrganizasyon.class);
			for (UserDigerOrganizasyon udo : userDigerOrganizasyonList) {
				Long id = udo.getUser().getId();
				List<Tanim> tanimList = orgMap.containsKey(id) ? orgMap.get(id) : new ArrayList<Tanim>();
				if (tanimList.isEmpty())
					orgMap.put(id, tanimList);
				tanimList.add(udo.getOrganizasyon());
			}
			sb = new StringBuffer();
			sb.append("select * from " + TesisBaglanti.TABLE_NAME + " " + PdksVeriOrtakAktar.getSelectLOCK());
			// sb.append(" where " + TesisBaglanti.COLUMN_NAME_TESIS + " = :b ");
			fields.clear();

			List<TesisBaglanti> tesisBaglantiList = pdksDAO.getNativeSQLList(fields, sb, TesisBaglanti.class);
			for (TesisBaglanti tb : tesisBaglantiList) {
				if (tb.getPersonelTipi() != null && tb.isIK() == false)
					continue;
				Long id = tb.getTesis().getId();
				List<Tanim> tanimList = tesisBaglantiMap.containsKey(id) ? tesisBaglantiMap.get(id) : new ArrayList<Tanim>();
				if (tanimList.isEmpty())
					tesisBaglantiMap.put(id, tanimList);
				tanimList.add(tb.getTesisBaglanti());
			}

			tesisBaglantiList = null;
			if (PdksUtil.hasStringValue(sirketKodu)) {
				fields.clear();
				sb = new StringBuffer();
				sb.append("select * from " + Sirket.TABLE_NAME + " " + PdksVeriOrtakAktar.getSelectLOCK());
				sb.append(" where " + Sirket.COLUMN_NAME_ERP_KODU + " = :b ");
				fields.put("b", sirketKodu);
				List<Sirket> sirketList = pdksDAO.getNativeSQLList(fields, sb, Sirket.class);
				if (sirketList.isEmpty() == false) {
					sirket = sirketList.get(0);
					if (PdksUtil.hasStringValue(tesisKodu)) {
						fields.clear();
						sb = new StringBuffer();
						sb.append("select * from " + Tanim.TABLE_NAME + " " + PdksVeriOrtakAktar.getSelectLOCK());
						sb.append(" where " + Tanim.COLUMN_NAME_TIPI + " = :t ");
						sb.append(" and ( " + Tanim.COLUMN_NAME_KODU + " = :k1 or " + Tanim.COLUMN_NAME_KODU + " = :k2 ) ");
						fields.put("t", Tanim.TIPI_TESIS);
						fields.put("k1", tesisKodu);
						fields.put("k2", sirketKodu + "-" + tesisKodu);
						List<Tanim> tesisList = pdksDAO.getNativeSQLList(fields, sb, Tanim.class);
						if (tesisList.isEmpty() == false)
							tesis = tesisList.get(0);
						tesisList = null;

					}
				}
				sirketList = null;
			}

			sb = new StringBuffer();
			sb.append("select * from " + UserRoles.TABLE_NAME + " " + PdksVeriOrtakAktar.getSelectLOCK());
			sb.append(" where " + UserRoles.COLUMN_NAME_USER + " :b ");
			fields.clear();
			fields.put("b", longList);
			List<UserRoles> userRolesList = pdksDAO.getNativeSQLList(fields, sb, UserRoles.class);
			longList = null;
			for (UserRoles userRoles : userRolesList) {
				Role role = userRoles.getRole();
				Personel personel = userRoles.getUser().getPdksPersonel();
				String key = role.getRolename();
				List<Tanim> tesisList = null;
				if (key.equals(Role.TIPI_IK_SIRKET)) {
					key = key + "_" + personel.getSirket().getId();
				} else if (key.equals(Role.TIPI_IK_Tesis)) {
					if (personel.getTesis() != null) {
						long tesisId = personel.getTesis().getId(), userId = userRoles.getUser().getId();
						tesisList = new ArrayList<Tanim>();
						tesisList.add(personel.getTesis());
						if (tesisBaglantiMap.containsKey(tesisId))
							tesisList.addAll(tesisBaglantiMap.get(tesisId));
						if (orgMap.containsKey(userId))
							tesisList.addAll(orgMap.get(userId));
						key = key + "_" + personel.getTesis().getId();
					}

					else
						key = null;

				} else if (key.equals(Role.TIPI_IK) == false)
					key = null;
				if (key != null) {
					User user = userRoles.getUser();
					if (tesisList == null) {
						List<User> list = userMap.containsKey(key) ? userMap.get(key) : new ArrayList<User>();
						if (list.isEmpty())
							userMap.put(key, list);
						list.add(user);
					} else {
						for (Tanim tesisUser : tesisList) {
							key = Role.TIPI_IK_Tesis + "_" + tesisUser.getId();
							List<User> list = userMap.containsKey(key) ? userMap.get(key) : new ArrayList<User>();
							if (list.isEmpty())
								userMap.put(key, list);
							list.add(user);
						}
					}

				}
			}
			userRolesList = null;
			tesisBaglantiMap = null;
			orgMap = null;
			if (userMap.isEmpty() == false) {

				String key = null;
				if (tesis != null) {
					key = Role.TIPI_IK_Tesis + "_" + tesis.getId();
					if (userMap.containsKey(key) == false)
						key = null;

				}
				if (sirket != null && key == null) {
					key = Role.TIPI_IK_SIRKET + "_" + sirket.getId();
					if (userMap.containsKey(key) == false)
						key = null;
				}
				if (key == null)
					key = Role.TIPI_IK;
				if (userMap.containsKey(key)) {
					MailObject mailObject = new MailObject();
					List<User> userList = userMap.get(key);
					PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
					for (User user : userList) {
						MailPersonel mailPersonel = new MailPersonel();
						mailPersonel.setAdiSoyadi(user.getPdksPersonel().getAdSoyad());
						String ePosta = user.getEmail();
						if (ortakAktar.getTestDurum())
							ePosta = "hasansayar58@gmail.com";
						mailPersonel.setePosta(ePosta);

						mailObject.getToList().add(mailPersonel);
					}

					HashMap<String, Object> mailMap = ortakAktar.sistemVerileriniYukle(pdksDAO, true);
					DenklestirmeAy denklestirmeAy = new DenklestirmeAy();
					denklestirmeAy.setAy(month);
					denklestirmeAy.setYil(year);
					String subject = denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " " + (sirket != null ? sirket.getAd() + " " : "") + "fazla mesai yükleme";
					String body = denklestirmeAy.getAyAdi() + " " + denklestirmeAy.getYil() + " dönemi " + (sirket != null ? sirket.getAd() + " " : "") + " fazla mesai dosyası " + dosyaAdi + " ektedir.";

					mailObject.setBody(body);
					mailObject.setSubject(subject);
					MailFile mailFile = new MailFile();
					mailFile.setDisplayName(dosyaAdi);
					mailFile.setIcerik(PdksUtil.getBytesUTF8(mesajStr));
					mailObject.getAttachmentFiles().add(mailFile);

					mailMap.put("mailObject", mailObject);
					MailManager.ePostaGonder(mailMap);
					mailMap = null;
				}

			}

			userMap = null;

		}
		userFieldList = null;

	}

	/**
	 * @param sirketKodu
	 * @param yil
	 * @param ay
	 * @param tesisKodu
	 * @param fonksiyonAdi
	 * @param mediaType
	 * @return
	 * @throws Exception
	 */
	private Response getMesaiPDKS(String sirketKodu, Integer yil, Integer ay, String tesisKodu, String fonksiyonAdi, String mediaType) throws Exception {
		Response response = null;

		List<String> perNoList = null;

		try {
			if (yil != null && ay != null)
				response = getFazlaMesaiVeri(fonksiyonAdi, mediaType, MethodAPI.GET.value(), yil, ay, sirketKodu, tesisKodu, perNoList);
		} catch (Exception e) {
		}
		return response;
	}

	@GET
	@Path("/getJSONMesaiPDKS")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getJSONMesaiPDKS(@QueryParam("sirketKodu") String sirketKodu, @QueryParam("yil") Integer yil, @QueryParam("ay") Integer ay, @QueryParam("tesisKodu") String tesisKodu) throws Exception {
		String mediaType = MediaType.APPLICATION_JSON;
		Response response = getMesaiPDKS(sirketKodu, yil, ay, tesisKodu, "getJSONMesaiPDKS", mediaType);
		return response;
	}

	@GET
	@Path("/getXMLMesaiPDKS")
	@Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_XML + ";charset=utf-8")
	public Response getXMLMesaiPDKS(@QueryParam("sirketKodu") String sirketKodu, @QueryParam("yil") Integer yil, @QueryParam("ay") Integer ay, @QueryParam("tesisKodu") String tesisKodu) throws Exception {
		String mediaType = MediaType.APPLICATION_XML;
		Response response = getMesaiPDKS(sirketKodu, yil, ay, tesisKodu, "getXMLMesaiPDKS", mediaType);
		return response;
	}

	@POST
	@Path("/postJSONMesaiPDKS")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	/**

	 {
	 "yil": 2025,
	 "ay": 10,
	 "sirketKodu": "1",
	 "tesisKodu": "1-100001",
	 "personelNo": [
	 "SU00001",
	 "SU00002",
	 "SU00003",
	 "SU00004",
	 "SU00006"
	 ]
	 }


	 */
	public Response postJSONMesaiPDKS() throws Exception {
		String mediaType = MediaType.APPLICATION_JSON;
		Response response = postMesaiPDKS("postJSONMesaiPDKS", mediaType);
		return response;
	}

	@POST
	@Path("/postXMLMesaiPDKS")
	@Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_XML + ";charset=utf-8")
	/**	
	
	 <sirketKodu>1</sirketKodu>
	 <ay>10</ay>
	 <yil>2025</yil>
	 <personelNo>SU00001</personelNo>
	 <personelNo>SU00002</personelNo>
	 <personelNo>SU00003</personelNo>
	 <personelNo>SU00004</personelNo>
	 <personelNo>SU00006</personelNo>
	 <tesisKodu>1-100001</tesisKodu>
	 **/
	public Response postXMLMesaiPDKS() throws Exception {
		String mediaType = MediaType.APPLICATION_XML;
		Response response = postMesaiPDKS("postXMLMesaiPDKS", mediaType);
		return response;
	}

	/**
	 * @param fonksiyonAdi
	 * @param mediaType
	 * @return
	 * @throws Exception
	 */
	private Response postMesaiPDKS(String fonksiyonAdi, String mediaType) throws Exception {
		Response response = null;
		String data = getBodyString(request);
		if (data != null) {
			try {
				Integer yil = null, ay = null;
				String sirketKodu = null, tesisKodu = null;
				List<String> perNoList = null;
				if (mediaType.equals(MediaType.APPLICATION_JSON)) {
					LinkedTreeMap<String, Object> jsonMap = gson.fromJson(data, LinkedTreeMap.class);
					yil = ((Double) jsonMap.get("yil")).intValue();
					ay = ((Double) jsonMap.get("ay")).intValue();
					sirketKodu = (String) jsonMap.get("sirketKodu");
					tesisKodu = (String) jsonMap.get("tesisKodu");
					perNoList = jsonMap.containsKey("personelNo") ? (List<String>) jsonMap.get("personelNo") : null;

				} else {
					JSONObject jsonMap = XML.toJSONObject(data);
					yil = ((Long) jsonMap.get("yil")).intValue();
					ay = ((Long) jsonMap.get("ay")).intValue();
					if (jsonMap.isNull("sirketKodu") == false)
						sirketKodu = "" + jsonMap.get("sirketKodu");
					if (jsonMap.isNull("tesisKodu") == false)
						tesisKodu = "" + jsonMap.get("tesisKodu");
					if (jsonMap.isNull("personelNo") == false) {
						perNoList = new ArrayList<String>();
						JSONArray array = (JSONArray) jsonMap.get("personelNo");
						for (int i = 0; i < array.length(); i++) {
							String personelNo = array.getString(i);
							perNoList.add(personelNo);
						}
					}
				}

				if (yil != null && ay != null) {
					response = getFazlaMesaiVeri(fonksiyonAdi, mediaType, MethodAPI.POST.value(), yil, ay, sirketKodu, tesisKodu, perNoList);

				}
			} catch (Exception e) {
				logger.error(e);
				e.getMessage();
			}

		}
		return response;
	}

	/**
	 * @param fonksiyonAdi
	 * @param mediaType
	 * @param method
	 * @param yil
	 * @param ay
	 * @param sirketKoduInput
	 * @param tesisKoduInput
	 * @param perNoList
	 * @return
	 */
	private Response getFazlaMesaiVeri(String fonksiyonAdi, String mediaType, String method, Integer yil, Integer ay, String sirketKoduInput, String tesisKoduInput, List<String> perNoList) {

		Response response;
		String sonuc = "";

		if (PdksUtil.hasStringValue(sirketKoduInput) == false) {
			sirketKoduInput = null;
			tesisKoduInput = null;
		}
		boolean tesisVar = PdksUtil.hasStringValue(tesisKoduInput), sirketVar = PdksUtil.hasStringValue(sirketKoduInput);

		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		try {
			if (yil >= 2020 && ay > 0 && ay < 13) {
				year = yil;
				month = ay;
				pdksDAO = Constants.pdksDAO;
				StringBuffer sb = new StringBuffer();
				sb.append("select A.* from " + Parameter.TABLE_NAME + " P " + PdksVeriOrtakAktar.getSelectLOCK());
				sb.append(" inner join " + ERPSistem.TABLE_NAME + " E " + PdksVeriOrtakAktar.getJoinLOCK() + " on E." + ERPSistem.COLUMN_NAME_ERP_SIRKET + " = P." + Parameter.COLUMN_NAME_DEGER);
				sb.append(" and E." + ERPSistem.COLUMN_NAME_DURUM + " = 1");
				sb.append(" inner join " + FazlaMesaiERP.TABLE_NAME + " A " + PdksVeriOrtakAktar.getJoinLOCK() + " on A." + FazlaMesaiERP.COLUMN_NAME_ERP_SISTEM + " = E." + ERPSistem.COLUMN_NAME_ID);
				sb.append(" where P." + Parameter.COLUMN_NAME_ADI + " = :b and P." + Parameter.COLUMN_NAME_DURUM + " = 1");
				HashMap fields = new HashMap();
				fields.put("b", "uygulamaBordro");
				List<FazlaMesaiERP> fazlaMesaiERPList = pdksDAO.getNativeSQLList(fields, sb, FazlaMesaiERP.class);
				boolean devam = true;
				if (fazlaMesaiERPList.isEmpty() == false) {
					fields.clear();
					fazlaMesaiERP = fazlaMesaiERPList.get(0);
					List<FazlaMesaiERPDetay> detayList = null;
					if (PdksUtil.hasStringValue(fazlaMesaiERP.getServerURL())) {
						sb = new StringBuffer();
						sb.append("select A.* from " + FazlaMesaiERPDetay.TABLE_NAME + " A " + PdksVeriOrtakAktar.getSelectLOCK());
						sb.append(" where A." + FazlaMesaiERPDetay.COLUMN_NAME_FAZLA_MESAI_ERP + " = " + fazlaMesaiERP.getId());
						sb.append(" order by A." + FazlaMesaiERPDetay.COLUMN_NAME_SIRA);
						detayList = pdksDAO.getNativeSQLList(fields, sb, FazlaMesaiERPDetay.class);
					}
					if (detayList != null && detayList.isEmpty() == false) {
						Sirket sirket = null;
						if (PdksUtil.hasStringValue(sirketKoduInput)) {
							sb = new StringBuffer();
							sb.append("select A.* from " + Sirket.TABLE_NAME + " A " + PdksVeriOrtakAktar.getSelectLOCK());
							sb.append(" where A." + Sirket.COLUMN_NAME_ERP_KODU + " = :s ");
							fields.put("s", sirketKoduInput);
							List<Sirket> sirketList = pdksDAO.getNativeSQLList(fields, sb, Sirket.class);
							sirket = sirketList.isEmpty() ? null : sirketList.get(0);
							sirketList = null;
						}

						Tanim tesis = null;
						if (sirket != null && PdksUtil.hasStringValue(tesisKoduInput)) {
							fields.clear();
							sb = new StringBuffer();
							sb.append("select A.* from " + Tanim.TABLE_NAME + " A " + PdksVeriOrtakAktar.getSelectLOCK());
							sb.append(" where A." + Tanim.COLUMN_NAME_TIPI + " = :t ");
							sb.append(" and ( A." + Tanim.COLUMN_NAME_ERP_KODU + " = :k1 or A." + Tanim.COLUMN_NAME_ERP_KODU + " = :k2 )");
							fields.put("t", Tanim.TIPI_TESIS);
							fields.put("k1", tesisKoduInput);
							fields.put("k2", sirketKoduInput + "-" + tesisKoduInput);
							List<Tanim> tesisList = pdksDAO.getNativeSQLList(fields, sb, Tanim.class);
							tesis = tesisList.isEmpty() ? null : tesisList.get(0);
							tesisList = null;
						}
						HashMap<Long, LinkedHashMap<String, Double>> pdMap = new HashMap<Long, LinkedHashMap<String, Double>>();
						LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
						veriMap.put("sirketId", sirket != null ? sirket.getId() : 0L);
						veriMap.put("yil", yil);
						veriMap.put("ay", ay);
						veriMap.put(BaseDAOHibernate.MAP_KEY_SELECT, "SP_GET_FAZLA_MESAI");
						HashMap<Long, Personel> idMap = new HashMap<Long, Personel>();

						List<PersonelMesai> personelMesaiList = pdksDAO.execSPList(veriMap, PersonelMesai.class);
						for (PersonelMesai pm : personelMesaiList) {
							Personel personel = pm.getPersonel();
							if (perNoList != null && perNoList.contains(personel.getPdksSicilNo()) == false)
								continue;
							if (tesis != null && (personel.getTesis() == null || personel.getTesis().getId().equals(tesis.getId())) == false)
								continue;

							if (pm.getErpKodu().equals(MethodAlanAPI.HT.value()) && fazlaMesaiERP.getHtAlanAdi() == null)
								continue;
							Long personelId = personel.getId();
							idMap.put(personelId, pm.getPersonel());
							LinkedHashMap<String, Double> mesaiMap = pdMap.containsKey(personelId) ? pdMap.get(personelId) : new LinkedHashMap<String, Double>();
							if (mesaiMap.isEmpty())
								pdMap.put(personelId, mesaiMap);
							mesaiMap.put(pm.getErpKodu(), pm.getSure());

						}

						boolean baslikAlan = PdksUtil.hasStringValue(fazlaMesaiERP.getBaslikAlanAdi());
						LinkedHashMap<String, String> headerMap = new LinkedHashMap<String, String>();
						List<FazlaMesaiERPDetay> baslikList = new ArrayList<FazlaMesaiERPDetay>();
						boolean uomDetay = true, rtDetay = true, htDetay = true;
						for (FazlaMesaiERPDetay fd : detayList) {
							if (fd.isBaslikAlan())
								baslikList.add(fd);
						}
						baslikAlan = baslikList.isEmpty() == false && PdksUtil.hasStringValue(fazlaMesaiERP.getBaslikAlanAdi()) && PdksUtil.hasStringValue(fazlaMesaiERP.getDetayAlanAdi());
						Object dataMap = null;
						List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

						if (PdksUtil.hasStringValue(fazlaMesaiERP.getRootAdi())) {
							LinkedHashMap<String, Object> verilerMap = new LinkedHashMap<String, Object>();
							if (baslikAlan) {
								LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
								verilerMap.put(fazlaMesaiERP.getRootAdi(), map);
								LinkedHashMap<String, Object> baslikAlanMap = getBaslikHeaderMap(sirketKoduInput, baslikList);
								map.put(fazlaMesaiERP.getBaslikAlanAdi(), baslikAlanMap);
								if (fazlaMesaiERP.isDetayBaslikIcineYazin())
									baslikAlanMap.put(fazlaMesaiERP.getDetayAlanAdi(), dataList);
								else
									map.put(fazlaMesaiERP.getDetayAlanAdi(), dataList);
							} else
								verilerMap.put(fazlaMesaiERP.getRootAdi(), dataList);

							dataMap = verilerMap;
						} else {
							if (baslikAlan) {
								LinkedHashMap<String, Object> veriAlanMap = new LinkedHashMap<String, Object>();
								LinkedHashMap<String, Object> baslikAlanMap = getBaslikHeaderMap(sirketKoduInput, baslikList);
								veriAlanMap.put(fazlaMesaiERP.getBaslikAlanAdi(), baslikAlanMap);
								if (fazlaMesaiERP.isDetayBaslikIcineYazin()) {
									baslikAlanMap.put(fazlaMesaiERP.getDetayAlanAdi(), dataList);
								} else
									veriAlanMap.put(fazlaMesaiERP.getDetayAlanAdi(), dataList);
								dataMap = veriAlanMap;

							} else
								dataMap = dataList;
						}

						if (pdMap.isEmpty() == false) {
							HashMap<String, String> fmTanimMap = new HashMap<String, String>();
							fields.clear();
							sb = new StringBuffer();
							sb.append("select A.* from " + Tanim.TABLE_NAME + " A " + PdksVeriOrtakAktar.getSelectLOCK());
							sb.append(" where A." + Tanim.COLUMN_NAME_TIPI + " = :t and A." + Tanim.COLUMN_NAME_DURUM + " = 1");
							fields.put("t", Tanim.TIPI_ERP_FAZLA_MESAI);

							List<Tanim> fmTanimList = pdksDAO.getNativeSQLList(fields, sb, Tanim.class);

							for (Tanim tanim : fmTanimList)
								fmTanimMap.put(tanim.getErpKodu(), tanim.getKodu());

							for (Iterator iterator = detayList.iterator(); iterator.hasNext();) {
								FazlaMesaiERPDetay fmd = (FazlaMesaiERPDetay) iterator.next();
								MethodAlanAPI methodAlanAPI = fmd.getMethodAlanAPI();
								if (methodAlanAPI == null)
									iterator.remove();
								else if (methodAlanAPI.equals(MethodAlanAPI.USER_NAME) || methodAlanAPI.equals(MethodAlanAPI.PASSWORD)) {
									headerMap.put(fmd.getAlanAdi(), fmd.getAlanDeger());
									iterator.remove();
								} else if (fmd.isBaslikAlan() && baslikAlan) {
									baslikList.add(fmd);
									iterator.remove();
								} else {
									if (methodAlanAPI.equals(MethodAlanAPI.UOM))
										uomDetay = false;
									else if (methodAlanAPI.equals(MethodAlanAPI.RT))
										rtDetay = false;
									else if (methodAlanAPI.equals(MethodAlanAPI.HT))
										htDetay = false;
								}

							}
							baslikAlan = baslikList.isEmpty() == false && PdksUtil.hasStringValue(fazlaMesaiERP.getDetayAlanAdi());
							for (Long personelId : pdMap.keySet()) {
								Personel personelERP = idMap.get(personelId);
								String kimlikNo = personelERP.getPersonelKGS().getKimlikNo();
								String tesisKodu = null, sirketKodu = personelERP.getSirket().getErpKodu();

								if (personelERP.getTesis() != null) {
									tesisKodu = personelERP.getTesis().getErpKodu();
									if (tesisKodu != null && tesisKodu.startsWith(sirketKodu + "-"))
										tesisKodu = tesisKodu.substring(tesisKodu.indexOf("-") + 1);
								}
								LinkedHashMap<String, Object> perMap = new LinkedHashMap<String, Object>();
								LinkedHashMap<String, Double> mesaiMap = pdMap.get(personelId);
								for (FazlaMesaiERPDetay fmd : detayList) {
									MethodAlanAPI methodAlanAPI = fmd.getMethodAlanAPI();
									if (methodAlanAPI != null) {
										String key = fmd.getAlanAdi();
										if (methodAlanAPI.equals(MethodAlanAPI.PERSONEL))
											perMap.put(key, personelERP.getPdksSicilNo());
										else if (methodAlanAPI.equals(MethodAlanAPI.SIRKET))
											perMap.put(key, sirketKodu);
										else if (methodAlanAPI.equals(MethodAlanAPI.TESIS)) {
											if (tesisKodu != null)
												perMap.put(key, tesisKodu);
										} else if (methodAlanAPI.equals(MethodAlanAPI.KIMLIK))
											perMap.put(key, kimlikNo != null ? kimlikNo : "");
										else {
											Double tutar = null;
											if (fmTanimMap.containsKey(fmd.getAlanTipi())) {
												MethodAlanAPI mesaiAlanAPI = MethodAlanAPI.fromValue(fmTanimMap.get(fmd.getAlanTipi()));
												if (mesaiAlanAPI != null) {
													tutar = mesaiMap.containsKey(mesaiAlanAPI.value()) ? mesaiMap.get(mesaiAlanAPI.value()) : 0.0d;
													mesaiMap.remove(mesaiAlanAPI.value());
													if (mesaiAlanAPI.equals(MethodAlanAPI.UOM))
														key = fazlaMesaiERP.getUomAlanAdi();
													else if (mesaiAlanAPI.equals(MethodAlanAPI.RT))
														key = fazlaMesaiERP.getRtAlanAdi();
													else if (fazlaMesaiERP.getHtAlanAdi() != null && mesaiAlanAPI.equals(MethodAlanAPI.HT))
														key = fazlaMesaiERP.getHtAlanAdi();
													perMap.put(key, tutar);
												}
											}
										}

									}

								}
								if (fazlaMesaiERP.isOdenenSaatKolonYaz()) {
									if (mesaiMap.isEmpty() == false) {
										if (uomDetay)
											perMap.put(fazlaMesaiERP.getUomAlanAdi(), mesaiMap.containsKey(MethodAlanAPI.UOM.value()) ? mesaiMap.get(MethodAlanAPI.UOM.value()) : 0.0d);
										if (htDetay && fazlaMesaiERP.getHtAlanAdi() != null)
											perMap.put(fazlaMesaiERP.getHtAlanAdi(), mesaiMap.containsKey(MethodAlanAPI.HT.value()) ? mesaiMap.get(MethodAlanAPI.HT.value()) : 0.0d);
										if (rtDetay)
											perMap.put(fazlaMesaiERP.getRtAlanAdi(), mesaiMap.containsKey(MethodAlanAPI.UOM.value()) ? mesaiMap.get(MethodAlanAPI.UOM.value()) : 0.0d);
									}
									dataList.add(perMap);
								} else {
									boolean detayRootVar = PdksUtil.hasStringValue(fazlaMesaiERP.getDetayKokAdi());
									LinkedHashMap<String, Object> perDataMap = null;
									if (detayRootVar) {
										perDataMap = new LinkedHashMap<String, Object>();
										LinkedHashMap<String, Object> veriDataMap = new LinkedHashMap<String, Object>();
										veriDataMap.putAll(perMap);
										veriDataMap.put(fazlaMesaiERP.getDetayKokAdi(), perDataMap);
										dataList.add(veriDataMap);
									}

									for (String key : mesaiMap.keySet()) {
										if (detayRootVar == false) {
											perDataMap = new LinkedHashMap<String, Object>();
											perDataMap.putAll(perMap);
											dataList.add(perDataMap);
										}
										if (key.equals(MethodAlanAPI.UOM.value()))
											perDataMap.put(fazlaMesaiERP.getUomAlanAdi(), mesaiMap.get(key));
										else if (key.equals(MethodAlanAPI.RT.value()))
											perDataMap.put(fazlaMesaiERP.getRtAlanAdi(), mesaiMap.get(key));
										else if (key.equals(MethodAlanAPI.HT.value()))
											perDataMap.put(fazlaMesaiERP.getHtAlanAdi(), mesaiMap.get(key));

									}
									if (detayRootVar == false)
										dataList.add(perMap);
									perMap = null;
								}
							}
							Gson gson = new Gson();
							sonuc = gson.toJson(dataMap);
							devam = false;
						}

					}

				}
				if (devam) {
					List<MesaiPDKS> list = ortakAktar.getMesaiPDKS(sirketKoduInput, yil, ay, false, tesisKoduInput);
					if (perNoList != null) {
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							MesaiPDKS mesaiPDKS = (MesaiPDKS) iterator.next();
							if (perNoList.contains(mesaiPDKS.getPersonelNo()) == false)
								iterator.remove();

						}
					}
					if (sirketVar || tesisVar) {
						for (MesaiPDKS mesaiPDKS : list) {
							if (sirketVar)
								mesaiPDKS.setSirketKodu(null);
							if (tesisVar)
								mesaiPDKS.setTesisKodu(null);

						}
					}
					sonuc = gson.toJson(list);
				}
			}
		} catch (Exception e) {

			logger.error(e);
			e.printStackTrace();
		}
		String dosyaAdiBaslangic = fonksiyonAdi + "_" + yil + "" + ay + "_" + sirketKoduInput + (PdksUtil.hasStringValue(tesisKoduInput) ? "_" + tesisKoduInput : "");
		String dosyaAdi = dosyaAdiBaslangic + ".json";
		if (mediaType != null && mediaType.equals(MediaType.APPLICATION_XML)) {
			try {
				JSONObject jsonObject = new JSONObject(sonuc);
				String xml = XML.toString(jsonObject);
				sonuc = xml;
				dosyaAdi = dosyaAdiBaslangic + ".xml";
			} catch (Exception e) {
			}
		}
		if (PdksUtil.hasStringValue(sonuc)) {
			try {
				sendIKMail(sirketKoduInput, tesisKoduInput, dosyaAdi, sonuc);
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

		}
		response = Response.ok(sonuc, mediaType).build();
		return response;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/sendMail")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response sendMail() throws Exception {
		MailObject mail = null;
		String sonuc = "";
		String data = getBodyString(request);
		MailStatu mailStatu = null;
		if (data != null) {
			mail = gson.fromJson(data, MailObject.class);
			PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
			try {
				mailStatu = ortakAktar.sendMail(mail);
			} catch (Exception e) {
				mailStatu = new MailStatu();
				if (e.getMessage() != null) {
					String mesaj = e.getMessage();
					if (mesaj.indexOf("{") >= 0)
						PdksUtil.replaceAll(mesaj, "{", "[");
					if (mesaj.indexOf("}") >= 0)
						PdksUtil.replaceAll(mesaj, "}", "]");
					mailStatu.setHataMesai(mesaj);
				}

				else
					mailStatu.setHataMesai("Hata oluştu!");

			}
			sonuc = gson.toJson(mailStatu);
		}
		Response response = null;
		try {
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
	@Path("/helpDeskDate")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response helpDeskDate() throws Exception {
		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		String status = ortakAktar.helpDeskDate();
		HashMap<String, Object> durumMap = new HashMap<String, Object>();
		durumMap.put("dt", status);
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
	@Path("/helpDeskStatus")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response helpDeskStatus() throws Exception {
		PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
		Boolean status = ortakAktar.helpDeskStatus();
		HashMap<String, Object> durumMap = new HashMap<String, Object>();
		durumMap.put("status", status);
		Response response = null;
		try {
			String sonuc = gson.toJson(durumMap);
			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @param izinList
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/saveIzinler")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveIzinler() throws Exception {
		List<IzinERP> izinList = null;
		String sonuc = "";
		String data = getBodyString(request);
		if (data != null) {
			List<LinkedTreeMap> jsonList = gson.fromJson(data, List.class);
			izinList = new ArrayList<IzinERP>();
			for (LinkedTreeMap map : jsonList) {
				String json = gson.toJson(map);
				IzinERP izinERP = gson.fromJson(json, IzinERP.class);
				izinList.add(izinERP);
			}
			jsonList = null;
			if (!izinList.isEmpty()) {
				PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
				ortakAktar.saveIzinler(izinList);
				sonuc = gson.toJson(izinList);
			}
		}
		Response response = null;
		try {
			response = Response.ok(sonuc, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @param personelList
	 * @throws Exception
	 */
	@POST
	@Path("/savePersoneller")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response savePersoneller() throws Exception {
		List<PersonelERP> personelList = null;
		String data = getBodyString(request);
		if (data != null) {
			List<LinkedTreeMap> list3 = gson.fromJson(data, List.class);
			personelList = new ArrayList<PersonelERP>();
			TreeMap<String, PersonelERP> perMap = new TreeMap<String, PersonelERP>();
			TreeMap<String, TreeMap> dataMap = new TreeMap<String, TreeMap>();
			dataMap.put("personelERPMap", perMap);
			for (LinkedTreeMap map : list3) {
				String json = gson.toJson(map);
				try {
					PersonelERP personelERP = gson.fromJson(json, PersonelERP.class);
					personelERP.setYazildi(Boolean.FALSE);
					personelList.add(personelERP);

					perMap.put(personelERP.getPersonelNo(), personelERP);
				} catch (Exception e) {

				}
			}
			if (!personelList.isEmpty()) {
				PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
				ortakAktar.savePersoneller(personelList);

			}

		} else
			personelList = new ArrayList<PersonelERP>();
		data = gson.toJson(personelList);
		Response response = null;
		try {
			response = Response.ok(data, MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public static String getBodyString(HttpServletRequest request) throws Exception {
		String data = PdksUtil.StringToByInputStream(request.getInputStream());
		return data;
	}
}
