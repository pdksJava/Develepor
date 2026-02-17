package org.pdks.session;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

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
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.CalismaModeli;
import org.pdks.entity.CalismaModeliGun;
import org.pdks.entity.CalismaModeliVardiya;
import org.pdks.entity.Departman;
import org.pdks.entity.Liste;
import org.pdks.entity.Sirket;
import org.pdks.entity.Tanim;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaSablonu;
import org.pdks.security.entity.User;

@Name("calismaModeliHome")
public class CalismaModeliHome extends EntityHome<CalismaModeli> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1146930923797257560L;

	static Logger logger = Logger.getLogger(CalismaModeliHome.class);
	@RequestParameter
	Long calismaModeliId;

	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = false, create = true)
	User authenticatedUser;
	@In(required = false)
	FacesMessages facesMessages;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	FazlaMesaiOrtakIslemler fazlaMesaiOrtakIslemler;

	public static String sayfaURL = "calismaModeliTanimlama";
	private CalismaModeli calismaModeli;

	private List<CalismaModeli> calismaModeliList;
	private List<VardiyaSablonu> sablonList;
	private List<Sirket> sirketList, pdksSirketList;
	private List<Vardiya> vardiyaList = new ArrayList<Vardiya>(), kayitliVardiyaList = new ArrayList<Vardiya>();
	private List<CalismaModeliGun> cmGunList;
	private List<Departman> departmanList;
	private List<SelectItem> haftaTatilGunleri, tesisIdList;
	private Long tesisId;

	private HashMap<Integer, List<CalismaModeliGun>> cmGunMap;
	private Sirket seciliSirket;
	private CalismaModeliGun cmgPage = new CalismaModeliGun();

	private Boolean sirketGoster = Boolean.FALSE, tesisGoster = Boolean.FALSE, yoneticiGuncelleGoster = Boolean.FALSE, suaGoster = Boolean.FALSE, pasifGoster = Boolean.FALSE, hareketKaydiVardiyaBul = Boolean.FALSE, saatlikCalismaVar = false, otomatikFazlaCalismaOnaylansinVar = false,
			izinGoster = false;

	private Session session;

	@Override
	public Object getId() {
		if (calismaModeliId == null) {
			return super.getId();
		} else {
			return calismaModeliId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	/**
	 * @param xCalismaModeli
	 * @return
	 */
	public String calismaModeliEkle(CalismaModeli xCalismaModeli) {

		if (xCalismaModeli == null) {

			xCalismaModeli = new CalismaModeli();
			if (!saatlikCalismaVar)
				xCalismaModeli.setAylikMaas(Boolean.TRUE);
			if (seciliSirket != null) {
				xCalismaModeli.setSirket(seciliSirket);
				xCalismaModeli.setDepartman(seciliSirket.getDepartman());
			}
		}

		setCalismaModeli(xCalismaModeli);

		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
		if (xCalismaModeli.getId() == null && departmanList.size() > 0)
			xCalismaModeli.setDepartman(departmanList.get(0));
		tesisId = xCalismaModeli.getTesis() != null ? xCalismaModeli.getTesis().getId() : null;
		fillVardiyalar(xCalismaModeli);

		return "";
	}

	/**
	 * @param vardiyaDoldur
	 * @param cm
	 * @return
	 */
	public String tesisDoldur(boolean vardiyaDoldur, CalismaModeli cm) {
		if (tesisIdList == null)
			tesisIdList = new ArrayList<SelectItem>();
		if (vardiyaDoldur)
			cm.setTesisId(null);
		tesisId = fazlaMesaiOrtakIslemler.tesisDoldur(cm, cm.getTesisId(), tesisIdList, session);
		cm.setTesis(tesisId != null ? ortakIslemler.getTanimById(tesisId, session) : null);
		if (vardiyaDoldur) {
			fillVardiyalar(cm);
		}

		return "";

	}

	private void gunleriSifirla() {
		if (cmGunMap == null)
			cmGunMap = new HashMap<Integer, List<CalismaModeliGun>>();
		else
			cmGunMap.clear();
		if (cmGunList == null)
			cmGunList = new ArrayList<CalismaModeliGun>();
		else
			cmGunList.clear();
	}

	public String calismaModeliKopyala(CalismaModeli xCalismaModeli) {
		CalismaModeli calismaModeliYeni = (CalismaModeli) xCalismaModeli.cloneEmpty();

		calismaModeliYeni.setId(null);
		if (calismaModeliYeni.getAciklama() != null)
			calismaModeliYeni.setAciklama(xCalismaModeli.getAciklama() + " kopya");
		if (authenticatedUser.isAdmin())
			fillBagliOlduguDepartmanTanimList();
		setCalismaModeli(calismaModeliYeni);
		fillVardiyalar(xCalismaModeli);
		return "";

	}

	/**
	 * @param gunTipi
	 * @return
	 */
	public String fillGunList(int gunTipi) {
		cmgPage.setGunTipi(gunTipi);
		List<CalismaModeliGun> list = null;
		if (cmGunMap.containsKey(gunTipi))
			list = cmGunMap.get(gunTipi);
		else {
			TreeMap<String, CalismaModeliGun> map = null;
			if (calismaModeli.getId() != null) {
				HashMap fields = new HashMap();
				fields.put("calismaModeli.id", calismaModeli.getId());
				fields.put("gunTipi", gunTipi);
				fields.put(PdksEntityController.MAP_KEY_MAP, "getKey");
				if (session != null)
					fields.put(PdksEntityController.MAP_KEY_SESSION, session);
				map = pdksEntityController.getObjectByInnerObjectMap(fields, CalismaModeliGun.class, false);
			} else
				map = new TreeMap<String, CalismaModeliGun>();
			list = new ArrayList<CalismaModeliGun>();
			Double sure = null;
			try {
				sure = gunTipi == CalismaModeliGun.GUN_SAAT ? calismaModeli.getHaftaIci() : calismaModeli.getHaftaIciSutIzniSure();
			} catch (Exception e) {
				sure = 0.0d;
				logger.error(e);
				e.printStackTrace();

			}
			for (int i = Calendar.MONDAY; i < Calendar.SATURDAY; i++) {
				String key = CalismaModeliGun.getKey(calismaModeli, gunTipi, i);
				if (!map.containsKey(key)) {
					CalismaModeliGun cmg = new CalismaModeliGun(calismaModeli, gunTipi, i);
					cmg.setSure(sure);
					map.put(key, cmg);
				}
				CalismaModeliGun cmg = map.get(key);
				cmg.setGuncellendi(false);
				list.add(cmg);
			}
			cmGunMap.put(gunTipi, list);
		}
		cmGunList = list;
		return "";
	}

	public void fillBagliOlduguDepartmanTanimList() {
		List tanimList = null;
		try {
			tanimList = ortakIslemler.fillDepartmanTanimList(session);
		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		} finally {

		}

		setDepartmanList(tanimList);
	}

	/**
	 * @param list
	 * @return
	 */
	private List<Vardiya> vardiyaAyir(List<Vardiya> list) {
		list = PdksUtil.sortObjectStringAlanList(list, "getAdi", null);
		List<Vardiya> ozelList = new ArrayList<Vardiya>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Vardiya vardiya = (Vardiya) iterator.next();
			if (vardiya.getGenel().equals(Boolean.FALSE)) {
				ozelList.add(vardiya);
				iterator.remove();
			}
		}
		if (!ozelList.isEmpty())
			list.addAll(ozelList);
		ozelList = null;
		return list;

	}

	/**
	 * @param cm
	 * @return
	 */
	public String fillVardiyalar(CalismaModeli cm) {
		Sirket sirket = cm.getSirket();
		tesisDoldur(false, cm);
		haftaTatilGunleri.clear();
		Calendar cal = Calendar.getInstance();
		haftaTatilGunleri.add(new SelectItem(null, "Sabit Gün Değil"));
		for (int i = 1; i <= 7; i++) {
			cal.set(Calendar.DAY_OF_WEEK, i);
			haftaTatilGunleri.add(new SelectItem(i, PdksUtil.convertToDateString(cal.getTime(), "EEEEE")));
		}
		gunleriSifirla();
		Long departmanId = cm.getDepartman() != null ? cm.getDepartman().getId() : null;

		sablonList = ortakIslemler.getVardiyaSablonuList(sirket, cm.getTesis(), departmanId, session);

		for (Iterator iterator = sablonList.iterator(); iterator.hasNext();) {
			VardiyaSablonu sablonu = (VardiyaSablonu) iterator.next();
			if (sablonu.getCalismaModeli() != null)
				iterator.remove();

		}
		if (cm.getBagliVardiyaSablonu() != null) {
			boolean ekle = true;
			Long id = cm.getBagliVardiyaSablonu().getId();
			for (VardiyaSablonu sablonu : sablonList) {
				if (sablonu.getId().equals(id)) {
					ekle = false;
					break;
				}
			}
			if (ekle)
				sablonList.add(cm.getBagliVardiyaSablonu());
		}
		Long cmaDepartmanId = cm.getDepartman() != null ? cm.getDepartman().getId() : null;
		HashMap parametreMap = new HashMap();
		Tanim seciliTesis = cm.getTesisId() != null ? new Tanim(cm.getTesisId()) : null;
		vardiyaList = ortakIslemler.getVardiyaList(sirket, seciliTesis, cmaDepartmanId, session);
		for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
			Vardiya vardiya = (Vardiya) iterator.next();
			if (vardiya.getKisaAdi().equals("TA") || vardiya.getKisaAdi().equals("TG"))
				logger.debug(vardiya.getId() + " " + vardiya.getKisaAdi());
			if (vardiya.isCalisma() == false)
				iterator.remove();

		}
		if (vardiyaList.size() > 1)
			vardiyaList = vardiyaAyir(vardiyaList);
		if (cm.getId() != null) {
			parametreMap.clear();
			parametreMap.put(PdksEntityController.MAP_KEY_SELECT, "vardiya");
			parametreMap.put("calismaModeli.id", cm.getId());
			if (session != null)
				parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			kayitliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				Vardiya vardiya = (Vardiya) iterator.next();
				for (Vardiya vardiyaCalisma : kayitliVardiyaList) {
					if (vardiyaCalisma.getId().equals(vardiya.getId())) {
						iterator.remove();
						break;
					}

				}
			}
			if (kayitliVardiyaList.size() > 1)
				kayitliVardiyaList = vardiyaAyir(kayitliVardiyaList);

		} else
			kayitliVardiyaList = new ArrayList<Vardiya>();
		sirketList = ortakIslemler.getDepartmanPDKSSirketList(cm.getDepartman(), session);

		return "";
	}

	public void instanceRefresh() {
		if (calismaModeli.getId() != null)
			session.refresh(calismaModeli);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		pasifGoster = false;
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		pdksSirketList = ortakIslemler.getDepartmanPDKSSirketList(null, session);
		fillCalismaModeliList();
	}

	@Transactional
	public String kaydet() {
		try {
			boolean devam = true;
			if (calismaModeli.getHaftaTatilGun() != null) {
				double saat = calismaModeli.getSaat(calismaModeli.getHaftaTatilGun());
				if (saat != 0) {
					devam = false;
					PdksUtil.addMessageWarn("Hafta tatil günü çalışma saati tanımlıdır!");
				}
			}
			if (devam) {

				if (calismaModeli.getId() != null) {
					calismaModeli.setGuncellemeTarihi(new Date());
					calismaModeli.setGuncelleyenUser(authenticatedUser);
				} else {
					calismaModeli.setOlusturmaTarihi(new Date());
					calismaModeli.setOlusturanUser(authenticatedUser);
				}
				if (calismaModeli.isHareketKaydiVardiyaBulsunmu() == false) {
					calismaModeli.setHaftaTatilHareketGuncelle(Boolean.FALSE);
					calismaModeli.setOffHareketGuncelle(Boolean.FALSE);
				}
				List<CalismaModeliVardiya> kayitliCalismaModeliVardiyaList = null;
				if (calismaModeli.getId() != null && calismaModeli.getGenelVardiya().equals(Boolean.FALSE)) {
					HashMap parametreMap = new HashMap();
					parametreMap.put("calismaModeli.id", calismaModeli.getId());
					if (session != null)
						parametreMap.put(PdksEntityController.MAP_KEY_SESSION, session);
					kayitliCalismaModeliVardiyaList = pdksEntityController.getObjectByInnerObjectList(parametreMap, CalismaModeliVardiya.class);
				} else
					kayitliCalismaModeliVardiyaList = new ArrayList<CalismaModeliVardiya>();
				String haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum");
				if (!haftaTatilDurum.equals("1"))
					calismaModeli.setHaftaTatilMesaiOde(Boolean.FALSE);
				if (calismaModeli.getHaftaTatilMesaiOde().equals(Boolean.FALSE))
					calismaModeli.setGeceHaftaTatilMesaiParcala(Boolean.FALSE);
				if (calismaModeli.getSirket() != null)
					calismaModeli.setDepartman(calismaModeli.getSirket().getDepartman());
				pdksEntityController.saveOrUpdate(session, entityManager, calismaModeli);
				if (calismaModeli.getGenelVardiya() || calismaModeli.isOrtakVardiyadir())
					kayitliVardiyaList.clear();
				for (Iterator iterator = kayitliVardiyaList.iterator(); iterator.hasNext();) {
					Vardiya kayitliVardiya = (Vardiya) iterator.next();
					boolean ekle = true;
					for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
						CalismaModeliVardiya cmv = (CalismaModeliVardiya) iterator2.next();
						if (cmv.getVardiya().getId().equals(kayitliVardiya.getId())) {
							ekle = false;
							iterator2.remove();
							break;
						}

					}
					if (ekle) {
						CalismaModeliVardiya cmv = new CalismaModeliVardiya(kayitliVardiya, calismaModeli);
						pdksEntityController.saveOrUpdate(session, entityManager, cmv);
					}
				}
				boolean sirala = false;
				for (Iterator iterator2 = kayitliCalismaModeliVardiyaList.iterator(); iterator2.hasNext();) {
					CalismaModeliVardiya cmv = (CalismaModeliVardiya) iterator2.next();
					pdksEntityController.deleteObject(session, entityManager, cmv);
					sirala = true;
				}
				if (cmGunMap != null && !cmGunMap.isEmpty()) {
					for (Integer gunTipi : cmGunMap.keySet()) {
						double sure = gunTipi.equals(CalismaModeliGun.GUN_SAAT) ? calismaModeli.getHaftaIci() : calismaModeli.getHaftaIciSutIzniSure();
						List<CalismaModeliGun> list = cmGunMap.get(gunTipi);
						for (CalismaModeliGun calismaModeliGun : list) {
							if (calismaModeliGun.getSure() == sure) {
								if (calismaModeliGun.getId() != null)
									session.delete(calismaModeliGun);
							} else if (calismaModeliGun.isGuncellendi())
								pdksEntityController.saveOrUpdate(session, entityManager, calismaModeliGun);
						}
					}
				}
				session.flush();
				if (sirala) {
					try {
						pdksEntityController.savePrepareTableID(true, CalismaModeliVardiya.class, entityManager, session);
					} catch (Exception e) {
					}
					session.flush();
				}
				fillCalismaModeliList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * @param d
	 * @return
	 */
	private boolean veriVar(Double d) {
		boolean v = d != null && d.doubleValue() > 0.0d;
		return v;
	}

	public void fillCalismaModeliList() {

		haftaTatilGunleri = ortakIslemler.getSelectItemList("haftaTatilGun", authenticatedUser);
		sirketGoster = false;
		tesisGoster = false;
		izinGoster = false;
		suaGoster = false;
		yoneticiGuncelleGoster = false;
		hareketKaydiVardiyaBul = ortakIslemler.getParameterKey("hareketKaydiVardiyaBul").equals("1");
		saatlikCalismaVar = ortakIslemler.getParameterKey("saatlikCalismaVar").equals("1");
		otomatikFazlaCalismaOnaylansinVar = ortakIslemler.getParameterKey("otomatikFazlaCalismaOnaylansin").equals("1");
		calismaModeli = new CalismaModeli();

		calismaModeliList = pdksEntityController.getSQLParamByFieldList(CalismaModeli.TABLE_NAME, pasifGoster == false ? CalismaModeli.COLUMN_NAME_DURUM : null, Boolean.TRUE, CalismaModeli.class, session);
		if (seciliSirket != null) {
			Departman seciliDepartman = seciliSirket.getDepartman();
			for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
				CalismaModeli cmd = (CalismaModeli) iterator.next();
				Departman departman = cmd.getDepartman();
				Sirket sirket = cmd.getSirket();
				boolean sil = false;
				if (departman != null && !seciliDepartman.getId().equals(departman.getId()))
					sil = true;
				if (sirket != null && !seciliSirket.getId().equals(sirket.getId()))
					sil = true;
				if (sil)
					iterator.remove();
			}

		}
		if (!hareketKaydiVardiyaBul || !otomatikFazlaCalismaOnaylansinVar) {
			List<CalismaModeli> pasifList = new ArrayList<CalismaModeli>();
			for (Iterator iterator = calismaModeliList.iterator(); iterator.hasNext();) {
				CalismaModeli cm = (CalismaModeli) iterator.next();
				if (cm.getDurum().booleanValue() == false) {
					pasifList.add(cm);
					iterator.remove();
				}

			}
			if (authenticatedUser.isAdmin() && !pasifList.isEmpty())
				calismaModeliList.addAll(pasifList);
			pasifList = null;
			for (CalismaModeli cm : calismaModeliList) {
				if (sirketGoster == false)
					sirketGoster = cm.getSirket() != null;
				if (tesisGoster == false)
					tesisGoster = cm.getTesis() != null;
				if (suaGoster == false)
					suaGoster = cm.getSuaDurum() != null && cm.getSuaDurum().booleanValue();
				if (yoneticiGuncelleGoster == false)
					yoneticiGuncelleGoster = cm.isYoneticiGuncelleyebilir();

				if (cm.getDurum()) {
					if (!izinGoster)
						izinGoster = veriVar(cm.getIzin()) || veriVar(cm.getCumartesiIzinSaat()) || veriVar(cm.getPazarIzinSaat());
					if (!otomatikFazlaCalismaOnaylansinVar)
						otomatikFazlaCalismaOnaylansinVar = cm.isOtomatikFazlaCalismaOnaylansinmi();
					if (!hareketKaydiVardiyaBul)
						hareketKaydiVardiyaBul = cm.isHareketKaydiVardiyaBulsunmu();
				}

			}
		}
	}

	public String excelAktar() {
		try {

			ByteArrayOutputStream baosDosya = excelDevam();
			if (baosDosya != null)
				PdksUtil.setExcelHttpServletResponse(baosDosya, ortakIslemler.calismaModeliAciklama() + (pasifGoster == false ? "Aktif" : "") + "Listesi.xlsx");

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}

		return "";
	}

	private ByteArrayOutputStream excelDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		try {
			String aciklama = ortakIslemler.calismaModeliAciklama();
			String vardiyaAciklama = ortakIslemler.vardiyaAciklama();
			Sheet sheet = ExcelUtil.createSheet(wb, aciklama + (pasifGoster == false ? "Aktif" : "") + " Listesi", false);
			// Drawing drawing = sheet.createDrawingPatriarch();
			// CreationHelper helper = wb.getCreationHelper();
			// ClientAnchor anchor = helper.createClientAnchor();
			CellStyle header = ExcelUtil.getStyleHeader(wb);
			CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
			CellStyle styleOddRed = ExcelUtil.getStyleOdd(null, wb);
			ExcelUtil.setFontColor(styleOddRed, Color.RED);
			CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
			CellStyle styleOddSayi = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATA_NUMBER, wb);
			CellStyle styleOddTutar = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATA_TUTAR, wb);
			CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
			CellStyle styleEvenRed = ExcelUtil.getStyleOdd(null, wb);
			ExcelUtil.setFontColor(styleEvenRed, Color.RED);
			CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
			CellStyle styleEvenSayi = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATA_NUMBER, wb);
			CellStyle styleEvenTutar = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATA_TUTAR, wb);
			int row = 0;
			int col = 0;
			boolean admin = authenticatedUser.isAdmin();
			boolean geceCalismaOde = ortakIslemler.getParameterKey("aksamBordroBasZamani").equals("") == false && ortakIslemler.getParameterKey("aksamBordroBitZamani").equals("");
			boolean haftaTatilDurum = ortakIslemler.getParameterKey("haftaTatilDurum").equals("1");
			if (admin)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(aciklama + " Id");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Adı");
			if (sirketGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
			if (tesisGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hafta İçi (Saat)");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.getGunAdi(6, null) + " (Saat)");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.getGunAdi(7, null) + " (Saat)");
			if (izinGoster) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin (Saat)");
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin " + ortakIslemler.getGunAdi(6, null) + " (Saat)");
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İzin " + ortakIslemler.getGunAdi(7, null) + " (Saat)");
			}
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Arife (Saat)");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hafta Tatil");
			if (admin)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Negatif Bakiye Max (Saat)");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Bağlı " + vardiyaAciklama + " Şablonu");
			if (saatlikCalismaVar)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Maaş Ödeme Tipi");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İdari Model");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İlk Plan Onaylı");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Fazla Mesai Var");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("ÇGS Güncellenir");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.getMenuAdi("pdksVardiyaTanimlama") + " Görüntülensin");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(vardiyaAciklama + " Kontrol Edilmez");
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Tüm " + vardiyaAciklama + "lar");
			if (geceCalismaOde)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Gece Çalışması Öde");
			if (hareketKaydiVardiyaBul)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hareket Kayıtlarından " + vardiyaAciklama + " Bul");
			if (otomatikFazlaCalismaOnaylansinVar)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Fazla Çalışma Otomatik Onaylansın");
			if (haftaTatilDurum) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hafta Sonu Fazla Mesai Öde");
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Gece " + vardiyaAciklama + " Hafta Sonu Fazla Mesai Parçala");
			}
			if (suaGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Şua");
			if (yoneticiGuncelleGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Yönetici Güncelle");
			TreeMap<Long, List<Vardiya>> vMap = new TreeMap<Long, List<Vardiya>>();
			if (admin) {
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue(vardiyaAciklama);
				List<Long> idList = new ArrayList<Long>();
				for (CalismaModeli cm : calismaModeliList) {

					idList.add(cm.getId());

				}
				if (idList.isEmpty() == false) {
					List<CalismaModeliVardiya> list = pdksEntityController.getSQLParamByFieldList(CalismaModeliVardiya.TABLE_NAME, CalismaModeliVardiya.COLUMN_NAME_CALISMA_MODELI, idList, CalismaModeliVardiya.class, session);
					if (list.isEmpty() == false) {
						List<Liste> list2 = new ArrayList<Liste>();
						for (CalismaModeliVardiya cmv : list) {
							Vardiya vardiya = cmv.getVardiya();
							if (vardiya != null && vardiya.getDurum()) {
								list2.add(new Liste(cmv.getCalismaModeli().getId() + "_" + vardiya.getAdi(), cmv));
							}

						}
						if (list2.isEmpty() == false) {
							list2 = PdksUtil.sortObjectStringAlanList(list2, "getId", null);
							for (Liste liste : list2) {
								CalismaModeliVardiya cmv = (CalismaModeliVardiya) liste.getValue();
								Long key = cmv.getCalismaModeli().getId();
								List<Vardiya> vardiyaList = vMap.containsKey(key) ? vMap.get(key) : new ArrayList<Vardiya>();
								if (vardiyaList.isEmpty())
									vMap.put(key, vardiyaList);
								vardiyaList.add(cmv.getVardiya());
							}
						}
						list2 = null;
					}
					list = null;
				}
				idList = null;
			}
			if (pasifGoster)
				ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Aktif");
			boolean renk = true;
			for (CalismaModeli calismaModeli : calismaModeliList) {
				col = 0;
				row++;
				CellStyle style = null, styleCenter = null, cellStyleTutar = null, cellStyleSayi = null;
				if (renk) {
					cellStyleTutar = styleOddTutar;
					cellStyleSayi = styleOddSayi;
					style = styleOdd;
					styleCenter = styleOddCenter;

				} else {
					cellStyleTutar = styleEvenTutar;
					cellStyleSayi = styleEvenSayi;
					style = styleEven;
					styleCenter = styleEvenCenter;

				}
				renk = !renk;
				if (admin)
					ExcelUtil.getCell(sheet, row, col++, cellStyleSayi).setCellValue(calismaModeli.getId());
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(calismaModeli.getAciklama());
				if (sirketGoster)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(calismaModeli.getSirket() == null ? "" : calismaModeli.getSirket().getAd());
				if (tesisGoster)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(calismaModeli.getTesis() == null ? "" : calismaModeli.getTesis().getAciklama());
				if (calismaModeli.getHaftaIci() > 0.0d)
					ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(calismaModeli.getHaftaIci());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (calismaModeli.getCumartesiSaat() > 0.0d)
					ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(calismaModeli.getCumartesiSaat());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (calismaModeli.getPazarSaat() > 0.0d)
					ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(calismaModeli.getPazarSaat());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				if (izinGoster) {
					if (calismaModeli.getIzin() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(calismaModeli.getIzin());
					else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					if (calismaModeli.getCumartesiIzinSaat() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(calismaModeli.getCumartesiIzinSaat());
					else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
					if (calismaModeli.getPazarIzinSaat() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(calismaModeli.getPazarIzinSaat());
					else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
				}
				if (calismaModeli.getArife() > 0.0d)
					ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(calismaModeli.getArife());
				else
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(calismaModeli.getHaftaTatil() != null ? calismaModeli.getHaftaTatil() : "");
				if (admin) {
					if (calismaModeli.getNegatifBakiyeDenkSaat() > 0.0d)
						ExcelUtil.getCell(sheet, row, col++, cellStyleTutar).setCellValue(calismaModeli.getNegatifBakiyeDenkSaat());
					else
						ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");

				}

				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(calismaModeli.getBagliVardiyaSablonu() != null ? calismaModeli.getBagliVardiyaSablonu().getAdi() : "");
				if (saatlikCalismaVar)
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(calismaModeli.getMaasOdemeTipiAciklama());
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getIdariModel()));
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getIlkPlanOnayliDurum()));
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getFazlaMesaiVar()));
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.isUpdateCGS()));
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getGenelModel()));
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getOrtakVardiya()));
				ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getGenelVardiya() && calismaModeli.getOrtakVardiya().booleanValue() == false));
				if (geceCalismaOde)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getGeceCalismaOdemeVar()));
				if (hareketKaydiVardiyaBul)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getHareketKaydiVardiyaBul()));
				if (otomatikFazlaCalismaOnaylansinVar)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getOtomatikFazlaCalismaOnaylansin()));
				if (haftaTatilDurum)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getHaftaTatilMesaiOde()));

				if (suaGoster)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getSuaDurum()));
				if (yoneticiGuncelleGoster)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.isYoneticiGuncelleyebilir()));
				if (admin) {
					StringBuilder cmAciklama = new StringBuilder();
					if (vMap.containsKey(calismaModeli.getId())) {
						List<Vardiya> list = vMap.get(calismaModeli.getId());
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							Vardiya vardiya = (Vardiya) iterator.next();
							cmAciklama.append(vardiya.getKisaAdi());
							if (iterator.hasNext())
								cmAciklama.append(", ");
						}
						list = null;
					}
					ExcelUtil.getCell(sheet, row, col++, style).setCellValue(cmAciklama.toString());
					cmAciklama = null;
				}
				if (pasifGoster)
					ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(authenticatedUser.getYesNo(calismaModeli.getDurum()));

			}
			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return baos;
	}

	public List<Vardiya> getVardiyaList() {
		return vardiyaList;
	}

	public void setVardiyaList(List<Vardiya> vardiyaList) {
		this.vardiyaList = vardiyaList;
	}

	public List<CalismaModeli> getCalismaModeliList() {
		return calismaModeliList;
	}

	public void setCalismaModeliList(List<CalismaModeli> calismaModeliList) {
		this.calismaModeliList = calismaModeliList;
	}

	public List<Vardiya> getKayitliVardiyaList() {
		return kayitliVardiyaList;
	}

	public void setKayitliVardiyaList(List<Vardiya> kayitliVardiyaList) {
		this.kayitliVardiyaList = kayitliVardiyaList;
	}

	public CalismaModeli getCalismaModeli() {
		return calismaModeli;
	}

	public void setCalismaModeli(CalismaModeli calismaModeli) {
		this.calismaModeli = calismaModeli;
	}

	public List<VardiyaSablonu> getSablonList() {
		return sablonList;
	}

	public void setSablonList(List<VardiyaSablonu> sablonList) {
		this.sablonList = sablonList;
	}

	public List<Departman> getDepartmanList() {
		return departmanList;
	}

	public void setDepartmanList(List<Departman> departmanList) {
		this.departmanList = departmanList;
	}

	public Boolean getHareketKaydiVardiyaBul() {
		return hareketKaydiVardiyaBul;
	}

	public void setHareketKaydiVardiyaBul(Boolean hareketKaydiVardiyaBul) {
		this.hareketKaydiVardiyaBul = hareketKaydiVardiyaBul;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * @return the saatlikCalismaVar
	 */
	public Boolean getSaatlikCalismaVar() {
		return saatlikCalismaVar;
	}

	/**
	 * @param saatlikCalismaVar
	 *            the saatlikCalismaVar to set
	 */
	public void setSaatlikCalismaVar(Boolean saatlikCalismaVar) {
		this.saatlikCalismaVar = saatlikCalismaVar;
	}

	/**
	 * @return the otomatikFazlaCalismaOnaylansinVar
	 */
	public Boolean getOtomatikFazlaCalismaOnaylansinVar() {
		return otomatikFazlaCalismaOnaylansinVar;
	}

	/**
	 * @param otomatikFazlaCalismaOnaylansinVar
	 *            the otomatikFazlaCalismaOnaylansinVar to set
	 */
	public void setOtomatikFazlaCalismaOnaylansinVar(Boolean otomatikFazlaCalismaOnaylansinVar) {
		this.otomatikFazlaCalismaOnaylansinVar = otomatikFazlaCalismaOnaylansinVar;
	}

	public Boolean getIzinGoster() {
		return izinGoster;
	}

	public void setIzinGoster(Boolean izinGoster) {
		this.izinGoster = izinGoster;
	}

	public List<CalismaModeliGun> getCmGunList() {
		return cmGunList;
	}

	public void setCmGunList(List<CalismaModeliGun> cmGunList) {
		this.cmGunList = cmGunList;
	}

	public HashMap<Integer, List<CalismaModeliGun>> getCmGunMap() {
		return cmGunMap;
	}

	public void setCmGunMap(HashMap<Integer, List<CalismaModeliGun>> cmGunMap) {
		this.cmGunMap = cmGunMap;
	}

	public CalismaModeliGun getCmgPage() {
		return cmgPage;
	}

	public void setCmgPage(CalismaModeliGun cmgPage) {
		this.cmgPage = cmgPage;
	}

	public static String getSayfaURL() {
		return sayfaURL;
	}

	public static void setSayfaURL(String sayfaURL) {
		CalismaModeliHome.sayfaURL = sayfaURL;
	}

	public List<SelectItem> getHaftaTatilGunleri() {
		return haftaTatilGunleri;
	}

	public void setHaftaTatilGunleri(List<SelectItem> haftaTatilGunleri) {
		this.haftaTatilGunleri = haftaTatilGunleri;
	}

	public Boolean getPasifGoster() {
		return pasifGoster;
	}

	public void setPasifGoster(Boolean pasifGoster) {
		this.pasifGoster = pasifGoster;
	}

	public List<Sirket> getSirketList() {
		return sirketList;
	}

	public void setSirketList(List<Sirket> sirketList) {
		this.sirketList = sirketList;
	}

	public List<Sirket> getPdksSirketList() {
		return pdksSirketList;
	}

	public void setPdksSirketList(List<Sirket> pdksSirketList) {
		this.pdksSirketList = pdksSirketList;
	}

	public Sirket getSeciliSirket() {
		return seciliSirket;
	}

	public void setSeciliSirket(Sirket seciliSirket) {
		this.seciliSirket = seciliSirket;
	}

	public Boolean getSirketGoster() {
		return sirketGoster;
	}

	public void setSirketGoster(Boolean sirketGoster) {
		this.sirketGoster = sirketGoster;
	}

	public Boolean getSuaGoster() {
		return suaGoster;
	}

	public void setSuaGoster(Boolean suaGoster) {
		this.suaGoster = suaGoster;
	}

	public List<SelectItem> getTesisIdList() {
		return tesisIdList;
	}

	public void setTesisIdList(List<SelectItem> tesisIdList) {
		this.tesisIdList = tesisIdList;
	}

	public Long getTesisId() {
		return tesisId;
	}

	public void setTesisId(Long tesisId) {
		this.tesisId = tesisId;
	}

	public Boolean getTesisGoster() {
		return tesisGoster;
	}

	public void setTesisGoster(Boolean tesisGoster) {
		this.tesisGoster = tesisGoster;
	}

	public Boolean getYoneticiGuncelleGoster() {
		return yoneticiGuncelleGoster;
	}

	public void setYoneticiGuncelleGoster(Boolean yoneticiGuncelleGoster) {
		this.yoneticiGuncelleGoster = yoneticiGuncelleGoster;
	}

}
