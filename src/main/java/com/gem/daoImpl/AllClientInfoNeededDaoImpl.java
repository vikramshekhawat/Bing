package com.gem.daoImpl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.gem.bean.BudgetDataBean;
import com.gem.dao.AllClientInfoNeededDao;

@PropertySource("/ads.properties")
@Component
public class AllClientInfoNeededDaoImpl implements AllClientInfoNeededDao {
	DataSource dataSource;

	JdbcTemplate jdbcTemplate;

	HistoricalAnalysisDaoImpl history;

	@Autowired
	Environment env;

	GetRefreshTokenImpl getRefreshTokenImpl;

	public List<Map<String, Object>> getAllClientInfo(String clientId) {
		String sql = "SELECT " + env.getProperty("internalClientIdColumn") + ", "
				+ env.getProperty("ClientCustomerIdColumn") + ", " + env.getProperty("company_nameColumn") + ", "
				+ env.getProperty("is_google_adwords_clientColumn") + ", " + env.getProperty("budget_periodColumn")
				+ ", " + env.getProperty("google_currency_codeColumn") + ", "
				+ env.getProperty("google_currency_symbolColumn") + ", "
				+ env.getProperty("google_currency_symbol_nativeColumn") + ", "
				+ env.getProperty("google_display_ad_budgetColumn") + ", "
				+ env.getProperty("google_search_ad_budgetColumn") + ", "
				+ env.getProperty("google_youtube_search_ad_budgetColumn") + ", "
				+ env.getProperty("google_youtube_videos_ad_budgetColumn") + " FROM "
				+ env.getProperty("google_adwords_client_config") + " where "
				+ env.getProperty("ClientCustomerIdColumn") + "='" + clientId + "'";

		List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

		return results;
	}

	public List<Map<String, Object>> getAllClientInfo() {
		String sql = "SELECT " + env.getProperty("internalClientIdColumn") + ", "
				+ env.getProperty("ClientCustomerIdColumn") + ", " + env.getProperty("company_nameColumn") + ", "
				+ env.getProperty("is_google_adwords_clientColumn") + ", " + env.getProperty("budget_periodColumn")
				+ ", " + env.getProperty("google_currency_codeColumn") + ", "
				+ env.getProperty("google_currency_symbolColumn") + ", "
				+ env.getProperty("google_currency_symbol_nativeColumn") + ", "
				+ env.getProperty("google_display_ad_budgetColumn") + ", "
				+ env.getProperty("google_search_ad_budgetColumn") + ", "
				+ env.getProperty("google_youtube_search_ad_budgetColumn") + ", "
				+ env.getProperty("google_youtube_videos_ad_budgetColumn") + " FROM "
				+ env.getProperty("google_adwords_client_config") + ";";

		List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

		return results;
	}

	public AllClientInfoNeededDaoImpl(DataSource dataSource, HistoricalAnalysisDaoImpl history,
			GetRefreshTokenImpl getRefreshTokenImpl) {
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.getRefreshTokenImpl = getRefreshTokenImpl;
		this.history = history;
	}

	public List<Map<String, Object>> createURLFromAdwordsData(List<Map<String, Object>> listAdwordsId,
			String clientId) {
		Map<String, Object> map = null;
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		try {
			for (int i = 0; i < listAdwordsId.size(); i++) {
				Map<String, Object> mapAccess = new HashMap<String, Object>();
				map = listAdwordsId.get(i);
				
				String refreshToken = getRefreshTokenImpl
						.getRefreshToken((String) map.get(env.getProperty("ClientCustomerIdColumn")));

				BudgetDataBean dataBean = new BudgetDataBean(
						String.valueOf(map.get(env.getProperty("budget_periodColumn"))),
						String.valueOf(map.get(env.getProperty("google_display_ad_budgetColumn"))),
						String.valueOf(map.get(env.getProperty("google_search_ad_budgetColumn"))),
						String.valueOf(map.get(env.getProperty("google_youtube_search_ad_budgetColumn"))),
						String.valueOf(map.get(env.getProperty("google_youtube_videos_ad_budgetColumn"))),
						String.valueOf(map.get(env.getProperty("internalClientIdColumn"))),
						String.valueOf(map.get(env.getProperty("ClientCustomerIdColumn"))),
						String.valueOf(new Date(System.currentTimeMillis())),
						String.valueOf(map.get(env.getProperty("company_nameColumn"))));

				mapAccess.put("refreshToken" + i, refreshToken);
				mapAccess.put("dataBean" + i, dataBean);
				
				mapList.add(mapAccess);

				history.createHistoryAnalysis(dataBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(String.valueOf(map.get(env.getProperty("ClientCustomerIdColumn"))) + " -- "
					+ String.valueOf(map.get(env.getProperty("company_nameColumn"))));
		}
		return mapList;
	}

	public void closeSessionAndCluster() {
		history.closeSessionAndCluster();
	}

}
