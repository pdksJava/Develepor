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

@Entity(name = PersonelDonemselDurum.TABLE_NAME)
public class PersonelDonemselDurum extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4547549693648759736L;
	public static final String TABLE_NAME = "PERSONEL_DONEMSEL_DURUM";
	public static final String COLUMN_NAME_PERSONEL = "PERSONEL_ID";
	public static final String COLUMN_NAME_BASLANGIC_ZAMANI = "BAS_TARIH";
	public static final String COLUMN_NAME_BITIS_ZAMANI = "BIT_TARIH";
	public static final String COLUMN_NAME_DURUM_TIPI = "DURUM_TIPI";

	private Personel personel;

	private Date basTarih, bitTarih;
	private PersonelDurumTipi personelDurumTipi;
	private Integer personelDurumTipiId;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonel() {
		return personel;
	}

	public void setPersonel(Personel personel) {
		this.personel = personel;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BASLANGIC_ZAMANI)
	public Date getBasTarih() {
		return basTarih;
	}

	public void setBasTarih(Date basTarih) {
		this.basTarih = basTarih;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = COLUMN_NAME_BITIS_ZAMANI)
	public Date getBitTarih() {
		return bitTarih;
	}

	public void setBitTarih(Date bitTarih) {
		this.bitTarih = bitTarih;
	}

	@Column(name = COLUMN_NAME_DURUM_TIPI, nullable = false)
	public Integer getPersonelDurumTipiId() {
		return personelDurumTipiId;
	}

	public void setPersonelDurumTipiId(Integer value) {
		this.personelDurumTipi = value != null ? PersonelDurumTipi.fromValue(value) : null;
		this.personelDurumTipiId = value;
	}

	@Transient
	public PersonelDurumTipi getPersonelDurumTipi() {
		return personelDurumTipi;
	}

	public void setPersonelDurumTipi(PersonelDurumTipi personelDurumTipi) {
		this.personelDurumTipi = personelDurumTipi;
	}

	@Transient
	public String getPersonelDurumTipiAciklama() {
		return PersonelDonemselDurum.getPersonelDurumTipiAciklama(personelDurumTipiId);
	}

	@Transient
	public boolean isGebe() {
		return personelDurumTipiId != null && PersonelDurumTipi.GEBE.value().equals(personelDurumTipiId);
	}

	@Transient
	public boolean isSutIzni() {
		return personelDurumTipiId != null && PersonelDurumTipi.SUT_IZNI.value().equals(personelDurumTipiId);
	}

	@Transient
	public static String getPersonelDurumTipiAciklama(Integer value) {
		String aciklama = "";
		if (value != null) {
			if (value.equals(PersonelDurumTipi.GEBE.value()))
				aciklama = "Gebe";
			else if (value.equals(PersonelDurumTipi.SUT_IZNI.value()))
				aciklama = "Süt İzni";
		}
		return aciklama;
	}

	public void entityRefresh() {
		// TODO entityRefresh

	}
}
