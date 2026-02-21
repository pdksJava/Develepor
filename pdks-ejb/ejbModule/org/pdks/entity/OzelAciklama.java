package org.pdks.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = OzelAciklama.TABLE_NAME)
public class OzelAciklama extends BasePDKSObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8870804083675438938L;
	public static final String TABLE_NAME = "OZEL_ACIKLAMA";
	public static final String COLUMN_NAME_ACIKLAMA = "ACIKLAMA";

	private String aciklama;

	@Column(name = COLUMN_NAME_ACIKLAMA, length = 256)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	public void entityRefresh() {

	}
}
