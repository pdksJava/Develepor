package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.pdks.enums.MethodAPI;

@Entity(name = FazlaMesaiERP.TABLE_NAME)
public class FazlaMesaiERP extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1067969681278459227L;
	static Logger logger = Logger.getLogger(FazlaMesaiERP.class);
	public static final String TABLE_NAME = "FAZLA_MESAI_ERP";
	public static final String COLUMN_NAME_ERP_SIRKET = "ERP_SIRKET_ADI";
	public static final String COLUMN_NAME_ODENEN_SAAT = "ODENEN_SAAT_KOLON_YAZ";
	public static final String COLUMN_NAME_URL = "SERVER_URL";
	public static final String COLUMN_NAME_UOM = "UOM_ALAN_ADI";
	public static final String COLUMN_NAME_ROOT = "KOK_ADI";
	public static final String COLUMN_NAME_RT = "RT_ALAN_ADI";
	public static final String COLUMN_NAME_HT = "HT_ALAN_ADI";
	public static final String COLUMN_NAME_METHOT_ADI = "METHOT_ADI";
	public static final String COLUMN_NAME_LOGIN = "LOGIN";

	private String sirketAdi, serverURL, rootAdi, uomAlanAdi, rtAlanAdi, htAlanAdi, loginBilgi, methodAdi = MethodAPI.POST.value();

	private boolean odenenSaatKolonYaz;

	private MethodAPI methodAPI;

	@Column(name = COLUMN_NAME_ERP_SIRKET, nullable = false)
	public String getSirketAdi() {
		return sirketAdi;
	}

	public void setSirketAdi(String sirketAdi) {
		this.sirketAdi = sirketAdi;
	}

	@Column(name = COLUMN_NAME_URL, nullable = false)
	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	@Column(name = COLUMN_NAME_METHOT_ADI, nullable = false)
	public String getMethodAdi() {
		return methodAdi;
	}

	public void setMethodAdi(String value) {
		this.methodAPI = value != null ? MethodAPI.fromValue(value) : null;
		this.methodAdi = value;
	}

	@Column(name = COLUMN_NAME_UOM, nullable = false)
	public String getUomAlanAdi() {
		return uomAlanAdi;
	}

	public void setUomAlanAdi(String uomAlanAdi) {
		this.uomAlanAdi = uomAlanAdi;
	}

	@Column(name = COLUMN_NAME_RT, nullable = false)
	public String getRtAlanAdi() {
		return rtAlanAdi;
	}

	public void setRtAlanAdi(String rtAlanAdi) {
		this.rtAlanAdi = rtAlanAdi;
	}

	@Column(name = COLUMN_NAME_HT)
	public String getHtAlanAdi() {
		return htAlanAdi;
	}

	public void setHtAlanAdi(String htAlanAdi) {
		this.htAlanAdi = htAlanAdi;
	}

	@Column(name = COLUMN_NAME_ODENEN_SAAT)
	public boolean isOdenenSaatKolonYaz() {
		return odenenSaatKolonYaz;
	}

	public void setOdenenSaatKolonYaz(boolean odenenSaatKolonYaz) {
		this.odenenSaatKolonYaz = odenenSaatKolonYaz;
	}

	@Column(name = COLUMN_NAME_ROOT)
	public String getRootAdi() {
		return rootAdi;
	}

	public void setRootAdi(String rootAdi) {
		this.rootAdi = rootAdi;
	}

	@Column(name = COLUMN_NAME_LOGIN)
	public String getLoginBilgi() {
		return loginBilgi;
	}

	public void setLoginBilgi(String loginBilgi) {
		this.loginBilgi = loginBilgi;
	}

	@Transient
	public MethodAPI getMethodAPI() {
		return methodAPI;
	}

	public void setMethodAPI(MethodAPI methodAPI) {
		this.methodAPI = methodAPI;
	}

	public void entityRefresh() {

	}

}
