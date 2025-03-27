package org.pdks.security.entity;

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
import org.pdks.entity.BasePDKSObject;
import org.pdks.entity.Tanim;
import org.pdks.enums.OrganizasyonTipi;

@Entity(name = UserDigerOrganizasyon.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { UserDigerOrganizasyon.COLUMN_NAME_USER, UserDigerOrganizasyon.COLUMN_NAME_TIPI, UserDigerOrganizasyon.COLUMN_NAME_ORGANIZASYON }) })
public class UserDigerOrganizasyon extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5565458340913573315L;
	public static final String FN_NAME = "FN_GET_USER_ORGANIZASYON";
	public static final String TABLE_NAME = "USER_DIGER_ORGANIZASYON";

	public static final String COLUMN_NAME_USER = "USER_ID";
	public static final String COLUMN_NAME_TIPI = "TIPI";
	public static final String COLUMN_NAME_ORGANIZASYON = "ORGANIZASYON_ID";

	private User user;

	public UserDigerOrganizasyon() {
		super();

	}

	public UserDigerOrganizasyon(User user, OrganizasyonTipi organizasyonTipi, Tanim organizasyon) {
		super();
		this.user = user;
		this.organizasyonTipi = organizasyonTipi;
		this.tipi = organizasyonTipi != null ? organizasyonTipi.value() : null;
		this.organizasyon = organizasyon;
	}

	private OrganizasyonTipi organizasyonTipi;

	private Integer tipi;

	private Tanim organizasyon;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_USER, nullable = false)
	@Fetch(FetchMode.JOIN)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_ORGANIZASYON, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Tanim getOrganizasyon() {
		return organizasyon;
	}

	public void setOrganizasyon(Tanim organizasyon) {
		this.organizasyon = organizasyon;
	}

	@Transient
	public OrganizasyonTipi getOrganizasyonTipi() {
		if (organizasyonTipi == null && tipi != null)
			this.organizasyonTipi = OrganizasyonTipi.fromValue(tipi);
		return organizasyonTipi;
	}

	public void setOrganizasyonTipi(OrganizasyonTipi organizasyonTipi) {
		this.organizasyonTipi = organizasyonTipi;
	}

	@Column(name = COLUMN_NAME_TIPI)
	public Integer getTipi() {
		return tipi;
	}

	public void setTipi(Integer tipi) {
		if (tipi != null)
			this.organizasyonTipi = OrganizasyonTipi.fromValue(tipi);
		this.tipi = tipi;
	}

	@Transient
	public static String getKey(User user, Integer tipi, Tanim organizasyon) {
		String str = (user != null ? user.getId() : 0) + "_" + (tipi != null ? tipi : 0) + "_" + (organizasyon != null ? organizasyon.getId() : 0);
		return str;
	}

	@Transient
	public String getKey() {
		return getKey(user, tipi, organizasyon);
	}

	@Transient
	public boolean isTesis() {
		return tipi != null && tipi.equals(OrganizasyonTipi.TESIS.value());
	}

	@Transient
	public boolean isBolum() {
		return tipi != null && tipi.equals(OrganizasyonTipi.BOLUM.value());
	}

	public void entityRefresh() {

	}

}
