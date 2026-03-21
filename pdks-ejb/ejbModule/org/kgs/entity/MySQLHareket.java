package org.kgs.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = MySQLHareket.TABLE_NAME)
public class MySQLHareket implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3413299234360760587L;
	public static final String TABLE_NAME = "MYSQL_HAREKET";
	public static final String COLUMN_NAME_ID = "TransactionId";
	public static final String COLUMN_NAME_YON = "Yon";
	public static final String COLUMN_NAME_TARIH = "Tarih";
	public static final String COLUMN_NAME_PERSONEL = "PersonelId";
	public static final String COLUMN_NAME_TERMINAL = "TerminalId";
	public static final String COLUMN_NAME_DURUM = "Aktif";
	private Long id;
	private Integer yonDurum;
	private Date tarih;
	private MySQLPersonel personel;
	private MySQLTerminal terminal;
	private Boolean durum;
	private ENumHareketYon hareketYon;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = COLUMN_NAME_ID)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = COLUMN_NAME_TARIH)
	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public MySQLPersonel getPersonel() {
		return personel;
	}

	public void setPersonel(MySQLPersonel personel) {
		this.personel = personel;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_TERMINAL, nullable = false)
	@Fetch(FetchMode.JOIN)
	public MySQLTerminal getTerminal() {
		return terminal;
	}

	public void setTerminal(MySQLTerminal terminal) {
		this.terminal = terminal;
	}

	@Column(name = COLUMN_NAME_YON)
	public Integer getYonDurum() {
		return yonDurum;
	}

	public void setYonDurum(Integer value) {
		this.hareketYon = value != null ? ENumHareketYon.fromValue(value) : null;
		this.yonDurum = value;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public void setDurum(Boolean durum) {
		this.durum = durum;
	}

	@Transient
	public ENumHareketYon getHareketYon() {
		return hareketYon;
	}

	public void setHareketYon(ENumHareketYon hareketYon) {
		this.hareketYon = hareketYon;
	}

}
