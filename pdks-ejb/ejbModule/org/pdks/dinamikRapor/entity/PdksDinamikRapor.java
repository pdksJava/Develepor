package org.pdks.dinamikRapor.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.pdks.entity.BasePDKSObject;

@Entity(name = PdksDinamikRapor.TABLE_NAME)
public class PdksDinamikRapor extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1278693288034326146L;

	public static final String TABLE_NAME = "PDKS_DINAMIK_RAPOR";

	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";
	public static final String COLUMN_NAME_DB_TANIM = "DB_TANIM";
	public static final String COLUMN_NAME_RAPOR_TIPI = "RAPOR_TIPI";
	public static final String COLUMN_NAME_GORUNTULENSIN = "GORUNTULENSIN";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	private String aciklama, dbTanim;

	private PdksDinamikRaporTipi raporTipi;

	private Integer raporTipiId;

	private Boolean durum = Boolean.TRUE, goruntulemeDurum = Boolean.FALSE;

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = COLUMN_NAME_DB_TANIM)
	public String getDbTanim() {
		return dbTanim;
	}

	public void setDbTanim(String dbTanim) {
		this.dbTanim = dbTanim;
	}

	@Column(name = COLUMN_NAME_RAPOR_TIPI)
	public Integer getRaporTipiId() {
		return raporTipiId;
	}

	public void setRaporTipiId(Integer value) {
		this.raporTipi = null;
		if (value != null)
			this.raporTipi = PdksDinamikRaporTipi.fromValue(value);
		this.raporTipiId = value;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Column(name = COLUMN_NAME_GORUNTULENSIN)
	public Boolean getGoruntulemeDurum() {
		return goruntulemeDurum;
	}

	public void setGoruntulemeDurum(Boolean goruntulemeDurum) {
		this.goruntulemeDurum = goruntulemeDurum;
	}

	@Transient
	public PdksDinamikRaporTipi getRaporTipi() {
		return raporTipi;
	}

	public void setRaporTipi(PdksDinamikRaporTipi raporTipi) {
		this.raporTipi = raporTipi;
	}

	@Transient
	public static String getPdksDinamikRaporAlanAciklama(Integer key) {
		String str = "";
		if (key != null) {
			if (key.equals(PdksRaporAlanTipi.KARAKTER.value()))
				str = "Karakter";
			else if (key.equals(PdksRaporAlanTipi.SAYISAL.value()))
				str = "Sayısal";
			else if (key.equals(PdksRaporAlanTipi.TARIH.value()))
				str = "Tarih";
			else if (key.equals(PdksRaporAlanTipi.SAAT.value()))
				str = "Saat";
			else if (key.equals(PdksRaporAlanTipi.TARIH_SAAT.value()))
				str = "Tarih Saat";
		}
		return str;
	}

	@Transient
	public static String getPdksDinamikRaporAlanhHizalaAciklama(Integer key) {
		String str = "";
		if (key != null) {
			if (key.equals(PdksRaporAlanHizalaTipi.SAGA.value()))
				str = "Sağa";
			else if (key.equals(PdksRaporAlanHizalaTipi.SOLA.value()))
				str = "Sola";
			else if (key.equals(PdksRaporAlanHizalaTipi.ORTALA.value()))
				str = "Ortala";

		}
		return str;
	}

	@Transient
	public static String getPdksDinamikRaporTipiAciklama(Integer key) {
		String str = "";
		if (key != null) {
			if (key.equals(PdksDinamikRaporTipi.VIEW.value()))
				str = "View";
			else if (key.equals(PdksDinamikRaporTipi.FUNCTION.value()))
				str = "Function";
			else if (key.equals(PdksDinamikRaporTipi.STORE_PROCEDURE.value()))
				str = "Store Procedure";
		}

		return str;
	}

	@Transient
	public String getPdksDinamikRaporTipiAciklama() {
		return PdksDinamikRapor.getPdksDinamikRaporTipiAciklama(raporTipiId);
	}

	@Transient
	public boolean isView() {
		return raporTipiId != null && raporTipiId.equals(PdksDinamikRaporTipi.VIEW.value());
	}

	@Transient
	public boolean isFunction() {
		return raporTipiId != null && raporTipiId.equals(PdksDinamikRaporTipi.FUNCTION.value());
	}

	@Transient
	public boolean isStoreProcedure() {
		return raporTipiId != null && raporTipiId.equals(PdksDinamikRaporTipi.STORE_PROCEDURE.value());
	}

	public void entityRefresh() {
		// TODO Auto-generated method stub

	}
}
