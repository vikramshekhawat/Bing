package com.gem.entity;

import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table("adwords_daily_keyword_performance")
public class DailyKeywordPerformanceBean {

	@PrimaryKeyColumn(name = "client_stamp", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String client_stamp;
	@Column
	private String ad_group;
	@Column
	private Float average_position;
	@Column
	private Float average_visit_duration;
	@Column
	private Float bounce_rate;
	@Column
	private String campaign;
	@Column
	private Integer clicks;
	@Column
	private String client_customer_id;

	@Column
	private Float conversions;
	@Column
	private Float cost;
	@Column
	private Float cost_per_conversion;
	@Column
	private Float impressions;

	@PrimaryKeyColumn(name = "keyword", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	@Column
	private String keyword;
	@Column
	private String match_type;
	@Column
	private String campaign_name;
	@Column
	private String ctr;
	@Column
	private String date;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCtr() {
		return ctr;
	}

	public void setCtr(String ctr) {
		this.ctr = ctr;
	}

	public String getCampaign_name() {
		return campaign_name;
	}

	public void setCampaign_name(String campaign_name) {
		this.campaign_name = campaign_name;
	}

	@PrimaryKeyColumn(name = "time", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	private Long time;

	public DailyKeywordPerformanceBean() {
		super();
	}

	public String getClient_stamp() {
		return client_stamp;
	}

	public void setClient_stamp(String client_stamp) {
		this.client_stamp = client_stamp;
	}

	public String getAd_group() {
		return ad_group;
	}

	public void setAd_group(String ad_group) {
		this.ad_group = ad_group;
	}

	public Float getAverage_position() {
		return average_position;
	}

	public void setAverage_position(Float average_position) {
		this.average_position = average_position;
	}

	public Float getAverage_visit_duration() {
		return average_visit_duration;
	}

	public void setAverage_visit_duration(Float average_visit_duration) {
		this.average_visit_duration = average_visit_duration;
	}

	public Float getBounce_rate() {
		return bounce_rate;
	}

	public void setBounce_rate(Float bounce_rate) {
		this.bounce_rate = bounce_rate;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
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

	public Float getImpressions() {
		return impressions;
	}

	public void setImpressions(Float impressions) {
		this.impressions = impressions;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getMatch_type() {
		return match_type;
	}

	public void setMatch_type(String match_type) {
		this.match_type = match_type;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

}