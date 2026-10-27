package org.pdks.session;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.Dosya;
import org.pdks.entity.FileUpload;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Kapi;
import org.pdks.entity.KapiKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelKGS;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Tanim;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("argeGirisCikisEkleHome")
public class ArgeGirisCikisEkleHome extends EntityHome<HareketKGS> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5981833922302825689L;

	static Logger logger = Logger.getLogger(ArgeGirisCikisEkleHome.class);

	@RequestParameter
	Long pdksYemekId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;

	public static String sayfaURL = "argeGirisCikisEkle";
	private Date tarih;
	private int saat;
	private int dakika;
	private List<SelectItem> kapiList, nedenList;

	private List<Tanim> hareketIslemList = new ArrayList<Tanim>();
	private ArrayList<FileUpload> files = new ArrayList<FileUpload>();
	private ArrayList<String> sicilNoList;
	private ArrayList<PersonelView> excelList = new ArrayList<PersonelView>();
	private List<VardiyaGun> vardiyaGunleri = new ArrayList<VardiyaGun>();
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private AramaSecenekleri aramaSecenekleri = null;
	private HashMap<Long, KapiView> map1 = null;
	private boolean dosyaTamam = false;
	private Long kapiId, nedenId;
	private Dosya dosya;
	private Session session;

	public void fillKapiList() {
		kapiList = ortakIslemler.getSelectItemList("kapi", authenticatedUser);
		List<KapiView> kapiViewList = new ArrayList<KapiView>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("kapi.durum", Boolean.TRUE);
		parametreMap.put("kapi.arge", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<KapiKGS> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, KapiKGS.class);
		KapiKGS manuelGiris = null, manuelCikis = null;
		map1 = new HashMap<Long, KapiView>();
		HashMap<String, KapiView> manuelKapiMap = new HashMap<String, KapiView>();
		for (KapiKGS kapiKGS : list) {
			KapiView view = kapiKGS.getKapiView();
			map1.put(kapiKGS.getId(), view);
			kapiViewList.add(view);
			String key = kapiKGS.getKapi().isGirisKapi() ? Kapi.TIPI_KODU_GIRIS : Kapi.TIPI_KODU_CIKIS;
			manuelKapiMap.put(key, view);
		}

		manuelGiris = manuelKapiMap.containsKey(Kapi.TIPI_KODU_GIRIS) ? manuelKapiMap.get(Kapi.TIPI_KODU_GIRIS).getKapiKGS() : null;
		manuelCikis = manuelKapiMap.containsKey(Kapi.TIPI_KODU_CIKIS) ? manuelKapiMap.get(Kapi.TIPI_KODU_CIKIS).getKapiKGS() : null;
		manuelKapiMap = null;
		if (manuelGiris != null && manuelCikis != null) {
			kapiViewList.clear();
			kapiViewList.add(manuelGiris.getKapiView());
			kapiViewList.add(manuelCikis.getKapiView());
		} else
			kapiViewList = PdksUtil.sortObjectStringAlanList(kapiViewList, "getAciklama", null);

		for (KapiView kapiView : kapiViewList)
			kapiList.add(new SelectItem(kapiView.getId(), kapiView.getKapi().getAciklama()));

	}

	public void fillHareketIslemList() {
		List<Tanim> islemList = pdksEntityController.getSQLParamByFieldList(Tanim.TABLE_NAME, Tanim.COLUMN_NAME_TIPI, Tanim.TIPI_HAREKET_NEDEN, Tanim.class, session);

		if (nedenList == null)
			nedenList = new ArrayList<SelectItem>();
		else
			nedenList.clear();
		for (Iterator iterator = islemList.iterator(); iterator.hasNext();) {
			Tanim tanim = (Tanim) iterator.next();
			String kodu = tanim.getKodu().toUpperCase(), koduERP = tanim.getErpKodu().toUpperCase();
			if (kodu.indexOf("ARGE") >= 0 || koduERP.indexOf("ARGE") >= 0)
				if (tanim.getDurum().booleanValue() == false)
					nedenList.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
		}
		if (nedenList.size() == 1)
			nedenId = (Long) nedenList.get(0).getValue();
		setHareketIslemList(islemList);
	}

	public int getSize() {
		if (getFiles().size() > 0) {
			return getFiles().size();
		} else {
			return 0;
		}
	}

	public void paint(OutputStream stream, Object object) throws IOException {

	}

	public String dosyaSifirla() {
		if (dosya != null)
			dosya.setDosyaIcerik(null);
		else
			dosya = new Dosya();
		dosyaTamam = false;
		vardiyaGunleri.clear();
		return "";
	}

	public void listener(UploadEvent event) throws Exception {
		vardiyaGunleri.clear();
		UploadItem item = event.getUploadItem();
		FileUpload file = new FileUpload();
		file.setName(item.getFileName());
		file.setLength(item.getData().length);
		file.setData(item.getData());
		try {
			veriOku(file);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());
			logger.debug(e.getMessage());
		}

		dosya = ortakIslemler.getDosyaFromFileUpload(file);
		dosyaTamam = false;
	}

	public String dosyaOku() throws IOException {
		dosyaTamam = false;
		Workbook wb = ortakIslemler.getWorkbook(dosya);
		vardiyaGunleri.clear();
		sicilNoList = new ArrayList<String>();
		// LinkedHashMap<String, Date> veriMap = new LinkedHashMap<String, Date>();
		LinkedHashMap<String, String> perMap = new LinkedHashMap<String, String>();
		LinkedHashMap<Long, HashMap<Integer, Object>> dataMap = new LinkedHashMap<Long, HashMap<Integer, Object>>();
		String datePattern = "yyyy-MM-dd", timePattern = "HH:mm:ss";
		if (wb != null) {
			Sheet sheet = wb.getSheetAt(0);
			tarih = new Date();
			try {
				long sira = 0;
				Cell cell = null;
				for (int j = 1; j <= sheet.getLastRowNum(); j++) {
					String sicilNo = "", adSoyad = "";

					Row row = sheet.getRow(j);

					try {
						cell = row.getCell(0);
						sicilNo = cell.getStringCellValue();
					} catch (Exception e) {
						try {
							Double numara = cell.getNumericCellValue();
							sicilNo = numara.longValue() + "";

						} catch (Exception e2) {
							sicilNo = "!!";
						}
					}

					try {
						cell = row.getCell(1);
						adSoyad = cell.getStringCellValue();
					} catch (Exception e) {

					}

					Date girisZaman = null, cikisZaman = null, zaman = null;
					try {
						zaman = ExcelUtil.getSheetDateValueTry(sheet, j, 2, "yyyy-MM-dd");
					} catch (Exception e) {
						zaman = null;
					}
					if (zaman == null) {
						try {
							cell = row.getCell(2);
							if (cell == null)
								break;
							String tarihStr = cell.getStringCellValue();
							zaman = PdksUtil.getDateFromString(tarihStr);
						} catch (Exception e) {
							continue;
						}

					}
					try {
						girisZaman = ExcelUtil.getSheetDateValueTry(sheet, j, 3, timePattern);
					} catch (Exception e) {
						girisZaman = null;
					}
					if (girisZaman == null) {
						cell = row.getCell(3);
						try {
							if (cell == null)
								break;
							String tarihStr = cell.getStringCellValue();
							if (tarihStr.indexOf(":") == tarihStr.lastIndexOf(":"))
								tarihStr += ":00";
							girisZaman = PdksUtil.convertToJavaDate(tarihStr, timePattern);
						} catch (Exception e) {
							continue;
						}

					}
					try {
						cikisZaman = ExcelUtil.getSheetDateValueTry(sheet, j, 4, timePattern);
					} catch (Exception e) {
						cikisZaman = null;
					}
					if (cikisZaman == null) {
						cell = row.getCell(4);
						try {
							if (cell == null)
								break;
							String tarihStr = cell.getStringCellValue();
							if (tarihStr.indexOf(":") == tarihStr.lastIndexOf(":"))
								tarihStr += ":00";
							cikisZaman = PdksUtil.convertToJavaDate(tarihStr, timePattern);
						} catch (Exception e) {
							continue;
						}

					}
					if (sicilNo.equals("!!"))
						break;
					if (zaman == null || girisZaman == null || cikisZaman == null)
						continue;

					++sira;
					HashMap<Integer, Object> map = new HashMap<Integer, Object>();
					map.put(0, sicilNo);
					map.put(1, adSoyad);
					map.put(2, PdksUtil.convertToDateString(zaman, datePattern) + " " + PdksUtil.convertToDateString(girisZaman, timePattern));
					map.put(3, PdksUtil.convertToDateString(zaman, datePattern) + " " + PdksUtil.convertToDateString(cikisZaman, timePattern));

					// veriMap.put(sicilNo, zaman);
					perMap.put(sicilNo, adSoyad);

					dataMap.put(sira, map);

				}
			} catch (Exception e) {
				logger.error("PDKS hata in : \n");
				e.printStackTrace();
				logger.error("PDKS hata out : " + e.getMessage());
			}
		}
		if (!dataMap.isEmpty()) {
			List<Long> dataIdList = new ArrayList(dataMap.keySet());
			List<String> perNoList = new ArrayList(perMap.keySet());
			String fieldName = "personelKGS.sicilNo";
			HashMap parametreMap = new HashMap();
			parametreMap.put(fieldName, perNoList);
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_MAP, "getPdksSicilNo");
			// parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			// TreeMap<String, Personel> personelMap = pdksEntityController.getObjectByInnerObjectMap(parametreMap, Personel.class, false);
			TreeMap<String, PersonelView> personelMap = ortakIslemler.getParamTreeMap(Boolean.FALSE, "getKgsSicilNo", false, perNoList, fieldName, parametreMap, PersonelView.class, session);
			List<Personel> list = new ArrayList<Personel>();

			for (SelectItem st : kapiList) {
				KapiView kapiView = new KapiView();
				kapiView.setId((Long) st.getValue());
				kapiView.setKapiAciklama(st.getLabel());

			}
			String pattern = datePattern + " " + timePattern;
			for (Long sira : dataIdList) {
				HashMap<Integer, Object> map = dataMap.get(sira);
				String pdksSicilNo = null;
				try {
					pdksSicilNo = (String) map.get(0);
				} catch (Exception e) {
					pdksSicilNo = "";
				}
				Personel personel = personelMap.containsKey(pdksSicilNo) ? personelMap.get(pdksSicilNo).getPdksPersonel() : null;
				if (personel == null)
					personel = new Personel();
				if (personel.getId() == null) {
					personel.setAd(perMap.get(pdksSicilNo));
					personel.setSoyad("");
					personel.setPdksSicilNo(pdksSicilNo);
					list.add(personel);
				} else {
					Date giris = PdksUtil.convertToJavaDate((String) map.get(2), pattern);
					Date cikis = PdksUtil.convertToJavaDate((String) map.get(3), pattern);
					VardiyaGun vgGiris = new VardiyaGun(personel, null, PdksUtil.getDate(giris));
					VardiyaGun vgCikis = new VardiyaGun(personel, null, PdksUtil.getDate(cikis));
					for (SelectItem st : kapiList) {
						Long kapiId = (Long) st.getValue();
						KapiView value = map1.get(kapiId);
						Kapi kapi = value.getKapi();
						VardiyaGun vg = kapi.isGirisKapi() ? vgGiris : vgCikis;
						Date zaman = kapi.isGirisKapi() ? giris : cikis;
						HareketKGS hareketKGS = new HareketKGS();
						hareketKGS.setKapiId(kapiId);
						hareketKGS.setKapiView(value);
						hareketKGS.setZaman(zaman);
						vg.setIlkGiris(hareketKGS);
					}
					vardiyaGunleri.add(vgGiris);
					vardiyaGunleri.add(vgCikis);

				}
			}
			dosyaTamam = list.isEmpty();
			if (dosyaTamam == false) {
				PdksUtil.addMessageAvailableInfo("Aşağıdaki personel" + (list.size() > 1 ? "ler" : "") + "  bilgileri bulunamadı!");
				for (Personel personel : list) {
					PdksUtil.addMessageAvailableWarn(personel.getPdksSicilNo() + " " + personel.getAdSoyad());
				}
			}
			list = null;
		}

		return "";
	}

	public void veriOku(FileUpload file) throws Exception {

	}

	public String clearUploadData() {
		files.clear();

		return null;
	}

	public long getTimeStamp() {
		return System.currentTimeMillis();
	}

	@Override
	public Object getId() {
		if (pdksYemekId == null) {
			return super.getId();
		} else {
			return pdksYemekId;
		}
	}

	@Override
	public void create() {
		super.create();
	}

	@Transactional
	public String dosyaKaydet() throws Exception {
		HareketKGS hareketler = this.getInstance();
		KapiView kapiView = new KapiView();
		kapiView.setId(kapiId);
		hareketler.setKapiView(kapiView);
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		List<Long> kapiIdList = new ArrayList<Long>();
		kapiIdList.add(kapiId);
		List<Long> personelId = new ArrayList<Long>();
		Calendar cal = Calendar.getInstance();
		for (VardiyaGun vg : vardiyaGunleri)
			personelId.add(vg.getPersonel().getPersonelKGS().getId());
		kgsList = ortakIslemler.getHareketAktifBilgileri(kapiIdList, personelId, tarih, ortakIslemler.tariheGunEkleCikar(cal, tarih, 1), HareketKGS.class, session);
		TreeMap<Long, List<HareketKGS>> hMap = new TreeMap<Long, List<HareketKGS>>();
		for (HareketKGS hareketKGS : kgsList) {
			Long key = hareketKGS.getPersonelId();
			List<HareketKGS> list = hMap.containsKey(key) ? hMap.get(key) : new ArrayList<HareketKGS>();
			if (list.isEmpty())
				hMap.put(key, list);
			list.add(hareketKGS);
		}
		Date bugun = Calendar.getInstance().getTime();
		boolean hareketTarihKontrol = ortakIslemler.getParameterKeyHasStringValue("hareketTarihKontrol");
		String formatStr = "yyyy-MM-dd HH:mm";
		personelId = null;
		kapiId = null;
		Tanim neden = nedenId != null ? new Tanim(nedenId) : null;
		for (Iterator iterator1 = vardiyaGunleri.iterator(); iterator1.hasNext();) {
			VardiyaGun vg = (VardiyaGun) iterator1.next();
			HareketKGS hareketKGS = vg.getIlkGiris();
			Date zaman = hareketKGS.getZaman();
			String zamanStr = PdksUtil.convertToDateString(zaman, formatStr);
			if ((hareketTarihKontrol && zaman.after(bugun)))
				continue;
			PersonelKGS personel = vg.getPersonel().getPersonelKGS();
			Boolean yaz = true;
			List<HareketKGS> list = hMap.containsKey(personel.getId()) ? hMap.get(personel.getId()) : new ArrayList<HareketKGS>();
			for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
				HareketKGS kgs = (HareketKGS) iterator2.next();
				String dateStr = PdksUtil.convertToDateString(new Date(kgs.getZaman().getTime()), formatStr);
				if ((tarih == null || kapiId == null) && zamanStr.equals(dateStr))
					yaz = Boolean.FALSE;
				iterator2.remove();

			}
			if (yaz) {
				String nedenAciklama = PdksUtil.hasStringValue(vg.getVardiyaKisaAciklama()) ? vg.getVardiyaKisaAciklama() : hareketler.getIslem().getAciklama();

				KapiView kv = hareketKGS.getKapiView();

				pdksEntityController.hareketEkle(kv, personel.getPersonelView(), hareketKGS.getZaman(), authenticatedUser, neden.getId(), nedenAciklama, session);

			}

		}
		pdksEntityController.sessionFlush(session);
		dosyaSifirla();
		kgsList = null;
		return "persisted";

	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			pdksEntityController.sessionRefresh(session, entityManager, getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		Connection connection = pdksEntityController.sessionKapat(session);
		if (pdksEntityController.isSessionKapali(connection, session))
			session = PdksUtil.getSessionUserCalistiSayfa(entityManager, authenticatedUser, sayfaURL);
		try {
			ortakIslemler.setUserMenuItemTime(entityManager, session, sayfaURL);
			if (authenticatedUser.isAdmin() == false || aramaSecenekleri == null)
				aramaSecenekleri = new AramaSecenekleri(authenticatedUser);
			aramaSecenekleri.setSessionClear(Boolean.FALSE);
			aramaSecenekleri.setStajyerOlmayanSirket(Boolean.TRUE);
			tarih = null;
			HareketKGS hareket = new HareketKGS();
			hareket.setPersonel(new PersonelView());
			hareket.setKapiView(new KapiView());
			hareket.setIslem(new PersonelHareketIslem());
			setInstance(hareket);
			fillKapiList();
			fillHareketIslemList();
			vardiyaGunleri.clear();
			dosyaSifirla();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

	}

	public ArrayList<FileUpload> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<FileUpload> files) {
		this.files = files;
	}

	public ArrayList<String> getSicilNoList() {
		return sicilNoList;
	}

	public void setSicilNoList(ArrayList<String> sicilNoList) {
		this.sicilNoList = sicilNoList;
	}

	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	public int getSaat() {
		return saat;
	}

	public void setSaat(int saat) {
		this.saat = saat;
	}

	public int getDakika() {
		return dakika;
	}

	public void setDakika(int dakika) {
		this.dakika = dakika;
	}

	public List<Tanim> getHareketIslemList() {
		return hareketIslemList;
	}

	public void setHareketIslemList(List<Tanim> hareketIslemList) {
		this.hareketIslemList = hareketIslemList;
	}

	public List<SelectItem> getKapiList() {
		return kapiList;
	}

	public void setKapiList(List<SelectItem> kapiList) {
		this.kapiList = kapiList;
	}

	public ArrayList<PersonelView> getExcelList() {
		return excelList;
	}

	public void setExcelList(ArrayList<PersonelView> excelList) {
		this.excelList = excelList;
	}

	public HashMap<String, List<Tanim>> getEkSahaListMap() {
		return ekSahaListMap;
	}

	public void setEkSahaListMap(HashMap<String, List<Tanim>> ekSahaListMap) {
		this.ekSahaListMap = ekSahaListMap;
	}

	public TreeMap<String, Tanim> getEkSahaTanimMap() {
		return ekSahaTanimMap;
	}

	public void setEkSahaTanimMap(TreeMap<String, Tanim> ekSahaTanimMap) {
		this.ekSahaTanimMap = ekSahaTanimMap;
	}

	public AramaSecenekleri getAramaSecenekleri() {
		return aramaSecenekleri;
	}

	public void setAramaSecenekleri(AramaSecenekleri aramaSecenekleri) {
		this.aramaSecenekleri = aramaSecenekleri;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Dosya getDosya() {
		return dosya;
	}

	public void setDosya(Dosya dosya) {
		this.dosya = dosya;
	}

	public List<VardiyaGun> getVardiyaGunleri() {
		return vardiyaGunleri;
	}

	public void setVardiyaGunleri(List<VardiyaGun> vardiyaGunleri) {
		this.vardiyaGunleri = vardiyaGunleri;
	}

	public boolean isDosyaTamam() {
		return dosyaTamam;
	}

	public void setDosyaTamam(boolean dosyaTamam) {
		this.dosyaTamam = dosyaTamam;
	}

	public Long getKapiId() {
		return kapiId;
	}

	public void setKapiId(Long kapiId) {
		this.kapiId = kapiId;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		ArgeGirisCikisEkleHome.sayfaURL = sayfaURL;
	}

	public Long getNedenId() {
		return nedenId;
	}

	public void setNedenId(Long nedenId) {
		this.nedenId = nedenId;
	}

	public List<SelectItem> getNedenList() {
		return nedenList;
	}

	public void setNedenList(List<SelectItem> nedenList) {
		this.nedenList = nedenList;
	}

}
