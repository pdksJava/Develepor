package org.pdks.dinamikRapor.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Transient;

import org.pdks.entity.BasePDKSObject;

public class PdksDinamikRaporBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8144919740882242981L;

	private PdksRaporAlanTipi raporAlanTipi;

	private String karakterDeger;

	private BigDecimal decimalDeger;

	private Double doubleDeger;

	private Date tarihDeger;
	
	@Transient
	public String getRaporAlanTipiAciklama() {
		String str="";
		if (raporAlanTipi != null )
			str=PdksDinamikRapor.getPdksDinamikRaporAlanAciklama(raporAlanTipi.value());
		return str;
	}

	@Transient
	public boolean isSaat() {
		return raporAlanTipi != null && raporAlanTipi.value().equals(PdksRaporAlanTipi.SAAT.value());
	}

	@Transient
	public boolean isTarihSaat() {
		return raporAlanTipi != null && raporAlanTipi.value().equals(PdksRaporAlanTipi.TARIH_SAAT.value());
	}



	@Transient
	public boolean isKarakter() {
		return raporAlanTipi != null && raporAlanTipi.value().equals(PdksRaporAlanTipi.KARAKTER.value());
	}

	@Transient
	public boolean isSayisal() {
		return raporAlanTipi != null && raporAlanTipi.value().equals(PdksRaporAlanTipi.SAYISAL.value());
	}

	@Transient
	public boolean isTarih() {
		return raporAlanTipi != null && raporAlanTipi.value().equals(PdksRaporAlanTipi.TARIH.value());
	}

	@Transient
	public PdksRaporAlanTipi getRaporAlanTipi() {
		return raporAlanTipi;
	}

	public void setRaporAlanTipi(PdksRaporAlanTipi raporAlanTipi) {
		this.raporAlanTipi = raporAlanTipi;
	}

	@Transient
	public Date getTarihDeger() {
		return tarihDeger;
	}

	public void setTarihDeger(Date tarihDeger) {
		this.tarihDeger = tarihDeger;
	}

	@Transient
	public String getKarakterDeger() {
		return karakterDeger;
	}

	public void setKarakterDeger(String karakterDeger) {
		this.karakterDeger = karakterDeger;
	}

	@Transient
	public BigDecimal getDecimalDeger() {
		return decimalDeger;
	}

	public void setDecimalDeger(BigDecimal decimalDeger) {
		this.decimalDeger = decimalDeger;
	}

	@Transient
	public Double getDoubleDeger() {
		return doubleDeger;
	}

	public void setDoubleDeger(Double doubleDeger) {
		this.doubleDeger = doubleDeger;
	}

	public void entityRefresh() {
		// TODO Auto-generated method stub

	}

	@Transient
	public Object clone() {
		BasePDKSObject object = null;
		try {
			object = (BasePDKSObject) super.clone();
			object.setId(null);
		} catch (CloneNotSupportedException e) {

		}
		return object;
	}

}
