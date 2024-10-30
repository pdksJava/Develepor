package org.bordro.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;
import org.pdks.erp.entity.PersonelERPDB;

@Entity(name = PersonelERPDBPView.VIEW_NAME)
@Immutable
public class PersonelERPDBPView extends PersonelERPDB {

 

	/**
	 * 
	 */
	private static final long serialVersionUID = 2789903705843089447L;
	public static final String VIEW_NAME = "Z_NOT_USED_BORDRO_PERSONEL_ERP_DB";
 

}
