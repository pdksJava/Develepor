package org.pdks.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "PERSONEL_DONEMSEL_DURUM")
public class PersonelDonemselDurum extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4547549693648759736L;

	private Personel personel;

	private Date basTarih, bitTarih;
	private PersonelDurumTipi personelDurumTipi;
	private Integer personelDurumTipiId;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "PERSONEL_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "BAS_TARIH")
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "BIT_TARIH")
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	@Column(name = "DURUM_TIPI")
	public PersonelDurumTipi getPersonelDurumTipi() {
		return personelDurumTipi;
	}

	public void setPersonelDurumTipi(PersonelDurumTipi personelDurumTipi) {
		this.personelDurumTipiId = personelDurumTipi != null ? personelDurumTipi.value() : null;
		this.personelDurumTipi = personelDurumTipi;
	}

	@Transient
	public Integer getPersonelDurumTipiId() {
		return personelDurumTipiId;
	}

	public void setPersonelDurumTipiId(Integer personelDurumTipiId) {
		this.personelDurumTipi = personelDurumTipiId != null ? PersonelDurumTipi.fromValue(personelDurumTipiId) : null;
		this.personelDurumTipiId = personelDurumTipiId;
	}

	@Transient
	public String getPersonelDurumTipiAciklama() {
		return PersonelDonemselDurum.getPersonelDurumTipiAciklama(personelDurumTipi);
	}

	@Transient
	public static String getPersonelDurumTipiAciklama(PersonelDurumTipi tipi) {
		String aciklama = "";
		if (tipi != null) {
			Integer value = tipi.value();
			if (value.equals(PersonelDurumTipi.GEBE.value()))
				aciklama = "Gebe";
			else if (value.equals(PersonelDurumTipi.SUT_IZNI.value()))
				aciklama = "Süt İzni";
		}
		return aciklama;
	}
}
