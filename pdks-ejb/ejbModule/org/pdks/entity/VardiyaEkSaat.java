package org.pdks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.pdks.session.PdksUtil;

@Entity(name = VardiyaEkSaat.TABLE_NAME)
public class VardiyaEkSaat extends BasePDKSObject implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4074174169906378370L;

	static Logger logger = Logger.getLogger(VardiyaEkSaat.class);

	public static final String TABLE_NAME = "VARDIYA_EK_SAAT";
	public static final String COLUMN_NAME_RESMI_TATIL_SURESI = "RESMI_TATIL_SURESI";
	public static final String COLUMN_NAME_AKSAM_VARDIYA = "AKSAM_VARDIYA_SURESI";
	public static final String COLUMN_NAME_RT_KANUNI_EKLENEN_SURE = "RT_KANUNI_EKLENEN_SURE";

	private double resmiTatilSure = 0d, aksamVardiyaSaatSayisi = 0d;
	private Double resmiTatilKanunenEklenenSure = 0d;

	public VardiyaEkSaat() {
		super();
		this.guncellendi = false;
	}

	public VardiyaEkSaat(double resmiTatilSureInput, double aksamVardiyaSaatSayisiInput, Double resmiTatilKanunenEklenenSureInput) {
		super();
		this.guncelle(resmiTatilSureInput, aksamVardiyaSaatSayisiInput, resmiTatilKanunenEklenenSureInput);

	}

	@Column(name = COLUMN_NAME_RESMI_TATIL_SURESI)
	public double getResmiTatilSure() {
		return resmiTatilSure;
	}

	public void setResmiTatilSure(double value) {
		if (!guncellendi)
			guncellendi = value != resmiTatilSure;
		this.resmiTatilSure = value;
	}

	@Column(name = COLUMN_NAME_RT_KANUNI_EKLENEN_SURE)
	public Double getResmiTatilKanunenEklenenSure() {
		return resmiTatilKanunenEklenenSure;
	}

	public void setResmiTatilKanunenEklenenSure(Double value) {
		if (!guncellendi)
			guncellendi = PdksUtil.isDoubleDegisti(value, resmiTatilKanunenEklenenSure);
		this.resmiTatilKanunenEklenenSure = value;
	}

	@Column(name = COLUMN_NAME_AKSAM_VARDIYA)
	public double getAksamVardiyaSaatSayisi() {
		return aksamVardiyaSaatSayisi;
	}

	public void setAksamVardiyaSaatSayisi(double value) {
		if (!guncellendi)
			guncellendi = value != aksamVardiyaSaatSayisi;
		this.aksamVardiyaSaatSayisi = value;
	}

	/**
	 * @param resmiTatilSureInput
	 * @param aksamVardiyaSaatSayisiInput
	 * @param resmiTatilKanunenEklenenSureInput
	 */
	public void guncelle(double resmiTatilSureInput, double aksamVardiyaSaatSayisiInput, Double resmiTatilKanunenEklenenSureInput) {
		this.guncellendi = id == null;
		this.setResmiTatilSure(resmiTatilSureInput);
		this.setAksamVardiyaSaatSayisi(aksamVardiyaSaatSayisiInput);
		this.setResmiTatilKanunenEklenenSure(resmiTatilKanunenEklenenSureInput != null ? resmiTatilKanunenEklenenSureInput : 0.0d);
	}

	@Transient
	public Object clone() {
		try {

			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

	public void entityRefresh() {

	}

}
