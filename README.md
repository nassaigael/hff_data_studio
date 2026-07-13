Voici les classes JPA complètes pour Spring Boot, basées sur le modèle de données fourni. Tous les noms sont en anglais, snake_case converti en camelCase pour Java.

---

## 1. Package `com.hff.catalyst.entity`

### User.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "last_name", length = 80, nullable = false)
    private String lastName;

    @Column(name = "first_name", length = 80, nullable = false)
    private String firstName;

    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private UserCategory category;

    @OneToMany(mappedBy = "creator")
    private List<Project> projects;

    @OneToMany(mappedBy = "user")
    private List<SourceFile> sourceFiles;

    @OneToMany(mappedBy = "user")
    private List<AnalysisExecution> analysisExecutions;

    @OneToMany(mappedBy = "user")
    private List<Export> exports;

    @OneToMany(mappedBy = "user")
    private List<AuditLog> auditLogs;

    @OneToMany(mappedBy = "user")
    private List<CleaningHistory> cleaningHistories;
}
```

---

### UserCategory.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "user_category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "label", length = 60, nullable = false, unique = true)
    private String label;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "access_level", nullable = false)
    @Builder.Default
    private Integer accessLevel = 1;

    @OneToMany(mappedBy = "category")
    private List<User> users;

    @ManyToMany
    @JoinTable(
        name = "category_permission",
        joinColumns = @JoinColumn(name = "category_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions;
}
```

---

### Permission.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permission")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @Column(name = "code", length = 80, nullable = false, unique = true)
    private String code;

    @Column(name = "label", length = 150, nullable = false)
    private String label;

    @Column(name = "module", length = 60, nullable = false)
    private String module;
}
```

---

### Project.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "project")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name", length = 150, nullable = false)
    private String projectName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "IN_PROGRESS";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "project")
    private List<SourceFile> sourceFiles;
}
```

---

### SourceFile.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "source_file")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_type", length = 20, nullable = false)
    private String fileType;

    @Column(name = "storage_path", length = 500, nullable = false)
    private String storagePath;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "processing_status", length = 30, nullable = false)
    @Builder.Default
    private String processingStatus = "RECEIVED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sourceFile")
    private List<Dataset> datasets;
}
```

---

### Dataset.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "dataset")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(name = "dataset_name", length = 150, nullable = false)
    private String datasetName;

    @Column(name = "row_count", nullable = false)
    @Builder.Default
    private Integer rowCount = 0;

    @Column(name = "column_count", nullable = false)
    @Builder.Default
    private Integer columnCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_cleaned", nullable = false)
    @Builder.Default
    private Boolean isCleaned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private SourceFile sourceFile;

    @OneToMany(mappedBy = "dataset")
    private List<DatasetColumn> columns;

    @OneToOne(mappedBy = "dataset", cascade = CascadeType.ALL)
    private ExplorationReport explorationReport;

    @OneToMany(mappedBy = "dataset")
    private List<CleaningHistory> cleaningHistories;

    @OneToMany(mappedBy = "dataset")
    private List<AnalysisExecution> analysisExecutions;
}
```

---

### DatasetColumn.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "dataset_column")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "column_id")
    private Long columnId;

    @Column(name = "original_name", length = 150, nullable = false)
    private String originalName;

    @Column(name = "normalized_name", length = 150)
    private String normalizedName;

    @Column(name = "detected_type", length = 30, nullable = false)
    private String detectedType;

    @Column(name = "target_type", length = 30)
    private String targetType;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "null_count", nullable = false)
    @Builder.Default
    private Integer nullCount = 0;

    @Column(name = "unique_count", nullable = false)
    @Builder.Default
    private Integer uniqueCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @OneToMany(mappedBy = "column")
    private List<CleaningRule> cleaningRules;
}
```

---

### ExplorationReport.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exploration_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplorationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    @Column(name = "duplicate_count", nullable = false)
    @Builder.Default
    private Integer duplicateCount = 0;

    @Column(name = "missing_values_count", nullable = false)
    @Builder.Default
    private Integer missingValuesCount = 0;

    @Column(name = "quality_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal qualityScore = BigDecimal.ZERO;

    @Column(name = "report_pdf_path", length = 500)
    private String reportPdfPath;

    @OneToOne
    @JoinColumn(name = "dataset_id", nullable = false, unique = true)
    private Dataset dataset;
}
```

