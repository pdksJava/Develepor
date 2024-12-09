package com.pdks.webService;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.pdks.kgs.model.Cihaz;
import org.pdks.kgs.model.CihazGecis;
import org.pdks.kgs.model.CihazPersonel;
import org.pdks.kgs.model.Sonuc;

public class CihazVeriAktar implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6449982763733901391L;

	private String fonksiyon;

	@Resource
	private WebServiceContext context;

	public Sonuc saveCihaz(@WebParam(name = "cihazlar") List<Cihaz> cihazlar) throws Exception {
		fonksiyon = "saveCihaz";
		Sonuc sonuc = null;
		LinkedHashMap<String, String> headers = getHeaders();
		if (headers != null && headers.isEmpty() == false) {
			if (cihazlar != null && cihazlar.isEmpty() == false) {
				CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
				sonuc = cihazVeriOrtakAktar.saveCihaz(cihazlar, headers);
			} else
				sonuc = getKullaniciHatali("Cihaz yok!");

		} else
			sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");
		return sonuc;
	}

	public Sonuc savePersonel(@WebParam(name = "personeller") List<CihazPersonel> personeller) throws Exception {
		fonksiyon = "savePersonel";
		Sonuc sonuc = null;
		LinkedHashMap<String, String> headers = getHeaders();
		if (headers != null && headers.isEmpty() == false) {
			if (personeller != null && personeller.isEmpty() == false) {
				CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
				sonuc = cihazVeriOrtakAktar.savePersonel(personeller, headers);
			} else
				sonuc = getKullaniciHatali("Personel yok!");

		} else
			sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");
		return sonuc;
	}

	public Sonuc saveCihazGecis(@WebParam(name = "gecisler") List<CihazGecis> gecisler) throws Exception {
		fonksiyon = "saveCihazGecis";
		Sonuc sonuc = null;
		LinkedHashMap<String, String> headers = getHeaders();
		if (headers != null && headers.isEmpty() == false) {
			if (gecisler != null && gecisler.isEmpty() == false) {
				CihazVeriOrtakAktar cihazVeriOrtakAktar = new CihazVeriOrtakAktar(fonksiyon);
				sonuc = cihazVeriOrtakAktar.saveCihazGecis(gecisler, headers);
			} else
				sonuc = getKullaniciHatali("Cihaz geçiş yok!");
		} else
			sonuc = getKullaniciHatali("Kullanıcı bilgileri eksik!");
		return sonuc;
	}

	private LinkedHashMap<String, String> getHeaders() {
		LinkedHashMap<String, String> headerMap = new LinkedHashMap<String, String>();
		MessageContext messageContext = (MessageContext) context.getMessageContext();
		if (messageContext == null || !(messageContext instanceof WrappedMessageContext)) {
			return headerMap;
		}
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
