package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = TesisBaglanti.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { TesisBaglanti.COLUMN_NAME_TESIS, TesisBaglanti.COLUMN_NAME_TESIS_BAGLANTI }) })
public class TesisBaglanti extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3903397099513071228L;
	public static final String TABLE_NAME = "TESIS_BAGLANTI";
	public static final String COLUMN_NAME_TESIS = "TESIS_ID";
	public static final String COLUMN_NAME_TESIS_BAGLANTI = "TESIS_BAGLANTI_ID";

	private Tanim tesis, tesisBaglanti;

	public TesisBaglanti() {
		super();

	}

	public TesisBaglanti(Tanim tesis, Tanim tesisBaglanti) {
		super();
		this.tesis = tesis;
		this.tesisBaglanti = tesisBaglanti;
		this.setCheckBoxDurum(false);
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

	public void entityRefresh() {

	}

}
