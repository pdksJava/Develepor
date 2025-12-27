package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.Dosya;
import org.pdks.entity.IzinIstirahat;
import org.pdks.entity.IzinTipi;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelDenklestirme;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.TempIzin;
import org.pdks.security.entity.User;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import com.pdks.webservice.IzinERP;
import com.pdks.webservice.PdksSoapVeriAktar;

@Name("kullanilanIzinlerHome")
public class KullanilanIzinlerHome extends EntityHome<PersonelIzin> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1297221062917679868L;
	static Logger logger = Logger.getLogger(KullanilanIzinlerHome.class);

	@RequestParameter
	Long personelIzinId;

	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;

	@In(required = false)
	FacesMessages facesMessages;

	private List<PersonelIzin> personelIzinList = new ArrayList<PersonelIzin>(), bakiyeIzinler;

	private TempIzin updateTempIzin;
	private PersonelIzin updateIzin;

	private List<String> yilList = new ArrayList<String>();
	private List<IzinERP> izinERPList = new ArrayList<IzinERP>(), izinERPReturnList;
	private TreeMap<Long, IzinIstirahat> izinIstirahatMap;
	private TreeMap<String, Personel> personelMap;
	private String islemTipi;
	private List<SelectItem> islemTipleri;
	private Dosya izinDosya = new Dosya();
	private Date basTarih, bitTarih;
	private Boolean degisti;

	private Integer yil;

	private boolean tumIzinler = Boolean.FALSE, istenAyrilanEkle = Boolean.FALSE, servisAktarDurum, dosyaGuncellemeYetki, servisCalisti, referansOtomatikOlustur;
	private Session session;
	private AramaSecenekleri aramaSecenekleri = null;
	private List<SelectItem> izinTanimIdList;
	private List<IzinTipi> bakiyeIzinTipiList;
	private Tanim izinTipiTanim;
	private Long izinTipiId;

	@Override
	public Object getId() {
		if (personelIzinId == null) {
			return super.getId();
		} else {
			return personelIzinId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	private void fillEkSahaTanim() {
		ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, Boolean.TRUE, aramaSecenekleri);
		List<Sirket> sirketler = ortakIslemler.fillSirketList(session, null, Boolean.TRUE);

		if (aramaSecenekleri.getSirketIdList() == null)
			aramaSecenekleri.setSirketIdList(new ArrayList<SelectItem>());
		else
			aramaSecenekleri.getSirketIdList().clear();
		for (Sirket sirket : sirketler) {
			if (sirket.getFazlaMesai() && sirket.getPdks())
				aramaSecenekleri.getSirketIdList().add(new SelectItem(sirket.getId(), sirket.getAd()));
		}
		if (aramaSecenekleri.getSirketIdList().size() == 1) {
			aramaSecenekleri.setSirketId((Long) aramaSecenekleri.getSirketIdList().get(0).getValue());

		}
		fillTesisList();
	}

	public String fillTesisList() {
		Date bugun = PdksUtil.getDate(new Date());
		ortakIslemler.setAramaSecenekTesisData(aramaSecenekleri, bugun, bugun, true, session);
		return "";
	}

	public String fillEkSahaList() {
		Date bugun = PdksUtil.getDate(new Date());
		ortakIslemler.setAramaSecenekEkDataDoldur(aramaSecenekleri, bugun, bugun, session);
		return "";
	}

	public String excelAktar() {
		try {
			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "tumKullanilanIzinler.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	public ByteArrayOutputStream excelDevam() throws Exception {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Izinler", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
		int row = 0;
		int col = 0;
		HashMap<String, Boolean> map = ortakIslemler.getListEkSahaDurumMap(personelIzinList, null);
		boolean ekSaha1 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha1") && map.containsKey("ekSaha1");
		boolean ekSaha2 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha2") && map.containsKey("ekSaha2");
		boolean ekSaha3 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha3") && map.containsKey("ekSaha3");
		boolean ekSaha4 = (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) && aramaSecenekleri.getEkSahaTanimMap().containsKey("ekSaha4") && map.containsKey("ekSaha4");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		if (ekSaha1)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha1").getAciklama());
		if (ekSaha2)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha2").getAciklama());
		if (ekSaha3)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha3").getAciklama());
		if (ekSaha4)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha4").getAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Tipi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Başlangıç Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.bitisZamaniAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süresi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son İşlem Yapan");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son İşlem Tarihi");
		if (islemTipi != null && islemTipi.equals("B"))
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Onay Son Durum");
		boolean renk = true;
		for (Iterator iter = personelIzinList.iterator(); iter.hasNext();) {
			PersonelIzin izin = (PersonelIzin) iter.next();
			row++;
			col = 0;
			CellStyle style = null, styleCenter = null, styleDateTime = null;
			if (renk) {
				styleDateTime = styleOddDateTime;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				styleDateTime = styleEvenDateTime;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			Personel personel = izin.getIzinSahibi();
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			String sirket = "";
			try {
				sirket = personel.getSirket().getAd();
			} catch (Exception e1) {
				sirket = "" + ortakIslemler.sirketAciklama() + " tanımsız";
			}
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket);
			if (ekSaha1)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha1() != null ? personel.getEkSaha1().getAciklama() : "");
			if (ekSaha2)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha2() != null ? personel.getEkSaha2().getAciklama() : "");
			if (ekSaha3)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			if (ekSaha4)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");

			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getIzinTipiAciklama());
			ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(izin.getBaslangicZamani());
			ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(izin.getBitisZamani());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getSureAciklama());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getAciklama());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getSonIslemYapan().getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, styleDateTime).setCellValue(izin.getSonIslemTarihi());
			if (islemTipi != null && islemTipi.equals("B"))
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getIzinDurumuAciklama(ortakIslemler, session));
		}

		for (int i = 0; i < col; i++)

			sheet.autoSizeColumn(i);
		try {
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

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
			aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
		aramaSecenekleri.setStajyerOlmayanSirket(Boolean.FALSE);
		Calendar cal = Calendar.getInstance();
		setBitTarih(ortakIslemler.tariheAyEkleCikar(cal, new Date(), 2));
		setTumIzinler(Boolean.FALSE);
		cal.add(Calendar.YEAR, -1);
		setBasTarih(cal.getTime());
		fillEkSahaTanim();
		fillIzinTanimList();
		islemTipleri = ortakIslemler.getSelectItemList("islemTip", authenticatedUser);

		islemTipleri.add(new SelectItem("K", "Onaylanan izinler"));
		if (authenticatedUser.isIzinGirebilir())
			islemTipleri.add(new SelectItem("B", "Onay bekleyen izinler"));

		setIslemTipi((String) islemTipleri.get(0).getValue());
		if (!authenticatedUser.isIK() && !authenticatedUser.isAdmin()) {
			istenAyrilanEkle = Boolean.FALSE;
			aramaSecenekleri.setSirketId(authenticatedUser.getPdksPersonel().getSirket().getId());
		}
		dosyaGuncelleDurum();
		setPersonelIzinList(new ArrayList<PersonelIzin>());
	}

	public void fillIzinTanimList() {
		HashMap map = new HashMap();
		map.put(PdksEntityController.MAP_KEY_MAP, "getKodu");
		map.put(PdksEntityController.MAP_KEY_SELECT, "izinTipiTanim");
		map.put("bakiyeIzinTipi=", null);
		// map.put("izinTipiTanim.kodu<>", IzinTipi.SSK_ISTIRAHAT);
		// map.put("personelGirisTipi<>", IzinTipi.GIRIS_TIPI_YOK);
		if (!authenticatedUser.isAdmin()) {
			map.put("departman=", authenticatedUser.getDepartman());
			if (!authenticatedUser.isIK() && !authenticatedUser.getPdksPersonel().isOnaysizIzinKullanir())
				map.put("onaylayanTipi<>", IzinTipi.ONAYLAYAN_TIPI_YOK);

		}
		if (session != null)
			map.put(PdksEntityController.MAP_KEY_SESSION, session);
		TreeMap<String, Tanim> izinTipiTanimMap = pdksEntityController.getObjectByInnerObjectMapInLogic(map, IzinTipi.class, Boolean.FALSE);
		List<Tanim> list = new ArrayList<Tanim>(izinTipiTanimMap.values());
		if (list.size() > 1)
			list = PdksUtil.sortObjectStringAlanList(list, "getAciklama", null);
		izinTanimIdList = ortakIslemler.getSelectItemList("izinTanim", authenticatedUser);
		for (Tanim tanim : list) {
			izinTanimIdList.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
		}
		if (bakiyeIzinTipiList == null)
			bakiyeIzinTipiList = new ArrayList<IzinTipi>();

		if (authenticatedUser.isAdmin() || ortakIslemler.getParameterKeyHasStringValue("dosyaIzinGuncellemeYetki")) {
			bakiyeIzinTipiList = ortakIslemler.getYillikIzinBakiyeListesi(session);
		} else
			bakiyeIzinTipiList.clear();

	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String fileImportDosyaSifirla() throws Exception {
		if (bakiyeIzinler == null)
			bakiyeIzinler = new ArrayList<PersonelIzin>();
		else
			bakiyeIzinler.clear();
		izinDosya.setDosyaIcerik(null);
		degisti = Boolean.FALSE;

		return "";
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public void listenerFileImportDosya(UploadEvent event) throws Exception {
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, izinDosya);
		bakiyeIzinler.clear();
	}

	public String bakiyeIzinDosyaOku() {
		personelMap = null;
		bakiyeIzinler.clear();
		try {
			degisti = false;

			Workbook wb = ortakIslemler.getWorkbook(izinDosya);

			if (wb != null) {
				Sheet sheet = wb.getSheetAt(0);
				// logger.info(sheet.getSheetName());
				int COL_SICIL_NO = 0;
				int COL_AD_SOYAD = 1;

				int COL_DEVIR_BAKIYE = 2;
				String perSicilNo = null;
				List<String> siciller = new ArrayList<String>();
				String sicilNoUzunlukStr = ortakIslemler.getParameterKey("sicilNoUzunluk");
				int maxTextLength = 0;
				try {
					if (PdksUtil.hasStringValue(sicilNoUzunlukStr))
						maxTextLength = Integer.parseInt(sicilNoUzunlukStr);
				} catch (Exception e) {
					maxTextLength = 0;
				}
				int sonSatir = sheet.getLastRowNum();
				LinkedHashMap<String, PersonelIzin> bakiyeMap = new LinkedHashMap<String, PersonelIzin>();

				for (int row = 1; row <= sonSatir; row++) {
					try {
						String key = null;
						try {
							perSicilNo = getSheetStringValue(sheet, row, COL_SICIL_NO);
							if (maxTextLength > 0 && perSicilNo != null && perSicilNo.trim().length() < maxTextLength)
								perSicilNo = PdksUtil.textBaslangicinaKarakterEkle(perSicilNo, '0', maxTextLength);
							key = perSicilNo;
						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							break;
						}
						if (!bakiyeMap.containsKey(perSicilNo)) {
							siciller.add(perSicilNo);
							PersonelIzin personelIzin = new PersonelIzin();
							Personel izinSahibi = new Personel();
							izinSahibi.setAd(getSheetStringValue(sheet, row, COL_AD_SOYAD));
							izinSahibi.setSoyad("");
							izinSahibi.setPdksSicilNo(perSicilNo);
							personelIzin.setIzinSahibi(izinSahibi);
							personelIzin.setIzinSuresi(ExcelUtil.getSheetDoubleValue(ExcelUtil.getCell(sheet, row, COL_DEVIR_BAKIYE)));
							bakiyeMap.put(key, personelIzin);
						}

					} catch (Exception e) {
						logger.error("PDKS hata in : \n");
						e.printStackTrace();
						logger.error("PDKS hata out : " + e.getMessage());

					}

				}
				if (!siciller.isEmpty()) {
					String fieldName = "pId";
					HashMap fields = new HashMap();
					StringBuilder sb = new StringBuilder();
					sb.append("select * from " + Personel.TABLE_NAME + " V " + PdksEntityController.getSelectLOCK() + " ");
					sb.append(" where " + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :" + fieldName);
					fields.put(fieldName, siciller);
					if (session != null)
						fields.put(PdksEntityController.MAP_KEY_SESSION, session);
					personelMap = pdksEntityController.getTreeMapByList(pdksEntityController.getSQLParamList(siciller, sb, fieldName, fields, Personel.class, session), "getPdksSicilNo", true);
					sb = null;
					if (!personelMap.isEmpty()) {
						List personelIdler = new ArrayList(), izinTipiIdler = new ArrayList();
						for (IzinTipi izinTipi : bakiyeIzinTipiList)
							izinTipiIdler.add(izinTipi.getId());

						for (Iterator iterator = personelMap.keySet().iterator(); iterator.hasNext();) {
							String key = (String) iterator.next();
							bakiyeMap.get(key).setIzinSahibi(personelMap.get(key));
							personelIdler.add(personelMap.get(key).getId());
						}
						fields.clear();
						sb = new StringBuilder();
						fieldName = "pId";
						sb.append("select * from " + PersonelIzin.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
						sb.append(" where " + PersonelIzin.COLUMN_NAME_IZIN_TIPI + " :t and " + PersonelDenklestirme.COLUMN_NAME_PERSONEL + " :" + fieldName);
						sb.append(" and " + PersonelIzin.COLUMN_NAME_BASLANGIC_ZAMANI + " = :b ");
						fields.put("b", PdksUtil.convertToJavaDate("19000101", "yyyyMMdd"));
						fields.put("t", izinTipiIdler);
						fields.put(fieldName, personelIdler);
						if (session != null)
							fields.put(PdksEntityController.MAP_KEY_SESSION, session);

						List<PersonelIzin> list = pdksEntityController.getSQLParamList(personelIdler, sb, fieldName, fields, PersonelIzin.class, session);
						for (PersonelIzin pd : list) {
							String key = pd.getIzinSahibi().getPdksSicilNo();
							PersonelIzin personelIzin = bakiyeMap.get(key);
							if (!degisti)
								degisti = PdksUtil.isDoubleDegisti(pd.getIzinSuresi(), personelIzin.getIzinSuresi());
							personelIzin.setOrjIzin(pd);
						}
						list = null;
						personelIdler = null;
						izinTipiIdler = null;

					}
					personelMap = null;
				}
				bakiyeIzinler.addAll(new ArrayList<PersonelIzin>(bakiyeMap.values()));
				bakiyeMap = null;
			}
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		izinDosya.setDosyaIcerik(null);

		return "";
	}

	public String getPersonelAdiSoyadi(String perNo) {
		String deger = null;
		if (personelMap != null && perNo != null)
			deger = personelMap.get(perNo).getAdSoyad();
		return deger;
	}

	private String getSheetStringValue(Sheet sheet, int row, int col) throws Exception {
		String value = null;

		try {
			value = ExcelUtil.getSheetStringValue(sheet, row, col);
			if (value == null)
				value = String.valueOf(ExcelUtil.getSheetDoubleValue(sheet, row, col).longValue());
		} catch (Exception e) {
			value = String.valueOf(ExcelUtil.getSheetDoubleValue(sheet, row, col).longValue());

		}
		return value;
	}

	@Transactional
	public String bakiyeIzinDosyaYaz() throws Exception {
		boolean flush = false;
		for (PersonelIzin personelIzin : bakiyeIzinler) {
			Personel personel = personelIzin.getIzinSahibi();
			if (personel.getId() != null) {
				PersonelIzin orjIzin = personelIzin.getOrjIzin();
				if (orjIzin == null)
					orjIzin = ortakIslemler.getPersonelBakiyeIzin(0, authenticatedUser, personel, session);

				if (PdksUtil.isDoubleDegisti(orjIzin.getIzinSuresi(), personelIzin.getIzinSuresi())) {
					orjIzin.setIzinSuresi(personelIzin.getIzinSuresi());
					pdksEntityController.saveOrUpdate(session, entityManager, orjIzin);
					flush = true;
				}
			}
		}
		if (flush) {
			session.flush();
			bakiyeIzinler.clear();
			PdksUtil.addMessageAvailableInfo("Güncelleneme başarılı yapıldı.");
		} else
			PdksUtil.addMessageWarn("Güncellenecek bakiye yoktur!");

		return "";

	}

	public void fillIzinList() {
		setInstance(null);
		servisAktarDurum = Boolean.FALSE;
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		// ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.TRUE, istenAyrilanEkle, session);
		ArrayList<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, istenAyrilanEkle, session);
		if (sicilNoList != null && !sicilNoList.isEmpty()) {
			HashMap parametreMap = new HashMap();

			List<Personel> personeller = pdksEntityController.getSQLParamByFieldList(Personel.TABLE_NAME, Personel.COLUMN_NAME_PDKS_SICIL_NO, sicilNoList, Personel.class, session);

			Sirket sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, aramaSecenekleri.getSirketId(), Sirket.class, session);

			if (izinTipiTanim != null) {
				if (sirket != null)
					parametreMap.put("departman.id=", sirket.getDepartman().getId());
				else if (!authenticatedUser.isAdmin())
					parametreMap.put("departman.id=", authenticatedUser.getDepartman().getId());

			}
			parametreMap.clear();
			boolean kontrolEt = Boolean.FALSE;
			if (personeller.size() < PdksEntityController.LIST_MAX_SIZE)
				parametreMap.put("izinSahibi", personeller);
			else
				kontrolEt = Boolean.TRUE;
			parametreMap.put("izinTipi.bakiyeIzinTipi=", null);
			if (!tumIzinler) {
				if (izinTipiId != null)
					parametreMap.put("izinTipi.izinTipiTanim.id=", izinTipiId);
				else if (izinTipiTanim != null) {
					parametreMap.put("izinTipi.izinTipiTanim.id=", izinTipiTanim.getId());
				}
			}
			Date bitisTarihi = (Date) bitTarih.clone();
			if (PdksUtil.tarihKarsilastirNumeric(bitTarih, basTarih) == 0)
				bitisTarihi = ortakIslemler.tariheGunEkleCikar(null, bitisTarihi, 1);
			parametreMap.put("baslangicZamani<=", bitisTarihi);
			parametreMap.put("bitisZamani>=", basTarih);
			List<Integer> izinDurumuList = new ArrayList<Integer>();
			if (islemTipi.equals("K")) {
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_ERP_GONDERILDI);
			} else if (islemTipi.equals("B")) {
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);

			}

			parametreMap.put("izinDurumu", izinDurumuList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			if (!izinDurumuList.isEmpty())
				izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
			if (kontrolEt) {
				TreeMap<Long, Long> perMap = new TreeMap<Long, Long>();
				for (Personel personel : personeller)
					perMap.put(personel.getId(), personel.getId());
				for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
					PersonelIzin personelIzin = (PersonelIzin) iterator.next();

					if (personelIzin.getIzinSahibi() == null || !perMap.containsKey(personelIzin.getIzinSahibi().getId()))
						iterator.remove();

				}

			}
			if (!izinList.isEmpty())
				izinList = PdksUtil.sortListByAlanAdi(izinList, "baslangicZamani", Boolean.TRUE);

		}
		if (islemTipi != null && islemTipi.equalsIgnoreCase("K"))
			servisAktarDurum = ortakIslemler.erpIzinDoldur(izinList, session);
		setPersonelIzinList(izinList);

	}

	public String excelIzinIstirahatList() {
		try {
			ByteArrayOutputStream baosDosya = excelAktarDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, "sskIstirahatIzinleri.xlsx");

		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}
		return "";
	}

	public ByteArrayOutputStream excelAktarDevam() {

		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "SSK Izin Rapor", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		int row = 0, col = 0;
		boolean ekSaha1 = false, ekSaha2 = false, ekSaha3 = false, ekSaha4 = false;
		HashMap<String, Boolean> map = ortakIslemler.getListEkSahaDurumMap(personelIzinList, null);
		if (authenticatedUser.isAdmin() || authenticatedUser.isIKAdmin()) {
			ekSaha1 = map.containsKey("ekSaha1");
			ekSaha2 = map.containsKey("ekSaha2");
			ekSaha3 = map.containsKey("ekSaha3");
			ekSaha4 = map.containsKey("ekSaha4");
		}
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		if (ekSaha1)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha1").getAciklama());
		if (ekSaha2)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha2").getAciklama());
		if (ekSaha3)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha3").getAciklama());
		if (ekSaha4)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aramaSecenekleri.getEkSahaTanimMap().get("ekSaha4").getAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin Başlangıç Zamanı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.bitisZamaniAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Süresi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Teşhis");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Rapor Kaynağı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Veren Kurum");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Veren Hekim Adı");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");

		boolean renk = true;
		for (PersonelIzin izin : personelIzinList) {
			Personel personel = izin.getIzinSahibi();
			++row;
			col = 0;
			CellStyle style = null, styleCenter = null, styleDate = null;
			if (renk) {
				styleDate = styleOddDate;
				style = styleOdd;
				styleCenter = styleOddCenter;
			} else {
				styleDate = styleEvenDate;
				style = styleEven;
				styleCenter = styleEvenCenter;
			}
			renk = !renk;
			IzinIstirahat istirahat = getIzinIstirahat(izin.getId());
			if (istirahat == null)
				istirahat = new IzinIstirahat();
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
			if (ekSaha1)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha1() != null ? personel.getEkSaha1().getAciklama() : "");
			if (ekSaha2)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha2() != null ? personel.getEkSaha2().getAciklama() : "");
			if (ekSaha3)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			if (ekSaha4)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha4() != null ? personel.getEkSaha4().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(izin.getBaslangicZamani());
			ExcelUtil.getCell(sheet, row, col++, styleDate).setCellValue(izin.getBitisZamani());
			String sure = "";
			try {
				sure = izin.getSureAciklama();
			} catch (Exception e) {
				sure = "";
			}
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sure);
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(istirahat.getTeshis());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(PdksUtil.getMessageBundleMessage(istirahat.getRaporKaynagiAciklama()));
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(istirahat.getVerenKurum());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(istirahat.getVerenHekimAdi());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(izin.getAciklama());
		}

		for (int i = 0; i < col; i++)
			sheet.autoSizeColumn(i);
		try {
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

	public void fillIzinIstirahatList() {
		setInstance(null);
		setIzinIstirahatMap(new TreeMap<Long, IzinIstirahat>());

		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		// ArrayList<String> sicilNoList = ortakIslemler.getPersonelSicilNo(ad, soyad, sicilNo, sirket, seciliEkSaha1, seciliEkSaha2, seciliEkSaha3, seciliEkSaha4, Boolean.FALSE, session);
		List<String> sicilNoList = ortakIslemler.getAramaPersonelSicilNo(aramaSecenekleri, Boolean.TRUE, istenAyrilanEkle, session);
		if (sicilNoList != null && !sicilNoList.isEmpty()) {
			HashMap parametreMap = new HashMap();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "id");

			parametreMap.put("pdksSicilNo", sicilNoList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			List<Long> personeller = pdksEntityController.getObjectByInnerObjectList(parametreMap, Personel.class);
			IzinTipi izinTipi = null;

			Sirket sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, aramaSecenekleri.getSirketId(), Sirket.class, session);

			parametreMap.clear();
			if (sirket != null)
				parametreMap.put("departman.id=", sirket.getDepartman().getId());
			else if (!authenticatedUser.isAdmin())
				parametreMap.put("departman.id=", authenticatedUser.getDepartman().getId());

			if (parametreMap.containsKey("departman.id=")) {
				parametreMap.put("izinTipiTanim.kodu=", IzinTipi.SSK_ISTIRAHAT);
				parametreMap.put("bakiyeIzinTipi=", null);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinTipi = (IzinTipi) pdksEntityController.getObjectByInnerObjectInLogic(parametreMap, IzinTipi.class);
			}
			if (izinTipi != null) {
				parametreMap.clear();
				parametreMap.put("izinSahibi.id", personeller);
				if (izinTipi != null)
					parametreMap.put("izinTipi.id=", izinTipi.getId());

				Date bitisTarihi = (Date) bitTarih.clone();
				if (PdksUtil.tarihKarsilastirNumeric(bitTarih, basTarih) == 0)
					bitisTarihi = ortakIslemler.tariheGunEkleCikar(null, bitisTarihi, 1);
				parametreMap.put("baslangicZamani<=", bitisTarihi);
				parametreMap.put("bitisZamani>=", basTarih);
				List<Integer> izinDurumuList = new ArrayList<Integer>();
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IKINCI_YONETICI_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_IK_ONAYINDA);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_ONAYLANDI);
				izinDurumuList.add(PersonelIzin.IZIN_DURUMU_ERP_GONDERILDI);

				// parametreMap.put("izinDurumu", izinDurumuList);
				if (session != null)
					parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
				izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap, PersonelIzin.class);
				if (!izinList.isEmpty()) {
					izinList = PdksUtil.sortListByAlanAdi(izinList, "baslangicZamani", Boolean.TRUE);
					List<Long> list = new ArrayList<Long>();
					for (Iterator iterator = izinList.iterator(); iterator.hasNext();) {
						PersonelIzin personelIzin = (PersonelIzin) iterator.next();
						if (izinDurumuList.contains(personelIzin.getIzinDurumu()))
							list.add(personelIzin.getId());
						else
							iterator.remove();
					}

					if (!list.isEmpty()) {
						parametreMap.clear();
						parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getPersonelIzinId");
						parametreMap.put("personelIzin.id", list);
						if (session != null)
							parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
						TreeMap<Long, IzinIstirahat> istirahatMap = pdksEntityController.getObjectByInnerObjectMap(parametreMap, IzinIstirahat.class, Boolean.FALSE);
						setIzinIstirahatMap(istirahatMap);
					}
					list = null;
				}

			}

		}
		setPersonelIzinList(izinList);

	}

	public String personelDosyaOkuBasla() throws Exception {
		izinERPList.clear();
		return "";

	}

	public String excelServiceAktar() {
		ortakIslemler.excelServiceAktar(personelIzinList);
		return "";
	}

	public void listenerizinDosya(UploadEvent event) throws Exception {
		servisCalisti = Boolean.FALSE;
		UploadItem item = event.getUploadItem();
		PdksUtil.getDosya(item, izinDosya);
		if (izinERPList == null)
			izinERPList = new ArrayList<IzinERP>();
		else
			izinERPList.clear();

	}

	public String listenerizinDosya() throws Exception {
		return "";
	}

	public String izinDosyaOkuBasla() throws Exception {
		izinDosyaSifirla();
		dosyaGuncelleDurum();
		return "";
	}

	public String izinDosyaSifirla() throws Exception {
		referansOtomatikOlustur = Boolean.FALSE;
		servisCalisti = Boolean.FALSE;
		izinERPList.clear();
		izinDosya.setDosyaIcerik(null);
		if (izinERPReturnList == null)
			izinERPReturnList = new ArrayList<IzinERP>();
		else
			izinERPReturnList.clear();

		return "";
	}

	public String izinDosyaOku() throws Exception {
		servisCalisti = Boolean.FALSE;
		if (izinERPList == null)
			izinERPList = new ArrayList<IzinERP>();
		else
			izinERPList.clear();
		Workbook wb = ortakIslemler.getWorkbook(izinDosya);
		if (wb != null) {
			Sheet sheet = wb.getSheetAt(0);
			int col = 0;
			IzinTipi yillikIzin = null;
			if (bakiyeIzinTipiList != null) {
				for (IzinTipi bakiyeIzinTipi : bakiyeIzinTipiList) {
					if (bakiyeIzinTipi.getBakiyeIzinTipi() != null) {
						IzinTipi izinTipi = bakiyeIzinTipi.getBakiyeIzinTipi();
						if (izinTipi.isSenelikIzin())
							yillikIzin = izinTipi;
					}

				}
			}

			String pattern = "yyyy-MM-dd HH:mm";
			LinkedHashMap<String, List<IzinERP>> izinMap = new LinkedHashMap<String, List<IzinERP>>();
			HashMap<String, String> perMap = new HashMap<String, String>();
			for (int row = 1; row <= sheet.getLastRowNum(); row++) {
				col = 0;
				try {
					Cell cellPersonelNo = ExcelUtil.getCell(sheet, row, col++);
					if (PdksUtil.hasStringValue(cellPersonelNo.getStringCellValue())) {
						Cell cellAdiSoyad = ExcelUtil.getCell(sheet, row, col++);
						if (PdksUtil.hasStringValue(cellAdiSoyad.getStringCellValue())) {
							IzinERP izinERP = new IzinERP();
							izinERP.setPersonelNo(cellPersonelNo.getStringCellValue());

							izinERP.setIzinSuresi(ExcelUtil.getSheetDoubleValue(sheet, row, col++));

							Cell cellBasZaman = ExcelUtil.getCell(sheet, row, col++);
							if (cellBasZaman.getDateCellValue() == null)
								izinERP.setBasZaman(cellBasZaman.getStringCellValue());
							else
								izinERP.setBasZaman(PdksUtil.convertToDateString(cellBasZaman.getDateCellValue(), pattern));

							Cell cellBitZaman = ExcelUtil.getCell(sheet, row, col++);
							if (cellBitZaman.getDateCellValue() == null)
								izinERP.setBitZaman(cellBitZaman.getStringCellValue());
							else
								izinERP.setBitZaman(PdksUtil.convertToDateString(cellBitZaman.getDateCellValue(), pattern));

							Cell cellReferansNoERP = ExcelUtil.getCell(sheet, row, col++);
							String referansNoERP = cellReferansNoERP != null ? cellReferansNoERP.getStringCellValue() : null;
							if (referansOtomatikOlustur || PdksUtil.hasStringValue(referansNoERP) == false)
								referansNoERP = PersonelIzin.IZIN_MANUEL_EK + "_" + izinERP.getPersonelNo() + PdksUtil.replaceAll(izinERP.getBasZaman().substring(0, 10), "-", "");
							izinERP.setReferansNoERP(referansNoERP);
							Cell cellAciklama = ExcelUtil.getCell(sheet, row, col++);
							izinERP.setAciklama(cellAciklama.getStringCellValue());
							String izinTipi = "";
							Cell cellIzinTipi = ExcelUtil.getCell(sheet, row, col++);
							if (cellIzinTipi != null) {

								Double kodu = null;
								try {
									kodu = cellIzinTipi.getNumericCellValue();
								} catch (Exception e) {
									kodu = null;
								}
								if (kodu != null)
									izinTipi = String.valueOf(kodu.longValue());
								else
									izinTipi = cellIzinTipi.getStringCellValue();

								izinERP.setIzinTipi(izinTipi);

							}
							if (PdksUtil.hasStringValue(izinTipi) == false && yillikIzin != null && yillikIzin.getIzinTipiTanim() != null)
								izinERP.setIzinTipi(yillikIzin.getIzinTipiTanim().getErpKodu());
							Cell cellIzinTipiAciklama = ExcelUtil.getCell(sheet, row, col++);
							if (cellIzinTipiAciklama != null && PdksUtil.hasStringValue(cellIzinTipiAciklama.getStringCellValue()))
								izinERP.setIzinTipiAciklama(cellIzinTipiAciklama.getStringCellValue());
							else if (yillikIzin != null && yillikIzin.getIzinTipiTanim() != null)
								izinERP.setIzinTipiAciklama(yillikIzin.getIzinTipiTanim().getAciklama());

							Cell cellDurum = ExcelUtil.getCell(sheet, row, col++);
							if (cellDurum != null)
								izinERP.setDurum(cellDurum.getStringCellValue() == null ? new Boolean(cellDurum.getBooleanCellValue()) : new Boolean(cellDurum.getStringCellValue()));
							else
								izinERP.setDurum(Boolean.TRUE);

							Cell cellSureBirimi = ExcelUtil.getCell(sheet, row, col++);
							if (cellSureBirimi != null && PdksUtil.hasStringValue(cellSureBirimi.getStringCellValue()))
								izinERP.setSureBirimi(cellSureBirimi.getStringCellValue());
							else if (yillikIzin != null && yillikIzin.getHesapTipi() != null)
								izinERP.setSureBirimi(String.valueOf(yillikIzin.getHesapTipi()));

							List<IzinERP> list = izinMap.containsKey(izinERP.getPersonelNo()) ? izinMap.get(izinERP.getPersonelNo()) : new ArrayList<IzinERP>();
							if (list.isEmpty()) {
								perMap.put(izinERP.getPersonelNo(), cellAdiSoyad.getStringCellValue());
								izinMap.put(izinERP.getPersonelNo(), list);
							}
							list.add(izinERP);
						}
					} else
						break;
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}

			}
			if (!izinMap.isEmpty()) {
				String fieldName = "p";
				List<String> dataIdList = new ArrayList<String>(izinMap.keySet());
				HashMap fields = new HashMap();
				StringBuilder sb = new StringBuilder();
				sb.append("select * from " + Personel.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
				sb.append(" where " + Personel.COLUMN_NAME_PDKS_SICIL_NO + " :" + fieldName);
				fields.put(PdksEntityController.MAP_KEY_MAP, "getPdksSicilNo");
				fields.put(fieldName, dataIdList);
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				personelMap = pdksEntityController.getSQLParamTreeMap("getPdksSicilNo", true, dataIdList, sb, fieldName, fields, Personel.class, session);
				sb = new StringBuilder();
				for (String key : dataIdList) {
					if (personelMap.containsKey(key))
						izinERPList.addAll(izinMap.get(key));
					else {
						if (sb.length() > 0)
							sb.append(", ");
						sb.append(key + " " + perMap.get(key));
					}
				}
				if (sb.length() > 0)
					PdksUtil.addMessageAvailableWarn(sb.toString() + " personel bilgisi bulunamadı!");
			}
		}
		if (personelMap == null)
			personelMap = new TreeMap<String, Personel>();
		return "";
	}

	public String izinDosyaYaz() throws Exception {
		TreeMap<String, IzinERP> izinMap = new TreeMap<String, IzinERP>();
		for (IzinERP izinERP : izinERPList) {
			izinMap.put(izinERP.getReferansNoERP(), izinERP);
			izinERP.setYazildi(null);
		}
		servisCalisti = Boolean.FALSE;
		PdksSoapVeriAktar service = null;
		try {
			izinERPReturnList = null;
			try {

				service = ortakIslemler.getPdksSoapVeriAktar(true);

				izinERPReturnList = service.saveIzinler(izinERPList);
			} catch (Exception e) {
				service = ortakIslemler.getPdksSoapVeriAktar(false);

				izinERPReturnList = service.saveIzinler(izinERPList);
			}
			if (izinERPReturnList != null) {
				izinERPList.clear();
				for (Iterator iterator = izinERPReturnList.iterator(); iterator.hasNext();) {
					IzinERP returnERP = (IzinERP) iterator.next();
					if (izinMap.containsKey(returnERP.getReferansNoERP())) {
						IzinERP izinERP = izinMap.get(returnERP.getReferansNoERP());
						izinERP.setYazildi(returnERP.getYazildi());
						if (returnERP.getYazildi()) {
							izinERP.setId(returnERP.getId());
							izinERPList.add(izinERP);
							iterator.remove();
						} else {
							izinERP.getHataList().addAll(returnERP.getHataList());
						}
					} else
						returnERP.getHataList().add("İşlem yapılmadı!");
				}
				if (!izinERPReturnList.isEmpty()) {
					izinERPList.clear();
					PdksUtil.addMessageWarn("Hata oluştu!");
				} else
					PdksUtil.addMessageInfo("Güncelleme  başarılı tamamlandı.");

			}

			servisCalisti = Boolean.TRUE;
		} catch (Exception e) {
			izinERPReturnList = null;
			logger.error(e);
		}

		return "";

	}

	/**
	 * 
	 */
	private void dosyaGuncelleDurum() {
		dosyaGuncellemeYetki = (ortakIslemler.getTestDurum() || (bakiyeIzinTipiList != null && bakiyeIzinTipiList.isEmpty() == false)) && (authenticatedUser.isAdmin() || authenticatedUser.isSistemYoneticisi() || authenticatedUser.isIK());
		if (dosyaGuncellemeYetki) {
			if (authenticatedUser.isIK()) {
				String dosyaGuncellemeYetkiStr = ortakIslemler.getParameterKey("dosyaIzinGuncellemeYetki");
				dosyaGuncellemeYetki = dosyaGuncellemeYetkiStr.equalsIgnoreCase("IK");
			}

		}
	}

	public IzinIstirahat getIzinIstirahat(Long personelIzinId) {
		IzinIstirahat izinIstirahat = null;
		if (izinIstirahatMap != null && izinIstirahatMap.containsKey(personelIzinId)) {
			izinIstirahat = izinIstirahatMap.get(personelIzinId);
			izinIstirahat.aciklamaAta();
		}

		return izinIstirahat;
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

	public List<String> getYilList() {
		return yilList;
	}

	public void setYilList(List<String> yilList) {
		this.yilList = yilList;
	}

	public Integer getYil() {
		return yil;
	}

	public void setYil(Integer yil) {
		this.yil = yil;
	}

	public TempIzin getUpdateTempIzin() {
		return updateTempIzin;
	}

	public void setUpdateTempIzin(TempIzin updateTempIzin) {
		this.updateTempIzin = updateTempIzin;
	}

	public PersonelIzin getUpdateIzin() {
		return updateIzin;
	}

	public void setUpdateIzin(PersonelIzin updateIzin) {
		this.updateIzin = updateIzin;
	}

	public Tanim getIzinTipiTanim() {
		return izinTipiTanim;
	}

	public void setIzinTipiTanim(Tanim izinTipiTanim) {
		this.izinTipiTanim = izinTipiTanim;
	}

	public List<PersonelIzin> getPersonelIzinList() {
		return personelIzinList;
	}

	public void setPersonelIzinList(List<PersonelIzin> personelIzinList) {
		this.personelIzinList = personelIzinList;
	}

	public TreeMap<Long, IzinIstirahat> getIzinIstirahatMap() {
		return izinIstirahatMap;
	}

	public void setIzinIstirahatMap(TreeMap<Long, IzinIstirahat> izinIstirahatMap) {
		this.izinIstirahatMap = izinIstirahatMap;
	}

	public boolean isTumIzinler() {
		return tumIzinler;
	}

	public void setTumIzinler(boolean tumIzinler) {
		this.tumIzinler = tumIzinler;
	}

	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	public List<SelectItem> getIslemTipleri() {
		return islemTipleri;
	}

	public void setIslemTipleri(List<SelectItem> islemTipleri) {
		this.islemTipleri = islemTipleri;
	}

	public boolean isIstenAyrilanEkle() {
		return istenAyrilanEkle;
	}

	public void setIstenAyrilanEkle(boolean istenAyrilanEkle) {
		this.istenAyrilanEkle = istenAyrilanEkle;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public List<SelectItem> getIzinTanimIdList() {
		return izinTanimIdList;
	}

	public void setIzinTanimIdList(List<SelectItem> izinTanimIdList) {
		this.izinTanimIdList = izinTanimIdList;
	}

	public Long getIzinTipiId() {
		return izinTipiId;
	}

	public void setIzinTipiId(Long izinTipiId) {
		this.izinTipiId = izinTipiId;
	}

	public boolean isServisAktarDurum() {
		return servisAktarDurum;
	}

	public void setServisAktarDurum(boolean servisAktarDurum) {
		this.servisAktarDurum = servisAktarDurum;
	}

	public boolean isDosyaGuncellemeYetki() {
		return dosyaGuncellemeYetki;
	}

	public void setDosyaGuncellemeYetki(boolean dosyaGuncellemeYetki) {
		this.dosyaGuncellemeYetki = dosyaGuncellemeYetki;
	}

	public List<IzinERP> getIzinERPList() {
		return izinERPList;
	}

	public void setIzinERPList(List<IzinERP> izinERPList) {
		this.izinERPList = izinERPList;
	}

	public Dosya getIzinDosya() {
		return izinDosya;
	}

	public void setIzinDosya(Dosya izinDosya) {
		this.izinDosya = izinDosya;
	}

	public boolean isServisCalisti() {
		return servisCalisti;
	}

	public void setServisCalisti(boolean servisCalisti) {
		this.servisCalisti = servisCalisti;
	}

	public List<IzinERP> getIzinERPReturnList() {
		return izinERPReturnList;
	}

	public void setIzinERPReturnList(List<IzinERP> izinERPReturnList) {
		this.izinERPReturnList = izinERPReturnList;
	}

	public List<PersonelIzin> getBakiyeIzinler() {
		return bakiyeIzinler;
	}

	public void setBakiyeIzinler(List<PersonelIzin> bakiyeIzinler) {
		this.bakiyeIzinler = bakiyeIzinler;
	}

	public Boolean getDegisti() {
		return degisti;
	}

	public void setDegisti(Boolean degisti) {
		this.degisti = degisti;
	}

	public List<IzinTipi> getBakiyeIzinTipiList() {
		return bakiyeIzinTipiList;
	}

	public void setBakiyeIzinTipiList(List<IzinTipi> bakiyeIzinTipiList) {
		this.bakiyeIzinTipiList = bakiyeIzinTipiList;
	}

	public boolean isReferansOtomatikOlustur() {
		return referansOtomatikOlustur;
	}

	public void setReferansOtomatikOlustur(boolean referansOtomatikOlustur) {
		this.referansOtomatikOlustur = referansOtomatikOlustur;
	}

	public TreeMap<String, Personel> getPersonelMap() {
		return personelMap;
	}

	public void setPersonelMap(TreeMap<String, Personel> personelMap) {
		this.personelMap = personelMap;
	}

}
