package com.gem.daoImpl;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.gem.dao.GetRefreshToken;

@PropertySource("/ads.properties")
@Component
@Service

public class GetRefreshTokenImpl implements GetRefreshToken {

	@Autowired
	Environment env;
	JdbcTemplate jdbcTemplate;

	public GetRefreshTokenImpl(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public String getRefreshToken(String ClientId) {

		String sqlQueryRefreshToken = "Select " + env.getProperty("google_adwords_refresh_token") + " from "
				+ env.getProperty("google_adwords_client_config") + " where "
				+ env.getProperty("ClientCustomerIdColumn") + " = " + "'" + ClientId + "';";
		List<Map<String, Object>> mapRefreshToken = jdbcTemplate.queryForList(sqlQueryRefreshToken);
		String refreshToken = (String) mapRefreshToken.get(0).get(env.getProperty("google_adwords_refresh_token"));
		return refreshToken;
	}

}
