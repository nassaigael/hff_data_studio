Parfait ! Passons au package `validation` avec des validateurs personnalisés pour les DTOs.

---

## Structure du package

```
com.henri_fraise.hff_data_studio.validation
├── Annotation
│   ├── ValidEmail.java
│   ├── ValidPassword.java
│   ├── ValidFileType.java
│   ├── ValidEnumValue.java
│   ├── ValidDateFormat.java
│   ├── ValidPhoneNumber.java
│   ├── ValidUrl.java
│   └── ValidFilePath.java
├── Validator
│   ├── EmailValidator.java
│   ├── PasswordValidator.java
│   ├── FileTypeValidator.java
│   ├── EnumValueValidator.java
│   ├── DateFormatValidator.java
│   ├── PhoneNumberValidator.java
│   ├── UrlValidator.java
│   └── FilePathValidator.java
├── Constraint
│   ├── UniqueEmail.java
│   ├── UniqueProjectName.java
│   └── ExistingEntityId.java
├── Handler
│   ├── GlobalValidationExceptionHandler.java
│   └── ValidationErrorResponse.java
└── Group
    ├── OnCreate.java
    ├── OnUpdate.java
    └── OnDelete.java
```

---

## 1. Annotations

### ValidEmail.java

```java
package com.henri_fraise.hff_data_studio.validation.annotation;

import com.henri_fraise.hff_data_studio.validation.validator.EmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {

    String message() default "Email must be valid and from an allowed domain";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean allowNull() default false;

    boolean allowEmpty() default false;

    String[] allowedDomains() default {};
}
```

---

### ValidPassword.java

```java
package com.henri_fraise.hff_data_studio.validation.annotation;

import com.henri_fraise.hff_data_studio.validation.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be at least 8 characters, contain uppercase, lowercase, digit and special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minLength() default 8;

    int maxLength() default 32;

    boolean requireUppercase() default true;

    boolean requireLowercase() default true;

    boolean requireDigit() default true;

    boolean requireSpecialChar() default true;
}
```

---

### ValidFileType.java

```java
package com.henri_fraise.hff_data_studio.validation.annotation;

import com.henri_fraise.hff_data_studio.validation.validator.FileTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileType {

    String message() default "File type not allowed. Allowed types: CSV, EXCEL, SQL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] allowedTypes() default {"CSV", "EXCEL", "SQL"};

    long maxSize() default 200 * 1024 * 1024; // 200 MB default
}
```

---

### ValidEnumValue.java

```java
package com.henri_fraise.hff_data_studio.validation.annotation;

import com.henri_fraise.hff_data_studio.validation.validator.EnumValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnumValue {

    String message() default "Invalid enum value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();

    boolean ignoreCase() default false;

    boolean allowNull() default false;
}
```

---

### ValidDateFormat.java

```java
package com.henri_fraise.hff_data_studio.validation.annotation;

import com.henri_fraise.hff_data_studio.validation.validator.DateFormatValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateFormat {

    String message() default "Invalid date format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String pattern() default "yyyy-MM-dd";

    boolean allowNull() default false;
}
```

---

### ValidPhoneNumber.java

```java
package com.henri_fraise.hff_data_studio.validation.annotation;

import com.henri_fraise.hff_data_studio.validation.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    String message() default "Invalid phone number format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean allowNull() default false;

    boolean allowEmpty() default false;

    String countryCode() default "";
}
```

---

### ValidUrl.java

```java
package com.henri_fraise.hff_data_studio.validation.annotation;

import com.henri_fraise.hff_data_studio.validation.validator.UrlValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {

    String message() default "Invalid URL format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean allowNull() default false;

    boolean requireHttps() default false;

    String[] allowedProtocols() default {"http", "https"};
}
```

---

### ValidFilePath.java

