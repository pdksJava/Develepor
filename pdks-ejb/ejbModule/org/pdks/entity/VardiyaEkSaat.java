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
	public static final String COLUMN_NAME_UCM_FAZLA_CALISMA_SURE = "UCM_FAZLA_CALISMA_SURE";
	public static final String COLUMN_NAME_ICAP_FAZLA_CALISMA_SURE = "ICAP_FAZLA_CALISMA_SURE";

	private double resmiTatilSure = 0d, aksamVardiyaSaatSayisi = 0d, ucretiOdenenFazlaMesaiSaat = 0d;
	private Double resmiTatilKanunenEklenenSure = 0d, icapciMesaiSaat = 0d;

	public VardiyaEkSaat() {
		super();
		this.guncellendi = false;
	}

	public VardiyaEkSaat(double resmiTatilSureInput, double aksamVardiyaSaatSayisiInput, Double resmiTatilKanunenEklenenSureInput, Double ucretiOdenenFazlaMesaiSaatInput, Double icapciMesaiSaatInput) {
		super();
		this.guncelle(resmiTatilSureInput, aksamVardiyaSaatSayisiInput, resmiTatilKanunenEklenenSureInput, ucretiOdenenFazlaMesaiSaatInput, icapciMesaiSaatInput);
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

	@Column(name = COLUMN_NAME_UCM_FAZLA_CALISMA_SURE)
	public double getUcretiOdenenFazlaMesaiSaat() {
		return ucretiOdenenFazlaMesaiSaat;
	}

	public void setUcretiOdenenFazlaMesaiSaat(double ucretiOdenenFazlaMesaiSaat) {
		this.ucretiOdenenFazlaMesaiSaat = ucretiOdenenFazlaMesaiSaat;
	}

	@Column(name = COLUMN_NAME_ICAP_FAZLA_CALISMA_SURE)
	public Double getIcapciMesaiSaat() {
		return icapciMesaiSaat;
	}

	public void setIcapciMesaiSaat(Double icapciMesaiSaat) {
		this.icapciMesaiSaat = icapciMesaiSaat;
	}

	/**
	 * @param resmiTatilSureInput
	 * @param aksamVardiyaSaatSayisiInput
	 * @param resmiTatilKanunenEklenenSureInput
	 * @param ucretiOdenenFazlaMesaiSaat
	 */
	public void guncelle(double resmiTatilSureInput, double aksamVardiyaSaatSayisiInput, Double resmiTatilKanunenEklenenSureInput, Double ucretiOdenenFazlaMesaiSaat, Double icapciMesaiSaat) {
		this.guncellendi = id == null;
		this.setResmiTatilSure(resmiTatilSureInput);
		this.setAksamVardiyaSaatSayisi(aksamVardiyaSaatSayisiInput);
		this.setResmiTatilKanunenEklenenSure(resmiTatilKanunenEklenenSureInput != null ? resmiTatilKanunenEklenenSureInput : 0.0d);
		this.setUcretiOdenenFazlaMesaiSaat(ucretiOdenenFazlaMesaiSaat);
		this.setIcapciMesaiSaat(icapciMesaiSaat);
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

	@Transient
	public String getTableName() {
		return TABLE_NAME;
	}

}
