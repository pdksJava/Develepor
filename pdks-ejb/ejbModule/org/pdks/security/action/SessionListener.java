package org.pdks.security.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.pdks.security.entity.User;
import org.pdks.session.PdksUtil;

public class SessionListener implements HttpSessionListener, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6192060615247592440L;

	static final String LIST_NAME = "pdksSession";
	static Logger logger = Logger.getLogger(SessionListener.class);

	public static final String SESSION_USER_NAME = "authenticatedUser";

	public static List<HttpSession> getSessionList(ServletContext servletContext) {

		List sessionList = null;

		if (servletContext.getAttribute(LIST_NAME) != null)
			sessionList = (List<HttpSession>) servletContext.getAttribute(LIST_NAME);
		else {
			sessionList = new ArrayList<HttpSession>();
			servletContext.setAttribute(LIST_NAME, sessionList);
		}
		return sessionList;
	}

	public SessionListener() {

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		session.setMaxInactiveInterval(1200);
		User authenticatedUser = (User) session.getAttribute(SESSION_USER_NAME);
		List<HttpSession> sessionList = null;
		if (authenticatedUser == null)
			sessionList = getSessionList(session.getServletContext());
		synchronized (this) {
			if (sessionList != null)
				sessionList.add(session);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		StringBuilder sb = new StringBuilder();
		User authenticatedUser = (User) session.getAttribute(SESSION_USER_NAME);
		if (authenticatedUser != null) {
			PdksUtil.getSessionUser(null, authenticatedUser);
			sb.append(authenticatedUser.getUsername() + " " + authenticatedUser.getAdSoyad() + " kullanıcısı PDKS sisteminden logout oldu.");

		}
		List<HttpSession> sessionList = getSessionList(session.getServletContext());
		synchronized (this) {
			if (sessionList.contains(session))
				sessionList.remove(session);
		}
		if (!sessionList.isEmpty())
			sb.append("PDKS are now : " + sessionList.size());
		String message = sb.toString();
		if (authenticatedUser != null)
			logger.info(message);
		message = null;
		sb = null;

	}

}