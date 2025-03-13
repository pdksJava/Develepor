package org.pdks.security.action;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("pdksCredentials")
@Scope(ScopeType.PAGE)
public class PdksCredentials implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7550462984060785773L;

	private boolean forgetPassword;

	public boolean isForgetPassword() {
		return forgetPassword;
	}

	public void setForgetPassword(boolean forgetPassword) {
		this.forgetPassword = forgetPassword;
	}

}
