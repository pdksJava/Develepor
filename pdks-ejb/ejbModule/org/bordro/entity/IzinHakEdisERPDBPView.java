package org.bordro.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;
import org.pdks.erp.entity.IzinHakEdisERPDB;

@Entity(name = IzinHakEdisERPDBPView.VIEW_NAME)
@Immutable
public class IzinHakEdisERPDBPView extends IzinHakEdisERPDB{

 
	/**
	 * 
	 */
	private static final long serialVersionUID = -6316938981806113968L;
	public static final String VIEW_NAME = "Z_NOT_USED_BORDRO_IZIN_HAKEDIS_ERP_DB";
 
}
