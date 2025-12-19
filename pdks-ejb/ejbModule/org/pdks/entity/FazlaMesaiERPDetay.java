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
import org.pdks.enums.MethodAlanAPI;

@Entity(name = FazlaMesaiERPDetay.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { FazlaMesaiERPDetay.COLUMN_NAME_FAZLA_MESAI_ERP, FazlaMesaiERPDetay.COLUMN_NAME_ALAN_TIPI }) })
public class FazlaMesaiERPDetay extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 929729411027043798L;
	static Logger logger = Logger.getLogger(FazlaMesaiERPDetay.class);
	public static final String TABLE_NAME = "FAZLA_MESAI_ERP_DETAY";
	public static final String COLUMN_NAME_FAZLA_MESAI_ERP = "FAZLA_MESAI_ERP";
	public static final String COLUMN_NAME_SIRA = "SIRA";
	public static final String COLUMN_NAME_ALAN_TIPI = "ALAN_TIPI";
	public static final String COLUMN_NAME_ALAN_ADI = "ALAN_ADI";

	private FazlaMesaiERP fazlaMesaiERP;

	private Integer sira;

	private String alanTipi, alanAdi;

	private MethodAlanAPI methodAlanAPI;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = COLUMN_NAME_FAZLA_MESAI_ERP, nullable = false)
	@Fetch(FetchMode.JOIN)
	public FazlaMesaiERP getFazlaMesaiERP() {
		return fazlaMesaiERP;
	}

	public void setFazlaMesaiERP(FazlaMesaiERP fazlaMesaiERP) {
		this.fazlaMesaiERP = fazlaMesaiERP;
	}

	@Column(name = COLUMN_NAME_SIRA)
	public Integer getSira() {
		return sira;
	}

	public void setSira(Integer sira) {
		this.sira = sira;
	}

	@Column(name = COLUMN_NAME_ALAN_TIPI, nullable = false)
	public String getAlanTipi() {
		return alanTipi;
	}

	public void setAlanTipi(String value) {
		this.methodAlanAPI = value != null ? MethodAlanAPI.fromValue(value) : null;
		this.alanTipi = value;
	}

	@Column(name = COLUMN_NAME_ALAN_ADI, nullable = false)
	public String getAlanAdi() {
		return alanAdi;
	}

	public void setAlanAdi(String alanAdi) {
		this.alanAdi = alanAdi;
	}

	@Transient
	public static String getAlanAciklama(String alan) {
		String str = "";
		if (alan != null) {
			MethodAlanAPI alanAPI = MethodAlanAPI.fromValue(alan);
			if (alanAPI != null) {
				if (alanAPI.equals(MethodAlanAPI.KIMLIK))
					str = "Kimlik No";
				else if (alanAPI.equals(MethodAlanAPI.MASRAF_YERI))
					str = "Masraf Yeri No";
				else if (alanAPI.equals(MethodAlanAPI.SIRKET))
					str = "Şirket No";
				else if (alanAPI.equals(MethodAlanAPI.TESIS))
					str = "Tesis No";
			}
		}
		return str;
	}

	@Transient
	public String getAlanAciklama() {
		String str = FazlaMesaiERPDetay.getAlanAciklama(alanAdi);
		return str;
	}

	@Transient
	public MethodAlanAPI getMethodAlanAPI() {
		return methodAlanAPI;
	}

	public void setMethodAlanAPI(MethodAlanAPI methodAlanAPI) {
		this.methodAlanAPI = methodAlanAPI;
	}

	public void entityRefresh() {

	}

}
