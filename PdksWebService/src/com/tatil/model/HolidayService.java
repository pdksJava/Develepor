package com.tatil.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.pdks.genel.model.PdksUtil;

import com.ibm.icu.util.IslamicCalendar;
import com.ibm.icu.util.ULocale;

public class HolidayService implements Serializable {

	private static final long serialVersionUID = -4021629424879878878L;

	/**
	 * @param year
	 * @return
	 */
	public List<Holiday> calculateHolidays(int year) {
		List<Holiday> tatilList = new ArrayList<Holiday>();
		Date buYilBasi = PdksUtil.convertToJavaDate(year + "0101", "yyyyMMdd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(buYilBasi);
		cal.add(Calendar.DATE, -4);
		Date date = cal.getTime();
		cal.setTime(buYilBasi);
		cal.add(Calendar.YEAR, 1);
		cal.set(Calendar.DATE, 4);
		Date stopDate = cal.getTime();
		cal.setTime(date);
		ULocale locale = new ULocale("@calendar=islamic-umalqura");
		IslamicCalendar calIs = new IslamicCalendar(locale);

		Holiday holiday = null;
		while (date.after(stopDate) == false) {
			cal.setTime(date);
			calIs.setTime(date);
			int hijriMonth = calIs.get(IslamicCalendar.MONTH);
			int hijriDay = calIs.get(IslamicCalendar.DAY_OF_MONTH);
			if (hijriMonth == 9) {// 1. Ramazan Bayramı Hesaplama (Şevval Ayı 1, 2, 3. Günler)
				if (hijriDay == 1) {
					Date tarih = PdksUtil.tariheGunEkleCikar(date, -1);
					cal.setTime(tarih);
					if (cal.get(Calendar.YEAR) == year) {
						holiday = new Holiday("R", tarih, 3);
						tatilList.add(holiday);
					}

				} else if (hijriDay == 2) {
					// createHolidayMap(holidayMap, "RB2", date);
				} else if (hijriDay == 3) {
					if (holiday != null)
						holiday.setBitTarih(date);
					// createHolidayMap(holidayMap, "RB3", date);
				}
			}

			if (hijriMonth == 11) {// 2. Kurban Bayramı Hesaplama (Zilhicce Ayı 9, 10, 11, 12, 13. Günler)
				if (hijriDay == 9) {
					Date tarih = date;
					cal.setTime(tarih);
					if (cal.get(Calendar.YEAR) == year) {
						holiday = new Holiday("K", tarih, 4);
						tatilList.add(holiday);
					}
					// createHolidayMap(holidayMap, "KB0", date);
				} else if (hijriDay == 10) {
					// createHolidayMap(holidayMap, "KB1", date);
				} else if (hijriDay == 11) {
					// createHolidayMap(holidayMap, "KB2", date);
				} else if (hijriDay == 12) {
					// createHolidayMap(holidayMap, "KB3", date);
				} else if (hijriDay == 13) {
					if (holiday != null)
						holiday.setBitTarih(date);
					// createHolidayMap(holidayMap, "KB4", date);
				}
			}

			date = PdksUtil.tariheGunEkleCikar(date, 1);
		}
		if (holiday != null && holiday.getBitGun() == null) {
			cal.setTime(holiday.getBasTarih());
			cal.add(Calendar.DATE, holiday.getGunAdet());
			holiday.setBitTarih(cal.getTime());
		}
		return tatilList;
	}

}
