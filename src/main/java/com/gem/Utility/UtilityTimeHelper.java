package com.gem.Utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UtilityTimeHelper {
	public int dayInMonths(int month, int year) {

		int daysInMonth = 0;
		if (month == 4 || month == 6 || month == 9 || month == 11)

			daysInMonth = 30;
		else if (month == 2)

			daysInMonth = (year % 4 == 0 && ((year % 100 == 0 && year % 400 == 0))) ? 29 : 28;
		else {

			daysInMonth = 31;
		}
		return daysInMonth;
	}

	public static String getYesterdayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return dateFormat.format(cal.getTime());
	}
}
