package com.pdks.webService;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;
import org.pdks.kgs.model.Cihaz;
import org.pdks.kgs.model.CihazGecis;
import org.pdks.kgs.model.CihazPersonel;
import org.pdks.kgs.model.CihazUser;
import org.pdks.kgs.model.Sonuc;

@WebService(targetNamespace = "http://webService.pdks.com/", portName = "CihazSoapVeriAktarPort", serviceName = "CihazSoapVeriAktarService")
@InInterceptors(interceptors = { "com.pdks.webService.WSLoggingInInterceptor" })
public class CihazSoapVeriAktar implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6449982763733901391L;
	public Logger logger = Logger.getLogger(CihazSoapVeriAktar.class);

	private String fonksiyon;

	@Resource
	private WebServiceContext context;

	@Context
	HttpServletRequest request;

	@WebResult(name = "sonuc", targetNamespace = "")
	@WebMethod(operationName = "saveCihaz", action = "urn:SaveCihaz")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SaveCihaz", localName = "saveCihaz", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SaveCihazResponse", localName = "saveCihazResponse", targetNamespace = "http://webService.pdks.com/")
	public Sonuc saveCihaz(@WebParam(name = "cihazlar") List<Cihaz> cihazlar, @WebParam(name = "user") CihazUser user) {
		fonksiyon = "saveCihaz";
		Sonuc sonuc = null;
		if (user != null) {
			if (cihazlar != null && cihazlar.isEmpty() == false) {
				CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
				try {
					sonuc = cihazVeriOrtakAktar.saveCihaz(cihazlar, user);
				} catch (Exception e) {
					if (e.getMessage() != null)
						sonuc = getKullaniciHatali(e.getMessage());
					else
						sonuc = getKullaniciHatali("Hata oluştu!");
				}
			} else
				sonuc = getKullaniciHatali("Cihaz yok!");

		} else
			sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");
		return sonuc;
	}

	@WebResult(name = "sonuc", targetNamespace = "")
	@WebMethod(operationName = "savePersonel", action = "urn:SavePersonel")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SavePersonel", localName = "savePersonel", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SavePersonelResponse", localName = "savePersonelResponse", targetNamespace = "http://webService.pdks.com/")
	public Sonuc savePersonel(@WebParam(name = "personeller") List<CihazPersonel> personeller, @WebParam(name = "user") CihazUser user) {
		fonksiyon = "savePersonel";
		Sonuc sonuc = null;
		if (user != null) {
			if (personeller != null && personeller.isEmpty() == false) {
				CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
				try {
					sonuc = cihazVeriOrtakAktar.savePersonel(personeller, user);
				} catch (Exception e) {
					if (e.getMessage() != null)
						sonuc = getKullaniciHatali(e.getMessage());
					else
						sonuc = getKullaniciHatali("Hata oluştu!");
				}
			} else
				sonuc = getKullaniciHatali("Personel yok!");

		} else
			sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");
		return sonuc;
	}

	@WebResult(name = "sonuc", targetNamespace = "")
	@WebMethod(operationName = "saveCihazGecis", action = "urn:SaveCihazGecis")
	@RequestWrapper(className = "com.pdks.webService.jaxws.SaveCihazGecis", localName = "saveCihazGecis", targetNamespace = "http://webService.pdks.com/")
	@ResponseWrapper(className = "com.pdks.webService.jaxws.SaveCihazGecisResponse", localName = "saveCihazGecisResponse", targetNamespace = "http://webService.pdks.com/")
	public Sonuc saveCihazGecis(@WebParam(name = "gecisler") List<CihazGecis> gecisler, @WebParam(name = "user") CihazUser user) {
		fonksiyon = "saveCihazGecis";
		Sonuc sonuc = null;
		if (user != null) {
			if (gecisler != null && gecisler.isEmpty() == false) {
				CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
				try {
					sonuc = cihazVeriOrtakAktar.saveCihazGecis(gecisler, user);
				} catch (Exception e) {
					if (e.getMessage() != null)
						sonuc = getKullaniciHatali(e.getMessage());
					else
						sonuc = getKullaniciHatali("Hata oluştu!");
				}
			} else
				sonuc = getKullaniciHatali("Cihaz geçiş yok!");
		} else
			sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");
		return sonuc;
	}

	protected LinkedHashMap<String, String> getMessageHeaders() {
		LinkedHashMap<String, String> headerMap = new LinkedHashMap<String, String>();
		if (context.getMessageContext() != null) {
			try {
				MessageContext messageContext = (MessageContext) context.getMessageContext();
				if (!(messageContext == null || !(messageContext instanceof WrappedMessageContext))) {

					Message message = ((WrappedMessageContext) messageContext).getWrappedMessage();
					List<Header> headers = CastUtils.cast((List<?>) message.get(Header.HEADER_LIST));
					if (headers != null) {

						// String username = null, password = null;
						// for (Header header : headers) {
						//
						// }
						// if (username != null)
						// headerMap.put("username", username);
						// if (password != null)
						// headerMap.put("password", password);

					}
				}
			} catch (Exception e) {

			}

		}

		return headerMap;
	}

	/**
	 * @param mesaj
	 * @return
	 */
	private Sonuc getKullaniciHatali(String mesaj) {
		Sonuc sonuc = new Sonuc(fonksiyon, mesaj == null, mesaj);
		return sonuc;
	}
}
