package org.bordro.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
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
	public static final String VIEW_NAME = "Z_NOT_USED_TABLE_IZIN_BORDRO_ERP_DELETE";
	public static final String COLUMN_NAME_ID = "REFERANS_ID";
	public static final String COLUMN_NAME_DURUM = "DURUM";
	public static final String COLUMN_NAME_GUNCELLEME_ZAMANI = "UPDATEDATETIME";

	private String id;

	private Date guncellemeTarihi;

	private Boolean durum;

	@Id
	@Column(name = COLUMN_NAME_ID)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_GUNCELLEME_ZAMANI)
	public Date getGuncellemeTarihi() {
		return guncellemeTarihi;
	}

	public void setGuncellemeTarihi(Date guncellemeTarihi) {
		this.guncellemeTarihi = guncellemeTarihi;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

}
