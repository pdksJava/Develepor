package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = IzinReferansERP.TABLE_NAME)
public class IzinReferansERP implements Serializable {
	// seam-gen attributes (you should probably edit these)

	/**
	 * 
	 */
	private static final long serialVersionUID = -1508779437229330518L;
	static Logger logger = Logger.getLogger(IzinReferansERP.class);

	public static final String TABLE_NAME = "IZIN_REFERANS_ERP";
	public static final String COLUMN_NAME_ID = "REFERANS_ID";
	public static final String COLUMN_NAME_IZIN_ID = "IZIN_ID";
	public static final String COLUMN_NAME_SILINEBILIR = "SILINEBILIR";
	public static final String PDKS_REFERANS_START = "NP_";

	private String id;

	private PersonelIzin izin;

	private Boolean silinebilir = Boolean.TRUE;

	public IzinReferansERP() {
		super();

	}

	public IzinReferansERP(String id) {
		super();
		this.id = id;
		this.silinebilir = Boolean.TRUE;
		this.izin = new PersonelIzin();
	}

	@Id
	@Column(name = COLUMN_NAME_ID)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_IZIN_ID, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelIzin getIzin() {
		return izin;
	}

	public void setIzin(PersonelIzin izin) {
		this.izin = izin;
	}

	@Column(name = COLUMN_NAME_SILINEBILIR)
	public Boolean getSilinebilir() {
		return silinebilir;
	}

	public void setSilinebilir(Boolean silinebilir) {
		this.silinebilir = silinebilir;
	}

	@Transient
	public Object getSortAlan() {
		Object sortAlan = izin != null ? izin.getBitisZamani() : new Date();
		return sortAlan;
	}

}
