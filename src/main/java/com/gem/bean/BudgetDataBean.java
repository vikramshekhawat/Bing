package com.gem.bean;

public class BudgetDataBean {

	private String budget_period;
	private String google_display_ad_budget;
	private String google_search_ad_budget;
	private String google_youtube_search_ad_budget;
	private String google_youtube_videos_ad_budget;
	private String client_stamp;
	private String client_customer_id;
	private String date;
	private String company_name;

	public BudgetDataBean() {
	}

	public BudgetDataBean(String budget_period, String google_display_ad_budget, String google_search_ad_budget,
			String google_youtube_search_ad_budget, String google_youtube_videos_ad_budget, String client_stamp,
			String client_customer_id, String date, String company_name) {
		super();
		this.budget_period = budget_period;
		this.google_display_ad_budget = google_display_ad_budget;
		this.google_search_ad_budget = google_search_ad_budget;
		this.google_youtube_search_ad_budget = google_youtube_search_ad_budget;
		this.google_youtube_videos_ad_budget = google_youtube_videos_ad_budget;
		this.client_stamp = client_stamp;
		this.client_customer_id = client_customer_id;
		this.date = date;
		this.company_name = company_name;
	}

	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	public String getBudget_period() {
		return budget_period;
	}

	public void setBudget_period(String budget_period) {
		this.budget_period = budget_period;
	}

	public String getGoogle_display_ad_budget() {
		return google_display_ad_budget;
	}

	public void setGoogle_display_ad_budget(String google_display_ad_budget) {
		this.google_display_ad_budget = google_display_ad_budget;
	}

	public String getGoogle_search_ad_budget() {
		return google_search_ad_budget;
	}

	public void setGoogle_search_ad_budget(String google_search_ad_budget) {
		this.google_search_ad_budget = google_search_ad_budget;
	}

	public String getGoogle_youtube_search_ad_budget() {
		return google_youtube_search_ad_budget;
	}

	public void setGoogle_youtube_search_ad_budget(String google_youtube_search_ad_budget) {
		this.google_youtube_search_ad_budget = google_youtube_search_ad_budget;
	}

	public String getGoogle_youtube_videos_ad_budget() {
		return google_youtube_videos_ad_budget;
	}

	public void setGoogle_youtube_videos_ad_budget(String google_youtube_videos_ad_budget) {
		this.google_youtube_videos_ad_budget = google_youtube_videos_ad_budget;
	}

	public String getClient_stamp() {
		return client_stamp;
	}

	public void setClient_stamp(String client_stamp) {
		this.client_stamp = client_stamp;
	}

	public String getClient_customer_id() {
		return client_customer_id;
	}

	public void setClient_customer_id(String client_customer_id) {
		this.client_customer_id = client_customer_id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}