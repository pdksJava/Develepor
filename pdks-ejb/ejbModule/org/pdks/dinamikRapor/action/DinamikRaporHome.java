package org.pdks.dinamikRapor.action;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;
import org.pdks.dinamikRapor.entity.PdksDinamikRapor;
import org.pdks.dinamikRapor.entity.PdksDinamikRaporAlan;
import org.pdks.dinamikRapor.entity.PdksDinamikRaporParametre;
import org.pdks.dinamikRapor.enums.ENumBaslik;
import org.pdks.entity.AramaSecenekleri;
import org.pdks.entity.Sirket;
import org.pdks.security.entity.MenuItemConstant;
import org.pdks.security.entity.User;
import org.pdks.session.ExcelUtil;
import org.pdks.session.OrtakIslemler;
import org.pdks.session.PdksEntityController;
import org.pdks.session.PdksUtil;

@Name("dinamikRaporHome")
public class DinamikRaporHome extends EntityHome<PdksDinamikRapor> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7573955904085170923L;
	static Logger logger = Logger.getLogger(DinamikRaporHome.class);
	@RequestParameter
	Long pdksDepartmanId;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	PdksEntityController pdksEntityController;
	@In(required = true, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	User authenticatedUser;

	public static String sayfaURL = "dinamikRapor";
	private PdksDinamikRapor seciliPdksDinamikRapor;
	private List<PdksDinamikRapor> dinamikRaporList;
	private List<PdksDinamikRaporAlan> dinamikRaporAlanList;
	private List<PdksDinamikRaporParametre> dinamikRaporParametreList;
	private List<SelectItem> tesisIdList;
	private List<Long> tesisId;
	private List<Object[]> dinamikRaporDataList;
	private PdksDinamikRaporParametre sirketParametre, tesisParametre, basTarihParametre, bitTarihParametre;

	private Session session;

	@Override
	public Object getId() {
		if (pdksDepartmanId == null) {
			return super.getId();
		} else {
			return pdksDepartmanId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public String sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(session, sayfaURL);
		String sayfa = "";
		fillPdksDinamikRaporList();
		if (dinamikRaporDataList == null)
			dinamikRaporDataList = new ArrayList<Object[]>();
		else
			dinamikRaporDataList.clear();
		seciliPdksDinamikRapor = null;
		if (dinamikRaporList.isEmpty()) {
			PdksUtil.addMessageAvailableWarn("Rapor alınacak tanımlanmış veri yoktur!");
			sayfa = MenuItemConstant.home;
		} else if (dinamikRaporList.size() == 1) {
			PdksDinamikRapor dinamikRapor = dinamikRaporList.get(0);
			dinamikRaporGuncelle(dinamikRapor);
		}
		return sayfa;
	}

	/**
	 * @param dinamikRapor
	 * @param tip
	 * @return
	 */
	public String dinamikRaporGuncelle(PdksDinamikRapor dinamikRapor) {
		seciliPdksDinamikRapor = dinamikRapor;
		fillDinamikRaporAlanList();
		filllDinamikRaporParametreList();
		dinamikRaporDataList.clear();
		return "";

	}

	public String fillDinamikRaporList() {
		StringBuffer sb = new StringBuffer();
		if (seciliPdksDinamikRapor.isStoreProcedure()) {
			sb.append(seciliPdksDinamikRapor.getDbTanim());
			LinkedHashMap<String, Object> veriMap = new LinkedHashMap<String, Object>();
			for (Iterator iterator = dinamikRaporParametreList.iterator(); iterator.hasNext();) {
				PdksDinamikRaporParametre rp = (PdksDinamikRaporParametre) iterator.next();
				String adi = "p" + rp.getSira();
				if (rp.getSecimList() != null)
					veriMap.put(adi, rp.getValue());
				else if (rp.isKarakter())
					veriMap.put(adi, rp.getKarakterDeger());
				else if (rp.isSayisal())
					veriMap.put(adi, rp.getSayisalDeger());
				else if (rp.isTarih())
					veriMap.put(adi, rp.getTarihDeger());
			}
			if (session != null)
				veriMap.put(PdksEntityController.MAP_KEY_SESSION, session);
			try {
				dinamikRaporDataList = pdksEntityController.execSPList(veriMap, sb, null);
			} catch (Exception e) {
				dinamikRaporDataList = new ArrayList<Object[]>();
			}
		} else {
			HashMap fields = new HashMap();
			sb.append("select ");
			for (Iterator iterator = dinamikRaporAlanList.iterator(); iterator.hasNext();) {
				PdksDinamikRaporAlan ra = (PdksDinamikRaporAlan) iterator.next();
				sb.append(ra.getDbTanim() + (iterator.hasNext() ? ", " : ""));
			}
			String str = " where ";
			if (seciliPdksDinamikRapor.isFunction()) {
				sb.append(" from " + seciliPdksDinamikRapor.getDbTanim() + "(");
				String strFunction = "";
				for (Iterator iterator = dinamikRaporParametreList.iterator(); iterator.hasNext();) {
					PdksDinamikRaporParametre rp = (PdksDinamikRaporParametre) iterator.next();
					if (rp.getParametreDurum()) {
						String adi = "p" + rp.getSira();
						if (strFunction.length() > 0)
							strFunction += ", ";
						strFunction += " :" + adi;
						if (rp.getSecimList() != null)
							fields.put(adi, rp.getValue());
						else if (rp.isKarakter())
							fields.put(adi, rp.getKarakterDeger());
						else if (rp.isSayisal())
							fields.put(adi, rp.getSayisalDeger());
						else if (rp.isTarih())
							fields.put(adi, rp.getTarihDeger());
					}

				}
				sb.append(strFunction + " )");
				for (Iterator iterator = dinamikRaporParametreList.iterator(); iterator.hasNext();) {
					PdksDinamikRaporParametre rp = (PdksDinamikRaporParametre) iterator.next();
					if (rp.getParametreDurum().booleanValue() == false) {
						String adi = "p" + rp.getSira();
						Object veri = null;
						if (rp.getSecimList() != null)
							veri = rp.getValue();
						else if (rp.isKarakter()) {
							veri = rp.getKarakterDeger();
							if (PdksUtil.hasStringValue(rp.getKarakterDeger()) == false && rp.getZorunlu().booleanValue() == false)
								veri = null;
						} else if (rp.isSayisal())
							veri = rp.getSayisalDeger();
						else if (rp.isTarih())
							veri = rp.getTarihDeger();
						if (veri != null || rp.getZorunlu()) {
							String esitlik = PdksUtil.hasStringValue(rp.getEsitlik()) ? rp.getEsitlik().trim() : "=";
							if (esitlik.equalsIgnoreCase("like"))
								veri = "%" + veri + "%";
							sb.append(str + rp.getDbTanim() + " " + esitlik + " :" + adi);
							fields.put(adi, veri);
							str = " and ";
						}
					}
				}

			} else if (seciliPdksDinamikRapor.isView()) {
				sb.append(" from " + seciliPdksDinamikRapor.getDbTanim() + " " + PdksEntityController.getSelectLOCK());
				for (Iterator iterator = dinamikRaporParametreList.iterator(); iterator.hasNext();) {
					PdksDinamikRaporParametre rp = (PdksDinamikRaporParametre) iterator.next();
					String adi = "p" + rp.getSira();
					Object veri = null;
					if (rp.getSecimList() != null)
						veri = rp.getValue();
					else if (rp.isKarakter()) {
						veri = rp.getKarakterDeger();
						if (PdksUtil.hasStringValue(rp.getKarakterDeger()) == false && rp.getZorunlu().booleanValue() == false)
							veri = null;
					} else if (rp.isSayisal())
						veri = rp.getSayisalDeger();
					else if (rp.isTarih())
						veri = rp.getTarihDeger();
					if (veri != null || rp.getZorunlu()) {
						String esitlik = PdksUtil.hasStringValue(rp.getEsitlik()) ? rp.getEsitlik().trim() : "=";
						if (esitlik.equalsIgnoreCase("like"))
							veri = "%" + veri + "%";
						sb.append(str + rp.getDbTanim() + " " + esitlik + " :" + adi);
						fields.put(adi, veri);
						str = " and ";
					}

				}

			}
			if (PdksUtil.hasStringValue(seciliPdksDinamikRapor.getWhereSQL()))
				sb.append(str + " " + seciliPdksDinamikRapor.getWhereSQL());
			if (PdksUtil.hasStringValue(seciliPdksDinamikRapor.getOrderSQL()))
				sb.append(" order by " + seciliPdksDinamikRapor.getOrderSQL());
			if (session != null)
				fields.put(PdksEntityController.MAP_KEY_SESSION, session);
			dinamikRaporDataList = pdksEntityController.getObjectBySQLList(sb, fields, null);
		}
		return "";
	}

	public String excelDinamikRaporList() {
		try {
			ByteArrayOutputStream baosDosya = excelDinamikRaporListDevam();
			if (baosDosya != null) {
				String dosyaAdi = seciliPdksDinamikRapor.getAciklama() + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
		}
		return "";
	}

	private ByteArrayOutputStream excelDinamikRaporListDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Rapor", Boolean.TRUE);
		XSSFCellStyle header = (XSSFCellStyle) ExcelUtil.getStyleHeader(9, wb);
		CellStyle styleOdd = ExcelUtil.getStyleOdd(null, wb);
		CellStyle styleOddCenter = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleOddRight = ExcelUtil.getStyleOdd(ExcelUtil.ALIGN_RIGHT, wb);
		CellStyle styleOddNumber = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleOddDate = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleOddDateTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleOddTime = ExcelUtil.getStyleOdd(ExcelUtil.FORMAT_TIME, wb);
		CellStyle styleEven = ExcelUtil.getStyleEven(null, wb);
		CellStyle styleEvenCenter = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_CENTER, wb);
		CellStyle styleEvenRight = ExcelUtil.getStyleEven(ExcelUtil.ALIGN_RIGHT, wb);
		CellStyle styleEvenNumber = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_NUMBER, wb);
		CellStyle styleEvenDate = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATE, wb);
		CellStyle styleEvenDateTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_DATETIME, wb);
		CellStyle styleEvenTime = ExcelUtil.getStyleEven(ExcelUtil.FORMAT_TIME, wb);

		int col = 0, row = 0;
		for (PdksDinamikRaporAlan ra : dinamikRaporAlanList) {
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.getDinamikRaporAlanAciklama(ra.getAciklama()));
		}
		boolean renk = true;
		for (Object[] veri : dinamikRaporDataList) {
			col = 0;
			++row;
			CellStyle style = null;
			CellStyle styleCenter = null;
			CellStyle styleRight = null;
			CellStyle styleGenel = null;
			CellStyle styleNumber = null;
			CellStyle styleDate = null;
			CellStyle styleDateTime = null;
			CellStyle styleTime = null;

			if (renk) {
				styleGenel = styleOdd;
				styleCenter = styleOddCenter;
				styleRight = styleOddRight;
				styleNumber = styleOddNumber;
				styleDate = styleOddDate;
				styleDateTime = styleOddDateTime;
				styleTime = styleOddTime;
			} else {
				styleGenel = styleEven;
				styleCenter = styleEvenCenter;
				styleRight = styleEvenRight;
				styleNumber = styleEvenNumber;
				styleDate = styleEvenDate;
				styleDateTime = styleEvenDateTime;
				styleTime = styleEvenTime;
			}

			for (PdksDinamikRaporAlan ra : dinamikRaporAlanList) {
				Object data = getDinamikRaporAlan(veri, ra.getSira());
				if (data != null) {
					if (ra.isKarakter()) {
						String str = (String) data;
						style = styleGenel;
						if (ra.isHizalaOrtala())
							style = styleCenter;
						else if (ra.isHizalaSaga())
							style = styleRight;
						ExcelUtil.getCell(sheet, row, col, style).setCellValue(str);
					} else if (ra.isSayisal() == false) {
						Date tarih = (Date) data;
						style = styleDate;
						if (ra.isTarihSaat())
							style = styleDateTime;
						else if (ra.isSaat())
							style = styleTime;
						ExcelUtil.getCell(sheet, row, col, style).setCellValue(tarih);
					} else {
						Double db = null;
						style = styleNumber;
						if (data instanceof BigDecimal)
							db = ((BigDecimal) data).doubleValue();
						else if (data instanceof BigInteger)
							db = ((BigInteger) data).doubleValue();
						else if (data instanceof Double)
							db = ((Double) data).doubleValue();
						else if (data instanceof Integer)
							db = ((Integer) data).doubleValue();
						else
							db = new Double(data.toString());
						ExcelUtil.getCell(sheet, row, col, style).setCellValue(db);
					}
				} else {
					ExcelUtil.getCell(sheet, row, col, styleGenel).setCellValue("");
				}
				++col;

			}
			renk = !renk;
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
	 * @param veri
	 * @param index
	 * @return
	 */
	public Object getDinamikRaporAlan(Object[] veri, Integer index) {
		Object object = null;
		if (veri != null && index != null && veri.length >= index)
			object = veri[index];
		return object;
	}

	private void fillPdksDinamikRaporList() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String paramStr = (String) req.getParameter("id");
		Long id = null;
		if (paramStr != null) {
			String str = PdksUtil.getDecodeStringByBase64(paramStr);
			HashMap<String, String> map = new HashMap();
			List<String> list = PdksUtil.getListStringTokenizer(str, "&");
			for (String string : list) {
				if (string.indexOf("=") > 0) {
					String[] strings = string.split("=");
					if (strings.length == 2)
						map.put(strings[0], strings[1]);
				}

			}
			if (map.containsKey("id"))
				try {
					id = Long.parseLong(map.get("id"));
				} catch (Exception e) {
				}
			map = null;
			list = null;
		}

		session.clear();
		HashMap fields = new HashMap();
		if (session != null)
			fields.put(PdksEntityController.MAP_KEY_SESSION, session);
		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + PdksDinamikRapor.TABLE_NAME + " " + PdksEntityController.getSelectLOCK());
		dinamikRaporList = pdksEntityController.getSQLParamByFieldList(PdksDinamikRapor.TABLE_NAME, PdksDinamikRapor.COLUMN_NAME_DURUM, Boolean.TRUE, PdksDinamikRapor.class, session);

		for (Iterator iterator = dinamikRaporList.iterator(); iterator.hasNext();) {
			PdksDinamikRapor pr = (PdksDinamikRapor) iterator.next();
			if (authenticatedUser.isAdmin() == false && pr.getGoruntulemeDurum().booleanValue() == false)
				iterator.remove();
			else if (id != null && pr.getId().equals(id) == false)
				iterator.remove();
		}

	}

	private void fillDinamikRaporAlanList() {
		dinamikRaporAlanList = pdksEntityController.getSQLParamByAktifFieldList(PdksDinamikRaporAlan.TABLE_NAME, PdksDinamikRaporAlan.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporAlan.class, session);
		if (dinamikRaporAlanList.size() > 1)
			dinamikRaporAlanList = PdksUtil.sortListByAlanAdi(dinamikRaporAlanList, "sira", Boolean.FALSE);
	}

	/**
	 * @param parametre
	 * @return
	 */
	public String tesisDoldur(PdksDinamikRaporParametre parametre) {
		dinamikRaporDataList.clear();
		if ((sirketParametre != null || tesisParametre != null) && parametre != null) {
			Date basTarih = basTarihParametre != null ? basTarihParametre.getTarihDeger() : PdksUtil.convertToJavaDate(PdksUtil.getSistemBaslangicYili() + "0101", "yyyyMMdd");
			Date bitTarih = bitTarihParametre != null ? bitTarihParametre.getTarihDeger() : new Date();
			if (parametre.isSirketBilgisi()) {
				Sirket sirket = null;
				Long sirketId = parametre.getValue() != null ? Long.parseLong((String) parametre.getValue()) : null;
				if (tesisParametre != null) {

					if (tesisIdList == null)
						tesisIdList = new ArrayList<SelectItem>();
					else
						tesisIdList.clear();
					Long oncekiId = tesisParametre.getValue() != null ? Long.parseLong(tesisParametre.getValue().toString()) : null;
					tesisParametre.setValue(null);
					if (sirketId != null) {
						sirket = (Sirket) pdksEntityController.getSQLParamByFieldObject(Sirket.TABLE_NAME, Sirket.COLUMN_NAME_ID, sirketId, Sirket.class, session);
						if (sirket.isTesisDurumu()) {
							AramaSecenekleri aramaSecenekleri = new AramaSecenekleri();
							aramaSecenekleri.setSirketId(sirketId);
							List<SelectItem> list = ortakIslemler.setAramaSecenekTesisData(aramaSecenekleri, basTarih, bitTarih, false, session);
							if (list.size() == 1)
								tesisParametre.setValue("" + list.get(0).getValue());
							else if (oncekiId != null) {
								for (SelectItem selectItem : list) {
									if (oncekiId.equals(selectItem.getValue()))
										tesisParametre.setValue("" + selectItem.getValue());
								}
							}
							tesisIdList.addAll(list);
						}
					}
				}
			} else if (parametre.isTesisBilgisi() == false && sirketParametre != null) {
				AramaSecenekleri aramaSecenekleri = new AramaSecenekleri();
				List<SelectItem> secimList = ortakIslemler.setAramaSecenekSirketVeTesisData(aramaSecenekleri, basTarih, bitTarih, false, session);
				Long oncekiId = sirketParametre.getValue() != null ? Long.parseLong(sirketParametre.getValue().toString()) : null;
				sirketParametre.setSecimList(secimList);
				if (secimList.size() == 1)
					sirketParametre.setValue("" + secimList.get(0).getValue());
				else if (oncekiId != null) {
					for (SelectItem selectItem : secimList) {
						if (oncekiId.equals(selectItem.getValue()))
							sirketParametre.setValue("" + selectItem.getValue());
					}
				}
				if (tesisParametre != null) {

					tesisDoldur(sirketParametre);
				}

			}

		}

		return "";
	}

	private void filllDinamikRaporParametreList() {
		dinamikRaporParametreList = pdksEntityController.getSQLParamByAktifFieldList(PdksDinamikRaporParametre.TABLE_NAME, PdksDinamikRaporParametre.COLUMN_NAME_DINAMIK_RAPOR, seciliPdksDinamikRapor.getId(), PdksDinamikRaporParametre.class, session);
		if (dinamikRaporParametreList.size() > 1)
			dinamikRaporParametreList = PdksUtil.sortListByAlanAdi(dinamikRaporParametreList, "sira", Boolean.FALSE);
		Date tarihDeger = null, basTarih = PdksUtil.convertToJavaDate(PdksUtil.getSistemBaslangicYili() + "0101", "yyyyMMdd"), bitTarih = new Date();
		Long sirketId = null;
		int adet = 0;
		tesisParametre = null;
		for (Iterator iterator = dinamikRaporParametreList.iterator(); iterator.hasNext();) {
			PdksDinamikRaporParametre pr = (PdksDinamikRaporParametre) iterator.next();
			if (pr.isTarih()) {
				ENumBaslik baslik = ENumBaslik.fromValue(pr.getAciklama());
				if (baslik != null) {
					if (baslik.value().equals(ENumBaslik.BAS_TARIH.value()))
						basTarihParametre = pr;
					else if (baslik.value().equals(ENumBaslik.BIT_TARIH.value()))
						bitTarihParametre = pr;
				}

				if (tarihDeger == null)
					tarihDeger = ortakIslemler.getBugun();
				pr.setTarihDeger(tarihDeger);
			} else {
				ENumBaslik baslik = ENumBaslik.fromValue(pr.getAciklama());
				if (baslik != null) {
					List<SelectItem> list = null;
					if (baslik.equals(ENumBaslik.SIRKET)) {
						sirketParametre = pr;
						++adet;
						AramaSecenekleri aramaSecenekleri = new AramaSecenekleri();
						list = ortakIslemler.setAramaSecenekSirketVeTesisData(aramaSecenekleri, basTarih, bitTarih, false, session);
					} else if (baslik.equals(ENumBaslik.TESIS)) {
						++adet;
						tesisParametre = pr;
						AramaSecenekleri aramaSecenekleri = new AramaSecenekleri();
						aramaSecenekleri.setSirketId(sirketId);
						list = ortakIslemler.setAramaSecenekTesisData(aramaSecenekleri, basTarih, bitTarih, false, session);
						tesisIdList = list;
					}
					if (list != null) {
						if (!list.isEmpty()) {
							if (list.size() == 1) {
								pr.setValue(list.get(0).getValue());
								if (baslik.equals(ENumBaslik.SIRKET))
									sirketId = (Long) pr.getValue();
							}
							pr.setSecimList(list);
						} else
							iterator.remove();
					}

				}
			}
		}

		if (adet == 2) {
			if (tesisIdList != null && tesisIdList.size() > 1) {
				if (sirketId == null)
					tesisIdList.clear();
			}
		} else
			tesisParametre = null;

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
		DinamikRaporHome.sayfaURL = sayfaURL;
	}

	public List<PdksDinamikRapor> getDinamikRaporList() {
		return dinamikRaporList;
	}

	public void setDinamikRaporList(List<PdksDinamikRapor> dinamikRaporList) {
		this.dinamikRaporList = dinamikRaporList;
	}

	public List<PdksDinamikRaporAlan> getDinamikRaporAlanList() {
		return dinamikRaporAlanList;
	}

	public void setDinamikRaporAlanList(List<PdksDinamikRaporAlan> dinamikRaporAlanList) {
		this.dinamikRaporAlanList = dinamikRaporAlanList;
	}

	public List<PdksDinamikRaporParametre> getDinamikRaporParametreList() {
		return dinamikRaporParametreList;
	}

	public void setDinamikRaporParametreList(List<PdksDinamikRaporParametre> dinamikRaporParametreList) {
		this.dinamikRaporParametreList = dinamikRaporParametreList;
	}

	public PdksDinamikRapor getSeciliPdksDinamikRapor() {
		return seciliPdksDinamikRapor;
	}

	public void setSeciliPdksDinamikRapor(PdksDinamikRapor seciliPdksDinamikRapor) {
		this.seciliPdksDinamikRapor = seciliPdksDinamikRapor;
	}

	public List<Object[]> getDinamikRaporDataList() {
		return dinamikRaporDataList;
	}

	public void setDinamikRaporDataList(List<Object[]> dinamikRaporDataList) {
		this.dinamikRaporDataList = dinamikRaporDataList;
	}

	public List<SelectItem> getTesisIdList() {
		return tesisIdList;
	}

	public void setTesisIdList(List<SelectItem> tesisIdList) {
		this.tesisIdList = tesisIdList;
	}

	public List<Long> getTesisId() {
		return tesisId;
	}

	public void setTesisId(List<Long> tesisId) {
		this.tesisId = tesisId;
	}

}