```java
package com.henri_fraise.hff_data_studio.validation.annotation;

import com.henri_fraise.hff_data_studio.validation.validator.FilePathValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FilePathValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFilePath {

    String message() default "Invalid file path";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean allowNull() default false;

    boolean mustExist() default false;

    String[] allowedExtensions() default {};
}
```

---

## 2. Validateurs

### EmailValidator.java

```java
package com.henri_fraise.hff_data_studio.validation.validator;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private boolean allowNull;
    private boolean allowEmpty;
    private String[] allowedDomains;

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.allowedDomains = constraintAnnotation.allowedDomains();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Handle null and empty
        if (value == null) {
            return allowNull;
        }
        if (value.trim().isEmpty()) {
            return allowEmpty;
        }

        // Check email format
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email format is invalid")
                .addConstraintViolation();
            return false;
        }

        // Check allowed domains
        if (allowedDomains.length > 0) {
            String domain = value.substring(value.indexOf('@') + 1);
            boolean domainAllowed = Arrays.stream(allowedDomains)
                .anyMatch(allowed -> domain.equalsIgnoreCase(allowed) || domain.endsWith("." + allowed));
            
            if (!domainAllowed) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Email domain must be one of: " + String.join(", ", allowedDomains)
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
```

---

### PasswordValidator.java

```java
package com.henri_fraise.hff_data_studio.validation.validator;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        List<String> errors = new ArrayList<>();

        // Check length
        if (value.length() < minLength) {
            errors.add("Password must be at least " + minLength + " characters");
        }
        if (value.length() > maxLength) {
            errors.add("Password must not exceed " + maxLength + " characters");
        }

        // Check uppercase
        if (requireUppercase && !value.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter");
        }

        // Check lowercase
        if (requireLowercase && !value.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter");
        }

        // Check digit
        if (requireDigit && !value.matches(".*\\d.*")) {
            errors.add("Password must contain at least one digit");
        }

        // Check special character
        if (requireSpecialChar && !value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errors.add("Password must contain at least one special character");
        }

        if (!errors.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.join("; ", errors))
                .addConstraintViolation();
            return false;
        }

        return true;
    }
}
```

---

### FileTypeValidator.java

```java
package com.henri_fraise.hff_data_studio.validation.validator;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidFileType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class FileTypeValidator implements ConstraintValidator<ValidFileType, MultipartFile> {

    private String[] allowedTypes;
    private long maxSize;

    @Override
    public void initialize(ValidFileType constraintAnnotation) {
        this.allowedTypes = constraintAnnotation.allowedTypes();
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "File size exceeds maximum allowed size: " + (maxSize / (1024 * 1024)) + " MB"
            ).addConstraintViolation();
            return false;
        }

        // Check file type
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }

        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot + 1).toUpperCase();
        }

        // Check content type as well
        String contentType = file.getContentType();
        
        boolean typeAllowed = Arrays.stream(allowedTypes)
            .anyMatch(type -> {
                if (type.equals("CSV")) {
                    return extension.equals("CSV") || 
                           (contentType != null && contentType.contains("csv"));
                }
                if (type.equals("EXCEL")) {
                    return extension.equals("XLSX") || extension.equals("XLS") ||
                           (contentType != null && (contentType.contains("excel") || 
                            contentType.contains("spreadsheet")));
                }
                if (type.equals("SQL")) {
                    return extension.equals("SQL") ||
                           (contentType != null && contentType.contains("sql"));
                }
                return false;
            });

        if (!typeAllowed) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "File type not allowed. Allowed types: " + String.join(", ", allowedTypes)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
```

---

### EnumValueValidator.java

```java
package com.henri_fraise.hff_data_studio.validation.validator;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidEnumValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValueValidator implements ConstraintValidator<ValidEnumValue, String> {

    private Class<? extends Enum<?>> enumClass;
    private boolean ignoreCase;
    private boolean allowNull;

    @Override
    public void initialize(ValidEnumValue constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return allowNull;
        }

        String trimmed = value.trim();
        
        for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
            String enumName = enumConstant.name();
            if (ignoreCase) {
                if (enumName.equalsIgnoreCase(trimmed)) {
                    return true;
                }
            } else {
                if (enumName.equals(trimmed)) {
                    return true;
                }
            }
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "Value must be one of: " + Arrays.toString(enumClass.getEnumConstants())
        ).addConstraintViolation();
        
        return false;
    }
}
```

