package org.kgs.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity(name = MySQLTerminal.TABLE_NAME)
public class MySQLTerminal implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6541019309141599811L;

	public static final String JSON_KODU = "subeKodu";
	public static final String JSON_ADI = "sube";
	public static final String JSON_TIPI = "islemTipi";

	public static final String TABLE_NAME = "MYSQL_TERMINAL";
	public static final String NAME = "terminal_Seq";
	public static final String SEQUENCE_NAME = "SEQ_MYSQL_TERMINAL";
	public static final String COLUMN_NAME_ID = "TerminalId";
	public static final String COLUMN_NAME_YON = "Yon";
	public static final String COLUMN_NAME_ACIKLAMA = "Aciklama";
	public static final String COLUMN_NAME_KODU = "Kodu";
	public static final String COLUMN_NAME_DURUM = "Aktif";
	private Long id;
	private Integer yonDurum;
	private String aciklama, kodu;
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

	@Column(name = COLUMN_NAME_YON)
	public Integer getYonDurum() {
		return yonDurum;
	}

	public void setYonDurum(Integer value) {
		this.hareketYon = value != null ? ENumHareketYon.fromValue(value) : null;
		this.yonDurum = value;
	}

	@Column(name = COLUMN_NAME_ACIKLAMA)
	public String getAciklama() {
		return aciklama;
	}

	public void setAciklama(String aciklama) {
		this.aciklama = aciklama;
	}

	@Column(name = COLUMN_NAME_KODU)
	public String getKodu() {
		return kodu;
	}

	public void setKodu(String kodu) {
		this.kodu = kodu;
	}

	@Column(name = COLUMN_NAME_DURUM)
	public Boolean getDurum() {
		return durum;
	}

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
