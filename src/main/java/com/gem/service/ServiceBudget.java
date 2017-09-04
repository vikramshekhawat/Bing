package com.gem.service;

public interface ServiceBudget {

	public String getAllClientsMonthlyBudget(String clientMonthlyBudget, String clientWeeklyBudget, String date);

	public String getAllClientsWeeklyBudget(String clientMonthlyBudget, String clientWeeklyBudget, String date);

	public String getAllClientsDailyBudget(String clientMonthlyBudget, String clientWeeklyBudget, String date);

}
