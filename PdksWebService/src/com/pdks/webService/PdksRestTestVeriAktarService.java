package com.pdks.webService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.pdks.dao.PdksDAO;
import org.pdks.entity.IzinReferansERP;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.genel.model.Constants;
import org.pdks.genel.model.PdksUtil;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
@Path("/servicesTestPDKS")
// @WSDLDocumentation("http://localhost:8080/PdksWebService/rest/servicesTestPDKS")
public class PdksRestTestVeriAktarService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6045365692246632993L;

	public Logger logger = Logger.getLogger(PdksRestTestVeriAktarService.class);

	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;

	private Gson gson = new Gson();

	private PdksDAO pdksDAO;

	@GET
	@Path("/getJSONPersonel")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getJSONPersonel(@QueryParam("sirketKodu") String sirketKodu, @QueryParam("personelNo") String personelNo, @QueryParam("tarih") String tarih) throws Exception {
		String mediaType = MediaType.APPLICATION_JSON;
		Response response = getPersonelPDKS(sirketKodu, personelNo, tarih, null, mediaType);
		return response;
	}

	@GET
	@Path("/getXMLPersonel")
	@Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_XML + ";charset=utf-8")
	public Response getXMLPersonel(@QueryParam("sirketKodu") String sirketKodu, @QueryParam("personelNo") String personelNo, @QueryParam("tarih") String tarih) throws Exception {
		String mediaType = MediaType.APPLICATION_XML;
		Response response = getPersonelPDKS(sirketKodu, personelNo, tarih, "GetXMLPersonel", mediaType);
		return response;
	}

	@GET
	@Path("/getJSONIzin")
	@Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getJSONIzin(@QueryParam("sirketKodu") String sirketKodu, @QueryParam("personelNo") String personelNo, @QueryParam("tarih") String tarih) throws Exception {
		String mediaType = MediaType.APPLICATION_JSON;
		Response response = getIzinPDKS(sirketKodu, personelNo, tarih, null, mediaType);
		return response;
	}

	@GET
	@Path("/getXMLIzin")
	@Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
	@Consumes(MediaType.APPLICATION_XML + ";charset=utf-8")
	public Response getXMLIzin(@QueryParam("sirketKodu") String sirketKodu, @QueryParam("personelNo") String personelNo, @QueryParam("tarih") String tarih) throws Exception {
		String mediaType = MediaType.APPLICATION_XML;
		Response response = getIzinPDKS(sirketKodu, personelNo, tarih, "GetXMLIzin", mediaType);
		return response;
	}

	/**
	 * @param sirketKodu
	 * @param personelNo
	 * @param tarih
	 * @param fonksiyonAdi
	 * @param mediaType
	 * @return
	 * @throws Exception
	 */
	private Response getIzinPDKS(String sirketKodu, String personelNo, String tarih, String fonksiyonAdi, String mediaType) throws Exception {
		Response response = null;
		HashMap fields = new HashMap();
		Date zaman = null;
		boolean perNoVar = PdksUtil.hasStringValue(personelNo);
		StringBuffer sb = new StringBuffer();
		sb.append("select R.* from " + PersonelIzin.TABLE_NAME + " I " + PdksVeriOrtakAktar.getSelectLOCK());
		sb.append(" inner join " + IzinReferansERP.TABLE_NAME + " R " + PdksVeriOrtakAktar.getJoinLOCK() + " on R." + IzinReferansERP.COLUMN_NAME_IZIN_ID + " = I." + PersonelIzin.COLUMN_NAME_ID);
		sb.append(" inner join " + Personel.TABLE_NAME + " P " + PdksVeriOrtakAktar.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = I." + PersonelIzin.COLUMN_NAME_PERSONEL);
		if (perNoVar) {
			sb.append(" and P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " = :p");
			fields.put("p", personelNo);
		} else {
			zaman = PdksUtil.getDateFromString(tarih);
			sb.append(" inner join " + Sirket.TABLE_NAME + " S " + PdksVeriOrtakAktar.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
			sb.append(" and S." + Sirket.COLUMN_NAME_ERP_DURUM + " = 1");
			if (PdksUtil.hasStringValue(sirketKodu)) {
				sb.append(" and S." + Sirket.COLUMN_NAME_ERP_KODU + " = :k");
				fields.put("k", sirketKodu);
			}
		}

		sb.append(" inner join " + IzinTipi.TABLE_NAME + " T " + PdksVeriOrtakAktar.getJoinLOCK() + " on T." + IzinTipi.COLUMN_NAME_ID + " = I." + PersonelIzin.COLUMN_NAME_IZIN_TIPI);
		sb.append(" and T." + IzinTipi.COLUMN_NAME_BAKIYE_IZIN_TIPI + " is null");
		if (zaman != null) {
			sb.append(" where I." + PersonelIzin.COLUMN_NAME_GUNCELLEME_TARIHI + " >= :g1 or I." + PersonelIzin.COLUMN_NAME_OLUSTURMA_TARIHI + " >= :g2");
			fields.put("g1", zaman);
			fields.put("g2", zaman);
		}
		List<IzinERP> izinERPList = new ArrayList<IzinERP>();
		pdksDAO = Constants.pdksDAO;
		List<IzinReferansERP> izinList = pdksDAO.getNativeSQLList(fields, sb, IzinReferansERP.class);
		for (IzinReferansERP izinReferansERP : izinList) {
			PersonelIzin personelIzin = izinReferansERP.getIzin();
			Tanim izinTipiTanim = personelIzin.getIzinTipi() != null ? personelIzin.getIzinTipi().getIzinTipiTanim() : null;
			if (izinTipiTanim != null) {
				String aciklama = personelIzin.getAciklama();
				if (aciklama != null && aciklama.lastIndexOf("(") > 0)
					aciklama = aciklama.substring(0, aciklama.lastIndexOf("(")).trim();
				IzinERP izin = new IzinERP();
				izin.setReferansNoERP(izinReferansERP.getId());
				izin.setPersonelNo(personelIzin.getPersonelNo());
				izin.setBasZaman(PdksUtil.convertToDateString(personelIzin.getBaslangicZamani(), PdksVeriOrtakAktar.FORMAT_DATE_TIME));
				izin.setBitZaman(PdksUtil.convertToDateString(personelIzin.getBitisZamani(), PdksVeriOrtakAktar.FORMAT_DATE_TIME));
				izin.setIzinSuresi(personelIzin.getIzinSuresi());
				izin.setAciklama(aciklama);
				izin.setIzinTipi(izinTipiTanim.getErpKodu());
				izin.setIzinTipiAciklama(izinTipiTanim.getAciklama());
				izin.setDurum(personelIzin.getIzinDurumu() == PersonelIzin.IZIN_DURUMU_ONAYLANDI);
				izin.setSureBirimi(SureBirimi.GUN);
				izin.setYazildi(null);
				izin.setGuncellemeZamani(PdksUtil.convertToDateString(personelIzin.getSonIslemTarihi(), PdksVeriOrtakAktar.FORMAT_DATE_TIME));
				izinERPList.add(izin);
			}

		}
		Gson gson = new Gson();
		String sonuc = gson.toJson(izinERPList);
		if (fonksiyonAdi != null)
			sonuc = PdksRestFulVeriAktarService.convertJSONtoXML(fonksiyonAdi, sonuc);

		response = Response.ok(sonuc).type(mediaType + ";charset=utf-8").build();

		return response;
	}

	/**
	 * @param sirketKodu
	 * @param personelNo
	 * @param tarih
	 * @param fonksiyonAdi
	 * @param mediaType
	 * @return
	 * @throws Exception
	 */
	private Response getPersonelPDKS(String sirketKodu, String personelNo, String tarih, String fonksiyonAdi, String mediaType) throws Exception {
		Response response = null;
		HashMap fields = new HashMap();
		boolean perNoVar = PdksUtil.hasStringValue(personelNo);
		Date zaman = perNoVar == false ? PdksUtil.getDateFromString(tarih) : null;
		StringBuffer sb = new StringBuffer();
		sb.append("select P.* from " + Personel.TABLE_NAME + " P " + PdksVeriOrtakAktar.getSelectLOCK());
		sb.append(" inner join " + Sirket.TABLE_NAME + " S " + PdksVeriOrtakAktar.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
		sb.append(" and S." + Sirket.COLUMN_NAME_ERP_DURUM + " = 1");
		if (PdksUtil.hasStringValue(sirketKodu) && perNoVar == false) {
			sb.append(" and S." + Sirket.COLUMN_NAME_ERP_KODU + " = :k");
			fields.put("k", sirketKodu);
		}
		if (perNoVar) {
			sb.append(" where P." + Personel.COLUMN_NAME_PDKS_SICIL_NO + " = :p");
			fields.put("p", personelNo);
		} else if (zaman != null) {
			sb.append(" where P." + Personel.COLUMN_NAME_GUNCELLEME_TARIHI + " >= :g1 or P." + PersonelIzin.COLUMN_NAME_OLUSTURMA_TARIHI + " >= :g2");
			fields.put("g1", zaman);
			fields.put("g2", zaman);
		}
		List<PersonelERP> personelERPList = new ArrayList<PersonelERP>();
		pdksDAO = Constants.pdksDAO;
		List<Personel> personelList = pdksDAO.getNativeSQLList(fields, sb, Personel.class);
		for (Personel personel : personelList) {
			PersonelERP personelERP = new PersonelERP();
			personelERP.setAdi(personel.getAd());
			personelERP.setSoyadi(personel.getSoyad());
			personelERP.setPersonelNo(personel.getPdksSicilNo());
			personelERP.setSirketAdi(personel.getSirket().getAd());
			personelERP.setSirketKodu(personel.getSirket().getErpKodu());
			personelERP.setIseGirisTarihi(PdksUtil.convertToDateString(personel.getIseBaslamaTarihi(), PdksVeriOrtakAktar.FORMAT_DATE));
			personelERP.setIstenAyrilmaTarihi(PdksUtil.convertToDateString(personel.getIstenAyrilisTarihi(), PdksVeriOrtakAktar.FORMAT_DATE));
			personelERP.setDogumTarihi(PdksUtil.convertToDateString(personel.getDogumTarihi(), PdksVeriOrtakAktar.FORMAT_DATE));
			personelERP.setKidemTarihi(PdksUtil.convertToDateString(personel.getIzinHakEdisTarihi(), PdksVeriOrtakAktar.FORMAT_DATE));
			if (personel.getCinsiyet() != null) {
				personelERP.setCinsiyeti(personel.getCinsiyet().getAciklama());
				personelERP.setCinsiyetKodu(personel.getCinsiyet().getErpKodu());
			}
			if (personel.getMasrafYeri() != null) {
				personelERP.setMasrafYeriAdi(personel.getMasrafYeri().getAciklama());
				personelERP.setMasrafYeriKodu(personel.getMasrafYeri().getErpKodu());
			}
			if (personel.getGorevTipi() != null) {
				personelERP.setGorevi(personel.getGorevTipi().getAciklama());
				personelERP.setGorevKodu(personel.getGorevTipi().getErpKodu());
			}
			if (personel.getEkSaha1() != null) {
				personelERP.setDepartmanAdi(personel.getEkSaha1().getAciklama());
				personelERP.setDepartmanKodu(personel.getEkSaha1().getErpKodu());
			}
			if (personel.getEkSaha3() != null) {
				personelERP.setBolumAdi(personel.getEkSaha3().getAciklama());
				personelERP.setBolumKodu(personel.getEkSaha3().getErpKodu());
			}
			if (personel.getEkSaha4() != null) {
				personelERP.setBordroAltAlanAdi(personel.getEkSaha4().getAciklama());
				personelERP.setBordroAltAlanKodu(personel.getEkSaha4().getErpKodu());
			}
			if (personel.getTesis() != null) {
				personelERP.setTesisAdi(personel.getTesis().getAciklama());
				personelERP.setTesisKodu(personel.getTesis().getKodu());
			}
			personelERP.setSanalPersonel(personel.getSanalPersonel());
			personelERP.setYoneticiPerNo(personel.getYoneticisi() != null ? personel.getYoneticisi().getPdksSicilNo() : "");
			personelERP.setKimlikNo(personel.getPersonelKGS().getKimlikNo());
			personelERP.setGuncellemeZamani(PdksUtil.convertToDateString(personel.getSonIslemTarihi(), PdksVeriOrtakAktar.FORMAT_DATE_TIME));
			personelERP.setYazildi(null);
			personelERPList.add(personelERP);

		}
		Gson gson = new Gson();
		String sonuc = gson.toJson(personelERPList);
		if (fonksiyonAdi != null)
			sonuc = PdksRestFulVeriAktarService.convertJSONtoXML(fonksiyonAdi, sonuc);

		response = Response.ok(sonuc).type(mediaType + ";charset=utf-8").build();
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

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public PdksDAO getPdksDAO() {
		return pdksDAO;
	}

	public void setPdksDAO(PdksDAO pdksDAO) {
		this.pdksDAO = pdksDAO;
	}
}