---

### CleaningRule.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "cleaning_rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "rule_type", length = 40, nullable = false)
    private String ruleType;

    @Column(name = "parameters_json", columnDefinition = "TEXT")
    private String parametersJson;

    @Column(name = "execution_order", nullable = false)
    @Builder.Default
    private Integer executionOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id", nullable = false)
    private DatasetColumn column;

    @OneToMany(mappedBy = "rule")
    private List<CleaningHistory> cleaningHistories;
}
```

---

### CleaningHistory.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cleaning_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private CleaningRule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

---

### PredefinedAnalysis.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "predefined_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredefinedAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @Column(name = "analysis_name", length = 150, nullable = false)
    private String analysisName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 60, nullable = false)
    private String category;

    @Column(name = "reference_script", length = 255, nullable = false)
    private String referenceScript;

    @Column(name = "required_parameters_json", columnDefinition = "TEXT")
    private String requiredParametersJson;

    @OneToMany(mappedBy = "predefinedAnalysis")
    private List<AnalysisExecution> analysisExecutions;
}
```

---

### AnalysisExecution.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "analysis_execution")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long executionId;

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "IN_PROGRESS";

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "used_parameters_json", columnDefinition = "TEXT")
    private String usedParametersJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private PredefinedAnalysis predefinedAnalysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "execution")
    private List<AnalysisResult> results;

    @OneToMany(mappedBy = "execution")
    private List<Export> exports;
}
```

---

### AnalysisResult.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analysis_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "result_type", length = 20, nullable = false)
    private String resultType;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;

    @Column(name = "file_format", length = 10, nullable = false)
    private String fileFormat;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private AnalysisExecution execution;

    @OneToOne(mappedBy = "result", cascade = CascadeType.ALL)
    private Chart chart;
}
```

---

### Chart.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chart")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chart_id")
    private Long chartId;

    @Column(name = "chart_type", length = 30, nullable = false)
    private String chartType;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @OneToOne
    @JoinColumn(name = "result_id", nullable = false, unique = true)
    private AnalysisResult result;
}
```

---

### Export.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "export")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Export {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "export_id")
    private Long exportId;

    @Column(name = "export_format", length = 10, nullable = false)
    private String exportFormat;

    @CreationTimestamp
    @Column(name = "exported_at", nullable = false, updatable = false)
    private LocalDateTime exportedAt;

    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private AnalysisExecution execution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

---

### AuditLog.java

```java
package com.hff.catalyst.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "action", length = 100, nullable = false)
    private String action;

    @Column(name = "concerned_entity", length = 60)
    private String concernedEntity;

    @Column(name = "entity_id")
    private Long entityId;

    @CreationTimestamp
    @Column(name = "action_date", nullable = false, updatable = false)
    private LocalDateTime actionDate;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

---

## 2. Enums pour les valeurs constantes

### FileType.java

```java
package com.hff.catalyst.enums;

public enum FileType {
    CSV, EXCEL, SQL
}
```

### FileProcessingStatus.java

```java
package com.hff.catalyst.enums;

public enum FileProcessingStatus {
    RECEIVED, ANALYZING, EXPLORED, CLEANED, ERROR
}
```

### ProjectStatus.java

```java
package com.hff.catalyst.enums;

public enum ProjectStatus {
    IN_PROGRESS, COMPLETED, ARCHIVED
}
```

### ColumnType.java

```java
package com.hff.catalyst.enums;

public enum ColumnType {
    STRING, INTEGER, FLOAT, DATE, BOOLEAN
}
```

### RuleType.java

```java
package com.hff.catalyst.enums;

public enum RuleType {
    TYPE_CONVERSION, DUPLICATE_REMOVAL, NULL_IMPUTATION, REGEX_CLEANING, TRIM, VALUE_CONSTRAINT
}
```

### AnalysisCategory.java

```java
package com.hff.catalyst.enums;

public enum AnalysisCategory {
    STATISTICAL, CORRELATION, TEMPORAL, SEGMENTATION, FINANCIAL
}
```

### ExecutionStatus.java

```java
package com.hff.catalyst.enums;

public enum ExecutionStatus {
    IN_PROGRESS, COMPLETED, ERROR
}
```

### ResultType.java

