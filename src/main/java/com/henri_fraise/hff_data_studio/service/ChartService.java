package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.ChartType;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.ChartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartService {

	private final ChartRepository chartRepository;

	@Transactional
	public Chart createChart(AnalysisResult result, String chartType, String configJson) {
		Chart chart = Chart.builder()
				.result(result)
				.chartType(ChartType.valueOf(chartType.toUpperCase()))
				.configJson(configJson != null ? parseConfigJson(configJson) : getDefaultConfig(chartType))
				.build();
		Chart saved = chartRepository.save(chart);
		if (log.isInfoEnabled()) {
			log.info(" Chart created: {} for result: {}", saved.getId(), result.getId());
		}
		return saved;
	}

	@Transactional
	public Chart createChart(AnalysisResult result, ChartType chartType, Map<String, Object> config) {
		Chart chart = Chart.builder()
				.result(result)
				.chartType(chartType)
				.configJson(config != null ? config : getDefaultConfigMap(chartType))
				.build();
		Chart saved = chartRepository.save(chart);
		if (log.isInfoEnabled()) {
			log.info("Chart created: {} for result: {}", saved.getId(), result.getId());
		}
		return saved;
	}

	public Chart getChartEntityById(UUID chartId) {
		return chartRepository.findById(chartId)
				.orElseThrow(() -> new ResourceNotFoundException("Chart not found: " + chartId));
	}

	public Chart getChartByResultId(UUID resultId) {
		return chartRepository.findByResultId(resultId)
				.orElseThrow(() -> new ResourceNotFoundException("Chart not found for result: " + resultId));
	}

	public List<Chart> getChartsByResultIds(List<UUID> resultIds) {
		return chartRepository.findByResultIdIn(resultIds);
	}

	public List<Chart> getChartsByType(ChartType chartType) {
		return chartRepository.findByChartType(chartType);
	}

	@Transactional
	public Chart updateChart(UUID chartId, String chartType, Map<String, Object> configJson) {
		Chart chart = getChartEntityById(chartId);
		if (chartType != null) {
			chart.setChartType(ChartType.valueOf(chartType.toUpperCase()));
		}
		if (configJson != null) {
			chart.setConfigJson(configJson);
		}
		Chart updated = chartRepository.save(chart);
		log.info("Chart updated: {}", chartId);
		return updated;
	}

	@Transactional
	public Chart updateChartConfig(UUID chartId, Map<String, Object> configJson) {
		Chart chart = getChartEntityById(chartId);
		chart.setConfigJson(configJson);
		Chart updated = chartRepository.save(chart);
		log.info("Chart config updated: {}", chartId);
		return updated;
	}

	@Transactional
	public void deleteChart(UUID chartId) {
		Chart chart = getChartEntityById(chartId);
		chartRepository.delete(chart);
		log.info("Chart deleted: {}", chartId);
	}

	@Transactional
	public void deleteChartByResultId(UUID resultId) {
		chartRepository.deleteByResultId(resultId);
		log.info("Chart deleted for result: {}", resultId);
	}

	@Transactional
	public void deleteChartsByResultIds(List<UUID> resultIds) {
		chartRepository.deleteByResultIdIn(resultIds);
		log.info("Charts deleted for {} results", resultIds.size());
	}

	public long countCharts() {
		return chartRepository.count();
	}

	public long countChartsByType(ChartType chartType) {
		return chartRepository.countByChartType(chartType);
	}

	public Map<String, Long> getChartTypeDistribution() {
		List<Object[]> results = chartRepository.countGroupByChartType();
		Map<String, Long> distribution = new HashMap<>();
		for (Object[] result : results) {
			distribution.put(((ChartType) result[0]).name(), (Long) result[1]);
		}
		return distribution;
	}

	public Map<String, Object> getDefaultConfig(String chartType) {
		return getDefaultConfigMap(ChartType.valueOf(chartType.toUpperCase()));
	}

	public Map<String, Object> getDefaultConfigMap(ChartType chartType) {
		Map<String, Object> config = new HashMap<>();

		config.put("colors", List.of("#0B2545", "#FFCD11", "#58595B", "#2E7D32", "#C62828"));
		config.put("fontFamily", "Calibri, Segoe UI, sans-serif");
		config.put("fontSize", 12);
		config.put("titleFontSize", 16);
		config.put("titleFontWeight", "bold");
		config.put("titleColor", "#0B2545");
		config.put("axisLabelColor", "#1A1A1A");
		config.put("gridColor", "#F2F2F2");
		config.put("legendPosition", "top");
		config.put("backgroundColor", "#FFFFFF");

		switch (chartType) {
			case BAR -> {
				config.put("barWidth", 0.7);
				config.put("barSpacing", 0.2);
				config.put("showValues", false);
				config.put("orientation", "vertical");
			}
			case LINE -> {
				config.put("lineWidth", 2.5);
				config.put("showPoints", true);
				config.put("pointSize", 6);
				config.put("smooth", true);
				config.put("fillArea", false);
				config.put("areaOpacity", 0.3);
			}
			case PIE -> {
				config.put("holeSize", 0);
				config.put("showLabels", true);
				config.put("labelPosition", "outside");
				config.put("labelColor", "#1A1A1A");
				config.put("startAngle", 0);
			}
			case SCATTER -> {
				config.put("pointSize", 8);
				config.put("pointOpacity", 0.7);
				config.put("showTrendLine", false);
				config.put("jitter", 0);
			}
			case AREA -> {
				config.put("lineWidth", 2);
				config.put("areaOpacity", 0.5);
				config.put("showPoints", false);
				config.put("stacked", false);
				config.put("smooth", true);
			}
		}

		return config;
	}

	private Map<String, Object> parseConfigJson(String configJson) {
		try {
			return new com.fasterxml.jackson.databind.ObjectMapper().readValue(configJson, Map.class);
		} catch (Exception e) {
			log.warn("Failed to parse config JSON, using default: {}", e.getMessage());
			return getDefaultConfigMap(ChartType.BAR);
		}
	}

	public Chart duplicateChart(UUID chartId) {
		Chart original = getChartEntityById(chartId);
		Chart duplicate = Chart.builder()
				.result(original.getResult())
				.chartType(original.getChartType())
				.configJson(new HashMap<>(original.getConfigJson()))
				.build();
		Chart saved = chartRepository.save(duplicate);
		log.info("Chart duplicated: {} -> {}", chartId, saved.getId());
		return saved;
	}

	public boolean existsByResultId(UUID resultId) {
		return chartRepository.existsByResultId(resultId);
	}

	public List<Chart> getChartsByResultIdsWithConfig(List<UUID> resultIds) {
		return chartRepository.findByResultIdIn(resultIds);
	}

	public Map<String, Object> getChartWithResultData(UUID chartId) {
		Chart chart = getChartEntityById(chartId);
		AnalysisResult result = chart.getResult();

		Map<String, Object> response = new HashMap<>();
		response.put("chart", chart);
		response.put("resultId", result.getId());
		response.put("resultTitle", result.getTitle());
		response.put("resultType", result.getResultType());
		response.put("filePath", result.getFilePath());

		return response;
	}

	public List<Chart> getChartsByResult(AnalysisResult result) {
		return chartRepository.findByResult(result);
	}

	@Transactional
	public Chart updateChartType(UUID chartId, ChartType chartType) {
		Chart chart = getChartEntityById(chartId);
		chart.setChartType(chartType);
		Chart updated = chartRepository.save(chart);
		log.info("Chart type updated: {} -> {}", chartId, chartType);
		return updated;
	}

	@Transactional
	public Chart updateChartColors(UUID chartId, List<String> colors) {
		Chart chart = getChartEntityById(chartId);
		Map<String, Object> config = chart.getConfigJson();
		if (config == null) {
			config = getDefaultConfigMap(chart.getChartType());
		}
		config.put("colors", colors);
		chart.setConfigJson(config);
		Chart updated = chartRepository.save(chart);
		log.info("Chart colors updated: {}", chartId);
		return updated;
	}

	public Map<String, Object> getHFFDefaultBranding() {
		Map<String, Object> branding = new HashMap<>();
		branding.put("primaryColor", "#FFCD11");
		branding.put("secondaryColor", "#0B2545");
		branding.put("darkColor", "#1A1A1A");
		branding.put("grayColor", "#58595B");
		branding.put("lightGray", "#F2F2F2");
		branding.put("successColor", "#2E7D32");
		branding.put("warningColor", "#E8720C");
		branding.put("errorColor", "#C62828");
		branding.put("infoColor", "#1565C0");
		branding.put("backgroundColor", "#FFFFFF");
		branding.put("fontFamily", "Calibri, Segoe UI, sans-serif");
		branding.put("titleColor", "#0B2545");
		branding.put("textColor", "#1A1A1A");
		return branding;
	}
}