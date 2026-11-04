package com.henri_fraise.hff_data_studio.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.data-quality")
public record DataQualityProperties(String url, Duration timeout) {
}