---

### DateFormatValidator.java

```java
package com.henri_fraise.hff_data_studio.validation.validator;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidDateFormat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateFormatValidator implements ConstraintValidator<ValidDateFormat, String> {

    private String pattern;
    private boolean allowNull;

    @Override
    public void initialize(ValidDateFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return allowNull;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            // Try to parse as LocalDate or LocalDateTime
            if (pattern.contains("HH") || pattern.contains("mm") || pattern.contains("ss")) {
                LocalDateTime.parse(value.trim(), formatter);
            } else {
                LocalDate.parse(value.trim(), formatter);
            }
            return true;
        } catch (DateTimeParseException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Date must be in format: " + pattern
            ).addConstraintViolation();
            return false;
        }
    }
}
```

---

### PhoneNumberValidator.java

```java
package com.henri_fraise.hff_data_studio.validation.validator;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{1,4}?[-.\\s]?[(]?[0-9]{1,3}[)]?[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,9}$"
    );

    private boolean allowNull;
    private boolean allowEmpty;
    private String countryCode;

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.allowEmpty = constraintAnnotation.allowEmpty();
        this.countryCode = constraintAnnotation.countryCode();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }
        if (value.trim().isEmpty()) {
            return allowEmpty;
        }

        String cleaned = value.trim();

        // Check country code if specified
        if (!countryCode.isEmpty() && !cleaned.startsWith(countryCode)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Phone number must start with country code: " + countryCode
            ).addConstraintViolation();
            return false;
        }

        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Phone number format is invalid. Expected format: [+][country code][number]"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
```

---

### UrlValidator.java

```java
package com.henri_fraise.hff_data_studio.validation.validator;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidUrl;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

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
        if (value == null || value.trim().isEmpty()) {
            return allowNull;
        }

        String trimmed = value.trim();

        try {
            URI uri = new URI(trimmed);
            
            // Check if it's a valid URL
            uri.toURL();

            // Check protocol
            String protocol = uri.getScheme();
            if (protocol == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("URL must have a protocol (http, https, etc.)")
                    .addConstraintViolation();
                return false;
            }

            // Check HTTPS requirement
            if (requireHttps && !"https".equalsIgnoreCase(protocol)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("URL must use HTTPS protocol")
                    .addConstraintViolation();
                return false;
            }

            // Check allowed allowedProtocols
            if (allowedProtocols.length > 0) {
                boolean protocolAllowed = Arrays.stream(allowedProtocols)
                    .anyMatch(p -> p.equalsIgnoreCase(protocol));
                
                if (!protocolAllowed) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        "Protocol must be one of: " + String.join(", ", allowedProtocols)
                    ).addConstraintViolation();
                    return false;
                }
            }

            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid URL format")
                .addConstraintViolation();
            return false;
        }
    }
}
```

---

### FilePathValidator.java

