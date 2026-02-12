package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
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
import org.pdks.entity.HareketKGS;
import org.pdks.entity.Kapi;
import org.pdks.entity.Liste;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;

@Name("devamsizlikRaporuHome")
public class DevamsizlikRaporuHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4025960383128256337L;
	static Logger logger = Logger.getLogger(DevamsizlikRaporuHome.class);

	@RequestParameter
	Long kgsHareketId;
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

	public static String sayfaURL = "devamsizlikRaporu";
	private Date date, bitisTarih;
	List<Personel> devamsizlikList = new ArrayList<Personel>();
	List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
	List<Personel> personelList = new ArrayList<Personel>();

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();
	private boolean izinliGoster = Boolean.FALSE, hepsiniGoster = Boolean.FALSE, hareketleriGoster = Boolean.TRUE;

	private List<Liste> durumList = new ArrayList<Liste>();
	private HashMap<String, List<Tanim>> ekSahaListMap;
	private TreeMap<String, Tanim> ekSahaTanimMap;
	private String bolumAciklama;
	private Session session;

	@In(required = false)
	FacesMessages facesMessages;

	@Override
	public Object getId() {
		if (kgsHareketId == null) {
			return super.getId();
		} else {
			return kgsHareketId;
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
		// default bugun icin ise gelmeyen raporu cekili olsun
		Date dateBas = PdksUtil.buGun();
		setDate(dateBas);
		setBitisTarih(dateBas);
		vardiyaGunList.clear();
		durumList.clear();

		durumList.add(new Liste(1, "Erken Giriş"));
		durumList.add(new Liste(2, "Erken Çıkış"));
		durumList.add(new Liste(3, "Geç Giriş"));
		durumList.add(new Liste(4, "Geç Çıkış"));
		durumList.add(new Liste(5, "Eksik Kart Basıldı"));
		durumList.add(new Liste(6, "Plansız Giriş"));
		durumList.add(new Liste(7, "Kart Basılmadı"));
		Liste izinli = new Liste(8, "İzinli");
		izinli.setSecili(false);
		durumList.add(izinli);

		// devamsizlikListeOlustur();

	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		setEkSahaListMap((HashMap<String, List<Tanim>>) sonucMap.get("ekSahaList"));
		setEkSahaTanimMap((TreeMap<String, Tanim>) sonucMap.get("ekSahaTanimMap"));
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public Date getDate() {
		return date;
	}

	public String excelAktar() {
		ByteArrayOutputStream baosDosya = null;
		try {
			baosDosya = excelAktarDevam();
			if (baosDosya != null) {
				String dosyaAdi = null;
				if (bitisTarih == null || PdksUtil.tarihKarsilastirNumeric(date, bitisTarih) == 0)
					dosyaAdi = "DevamsizlikRaporu_" + PdksUtil.convertToDateString(date, "yyyy_MM_dd") + ".xlsx";
				else
					dosyaAdi = "DevamsizlikRaporu_" + PdksUtil.convertToDateString(date, "yyyyMMdd") + "_" + PdksUtil.convertToDateString(bitisTarih, "yyyyMMdd") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {

		}

		return "";
	}

	private ByteArrayOutputStream excelAktarDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, PdksUtil.convertToDateString(date, "d MMMMM yyyy") + " Devamsizlik Raporu", Boolean.TRUE);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);

		CreationHelper helper = wb.getCreationHelper();
		ClientAnchor anchor = helper.createClientAnchor();
		Drawing drawing = sheet.createDrawingPatriarch();
		int row = 0, col = 0;
		boolean aciklamaGoster = (authenticatedUser.isIK() || authenticatedUser.isAdmin()) || izinliGoster || hepsiniGoster;

		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		boolean tesisDurum = ortakIslemler.getListTesisDurum(vardiyaGunList);
		boolean tekTarih = bitisTarih == null || PdksUtil.tarihKarsilastirNumeric(date, bitisTarih) == 0;
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı Soyadı");
		if (tekTarih == false)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tarihAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.vardiyaAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Giriş");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çıkış");
		if (aciklamaGoster)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Açıklama");
		if (hareketleriGoster) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Kapı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Zaman");
		}
		boolean renk = true;
		for (VardiyaGun vardiyaGun : vardiyaGunList) {
			Personel personel = vardiyaGun.getPersonel();
			Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
			List hareketler = hareketleriGoster ? vardiyaGun.getHareketler() : null;
			boolean sifirla = hareketler == null;
			if (sifirla) {
				hareketler = new ArrayList<HareketKGS>();
				hareketler.add(null);
			}
			for (Object hareket : hareketler) {
				HareketKGS hareketKGS = hareket != null ? (HareketKGS) hareket : null;
				row++;
				col = 0;
				CellStyle style = null, styleCenter = null, cellStyleDateTime = null, cellStyleDate = null;
				if (renk) {
					cellStyleDate = styleOddDate;
					cellStyleDateTime = styleOddDateTime;
					style = styleOdd;
					styleCenter = styleOddCenter;

				} else {
					cellStyleDate = styleEvenDate;
					cellStyleDateTime = styleEvenDateTime;
					style = styleEven;
					styleCenter = styleEvenCenter;
				}
				renk = !renk;
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getSirket().getAd());
				if (tesisDurum)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
				if (personel.getYoneticisi() != null) {
					Personel yonetici = personel.getYoneticisi();
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(yonetici.getPdksSicilNo() + " - " + yonetici.getAdSoyad());
				} else {
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				}
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
				if (tekTarih == false)
					ExcelUtil.getCell(sheet, row, col++, cellStyleDate).setCellValue(vardiyaGun.getVardiyaDate());
				Cell vardiyaCell = ExcelUtil.getCell(sheet, row, col++, styleCenter);

				vardiyaCell.setCellValue(islemVardiya.getKisaAdi());
				String vardiyaTitle = authenticatedUser.timeFormatla(islemVardiya.getVardiyaBasZaman()) + " - " + authenticatedUser.timeFormatla(islemVardiya.getVardiyaBitZaman());
				ExcelUtil.setCellComment(vardiyaCell, anchor, helper, drawing, vardiyaTitle);
				if (vardiyaGun.getGirisHareketleri() != null)
					ExcelUtil.getCell(sheet, row, col++, cellStyleDateTime).setCellValue(vardiyaGun.getGirisHareket().getOrjinalZaman());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (vardiyaGun.getCikisHareketleri() != null)
					ExcelUtil.getCell(sheet, row, col++, cellStyleDateTime).setCellValue(vardiyaGun.getCikisHareket().getOrjinalZaman());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (aciklamaGoster) {
					String aciklama = getVardiyaAciklama(vardiyaGun);
					CellStyle styleIzin = vardiyaGun.getIzin() == null ? style : header;
					Cell createCell = ExcelUtil.getCell(sheet, row, col++, styleIzin);
					createCell.setCellValue(aciklama);
					if (vardiyaGun.getIzin() != null) {
						String title = vardiyaGun.getIzin().getIzinTipiAciklama();
						ExcelUtil.setCellComment(createCell, anchor, helper, drawing, title);
					}
				}
				if (hareketleriGoster) {
					if (hareketKGS != null) {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue(hareketKGS.getKapiView().getKapi().getAciklama());
						ExcelUtil.getCell(sheet, row, col++, cellStyleDateTime).setCellValue(hareketKGS.getZaman());
					} else {
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					}
				}
			}
			if (sifirla)
				hareketler = null;
		}

		try {

			for (int i = 0; i <= col; i++)
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

	/**
	 * @param vg
	 * @return
	 */
	public String getVardiyaAciklama(VardiyaGun vg) {
		String aciklama = null;
		int girisAdet = vg.getGirisHareketleri() != null ? vg.getGirisHareketleri().size() : 0;
		int cikisAdet = vg.getCikisHareketleri() != null ? vg.getCikisHareketleri().size() : 0;
		if (vg.getIzin() != null) {
			aciklama = "İzinli";
			if (girisAdet + cikisAdet > 0)
				aciklama += " (Geçiş bilgisi mevcut)";
			aciklama += ".";
		} else if (vg.getVersion() == 0) {

			Vardiya vardiya = vg.getIslemVardiya();
			if (vardiya.isCalisma() == false) {
				if (girisAdet + cikisAdet > 0)
					aciklama = "Plansız Giriş.";
			} else {
				if (vg.getNormalSure() > 0.0) {
					aciklama = "";
				} else if (girisAdet + cikisAdet == 0) {
					if (vardiya.isCalisma())
						aciklama = "Kart Basılmadı.";
				} else if (cikisAdet != girisAdet) {
					aciklama = "Eksik Kart Basıldı.";
					Date giris = null;
					StringBuffer sb = new StringBuffer();
					try {
						if (girisAdet > 0) {
							giris = vg.getGirisHareketleri().get(0).getOrjinalZaman();
							if (giris.before(vardiya.getVardiyaTelorans1BasZaman()))
								sb.append("Erken Giriş.");
							else if (giris.after(vardiya.getVardiyaTelorans2BasZaman()))
								sb.append("Geç Giriş.");
						}
					} catch (Exception e) {
					}
					try {
						if (cikisAdet > 0) {
							Date cikis = vg.getGirisHareketleri().get(cikisAdet - 1).getOrjinalZaman();
							if (cikis.after(vardiya.getVardiyaTelorans2BitZaman()))
								sb.append("Geç Çıkış.");
							else if (cikis.before(vardiya.getVardiyaTelorans1BitZaman()))
								sb.append("Erken Çıkış.");
						} else if (vardiya.getVardiyaTelorans1BitZaman().after(new Date()))
							aciklama = null;
					} catch (Exception e) {
						// TODO: handle exception
					}

					if (sb.length() > 0)
						aciklama = sb.toString();
				}
			}
		}
		if (aciklama == null)
			logger.debug(vg.getVardiyaKeyStr() + " " + PdksUtil.getCurrentTimeStampStr());

		return aciklama;
	}

	/**
	 * @param pdksVardiyaGun
	 */
	public void hareketGoster(VardiyaGun pdksVardiyaGun) {
		setInstance(pdksVardiyaGun);
		List<HareketKGS> kgsList = pdksVardiyaGun.getHareketler();
		setHareketList(kgsList);

	}

	public String devamsizlikListeOlustur() {
		try {
			if (vardiyaGunList != null)
				vardiyaGunList.clear();
			else
				vardiyaGunList = new ArrayList<VardiyaGun>();
			boolean devam = hepsiniGoster;
			for (Liste liste : durumList) {
				if (devam == false)
					devam = liste.isSecili();

			}
			if (devam == false)
				PdksUtil.addMessageWarn("Hata durum seçiniz!");
			else if (ortakIslemler.ileriTarihSeciliDegil(date))
				devamsizlikListeRaporuOlustur();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return "";
	}

	private void devamsizlikListeRaporuOlustur() {

		/*
		 * yetkili oldugu Tum personellerin uzerinden dönülür,tek tarih icin cekilir. Vardiyadaki calismasi gereken saat ile hareketten calistigi saatler karsilastirilir. Eksik varsa izin var mi diye bakilir. Diyelim 4 saat eksik calisti 2 saat mazeret buldu. Hala 2 saat eksik vardir. Bunu
		 * gosteririrz. Diyelim hic mazeret girmemiş 4 saat gösteririz
		 */
		List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>();
		izinliGoster = hepsiniGoster || durumList.get(durumList.size() - 1).isSecili();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		Date tarih1 = null;
		Date tarih2 = null;
		ArrayList<Personel> tumPersoneller = (ArrayList<Personel>) authenticatedUser.getTumPersoneller().clone();
		for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
			Personel pdksPersonel = (Personel) iterator.next();
			// if (!pdksPersonel.getPdksSicilNo().equals("0883"))
			// iterator.remove();
			// else
			if (pdksPersonel.getPdks() == null || !pdksPersonel.getPdks())
				iterator.remove();
			else if (pdksPersonel.getSirket().isPdksMi() == false)
				iterator.remove();

		}
		if (!tumPersoneller.isEmpty()) {
			Calendar cal = Calendar.getInstance();
			Date date2 = bitisTarih == null ? date : bitisTarih;
			Date basTarih = ortakIslemler.tariheGunEkleCikar(cal, date, -2);
			Date bitTarih = ortakIslemler.tariheGunEkleCikar(cal, date2, 1);
			TreeMap<String, VardiyaGun> vardiyaMap = null;
			try {
				vardiyaMap = ortakIslemler.getIslemVardiyalar((List<Personel>) tumPersoneller, basTarih, bitTarih, Boolean.FALSE, session, Boolean.TRUE);
				// boolean islem = ortakIslemler.getVardiyaHareketIslenecekList(new ArrayList<VardiyaGun>(vardiyaMap.values()), date, date2, session);
				// if (islem)
				// vardiyaMap = ortakIslemler.getIslemVardiyalar((List<Personel>) tumPersoneller, basTarih, bitTarih, Boolean.FALSE, session, Boolean.TRUE);

			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
			vardiyaList = vardiyaMap != null ? new ArrayList<VardiyaGun>(vardiyaMap.values()) : new ArrayList<VardiyaGun>();
			ortakIslemler.sonrakiGunVardiyalariAyikla(date2, vardiyaList, session);
			// butun personeller icin hareket cekerken bu en kucuk tarih ile en
			// buyuk tarih araligini kullanacaktir
			// bu araliktaki tum hareketleri cekecektir.
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
				if (pdksVardiyaGun.getVardiyaDate().before(date) || pdksVardiyaGun.getVardiyaDate().after(date2)) {
					iterator.remove();
					continue;

				}
				if (pdksVardiyaGun.getVardiya() == null || !pdksVardiyaGun.getVardiya().isCalisma()) {
					iterator.remove();
					continue;
				}

				if (tarih1 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman().getTime() < tarih1.getTime())
					tarih1 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans1BasZaman();

				if (tarih2 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman().getTime() > tarih2.getTime())
					tarih2 = pdksVardiyaGun.getIslemVardiya().getVardiyaTelorans2BitZaman();

			}
			if (tarih1 != null && tarih2 != null) {

				List<Long> kapiIdler = ortakIslemler.getPdksDonemselKapiIdler(tarih1, tarih2, session);
				kgsList = null;
				if (kapiIdler != null && !kapiIdler.isEmpty()) {
					try {
						kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, (List<Personel>) tumPersoneller.clone(), tarih1, tarih2, HareketKGS.class, session);

					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}

				}
				if (kgsList == null)
					kgsList = new ArrayList<HareketKGS>();
				if (!kgsList.isEmpty()) {
					for (Iterator iterator = kgsList.iterator(); iterator.hasNext();) {
						HareketKGS kgsHareket = (HareketKGS) iterator.next();
						try {
							if (kgsHareket.getPersonel().getPdksPersonel() != null && !kgsHareket.getPersonel().getPdksPersonel().getPdks())
								iterator.remove();

						} catch (Exception e) {
							logger.error("PDKS hata in : \n");
							e.printStackTrace();
							logger.error("PDKS hata out : " + e.getMessage());
							iterator.remove();
						}

					}
					if (kgsList.size() > 1)
						kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);

				}

				try {
					HashMap<Long, List<HareketKGS>> hareketMap = new HashMap<Long, List<HareketKGS>>();
					HashMap<Long, List<PersonelIzin>> izinMap = new HashMap<Long, List<PersonelIzin>>();
					for (Iterator iterator2 = izinList.iterator(); iterator2.hasNext();) {
						PersonelIzin personelIzin = (PersonelIzin) iterator2.next();
						Long id = personelIzin.getIzinSahibi().getId();
						List<PersonelIzin> list = izinMap.containsKey(id) ? izinMap.get(id) : new ArrayList<PersonelIzin>();
						if (list.isEmpty())
							izinMap.put(id, list);
						list.add(personelIzin);

					}
					for (Iterator iterator1 = kgsList.iterator(); iterator1.hasNext();) {
						HareketKGS kgsHareket = (HareketKGS) iterator1.next();
						Long id = kgsHareket.getPersonel().getPdksPersonel().getId();
						List<HareketKGS> list = hareketMap.containsKey(id) ? hareketMap.get(id) : new ArrayList<HareketKGS>();
						if (list.isEmpty())
							hareketMap.put(id, list);
						list.add(kgsHareket);
					}
					for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
						VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
						if (!vardiyaGun.getIslemVardiya().isCalisma()) {
							iterator.remove();
							continue;
						}

						String aciklama = null;
						Vardiya vardiya = vardiyaGun.getVardiya();
						vardiyaGun.setHareketler(null);
						vardiyaGun.setGirisHareketleri(null);
						vardiyaGun.setCikisHareketleri(null);
						vardiyaGun.setGecersizHareketler(null);
						Long id = vardiyaGun.getPersonel().getId();
						if (hareketMap.containsKey(id)) {
							List<HareketKGS> list = hareketMap.get(id);
							for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
								HareketKGS kgsHareket = (HareketKGS) iterator1.next();
								if (vardiyaGun.addHareket(kgsHareket, Boolean.TRUE))
									iterator1.remove();
							}
						}

						boolean yaz = Boolean.TRUE;
						ArrayList<HareketKGS> girisHareketleriList = vardiyaGun.getGirisHareketleri();
						ArrayList<HareketKGS> cikisHareketleriList = vardiyaGun.getCikisHareketleri();
						int girisAdet = girisHareketleriList != null ? girisHareketleriList.size() : 0;
						int cikisAdet = cikisHareketleriList != null ? cikisHareketleriList.size() : 0;
						if (girisAdet + cikisAdet == 0) {
							boolean sil = false;
							if (vardiya.isCalisma() == false)
								sil = true;
							else if (vardiyaGun.getIzin() != null && izinliGoster == false)
								sil = true;
							if (sil) {
								iterator.remove();
								continue;
							}

						}
						boolean kontrolEt = true;
						if (vardiya.isCalisma()) {
							if (girisAdet + cikisAdet > 0) {
								PersonelIzin izin = vardiyaGun.getIzin();
								boolean izinDurum = Boolean.FALSE;
								if (izin != null) {
									long izinBaslangic = izin.getBaslangicZamani().getTime();
									long izinBitis = izin.getBitisZamani().getTime();
									izinDurum = vardiyaGun.getIslemVardiya().getVardiyaBasZaman().getTime() <= izinBitis && vardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime() >= izinBaslangic;
								}
								if (izinDurum) {
									izinDurum = izinliGoster;
									yaz = izinDurum;

								}
								if (vardiyaGun.getPdksPersonel().getPdksSicilNo().equals("4807"))
									logger.debug("");
								if (girisAdet == cikisAdet) {
									boolean hataVar = false, giris = true;
									for (HareketKGS hareketKGS : vardiyaGun.getHareketler()) {
										Kapi kapi = hareketKGS.getKapiKGS() != null ? hareketKGS.getKapiKGS().getKapi() : null;
										if (kapi != null) {
											if (giris) {
												if (kapi.isGirisKapi() == false)
													hataVar = true;
											} else if (kapi.isCikisKapi() == false)
												hataVar = true;
										} else
											hataVar = true;
										giris = !giris;
									}

									double calismaSaati = 0;

									if (hataVar == false && vardiyaGun.getIzin() == null) {
										for (int i = 0; i < girisHareketleriList.size(); i++) {
											HareketKGS girisHareket = girisHareketleriList.get(i);
											HareketKGS cikisHareket = cikisHareketleriList.get(i);
											if (girisHareket == null || cikisHareket == null)
												continue;
											if (girisHareket.getZaman() == null || cikisHareket.getZaman() == null)
												continue;
											calismaSaati += PdksUtil.getSaatFarki(cikisHareket.getZaman(), girisHareket.getZaman());
										}
										if (calismaSaati > 0) {
											double netSure = vardiya.getNetCalismaSuresi();
											yaz = hepsiniGoster || izinDurum;
											if (calismaSaati > netSure)
												calismaSaati = netSure;
											// eksik saati bulunup ekranda gosterilmelidir.
											// double eksikSaat = netSure > 0 ? netSure - calismaSaati : 0.0d;
											// vardiyaGun.setNormalSure(eksikSaat);
										}
										kontrolEt = hepsiniGoster;

									}

									vardiyaGun.setNormalSure(calismaSaati);
								}
							}

						}
						yaz = hepsiniGoster;
						if (kontrolEt) {
							aciklama = getVardiyaAciklama(vardiyaGun);
							if (aciklama != null && hepsiniGoster == false) {
								for (Liste liste : durumList) {
									if (liste.isSecili()) {
										if (aciklama.indexOf((String) liste.getValue()) >= 0) {
											yaz = true;
										}
									}
								}
							}
						}
						if (!yaz)
							iterator.remove();

					}
					ortakIslemler.otomatikHareketEkle(new ArrayList<VardiyaGun>(vardiyaMap.values()), session);
				} catch (Exception e) {
					logger.error("PDKS hata in : \n");
					e.printStackTrace();
					logger.error("PDKS hata out : " + e.getMessage());

				}
			}
		}
		ortakIslemler.vardiyaGunSirala(vardiyaList, authenticatedUser, session);
		if (!vardiyaList.isEmpty())
			fillEkSahaTanim();
		setVardiyaGunList(vardiyaList);

	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Personel> getDevamsizlikList() {
		return devamsizlikList;
	}

	public void setDevamsizlikList(List<Personel> devamsizlikList) {
		this.devamsizlikList = devamsizlikList;
	}

	public List<PersonelIzin> getIzinList() {
		return izinList;
	}

	public void setIzinList(List<PersonelIzin> izinList) {
		this.izinList = izinList;
	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
	}

	public List<VardiyaGun> getVardiyaGunList() {
		return vardiyaGunList;
	}

	public void setVardiyaGunList(List<VardiyaGun> vardiyaGunList) {
		this.vardiyaGunList = vardiyaGunList;
	}

	public List<Personel> getPersonelList() {
		return personelList;
	}

	public void setPersonelList(List<Personel> personelList) {
		this.personelList = personelList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public boolean isHareketleriGoster() {
		return hareketleriGoster;
	}

	public void setHareketleriGoster(boolean hareketleriGoster) {
		this.hareketleriGoster = hareketleriGoster;
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

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		DevamsizlikRaporuHome.sayfaURL = sayfaURL;
	}

	public Date getBitisTarih() {
		return bitisTarih;
	}

	public void setBitisTarih(Date bitisTarih) {
		this.bitisTarih = bitisTarih;
	}

	public List<Liste> getDurumList() {
		return durumList;
	}

	public void setDurumList(List<Liste> durumList) {
		this.durumList = durumList;
	}

	public boolean isHepsiniGoster() {
		return hepsiniGoster;
	}

	public void setHepsiniGoster(boolean hepsiniGoster) {
		this.hepsiniGoster = hepsiniGoster;
	}

}
