package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.ExternalDataSourceRequest;
import com.henri_fraise.hff_data_studio.dto.response.ExternalDataSourceResponse;
import com.henri_fraise.hff_data_studio.entity.ExternalDataSource;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.ExternalDataSourceRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import com.henri_fraise.hff_data_studio.service.connector.JdbcDatabaseConnector;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalDataSourceService {

	private final ExternalDataSourceRepository sourceRepository;
	private final JdbcDatabaseConnector jdbcConnector;
	private final SecurityUtils securityUtils;
	private final ObjectMapper objectMapper;

	@Value("${security.encryption.key:hff-default-encryption-key-32ch}")
	private String encryptionKey;

	@Transactional
	public ExternalDataSourceResponse create(ExternalDataSourceRequest request) {
		ExternalDataSource source = ExternalDataSource.builder()
				.name(request.getName())
				.type(request.getType())
				.configJson(serialize(request.getConfig()))
				.credentialsEncrypted(encrypt(serialize(request.getCredentials())))
				.user(securityUtils.getCurrentUser())
				.build();
		return toResponse(sourceRepository.save(source));
	}

	@Transactional
	public boolean testConnection(UUID sourceId) {
		ExternalDataSource source = getEntity(sourceId);
		try {
			Map<String, Object> config = deserialize(source.getConfigJson());
			Map<String, Object> credentials = deserialize(decrypt(source.getCredentialsEncrypted()));
			boolean success = jdbcConnector.testConnection(config, credentials);
			source.setLastTestAt(LocalDateTime.now());
			source.setLastTestSuccess(success);
			source.setLastError(success ? null : "Connection test failed");
			sourceRepository.save(source);
			return success;
		} catch (Exception e) {
			source.setLastTestAt(LocalDateTime.now());
			source.setLastTestSuccess(false);
			source.setLastError(e.getMessage());
			sourceRepository.save(source);
			return false;
		}
	}

	@Transactional(readOnly = true)
	public List<ExternalDataSourceResponse> list() {
		return sourceRepository.findByUserIdOrderByCreatedAtDesc(securityUtils.getCurrentUserId())
				.stream().map(this::toResponse).toList();
	}

	@Transactional
	public void delete(UUID sourceId) {
		sourceRepository.delete(getEntity(sourceId));
	}

	private ExternalDataSource getEntity(UUID id) {
		return sourceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Data source not found"));
	}

	private String encrypt(String value) {
		try {
			SecretKeySpec key = new SecretKeySpec(
					encryptionKey.substring(0, 32).getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException("Encryption failed", e);
		}
	}

	private String decrypt(String value) {
		try {
			SecretKeySpec key = new SecretKeySpec(
					encryptionKey.substring(0, 32).getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(cipher.doFinal(Base64.getDecoder().decode(value)));
		} catch (Exception e) {
			throw new RuntimeException("Decryption failed", e);
		}
	}

	private String serialize(Object value) {
		try {
			return value != null ? objectMapper.writeValueAsString(value) : null;
		} catch (Exception e) {
			return null;
		}
	}

	private Map<String, Object> deserialize(String json) {
		try {
			return json != null ? objectMapper.readValue(json, Map.class) : Map.of();
		} catch (Exception e) {
			return Map.of();
		}
	}

	private ExternalDataSourceResponse toResponse(ExternalDataSource s) {
		return ExternalDataSourceResponse.builder()
				.sourceId(s.getId())
				.name(s.getName())
				.type(s.getType())
				.config(deserialize(s.getConfigJson()))
				.isActive(s.getIsActive())
				.lastTestAt(s.getLastTestAt())
				.lastTestSuccess(s.getLastTestSuccess())
				.lastError(s.getLastError())
				.createdAt(s.getCreatedAt())
				.build();
	}
}