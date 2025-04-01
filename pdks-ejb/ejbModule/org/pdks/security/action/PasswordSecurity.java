package org.pdks.security.action;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

@Name("passwordSecurity")
@Scope(SESSION)
@Install(precedence = BUILT_IN)
@BypassInterceptors
public class PasswordSecurity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2253368202248387124L;

	private boolean forgot = false;

	public boolean isForgot() {
		return forgot;
	}

	public void setForgot(boolean forgot) {
		this.forgot = forgot;
	}

	public CallbackHandler createCallbackHandler() {
		return new CallbackHandler() {
			public void handle(Callback[] callbacks) {

			}
		};
	}
}
