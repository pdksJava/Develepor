package org.pdks.security.action;

import javax.ejb.Local;

@Local
public interface IAuthenticator {

	boolean authenticate();

}