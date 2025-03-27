package org.pdks.dinamikRapor.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.pdks.dinamikRapor.enums.ENumAlanHizalaTipi;
import org.pdks.dinamikRapor.enums.ENumDinamikRaporTipi;
import org.pdks.dinamikRapor.enums.ENumEsitlik;
import org.pdks.dinamikRapor.enums.ENumRaporAlanTipi;
import org.pdks.entity.BasePDKSObject;
import org.pdks.session.PdksUtil;

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
	public static final String COLUMN_NAME_WHERE_SQL = "WHERE_SQL";
	public static final String COLUMN_NAME_ORDER_SQL = "ORDER_SQL";
	public static final String COLUMN_NAME_DURUM = "DURUM";

	private String aciklama, dbTanim, whereSQL = "", orderSQL = "";

	private ENumDinamikRaporTipi raporTipi;

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
			this.raporTipi = ENumDinamikRaporTipi.fromValue(value);
		this.raporTipiId = value;
	}

	@Column(name = COLUMN_NAME_GORUNTULENSIN)
	public Boolean getGoruntulemeDurum() {
		return goruntulemeDurum;
	}

	public void setGoruntulemeDurum(Boolean goruntulemeDurum) {
		this.goruntulemeDurum = goruntulemeDurum;
	}

	@Column(name = COLUMN_NAME_WHERE_SQL)
	public String getWhereSQL() {
		return whereSQL;
	}

	public void setWhereSQL(String whereSQL) {
		this.whereSQL = whereSQL;
	}

	@Column(name = COLUMN_NAME_ORDER_SQL)
	public String getOrderSQL() {
		return orderSQL;
	}

	public void setOrderSQL(String orderSQL) {
		this.orderSQL = orderSQL;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public ENumDinamikRaporTipi getRaporTipi() {
		return raporTipi;
	}

	public void setRaporTipi(ENumDinamikRaporTipi raporTipi) {
		this.raporTipi = raporTipi;
	}

	@Transient
	public static String getPdksDinamikRaporAlanAciklama(Integer key) {
		String str = "";
		if (key != null) {
			if (key.equals(ENumRaporAlanTipi.KARAKTER.value()))
				str = "Karakter";
			else if (key.equals(ENumRaporAlanTipi.SAYISAL.value()))
				str = "Sayısal";
			else if (key.equals(ENumRaporAlanTipi.TARIH.value()))
				str = "Tarih";
			else if (key.equals(ENumRaporAlanTipi.SAAT.value()))
				str = "Saat";
			else if (key.equals(ENumRaporAlanTipi.TARIH_SAAT.value()))
				str = "Tarih Saat";
		}
		return str;
	}

	@Transient
	public static String getPdksDinamikRaporAlanhHizalaAciklama(Integer key) {
		String str = "";
		if (key != null) {
			if (key.equals(ENumAlanHizalaTipi.SAGA.value()))
				str = "Sağa";
			else if (key.equals(ENumAlanHizalaTipi.SOLA.value()))
				str = "Sola";
			else if (key.equals(ENumAlanHizalaTipi.ORTALA.value()))
				str = "Ortala";

		}
		return str;
	}

	@Transient
	public static String getPdksDinamikRaporTipiAciklama(Integer key) {
		String str = "";
		if (key != null) {
			if (key.equals(ENumDinamikRaporTipi.VIEW.value()))
				str = "View";
			else if (key.equals(ENumDinamikRaporTipi.FUNCTION.value()))
				str = "Function";
			else if (key.equals(ENumDinamikRaporTipi.STORE_PROCEDURE.value()))
				str = "Store Procedure";
		}

		return str;
	}

	@Transient
	public static String getEsitlikAciklama(String value) {
		String str = "";
		if (PdksUtil.hasStringValue(value)) {
			if (value.equalsIgnoreCase(ENumEsitlik.BUYUK.value()))
				str = "Büyük";
			else if (value.equalsIgnoreCase(ENumEsitlik.BUYUKESIT.value()))
				str = "Büyük Eşit";
			else if (value.equalsIgnoreCase(ENumEsitlik.KUCUK.value()))
				str = "Küçük";
			else if (value.equalsIgnoreCase(ENumEsitlik.KUCUKESIT.value()))
				str = "Küçük Eşit";
			else if (value.equalsIgnoreCase(ENumEsitlik.ICEREN.value()))
				str = "İçeren";
			else if (value.equalsIgnoreCase(ENumEsitlik.ESIT.value()))
				str = "Eşit";
		}
		return str;
	}

	@Transient
	public String getPdksDinamikRaporTipiAciklama() {
		return PdksDinamikRapor.getPdksDinamikRaporTipiAciklama(raporTipiId);
	}

	@Transient
	public boolean isView() {
		return raporTipiId != null && raporTipiId.equals(ENumDinamikRaporTipi.VIEW.value());
	}

	@Transient
	public boolean isFunction() {
		return raporTipiId != null && raporTipiId.equals(ENumDinamikRaporTipi.FUNCTION.value());
	}

	@Transient
	public boolean isStoreProcedure() {
		return raporTipiId != null && raporTipiId.equals(ENumDinamikRaporTipi.STORE_PROCEDURE.value());
	}

	public void entityRefresh() {
		// TODO Auto-generated method stub

	}

}
