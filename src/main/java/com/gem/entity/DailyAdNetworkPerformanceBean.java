package com.gem.entity;

import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table("adwords_daily_ad_network_account_performance")
public class DailyAdNetworkPerformanceBean {

	@PrimaryKeyColumn(name = "client_stamp", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String client_stamp;
	@PrimaryKeyColumn(name = "time", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private Long time;
	@Column
	private String client_customer_id;
	@PrimaryKeyColumn(name = "ad_network_type1", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	@Column
	private String ad_network_type1;
	@Column
	private String budget;
	@Column
	private String week;
	@Column
	private String external_customer_id;
	@Column
	private String account_descriptive_name;
	@Column
	private String account_currency_code;
	@Column
	private String account_timezone_id;
	@Column
	private Float average_position;
	@Column
	private Float conversions;
	@Column
	private Long impressions;
	@Column
	private Integer clicks;
	@Column
	private Float ctr;
	@Column
	private Float cost;
	@Column
	private Float cost_per_conversion;
	@Column
	private Float all_conversion_rate;
	@Column
	private Integer video_views;
	@Column
	private Float video_view_rate;
	@Column
	private Float average_cpv;
	@Column
	private Integer view_through_conversions;
	@Column
	private Float search_budget_lost_impression_share;
	@Column
	private Float search_exactmatch_impression_share;
	@Column
	private Float search_impression_share;
	@Column
	private Float search_rank_lost_impression_share;
	@Column
	private Integer invalid_clicks;
	@Column
	private Float invalid_click_rate;
	@Column
	private Float all_conversion_value;
	@Column
	private String date;

	public DailyAdNetworkPerformanceBean() {
		// TODO Auto-generated constructor stub
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getClient_customer_id() {
		return client_customer_id;
	}

	public void setClient_customer_id(String client_customer_id) {
		this.client_customer_id = client_customer_id;
	}

	public String getAd_network_type1() {
		return ad_network_type1;
	}

	public void setAd_network_type1(String ad_network_type1) {
		this.ad_network_type1 = ad_network_type1;
	}

	public String getBudget() {
		return budget;
	}

	public void setBudget(String budget) {
		this.budget = budget;
	}

	public String getClient_stamp() {
		return client_stamp;
	}

	public void setClient_stamp(String client_stamp) {
		this.client_stamp = client_stamp;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getExternal_customer_id() {
		return external_customer_id;
	}

	public void setExternal_customer_id(String external_customer_id) {
		this.external_customer_id = external_customer_id;
	}

	public String getAccount_descriptive_name() {
		return account_descriptive_name;
	}

	public void setAccount_descriptive_name(String account_descriptive_name) {
		this.account_descriptive_name = account_descriptive_name;
	}

	public String getAccount_currency_code() {
		return account_currency_code;
	}

	public void setAccount_currency_code(String account_currency_code) {
		this.account_currency_code = account_currency_code;
	}

	public String getAccount_timezone_id() {
		return account_timezone_id;
	}

	public void setAccount_timezone_id(String account_timezone_id) {
		this.account_timezone_id = account_timezone_id;
	}

	public Float getAverage_position() {
		return average_position;
	}

	public void setAverage_position(Float average_position) {
		this.average_position = average_position;
	}

	public Float getConversions() {
		return conversions;
	}

	public void setConversions(Float conversions) {
		this.conversions = conversions;
	}

	public Long getImpressions() {
		return impressions;
	}

	public void setImpressions(Long impressions) {
		this.impressions = impressions;
	}

	public Integer getClicks() {
		return clicks;
	}

	public void setClicks(Integer clicks) {
		this.clicks = clicks;
	}

	public Float getCtr() {
		return ctr;
	}

	public void setCtr(Float ctr) {
		this.ctr = ctr;
	}

	public Float getCost() {
		return cost;
	}

	public void setCost(Float cost) {
		this.cost = cost;
	}

	public Float getCost_per_conversion() {
		return cost_per_conversion;
	}

	public void setCost_per_conversion(Float cost_per_conversion) {
		this.cost_per_conversion = cost_per_conversion;
	}

	public Float getAll_conversion_rate() {
		return all_conversion_rate;
	}

	public void setAll_conversion_rate(Float all_conversion_rate) {
		this.all_conversion_rate = all_conversion_rate;
	}

	public Integer getVideo_views() {
		return video_views;
	}

	public void setVideo_views(Integer video_views) {
		this.video_views = video_views;
	}

	public Float getVideo_view_rate() {
		return video_view_rate;
	}

	public void setVideo_view_rate(Float video_view_rate) {
		this.video_view_rate = video_view_rate;
	}

	public Float getAverage_cpv() {
		return average_cpv;
	}

	public void setAverage_cpv(Float average_cpv) {
		this.average_cpv = average_cpv;
	}

	public Integer getView_through_conversions() {
		return view_through_conversions;
	}

	public void setView_through_conversions(Integer view_through_conversions) {
		this.view_through_conversions = view_through_conversions;
	}

	public Float getSearch_budget_lost_impression_share() {
		return search_budget_lost_impression_share;
	}

	public void setSearch_budget_lost_impression_share(Float search_budget_lost_impression_share) {
		this.search_budget_lost_impression_share = search_budget_lost_impression_share;
	}

	public Float getSearch_exactmatch_impression_share() {
		return search_exactmatch_impression_share;
	}

	public void setSearch_exactmatch_impression_share(Float search_exactmatch_impression_share) {
		this.search_exactmatch_impression_share = search_exactmatch_impression_share;
	}

	public Float getSearch_impression_share() {
		return search_impression_share;
	}

	public void setSearch_impression_share(Float search_impression_share) {
		this.search_impression_share = search_impression_share;
	}

	public Float getSearch_rank_lost_impression_share() {
		return search_rank_lost_impression_share;
	}

	public void setSearch_rank_lost_impression_share(Float search_rank_lost_impression_share) {
		this.search_rank_lost_impression_share = search_rank_lost_impression_share;
	}

	public Integer getInvalid_clicks() {
		return invalid_clicks;
	}

	public void setInvalid_clicks(Integer invalid_clicks) {
		this.invalid_clicks = invalid_clicks;
	}

	public Float getInvalid_click_rate() {
		return invalid_click_rate;
	}

	public void setInvalid_click_rate(Float invalid_click_rate) {
		this.invalid_click_rate = invalid_click_rate;
	}

	public Float getAll_conversion_value() {
		return all_conversion_value;
	}

	public void setAll_conversion_value(Float all_conversion_value) {
		this.all_conversion_value = all_conversion_value;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}
}
