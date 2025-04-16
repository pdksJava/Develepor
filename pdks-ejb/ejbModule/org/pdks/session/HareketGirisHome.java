package org.pdks.session;

import java.io.IOException;
import java.io.OutputStream;
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

@Name("hareketGirisHome")
public class HareketGirisHome extends EntityHome<HareketKGS> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5565958869291032758L;
	static Logger logger = Logger.getLogger(HareketGirisHome.class);

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

	public static String sayfaURL = "hareketGiris";
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
	private boolean dosyaTamam = false;
	private Long kapiId, nedenId;
	private Dosya dosya;
	private Session session;

	private void fillEkSahaTanim() {
		// HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		HashMap sonucMap = ortakIslemler.fillEkSahaTanimAramaSecenekAta(session, Boolean.FALSE, null, aramaSecenekleri);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));

	}

	public void fillKapiList() {
		kapiList = ortakIslemler.getSelectItemList("kapi", authenticatedUser);
		List<KapiView> kapiViewList = new ArrayList<KapiView>();
		HashMap parametreMap = new HashMap();
		parametreMap.put("kapi.durum", Boolean.TRUE);
		parametreMap.put("kapi.pdks", Boolean.TRUE);
		if (session != null)
			parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
		List<KapiKGS> list = pdksEntityController.getObjectByInnerObjectList(parametreMap, KapiKGS.class);
		KapiKGS manuelGiris = null, manuelCikis = null;
		for (KapiKGS kapiKGS : list)
			kapiViewList.add(kapiKGS.getKapiView());

		HashMap<String, KapiView> manuelKapiMap = ortakIslemler.getManuelKapiMap(kapiViewList, session);
		manuelGiris = manuelKapiMap.containsKey(Kapi.TIPI_KODU_GIRIS) ? manuelKapiMap.get(Kapi.TIPI_KODU_GIRIS).getKapiKGS() : null;
		manuelCikis = manuelKapiMap.containsKey(Kapi.TIPI_KODU_CIKIS) ? manuelKapiMap.get(Kapi.TIPI_KODU_CIKIS).getKapiKGS() : null;
		manuelKapiMap = null;
		if (manuelGiris != null && manuelCikis != null) {
			kapiViewList.clear();
			kapiViewList.add(manuelGiris.getKapiView());
			kapiViewList.add(manuelCikis.getKapiView());
		} else
			kapiViewList = PdksUtil.sortObjectStringAlanList(kapiViewList, "getAciklama", null);

		for (KapiView kapiView : kapiViewList) {
			kapiList.add(new SelectItem(kapiView.getId(), kapiView.getKapi().getAciklama()));
		}

	}

	public void fillHareketIslemList() {
		List<Tanim> islemList = ortakIslemler.getTanimList(Tanim.TIPI_HAREKET_NEDEN, session);
		if (nedenList == null)
			nedenList = new ArrayList<SelectItem>();
		else
			nedenList.clear();
		for (Iterator iterator = islemList.iterator(); iterator.hasNext();) {
			Tanim tanim = (Tanim) iterator.next();
			nedenList.add(new SelectItem(tanim.getId(), tanim.getAciklama()));
		}
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
		if (wb != null) {
			Sheet sheet = wb.getSheetAt(0);
			String dateStr = PdksUtil.convertToDateString(tarih, "yyyy-MM-dd") + " " + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(saat), '0', 2) + ":" + PdksUtil.textBaslangicinaKarakterEkle(String.valueOf(dakika), '0', 2);
			Date islemZamani = PdksUtil.convertToJavaDate(dateStr, "yyyy-MM-dd HH:mm");
			Double kapiDeger = new Double(kapiId != null ? kapiId : 0L);
			try {
				long adet = 0;
				long sira = 0;
				for (int j = 1; j <= sheet.getLastRowNum(); j++) {
					String sicilNo = "", adSoyad = "";
					Date zaman = null;
					Row row = sheet.getRow(j);
					try {
						sicilNo = row.getCell(0).getStringCellValue();
					} catch (Exception e) {
						try {
							Double numara = row.getCell(0).getNumericCellValue();
							sicilNo = numara.longValue() + "";

						} catch (Exception e2) {
							sicilNo = "!!!!";
						}
					}
					try {
						adSoyad = row.getCell(1).getStringCellValue();
					} catch (Exception e) {
						try {
							adSoyad = "";

						} catch (Exception e2) {
							sicilNo = "!!!!";
						}
					}
					try {
						zaman = ExcelUtil.getSheetDateValueTry(sheet, j, 2, "HH:mm:ss");
					} catch (Exception e) {

					}
					if (zaman == null)
						zaman = islemZamani;
					if (sicilNo.length() > 0)
						sicilNo = ortakIslemler.getSicilNo(sicilNo);
					else {
						if (adet < 3) {
							++adet;
							continue;
						}

						break;
					}
					++sira;
					adet = 0;
					// veriMap.put(sicilNo, zaman);
					perMap.put(sicilNo, adSoyad);
					HashMap<Integer, Object> map = new HashMap<Integer, Object>();

					for (int i = 0; i < 6; i++) {
						try {
							Cell cell = row.getCell(i);
							if (cell != null) {
								switch (cell.getCellType()) {
								case Cell.CELL_TYPE_STRING:
									if (cell.getStringCellValue() != null)
										map.put(i, cell.getStringCellValue());
									break;
								case Cell.CELL_TYPE_NUMERIC:
									if (i == 2 && cell.getDateCellValue() != null)
										map.put(i, cell.getDateCellValue());
									else
										map.put(i, cell.getNumericCellValue());
									break;
								case Cell.CELL_TYPE_BOOLEAN:

									map.put(i, cell.getBooleanCellValue());
									break;

								default:
									if (cell.getDateCellValue() != null)
										map.put(i, cell.getDateCellValue());
									break;
								}

							} else {
								if (i == 2)
									map.put(i, islemZamani);
								else if (i == 3)
									map.put(i, kapiDeger);

							}
						} catch (Exception e) {

						}

					}
					if (zaman != null)
						map.put(-1, zaman);
					if (!map.isEmpty())
						dataMap.put(sira, map);
					else
						map = null;

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
			HashMap<Long, KapiView> map1 = new HashMap<Long, KapiView>();
			for (SelectItem st : kapiList) {
				KapiView kapiView = new KapiView();
				kapiView.setId((Long) st.getValue());
				kapiView.setKapiAciklama(st.getLabel());
				map1.put(kapiView.getId(), kapiView);

			}
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
					Date tarih = map.containsKey(2) ? (Date) map.get(2) : null, vardiyaDate = null;
					if (tarih == null) {
						vardiyaDate = zamanGuncelle();
					} else {

						vardiyaDate = tarih;

					}
					VardiyaGun vg = new VardiyaGun(personel, null, PdksUtil.getDate(vardiyaDate));
					Long kapi = null;
					if (map.containsKey(3)) {
						try {
							if (map.get(3) instanceof Double) {
								Double veri = (Double) map.get(3);

								for (SelectItem st : kapiList) {
									if (st.getValue().equals(veri.longValue()))
										kapi = veri.longValue();

								}
								if (kapi != null)
									vg.setId(kapi);
							}
						} catch (Exception e) {

						}
					}
					if (kapi == null)
						kapi = kapiId;
					if (kapi != null && map1.containsKey(kapi)) {
						HareketKGS hareketKGS = new HareketKGS();
						hareketKGS.setKapiId(kapi);
						hareketKGS.setKapiView(map1.get(kapi));
						hareketKGS.setZaman(vardiyaDate);
						vg.setIlkGiris(hareketKGS);
					}

					if (map.containsKey(4)) {
						try {
							if (map.get(4) instanceof String) {
								String veri = (String) map.get(4);
								vg.setVardiyaKisaAciklama(veri);
							}
						} catch (Exception e) {

						}
					}
					vardiyaGunleri.add(vg);
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
	@Begin(join = true)
	public void create() {
		super.create();
	}

	private Date zamanGuncelle() {

		Date zaman = PdksUtil.setTarih(tarih, Calendar.HOUR_OF_DAY, saat);
		zaman = PdksUtil.setTarih(zaman, Calendar.MINUTE, dakika);
		zaman = PdksUtil.setTarih(zaman, Calendar.SECOND, 0);
		zaman = PdksUtil.setTarih(zaman, Calendar.MILLISECOND, 0);
		return zaman;
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
		session.flush();
		dosyaSifirla();
		kgsList = null;
		return "persisted";

	}

	public void instanceRefresh() {
		if (getInstance().getId() != null)
			session.refresh(getInstance());
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		try {
			ortakIslemler.setUserMenuItemTime(session, sayfaURL);
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
			fillEkSahaTanim();
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
		HareketGirisHome.sayfaURL = sayfaURL;
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
