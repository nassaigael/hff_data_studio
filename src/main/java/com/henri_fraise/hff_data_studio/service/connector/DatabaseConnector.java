package com.henri_fraise.hff_data_studio.service.connector;

import java.sql.Connection;
import java.util.Map;

public interface DatabaseConnector {
	Connection connect(Map<String, Object> config, Map<String, Object> credentials);
	boolean testConnection(Map<String, Object> config, Map<String, Object> credentials);
}