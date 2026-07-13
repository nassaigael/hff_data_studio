package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.CleaningRuleResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import org.springframework.stereotype.Component;

@Component
public class CleaningRuleMapper {

	public CleaningRuleResponse toResponse(CleaningRule rule) {
		if (rule == null)
			return null;

		return CleaningRuleResponse.builder()

				.build();
	}
}
