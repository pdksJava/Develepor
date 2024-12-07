/**
 * 
 */
package org.pdks.kgs.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CihazTipi")
@XmlEnum
public enum CihazTipi {
	@XmlEnumValue(value = "1")	GIRIS("1"),
	@XmlEnumValue(value = "2") 	CIKIS("2");


	private final String value;

	CihazTipi(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static CihazTipi fromValue(String v) {
		for (CihazTipi c : CihazTipi.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException( v);
	}

}