```java
package com.hff.catalyst.enums;

public enum ResultType {
    TABLE, CHART, KPI
}
```

### ChartType.java

```java
package com.hff.catalyst.enums;

public enum ChartType {
    BAR, LINE, PIE, SCATTER, AREA
}
```

### ExportFormat.java

```java
package com.hff.catalyst.enums;

public enum ExportFormat {
    CSV, XLSX, PNG, ZIP
}
```

---

## 3. Structure des packages recommandée

```
com.hff.catalyst
├── entity
│   ├── User.java
│   ├── UserCategory.java
│   ├── Permission.java
│   ├── Project.java
│   ├── SourceFile.java
│   ├── Dataset.java
│   ├── DatasetColumn.java
│   ├── ExplorationReport.java
│   ├── CleaningRule.java
│   ├── CleaningHistory.java
│   ├── PredefinedAnalysis.java
│   ├── AnalysisExecution.java
│   ├── AnalysisResult.java
│   ├── Chart.java
│   ├── Export.java
│   └── AuditLog.java
├── enums
│   ├── FileType.java
│   ├── FileProcessingStatus.java
│   ├── ProjectStatus.java
│   ├── ColumnType.java
│   ├── RuleType.java
│   ├── AnalysisCategory.java
│   ├── ExecutionStatus.java
│   ├── ResultType.java
│   ├── ChartType.java
│   └── ExportFormat.java
├── repository
│   ├── UserRepository.java
│   ├── UserCategoryRepository.java
│   ├── PermissionRepository.java
│   ├── ProjectRepository.java
│   ├── SourceFileRepository.java
│   ├── DatasetRepository.java
│   ├── DatasetColumnRepository.java
│   ├── ExplorationReportRepository.java
│   ├── CleaningRuleRepository.java
│   ├── CleaningHistoryRepository.java
│   ├── PredefinedAnalysisRepository.java
│   ├── AnalysisExecutionRepository.java
│   ├── AnalysisResultRepository.java
│   ├── ChartRepository.java
│   ├── ExportRepository.java
│   └── AuditLogRepository.java
├── service
│   ├── UserService.java
│   ├── ProjectService.java
│   ├── FileService.java
│   ├── DatasetService.java
│   ├── CleaningService.java
│   ├── AnalysisService.java
│   ├── ExportService.java
│   └── AuditService.java
├── controller
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ProjectController.java
│   ├── FileController.java
│   ├── DatasetController.java
│   ├── CleaningController.java
│   ├── AnalysisController.java
│   ├── ExportController.java
│   └── AdminController.java
├── dto
│   ├── request
│   │   ├── LoginRequest.java
│   │   ├── UserCreationRequest.java
│   │   ├── ProjectCreationRequest.java
│   │   ├── CleaningRuleRequest.java
│   │   ├── AnalysisExecutionRequest.java
│   │   └── ExportRequest.java
│   └── response
│       ├── TokenResponse.java
│       ├── UserResponse.java
│       ├── ProjectResponse.java
│       ├── FileResponse.java
│       ├── DatasetResponse.java
│       ├── ExplorationReportResponse.java
│       ├── AnalysisExecutionResponse.java
│       └── ExportResponse.java
├── config
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtUtil.java
└── exception
    ├── ResourceNotFoundException.java
    ├── UnauthorizedException.java
    └── GlobalExceptionHandler.java
```

---

## 4. Exemple de Repository

### UserRepository.java

```java
package com.hff.catalyst.repository;

import com.hff.catalyst.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Page<User> findAllByIsActiveTrue(Pageable pageable);

    boolean existsByEmail(String email);

    Page<User> findByLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(
        String lastName, String firstName, Pageable pageable
    );
}
```

### DatasetRepository.java

```java
package com.hff.catalyst.repository;

import com.hff.catalyst.entity.Dataset;
import com.hff.catalyst.entity.SourceFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {

    List<Dataset> findBySourceFile(SourceFile sourceFile);

    Page<Dataset> findBySourceFile_Project_ProjectId(Long projectId, Pageable pageable);

    Optional<Dataset> findByDatasetIdAndSourceFile_Project_ProjectId(Long datasetId, Long projectId);

    Page<Dataset> findByDatasetNameContainingIgnoreCase(String name, Pageable pageable);
}
```

---

## 5. Exemple de Service

