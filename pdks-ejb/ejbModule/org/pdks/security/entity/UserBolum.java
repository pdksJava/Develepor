package org.pdks.security.entity;

import java.io.Serializable;

import org.pdks.entity.BasePDKSObject;
import org.pdks.entity.Tanim;

public class UserBolum extends BasePDKSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5406435567126166359L;

	private User user;

	private Tanim bolum;

	public UserBolum() {
		super();

	}

	public UserBolum(User user, Tanim bolum) {
		super();
		this.user = user;
		this.bolum = bolum;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Tanim getBolum() {
		return bolum;
	}

	public void setBolum(Tanim bolum) {
		this.bolum = bolum;
	}

	public void entityRefresh() {

	}
}
