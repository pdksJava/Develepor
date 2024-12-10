package org.pdks.kgs.model;

import java.io.Serializable;

public class CihazUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5258110621889276049L;

	private String username;

	private String password;

	public CihazUser() {
		super();
	}

	public CihazUser(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
