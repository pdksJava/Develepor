package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.pdks.entity.Departman;
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

	private List<SelectItem> sirketList, tesisList;

	private List<Tanim> tesisTanimList = null;

	private Date basTarih, bitTarih;

	private Long sirketId, tesisId;

	private boolean tesisDurum;

	private Sirket sirket = null;

	private String bolumAciklama;

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
		basTarih = PdksUtil.tariheAyEkleCikar(bitTarih, -12);
		Departman departman = null;
		sirketId = null;
		tesisDurum = false;
		List<Long> tesisIdList = null;
		if (authenticatedUser.getYetkiliTesisler() != null && authenticatedUser.getYetkiliTesisler().isEmpty() == false) {
			tesisIdList = new ArrayList<Long>();
			for (Tanim tesis : authenticatedUser.getYetkiliTesisler())
				tesisIdList.add(tesis.getId());

		}
		if (tesisIdList == null && (authenticatedUser.isIKSirket() || authenticatedUser.isIK_Tesis()))
			sirketId = authenticatedUser.getPdksPersonel().getSirket().getId();
		if (!(authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()))
			departman = authenticatedUser.getDepartman();
		List<Sirket> pdksSirketList = ortakIslemler.getDepartmanPDKSSirketList(departman, session);
		sirketList = ortakIslemler.getSelectItemList("sirket", authenticatedUser);
		tesisList = ortakIslemler.getSelectItemList("tesis", authenticatedUser);
		if (tesisIdList != null) {
			for (Iterator iterator = tesisList.iterator(); iterator.hasNext();) {
				SelectItem tesis = (SelectItem) iterator.next();
				if (!tesisIdList.contains(tesis.getValue()))
					iterator.remove();
			}
		}
		if (personelDonemDurumList == null)
			personelDonemDurumList = new ArrayList<PersonelDonemselDurum>();
		else
			personelDonemDurumList.clear();
		tesisTanimList = null;
		for (Sirket pdksSirket : pdksSirketList) {
			if (pdksSirket.isGebelikSutIzinVar()) {
				if (sirketId == null || pdksSirket.getId().equals(sirketId)) {
					if (sirketId != null)
						sirket = pdksSirket;
					sirketList.add(new SelectItem(pdksSirket.getId(), pdksSirket.getAd()));
				}
			}
		}
		if (sirket != null)
			fillTesisList();
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	/**
	 * @return
	 */
	public String fillMudurlukList() {
		sirket = sirketId != null ? (Sirket) pdksEntityController.getSQLParamByAktifFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session) : null;
		fillMudurlukTesisList("S");
		return "";
	}

	/**
	 * @param tip
	 * @return
	 */
	public String fillMudurlukTesisList(String tip) {
		personelDonemDurumList.clear();
		tesisList.clear();
		tesisDurum = false;
		if (sirket != null && sirket.isTesisDurumu()) {
			HashMap fields = new HashMap();
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct T.* from " + Personel.TABLE_NAME + " P " + PdksEntityController.getSelectLOCK());
			sb.append(" inner join " + Tanim.TABLE_NAME + " T " + PdksEntityController.getJoinLOCK() + " on T." + Tanim.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_TESIS);
			sb.append(" where P." + Personel.COLUMN_NAME_SIRKET + " = :s and P." + Personel.COLUMN_NAME_ISE_BASLAMA_TARIHI + " <= :b2");
			sb.append(" and P." + Personel.COLUMN_NAME_SSK_CIKIS_TARIHI + " >= :b1");

			List<Long> list = null;
			if (authenticatedUser.isIK_Tesis()) {
				if (tesisTanimList == null)
					tesisTanimList = ortakIslemler.filUserTesisList(authenticatedUser, session);
				if (!tesisTanimList.isEmpty()) {
					sb.append(" and P." + Personel.COLUMN_NAME_TESIS + " :t");
					list = new ArrayList<Long>();
					for (Tanim tesis : tesisTanimList) {
						list.add(tesis.getId());
					}
					fields.put("t", list);
				}
			}
			fields.put("s", sirketId);
			fields.put("b1", basTarih);
			fields.put("b2", bitTarih);
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Tanim> tesisTanimList = pdksEntityController.getObjectBySQLList(PdksUtil.getStringBuffer(sb), fields, Tanim.class);
			if (tesisTanimList.size() > 1)
				tesisTanimList = PdksUtil.sortTanimList(null, tesisTanimList);
			for (Tanim tesis : tesisTanimList) {
				tesisList.add(new SelectItem(tesis.getId(), tesis.getAciklama()));
			}
			tesisDurum = tesisList.size() > 0;
		}
		return "";
	}

	public String fillTesisList() {
		tesisId = null;
		sirket = sirketId != null ? (Sirket) pdksEntityController.getSQLParamByAktifFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session) : null;
		fillMudurlukTesisList("T");
		return "";
	}

	public String fillPersonelDonemselDurumList() {
		List<Long> list = new ArrayList<Long>();
		HashMap fields = new HashMap();
		StringBuilder sb = new StringBuilder();
		sb.append("select D.* from " + PersonelDonemselDurum.TABLE_NAME + " D " + PdksEntityController.getSelectLOCK());
		sb.append(" inner join " + Personel.TABLE_NAME + " P " + PdksEntityController.getJoinLOCK() + " on P." + Personel.COLUMN_NAME_ID + " = D." + PersonelDonemselDurum.COLUMN_NAME_PERSONEL);
		if (sirketId != null) {
			if (sirket.getSirketGrup() == null) {
				sb.append(" AND P." + Personel.COLUMN_NAME_SIRKET + " = :s");
				fields.put("s", sirketId);
			} else {
				sb.append(" inner join " + Sirket.TABLE_NAME + " S " + PdksEntityController.getJoinLOCK() + " on S." + Sirket.COLUMN_NAME_ID + " = P." + Personel.COLUMN_NAME_SIRKET);
				sb.append(" and S." + Sirket.COLUMN_NAME_SIRKET_GRUP + " = :g");
				fields.put("g", sirket.getSirketGrup().getId());
			}
		}
		if (tesisId != null) {
			sb.append(" and P." + Personel.COLUMN_NAME_TESIS + " = :t");
			fields.put("t", tesisId);
		} else if (tesisTanimList != null && !tesisTanimList.isEmpty()) {
			for (Tanim tesis : tesisTanimList)
				list.add(tesis.getId());
			sb.append(" AND P." + Personel.COLUMN_NAME_TESIS + " :t");
			fields.put("t", list);
		}
		sb.append(" where D." + PersonelDonemselDurum.COLUMN_NAME_BASLANGIC_ZAMANI + " <= :b2");
		sb.append(" and D." + PersonelDonemselDurum.COLUMN_NAME_BITIS_ZAMANI + " >= :b1");
		sb.append(" order by D." + PersonelDonemselDurum.COLUMN_NAME_BITIS_ZAMANI + " desc");

		fields.put("b1", basTarih);
		fields.put("b2", bitTarih);
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		personelDonemDurumList = pdksEntityController.getObjectBySQLList(PdksUtil.getStringBuffer(sb), fields, PersonelDonemselDurum.class);
		if (sirketId == null)
			tesisDurum = false;
		for (Iterator iterator = personelDonemDurumList.iterator(); iterator.hasNext();) {
			PersonelDonemselDurum pdd = (PersonelDonemselDurum) iterator.next();
			if (pdd.getDurum().equals(Boolean.FALSE))
				iterator.remove();
			else if (sirketId == null) {
				Personel personel = pdd.getPersonel();
				if (!tesisDurum)
					tesisDurum = personel.getSirket().isTesisDurumu();
			}
		}
		list = null;
		return "";
	}

	public String excelListe() {
		try {

			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null) {
				String dosyaAdi = "GebeSutIzniRapor_" + PdksUtil.convertToDateString(basTarih, "yyyyMMdd") + "_" + PdksUtil.convertToDateString(bitTarih, "yyyyMMdd") + ".xlsx";
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
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tipi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.basTarihAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.bitTarihAciklama());
		;
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
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vg.getPersonelDurumTipiAciklama());
			ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(vg.getBasTarih());
			ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(vg.getBitTarih());

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

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public List<SelectItem> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<SelectItem> sirketList) {
		this.sirketList = sirketList;
	}

	public Long getSirketId() {
		return sirketId;
	}

	public void setSirketId(Long sirketId) {
		this.sirketId = sirketId;
	}

	public Sirket getSirket() {
		return sirket;
	}

	public void setSirket(Sirket sirket) {
		this.sirket = sirket;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

}
