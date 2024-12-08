/**
 * 
 */
package org.pdks.kgs.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Durum")
@XmlEnum
public enum Durum {
	AKTIF(1), PASIF(0);

	private final Integer value;

	Durum(Integer v) {
		value = v;
	}

	public Integer value() {
		return value;
	}

	public static Durum fromValue(Integer v) {
		for (Durum c : Durum.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		return null;
		 
	}

}
