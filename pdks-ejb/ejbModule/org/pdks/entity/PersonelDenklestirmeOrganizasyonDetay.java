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

@Entity(name = PersonelDenklestirmeOrganizasyonDetay.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PersonelDenklestirmeOrganizasyonDetay.COLUMN_NAME_PERSONEL_DENKLESTIRME_ORGANIZASYON, PersonelDenklestirmeOrganizasyonDetay.COLUMN_NAME_ALAN }) })
public class PersonelDenklestirmeOrganizasyonDetay extends BasePDKSObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2592705853361084224L;

	static Logger logger = Logger.getLogger(PersonelDenklestirmeOrganizasyonDetay.class);

	public static final String TABLE_NAME = "PERS_DENK_ORG_DETAY";
	public static final String COLUMN_NAME_PERSONEL_DENKLESTIRME_ORGANIZASYON = "PERS_DENK_ORG_ID";
	public static final String COLUMN_NAME_ALAN = "ALAN_ID";
	public static final String COLUMN_NAME_DEGER = "DEGER_ID";

	private PersonelDenklestirmeOrganizasyon personelDenklestirmeOrganizasyon;

	private Tanim alan, deger;

	public PersonelDenklestirmeOrganizasyonDetay() {
		super();

	}

	public PersonelDenklestirmeOrganizasyonDetay(PersonelDenklestirmeOrganizasyon personelDenklestirmeOrganizasyon,Tanim alan) {
		super();
		this.personelDenklestirmeOrganizasyon = personelDenklestirmeOrganizasyon;
		this.alan = alan;

	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_DENKLESTIRME_ORGANIZASYON, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PersonelDenklestirmeOrganizasyon getPersonelDenklestirmeOrganizasyon() {
		return personelDenklestirmeOrganizasyon;
	}

	public void setPersonelDenklestirmeOrganizasyon(PersonelDenklestirmeOrganizasyon personelDenklestirmeOrganizasyon) {
		this.personelDenklestirmeOrganizasyon = personelDenklestirmeOrganizasyon;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_ALAN)
	@Fetch(FetchMode.JOIN)
	public Tanim getAlan() {
		return alan;
	}

	public void setAlan(Tanim value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isTanimDegisti(alan, value));
		this.alan = value;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = COLUMN_NAME_DEGER)
	@Fetch(FetchMode.JOIN)
	public Tanim getDeger() {
		return deger;
	}

	public void setDeger(Tanim value) {
		if (this.isDegisti() == false)
			this.setDegisti(PdksUtil.isTanimDegisti(deger, value));
		this.deger = value;
	}

	@Transient
	public static String getKey(PersonelDenklestirmeOrganizasyon organizasyon, Tanim tanimKey) {
		String str = (organizasyon != null && organizasyon.getId() != null ? organizasyon.getId() : "") + "_" + (tanimKey != null && tanimKey.getId() != null ? tanimKey.getId() : "");
		return str;
	}

	@Transient
	public String getKey() {
		String str = getKey(personelDenklestirmeOrganizasyon, alan);
		return str;
	}

	public void entityRefresh() {

	}

}
