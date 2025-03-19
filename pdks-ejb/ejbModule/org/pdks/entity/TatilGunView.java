package org.pdks.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;

@Entity(name = TatilGunView.VIEW_NAME)
@Immutable
public class TatilGunView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5242917748178813052L;
	protected static final String VIEW_NAME = "Z_NOT_USED_TATIL_GUN";
	public static final String SP_NAME = "SP_GET_TATIL_GUNLERI";
	

	private Long id;

	private Tatil tatil;
	private Date tarih;
	private Boolean yarimGun;

	@Id
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@OneToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "TATIL_ID", nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tatil getTatil() {
		return tatil;
	}

	public void setTatil(Tatil tatil) {
		this.tatil = tatil;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "TARIH", nullable = false)
	public Date getTarih() {
		return tarih;
	}

	public void setTarih(Date tarih) {
		this.tarih = tarih;
	}

	@Column(name = "YARIM_GUN")
	public Boolean getYarimGun() {
		return yarimGun;
	}

	public void setYarimGun(Boolean yarimGun) {
		this.yarimGun = yarimGun;
	}

	@Transient
	public String getAciklama() {
		String aciklama = tatil != null ? tatil.getAd() : "";

		return aciklama;
	}

}