```java
package com.henri_fraise.hff_data_studio.validation.validator;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidFilePath;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FilePathValidator implements ConstraintValidator<ValidFilePath, String> {

    private boolean allowNull;
    private boolean mustExist;
    private String[] allowedExtensions;

    @Override
    public void initialize(ValidFilePath constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.mustExist = constraintAnnotation.mustExist();
        this.allowedExtensions = constraintAnnotation.allowedExtensions();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return allowNull;
        }

        String trimmed = value.trim();

        try {
            Path path = Paths.get(trimmed);

            // Check if file exists if required
            if (mustExist && !Files.exists(path)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("File does not exist: " + trimmed)
                    .addConstraintViolation();
                return false;
            }

            // Check file extension
            if (allowedExtensions.length > 0) {
                String fileName = path.getFileName().toString();
                String extension = "";
                int lastDot = fileName.lastIndexOf('.');
                if (lastDot > 0) {
                    extension = fileName.substring(lastDot + 1).toLowerCase();
                }

                boolean extensionAllowed = Arrays.stream(allowedExtensions)
                    .anyMatch(ext -> ext.toLowerCase().equals(extension));

                if (!extensionAllowed) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        "File extension must be one of: " + String.join(", ", allowedExtensions)
                    ).addConstraintViolation();
                    return false;
                }
            }

            // Prevent path traversal
            if (trimmed.contains("..")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Path traversal not allowed")
                    .addConstraintViolation();
                return false;
            }

            return true;
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid file path")
                .addConstraintViolation();
            return false;
        }
    }
}
```

---

## 3. Contraintes de validation

### UniqueEmail.java

```java
package com.henri_fraise.hff_data_studio.validation.constraint;

import com.henri_fraise.hff_data_studio.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}

@Component
@RequiredArgsConstructor
class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true;
        }
        return !userRepository.existsByEmail(email);
    }
}
```

---

### UniqueProjectName.java

```java
package com.henri_fraise.hff_data_studio.validation.constraint;

import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.UUID;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueProjectName {
    String message() default "Project name already exists";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
    UUID excludeProjectId() default 0;
}

@Component
@RequiredArgsConstructor
class UniqueProjectNameValidator implements ConstraintValidator<UniqueProjectName, String> {

    private final ProjectRepository projectRepository;
    private UUID excludeProjectId;

    @Override
    public void initialize(UniqueProjectName constraintAnnotation) {
        this.excludeProjectId = constraintAnnotation.excludeProjectId();
    }

    @Override
    public boolean isValid(String projectName, ConstraintValidatorContext context) {
        if (projectName == null || projectName.isEmpty()) {
            return true;
        }

        if (excludeProjectId != null && excludeProjectId != UUID.fromString("00000000-0000-0000-0000-000000000000")) {
            return !projectRepository.existsByProjectNameAndIdNot(projectName, excludeProjectId);
        }
        
        return !projectRepository.existsByProjectName(projectName);
    }
}
```

---

### ExistingEntityId.java

```java
package com.henri_fraise.hff_data_studio.validation.constraint;

import com.henri_fraise.hff_data_studio.repository.UserRepository;
import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.UUID;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExistingEntityId {
    String message() default "Entity not found";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
    EntityType entityType();
    
    enum EntityType {
        USER, PROJECT, DATASET, FILE, ANALYSIS
    }
}

@Component
@RequiredArgsConstructor
class ExistingEntityIdValidator implements ConstraintValidator<ExistingEntityId, UUID> {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private ExistingEntityId.EntityType entityType;

    @Override
    public void initialize(ExistingEntityId constraintAnnotation) {
        this.entityType = constraintAnnotation.entityType();
    }

    @Override
    public boolean isValid(UUID id, ConstraintValidatorContext context) {
        if (id == null) {
            return true;
        }

        return switch (entityType) {
            case USER -> userRepository.existsById(id);
            case PROJECT -> projectRepository.existsById(id);
            case DATASET -> true; // Would need DatasetRepository
            case FILE -> true; // Would need SourceFileRepository
            case ANALYSIS -> true; // Would need AnalysisRepository
        };
    }
}
```

---

## 4. Gestionnaire d'erreurs de validation

### ValidationErrorResponse.java

```java
package com.henri_fraise.hff_data_studio.validation.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private Integer status;

    private String error;

    private String message;

    private String path;

    private List<FieldError> fieldErrors;

    private Map<String, String> validationErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String rejectedValue;
        private String message;
    }
}
```

---

### GlobalValidationExceptionHandler.java

