package com.henri_fraise.hff_data_studio.service.connector;

import java.util.Map;

public interface CloudConnector {
	boolean test(Map<String, Object> config, Map<String, Object> credentials);
	byte[] download(String remotePath, Map<String, Object> config, Map<String, Object> credentials);
}