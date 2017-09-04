package com.gem.daoImpl;

import java.net.InetSocketAddress;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.cassandra.core.CassandraTemplate;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.gem.Utility.UtilityTimeHelper;
import com.gem.bean.BudgetDataBean;
import com.gem.bean.CassandraCredentials;
import com.gem.dao.CassandraDao;
import com.gem.entity.DailyAccountPerformanceBean;
import com.gem.entity.DailyAdNetworkPerformanceBean;
import com.gem.entity.DailyCampaignPerformanceBean;
import com.gem.entity.DailyKeywordPerformanceBean;
import com.gem.entity.DailyLast_30_campaign_performance;
import com.gem.entity.MonthlyAccountPerformanceBean;
import com.gem.entity.MonthlyAdNetworkPerformanceBean;
import com.gem.entity.MonthlyCampaignPerformanceBean;
import com.gem.entity.WeeklyAccountPerformanceBean;
import com.gem.entity.WeeklyAdNetworkPerformanceBean;
import com.gem.entity.WeeklyCampaignPerformanceBean;
import com.gem.service.ServiceBudgetImpl;

public class CassandraCRUDImpl implements CassandraDao {

	private Cluster cluster;

	private Session session;

	String weekly_account_performance_Query = "";

	String weekly_account_ad_network_performance_Query = "";

	String weekly_campaign_performance_Query = "";

	String daily_account_performance_Query = "";

	String daily_account_ad_network_performance_Query = "";

	String daily_campaign_performance_Query = "";

	String dailyQuery = "";

	String daily_last_30days_keyword_performance_report = "";

	String daily_last_30days_campaign_performance = "";

	CassandraCredentials cassandraCredentials;
	List<InetSocketAddress> address;

	ServiceBudgetImpl service;

	private CassandraTemplate cassandraOperations;

