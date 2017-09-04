package com.gem.entity;

import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

/*@Table("adwords_daily_last_30_account_campaign_performance")*/

@Table("daily_last30_account_campaign_performance")

public class DailyLast_30_campaign_performance {
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
	private float all_conversion_value;
	@Column
	private float average_position;
	@Column
	private float average_time_on_site;
	@Column
	private float bounce_rate;
	@PrimaryKeyColumn(name = "campaign_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	@Column
	private String campaign_id;
	@Column
	private String campaign_name;
	@Column
	private String campaign_status;
	@Column
	private float click_assisted_conversions;
	@Column
	private int clicks;
	@Column
	private String client_customer_id;
	@Column
	private float conversion_rate;
	@Column
	private float conversions;
	@Column
	private float cost;
	@Column
	private float cost_per_conversion;
	@Column
	private float ctr;
	@Column
	private String date;
	@Column
	private String impression_reach;
	@Column
	private Long impressions;
	@Column
	private float invalid_click_rate;
	@Column
	private int invalid_clicks;
	@Column
	private float search_budget_lost_impression_share;
	@Column
	private float search_exact_match_impression_share;
	@Column
	private float search_impression_share;
	@Column
	private float search_rank_lost_impression_share;
	@Column
	private float video_view_rate;
	@Column
	private int video_views;
	@Column
	private int view_through_conversions;

	public String getClient_stamp() {
		return client_stamp;
	}

	public void setClient_stamp(String client_stamp) {
		this.client_stamp = client_stamp;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long long1) {
		this.time = long1;
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

	public float getAll_conversion_value() {
		return all_conversion_value;
	}

	public void setAll_conversion_value(float all_conversion_value) {
		this.all_conversion_value = all_conversion_value;
	}

	public float getAverage_position() {
		return average_position;
	}

	public void setAverage_position(float average_position) {
		this.average_position = average_position;
	}

	public float getAverage_time_on_site() {
		return average_time_on_site;
	}

	public void setAverage_time_on_site(float average_time_on_site) {
		this.average_time_on_site = average_time_on_site;
	}

	public float getBounce_rate() {
		return bounce_rate;
	}

	public void setBounce_rate(float bounce_rate) {
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

	public float getClick_assisted_conversions() {
		return click_assisted_conversions;
	}

	public void setClick_assisted_conversions(float click_assisted_conversions) {
		this.click_assisted_conversions = click_assisted_conversions;
	}

	public int getClicks() {
		return clicks;
	}

	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

	public String getClient_customer_id() {
		return client_customer_id;
	}

	public void setClient_customer_id(String client_customer_id) {
		this.client_customer_id = client_customer_id;
	}

	public float getConversion_rate() {
		return conversion_rate;
	}

	public void setConversion_rate(float conversion_rate) {
		this.conversion_rate = conversion_rate;
	}

	public float getConversions() {
		return conversions;
	}

	public void setConversions(float conversions) {
		this.conversions = conversions;
	}

	public float getCost() {
		return cost;
	}

	public void setCost(float cost) {
		this.cost = cost;
	}

	public float getCost_per_conversion() {
		return cost_per_conversion;
	}

	public void setCost_per_conversion(float cost_per_conversion) {
		this.cost_per_conversion = cost_per_conversion;
	}

	public float getCtr() {
		return ctr;
	}

	public void setCtr(float ctr) {
		this.ctr = ctr;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String datestamp) {
		this.date = datestamp;
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

	public float getInvalid_click_rate() {
		return invalid_click_rate;
	}

	public void setInvalid_click_rate(float invalid_click_rate) {
		this.invalid_click_rate = invalid_click_rate;
	}

	public int getInvalid_clicks() {
		return invalid_clicks;
	}

	public void setInvalid_clicks(int invalid_clicks) {
		this.invalid_clicks = invalid_clicks;
	}

	public float getSearch_budget_lost_impression_share() {
		return search_budget_lost_impression_share;
	}

	public void setSearch_budget_lost_impression_share(float search_budget_lost_impression_share) {
		this.search_budget_lost_impression_share = search_budget_lost_impression_share;
	}

	public float getSearch_exact_match_impression_share() {
		return search_exact_match_impression_share;
	}

	public void setSearch_exact_match_impression_share(float search_exact_match_impression_share) {
		this.search_exact_match_impression_share = search_exact_match_impression_share;
	}

	public float getSearch_impression_share() {
		return search_impression_share;
	}

	public void setSearch_impression_share(float search_impression_share) {
		this.search_impression_share = search_impression_share;
	}

	public float getSearch_rank_lost_impression_share() {
		return search_rank_lost_impression_share;
	}

	public void setSearch_rank_lost_impression_share(float search_rank_lost_impression_share) {
		this.search_rank_lost_impression_share = search_rank_lost_impression_share;
	}

	public float getVideo_view_rate() {
		return video_view_rate;
	}

	public void setVideo_view_rate(float video_view_rate) {
		this.video_view_rate = video_view_rate;
	}

	public int getVideo_views() {
		return video_views;
	}

	public void setVideo_views(int video_views) {
		this.video_views = video_views;
	}

	public int getView_through_conversions() {
		return view_through_conversions;
	}

	public void setView_through_conversions(int view_through_conversions) {
		this.view_through_conversions = view_through_conversions;
	}

}
