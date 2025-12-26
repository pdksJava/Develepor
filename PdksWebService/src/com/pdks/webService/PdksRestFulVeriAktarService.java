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
import org.pdks.dao.PdksDAO;
import org.pdks.dao.impl.BaseDAOHibernate;
import org.pdks.entity.FazlaMesaiERP;
import org.pdks.entity.FazlaMesaiERPDetay;
import org.pdks.entity.Parameter;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelMesai;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.enums.MethodAlanAPI;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.PdksUtil;
import org.pdks.mail.model.MailObject;
import org.pdks.mail.model.MailStatu;
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
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("/getMesaiPDKS")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getMesaiPDKS(@QueryParam("sirketKodu") String sirketKodu, @QueryParam("yil") Integer yil, @QueryParam("ay") Integer ay, @QueryParam("tesisKodu") String tesisKodu) throws Exception {
		Response response = null;
		String sonuc = "";
		try {
			List<MesaiPDKS> list = null;
			if (yil != null && ay != null) {
				PdksVeriOrtakAktar ortakAktar = new PdksVeriOrtakAktar();
				try {
					if (yil >= 2020 && ay > 0 && ay < 13) {
						year = yil;
						month = ay;
						PdksDAO pdksDAO = Constants.pdksDAO;
						StringBuffer sb = new StringBuffer();
						sb.append("select A.* from " + Parameter.TABLE_NAME + " P " + PdksVeriOrtakAktar.getSelectLOCK());
						sb.append(" inner join " + FazlaMesaiERP.TABLE_NAME + " A " + PdksVeriOrtakAktar.getJoinLOCK() + " on A." + FazlaMesaiERP.COLUMN_NAME_ERP_SIRKET + " = P." + Parameter.COLUMN_NAME_DEGER);
						sb.append(" where P." + Parameter.COLUMN_NAME_ADI + " = :b and P." + Parameter.COLUMN_NAME_DURUM + " = 1");
						HashMap fields = new HashMap();
						fields.put("b", "uygulamaBordro");
						List<FazlaMesaiERP> fazlaMesaiERPList = pdksDAO.getNativeSQLList(fields, sb, FazlaMesaiERP.class);
						boolean devam = true;
						if (fazlaMesaiERPList.isEmpty() == false) {
							fields.clear();
							FazlaMesaiERP fazlaMesaiERP = fazlaMesaiERPList.get(0);
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
								if (PdksUtil.hasStringValue(sirketKodu)) {
									sb = new StringBuffer();
									sb.append("select A.* from " + Sirket.TABLE_NAME + " A " + PdksVeriOrtakAktar.getSelectLOCK());
									sb.append(" where A." + Sirket.COLUMN_NAME_ERP_KODU + " = :s ");
									fields.put("s", sirketKodu);
									List<Sirket> sirketList = pdksDAO.getNativeSQLList(fields, sb, Sirket.class);
									sirket = sirketList.isEmpty() ? null : sirketList.get(0);
									sirketList = null;
								}

								Tanim tesis = null;
								if (sirket != null && PdksUtil.hasStringValue(tesisKodu)) {
									sb = new StringBuffer();
									sb.append("select A.* from " + Tanim.TABLE_NAME + " A " + PdksVeriOrtakAktar.getSelectLOCK());
									sb.append(" where A." + Tanim.COLUMN_NAME_TIPI + " = :T ");
									sb.append(" and ( A." + Tanim.COLUMN_NAME_ERP_KODU + " = :k1 or A." + Tanim.COLUMN_NAME_ERP_KODU + " = :k2 )");
									fields.put("t", Tanim.TIPI_TESIS);
									fields.put("k1", tesisKodu);
									fields.put("k2", sirketKodu + "-" + tesisKodu);
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
								Object dataMap = null;
								List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
								String sirketERPKodu = sirketKodu;
								if (PdksUtil.hasStringValue(fazlaMesaiERP.getRootAdi())) {
									LinkedHashMap<String, Object> verilerMap = new LinkedHashMap<String, Object>();
									if (baslikAlan) {
										LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
										verilerMap.put(fazlaMesaiERP.getRootAdi(), map);
										LinkedHashMap<String, Object> baslikAlanMap = getBaslikHeaderMap(sirketERPKodu, baslikList);
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
										LinkedHashMap<String, Object> baslikAlanMap = getBaslikHeaderMap(sirketERPKodu, baslikList);
										if (fazlaMesaiERP.isDetayBaslikIcineYazin())
											baslikAlanMap.put(fazlaMesaiERP.getDetayAlanAdi(), dataList);

									} else
										dataMap = dataList;
								}

								if (pdMap.isEmpty() == false) {
									HashMap<String, String> fmTanimMap = new HashMap<String, String>();
									fields.clear();
									sb = new StringBuffer();
									sb.append("select A.* from " + Tanim.TABLE_NAME + " A " + PdksVeriOrtakAktar.getSelectLOCK());
									sb.append(" where A." + Tanim.COLUMN_NAME_TIPI + " = :T and A." + Tanim.COLUMN_NAME_DURUM + " = 1");
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
										if (personelERP.getTesis() != null) {
											tesisKodu = personelERP.getTesis().getErpKodu();
											if (sirketERPKodu != null && tesisKodu != null && tesisKodu.startsWith(sirketERPKodu + "-"))
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
													perMap.put(key, sirketERPKodu);
												else if (methodAlanAPI.equals(MethodAlanAPI.TESIS))
													perMap.put(key, tesisKodu);
												else if (methodAlanAPI.equals(MethodAlanAPI.KIMLIK))
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
							list = ortakAktar.getMesaiPDKS(sirketKodu, yil, ay, false, tesisKodu);
							sonuc = gson.toJson(list);
						}
					}
				} catch (Exception e) {

					logger.error(e);
					e.printStackTrace();
				}

			}
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
