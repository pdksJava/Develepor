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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.enums.PersonelTipi;

@Entity(name = TesisBaglanti.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { TesisBaglanti.COLUMN_NAME_TESIS, TesisBaglanti.COLUMN_NAME_TESIS_BAGLANTI }) })
public class TesisBaglanti extends BasePDKSObject implements Serializable {

	 
	/**
	 * 
	 */
	private static final long serialVersionUID = -6317664443922417141L;
	public static final String TABLE_NAME = "TESIS_BAGLANTI";
	public static final String COLUMN_NAME_TESIS = "TESIS_ID";
	public static final String COLUMN_NAME_TESIS_BAGLANTI = "TESIS_BAGLANTI_ID";
	public static final String COLUMN_NAME_PERSONEL_TIPI = "PERSONEL_TIPI";

	private Tanim tesis, tesisBaglanti;

	private PersonelTipi personelTipi;

	private Integer tipi;

	public TesisBaglanti() {
		super();

	}

	public TesisBaglanti(Tanim tesis, Tanim tesisBaglanti) {
		super();
		this.tesis = tesis;
		this.tesisBaglanti = tesisBaglanti;
		 
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_TESIS, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getTesis() {
		return tesis;
	}

	public void setTesis(Tanim tesis) {
		this.tesis = tesis;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_TESIS_BAGLANTI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getTesisBaglanti() {
		return tesisBaglanti;
	}

	public void setTesisBaglanti(Tanim tesisBaglanti) {
		this.tesisBaglanti = tesisBaglanti;
	}

	@Column(name = COLUMN_NAME_PERSONEL_TIPI)
	public Integer getTipi() {
		return tipi;
	}

	public void setTipi(Integer value) {
		this.personelTipi = value != null ? PersonelTipi.fromValue(value) : null;
		 
		this.tipi = value;
	}

	@Transient
	public PersonelTipi getPersonelTipi() {
		return personelTipi;
	}

	public void setPersonelTipi(PersonelTipi personelTipi) {
		this.personelTipi = personelTipi;
	}

	@Transient
	public String getPersonelTipiAciklama() {
		String personelTipiAciklama = "";
		return personelTipiAciklama;
	}

	@Transient
	public static String getPersonelTipiAciklama(PersonelTipi personelTipi) {
		String personelTipiAciklama = "";
		if (personelTipi != null) {
			if (personelTipi.equals(PersonelTipi.TUM))
				personelTipiAciklama = "IK ve Supervisor";
			else if (personelTipi.equals(PersonelTipi.IK))
				personelTipiAciklama = "IK";
			else if (personelTipi.equals(PersonelTipi.SUPERVISOR))
				personelTipiAciklama = "Supervisor";
		}
		return personelTipiAciklama;
	}

	@Transient
	public boolean isIK() {
		boolean personelTipiDurum = personelTipi != null && (personelTipi.equals(PersonelTipi.TUM) || personelTipi.equals(PersonelTipi.IK));
		return personelTipiDurum;
	}

	@Transient
	public boolean isSupervisor() {
		boolean personelTipiDurum = personelTipi != null && (personelTipi.equals(PersonelTipi.TUM) || personelTipi.equals(PersonelTipi.SUPERVISOR));
		return personelTipiDurum;
	}

	public void entityRefresh() {

	}

}
