package org.pdks.kgs.model;

import java.io.Serializable;

public class CihazGecis implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8533398208816407811L;

	private static String tarihFormat, saatFormat;

	private Long id;

	private Long cihazId;

	private Long personelId;

	private String tarih;

	private String saat;

	private String tipi;

	private Integer durum = Durum.AKTIF.value();

	public CihazGecis() {
		super();
	}

	// public CihazGecis(Long id, Long cihazId, Long personelId, String tarih, String saat, String tipi, Boolean durum) {
	// super();
	// this.id = id;
	// this.cihazId = cihazId;
	// this.personelId = personelId;
	// this.tarih = tarih;
	// this.saat = saat;
	// this.tipi = tipi;
	// this.durum = durum;
	// }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCihazId() {
		return cihazId;
	}

	public void setCihazId(Long cihazId) {
		this.cihazId = cihazId;
	}

	public Long getPersonelId() {
		return personelId;
	}

	public void setPersonelId(Long personelId) {
		this.personelId = personelId;
	}

	public String getTarih() {
		return tarih;
	}

	public void setTarih(String tarih) {
		this.tarih = tarih;
	}

	public String getSaat() {
		return saat;
	}

	public void setSaat(String saat) {
		this.saat = saat;
	}

	public String getTipi() {
		return tipi;
	}

	public void setTipi(String tipi) {
		this.tipi = tipi;
	}

	public Integer getDurum() {
		return durum;
	}

	public void setDurum(Integer durum) {
		this.durum = durum;
	}

	public static String getTarihFormat() {
		return tarihFormat;
	}

	public static void setTarihFormat(String tarihFormat) {
		CihazGecis.tarihFormat = tarihFormat;
	}

	public static String getSaatFormat() {
		return saatFormat;
	}

	public static void setSaatFormat(String saatFormat) {
		CihazGecis.saatFormat = saatFormat;
	}
}
