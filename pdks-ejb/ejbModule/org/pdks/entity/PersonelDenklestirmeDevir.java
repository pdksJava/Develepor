package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = PersonelDenklestirmeDevir.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDenklestirmeDevir.COLUMN_NAME_PERSONEL_DENKLESTIRME }) })
public class PersonelDenklestirmeDevir extends BaseObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1342234866826019287L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeDevir.class);

	public static final String TABLE_NAME = "PERS_DENK_DEVIR";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME = "PERS_DENK_ID";
	public static final String COLUMN_NAME_GECEN_AY_DEVIR_SAAT = "GECEN_AY_DEVIR_SAAT";

	private PersonelDenklestirme personelDenklestirme;

	private Double gecenAyDevirSaat = 0.0d;

	public PersonelDenklestirmeDevir() {
		super();

	}

	public PersonelDenklestirmeDevir(PersonelDenklestirme personelDenklestirme, Double gecenAyDevirSaat) {
		super();
		this.personelDenklestirme = personelDenklestirme;
		this.gecenAyDevirSaat = gecenAyDevirSaat;

	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_DENKLESTIRME, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelDenklestirme getPersonelDenklestirme() {
		return personelDenklestirme;
	}

	public void setPersonelDenklestirme(PersonelDenklestirme personelDenklestirme) {
		this.personelDenklestirme = personelDenklestirme;
	}

	@Column(name = COLUMN_NAME_GECEN_AY_DEVIR_SAAT)
	public Double getGecenAyDevirSaat() {
		return gecenAyDevirSaat;
	}

	public void setGecenAyDevirSaat(Double gecenAyDevirSaat) {
		this.gecenAyDevirSaat = gecenAyDevirSaat;
	}

	@Transient
	public Long getPersonelDenklestirmeId() {
		Long personelDenklestirmeId = personelDenklestirme != null ? personelDenklestirme.getId() : null;
		return personelDenklestirmeId;
	}

	public void entityRefresh() {

	}

}
