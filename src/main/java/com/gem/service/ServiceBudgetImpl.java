package com.gem.service;

public class ServiceBudgetImpl implements ServiceBudget {

	private int dayInMonths(int month, int year) {

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

	public String getAllClientsMonthlyBudget(String budgetNetworktype, String budget_period, String dates) {

		int month = 0;
		int year = 0;
		month = Integer.parseInt(dates.split("-")[1]);
		year = Integer.parseInt(dates.split("-")[0]);
		String budget = budgetNetworktype.split(",")[0];

		if (budget.equalsIgnoreCase("") || budget.equalsIgnoreCase("null")) {
			budget = "0";
		}

		String monthlyBudget = "";

		if (budget_period.equalsIgnoreCase("month")) {
			monthlyBudget = budget;

		} else if (budget_period.equalsIgnoreCase("week")) {
			monthlyBudget = String.valueOf(Math.round((Double.parseDouble(budget) * ((dayInMonths(month, year)) / 7))));
		} else {
			monthlyBudget = String.valueOf(Math.round((Double.parseDouble(budget) * dayInMonths(month, year))));
			
		}

		return monthlyBudget;
	}

	public String getAllClientsDailyBudget(String budgetNetworktype, String budget_period, String dates) {

		int month = 0;
		int year = 0;
		month = Integer.parseInt(dates.split("-")[1]);
		year = Integer.parseInt(dates.split("-")[0]);
		String budget = budgetNetworktype.split(",")[0];

		if (budget.equalsIgnoreCase("") || budget.equalsIgnoreCase("null")) {
			budget = "0";
		}
		String dailyBudget = "";

		if (budget_period.equalsIgnoreCase("month")) {
			dailyBudget = String.valueOf(Math.round((Double.parseDouble(budget) / dayInMonths(month, year))));

		} else if (budget_period.equalsIgnoreCase("week")) {
			dailyBudget = String.valueOf(Math.round((Double.parseDouble(budget) / 7)));
		} else {
			dailyBudget = budget;
			
		}

		return dailyBudget;
	}

	public String getAllClientsWeeklyBudget(String budgetNetworktype, String budget_period, String dates) {

		int month = 0;
		int year = 0;
		month = Integer.parseInt(dates.split("-")[1]);
		year = Integer.parseInt(dates.split("-")[0]);
		String budget = budgetNetworktype.split(",")[0];

		if (budget.equalsIgnoreCase("") || budget.equalsIgnoreCase("null")) {
			budget = "0";
		}
		String weeklyBudget = "";

		if (budget_period.equalsIgnoreCase("month")) {
			weeklyBudget = String.valueOf(Math.round((Double.parseDouble(budget) / (dayInMonths(month, year)) * 7)));
		} else if (budget_period.equalsIgnoreCase("week")) {
			weeklyBudget = budget;
		} else {
			weeklyBudget = String.valueOf(Math.round((Double.parseDouble(budget) * 7)));
		}

		return weeklyBudget;
	}
}
