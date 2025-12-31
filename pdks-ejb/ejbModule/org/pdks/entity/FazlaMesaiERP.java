package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.enums.MethodAPI;
import org.pdks.enums.VeriTipiAPI;
import org.pdks.session.PdksUtil;

@Entity(name = FazlaMesaiERP.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { FazlaMesaiERP.COLUMN_NAME_ERP_SISTEM }) })
public class FazlaMesaiERP extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1067969681278459227L;
	static Logger logger = Logger.getLogger(FazlaMesaiERP.class);
	public static final String TABLE_NAME = "FAZLA_MESAI_ERP";
	public static final String COLUMN_NAME_ERP_SISTEM = "ERP_SISTEM_ID";
	public static final String COLUMN_NAME_ERP_SIRKET = "ERP_SIRKET_ADI";
	public static final String COLUMN_NAME_ODENEN_SAAT = "ODENEN_SAAT_KOLON_YAZ";
	public static final String COLUMN_NAME_URL = "SERVER_URL";
	public static final String COLUMN_NAME_UOM = "UOM_ALAN_ADI";
	public static final String COLUMN_NAME_ROOT = "KOK_ADI";
	public static final String COLUMN_NAME_RT = "RT_ALAN_ADI";
	public static final String COLUMN_NAME_HT = "HT_ALAN_ADI";
	public static final String COLUMN_NAME_METHOT_ADI = "METHOT_ADI";
	public static final String COLUMN_NAME_BASLIK_ALAN = "BASLIK_ALAN_ADI";
	public static final String COLUMN_NAME_DETAY_ALAN = "DETAY_ALAN_ADI";
	public static final String COLUMN_NAME_DETAY_KOK = "DETAY_KOK_ADI";
	public static final String COLUMN_NAME_SERVIS_TIPI = "SERVIS_TIPI";

	public static final String COLUMN_NAME_DETAY_BASLIK_ICINDE = "DETAY_BASLIK_ICINDE";
	public static final String COLUMN_NAME_LOGIN = "LOGIN";

	private String sirketAdi, serverURL, rootAdi, baslikAlanAdi, detayAlanAdi, detayKokAdi, uomAlanAdi, rtAlanAdi, htAlanAdi, loginBilgi, methodAdi = MethodAPI.POST.value();
	private ERPSistem erpSistem;
	private boolean odenenSaatKolonYaz;
	private Boolean detayBaslikIcineYaz = Boolean.FALSE;
	private Integer servisVeriTipi = VeriTipiAPI.JSON.value();
	private VeriTipiAPI veriTipiAPI;

	private MethodAPI methodAPI;

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_ERP_SISTEM, nullable = false)
	@Fetch(FetchMode.JOIN)
	public ERPSistem getErpSistem() {
		return erpSistem;
	}

	public void setErpSistem(ERPSistem value) {
		if (erpSistem != null && sirketAdi == null)
			this.sirketAdi = erpSistem.getSirketAdi();
		this.erpSistem = value;
	}

	@Column(name = COLUMN_NAME_ERP_SIRKET)
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

	@Column(name = COLUMN_NAME_SERVIS_TIPI)
	public Integer getServisVeriTipi() {
		return servisVeriTipi;
	}

	public void setServisVeriTipi(Integer value) {
		this.veriTipiAPI = value != null ? VeriTipiAPI.fromValue(value) : null;
		this.servisVeriTipi = value;
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

	@Column(name = COLUMN_NAME_BASLIK_ALAN)
	public String getBaslikAlanAdi() {
		return baslikAlanAdi;
	}

	public void setBaslikAlanAdi(String baslikAlanAdi) {
		this.baslikAlanAdi = baslikAlanAdi;
	}

	@Column(name = COLUMN_NAME_DETAY_ALAN)
	public String getDetayAlanAdi() {
		return detayAlanAdi;
	}

	public void setDetayAlanAdi(String detayAlanAdi) {
		this.detayAlanAdi = detayAlanAdi;
	}

	@Column(name = COLUMN_NAME_DETAY_KOK)
	public String getDetayKokAdi() {
		return detayKokAdi;
	}

	public void setDetayKokAdi(String detayKokAdi) {
		this.detayKokAdi = detayKokAdi;
	}

	@Column(name = COLUMN_NAME_DETAY_BASLIK_ICINDE)
	public Boolean getDetayBaslikIcineYaz() {
		return detayBaslikIcineYaz;
	}

	public void setDetayBaslikIcineYaz(Boolean detayBaslikIcineYaz) {
		this.detayBaslikIcineYaz = detayBaslikIcineYaz;
	}

	@Column(name = COLUMN_NAME_LOGIN)
	public String getLoginBilgi() {
		return loginBilgi;
	}

	public void setLoginBilgi(String loginBilgi) {
		this.loginBilgi = loginBilgi;
	}

	@Transient
	public boolean isDetayBaslikIcineYazin() {
		boolean yaz = PdksUtil.hasStringValue(baslikAlanAdi);
		if (yaz)
			yaz = detayBaslikIcineYaz != null && detayBaslikIcineYaz.booleanValue();
		return yaz;
	}

	@Transient
	public String getMethodAciklama() {
		String aciklama = MethodAPI.getMethodAciklama(methodAdi);
		return aciklama;
	}

	@Transient
	public String getServisTipiAciklama() {
		String aciklama = VeriTipiAPI.getServisTipiAciklama(servisVeriTipi);
		return aciklama;
	}

	@Transient
	public VeriTipiAPI getVeriTipiAPI() {
		return veriTipiAPI;
	}

	public void setVeriTipiAPI(VeriTipiAPI veriTipiAPI) {
		this.veriTipiAPI = veriTipiAPI;
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
