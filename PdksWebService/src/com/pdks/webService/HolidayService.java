package com.pdks.webService;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.pdks.genel.model.PdksUtil;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.chrono.HijrahDate;
import org.threeten.bp.temporal.ChronoField;

public class HolidayService {

	public Map<String, String> calculateHolidays(int year) {
		Map<String, String> holidayMap = new LinkedHashMap<String, String>();

		// Java 7 uyumlu ThreeTenBP LocalDate kullanımı
		LocalDate startDate = LocalDate.of(year, 1, 1);
		LocalDate endDate = LocalDate.of(year, 12, 31);

		// LocalDate.isAfter ve plusDays döngüsü
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

			// Miladi tarihi ÜçTen Hicri takvimine çeviriyoruz
			HijrahDate hijrahDate = HijrahDate.from(date);

			int hijriMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR);
			int hijriDay = hijrahDate.get(ChronoField.DAY_OF_MONTH);

			if (hijriMonth == 10) {// 1. Ramazan Bayramı Hesaplama (Şevval Ayı 1, 2, 3. Günler)
				if (hijriDay == 1) {
					createHolidayMap(holidayMap, "RB0", date.plusDays(-1));
					createHolidayMap(holidayMap, "RB1", date);
				} else if (hijriDay == 2) {
					createHolidayMap(holidayMap, "RB2", date);
				} else if (hijriDay == 3) {
					createHolidayMap(holidayMap, "RB3", date);
				}
			}

			if (hijriMonth == 12) {// 2. Kurban Bayramı Hesaplama (Zilhicce Ayı 9, 10, 11, 12, 13. Günler)
				if (hijriDay == 9) {
					createHolidayMap(holidayMap, "KB0", date);
				} else if (hijriDay == 10) {
					createHolidayMap(holidayMap, "KB1", date);
				} else if (hijriDay == 11) {
					createHolidayMap(holidayMap, "KB2", date);
				} else if (hijriDay == 12) {
					createHolidayMap(holidayMap, "KB3", date);
				} else if (hijriDay == 13) {
					createHolidayMap(holidayMap, "KB4", date);
				}
			}
		}

		return holidayMap;
	}

	/**
	 * @param map
	 * @param name
	 * @param date
	 */
	private void createHolidayMap(Map<String, String> map, String name, LocalDate date) {
		Date tarih = DateTimeUtils.toDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
		map.put(name, PdksUtil.convertToDateString(tarih, "yyyy-MM-dd"));

	}
}