```java
package com.henri_fraise.hff_data_studio.validation.handler;

import com.henri_fraise.hff_data_studio.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));

        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid request parameters")
            .path(request.getDescription(false).replace("uri=", ""))
            .validationErrors(errors)
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) {

        log.error("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (existing, replacement) -> existing
            ));

        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Constraint Violation")
            .message("Validation failed")
            .path(request.getDescription(false).replace("uri=", ""))
            .validationErrors(errors)
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        log.error("Message not readable: {}", ex.getMessage());

        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Malformed JSON")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ValidationErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            WebRequest request) {

        log.error("Missing parameter: {}", ex.getMessage());

        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Missing Parameter")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            WebRequest request) {

        log.error("File size exceeded: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .code("FILE_SIZE_EXCEEDED")
            .message("File size exceeds maximum allowed limit: " + ex.getMaxUploadSize() + " bytes")
            .details(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
            .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .code("INVALID_ARGUMENT")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .status(HttpStatus.BAD_REQUEST.value())
            .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error: ", ex);

        ErrorResponse response = ErrorResponse.builder()
            .code("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred")
            .details(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

---

## 5. Groupes de validation

### OnCreate.java

```java
package com.henri_fraise.hff_data_studio.validation.group;

import jakarta.validation.groups.Default;

public interface OnCreate extends Default {
}
```

---

### OnUpdate.java

```java
package com.henri_fraise.hff_data_studio.validation.group;

import jakarta.validation.groups.Default;

public interface OnUpdate extends Default {
}
```

---

### OnDelete.java

```java
package com.henri_fraise.hff_data_studio.validation.group;

public interface OnDelete {
}
```

---

## 6. Utilisation dans les DTOs

```java
package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.validation.annotation.ValidEmail;
import com.henri_fraise.hff_data_studio.validation.annotation.ValidPassword;
import com.henri_fraise.hff_data_studio.validation.constraint.UniqueEmail;
import com.henri_fraise.hff_data_studio.validation.group.OnCreate;
import com.henri_fraise.hff_data_studio.validation.group.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructorpublic class UserCreationRequest {

    @NotBlank(message = "Last name is required", groups = OnCreate.class)
    private String lastName;

    @NotBlank(message = "First name is required", groups = OnCreate.class)
    private String firstName;

    @NotBlank(message = "Email is required", groups = OnCreate.class)
    @ValidEmail(
        message = "Email must be valid",
        allowedDomains = {"hff.re", "henri-fraise.com"},
        groups = {OnCreate.class, OnUpdate.class}
    )
    @UniqueEmail(groups = OnCreate.class)
    private String email;

    @NotBlank(message = "Password is required", groups = OnCreate.class)
    @ValidPassword(
        minLength = 8,
        maxLength = 32,
        groups = OnCreate.class
    )
    private String password;

    @NotNull(message = "Category ID is required", groups = OnCreate.class)
    private UUID categoryId;
}
```

---

## 7. Configuration pour le validateur de Bean Validation

### ValidationConfig.java

```java
package com.henri_fraise.hff_data_studio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }
}
```

---

## Résumé des validateurs créés

| Annotation | Validateur | Validation |
|------------|------------|------------|
| `@ValidEmail` | EmailValidator | Format email + domaines autorisés |
| `@ValidPassword` | PasswordValidator | Longueur, majuscule, minuscule, chiffre, caractère spécial |
| `@ValidFileType` | FileTypeValidator | Extension, taille, content type |
| `@ValidEnumValue` | EnumValueValidator | Valeur enum valide |
| `@ValidDateFormat` | DateFormatValidator | Format de date |
| `@ValidPhoneNumber` | PhoneNumberValidator | Format téléphone |
| `@ValidUrl` | UrlValidator | Format URL |
| `@ValidFilePath` | FilePathValidator | Chemin fichier |
| `@UniqueEmail` | UniqueEmailValidator | Email unique en base |
| `@UniqueProjectName` | UniqueProjectNameValidator | Nom projet unique en base |
| `@ExistingEntityId` | ExistingEntityIdValidator | ID existe en base |