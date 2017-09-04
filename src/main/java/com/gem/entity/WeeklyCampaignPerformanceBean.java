package com.gem.entity;

import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table("adwords_weekly_account_campaign_performance")
public class WeeklyCampaignPerformanceBean {
	@PrimaryKeyColumn(name = "client_stamp", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String client_stamp;
	@PrimaryKeyColumn(name = "time", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private Long time;
	@Column
	private String account_currency_code;
	@Column
	private String account_descriptive_name;
	@Column
	private String account_timezone_id;
	@Column
	private String advertising_channel_type;
	@Column
	private Float all_conversion_value;
	@Column
	private Float average_position;
	@Column
	private Float average_time_on_site;
	@Column
	private Float bounce_rate;

	@PrimaryKeyColumn(name = "campaign_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	@Column
	private String campaign_id;
	@Column
	private String campaign_name;
	@Column
	private String campaign_status;
	@Column
	private Float click_assisted_conversions;
	@Column
	private Integer clicks;
	@Column
	private String client_customer_id;
	@Column
	private Float conversion_rate;
	@Column
	private Float conversions;
	@Column
	private Float cost;
	@Column
	private Float cost_per_conversion;
	@Column
	private Float ctr;
	@Column
	private String impression_reach;
	@Column
	private Long impressions;
	@Column
	private Float invalid_click_rate;
	@Column
	private Integer invalid_clicks;
	@Column
	private Float search_budget_lost_impression_share;
	@Column
	private Float search_exact_match_impression_share;
	@Column
	private Float search_impression_share;
	@Column
	private Float search_rank_lost_impression_share;
	@Column
	private Float video_view_rate;
	@Column
	private Integer video_views;
	@Column
	private Integer view_through_conversions;
	@Column
	private String week;

	public WeeklyCampaignPerformanceBean() {
		// TODO Auto-generated constructor stub
	}

	public String getClient_stamp() {
		return client_stamp;
	}

	public void setClient_stamp(String client_stamp) {
		this.client_stamp = client_stamp;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getAccount_currency_code() {
		return account_currency_code;
	}

	public void setAccount_currency_code(String account_currency_code) {
		this.account_currency_code = account_currency_code;
	}

	public String getAccount_descriptive_name() {
		return account_descriptive_name;
	}

	public void setAccount_descriptive_name(String account_descriptive_name) {
		this.account_descriptive_name = account_descriptive_name;
	}

	public String getAccount_timezone_id() {
		return account_timezone_id;
	}

	public void setAccount_timezone_id(String account_timezone_id) {
		this.account_timezone_id = account_timezone_id;
	}

	public String getAdvertising_channel_type() {
		return advertising_channel_type;
	}

	public void setAdvertising_channel_type(String advertising_channel_type) {
		this.advertising_channel_type = advertising_channel_type;
	}

	public Float getAll_conversion_value() {
		return all_conversion_value;
	}

	public void setAll_conversion_value(Float all_conversion_value) {
		this.all_conversion_value = all_conversion_value;
	}

	public Float getAverage_position() {
		return average_position;
	}

	public void setAverage_position(Float average_position) {
		this.average_position = average_position;
	}

	public Float getAverage_time_on_site() {
		return average_time_on_site;
	}

	public void setAverage_time_on_site(Float average_time_on_site) {
		this.average_time_on_site = average_time_on_site;
	}

	public Float getBounce_rate() {
		return bounce_rate;
	}

	public void setBounce_rate(Float bounce_rate) {
		this.bounce_rate = bounce_rate;
	}

	public String getCampaign_id() {
		return campaign_id;
	}

	public void setCampaign_id(String campaign_id) {
		this.campaign_id = campaign_id;
	}

	public String getCampaign_name() {
		return campaign_name;
	}

	public void setCampaign_name(String campaign_name) {
		this.campaign_name = campaign_name;
	}

	public String getCampaign_status() {
		return campaign_status;
	}

	public void setCampaign_status(String campaign_status) {
		this.campaign_status = campaign_status;
	}

	public Float getClick_assisted_conversions() {
		return click_assisted_conversions;
	}

	public void setClick_assisted_conversions(Float click_assisted_conversions) {
		this.click_assisted_conversions = click_assisted_conversions;
	}

	public Integer getClicks() {
		return clicks;
	}

	public void setClicks(Integer clicks) {
		this.clicks = clicks;
	}

	public String getClient_customer_id() {
		return client_customer_id;
	}

	public void setClient_customer_id(String client_customer_id) {
		this.client_customer_id = client_customer_id;
	}

	public Float getConversion_rate() {
		return conversion_rate;
	}

	public void setConversion_rate(Float conversion_rate) {
		this.conversion_rate = conversion_rate;
	}

	public Float getConversions() {
		return conversions;
	}

	public void setConversions(Float conversions) {
		this.conversions = conversions;
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

	public Float getCtr() {
		return ctr;
	}

	public void setCtr(Float ctr) {
		this.ctr = ctr;
	}

	public String getImpression_reach() {
		return impression_reach;
	}

	public void setImpression_reach(String impression_reach) {
		this.impression_reach = impression_reach;
	}

	public Long getImpressions() {
		return impressions;
	}

	public void setImpressions(Long impressions) {
		this.impressions = impressions;
	}

	public Float getInvalid_click_rate() {
		return invalid_click_rate;
	}

	public void setInvalid_click_rate(Float invalid_click_rate) {
		this.invalid_click_rate = invalid_click_rate;
	}

	public Integer getInvalid_clicks() {
		return invalid_clicks;
	}

	public void setInvalid_clicks(Integer invalid_clicks) {
		this.invalid_clicks = invalid_clicks;
	}

	public Float getSearch_budget_lost_impression_share() {
		return search_budget_lost_impression_share;
	}

	public void setSearch_budget_lost_impression_share(Float search_budget_lost_impression_share) {
		this.search_budget_lost_impression_share = search_budget_lost_impression_share;
	}

	public Float getSearch_exact_match_impression_share() {
		return search_exact_match_impression_share;
	}

	public void setSearch_exact_match_impression_share(Float search_exact_match_impression_share) {
		this.search_exact_match_impression_share = search_exact_match_impression_share;
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

	public Float getVideo_view_rate() {
		return video_view_rate;
	}

	public void setVideo_view_rate(Float video_view_rate) {
		this.video_view_rate = video_view_rate;
	}

	public Integer getVideo_views() {
		return video_views;
	}

	public void setVideo_views(Integer video_views) {
		this.video_views = video_views;
	}

	public Integer getView_through_conversions() {
		return view_through_conversions;
	}

	public void setView_through_conversions(Integer view_through_conversions) {
		this.view_through_conversions = view_through_conversions;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

}
