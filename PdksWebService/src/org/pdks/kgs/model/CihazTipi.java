/**
 * 
 */
package org.pdks.kgs.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CihazTipi")
@XmlEnum
public enum CihazTipi {
	GIRIS(1), CIKIS(2);

	private final Integer value;

	CihazTipi(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static CihazTipi fromValue(Integer v) {
		CihazTipi ct = null;
		for (CihazTipi c : CihazTipi.values()) {
			if (c.value.equals(v)) {
				ct = c;
			}
		}
		return ct;

	}

}
