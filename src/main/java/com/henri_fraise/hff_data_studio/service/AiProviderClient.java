package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.AiMessage;
import java.util.List;

public interface AiProviderClient {
	String complete(String systemPrompt, List<AiMessage> history);
}