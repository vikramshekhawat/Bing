package com.gem.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gem.bean.BudgetDataBean;
import com.gem.daoImpl.AllClientInfoNeededDaoImpl;
import com.gem.daoImpl.CassandraCRUDImpl;
import com.gem.daoImpl.HistoricalAnalysisDaoImpl;
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
import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.client.reporting.ReportingConfiguration;
import com.google.api.ads.adwords.lib.factory.AdWordsServicesInterface;
import com.google.api.ads.adwords.lib.jaxb.v201702.DownloadFormat;
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponse;
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponseException;
import com.google.api.ads.adwords.lib.utils.ReportException;
import com.google.api.ads.adwords.lib.utils.v201702.ReportDownloaderInterface;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.repackaged.com.google.common.base.Splitter;
import com.google.api.client.util.Charsets;

public class App{
	static List<Map<String, Object>> urlList = null;

	static List<Map<String, Object>> list = null;

	static String clientId = "";
	Scanner scan = null;
	static CassandraCRUDImpl daos = null;
	static HistoricalAnalysisDaoImpl historyDao = null;

	private static String OAuthClientId;
	private static String OAuthClientSecret;
	private static String developerToken;

	public static void main(String[] args) throws Exception {

		Properties property = new Properties();

		InputStream input = null;

		try {

			String filename = "ads.properties";
			input = App.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				System.out.println("Sorry, unable to find " + filename);
				return;
			}
			property.load(input);

			OAuthClientId = property.getProperty("OAuthClientId");
			OAuthClientSecret = property.getProperty("OAuthClientSecret");
			developerToken = property.getProperty("DeveloperToken");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		AbstractApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("spring.xml");

			context.registerShutdownHook();

			AllClientInfoNeededDaoImpl dao = context.getBean("dao", AllClientInfoNeededDaoImpl.class);

			daos = context.getBean("daoCassendra", CassandraCRUDImpl.class);

			historyDao = context.getBean("History", HistoricalAnalysisDaoImpl.class);

			String clientId = "";
			Scanner scan = null;

			if (args.length > 0 && args[0].equalsIgnoreCase("specificClientId")) {
				System.out.println("Enter the client id in order to obtain the information : ");
				scan = new Scanner(System.in);
				clientId = scan.next();
			}

			if (args.length > 0 && args[0].equalsIgnoreCase("specificClientId")) {
				list = dao.getAllClientInfo(clientId);
			} else {
				list = dao.getAllClientInfo();
			}

			urlList = dao.createURLFromAdwordsData(list, clientId);

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < urlList.size(); i++) {
			AdWordsSession session = null;
			try {

				BudgetDataBean budgetDataBean = (BudgetDataBean) urlList.get(i).get("dataBean" + i);
				String refreshToken = (String) urlList.get(i).get("refreshToken" + i);

				Credential oAuth2Credential = new OfflineCredentials.Builder().forApi(Api.ADWORDS)
						.withClientSecrets(OAuthClientId, OAuthClientSecret).withRefreshToken(refreshToken).build()
						.generateCredential();

				session = new AdWordsSession.Builder().withClientCustomerId(budgetDataBean.getClient_customer_id())
						.withDeveloperToken(developerToken).withOAuth2Credential(oAuth2Credential).build();

				AdWordsServicesInterface adWordsServices = AdWordsServices.getInstance();

				// Alan! This makes connection with the Cassandra cluster.
				daos.getConection();

				// ---------------------------------------------------------------------------------------

				List<DailyAccountPerformanceBean> dailyAccountPerformanceList = dailyAccountPerformance(adWordsServices,
						session, budgetDataBean);

				daos.writeDataToAccountPerformanceToCassandraDaily(dailyAccountPerformanceList, budgetDataBean);

				// --------------------------------------------------------------------------------------

				List<DailyAdNetworkPerformanceBean> dailyAdNetworkPerformanceList = dailyAdNetworkPerformance(
						adWordsServices, session, budgetDataBean);

				daos.writeDataToAdNetworkAccountPerformanceCassandraDaily(dailyAdNetworkPerformanceList,
						budgetDataBean);

				// --------------------------------------------------------------------------------------

				List<DailyCampaignPerformanceBean> dailyCampaignPerformanceList = dailyCampaignPerformance(
						adWordsServices, session, budgetDataBean);

				daos.writeDataToCampaignAccountPerformanceCassandraDaily(dailyCampaignPerformanceList, budgetDataBean);

				// -------------------------------------------------------------------------------

				// new tables
				List<DailyKeywordPerformanceBean> daily_keyword_performance_report = daily_keyword_performance_report_for_last_30days(
						adWordsServices, session, budgetDataBean);
				daos.writeDataToKeywordPerformanceCassandraDaily(daily_keyword_performance_report, budgetDataBean);

				List<DailyLast_30_campaign_performance> daily_last_30days_campaign_performance = daily_last_30days_campaign_performance(
						adWordsServices, session, budgetDataBean);
				daos.writeDataToDailyLast_30_campaign_performance_CassandraDaily(daily_last_30days_campaign_performance,
						budgetDataBean);

				// -------------------------------------------------------------------------------
				List<WeeklyAccountPerformanceBean> weeklyAccountPerformanceList = weeklyAccountPerformance(
						adWordsServices, session, budgetDataBean);

				daos.writeDataToAccountPerformanceToCassandraWeekly(weeklyAccountPerformanceList, budgetDataBean);

				// -------------------------------------------------------------------------------
				List<WeeklyAdNetworkPerformanceBean> weeklyAdnetworkPerformanceList = weeklyAdnetworkPerformance(
						adWordsServices, session, budgetDataBean);

				daos.writeDataToAdNetworkAccountPerformanceCassandraWeekly(weeklyAdnetworkPerformanceList,
						budgetDataBean);

				// -------------------------------------------------------------------------------
				List<WeeklyCampaignPerformanceBean> weeklyCampaignPerformanceList = weeklyCampaignPerformance(
						adWordsServices, session, budgetDataBean);

				daos.writeDataToCampaignAccountPerformanceCassandraWeekly(weeklyCampaignPerformanceList,
						budgetDataBean);

				// -------------------------------------------------------------------------------
				List<MonthlyAccountPerformanceBean> monthlyAccountPerformanceList = monthlyAccountPerformance(
						adWordsServices, session, budgetDataBean);

				daos.writeDataToAccountPerformanceToCassandraMonthly(monthlyAccountPerformanceList, budgetDataBean);

				// -------------------------------------------------------------------------------
				List<MonthlyAdNetworkPerformanceBean> monthlyAdnetworkPerformanceList = monthlyAdnetworkPerformance(
						adWordsServices, session, budgetDataBean);

				daos.writeDataToAdNetworkAccountPerformanceCassandraMonthly(monthlyAdnetworkPerformanceList,
						budgetDataBean);

				// -------------------------------------------------------------------------------
				List<MonthlyCampaignPerformanceBean> monthlyCampaignPerformanceList = monthlyCampaignPerformance(
						adWordsServices, session, budgetDataBean);

				daos.writeDataToCampaignAccountPerformanceCassandraMonthly(monthlyCampaignPerformanceList,
						budgetDataBean);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				daos.closeSessionAndCluster();
				historyDao.closeSessionAndCluster();
				context.close();
			}
		}

	}

	public static List<MonthlyAccountPerformanceBean> monthlyAccountPerformance(
			AdWordsServicesInterface adWordsServices, AdWordsSession session, BudgetDataBean budgetDataBean) {

		List<MonthlyAccountPerformanceBean> monthlyAccountPerformanceBeanList = new ArrayList<MonthlyAccountPerformanceBean>();

		final String monthly_Account_performance_query = "SELECT Month,ExternalCustomerId,"
				+ "AccountDescriptiveName,AccountCurrencyCode," + "AccountTimeZone,"
				+ "AveragePosition,Conversions,Impressions,Clicks," + "Ctr,Cost,CostPerConversion,AllConversionRate,"
				+ "VideoViews,VideoViewRate,AverageCpv,ViewThroughConversions,"
				+ "SearchBudgetLostImpressionShare,SearchExactMatchImpressionShare,"
				+ "SearchImpressionShare,SearchRankLostImpressionShare,InvalidClicks,"
				+ "InvalidClickRate,AllConversionValue" + " FROM ACCOUNT_PERFORMANCE_REPORT DURING LAST_" + "30"
				+ "_DAYS";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader.downloadReport(monthly_Account_performance_query,
					DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);

			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {

					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(10));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(11));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					MonthlyAccountPerformanceBean monthlyAccountPerformanceBean = new MonthlyAccountPerformanceBean();
					monthlyAccountPerformanceBean.setMonth(strPerRow.get(0));
					monthlyAccountPerformanceBean.setExternal_customer_id(strPerRow.get(1));
					monthlyAccountPerformanceBean.setAccount_descriptive_name(strPerRow.get(2));
					monthlyAccountPerformanceBean.setAccount_currency_code(strPerRow.get(3));
					monthlyAccountPerformanceBean.setAccount_timezone_id(strPerRow.get(4));
					monthlyAccountPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(5)));
					monthlyAccountPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(6)));
					monthlyAccountPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(7)));
					monthlyAccountPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(8)));
					monthlyAccountPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(9)));
					monthlyAccountPerformanceBean.setCost(GBPvalue);
					monthlyAccountPerformanceBean.setCost_per_conversion(value);
					monthlyAccountPerformanceBean.setAll_conversion_rate(Float.parseFloat(strPerRow.get(12)));
					monthlyAccountPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(13)));
					monthlyAccountPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(14)));
					monthlyAccountPerformanceBean.setAverage_cpv(Float.parseFloat(strPerRow.get(15)));
					monthlyAccountPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(16)));
					monthlyAccountPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(17)));
					monthlyAccountPerformanceBean
							.setSearch_exactmatch_impression_share(Float.parseFloat(strPerRow.get(18)));
					monthlyAccountPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(19)));
					monthlyAccountPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(20)));
					monthlyAccountPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(21)));
					monthlyAccountPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(22)));
					monthlyAccountPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(23)));

					monthlyAccountPerformanceBeanList.add(monthlyAccountPerformanceBean);
					sizeOfLine++;
				} catch (Exception e) {

					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}

			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Monthly Account Performance> ");

		} catch (ReportException e) {

			e.printStackTrace();
		} catch (ReportDownloadResponseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return monthlyAccountPerformanceBeanList;

	}

	public static List<MonthlyAdNetworkPerformanceBean> monthlyAdnetworkPerformance(
			AdWordsServicesInterface adWordsServices, AdWordsSession session, BudgetDataBean budgetDataBean) {

		List<MonthlyAdNetworkPerformanceBean> monthlyAdNetworkPerformanceBeanList = new ArrayList<MonthlyAdNetworkPerformanceBean>();

		final String monthly_Ad_network_Performance_query = "SELECT  Month,ExternalCustomerId,"
				+ "AdNetworkType1,AccountDescriptiveName,AccountCurrencyCode," + "AccountTimeZone,"
				+ "AveragePosition,Conversions," + "Impressions,Clicks,Ctr,Cost,"
				+ "CostPerConversion,AllConversionRate,VideoViews," + "VideoViewRate,AverageCpv,ViewThroughConversions,"
				+ "SearchBudgetLostImpressionShare,SearchExactMatchImpressionShare,"
				+ "SearchImpressionShare,SearchRankLostImpressionShare,InvalidClicks,"
				+ "InvalidClickRate,AllConversionValue" + " FROM ACCOUNT_PERFORMANCE_REPORT " + "DURING LAST_" + "30"
				+ "_DAYS";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader
					.downloadReport(monthly_Ad_network_Performance_query, DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// //System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {
					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(11));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(12));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					MonthlyAdNetworkPerformanceBean monthlyAdNetworkPerformanceBean = new MonthlyAdNetworkPerformanceBean();
					monthlyAdNetworkPerformanceBean.setMonth(strPerRow.get(0));
					monthlyAdNetworkPerformanceBean.setExternal_customer_id(strPerRow.get(1));
					monthlyAdNetworkPerformanceBean.setAd_network_type1(strPerRow.get(2));
					monthlyAdNetworkPerformanceBean.setAccount_descriptive_name(strPerRow.get(3));
					monthlyAdNetworkPerformanceBean.setAccount_currency_code(strPerRow.get(4));
					monthlyAdNetworkPerformanceBean.setAccount_timezone_id(strPerRow.get(5));
					monthlyAdNetworkPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(6)));
					monthlyAdNetworkPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(7)));
					monthlyAdNetworkPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(8)));
					monthlyAdNetworkPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(9)));
					monthlyAdNetworkPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(10)));
					monthlyAdNetworkPerformanceBean.setCost(GBPvalue);
					monthlyAdNetworkPerformanceBean.setCost_per_conversion(value);
					monthlyAdNetworkPerformanceBean.setAll_conversion_rate(Float.parseFloat(strPerRow.get(13)));
					monthlyAdNetworkPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(14)));
					monthlyAdNetworkPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(15)));
					monthlyAdNetworkPerformanceBean.setAverage_cpv(Float.parseFloat(strPerRow.get(16)));
					monthlyAdNetworkPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(17)));
					monthlyAdNetworkPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(18)));
					monthlyAdNetworkPerformanceBean
							.setSearch_exactmatch_impression_share(Float.parseFloat(strPerRow.get(19)));
					monthlyAdNetworkPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(20)));
					monthlyAdNetworkPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(21)));
					monthlyAdNetworkPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(22)));
					monthlyAdNetworkPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(23)));
					monthlyAdNetworkPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(24)));

					monthlyAdNetworkPerformanceBeanList.add(monthlyAdNetworkPerformanceBean);
					sizeOfLine++;
				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Monthly Ad Network Performance> ");

		} catch (ReportException e) {

			e.printStackTrace();
		} catch (ReportDownloadResponseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
		return monthlyAdNetworkPerformanceBeanList;
	}

	public static List<MonthlyCampaignPerformanceBean> monthlyCampaignPerformance(
			AdWordsServicesInterface adWordsServices, AdWordsSession session, BudgetDataBean budgetDataBean) {

		List<MonthlyCampaignPerformanceBean> monthlyCampaignPerformanceBeanList = new ArrayList<MonthlyCampaignPerformanceBean>();

		final String monthly_Campaign_Performance_query = "SELECT Month,AccountCurrencyCode,"
				+ "AdvertisingChannelType,AccountDescriptiveName," + "AccountTimeZone,"
				+ "CampaignId,CampaignName,CampaignStatus,Cost," + "Impressions,Clicks,Ctr,Conversions,"
				+ "ConversionRate,CostPerConversion,VideoViews," + "VideoViewRate,ViewThroughConversions,InvalidClicks,"
				+ "InvalidClickRate,ImpressionReach,SearchBudgetLostImpressionShare,"
				+ "SearchExactMatchImpressionShare,SearchImpressionShare,"
				+ "SearchRankLostImpressionShare,AveragePosition,AverageTimeOnSite,"
				+ "BounceRate,ClickAssistedConversions,AllConversionValue" + " FROM CAMPAIGN_PERFORMANCE_REPORT "
				+ " WHERE CampaignStatus IN [ENABLED] DURING LAST_" + "30" + "_DAYS";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader.downloadReport(monthly_Campaign_Performance_query,
					DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {

					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(8));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(14));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					MonthlyCampaignPerformanceBean monthlyCampaignPerformanceBean = new MonthlyCampaignPerformanceBean();
					monthlyCampaignPerformanceBean.setMonth(strPerRow.get(0));
					monthlyCampaignPerformanceBean.setAccount_currency_code(strPerRow.get(1));
					monthlyCampaignPerformanceBean.setAdvertising_channel_type(strPerRow.get(2));
					monthlyCampaignPerformanceBean.setAccount_descriptive_name(strPerRow.get(3));
					monthlyCampaignPerformanceBean.setAccount_timezone_id(strPerRow.get(4));
					monthlyCampaignPerformanceBean.setCampaign_id(strPerRow.get(5));
					monthlyCampaignPerformanceBean.setCampaign_name(strPerRow.get(6));
					monthlyCampaignPerformanceBean.setCampaign_status(strPerRow.get(7));
					monthlyCampaignPerformanceBean.setCost(GBPvalue);
					monthlyCampaignPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(9)));
					monthlyCampaignPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(10)));
					monthlyCampaignPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(11)));
					monthlyCampaignPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(12)));
					monthlyCampaignPerformanceBean.setConversion_rate(Float.parseFloat(strPerRow.get(13)));
					monthlyCampaignPerformanceBean.setCost_per_conversion(value);
					monthlyCampaignPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(15)));
					monthlyCampaignPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(16)));
					monthlyCampaignPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(17)));
					monthlyCampaignPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(18)));
					monthlyCampaignPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(19)));
					monthlyCampaignPerformanceBean.setImpression_reach(strPerRow.get(20));

					monthlyCampaignPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(21)));
					monthlyCampaignPerformanceBean
							.setSearch_exact_match_impression_share(Float.parseFloat(strPerRow.get(22)));
					monthlyCampaignPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(23)));
					monthlyCampaignPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(24)));
					monthlyCampaignPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(25)));
					monthlyCampaignPerformanceBean.setAverage_time_on_site(Float.parseFloat(strPerRow.get(26)));

					monthlyCampaignPerformanceBean.setBounce_rate(Float.parseFloat(strPerRow.get(27)));

					monthlyCampaignPerformanceBean.setClick_assisted_conversions(Float.parseFloat(strPerRow.get(28)));

					monthlyCampaignPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(29)));

					monthlyCampaignPerformanceBeanList.add(monthlyCampaignPerformanceBean);
					sizeOfLine++;

				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Monthly Campaign Performance> ");

		} catch (ReportException e) {

			e.printStackTrace();
		} catch (ReportDownloadResponseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
		return monthlyCampaignPerformanceBeanList;
	}

	public static List<WeeklyAccountPerformanceBean> weeklyAccountPerformance(AdWordsServicesInterface adWordsServices,
			AdWordsSession session, BudgetDataBean budgetDataBean) {

		List<WeeklyAccountPerformanceBean> listWeeklyAccountPerformance = new ArrayList<WeeklyAccountPerformanceBean>();

		final String weekly_Account_Performance_Query = "SELECT Week,ExternalCustomerId,AccountDescriptiveName,"
				+ "AccountTimeZone," + " AccountCurrencyCode,AveragePosition,Conversions,"
				+ " Clicks,Ctr,Cost,CostPerConversion,"
				+ " AllConversionRate,VideoViews,VideoViewRate,AverageCpv,ViewThroughConversions,"
				+ " SearchBudgetLostImpressionShare,SearchExactMatchImpressionShare,SearchImpressionShare,"
				+ " SearchRankLostImpressionShare , InvalidClicks , InvalidClickRate , AllConversionValue,Impressions "
				+ " FROM ACCOUNT_PERFORMANCE_REPORT DURING LAST_7_DAYS";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader.downloadReport(weekly_Account_Performance_Query,
					DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {
					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(9));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(10));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					WeeklyAccountPerformanceBean weeklyAccountPerformanceBean = new WeeklyAccountPerformanceBean();
					weeklyAccountPerformanceBean.setWeek(strPerRow.get(0));
					weeklyAccountPerformanceBean.setExternal_customer_id(strPerRow.get(1));
					weeklyAccountPerformanceBean.setAccount_descriptive_name(strPerRow.get(2));
					weeklyAccountPerformanceBean.setAccount_currency_code(strPerRow.get(3));
					weeklyAccountPerformanceBean.setAccount_timezone_id(strPerRow.get(4));
					weeklyAccountPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(5)));
					weeklyAccountPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(6)));
					weeklyAccountPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(7)));
					weeklyAccountPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(8)));
					weeklyAccountPerformanceBean.setCost(GBPvalue);
					weeklyAccountPerformanceBean.setCost_per_conversion(value);
					weeklyAccountPerformanceBean.setAll_conversion_rate(Float.parseFloat(strPerRow.get(11)));
					weeklyAccountPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(12)));
					weeklyAccountPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(13)));
					weeklyAccountPerformanceBean.setAverage_cpv(Float.parseFloat(strPerRow.get(14)));
					weeklyAccountPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(15)));
					weeklyAccountPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(16)));
					weeklyAccountPerformanceBean
							.setSearch_exactmatch_impression_share(Float.parseFloat(strPerRow.get(17)));
					weeklyAccountPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(18)));
					weeklyAccountPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(19)));
					weeklyAccountPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(20)));
					weeklyAccountPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(21)));
					weeklyAccountPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(22)));
					weeklyAccountPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(23)));

					listWeeklyAccountPerformance.add(weeklyAccountPerformanceBean);
					sizeOfLine++;

				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Weekly Account Performance> ");

		} catch (ReportException e) {

			e.printStackTrace();
		} catch (ReportDownloadResponseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
		return listWeeklyAccountPerformance;
	}

	public static List<WeeklyAdNetworkPerformanceBean> weeklyAdnetworkPerformance(
			AdWordsServicesInterface adWordsServices, AdWordsSession session, BudgetDataBean budgetDataBean) {

		List<WeeklyAdNetworkPerformanceBean> weeklyAdNetworkPerformanceBeanList = new ArrayList<WeeklyAdNetworkPerformanceBean>();

		final String weekly_Ad_Network_Performance_Query = "SELECT Week,ExternalCustomerId,AdNetworkType1,AccountDescriptiveName,"
				+ "AccountTimeZone," + "AccountCurrencyCode,AveragePosition,Conversions,"
				+ "Impressions,Clicks,Ctr,Cost,CostPerConversion,"
				+ "AllConversionRate,VideoViews,VideoViewRate,AverageCpv,ViewThroughConversions,"
				+ "SearchBudgetLostImpressionShare,SearchExactMatchImpressionShare,SearchImpressionShare,"
				+ "SearchRankLostImpressionShare,InvalidClicks,InvalidClickRate,AllConversionValue"
				+ " FROM ACCOUNT_PERFORMANCE_REPORT  DURING LAST_7_DAYS";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader.downloadReport(weekly_Ad_Network_Performance_Query,
					DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {

					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(11));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(12));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					WeeklyAdNetworkPerformanceBean weeklyAdNetworkPerformanceBean = new WeeklyAdNetworkPerformanceBean();
					weeklyAdNetworkPerformanceBean.setWeek(strPerRow.get(0));
					weeklyAdNetworkPerformanceBean.setExternal_customer_id(strPerRow.get(1));
					weeklyAdNetworkPerformanceBean.setAd_network_type1(strPerRow.get(2));
					weeklyAdNetworkPerformanceBean.setAccount_descriptive_name(strPerRow.get(3));
					weeklyAdNetworkPerformanceBean.setAccount_timezone_id(strPerRow.get(4));
					weeklyAdNetworkPerformanceBean.setAccount_currency_code(strPerRow.get(5));
					weeklyAdNetworkPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(6)));
					weeklyAdNetworkPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(7)));
					weeklyAdNetworkPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(8)));
					weeklyAdNetworkPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(9)));
					weeklyAdNetworkPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(10)));
					weeklyAdNetworkPerformanceBean.setCost(GBPvalue);
					weeklyAdNetworkPerformanceBean.setCost_per_conversion(value);
					weeklyAdNetworkPerformanceBean.setAll_conversion_rate(Float.parseFloat(strPerRow.get(13)));
					weeklyAdNetworkPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(14)));
					weeklyAdNetworkPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(15)));
					weeklyAdNetworkPerformanceBean.setAverage_cpv(Float.parseFloat(strPerRow.get(16)));
					weeklyAdNetworkPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(17)));
					weeklyAdNetworkPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(18)));
					weeklyAdNetworkPerformanceBean
							.setSearch_exactmatch_impression_share(Float.parseFloat(strPerRow.get(19)));
					weeklyAdNetworkPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(20)));
					weeklyAdNetworkPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(21)));
					weeklyAdNetworkPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(22)));
					weeklyAdNetworkPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(23)));
					weeklyAdNetworkPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(24)));

					weeklyAdNetworkPerformanceBeanList.add(weeklyAdNetworkPerformanceBean);
					sizeOfLine++;
				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ "  Table Name :-  <Weekly Ad Network Performance> ");

		} catch (ReportException e) {

			e.printStackTrace();
		} catch (ReportDownloadResponseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
		return weeklyAdNetworkPerformanceBeanList;

	}

	public static List<WeeklyCampaignPerformanceBean> weeklyCampaignPerformance(
			AdWordsServicesInterface adWordsServices, AdWordsSession session, BudgetDataBean budgetDataBean) {

		List<WeeklyCampaignPerformanceBean> weeklyCampaignPerformanceBeanList = new ArrayList<WeeklyCampaignPerformanceBean>();

		final String weekly_campaign_performance_query = "SELECT  Week,AccountCurrencyCode,AdvertisingChannelType,"
				+ "AccountDescriptiveName," + "AccountTimeZone," + "CampaignId,CampaignName,"
				+ "CampaignStatus,Cost,Impressions,Clicks,Ctr,"
				+ "Conversions,ConversionRate,CostPerConversion,VideoViews,"
				+ "VideoViewRate,ViewThroughConversions,InvalidClicks,InvalidClickRate,"
				+ "ImpressionReach,SearchBudgetLostImpressionShare,SearchExactMatchImpressionShare,"
				+ "SearchImpressionShare,SearchRankLostImpressionShare,AveragePosition,"
				+ "AverageTimeOnSite,BounceRate,ClickAssistedConversions," + "AllConversionValue "
				+ "FROM CAMPAIGN_PERFORMANCE_REPORT " + "WHERE CampaignStatus IN [ENABLED] " + " DURING LAST_7_DAYS";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader.downloadReport(weekly_campaign_performance_query,
					DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {
				try {
					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(8));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(14));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					WeeklyCampaignPerformanceBean weeklyCampaignPerformanceBean = new WeeklyCampaignPerformanceBean();
					weeklyCampaignPerformanceBean.setWeek(strPerRow.get(0));
					weeklyCampaignPerformanceBean.setAccount_currency_code(strPerRow.get(1));
					weeklyCampaignPerformanceBean.setAdvertising_channel_type(strPerRow.get(2));
					weeklyCampaignPerformanceBean.setAccount_descriptive_name(strPerRow.get(3));
					weeklyCampaignPerformanceBean.setAccount_timezone_id(strPerRow.get(4));
					weeklyCampaignPerformanceBean.setCampaign_id(strPerRow.get(5));
					weeklyCampaignPerformanceBean.setCampaign_name(strPerRow.get(6));
					weeklyCampaignPerformanceBean.setCampaign_status(strPerRow.get(7));
					weeklyCampaignPerformanceBean.setCost(GBPvalue);
					weeklyCampaignPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(9)));
					weeklyCampaignPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(10)));
					weeklyCampaignPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(11)));
					weeklyCampaignPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(12)));
					weeklyCampaignPerformanceBean.setConversion_rate(Float.parseFloat(strPerRow.get(13)));
					weeklyCampaignPerformanceBean.setCost_per_conversion(value);
					weeklyCampaignPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(15)));
					weeklyCampaignPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(16)));
					weeklyCampaignPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(17)));
					weeklyCampaignPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(18)));
					weeklyCampaignPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(19)));
					weeklyCampaignPerformanceBean.setImpression_reach(strPerRow.get(20));

					weeklyCampaignPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(21)));
					weeklyCampaignPerformanceBean
							.setSearch_exact_match_impression_share(Float.parseFloat(strPerRow.get(22)));
					weeklyCampaignPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(23)));
					weeklyCampaignPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(24)));
					weeklyCampaignPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(25)));
					weeklyCampaignPerformanceBean.setAverage_time_on_site(Float.parseFloat(strPerRow.get(26)));

					weeklyCampaignPerformanceBean.setBounce_rate(Float.parseFloat(strPerRow.get(27)));

					weeklyCampaignPerformanceBean.setClick_assisted_conversions(Float.parseFloat(strPerRow.get(28)));

					weeklyCampaignPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(29)));

					weeklyCampaignPerformanceBeanList.add(weeklyCampaignPerformanceBean);
					sizeOfLine++;
				} catch (Exception e) {

					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Weekly Campaign Performance> ");

		} catch (ReportException e) {

			e.printStackTrace();
		} catch (ReportDownloadResponseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
		return weeklyCampaignPerformanceBeanList;

	}

	// daily_last_30days_keyword_performance_report

	public static List<DailyKeywordPerformanceBean> daily_keyword_performance_report_for_last_30days(
			AdWordsServicesInterface adWordsServices, AdWordsSession session, BudgetDataBean budgetDataBean)
			throws Exception {

		List<DailyKeywordPerformanceBean> daily_keyword_performance_report_list = new ArrayList<DailyKeywordPerformanceBean>();

		final String query = "SELECT " + "AdGroupId, AveragePosition, AverageTimeOnSite, BounceRate, " + "CampaignId,"
				+ "Clicks, ExternalCustomerId, Conversions, Cost , CostPerConversion , Impressions , Criteria , KeywordMatchType , CampaignName , Ctr "
				+ " FROM KEYWORDS_PERFORMANCE_REPORT " + "DURING LAST_30_DAYS";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();

		try {

			ReportDownloadResponse response = reportDownloader.downloadReport(query, DownloadFormat.CSV);

			response.getInputStream();

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {
				try {

					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(8));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(9));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					DailyKeywordPerformanceBean daily_keyword_performance_report = new DailyKeywordPerformanceBean();

					daily_keyword_performance_report.setClient_stamp(budgetDataBean.getClient_stamp());
					daily_keyword_performance_report.setAd_group(strPerRow.get(0));
					daily_keyword_performance_report.setAverage_position(Float.parseFloat(strPerRow.get(1)));
					daily_keyword_performance_report.setAverage_visit_duration(Float.parseFloat(strPerRow.get(2)));
					daily_keyword_performance_report.setBounce_rate(Float.parseFloat(strPerRow.get(3)));
					daily_keyword_performance_report.setCampaign(strPerRow.get(4));
					daily_keyword_performance_report.setClicks(Integer.parseInt(strPerRow.get(5)));
					daily_keyword_performance_report.setClient_customer_id(strPerRow.get(6));
					daily_keyword_performance_report.setConversions(Float.parseFloat(strPerRow.get(7)));
					daily_keyword_performance_report.setCost(GBPvalue);
					daily_keyword_performance_report.setCost_per_conversion(value);
					daily_keyword_performance_report.setImpressions(Float.parseFloat(strPerRow.get(10)));
					daily_keyword_performance_report.setKeyword(strPerRow.get(11));
					daily_keyword_performance_report.setMatch_type(strPerRow.get(12));
					daily_keyword_performance_report.setCampaign_name(strPerRow.get(13));
					daily_keyword_performance_report.setCtr(strPerRow.get(14));

					daily_keyword_performance_report_list.add(daily_keyword_performance_report);
					sizeOfLine++;
				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Daily Keyword Performance [Last 30 days]> ");

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return daily_keyword_performance_report_list;

	}

	// daily_last_30days_campaign_performance

	public static List<DailyLast_30_campaign_performance> daily_last_30days_campaign_performance(
			AdWordsServicesInterface adWordsServices, AdWordsSession session, BudgetDataBean budgetDataBean) {

		List<DailyLast_30_campaign_performance> DailyLast_30_campaign_performanceList = new ArrayList<DailyLast_30_campaign_performance>();

		String daily_last_30days_campaign_performance = "SELECT AccountCurrencyCode," + "AccountTimeZone,"
				+ "AdvertisingChannelType,AccountDescriptiveName," + "CampaignId,CampaignName,CampaignStatus,Cost,"
				+ "Impressions,Clicks,Ctr,Conversions," + "ConversionRate,CostPerConversion,VideoViews,VideoViewRate,"
				+ "ViewThroughConversions,InvalidClicks,InvalidClickRate,ImpressionReach,"
				+ "SearchBudgetLostImpressionShare,SearchExactMatchImpressionShare,SearchImpressionShare,"
				+ "SearchRankLostImpressionShare,AveragePosition,AverageTimeOnSite,BounceRate,"
				+ "ClickAssistedConversions,AllConversionValue " + "FROM CAMPAIGN_PERFORMANCE_REPORT "
				+ "WHERE CampaignStatus IN [ENABLED] " + " DURING LAST_30_DAYS";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();

		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader
					.downloadReport(daily_last_30days_campaign_performance, DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {

					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(7));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(13));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					DailyLast_30_campaign_performance dailyLast_30_campaign_performance = new DailyLast_30_campaign_performance();
					dailyLast_30_campaign_performance.setAccount_currency_code(strPerRow.get(0));
					dailyLast_30_campaign_performance.setAdvertising_channel_type(strPerRow.get(1));
					dailyLast_30_campaign_performance.setAccount_descriptive_name(strPerRow.get(2));
					dailyLast_30_campaign_performance.setAccount_timezone_id(strPerRow.get(3));
					dailyLast_30_campaign_performance.setCampaign_id(strPerRow.get(4));
					dailyLast_30_campaign_performance.setCampaign_name(strPerRow.get(5));
					dailyLast_30_campaign_performance.setCampaign_status(strPerRow.get(6));
					dailyLast_30_campaign_performance.setCost(GBPvalue);
					dailyLast_30_campaign_performance.setImpressions(Long.parseLong(strPerRow.get(8)));
					dailyLast_30_campaign_performance.setClicks(Integer.parseInt(strPerRow.get(9)));
					dailyLast_30_campaign_performance.setCtr(Float.parseFloat(strPerRow.get(10)));
					dailyLast_30_campaign_performance.setConversions(Float.parseFloat(strPerRow.get(11)));
					dailyLast_30_campaign_performance.setConversion_rate(Float.parseFloat(strPerRow.get(12)));
					dailyLast_30_campaign_performance.setCost_per_conversion(value);
					dailyLast_30_campaign_performance.setVideo_views(Integer.parseInt(strPerRow.get(14)));
					dailyLast_30_campaign_performance.setVideo_view_rate(Float.parseFloat(strPerRow.get(15)));
					dailyLast_30_campaign_performance.setView_through_conversions(Integer.parseInt(strPerRow.get(16)));
					dailyLast_30_campaign_performance.setInvalid_clicks(Integer.parseInt(strPerRow.get(17)));
					dailyLast_30_campaign_performance.setInvalid_click_rate(Float.parseFloat(strPerRow.get(18)));
					dailyLast_30_campaign_performance.setImpression_reach(strPerRow.get(19));

					dailyLast_30_campaign_performance
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(20)));
					dailyLast_30_campaign_performance
							.setSearch_exact_match_impression_share(Float.parseFloat(strPerRow.get(21)));
					dailyLast_30_campaign_performance.setSearch_impression_share(Float.parseFloat(strPerRow.get(22)));
					dailyLast_30_campaign_performance
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(23)));
					dailyLast_30_campaign_performance.setAverage_position(Float.parseFloat(strPerRow.get(24)));
					dailyLast_30_campaign_performance.setAverage_time_on_site(Float.parseFloat(strPerRow.get(25)));

					dailyLast_30_campaign_performance.setBounce_rate(Float.parseFloat(strPerRow.get(26)));

					dailyLast_30_campaign_performance
							.setClick_assisted_conversions(Float.parseFloat(strPerRow.get(27)));

					dailyLast_30_campaign_performance.setAll_conversion_value(Float.parseFloat(strPerRow.get(28)));

					DailyLast_30_campaign_performanceList.add(dailyLast_30_campaign_performance);
					sizeOfLine++;
				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println("Adwords Id is wrong with  : Client Stamp  -- >"
								+ budgetDataBean.getClient_stamp() + " & Company Name -- >"
								+ budgetDataBean.getCompany_name() + "& Date -->" + budgetDataBean.getDate());
					}
				} finally {

				}
			}
			System.out.println();

			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ReportException e) {
			e.printStackTrace();
		} catch (ReportDownloadResponseException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return DailyLast_30_campaign_performanceList;
	}

	public static List<DailyAccountPerformanceBean> dailyAccountPerformance(AdWordsServicesInterface adWordsServices,
			AdWordsSession session, BudgetDataBean budgetDataBean) throws Exception {

		List<DailyAccountPerformanceBean> dailyAccountPerformanceBeanList = new ArrayList<DailyAccountPerformanceBean>();

		final String query = "SELECT " + "Week, ExternalCustomerId, AccountDescriptiveName, AccountCurrencyCode, "
				+ "AccountTimeZone,"
				+ "AveragePosition, Conversions, Clicks, Ctr, Cost, CostPerConversion, AllConversionRate, "
				+ "VideoViews, VideoViewRate, AverageCpv, ViewThroughConversions, SearchBudgetLostImpressionShare, "
				+ "SearchExactMatchImpressionShare, SearchImpressionShare, SearchRankLostImpressionShare, InvalidClicks, "
				+ "InvalidClickRate, AllConversionValue, Impressions " + "FROM ACCOUNT_PERFORMANCE_REPORT "
				+ "DURING YESTERDAY";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			ReportDownloadResponse response = reportDownloader.downloadReport(query, DownloadFormat.CSV);

			response.getInputStream();

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {

					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(9));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(10));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					DailyAccountPerformanceBean dailyAccountPerformanceBean = new DailyAccountPerformanceBean();
					dailyAccountPerformanceBean.setWeek(strPerRow.get(0));
					dailyAccountPerformanceBean.setExternal_customer_id(strPerRow.get(1));
					dailyAccountPerformanceBean.setAccount_descriptive_name(strPerRow.get(2));
					dailyAccountPerformanceBean.setAccount_currency_code(strPerRow.get(3));
					dailyAccountPerformanceBean.setAccount_timezone_id(strPerRow.get(4));
					dailyAccountPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(5)));
					dailyAccountPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(6)));
					dailyAccountPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(7)));
					dailyAccountPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(8)));
					dailyAccountPerformanceBean.setCost(GBPvalue);
					dailyAccountPerformanceBean.setCost_per_conversion(value);
					dailyAccountPerformanceBean.setAll_conversion_rate(Float.parseFloat(strPerRow.get(11)));
					dailyAccountPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(12)));
					dailyAccountPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(13)));
					dailyAccountPerformanceBean.setAverage_cpv(Float.parseFloat(strPerRow.get(14)));
					dailyAccountPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(15)));
					dailyAccountPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(16)));
					dailyAccountPerformanceBean
							.setSearch_exactmatch_impression_share(Float.parseFloat(strPerRow.get(17)));
					dailyAccountPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(18)));
					dailyAccountPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(19)));
					dailyAccountPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(20)));
					dailyAccountPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(21)));
					dailyAccountPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(22)));
					dailyAccountPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(23)));

					dailyAccountPerformanceBeanList.add(dailyAccountPerformanceBean);
					sizeOfLine++;
				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Daily Account Performance> ");

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return dailyAccountPerformanceBeanList;
	}

	public static List<DailyAdNetworkPerformanceBean> dailyAdNetworkPerformance(
			AdWordsServicesInterface adWordsServices, AdWordsSession session, BudgetDataBean budgetDataBean)
			throws Exception {

		List<DailyAdNetworkPerformanceBean> dailyAdNetworkPerformanceBeanList = new ArrayList<DailyAdNetworkPerformanceBean>();

		final String daily_account_ad_network_performance = "SELECT  Date,Week,ExternalCustomerId,"
				+ "AccountDescriptiveName,AccountCurrencyCode," + "AccountTimeZone,"
				+ "AveragePosition,Conversions,Impressions,Clicks," + "Ctr,Cost,CostPerConversion,AllConversionRate,"
				+ "VideoViews,VideoViewRate,AverageCpv,ViewThroughConversions,"
				+ "SearchBudgetLostImpressionShare,SearchExactMatchImpressionShare,SearchImpressionShare,"
				+ "SearchRankLostImpressionShare,InvalidClicks,InvalidClickRate," + "AllConversionValue,AdNetworkType1 "
				+ "FROM ACCOUNT_PERFORMANCE_REPORT " 
				+ "DURING YESTERDAY";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();
		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader
					.downloadReport(daily_account_ad_network_performance, DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {

					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(11));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(12));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					DailyAdNetworkPerformanceBean dailyAdNetworkPerformanceBean = new DailyAdNetworkPerformanceBean();
					
					dailyAdNetworkPerformanceBean.setDate(strPerRow.get(0));
					dailyAdNetworkPerformanceBean.setWeek(strPerRow.get(1));
					dailyAdNetworkPerformanceBean.setExternal_customer_id(strPerRow.get(2));
					dailyAdNetworkPerformanceBean.setAccount_descriptive_name(strPerRow.get(3));
					dailyAdNetworkPerformanceBean.setAccount_currency_code(strPerRow.get(4));
					dailyAdNetworkPerformanceBean.setAccount_timezone_id(strPerRow.get(5));
					dailyAdNetworkPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(6)));
					dailyAdNetworkPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(7)));
					dailyAdNetworkPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(8)));
					dailyAdNetworkPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(9)));
					dailyAdNetworkPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(10)));
					dailyAdNetworkPerformanceBean.setCost(GBPvalue);
					dailyAdNetworkPerformanceBean.setCost_per_conversion(value);
					dailyAdNetworkPerformanceBean.setAll_conversion_rate(Float.parseFloat(strPerRow.get(13)));
					dailyAdNetworkPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(14)));
					dailyAdNetworkPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(15)));
					dailyAdNetworkPerformanceBean.setAverage_cpv(Float.parseFloat(strPerRow.get(16)));
					dailyAdNetworkPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(17)));
					dailyAdNetworkPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(18)));
					dailyAdNetworkPerformanceBean
							.setSearch_exactmatch_impression_share(Float.parseFloat(strPerRow.get(19)));
					dailyAdNetworkPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(20)));
					dailyAdNetworkPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(21)));
					dailyAdNetworkPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(22)));
					dailyAdNetworkPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(23)));
					dailyAdNetworkPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(24)));
					dailyAdNetworkPerformanceBean.setAd_network_type1(strPerRow.get(25));
					
					dailyAdNetworkPerformanceBeanList.add(dailyAdNetworkPerformanceBean);
					sizeOfLine++;

				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Daily Ad Network Performance> ");

			System.out.println();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return dailyAdNetworkPerformanceBeanList;
	}

	public static List<DailyCampaignPerformanceBean> dailyCampaignPerformance(AdWordsServicesInterface adWordsServices,
			AdWordsSession session, BudgetDataBean budgetDataBean) {

		List<DailyCampaignPerformanceBean> dailyCampaignPerformanceBeanList = new ArrayList<DailyCampaignPerformanceBean>();

		String daily_campaign_performance = "SELECT Week,AccountCurrencyCode," + "AccountTimeZone,"
				+ "AdvertisingChannelType,AccountDescriptiveName," + "CampaignId,CampaignName,CampaignStatus,Cost,"
				+ "Impressions,Clicks,Ctr,Conversions," + "ConversionRate,CostPerConversion,VideoViews,VideoViewRate,"
				+ "ViewThroughConversions,InvalidClicks,InvalidClickRate,ImpressionReach,"
				+ "SearchBudgetLostImpressionShare,SearchExactMatchImpressionShare,SearchImpressionShare,"
				+ "SearchRankLostImpressionShare,AveragePosition,AverageTimeOnSite,BounceRate,"
				+ "ClickAssistedConversions,AllConversionValue,Date " + "FROM CAMPAIGN_PERFORMANCE_REPORT "
				+ "WHERE CampaignStatus IN [ENABLED] " + " DURING YESTERDAY";

		ReportingConfiguration reportingConfiguration = new ReportingConfiguration.Builder()

				.skipReportHeader(true).skipColumnHeader(true).skipReportSummary(true)

				.includeZeroImpressions(false).build();

		session.setReportingConfiguration(reportingConfiguration);

		ReportDownloaderInterface reportDownloader = adWordsServices.getUtility(session,
				ReportDownloaderInterface.class);

		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		try {

			final ReportDownloadResponse response = reportDownloader.downloadReport(daily_campaign_performance,
					DownloadFormat.CSV);

			reader = new BufferedReader(new InputStreamReader(response.getInputStream(), Charsets.UTF_8));
			String line;

			Splitter splitter = Splitter.on(',');

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				line = line.replaceAll("%", "");
				line = line.replaceAll("--", "0");
				line = line.replaceAll("<", "");
				line = line.replaceAll(">", "");
				List<String> listPerRow = splitter.splitToList(line);
				list.add(listPerRow);
			}

			int sizeOfLine = 0;

			while (sizeOfLine < list.size()) {

				try {

					List<String> strPerRow = list.get(sizeOfLine);

					float costValue = Float.parseFloat(strPerRow.get(8));
					float dollarValue = costValue / 1000000;
					float GBPvalue = (float) dollarValue;
					float costperconversion = Float.parseFloat(strPerRow.get(14));
					float cost_per_conversion_value = costperconversion / 1000000;
					float value = (float) cost_per_conversion_value;

					DailyCampaignPerformanceBean dailyCampaignPerformanceBean = new DailyCampaignPerformanceBean();
					dailyCampaignPerformanceBean.setWeek(strPerRow.get(0));
					dailyCampaignPerformanceBean.setAccount_currency_code(strPerRow.get(1));
					dailyCampaignPerformanceBean.setAdvertising_channel_type(strPerRow.get(2));
					dailyCampaignPerformanceBean.setAccount_descriptive_name(strPerRow.get(3));
					dailyCampaignPerformanceBean.setAccount_timezone_id(strPerRow.get(4));
					dailyCampaignPerformanceBean.setCampaign_id(strPerRow.get(5));
					dailyCampaignPerformanceBean.setCampaign_name(strPerRow.get(6));
					dailyCampaignPerformanceBean.setCampaign_status(strPerRow.get(7));
					dailyCampaignPerformanceBean.setCost(GBPvalue);
					dailyCampaignPerformanceBean.setImpressions(Long.parseLong(strPerRow.get(9)));
					dailyCampaignPerformanceBean.setClicks(Integer.parseInt(strPerRow.get(10)));
					dailyCampaignPerformanceBean.setCtr(Float.parseFloat(strPerRow.get(11)));
					dailyCampaignPerformanceBean.setConversions(Float.parseFloat(strPerRow.get(12)));
					dailyCampaignPerformanceBean.setConversion_rate(Float.parseFloat(strPerRow.get(13)));
					dailyCampaignPerformanceBean.setCost_per_conversion(value);
					dailyCampaignPerformanceBean.setVideo_views(Integer.parseInt(strPerRow.get(15)));
					dailyCampaignPerformanceBean.setVideo_view_rate(Float.parseFloat(strPerRow.get(16)));
					dailyCampaignPerformanceBean.setView_through_conversions(Integer.parseInt(strPerRow.get(17)));
					dailyCampaignPerformanceBean.setInvalid_clicks(Integer.parseInt(strPerRow.get(18)));
					dailyCampaignPerformanceBean.setInvalid_click_rate(Float.parseFloat(strPerRow.get(19)));
					dailyCampaignPerformanceBean.setImpression_reach(strPerRow.get(20));

					dailyCampaignPerformanceBean
							.setSearch_budget_lost_impression_share(Float.parseFloat(strPerRow.get(21)));
					dailyCampaignPerformanceBean
							.setSearch_exact_match_impression_share(Float.parseFloat(strPerRow.get(22)));
					dailyCampaignPerformanceBean.setSearch_impression_share(Float.parseFloat(strPerRow.get(23)));
					dailyCampaignPerformanceBean
							.setSearch_rank_lost_impression_share(Float.parseFloat(strPerRow.get(24)));
					dailyCampaignPerformanceBean.setAverage_position(Float.parseFloat(strPerRow.get(25)));
					dailyCampaignPerformanceBean.setAverage_time_on_site(Float.parseFloat(strPerRow.get(26)));

					dailyCampaignPerformanceBean.setBounce_rate(Float.parseFloat(strPerRow.get(27)));

					dailyCampaignPerformanceBean.setClick_assisted_conversions(Float.parseFloat(strPerRow.get(28)));

					dailyCampaignPerformanceBean.setAll_conversion_value(Float.parseFloat(strPerRow.get(29)));
					dailyCampaignPerformanceBean.setDate(strPerRow.get(30));

					dailyCampaignPerformanceBeanList.add(dailyCampaignPerformanceBean);
					sizeOfLine++;
				} catch (Exception e) {
					if (e.getMessage().contains("AuthenticationError.CUSTOMER_NOT_FOUND")) {
						System.out.println(
								"Adwords Id is wrong with  : Client Stamp  -- >" + budgetDataBean.getClient_stamp()
										+ " & Company Name -- >" + budgetDataBean.getCompany_name());
					}
				} 
				finally {

				}

			}
			System.out.println("Number of Rows : " + list.size() + " :: Company Name :- "
					+ budgetDataBean.getCompany_name() + " ::Internal Client Id :- " + budgetDataBean.getClient_stamp()
					+ " Table Name :-  <Daily Campaign Performance> ");

			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ReportException e) {
			e.printStackTrace();
		} catch (ReportDownloadResponseException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return dailyCampaignPerformanceBeanList;
	}
}