### UserService.java

```java
package com.hff.catalyst.service;

import com.hff.catalyst.entity.User;
import com.hff.catalyst.entity.UserCategory;
import com.hff.catalyst.exception.ResourceNotFoundException;
import com.hff.catalyst.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User createUser(String lastName, String firstName, String email, String password, UserCategory category) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }

        User user = User.builder()
            .lastName(lastName)
            .firstName(firstName)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .isActive(true)
            .category(category)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User created: {} ({})", email, savedUser.getUserId());
        return savedUser;
    }

    @Transactional
    public User updateUser(Long userId, String lastName, String firstName, Boolean isActive, UserCategory category) {
        User user = getUserById(userId);

        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (isActive != null) {
            user.setIsActive(isActive);
        }
        if (category != null) {
            user.setCategory(category);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated: {} ({})", updatedUser.getEmail(), updatedUser.getUserId());
        return updatedUser;
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated: {} ({})", user.getEmail(), userId);
    }

    @Transactional
    public void updateLastLogin(String email) {
        User user = getUserByEmail(email);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.findByLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(
            searchTerm, searchTerm, pageable
        );
    }
}
```

---

## 6. Exemple de Configuration de Sécurité

### SecurityConfig.java

```java
package com.hff.catalyst.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/**").authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 7. Exemple de JWT Util

### JwtUtil.java

```java
package com.hff.catalyst.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

---

## 8. application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/catalyst
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
jwt.secret=your-256-bit-secret-key-for-jwt-signing-please-change-in-production
jwt.expiration=900000
jwt.refresh-expiration=604800000

# File Upload
file.upload-dir=./uploads
file.max-size=200MB

# CORS
cors.allowed-origins=http://localhost:3000,http://localhost:3001
```
Parfait ! Passons aux DTOs (Data Transfer Objects) pour toutes les entités. Je vais structurer les packages `request` et `response` comme demandé.

---

## Structure des packages

```
com.henri_fraise.hff_data_studio
├── dto
│   ├── request
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── UserCreationRequest.java
│   │   ├── UserUpdateRequest.java
│   │   ├── CategoryCreationRequest.java
│   │   ├── ProjectCreationRequest.java
│   │   ├── ProjectUpdateRequest.java
│   │   ├── FileUploadRequest.java
│   │   ├── DatasetColumnUpdateRequest.java
│   │   ├── CleaningRuleRequest.java
│   │   ├── AnalysisExecutionRequest.java
│   │   ├── ExportRequest.java
│   │   └── PermissionUpdateRequest.java
│   └── response
│       ├── TokenResponse.java
│       ├── UserResponse.java
│       ├── UserCategoryResponse.java
│       ├── PermissionResponse.java
│       ├── ProjectResponse.java
│       ├── SourceFileResponse.java
│       ├── DatasetResponse.java
│       ├── DatasetColumnResponse.java
│       ├── ExplorationReportResponse.java
│       ├── CleaningRuleResponse.java
│       ├── CleaningHistoryResponse.java
│       ├── PredefinedAnalysisResponse.java
│       ├── AnalysisExecutionResponse.java
│       ├── AnalysisResultResponse.java
│       ├── ChartResponse.java
│       ├── ExportResponse.java
│       ├── AuditLogResponse.java
│       ├── PageResponse.java
│       └── ErrorResponse.java
```

---

## 1. Request DTOs

### LoginRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
```

---

### RefreshTokenRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
```

---

### UserCreationRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.Email;
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
@AllArgsConstructor
public class UserCreationRequest {

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}
```

---

### UserUpdateRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String lastName;

    private String firstName;

    @Email(message = "Email must be valid")
    private String email;

    private Boolean isActive;

    private UUID categoryId;

    private String newPassword;
}
```

---

### CategoryCreationRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreationRequest {

    @NotBlank(message = "Category label is required")
    private String label;

    private String description;

    @NotNull(message = "Access level is required")
    private Integer accessLevel;
}
```

---

### ProjectCreationRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreationRequest {

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String description;
}
```

---

### ProjectUpdateRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String description;

    private ProjectStatus status;
}
```

---

### FileUploadRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.FileType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotNull(message = "File type is required")
    private FileType fileType;
}
```

---

### DatasetColumnUpdateRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.ColumnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetColumnUpdateRequest {

    private String normalizedName;

    private ColumnType targetType;
}
```

