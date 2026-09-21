package com.henri_fraise.hff_data_studio.service.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JdbcDatabaseConnector implements DatabaseConnector {

	@Override
	public Connection connect(Map<String, Object> config, Map<String, Object> credentials) {
		try {
			String host = (String) config.get("host");
			Integer port = (Integer) config.getOrDefault("port", 5432);
			String database = (String) config.get("database");
			String user = (String) credentials.get("username");
			String password = (String) credentials.get("password");
			String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
			return DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			throw new RuntimeException("Failed to connect: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean testConnection(Map<String, Object> config, Map<String, Object> credentials) {
		try (Connection c = connect(config, credentials)) {
			return c != null && c.isValid(5);
		} catch (Exception e) {
			log.warn("Connection test failed: {}", e.getMessage());
			return false;
		}
	}
}