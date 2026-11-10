package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.config.DataQualityProperties;
import com.henri_fraise.hff_data_studio.dto.response.CleanResponse;
import com.henri_fraise.hff_data_studio.dto.response.ColumnProfileResponse;
import com.henri_fraise.hff_data_studio.exception.ExternalServiceException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataQualityClient {

	private final DataQualityProperties properties;
	private final WebClient.Builder webClientBuilder;

	private WebClient client() {
		return webClientBuilder.baseUrl(properties.url()).build();
	}

	public List<ColumnProfileResponse> profileAndSuggest(Path filePath, String columnTypesJson) {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("file", new FileSystemResource(filePath));
		builder.part("column_types", columnTypesJson);

		try {
			return client()
					.post()
					.uri("/v1/profile-and-suggest")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(BodyInserters.fromMultipartData(builder.build()))
					.retrieve()
					.bodyToFlux(ColumnProfileResponse.class)
					.collectList()
					.block(properties.timeout());
		} catch (WebClientResponseException ex) {
			log.error("Profile error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
			throw new ExternalServiceException("Profile error: " + ex.getMessage(), ex);
		} catch (Exception ex) {
			log.error("Data quality engine unreachable: {}", ex.getMessage(), ex);
			throw new ExternalServiceException("Data quality engine unreachable", ex);
		}
	}

	public CleanResponse clean(CleanRequest request) {
		try {
			return client()
					.post()
					.uri("/v1/clean")
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(request)
					.retrieve()
					.bodyToMono(CleanResponse.class)
					.block(properties.timeout());
		} catch (WebClientResponseException ex) {
			log.error("Clean error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
			throw new ExternalServiceException("Clean error: " + ex.getMessage(), ex);
		} catch (Exception ex) {
			log.error("Data quality engine unreachable: {}", ex.getMessage(), ex);
			throw new ExternalServiceException("Data quality engine unreachable", ex);
		}
	}

	public record CleanRequest(
			String input_path,
			String output_path,
			String file_type,
			List<RuleSpec> rules) {}

	public record RuleSpec(String column, String type, Map<String, Object> parameters) {}
}