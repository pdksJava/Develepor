package org.pdks.entity;

import javax.persistence.Transient;

/**
 * Data Access Object (DAO) interface. This is an empty interface used to tag our DAO classes. Common methods for each interface could be added here.
 * 
 * @author Hasan Sayar
 */
public interface PdksInterface {

	public void entityRefresh();

	@Transient
	public String getTableName();

	public Long getId();

}
