package org.pdks.kgs.model;

import java.io.Serializable;

public class Cihaz implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8533398208816407811L;

	private Long id;

	private String adi;

	private String tipi;

	private CihazTipi cihazTipi;

	private Boolean durum = Boolean.TRUE;

	public Cihaz() {
		super();
	}

	public Cihaz(Long id, String adi, String tipi, Boolean durum) {
		super();
		this.id = id;
		this.adi = adi;
		this.tipi = tipi;
		this.durum = durum;
		this.setCihazTipi(CihazTipi.fromValue(tipi));
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAdi() {
		return adi;
	}

	public void setAdi(String adi) {
		this.adi = adi;
	}

	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.tipi = tipi;
	}

	public CihazTipi getCihazTipi() {
		if (cihazTipi == null && tipi != null)
			this.cihazTipi = CihazTipi.fromValue(tipi);
		return cihazTipi;
	}

	public void setCihazTipi(CihazTipi cihazTipi) {
		this.cihazTipi = cihazTipi;
	}

	public Boolean getDurum() {
		return durum;
	}

	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

}
