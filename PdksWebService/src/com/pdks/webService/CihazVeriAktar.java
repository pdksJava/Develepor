package com.pdks.webService;

import java.io.Serializable;
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

	public Sonuc saveCihaz(@WebParam(name = "cihazlar") List<Cihaz> cihazlar) throws Exception {
		fonksiyon = "saveCihaz";
		Sonuc sonuc = null;
		if (cihazlar != null && cihazlar.isEmpty() == false) {

		} else
			sonuc = getKullaniciHatali("Cihaz yok!");
		return sonuc;
	}

	public Sonuc savePersonel(@WebParam(name = "personeller") List<CihazPersonel> personeller) throws Exception {
		fonksiyon = "savePersonel";
		Sonuc sonuc = null;
		if (personeller != null && personeller.isEmpty() == false) {

		} else
			sonuc = getKullaniciHatali("Personel yok!");
		return sonuc;
	}

	public Sonuc saveCihazGecis(@WebParam(name = "gecisler") List<CihazGecis> gecisler) throws Exception {
		fonksiyon = "saveCihazGecis";
		Sonuc sonuc = null;
		if (gecisler != null && gecisler.isEmpty() == false) {

		} else
			sonuc = getKullaniciHatali("Cihaz geçiş yok!");
		return sonuc;
	}
	@Resource
	private WebServiceContext context;

	private List<Header> getHeaders() {
		MessageContext messageContext = (MessageContext) context.getMessageContext();
	    if (messageContext == null || !(messageContext instanceof WrappedMessageContext)) {
	        return null;
	    }

	    Message message = ((WrappedMessageContext) messageContext).getWrappedMessage();
	    List<Header> headers = CastUtils.cast((List<?>) message.get(Header.HEADER_LIST));
	    return headers;
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
