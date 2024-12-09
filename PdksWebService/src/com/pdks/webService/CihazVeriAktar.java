package com.pdks.webService;

import java.io.Serializable;
import java.util.List;

import javax.jws.WebParam;

import org.pdks.kgs.model.Cihaz;
import org.pdks.kgs.model.CihazGecis;
import org.pdks.kgs.model.CihazPersonel;
import org.pdks.kgs.model.Sonuc;

public class CihazVeriAktar implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6449982763733901391L;

	public Sonuc saveCihaz(@WebParam(name = "cihazlar") List<Cihaz> cihazlar) throws Exception {
		Sonuc sonuc = null;
		return sonuc;
	}

	public Sonuc savePersonel(@WebParam(name = "personeller") List<CihazPersonel> personeller) throws Exception {
		Sonuc sonuc = null;
		return sonuc;
	}

	public Sonuc saveCihazGecis(@WebParam(name = "gecisler") List<CihazGecis> gecisler) throws Exception {
		Sonuc sonuc = null;
		return sonuc;
	}
}
