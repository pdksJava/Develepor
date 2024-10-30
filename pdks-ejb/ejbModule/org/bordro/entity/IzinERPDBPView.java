package org.bordro.entity;

import java.io.Serializable;

import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.pdks.erp.entity.IzinERPDB;

@Entity(name = IzinERPDBPView.VIEW_NAME)
@Immutable
public class IzinERPDBPView extends IzinERPDB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4123083357201037607L;

	public static final String VIEW_NAME = "Z_NOT_USED_BORDRO_IZIN_ERP_DB";

	static Logger logger = Logger.getLogger(IzinERPDBPView.class);

}
