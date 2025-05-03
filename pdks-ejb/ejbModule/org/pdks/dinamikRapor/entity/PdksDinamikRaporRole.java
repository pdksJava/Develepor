package org.pdks.dinamikRapor.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.pdks.entity.BasePDKSObject;
import org.pdks.security.entity.Role;

/**
 * @author hasansayar
 * 
 */
@Entity(name = PdksDinamikRaporRole.TABLE_NAME)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { PdksDinamikRaporRole.COLUMN_NAME_DINAMIK_RAPOR, PdksDinamikRaporRole.COLUMN_NAME_ROLE }) })
public class PdksDinamikRaporRole extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7138748795297622046L;

	public static final String TABLE_NAME = "PDKS_DINAMIK_RAPOR_ROLE";

	public static final String COLUMN_NAME_DINAMIK_RAPOR = "DINAMIK_RAPOR_ID";
	public static final String COLUMN_NAME_ROLE = "ROLE_ID";

	private PdksDinamikRapor pdksDinamikRapor;

	private Role role;

	public PdksDinamikRaporRole(PdksDinamikRapor pdksDinamikRapor, Role role) {
		super();
		this.pdksDinamikRapor = pdksDinamikRapor;
		this.role = role;
	}

	public PdksDinamikRaporRole() {
		super();
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_DINAMIK_RAPOR, nullable = false)
	@Fetch(FetchMode.JOIN)
	public PdksDinamikRapor getPdksDinamikRapor() {
		return pdksDinamikRapor;
	}

	public void setPdksDinamikRapor(PdksDinamikRapor pdksDinamikRapor) {
		this.pdksDinamikRapor = pdksDinamikRapor;
	}

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COLUMN_NAME_ROLE, nullable = false)
	@Fetch(FetchMode.JOIN)
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Transient
	public String getKey() {
		String str = getKey(pdksDinamikRapor, role);
		return str;
	}

	@Transient
	public String getRoleAdi() {
		String str = role != null && role.getAciklama() != null ? role.getAciklama().trim() : "";
		return str;
	}

	@Transient
	public static String getKey(PdksDinamikRapor rapor, Role role) {
		String str = (rapor != null ? rapor.getId() : 0L) + "_" + (role != null ? role.getRolename() : "");
		return str;
	}

	public void entityRefresh() {

	}

	@Transient
	public Object clone() {
		BasePDKSObject object = null;
		try {
			object = (BasePDKSObject) super.clone();
			object.setId(null);
		} catch (CloneNotSupportedException e) {

		}
		return object;
	}

}
