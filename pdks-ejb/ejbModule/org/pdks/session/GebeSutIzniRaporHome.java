package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDonemselDurum;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.security.entity.User;

@Name("gebeSutIzniRaporHome")
public class GebeSutIzniRaporHome extends EntityHome<PersonelDonemselDurum> implements Serializable {

	private static final long serialVersionUID = -5535004868794021699L;

	static Logger logger = Logger.getLogger(GebeSutIzniRaporHome.class);

	@RequestParameter
	Long personelDonemselDurumId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	List<User> userList;
	@In(required = false)
	FacesMessages facesMessages;

	public static String sayfaURL = "gebeSutIzniRapor";

	private List<PersonelDonemselDurum> personelDonemDurumList;

	private List<SelectItem> mudurlukList, tesisList;

	private Date basTarih, bitTarih;
	
	private Long mudurlukId, tesisId;
	
	private boolean tesisDurum;

	private Session session;

	@Override
	public Object getId() {
		if (personelDonemselDurumId == null) {
			return super.getId();
		} else {
			return personelDonemselDurumId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		bitTarih = PdksUtil.getDate(new Date());
	}

	public String fillMudurlukList() {
		return "";
	}

	public String fillMudurlukTesisList(String tip) {
		return "";
	}

	public String fillTesisList() {
		return "";
	}

	public String fillPersonelDonemselDurumList() {
		return "";
	}

	public String excelListe() {
		try {

			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null) {
				String dosyaAdi = "GebeSutIzniRapor_" + PdksUtil.convertToDateString(basTarih, "yyyyMMdd") +"_" + PdksUtil.convertToDateString(bitTarih, "yyyyMMdd") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}
	
	private ByteArrayOutputStream excelDevam() {
		boolean tesisDurum = false;
		Tanim bolum = null;
		for (PersonelDonemselDurum vg : personelDonemDurumList) {
			Personel personel = vg.getPersonel();
			if (!tesisDurum)
				tesisDurum = personel.getSirket().getTesisDurum();
			if (bolum == null && personel.getEkSaha3() != null)
				bolum = personel.getEkSaha3().getParentTanim();

		}
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Personel Listesi", false);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		int row = 0;
		int col = 0;
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolum != null ? bolum.getAciklama() : ortakIslemler.bolumAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İlk Giriş");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son Çıkış");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son Giriş");
		boolean renk = true;
		for (PersonelDonemselDurum vg : personelDonemDurumList) {
			++row;
			col = 0;
			Personel personel = vg.getPersonel();
			CellStyle style = null, styleCenter = null, cellStyleDate = null;
			Sirket sirket = personel.getSirket();
			if (renk) {
				cellStyleDate = styleOddDate;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				cellStyleDate = styleEvenDate;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket != null ? sirket.getAd() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getYoneticisi() != null ? personel.getYoneticisi().getAdSoyad() : "");
			if (tesisDurum) {
				if (personel.getTesis() != null)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket != null && sirket.getTesisDurum() ? personel.getTesis().getAciklama() : "");
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			}
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
	  
		}
		try {

			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			baos = null;
		}
		return baos;

	}
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		GebeSutIzniRaporHome.sayfaURL = sayfaURL;
	}

	public List<PersonelDonemselDurum> getPersonelDonemDurumList() {
		return personelDonemDurumList;
	}

	public void setPersonelDonemDurumList(List<PersonelDonemselDurum> personelDonemDurumList) {
		this.personelDonemDurumList = personelDonemDurumList;
	}

	public List<SelectItem> getMudurlukList() {
		return mudurlukList;
	}

	public void setMudurlukList(List<SelectItem> mudurlukList) {
		this.mudurlukList = mudurlukList;
	}

	public List<SelectItem> getTesisList() {
		return tesisList;
	}

	public void setTesisList(List<SelectItem> tesisList) {
		this.tesisList = tesisList;
	}

	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	public boolean isTesisDurum() {
		return tesisDurum;
	}

	public void setTesisDurum(boolean tesisDurum) {
		this.tesisDurum = tesisDurum;
	}

	public Long getMudurlukId() {
		return mudurlukId;
	}

	public void setMudurlukId(Long mudurlukId) {
		this.mudurlukId = mudurlukId;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

}
