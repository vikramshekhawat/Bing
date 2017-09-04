package com.gem.daoImpl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.gem.bean.BudgetDataBean;
import com.gem.bean.CassandraCredentials;
import com.gem.dao.HistoricalAnalysisDao;

public class HistoricalAnalysisDaoImpl implements HistoricalAnalysisDao {

	Cluster cluster = null;

	private Session session;
	CassandraCredentials cassandraCredentials;
	List<InetSocketAddress> address;

	public HistoricalAnalysisDaoImpl(CassandraCredentials cassandraCredentials) {
		this.cassandraCredentials = cassandraCredentials;
		session = getConection();
	}

	synchronized public Session getConection() {
		if (session != null) {
			return session;
		} else {
			address = new ArrayList<InetSocketAddress>();
			address.add(new InetSocketAddress(cassandraCredentials.getContactpoint(),
					Integer.parseInt(cassandraCredentials.getPort())));
			cluster = Cluster.builder().addContactPointsWithPorts(address).withCredentials("root", "Dig$dev%").build();

			/* cluster =
			 Cluster.builder().addContactPointsWithPorts(address).build();*/
			try {
				session = cluster.connect(cassandraCredentials.getDb());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return session;
		}
	}

	public void createHistoryAnalysis(BudgetDataBean beanValue) {
		try {
			StringBuffer preparedStatementQuery = new StringBuffer("Insert into adwords_historical_analysis_data "
					+ "(client_stamp," + " time," + " budget_period," + " client_customer_id," + " date,"
					+ " google_display_ad_budget," + " google_search_ad_budget," + " google_youtube_search_ad_budget,"
					+ " google_youtube_videos_ad_budget" + " ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");

			PreparedStatement preparedStmt = session.prepare(preparedStatementQuery.toString());

			BoundStatement boundStmt = preparedStmt.bind().setString(0, beanValue.getClient_stamp())
					.setLong(1, System.currentTimeMillis()).setString(2, String.valueOf(beanValue.getBudget_period()))
					.setString(3, (String.valueOf(beanValue.getClient_customer_id())))
					.setString(4, (String.valueOf(beanValue.getDate())))
					.setString(5, (String.valueOf(beanValue.getGoogle_display_ad_budget())))
					.setString(6, (String.valueOf(beanValue.getGoogle_search_ad_budget())))
					.setString(7, (String.valueOf(beanValue.getGoogle_youtube_search_ad_budget())))
					.setString(8, (String.valueOf(beanValue.getGoogle_youtube_videos_ad_budget())));

			session.execute(boundStmt);

		} catch (Exception e) {

			System.out.println("Error in Saving Historical data for client id - " + beanValue.getClient_stamp()
					+ "  and Company name - " + beanValue.getCompany_name());
		}

	}

	public void closeSessionAndCluster() {
		session.close();
		cluster.close();
	}

}
