package com.henri_fraise.hff_data_studio.validation.Validator;

import com.henri_fraise.hff_data_studio.validation.Annotation.ValidUrl;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UrlValidator implements ConstraintValidator<ValidUrl, String> {

  private boolean allowNull;
  private boolean requireHttps;
  private String[] allowedProtocols;

  @Override
  public void initialize(ValidUrl constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
    this.requireHttps = constraintAnnotation.requireHttps();
    this.allowedProtocols = constraintAnnotation.allowedProtocols();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) return allowNull;

    String trimmed = value.trim();
    try {
      URI uri = new URI(trimmed);
      URL url = uri.toURL();

      String protocol = uri.getScheme();
      if (protocol == null) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("URL must have a protocol (http, https, etc.)")
            .addConstraintViolation();
        return false;
      }

      if (requireHttps && !"https".equalsIgnoreCase(protocol)) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("URL must use HTTPS protocol")
            .addConstraintViolation();
        return false;
      }

      if (allowedProtocols.length > 0) {
        boolean protocolAllowed =
            Arrays.stream(allowedProtocols).anyMatch(p -> p.equalsIgnoreCase(protocol));

        if (!protocolAllowed) {
          context.disableDefaultConstraintViolation();
          context
              .buildConstraintViolationWithTemplate(
                  "Protocol must be one of: " + String.join(", ", allowedProtocols))
              .addConstraintViolation();
          return false;
        }
      }

      return true;
    } catch (URISyntaxException | MalformedURLException e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Invalid URL format").addConstraintViolation();
      return false;
    }
  }
}