---

### CleaningRuleRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.RuleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningRuleRequest {

    @NotNull(message = "Rule type is required")
    private RuleType ruleType;

    private Map<String, Object> parametersJson;

    private Integer executionOrder;

    @Builder.Default
    private Boolean isActive = true;
}
```

---

### AnalysisExecutionRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisExecutionRequest {

    @NotNull(message = "Dataset ID is required")
    private UUID datasetId;

    private UUID analysisId;

    private Map<String, Object> parameters;
}
```

---

### ExportRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    @NotNull(message = "Execution ID is required")
    private UUID executionId;

    @NotNull(message = "Export format is required")
    private ExportFormat exportFormat;

    private List<UUID> resultIds;
}
```

---

### PermissionUpdateRequest.java

```java
package com.henri_fraise.hff_data_studio.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateRequest {

    private List<UUID> permissionIds;
}
```

---

## 2. Response DTOs

### TokenResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private String tokenType;

    @Builder.Default
    private String tokenType = "Bearer";
}
```

---

### UserResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID userId;

    private String lastName;

    private String firstName;

    private String email;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    private UserCategoryResponse category;
}
```

---

### UserCategoryResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryResponse {

    private UUID categoryId;

    private String label;

    private String description;

    private Integer accessLevel;

    private List<PermissionResponse> permissions;
}
```

---

### PermissionResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private UUID permissionId;

    private String code;

    private String label;

    private String module;
}
```

---

### ProjectResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private UUID projectId;

    private String projectName;

    private String description;

    private LocalDateTime createdAt;

    private ProjectStatus status;

    private UUID creatorUserId;

    private String creatorFullName;

    private Long fileCount;

    private Long datasetCount;
}
```

---

### SourceFileResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import com.henri_fraise.hff_data_studio.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceFileResponse {

    private UUID fileId;

    private String fileName;

    private FileType fileType;

    private String storagePath;

    private Long sizeBytes;

    private String sizeFormatted;

    private LocalDateTime uploadedAt;

    private FileProcessingStatus processingStatus;

    private UUID projectId;

    private String projectName;

    private UUID userId;

    private String userFullName;

    private Integer datasetCount;
}
```

---

### DatasetResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetResponse {

    private UUID datasetId;

    private String datasetName;

    private Integer rowCount;

    private Integer columnCount;

    private LocalDateTime createdAt;

    private Boolean isCleaned;

    private UUID fileId;

    private String fileName;

    private UUID projectId;

    private String projectName;

    private Integer qualityScore;

    private Long analysisCount;
}
```

---

### DatasetColumnResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ColumnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetColumnResponse {

    private UUID columnId;

    private String originalName;

    private String normalizedName;

    private ColumnType detectedType;

    private ColumnType targetType;

    private Integer position;

    private Integer nullCount;

    private Double nullPercentage;

    private Integer uniqueCount;

    private Double uniquePercentage;

    private Integer cleaningRuleCount;

    private UUID datasetId;
}
```

---

### ExplorationReportResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplorationReportResponse {

    private UUID reportId;

    private LocalDateTime generatedAt;

    private Integer totalRows;

    private Integer duplicateCount;

    private Integer missingValuesCount;

    private BigDecimal qualityScore;

    private String reportPdfPath;

    private UUID datasetId;

    private String datasetName;

    // Statistiques détaillées par colonne
    private Object columnStatistics;
}
```

---

### CleaningRuleResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.RuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningRuleResponse {

    private UUID ruleId;

    private RuleType ruleType;

    private String ruleTypeLabel;

    private Map<String, Object> parametersJson;

    private Integer executionOrder;

    private Boolean isActive;

    private UUID columnId;

    private String columnName;

    private UUID datasetId;

    private String datasetName;
}
```

---

### CleaningHistoryResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningHistoryResponse {

    private UUID historyId;

    private LocalDateTime executedAt;

    private CleaningHistoryStatus status;

    private String statusLabel;

    private String details;

    private UUID datasetId;

    private String datasetName;

    private UUID ruleId;

    private String ruleType;

    private UUID userId;

    private String userFullName;
}
```

---

### PredefinedAnalysisResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredefinedAnalysisResponse {

    private UUID analysisId;

    private String analysisName;

    private String description;

    private String category;

    private String categoryLabel;

    private String referenceScript;

    private Map<String, Object> requiredParametersJson;

    private Long executionCount;
}
```

---

### AnalysisExecutionResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisExecutionResponse {

    private UUID executionId;

    private LocalDateTime executedAt;

    private AnalysisExecutionStatus status;

    private String statusLabel;

    private Integer durationMs;

    private String durationFormatted;

    private Map<String, Object> usedParametersJson;

    private UUID datasetId;

    private String datasetName;

    private UUID analysisId;

    private String analysisName;

    private String analysisCategory;

    private UUID userId;

    private String userFullName;

    private List<AnalysisResultResponse> results;

    private Integer resultCount;
}
```

---

### AnalysisResultResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ResultType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultResponse {

    private UUID resultId;

    private ResultType resultType;

    private String resultTypeLabel;

    private String title;

    private String filePath;

    private String fileFormat;

    private Integer displayOrder;

    private UUID executionId;

    private ChartResponse chart;

    private String downloadUrl;
}
```

---

### ChartResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ChartType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponse {

    private UUID chartId;

    private ChartType chartType;

    private String chartTypeLabel;

    private Map<String, Object> configJson;

    private UUID resultId;
}
```

---

### ExportResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponse {

    private UUID exportId;

    private ExportFormat exportFormat;

    private String formatLabel;

    private LocalDateTime exportedAt;

    private String filePath;

    private String fileName;

    private Long fileSize;

    private String fileSizeFormatted;

    private UUID executionId;

    private String executionStatus;

    private UUID userId;

    private String userFullName;

    private String downloadUrl;
}
```

---

### AuditLogResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID logId;

    private String action;

    private String concernedEntity;

    private UUID entityId;

    private LocalDateTime actionDate;

    private String ipAddress;

    private UUID userId;

    private String userFullName;

    private String userEmail;
}
```

---

### PageResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;

    private Integer pageNumber;

    private Integer pageSize;

    private Long totalElements;

    private Integer totalPages;

    private Boolean isFirst;

    private Boolean isLast;

    private Boolean hasNext;

    private Boolean hasPrevious;

    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .isFirst(page.isFirst())
            .isLast(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
```

---

### ErrorResponse.java

```java
package com.henri_fraise.hff_data_studio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code;

    private String message;

    private String details;

    private LocalDateTime timestamp;

    private String path;

    private String method;

    private Integer status;

    private List<String> errors;

    private Map<String, String> validationErrors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
```

---

## 3. Mappers (pour la conversion Entity ↔ DTO)

### UserMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserCategoryResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final UserCategoryMapper categoryMapper;

    public UserMapper(UserCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserCategoryResponse categoryResponse = null;
        if (user.getCategory() != null) {
            categoryResponse = categoryMapper.toResponse(user.getCategory());
        }

        return UserResponse.builder()
            .userId(user.getId())
            .lastName(user.getLastName())
            .firstName(user.getFirstName())
            .email(user.getEmail())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .lastLogin(user.getLastLogin())
            .category(categoryResponse)
            .build();
    }
}
```

---

### UserCategoryMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.UserCategoryResponse;
import com.henri_fraise.hff_data_studio.dto.response.PermissionResponse;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserCategoryMapper {

    private final PermissionMapper permissionMapper;

    public UserCategoryMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public UserCategoryResponse toResponse(UserCategory category) {
        if (category == null) {
            return null;
        }

        return UserCategoryResponse.builder()
            .categoryId(category.getId())
            .label(category.getLabel())
            .description(category.getDescription())
            .accessLevel(category.getAccessLevel())
            .permissions(
                category.getPermissions() != null
                    ? category.getPermissions().stream()
                        .map(permissionMapper::toResponse)
                        .collect(Collectors.toList())
                    : null
            )
            .build();
    }
}
```

---

### PermissionMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.PermissionResponse;
import com.henri_fraise.hff_data_studio.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionResponse.builder()
            .permissionId(permission.getId())
            .code(permission.getCode())
            .label(permission.getLabel())
            .module(permission.getModule())
            .build();
    }
}
```

---

### ProjectMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectResponse.builder()
            .projectId(project.getId())
            .projectName(project.getProjectName())
            .description(project.getDescription())
            .createdAt(project.getCreatedAt())
            .status(project.getStatus())
            .creatorUserId(project.getCreator() != null ? project.getCreator().getId() : null)
            .creatorFullName(
                project.getCreator() != null
                    ? project.getCreator().getFirstName() + " " + project.getCreator().getLastName()
                    : null
            )
            .fileCount(
                project.getSourceFiles() != null ? (long) project.getSourceFiles().size() : 0L
            )
            .datasetCount(
                project.getSourceFiles() != null
                    ? project.getSourceFiles().stream()
                        .mapToLong(f -> f.getDatasets() != null ? f.getDatasets().size() : 0)
                        .sum()
                    : 0L
            )
            .build();
    }
}
```

---

### DatasetMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import org.springframework.stereotype.Component;

@Component
public class DatasetMapper {

    public DatasetResponse toResponse(Dataset dataset) {
        if (dataset == null) {
            return null;
        }

        return DatasetResponse.builder()
            .datasetId(dataset.getId())
            .datasetName(dataset.getDatasetName())
            .rowCount(dataset.getRowCount())
            .columnCount(dataset.getColumnCount())
            .createdAt(dataset.getCreatedAt())
            .isCleaned(dataset.getIsCleaned())
            .fileId(dataset.getSourceFile() != null ? dataset.getSourceFile().getId() : null)
            .fileName(dataset.getSourceFile() != null ? dataset.getSourceFile().getFileName() : null)
            .projectId(
                dataset.getSourceFile() != null && dataset.getSourceFile().getProject() != null
                    ? dataset.getSourceFile().getProject().getId()
                    : null
            )
            .projectName(
                dataset.getSourceFile() != null && dataset.getSourceFile().getProject() != null
                    ? dataset.getSourceFile().getProject().getProjectName()
                    : null
            )
            .qualityScore(
                dataset.getExplorationReport() != null
                    ? dataset.getExplorationReport().getQualityScore().intValue()
                    : null
            )
            .analysisCount(
                dataset.getAnalysisExecutions() != null
                    ? (long) dataset.getAnalysisExecutions().size()
                    : 0L
            )
            .build();
    }
}
```

