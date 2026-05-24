package com.tatil.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.pdks.genel.model.PdksUtil;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.chrono.HijrahDate;
import org.threeten.bp.temporal.ChronoField;

public class HolidayService implements Serializable {

	private static final long serialVersionUID = -4021629424879878878L;

	/**
	 * @param year
	 * @return
	 */
	public List<Holiday> calculateHolidays(int year) {
		// Map<String, String> holidayMap = new LinkedHashMap<String, String>();
		LocalDate startDate = LocalDate.of(year - 1, 12, 28);
		LocalDate endDate = LocalDate.of(year + 1, 1, 4);

		// LocalDate.isAfter ve plusDays döngüsü
		List<Holiday> tatilList = new ArrayList<Holiday>();
		Holiday holiday = null;
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

			// Miladi tarihi Hicri takvimine çeviriyoruz
			HijrahDate hijrahDate = HijrahDate.from(date);
			int hijriMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR);
			int hijriDay = hijrahDate.get(ChronoField.DAY_OF_MONTH);

			if (hijriMonth == 10) {// 1. Ramazan Bayramı Hesaplama (Şevval Ayı 1, 2, 3. Günler)
				if (hijriDay == 1) {
					Date tarih = getTarih(date.plusDays(-1));
					if (PdksUtil.getDateField(tarih, Calendar.YEAR) == year) {
						holiday = new Holiday("R", getTarih(date.plusDays(-1)), 3);
						tatilList.add(holiday);
					}

					// createHolidayMap(holidayMap, "RB0", date.plusDays(-1));
					// createHolidayMap(holidayMap, "RB1", date);
				} else if (hijriDay == 2) {
					// createHolidayMap(holidayMap, "RB2", date);
				} else if (hijriDay == 3) {
					if (holiday != null)
						holiday.setBitTarih(getTarih(date));
					// createHolidayMap(holidayMap, "RB3", date);
				}
			}

			if (hijriMonth == 12) {// 2. Kurban Bayramı Hesaplama (Zilhicce Ayı 9, 10, 11, 12, 13. Günler)
				if (hijriDay == 9) {
					Date tarih = getTarih(date);
					if (PdksUtil.getDateField(tarih, Calendar.YEAR) == year) {
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
						holiday.setBitTarih(getTarih(date));
					// createHolidayMap(holidayMap, "KB4", date);
				}
			}
		}
		if (holiday != null && holiday.getBitGun() == null)
			holiday.setBitTarih(PdksUtil.tariheGunEkleCikar(holiday.getBasTarih(), holiday.getGunAdet()));

		return tatilList;
	}

	private Date getTarih(LocalDate date) {
		Date tarih = DateTimeUtils.toDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return tarih;
	}
}
