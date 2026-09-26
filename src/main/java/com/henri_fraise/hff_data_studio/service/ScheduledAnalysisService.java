package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.AnalysisExecutionRequest;
import com.henri_fraise.hff_data_studio.dto.request.ScheduledAnalysisRequest;
import com.henri_fraise.hff_data_studio.dto.response.ScheduledAnalysisResponse;
import com.henri_fraise.hff_data_studio.entity.ScheduledAnalysis;
import com.henri_fraise.hff_data_studio.enums.ScheduleFrequency;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.ScheduledAnalysisRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import com.henri_fraise.hff_data_studio.service.DatasetService;
import com.henri_fraise.hff_data_studio.service.PredefinedAnalysisService;
import com.henri_fraise.hff_data_studio.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledAnalysisService {

	private final ScheduledAnalysisRepository scheduleRepository;
	private final DatasetService datasetService;
	private final PredefinedAnalysisService analysisService;
	private final UserService userService;
	private final SecurityUtils securityUtils;
	private final ObjectMapper objectMapper;
	private final com.henri_fraise.hff_data_studio.service.AnalysisService analysisServiceRunner;

	@Transactional
	public ScheduledAnalysisResponse create(ScheduledAnalysisRequest request) {
		ScheduledAnalysis schedule = ScheduledAnalysis.builder()
				.name(request.getName())
				.dataset(datasetService.getDatasetEntityById(request.getDatasetId()))
				.predefinedAnalysis(request.getAnalysisId() != null
						? analysisService.getAnalysisEntityById(request.getAnalysisId())
						: null)
				.parametersJson(serialize(request.getParameters()))
				.frequency(request.getFrequency())
				.cronExpression(request.getCronExpression())
				.isActive(request.getIsActive() != null ? request.getIsActive() : true)
				.user(userService.getUserEntityById(securityUtils.getCurrentUserId()))
				.nextRunAt(computeNextRun(request.getFrequency(), request.getCronExpression()))
				.build();
		return toResponse(scheduleRepository.save(schedule));
	}

	@Transactional
	public ScheduledAnalysisResponse update(UUID scheduleId, ScheduledAnalysisRequest request) {
		ScheduledAnalysis schedule = getEntity(scheduleId);
		schedule.setName(request.getName());
		schedule.setFrequency(request.getFrequency());
		schedule.setCronExpression(request.getCronExpression());
		if (request.getParameters() != null) {
			schedule.setParametersJson(serialize(request.getParameters()));
		}
		if (request.getIsActive() != null) {
			schedule.setIsActive(request.getIsActive());
		}
		schedule.setNextRunAt(computeNextRun(request.getFrequency(), request.getCronExpression()));
		return toResponse(scheduleRepository.save(schedule));
	}

	@Transactional(readOnly = true)
	public List<ScheduledAnalysisResponse> listByUser() {
		return scheduleRepository.findByUserIdOrderByCreatedAtDesc(securityUtils.getCurrentUserId())
				.stream().map(this::toResponse).toList();
	}

	@Transactional
	public void delete(UUID scheduleId) {
		scheduleRepository.delete(getEntity(scheduleId));
	}

	@Transactional
	public void toggle(UUID scheduleId, boolean active) {
		ScheduledAnalysis schedule = getEntity(scheduleId);
		schedule.setIsActive(active);
		if (active) {
			schedule.setNextRunAt(computeNextRun(schedule.getFrequency(), schedule.getCronExpression()));
		}
		scheduleRepository.save(schedule);
	}

	@Scheduled(fixedDelay = 60000)
	@Transactional
	public void runDueSchedules() {
		List<ScheduledAnalysis> due = scheduleRepository.findDueSchedules(LocalDateTime.now());
		for (ScheduledAnalysis schedule : due) {
			executeSchedule(schedule);
		}
	}

	@Transactional
	protected void executeSchedule(ScheduledAnalysis schedule) {
		try {
			AnalysisExecutionRequest request = new AnalysisExecutionRequest();
			request.setDatasetId(schedule.getDataset().getId());
			request.setAnalysisId(schedule.getPredefinedAnalysis() != null
					? schedule.getPredefinedAnalysis().getId() : null);
			if (schedule.getParametersJson() != null) {
				request.setParameters(objectMapper.readValue(schedule.getParametersJson(), java.util.Map.class));
			}
			analysisServiceRunner.runAnalysis(request);
			schedule.setRunCount(schedule.getRunCount() + 1);
		} catch (Exception e) {
			log.error("Scheduled analysis failed: {}", e.getMessage());
			schedule.setFailureCount(schedule.getFailureCount() + 1);
		}
		schedule.setLastRunAt(LocalDateTime.now());
		schedule.setNextRunAt(computeNextRun(schedule.getFrequency(), schedule.getCronExpression()));
		scheduleRepository.save(schedule);
	}

	private LocalDateTime computeNextRun(ScheduleFrequency frequency, String cron) {
		LocalDateTime now = LocalDateTime.now();
		if (frequency == ScheduleFrequency.CRON && cron != null && !cron.isBlank()) {
			CronExpression expr = CronExpression.parse(cron);
			return expr.next(now);
		}
		return switch (frequency) {
			case ONCE -> null;
			case HOURLY -> now.plusHours(1);
			case DAILY -> now.plusDays(1);
			case WEEKLY -> now.plusWeeks(1);
			case MONTHLY -> now.plusMonths(1);
			case CRON -> now.plusDays(1);
		};
	}

	private ScheduledAnalysis getEntity(UUID id) {
		return scheduleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));
	}

	private String serialize(Object value) {
		try {
			return value != null ? objectMapper.writeValueAsString(value) : null;
		} catch (Exception e) {
			return null;
		}
	}

	private ScheduledAnalysisResponse toResponse(ScheduledAnalysis s) {
		return ScheduledAnalysisResponse.builder()
				.scheduleId(s.getId())
				.name(s.getName())
				.datasetId(s.getDataset() != null ? s.getDataset().getId() : null)
				.datasetName(s.getDataset() != null ? s.getDataset().getDatasetName() : null)
				.analysisId(s.getPredefinedAnalysis() != null ? s.getPredefinedAnalysis().getId() : null)
				.analysisName(s.getPredefinedAnalysis() != null ? s.getPredefinedAnalysis().getAnalysisName() : null)
				.frequency(s.getFrequency())
				.cronExpression(s.getCronExpression())
				.nextRunAt(s.getNextRunAt())
				.lastRunAt(s.getLastRunAt())
				.isActive(s.getIsActive())
				.runCount(s.getRunCount())
				.failureCount(s.getFailureCount())
				.createdAt(s.getCreatedAt())
				.build();
	}
}