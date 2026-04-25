package org.pdks.session;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

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
import org.kgs.entity.MySQLTerminal;
import org.pdks.security.entity.User;

import com.google.gson.Gson;

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
		String str = "cihazQRGoster?cihazId=" + (terminal != null ? terminal.getId() : 0L);
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

	public void qrGoster(String kodu, String adi) {
		data = null;
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("kodu", kodu);
		map.put("adi", adi);
		Gson gson = new Gson();
		String text = gson.toJson(map);
//		text = "kodu:" + kodu + ";adi:" + adi;
		data = ortakIslemler.generateQR(text, null, null, false);

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
		String cihazIdStr = (String) req.getParameter("cihazId");
		terminal = null;
		data = null;
		if (PdksUtil.hasStringValue(cihazIdStr))
			try {
				terminal = (MySQLTerminal) pdksEntityController.getSQLParamByFieldObject(MySQLTerminal.TABLE_NAME, MySQLTerminal.COLUMN_NAME_ID, Integer.parseInt(cihazIdStr), MySQLTerminal.class, session);
				if (terminal != null)
					qrGoster(terminal.getKodu(), terminal.getAciklama());
			} catch (Exception e) {
				// TODO: handle exception
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

	public void fillTerminalList() {
		terminal = null;
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

}
