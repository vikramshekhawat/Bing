package com.gem.dao;

import java.util.List;
import java.util.Map;

public interface AllClientInfoNeededDao {

	// List<Map<String, Object>> getAllClientInfo();

	List<Map<String, Object>> getAllClientInfo(String clientId);

	List<Map<String, Object>> createURLFromAdwordsData(List<Map<String, Object>> listAdwordsId, String refreshToken);
}
