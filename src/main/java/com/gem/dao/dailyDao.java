package com.gem.dao;

import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.factory.AdWordsServicesInterface;

public interface dailyDao {
	
	 void dailyAccountPerformance(AdWordsServicesInterface adWordsServices, AdWordsSession session);

}
