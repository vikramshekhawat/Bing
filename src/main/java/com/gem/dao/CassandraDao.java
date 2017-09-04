package com.gem.dao;

import java.util.List;

import com.gem.bean.BudgetDataBean;
import com.gem.entity.DailyAccountPerformanceBean;
import com.gem.entity.DailyAdNetworkPerformanceBean;
import com.gem.entity.DailyCampaignPerformanceBean;
import com.gem.entity.DailyKeywordPerformanceBean;
import com.gem.entity.MonthlyAccountPerformanceBean;
import com.gem.entity.MonthlyAdNetworkPerformanceBean;
import com.gem.entity.MonthlyCampaignPerformanceBean;
import com.gem.entity.WeeklyAccountPerformanceBean;
import com.gem.entity.WeeklyAdNetworkPerformanceBean;
import com.gem.entity.WeeklyCampaignPerformanceBean;

public interface CassandraDao {

	public void writeDataToAdNetworkAccountPerformanceCassandraDaily(
			List<DailyAdNetworkPerformanceBean> account_performance_daily_data, BudgetDataBean budgetDataBean);

	public void writeDataToAccountPerformanceToCassandraDaily(
			List<DailyAccountPerformanceBean> account_performance_weekly_data, BudgetDataBean budgetDataBean);

	public void writeDataToCampaignAccountPerformanceCassandraDaily(
			List<DailyCampaignPerformanceBean> account_performance_weekly_data, BudgetDataBean budgetDataBean);
	
	public void writeDataToKeywordPerformanceCassandraDaily(
			List<DailyKeywordPerformanceBean> DailyKeywordPerformanceBeanList, BudgetDataBean budgetDataBean);

	public void writeDataToAdNetworkAccountPerformanceCassandraWeekly(
			List<WeeklyAdNetworkPerformanceBean> weekly_account_ad_network_performance_data,
			BudgetDataBean budgetDataBean);

	public void writeDataToAccountPerformanceToCassandraWeekly(
			List<WeeklyAccountPerformanceBean> account_performance_weekly_data, BudgetDataBean budgetDataBean);

	public void writeDataToCampaignAccountPerformanceCassandraWeekly(
			List<WeeklyCampaignPerformanceBean> account_performance_weekly_data, BudgetDataBean budgetDataBean);

	public void writeDataToCampaignAccountPerformanceCassandraMonthly(
			List<MonthlyCampaignPerformanceBean> account_performance_monthly_data, BudgetDataBean budgetDataBean);

	public void writeDataToAccountPerformanceToCassandraMonthly(
			List<MonthlyAccountPerformanceBean> account_performance_monthly_data, BudgetDataBean budgetDataBean);

	public void writeDataToAdNetworkAccountPerformanceCassandraMonthly(
			List<MonthlyAdNetworkPerformanceBean> account_performance_monthly_data, BudgetDataBean bean);
}
