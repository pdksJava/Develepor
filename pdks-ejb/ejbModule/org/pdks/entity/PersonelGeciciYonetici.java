package org.pdks.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.security.entity.User;

@Entity(name = PersonelGeciciYonetici.TABLE_NAME)
public class PersonelGeciciYonetici extends YetkiBaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6118941829992674855L;
	
	public static final String TABLE_NAME = "PERSONEL_GECICI_YONETICI";
	public static final String COLUMN_NAME_VEKALET_VEREN = "VEKALET_VEREN";
	public static final String COLUMN_NAME_PERSONEL_GECICI = "PERSONEL_GECICI";

	private Personel personelGecici;

	private User bagliYonetici;

	private boolean calendarPopup = Boolean.TRUE;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_VEKALET_VEREN, nullable = false)
	@Fetch(FetchMode.JOIN)
	public User getBagliYonetici() {
		return bagliYonetici;
	}

	public void setBagliYonetici(User bagliYonetici) {
		this.bagliYonetici = bagliYonetici;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_PERSONEL_GECICI, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Personel getPersonelGecici() {
		return personelGecici;
	}

	public void setPersonelGecici(Personel personelGecici) {
		this.personelGecici = personelGecici;
	}

	@Column(name = "CALENDARPOPUP")
	public boolean isCalendarPopup() {
		return calendarPopup;
	}

	public void setCalendarPopup(boolean calendarPopup) {
		this.calendarPopup = calendarPopup;
	}

	@Transient
	public Long getPersonelGeciciId() {
		return personelGecici != null ? personelGecici.getId() : 0;
	}

	@Transient
	public Personel getPdksPersonel() {
		return personelGecici;
	}
}
