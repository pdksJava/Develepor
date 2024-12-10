package org.pdks.kgs.model;

import java.io.Serializable;

import javax.persistence.Transient;

public class Cihaz implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8533398208816407811L;

	private Long id;

	private String adi;

	private Integer tipi;

	private Integer durum = Durum.AKTIF.value();

	public Cihaz() {
		super();
	}

	public Cihaz(Long id, String adi, Integer tipi, int durum) {
		super();
		this.id = id;
		this.adi = adi;
		this.tipi = tipi;
		this.durum = durum;
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

	public Integer getTipi() {
		return tipi;
	}

	public void setTipi(Integer tipi) {
		this.tipi = tipi;
	}

	public Integer getDurum() {
		return durum;
	}

	public void setDurum(Integer durum) {
		this.durum = durum;
	}

	@Transient
	public Object clone() {
		try {

			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}
}
