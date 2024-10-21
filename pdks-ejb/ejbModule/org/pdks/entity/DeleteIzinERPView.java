package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = DeleteIzinERPView.VIEW_NAME)
@Immutable
public class DeleteIzinERPView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5536485828562045534L;
	static Logger logger = Logger.getLogger(DeleteIzinERPView.class);
	/**
	 * 
	 */
	public static final String VIEW_NAME = "Z_NOT_USED_TABLE_IZIN_ERP_DELETE";
	public static final String COLUMN_NAME_ID = "REFERANS_ID";
	public static final String COLUMN_NAME_IZIN = "IZIN_ID";
	public static final String COLUMN_NAME_GUNCELLEME_ZAMANI = "UPDATEDATETIME";

	private String id;
	private PersonelIzin personelIzin;
	private Date guncellemeTarihi;

	@Id
	@Column(name = COLUMN_NAME_ID)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(insertable = false, updatable = false, name = COLUMN_NAME_IZIN, referencedColumnName = "ID", nullable = true)
	@Fetch(FetchMode.JOIN)
	public PersonelIzin getPersonelIzin() {
		return personelIzin;
	}

	public void setPersonelIzin(PersonelIzin personelIzin) {
		this.personelIzin = personelIzin;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_ZAMANI)
	public Date getGuncellemeTarihi() {
		return guncellemeTarihi;
	}

	public void setGuncellemeTarihi(Date guncellemeTarihi) {
		this.guncellemeTarihi = guncellemeTarihi;
	}

}
