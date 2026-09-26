package com.henri_fraise.hff_data_studio.service.connector;

import java.io.InputStream;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

@Component
@Slf4j
public class S3Connector implements CloudConnector {

	@Override
	public boolean test(Map<String, Object> config, Map<String, Object> credentials) {
		try (S3Client client = buildClient(config, credentials)) {
			String bucket = (String) config.get("bucket");
			client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).maxKeys(1).build());
			return true;
		} catch (Exception e) {
			log.warn("S3 test failed: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public byte[] download(String remotePath, Map<String, Object> config,
	                       Map<String, Object> credentials) {
		try (S3Client client = buildClient(config, credentials)) {
			String bucket = (String) config.get("bucket");
			ResponseInputStream<GetObjectResponse> stream = client.getObject(
					GetObjectRequest.builder().bucket(bucket).key(remotePath).build());
			return stream.readAllBytes();
		} catch (Exception e) {
			throw new RuntimeException("S3 download failed: " + e.getMessage(), e);
		}
	}

	private S3Client buildClient(Map<String, Object> config, Map<String, Object> credentials) {
		return S3Client.builder()
				.region(Region.of((String) config.getOrDefault("region", "us-east-1")))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(
								(String) credentials.get("accessKey"),
								(String) credentials.get("secretKey"))))
				.build();
	}
}