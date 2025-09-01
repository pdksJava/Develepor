package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.session.PdksUtil;

@Entity(name = PersonelDenklestirmeOrganizasyon.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDenklestirmeOrganizasyon.COLUMN_NAME_PERSONEL_DENKLESTIRME }) })
public class PersonelDenklestirmeOrganizasyon extends BasePDKSObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2592705853361084224L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeBordro.class);

	public static final String TABLE_NAME = "PERS_DENK_ORGANIZASYON";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME = "PERS_DENK_ID";
	public static final String COLUMN_NAME_SIRKET = "SIRKET_ID";
	public static final String COLUMN_NAME_TESIS = "TESIS_ID";
	public static final String COLUMN_NAME_DIREKTOR = "DIREKTOR_ID";
	public static final String COLUMN_NAME_BOLUM = "BOLUM_ID";
	public static final String COLUMN_NAME_GOREV = "GOREV_ID";

	private PersonelDenklestirme personelDenklestirme;

	private Tanim tesis, bolum, direktor, gorevTipi;

	public PersonelDenklestirmeOrganizasyon() {
		super();

	}

	public PersonelDenklestirmeOrganizasyon(PersonelDenklestirme personelDenklestirme) {
		super();
		this.personelDenklestirme = personelDenklestirme;
		if (personelDenklestirme.getPersonel() != null) {
			Personel personel = personelDenklestirme.getPersonel();
			this.tesis = personel.getSirket().getTesisDurum() ? personel.getTesis() : null;
			this.direktor = personel.getEkSaha1();
			this.bolum = personel.getEkSaha3();
			this.gorevTipi = personel.getGorevTipi();
		}

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

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_TESIS)
	@Fetch(FetchMode.JOIN)
	public Tanim getTesis() {
		return tesis;
	}

	public void setTesis(Tanim value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isTanimDegisti(tesis, value));
		this.tesis = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_BOLUM)
	@Fetch(FetchMode.JOIN)
	public Tanim getBolum() {
		return bolum;
	}

	public void setBolum(Tanim value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isTanimDegisti(bolum, value));
		this.bolum = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_DIREKTOR)
	@Fetch(FetchMode.JOIN)
	public Tanim getDirektor() {
		return direktor;
	}

	public void setDirektor(Tanim value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isTanimDegisti(direktor, value));
		this.direktor = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_GOREV)
	@Fetch(FetchMode.JOIN)
	public Tanim getGorevTipi() {
		return gorevTipi;
	}

	public void setGorevTipi(Tanim gorevTipi) {
		this.gorevTipi = gorevTipi;
	}

	@Transient
	public long getPersonelDenklestirmeId() {
		return personelDenklestirme != null ? personelDenklestirme.getIdLong() : 0l;
	}

	public void entityRefresh() {

	}

}
