package com.pdks.webService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

public abstract class BaseERPObject implements Serializable, Cloneable {

	private Long id;

	private Boolean yazildi = Boolean.FALSE;
	
	private String guncellemeZamani;

	private List<String> hataList;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3146798959145433206L;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getYazildi() {
		return yazildi;
	}

	public void setYazildi(Boolean yazildi) {
		this.yazildi = yazildi;
	}

	public List<String> getHataList() {
		if (hataList == null)
			hataList = new ArrayList<String>();
		return hataList;
	}

	public void setHataList(List<String> hataList) {
		this.hataList = hataList;
	}

	public void veriSifirla() {
		this.id = null;
		this.yazildi = null;
		this.hataList = null;
	}

	@Transient
	public Object clone() {
		try {

			return super.clone();
		} catch (CloneNotSupportedException e) {
			// bu class cloneable oldugu icin buraya girilmemeli...
			throw new InternalError();
		}
	}

	public String getGuncellemeZamani() {
		return guncellemeZamani;
	}

	public void setGuncellemeZamani(String guncellemeZamani) {
		this.guncellemeZamani = guncellemeZamani;
	}
}
