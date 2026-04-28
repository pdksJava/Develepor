package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.kgs.entity.ENumHareketYon;
import org.kgs.entity.MySQLTerminal;
import org.pdks.pdf.action.HeaderIText;
import org.pdks.pdf.action.PDFITextUtils;
import org.pdks.security.entity.User;

import com.google.gson.Gson;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Name("cihazHome")
public class CihazHome extends EntityHome<MySQLTerminal> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2910848709893234483L;
	static Logger logger = Logger.getLogger(CihazHome.class);
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

	public static String sayfaURL = "cihazTanimlama";
	private MySQLTerminal terminal;

	private List<MySQLTerminal> terminalList;
	private List<SelectItem> yonTipiList;
	private String tipAciklama, adi, kodu;
	private Integer tipi;

	private Session session;

	private byte[] data = null;

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

	public String cihazQRGoster() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		String tipi = null;
		if (terminal.getHareketYon() != null) {
			if (terminal.getHareketYon().equals(ENumHareketYon.CIFT_YON) == false) {
				tipi = terminal.getTipAciklama();
			}
		}
		map.put(MySQLTerminal.JSON_KODU, terminal.getKodu());
		map.put(MySQLTerminal.JSON_ADI, terminal.getAciklama());
		if (tipi != null) {
			map.put("tipAciklama", tipi);
			map.put(MySQLTerminal.JSON_TIPI, terminal.getHareketYon().value());
		}

		Gson gson = new Gson();
		String id = null;
		try {
			id = PdksUtil.encoderURL(PdksUtil.getEncodeStringByBase64(gson.toJson(map)), null);
		} catch (Exception e) {
		}
		String str = "cihazQRGoster?Id=" + id;
		return str;
	}

	/**
	 * @param xCalismaModeli
	 * @return
	 */
	public String terminalEkle(MySQLTerminal xTerminal) {
		if (xTerminal == null) {
			xTerminal = new MySQLTerminal();
		}
		terminal = xTerminal;
		return "";
	}

	public void instanceRefresh() {
		if (terminal.getId() != null)
			session.refresh(terminal);
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		ortakIslemler.setUserMenuItemTime(entityManager, session, sayfaURL);
		fillTerminalList();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaQRGirisAction() {
		if (PdksUtil.isSessionKapali(session))
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String cihazIdStr = (String) req.getParameter("Id");
		data = null;
		tipAciklama = null;
		adi = null;
		if (PdksUtil.hasStringValue(cihazIdStr))
			try {
				String text = PdksUtil.getDecodeStringByBase64(cihazIdStr);
				Gson gson = new Gson();
				LinkedHashMap<String, Object> map = gson.fromJson(text, LinkedHashMap.class);
				terminal = new MySQLTerminal();
				kodu = "";
				if (map.containsKey(MySQLTerminal.JSON_KODU))
					kodu = (String) map.get(MySQLTerminal.JSON_KODU);
				if (map.containsKey(MySQLTerminal.JSON_ADI))
					adi = (String) map.get(MySQLTerminal.JSON_ADI);
				if (map.containsKey("tipAciklama")) {
					tipAciklama = ("Personel " + (String) map.get("tipAciklama")).toUpperCase(Constants.TR_LOCALE);
					Double tipiD = (Double) map.get(MySQLTerminal.JSON_TIPI);
					int tipi = tipiD.intValue();
					map.put(MySQLTerminal.JSON_TIPI, tipi);
					int index = kodu.indexOf("_" + tipi);
					if (index > 0) {
						kodu = kodu.substring(0, index);
						map.put(MySQLTerminal.JSON_KODU, kodu);
					}

					if (adi != null) {
						String name = PdksUtil.setTurkishStr(adi).toLowerCase(Locale.ENGLISH);
						String tipEn = PdksUtil.setTurkishStr((String) map.get("tipAciklama")).toLowerCase(Locale.ENGLISH);
						index = name.indexOf(tipEn);
						if (index > 0) {
							name = adi.substring(0, index - 1);
							name += " " + adi.substring(index + tipEn.length());
							adi = PdksUtil.replaceAll(name, "  ", " ").trim();
						}
					}
					map.remove("tipAciklama");

				}
				if (adi != null)
					map.put(MySQLTerminal.JSON_ADI, adi);
				text = gson.toJson(map);
				data = ortakIslemler.generateQR(text, null, null);
			} catch (Exception e) {

			}
	}

	@Transactional
	public String kaydet() {
		try {
			boolean devam = true;

			if (devam) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public String qrPDFOlustur() {

		try {
			ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
			Document document = new Document(PageSize.A4, -60, -60, 30, 30);
			PdfWriter writer = PdfWriter.getInstance(document, baosPDF);

			HeaderIText event = new HeaderIText();
			writer.setPageEvent(event);
			document.open();
 			BaseFont baseFont = BaseFont.createFont("ARIAL.TTF", BaseFont.IDENTITY_H, true);
			Font fontBaslik = new Font(baseFont, 24f, Font.BOLD, BaseColor.BLACK);
			Paragraph bos = PDFITextUtils.getParagraph("", fontBaslik, Element.ALIGN_CENTER);
			Font fontFooter = new Font(baseFont, 24f, Font.NORMAL, BaseColor.RED);
			Image image = ortakIslemler.getProjeImage();
			PdfPTable tableImage = null;
			if (image != null) {
				tableImage = new PdfPTable(1);
				com.itextpdf.text.pdf.PdfPCell cellImage = new com.itextpdf.text.pdf.PdfPCell(image);
				cellImage.setHorizontalAlignment(Element.ALIGN_CENTER);

				cellImage.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cellImage.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
				tableImage.addCell(cellImage);
				document.add(tableImage);
				document.add(bos);
			}

			ENumHareketYon hareketYon = null;
			tipAciklama = null;
			if (tipi != null) {
				hareketYon = ENumHareketYon.fromValue(tipi);
				if (hareketYon != null) {
					if (hareketYon.equals(ENumHareketYon.CIFT_YON) == false)
						tipAciklama = MySQLTerminal.getTipAciklama(hareketYon);
					else
						hareketYon = null;
				}

			}
			Gson gson = new Gson();

			String aciklama = adi;
			String koduStr = kodu;
			if (hareketYon != null) {
				int index = koduStr.indexOf("_" + hareketYon.value());
				if (index > 0)
					koduStr = koduStr.substring(0, index);
				index = aciklama.indexOf(tipAciklama);
				if (index > 0) {
					aciklama = adi.substring(0, index - 1);
					aciklama += " " + adi.substring(index + tipAciklama.length());
					aciklama = PdksUtil.replaceAll(aciklama, "  ", " ").trim();

				}
			}
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.put(MySQLTerminal.JSON_ADI, aciklama);
			map.put(MySQLTerminal.JSON_KODU, koduStr);
			if (hareketYon != null)
				map.put(MySQLTerminal.JSON_TIPI, hareketYon.value());

			String text = gson.toJson(map);
			data = ortakIslemler.generateQR(text, null, null);
			image = Image.getInstance(data);
			tableImage = new PdfPTable(1);
			com.itextpdf.text.pdf.PdfPCell cellImage = new com.itextpdf.text.pdf.PdfPCell(image);
			cellImage.setHorizontalAlignment(Element.ALIGN_CENTER);

			cellImage.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cellImage.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
			tableImage.addCell(cellImage);
			document.add(bos);
			document.add(bos);
			document.add(PDFITextUtils.getParagraph(aciklama, fontBaslik, Element.ALIGN_CENTER));
			document.add(bos);
			document.add(bos);
			document.add(tableImage);

			if (PdksUtil.hasStringValue(tipAciklama)) {
				tipAciklama = "Personel " + tipAciklama;
				document.add(bos);
				document.add(PDFITextUtils.getParagraph(tipAciklama.toUpperCase(Constants.TR_LOCALE), fontFooter, Element.ALIGN_CENTER));
			}

			document.close();
			baosPDF.close();
			if (baosPDF != null && baosPDF.size() > 0) {

				String fileName = "qrCihaz.pdf";
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
				ServletOutputStream sos = response.getOutputStream();
				String characterEncoding = "ISO-8859-9";
				response.setContentType("application/pdf;charset=" + characterEncoding);
				response.setCharacterEncoding(characterEncoding);
				String fileNameURL = PdksUtil.encoderURL(fileName, characterEncoding);
				response.setHeader("Content-Disposition", "attachment;filename=" + fileNameURL);
				response.setContentLength(baosPDF.size());
				baosPDF.writeTo(sos);
				sos.flush();
				sos.close();
				FacesContext.getCurrentInstance().responseComplete();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;

	}

	public void fillTerminalList() {
		terminal = null;
		if (yonTipiList == null)
			yonTipiList = new ArrayList<SelectItem>();
		else
			yonTipiList.clear();
		MySQLTerminal tmp = new MySQLTerminal();
		for (ENumHareketYon c : ENumHareketYon.values()) {
			tmp.setHareketYon(c);
			yonTipiList.add(new SelectItem(c.value(), tmp.getTipAciklama()));
		}
		terminalList = pdksEntityController.getSQLTableList(MySQLTerminal.TABLE_NAME, MySQLTerminal.class, session);
	}

	public MySQLTerminal getTerminal() {
		return terminal;
	}

	public void setTerminal(MySQLTerminal terminal) {
		this.terminal = terminal;
	}

	public List<MySQLTerminal> getTerminalList() {
		return terminalList;
	}

	public void setTerminalList(List<MySQLTerminal> terminalList) {
		this.terminalList = terminalList;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public List<SelectItem> getYonTipiList() {
		return yonTipiList;
	}

	public void setYonTipiList(List<SelectItem> yonTipiList) {
		this.yonTipiList = yonTipiList;
	}

	public String getTipAciklama() {
		return tipAciklama;
	}

	public void setTipAciklama(String tipAciklama) {
		this.tipAciklama = tipAciklama;
	}

	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	public String getKodu() {
		return kodu;
	}

	public void setKodu(String kodu) {
		this.kodu = kodu;
	}

	public Integer getTipi() {
		return tipi;
	}

	public void setTipi(Integer tipi) {
		this.tipi = tipi;
	}

}