---

## 4. Exemple de Controller utilisant les DTOs

### UserController.java

```java
package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(PageResponse.from(users));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updated = userService.updateUser(userId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Résumé des DTOs créés

### Request DTOs (13)
1. `LoginRequest` - Connexion
2. `RefreshTokenRequest` - Rafraîchissement token
3. `UserCreationRequest` - Création utilisateur
4. `UserUpdateRequest` - Mise à jour utilisateur
5. `CategoryCreationRequest` - Création catégorie
6. `ProjectCreationRequest` - Création projet
7. `ProjectUpdateRequest` - Mise à jour projet
8. `FileUploadRequest` - Upload fichier
9. `DatasetColumnUpdateRequest` - Mise à jour colonne
10. `CleaningRuleRequest` - Création règle nettoyage
11. `AnalysisExecutionRequest` - Lancement analyse
12. `ExportRequest` - Export résultats
13. `PermissionUpdateRequest` - Mise à jour permissions

### Response DTOs (19)
1. `TokenResponse` - Réponse authentification
2. `UserResponse` - Utilisateur
3. `UserCategoryResponse` - Catégorie utilisateur
4. `PermissionResponse` - Permission
5. `ProjectResponse` - Projet
6. `SourceFileResponse` - Fichier source
7. `DatasetResponse` - Dataset
8. `DatasetColumnResponse` - Colonne dataset
9. `ExplorationReportResponse` - Rapport exploration
10. `CleaningRuleResponse` - Règle nettoyage
11. `CleaningHistoryResponse` - Historique nettoyage
12. `PredefinedAnalysisResponse` - Analyse prédéfinie
13. `AnalysisExecutionResponse` - Exécution analyse
14. `AnalysisResultResponse` - Résultat analyse
15. `ChartResponse` - Graphique
16. `ExportResponse` - Export
17. `AuditLogResponse` - Journal audit
18. `PageResponse<T>` - Pagination générique
19. `ErrorResponse` - Erreur

### Mappers (4)
1. `UserMapper`
2. `UserCategoryMapper`
3. `PermissionMapper`
4. `ProjectMapper`
5. `DatasetMapper`