	public void getConection() {

		if (session != null) {

		} else {

			address = new ArrayList<InetSocketAddress>();
			address.add(new InetSocketAddress(cassandraCredentials.getContactpoint(),
					Integer.parseInt(cassandraCredentials.getPort())));
			try {
				/*
				 * cluster =
				 * Cluster.builder().addContactPointsWithPorts(address).build();
				 */

				cluster = Cluster.builder().addContactPointsWithPorts(address).withCredentials("root", "Dig$dev%")
						.build();

				session = cluster.connect(cassandraCredentials.getDb());
				cassandraOperations = new CassandraTemplate(session);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public CassandraCRUDImpl() {

	}

	public CassandraCRUDImpl(CassandraCredentials cassandraCredentials, ServiceBudgetImpl service) {
		this.cassandraCredentials = cassandraCredentials;
		this.service = service;
	}

	public void writeDataToAccountPerformanceToCassandraWeekly(
			List<WeeklyAccountPerformanceBean> account_performance_weekly_data, BudgetDataBean budgetDataBean) {
		int size = account_performance_weekly_data.size();

		try {
			for (int i = 0; i < size; i++) {

				WeeklyAccountPerformanceBean weeklyAccountPerformanceBean = account_performance_weekly_data.get(i);
				weeklyAccountPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				weeklyAccountPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());

				weeklyAccountPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));

				cassandraOperations.insert(weeklyAccountPerformanceBean);

			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {

		}

	}

	public void writeDataToAdNetworkAccountPerformanceCassandraWeekly(
			List<WeeklyAdNetworkPerformanceBean> weekly_account_ad_network_performance_data,
			BudgetDataBean budgetDataBean) {

		int size = weekly_account_ad_network_performance_data.size();
		try {

			for (int i = 0; i < size; i++) {

				WeeklyAdNetworkPerformanceBean weeklyAdNetworkPerformanceBean = weekly_account_ad_network_performance_data
						.get(i);

				String adNetworkType = String.valueOf(weeklyAdNetworkPerformanceBean.getAd_network_type1());

				String dates = getDateThroughAdwordsDataWeekly(weeklyAdNetworkPerformanceBean, "Week");

				String budget = getBudgetType(adNetworkType, budgetDataBean);

				String budget_period = budgetDataBean.getBudget_period();

				String finalBudget = "";

				if (budget_period != null && !budget_period.equalsIgnoreCase("")
						&& !budget_period.equalsIgnoreCase("null")) {
					String finalTempBudget = String
							.valueOf(service.getAllClientsWeeklyBudget(budget, budget_period, dates));
					if (finalTempBudget != null) {
						finalBudget = finalTempBudget;
					}

				}
				weeklyAdNetworkPerformanceBean.setBudget(finalBudget);
				weeklyAdNetworkPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				weeklyAdNetworkPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				weeklyAdNetworkPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));
				weeklyAdNetworkPerformanceBean.setDate(new Date(System.currentTimeMillis()).toString());

				cassandraOperations.insert(weeklyAdNetworkPerformanceBean);

				List<String> strList = insertOtherAdnetworkTypes(adNetworkType);

				WeeklyAdNetworkPerformanceBean weeklyAdNetworkPerformanceBeanAdditionalNetworks = new WeeklyAdNetworkPerformanceBean();

				for (int j = 0; j < strList.size(); j++) {
					weeklyAdNetworkPerformanceBeanAdditionalNetworks.setTime(System.currentTimeMillis());
					weeklyAdNetworkPerformanceBeanAdditionalNetworks.setClient_stamp(budgetDataBean.getClient_stamp());
					weeklyAdNetworkPerformanceBeanAdditionalNetworks.setAd_network_type1(strList.get(j));
					String correspondingBudget = null;
					if (strList.get(j).equalsIgnoreCase("Search Network")) {
						correspondingBudget = budgetDataBean.getGoogle_search_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Display Network")) {
						correspondingBudget = budgetDataBean.getGoogle_display_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Youtube Videos")) {
						correspondingBudget = budgetDataBean.getGoogle_youtube_videos_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Youtube Search")) {
						correspondingBudget = budgetDataBean.getGoogle_youtube_search_ad_budget();
					}

					String customCalculatedBudget = String
							.valueOf(service.getAllClientsWeeklyBudget(correspondingBudget, budget_period, dates));

					weeklyAdNetworkPerformanceBeanAdditionalNetworks.setBudget(customCalculatedBudget);
					weeklyAdNetworkPerformanceBeanAdditionalNetworks
							.setDate(new Date(System.currentTimeMillis()).toString());
					weeklyAdNetworkPerformanceBeanAdditionalNetworks.setWeek(dates);

					cassandraOperations.insert(weeklyAdNetworkPerformanceBeanAdditionalNetworks);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}

	private List<String> insertOtherAdnetworkTypes(String adNetworkType) {
		List<String> remainingAdNetWorkTypes = new ArrayList<String>();
		if (adNetworkType.equalsIgnoreCase("Search Network")) {
			remainingAdNetWorkTypes.add("Display Network");
			remainingAdNetWorkTypes.add("Youtube Videos");
			remainingAdNetWorkTypes.add("Youtube Search");

		} else if (adNetworkType.equalsIgnoreCase("Display Network")) {
			remainingAdNetWorkTypes.add("Search Network");
			remainingAdNetWorkTypes.add("Youtube Videos");
			remainingAdNetWorkTypes.add("Youtube Search");

		} else if (adNetworkType.equalsIgnoreCase("Youtube Videos")) {
			remainingAdNetWorkTypes.add("Display Network");
			remainingAdNetWorkTypes.add("Search Network");
			remainingAdNetWorkTypes.add("Youtube Search");

		} else if (adNetworkType.equalsIgnoreCase("Youtube Search")) {
			remainingAdNetWorkTypes.add("Display Network");
			remainingAdNetWorkTypes.add("Search Network");
			remainingAdNetWorkTypes.add("Youtube Videos");
		}

		return remainingAdNetWorkTypes;
	}

	// new tables
	public void writeDataToKeywordPerformanceCassandraDaily(
			List<DailyKeywordPerformanceBean> DailyKeywordPerformanceBeanList, BudgetDataBean budgetDataBean) {
		int size = DailyKeywordPerformanceBeanList.size();
		try {

			for (int i = 0; i < size; i++) {

				DailyKeywordPerformanceBean dailyKeywordPerformanceBean = DailyKeywordPerformanceBeanList.get(i);
				dailyKeywordPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				dailyKeywordPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				dailyKeywordPerformanceBean.setDate(UtilityTimeHelper.getYesterdayDateString());

				dailyKeywordPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));

				cassandraOperations.insert(dailyKeywordPerformanceBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}

	// new tables
	public void writeDataToDailyLast_30_campaign_performance_CassandraDaily(
			List<DailyLast_30_campaign_performance> dailyLast_30_campaign_performance_list,
			BudgetDataBean budgetDataBean) {
		int size = dailyLast_30_campaign_performance_list.size();
		try {

			for (int i = 0; i < size; i++) {

				DailyLast_30_campaign_performance dailylast30CampaignPerformanceBean = dailyLast_30_campaign_performance_list
						.get(i);
				dailylast30CampaignPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				dailylast30CampaignPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				dailylast30CampaignPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));
				dailylast30CampaignPerformanceBean.setDate(UtilityTimeHelper.getYesterdayDateString());

				cassandraOperations.insert(dailylast30CampaignPerformanceBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}

	public void writeDataToAccountPerformanceToCassandraDaily(
			List<DailyAccountPerformanceBean> account_performance_weekly_data, BudgetDataBean budgetDataBean) {
		int size = account_performance_weekly_data.size();
		try {

			for (int i = 0; i < size; i++) {

				DailyAccountPerformanceBean dailyAccountPerformanceBean = account_performance_weekly_data.get(i);
				dailyAccountPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				dailyAccountPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				dailyAccountPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));

				cassandraOperations.insert(dailyAccountPerformanceBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void writeDataToAdNetworkAccountPerformanceCassandraDaily(
			List<DailyAdNetworkPerformanceBean> account_performance_daily_data, BudgetDataBean budgetDataBean) {

		int size = account_performance_daily_data.size();
		try {

			for (int i = 0; i < size; i++) {

				DailyAdNetworkPerformanceBean dailyAdNetworkPerformanceBean = account_performance_daily_data.get(i);

				String adNetworkType = String.valueOf(dailyAdNetworkPerformanceBean.getAd_network_type1());

				String dates = getDateThroughAdwordsDataDaily(dailyAdNetworkPerformanceBean, "Daily");

				String budget = getBudgetType(adNetworkType, budgetDataBean);

				String budget_period = budgetDataBean.getBudget_period();

				String finalBudget = "";
				if (budget_period != null && !budget_period.equalsIgnoreCase("")
						&& !budget_period.equalsIgnoreCase("null")) {
					String finalTempBudget = String
							.valueOf(service.getAllClientsDailyBudget(budget, budget_period, dates));
					finalBudget = finalTempBudget;
				}

				dailyAdNetworkPerformanceBean.setBudget(finalBudget);
				dailyAdNetworkPerformanceBean.setDate(dates);
				dailyAdNetworkPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				dailyAdNetworkPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				dailyAdNetworkPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));
				dailyAdNetworkPerformanceBean.setDate(new Date(System.currentTimeMillis()).toString());

				cassandraOperations.insert(dailyAdNetworkPerformanceBean);

				List<String> strList = insertOtherAdnetworkTypes(adNetworkType);

				DailyAdNetworkPerformanceBean dailyAdNetworkPerformanceBeanAdditionalNetworks = new DailyAdNetworkPerformanceBean();

				for (int j = 0; j < strList.size(); j++) {
					dailyAdNetworkPerformanceBeanAdditionalNetworks.setTime(System.currentTimeMillis());
					dailyAdNetworkPerformanceBeanAdditionalNetworks.setClient_stamp(budgetDataBean.getClient_stamp());
					dailyAdNetworkPerformanceBeanAdditionalNetworks.setAd_network_type1(strList.get(j));
					String correspondingBudget = null;
					if (strList.get(j).equalsIgnoreCase("Search Network")) {
						correspondingBudget = budgetDataBean.getGoogle_search_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Display Network")) {
						correspondingBudget = budgetDataBean.getGoogle_display_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Youtube Videos")) {
						correspondingBudget = budgetDataBean.getGoogle_youtube_videos_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Youtube Search")) {
						correspondingBudget = budgetDataBean.getGoogle_youtube_search_ad_budget();
					}

					String customCalculatedBudget = String
							.valueOf(service.getAllClientsDailyBudget(correspondingBudget, budget_period, dates));

					dailyAdNetworkPerformanceBeanAdditionalNetworks.setBudget(customCalculatedBudget);
					dailyAdNetworkPerformanceBeanAdditionalNetworks
							.setDate(new Date(System.currentTimeMillis()).toString());

					cassandraOperations.insert(dailyAdNetworkPerformanceBeanAdditionalNetworks);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}

	public void writeDataToCampaignAccountPerformanceCassandraDaily(
			List<DailyCampaignPerformanceBean> dailyCampaignPerformanceList, BudgetDataBean budgetDataBean) {
		int size = dailyCampaignPerformanceList.size();
		try {
			for (int i = 0; i < size; i++) {

				DailyCampaignPerformanceBean dailyCampaignPerformanceBean = dailyCampaignPerformanceList.get(i);
				dailyCampaignPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				dailyCampaignPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				dailyCampaignPerformanceBean.setDate(budgetDataBean.getDate());
				dailyCampaignPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));
				cassandraOperations.insert(dailyCampaignPerformanceBean);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void writeDataToCampaignAccountPerformanceCassandraWeekly(
			List<WeeklyCampaignPerformanceBean> account_performance_weekly_data, BudgetDataBean budgetDataBean) {
		int size = account_performance_weekly_data.size();
		try {
			for (int i = 0; i < size; i++) {

				WeeklyCampaignPerformanceBean weeklyCampaignPerformanceBean = account_performance_weekly_data.get(i);
				weeklyCampaignPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				weeklyCampaignPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				weeklyCampaignPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));
				cassandraOperations.insert(weeklyCampaignPerformanceBean);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void writeDataToCampaignAccountPerformanceCassandraMonthly(
			List<MonthlyCampaignPerformanceBean> account_performance_monthly_data, BudgetDataBean budgetDataBean) {
		int size = account_performance_monthly_data.size();
		try {

			for (int i = 0; i < size; i++) {

				MonthlyCampaignPerformanceBean monthlyCampaignPerformanceBean = account_performance_monthly_data.get(i);
				monthlyCampaignPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				monthlyCampaignPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				monthlyCampaignPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));

				cassandraOperations.insert(monthlyCampaignPerformanceBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void writeDataToAccountPerformanceToCassandraMonthly(
			List<MonthlyAccountPerformanceBean> account_performance_monthly_data, BudgetDataBean budgetDataBean) {

		int size = account_performance_monthly_data.size();

		try {

			for (int i = 0; i < size; i++) {
				MonthlyAccountPerformanceBean monthlyAccountPerformanceBean = account_performance_monthly_data.get(i);
				monthlyAccountPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				monthlyAccountPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				monthlyAccountPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));

				cassandraOperations.insert(monthlyAccountPerformanceBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}

	public void writeDataToAdNetworkAccountPerformanceCassandraMonthly(
			List<MonthlyAdNetworkPerformanceBean> account_performance_monthly_data, BudgetDataBean budgetDataBean) {

		int size = account_performance_monthly_data.size();
		try {

			for (int i = 0; i < size; i++) {

				MonthlyAdNetworkPerformanceBean monthlyAdNetworkPerformanceBean = account_performance_monthly_data
						.get(i);

				String adNetworkType = String.valueOf(monthlyAdNetworkPerformanceBean.getAd_network_type1());

				String dates = getDateThroughAdwordsDataMonthly(monthlyAdNetworkPerformanceBean, "Month");

				String budget = getBudgetType(adNetworkType, budgetDataBean);

				String budget_period = budgetDataBean.getBudget_period();

				String finalBudget = "";
				if (budget_period != null && !budget_period.equalsIgnoreCase("")
						&& !budget_period.equalsIgnoreCase("null")) {

					String finalTempBudget = String
							.valueOf(service.getAllClientsMonthlyBudget(budget, budget_period, dates));
					finalBudget = finalTempBudget;
				}

				monthlyAdNetworkPerformanceBean.setBudget(finalBudget);
				monthlyAdNetworkPerformanceBean.setClient_stamp(budgetDataBean.getClient_stamp());
				monthlyAdNetworkPerformanceBean.setClient_customer_id(budgetDataBean.getClient_customer_id());
				monthlyAdNetworkPerformanceBean.setTime(Long.valueOf(System.currentTimeMillis()));
				monthlyAdNetworkPerformanceBean.setDate(new Date(System.currentTimeMillis()).toString());

				cassandraOperations.insert(monthlyAdNetworkPerformanceBean);

				List<String> strList = insertOtherAdnetworkTypes(adNetworkType);

				MonthlyAdNetworkPerformanceBean monthlyAdNetworkPerformanceBeanAdditionalNetworks = new MonthlyAdNetworkPerformanceBean();

				for (int j = 0; j < strList.size(); j++) {
					monthlyAdNetworkPerformanceBeanAdditionalNetworks.setTime(System.currentTimeMillis());
					monthlyAdNetworkPerformanceBeanAdditionalNetworks.setClient_stamp(budgetDataBean.getClient_stamp());
					monthlyAdNetworkPerformanceBeanAdditionalNetworks.setAd_network_type1(strList.get(j));
					String correspondingBudget = null;

					if (strList.get(j).equalsIgnoreCase("Search Network")) {
						correspondingBudget = budgetDataBean.getGoogle_search_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Display Network")) {
						correspondingBudget = budgetDataBean.getGoogle_display_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Youtube Videos")) {
						correspondingBudget = budgetDataBean.getGoogle_youtube_videos_ad_budget();
					} else if (strList.get(j).equalsIgnoreCase("Youtube Search")) {
						correspondingBudget = budgetDataBean.getGoogle_youtube_search_ad_budget();
					}

					String customCalculatedBudget = String
							.valueOf(service.getAllClientsMonthlyBudget(correspondingBudget, budget_period, dates));

					monthlyAdNetworkPerformanceBeanAdditionalNetworks.setBudget(customCalculatedBudget);
					monthlyAdNetworkPerformanceBeanAdditionalNetworks
							.setDate(new Date(System.currentTimeMillis()).toString());
					monthlyAdNetworkPerformanceBeanAdditionalNetworks.setMonth(dates);

					cassandraOperations.insert(monthlyAdNetworkPerformanceBeanAdditionalNetworks);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(budgetDataBean.getClient_stamp());
		} finally {

		}

	}

	private String getDateThroughAdwordsDataMonthly(MonthlyAdNetworkPerformanceBean monthlyAdNetworkPerformanceBean,
			String string) {
		return monthlyAdNetworkPerformanceBean.getMonth();
	}

	private String getDateThroughAdwordsDataWeekly(WeeklyAdNetworkPerformanceBean weeklyAdNetworkPerformanceBean,
			String string) {

		return weeklyAdNetworkPerformanceBean.getWeek();
	}

	private String getDateThroughAdwordsDataDaily(DailyAdNetworkPerformanceBean dailyAdNetworkPerformanceBean,
			String string) {
		String s = "";
		if (dailyAdNetworkPerformanceBean.getDate() != null) {
			s = dailyAdNetworkPerformanceBean.getDate();
		} else {
			s = new Date(System.currentTimeMillis()).toString();
		}
		return s;
	}

	private String getBudgetType(String adNetworkType, BudgetDataBean budgetDataBean) {
		String budget = "";
		if (adNetworkType.equalsIgnoreCase("Search Network")) {
			budget = budgetDataBean.getGoogle_search_ad_budget() + "," + "Search Network";
		} else if (adNetworkType.equalsIgnoreCase("Display Network")) {
			budget = budgetDataBean.getGoogle_display_ad_budget() + "," + "Display Network";
		} else if (adNetworkType.equalsIgnoreCase("Youtube Videos")) {
			budget = budgetDataBean.getGoogle_youtube_videos_ad_budget() + "," + "Youtube Videos";
		} else if (adNetworkType.equalsIgnoreCase("Youtube Search")) {
			budget = budgetDataBean.getGoogle_youtube_search_ad_budget() + "," + "Youtube Search";
		}
		return budget;
	}

	public void closeSessionAndCluster() {
		session.close();
		session = null;
		cluster.close();
		cluster = null;
	}

}
