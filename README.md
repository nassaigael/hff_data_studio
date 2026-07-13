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

Voici tous les mappers pour convertir les entités en DTOs de réponse, et inversement pour les requêtes.

---

## Structure des packages

```
com.henri_fraise.hff_data_studio.mapper
├── UserMapper.java
├── UserCategoryMapper.java
├── PermissionMapper.java
├── ProjectMapper.java
├── SourceFileMapper.java
├── DatasetMapper.java
├── DatasetColumnMapper.java
├── ExplorationReportMapper.java
├── CleaningRuleMapper.java
├── CleaningHistoryMapper.java
├── PredefinedAnalysisMapper.java
├── AnalysisExecutionMapper.java
├── AnalysisResultMapper.java
├── ChartMapper.java
├── ExportMapper.java
├── AuditLogMapper.java
└── PageMapper.java
```

---

## 1. UserMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

	private final UserCategoryMapper categoryMapper;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserResponse toResponse(User user) {
		if (user == null) {
			return null;
		}

		return UserResponse.builder()
				.userId(user.getId())
				.lastName(user.getLastName())
				.firstName(user.getFirstName())
				.email(user.getEmail())
				.isActive(user.getIsActive())
				.createdAt(user.getCreatedAt())
				.lastLogin(user.getLastLogin())
				.category(categoryMapper.toResponse(user.getCategory()))
				.build();
	}

	public User toEntity(UserCreationRequest request, UserCategory category) {
		if (request == null) {
			return null;
		}

		return User.builder()
				.lastName(request.getLastName())
				.firstName(request.getFirstName())
				.email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getNewPassword()))
				.isActive(true)
				.category(category)
				.build();
	}

	public void updateEntity(User user, UserUpdateRequest request, UserCategory category) {
		if (request == null || user == null) {
			return;
		}

		if (request.getLastName() != null) {
			user.setLastName(request.getLastName());
		}
		if (request.getFirstName() != null) {
			user.setFirstName(request.getFirstName());
		}
		if (request.getEmail() != null) {
			user.setEmail(request.getEmail());
		}
		if (request.getIsActive() != null) {
			user.setIsActive(request.getIsActive());
		}
		if (category != null) {
			user.setCategory(category);
		}
		if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
			user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		}
	}
}
```

---

## 2. UserCategoryMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.CategoryCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserCategoryResponse;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserCategoryMapper {

    private final PermissionMapper permissionMapper;

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

    public UserCategory toEntity(CategoryCreationRequest request) {
        if (request == null) {
            return null;
        }

        return UserCategory.builder()
            .label(request.getLabel())
            .description(request.getDescription())
            .accessLevel(request.getAccessLevel())
            .build();
    }
}
```

---

## 3. PermissionMapper.java

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

## 4. ProjectMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.ProjectCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.ProjectUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
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

    public Project toEntity(ProjectCreationRequest request, User creator) {
        if (request == null) {
            return null;
        }

        return Project.builder()
            .projectName(request.getProjectName())
            .description(request.getDescription())
            .status(ProjectStatus.IN_PROGRESS)
            .creator(creator)
            .build();
    }

    public void updateEntity(Project project, ProjectUpdateRequest request) {
        if (project == null || request == null) {
            return;
        }

        if (request.getProjectName() != null) {
            project.setProjectName(request.getProjectName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
    }
}
```

---

## 5. SourceFileMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.SourceFileResponse;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import org.springframework.stereotype.Component;

@Component
public class SourceFileMapper {

    public SourceFileResponse toResponse(SourceFile file) {
        if (file == null) {
            return null;
        }

        return SourceFileResponse.builder()
            .fileId(file.getId())
            .fileName(file.getFileName())
            .fileType(file.getFileType())
            .storagePath(file.getStoragePath())
            .sizeBytes(file.getSizeBytes())
            .sizeFormatted(formatFileSize(file.getSizeBytes()))
            .uploadedAt(file.getUploadedAt())
            .processingStatus(file.getProcessingStatus())
            .projectId(file.getProject() != null ? file.getProject().getId() : null)
            .projectName(file.getProject() != null ? file.getProject().getProjectName() : null)
            .userId(file.getUser() != null ? file.getUser().getId() : null)
            .userFullName(
                file.getUser() != null
                    ? file.getUser().getFirstName() + " " + file.getUser().getLastName()
                    : null
            )
            .datasetCount(file.getDatasets() != null ? file.getDatasets().size() : 0)
            .build();
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null) {
            return "0 B";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
```

---

## 6. DatasetMapper.java

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

## 7. DatasetColumnMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.DatasetColumnUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.DatasetColumnResponse;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class DatasetColumnMapper {

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public DatasetColumnResponse toResponse(DatasetColumn column) {
        if (column == null) {
            return null;
        }

        int rowCount = column.getDataset() != null ? column.getDataset().getRowCount() : 0;
        double nullPercentage = rowCount > 0 ? (column.getNullCount() * 100.0) / rowCount : 0;
        double uniquePercentage = rowCount > 0 ? (column.getUniqueCount() * 100.0) / rowCount : 0;

        return DatasetColumnResponse.builder()
            .columnId(column.getId())
            .originalName(column.getOriginalName())
            .normalizedName(column.getNormalizedName())
            .detectedType(column.getDetectedType())
            .targetType(column.getTargetType())
            .position(column.getPosition())
            .nullCount(column.getNullCount())
            .nullPercentage(Double.valueOf(df.format(nullPercentage)))
            .uniqueCount(column.getUniqueCount())
            .uniquePercentage(Double.valueOf(df.format(uniquePercentage)))
            .cleaningRuleCount(column.getCleaningRules() != null ? column.getCleaningRules().size() : 0)
            .datasetId(column.getDataset() != null ? column.getDataset().getId() : null)
            .build();
    }

    public void updateEntity(DatasetColumn column, DatasetColumnUpdateRequest request) {
        if (column == null || request == null) {
            return;
        }

        if (request.getNormalizedName() != null) {
            column.setNormalizedName(request.getNormalizedName());
        }
        if (request.getTargetType() != null) {
            column.setTargetType(request.getTargetType());
        }
    }
}
```

---

## 8. ExplorationReportMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ExplorationReportResponse;
import com.henri_fraise.hff_data_studio.entity.ExplorationReport;
import org.springframework.stereotype.Component;

@Component
public class ExplorationReportMapper {

    public ExplorationReportResponse toResponse(ExplorationReport report) {
        if (report == null) {
            return null;
        }

        return ExplorationReportResponse.builder()
            .reportId(report.getId())
            .generatedAt(report.getGeneratedAt())
            .totalRows(report.getTotalRows())
            .duplicateCount(report.getDuplicateCount())
            .missingValuesCount(report.getMissingValuesCount())
            .qualityScore(report.getQualityScore())
            .reportPdfPath(report.getReportPdfPath())
            .datasetId(report.getDataset() != null ? report.getDataset().getId() : null)
            .datasetName(report.getDataset() != null ? report.getDataset().getDatasetName() : null)
            .build();
    }
}
```

---

## 9. CleaningRuleMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.CleaningRuleRequest;
import com.henri_fraise.hff_data_studio.dto.response.CleaningRuleResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import org.springframework.stereotype.Component;

@Component
public class CleaningRuleMapper {

    public CleaningRuleResponse toResponse(CleaningRule rule) {
        if (rule == null) {
            return null;
        }

        return CleaningRuleResponse.builder()
            .ruleId(rule.getId())
            .ruleType(rule.getRuleType())
            .ruleTypeLabel(getRuleTypeLabel(rule.getRuleType()))
            .parametersJson(rule.getParametersJson())
            .executionOrder(rule.getExecutionOrder())
            .isActive(rule.getIsActive())
            .columnId(rule.getColumn() != null ? rule.getColumn().getId() : null)
            .columnName(rule.getColumn() != null ? rule.getColumn().getOriginalName() : null)
            .datasetId(
                rule.getColumn() != null && rule.getColumn().getDataset() != null
                    ? rule.getColumn().getDataset().getId()
                    : null
            )
            .datasetName(
                rule.getColumn() != null && rule.getColumn().getDataset() != null
                    ? rule.getColumn().getDataset().getDatasetName()
                    : null
            )
            .build();
    }

    public CleaningRule toEntity(CleaningRuleRequest request, DatasetColumn column) {
        if (request == null) {
            return null;
        }

        return CleaningRule.builder()
            .ruleType(request.getRuleType())
            .parametersJson(request.getParametersJson())
            .executionOrder(
                request.getExecutionOrder() != null
                    ? request.getExecutionOrder()
                    : 0
            )
            .isActive(
                request.getIsActive() != null
                    ? request.getIsActive()
                    : true
            )
            .column(column)
            .build();
    }

    public void updateEntity(CleaningRule rule, CleaningRuleRequest request) {
        if (rule == null || request == null) {
            return;
        }

        if (request.getRuleType() != null) {
            rule.setRuleType(request.getRuleType());
        }
        if (request.getParametersJson() != null) {
            rule.setParametersJson(request.getParametersJson());
        }
        if (request.getExecutionOrder() != null) {
            rule.setExecutionOrder(request.getExecutionOrder());
        }
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }
    }

    private String getRuleTypeLabel(RuleType ruleType) {
        if (ruleType == null) {
            return null;
        }
        return switch (ruleType) {
            case TYPE_CONVERSION -> "Type Conversion";
            case DUPLICATE_REMOVAL -> "Duplicate Removal";
            case NULL_IMPUTATION -> "Null Imputation";
            case REGEX_CLEANING -> "Regex Cleaning";
            case TRIM -> "Trim Whitespace";
            case VALUE_CONSTRAINT -> "Value Constraint";
        };
    }
}
```

---

## 10. CleaningHistoryMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.CleaningHistoryResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningHistory;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import org.springframework.stereotype.Component;

@Component
public class CleaningHistoryMapper {

    public CleaningHistoryResponse toResponse(CleaningHistory history) {
        if (history == null) {
            return null;
        }

        return CleaningHistoryResponse.builder()
            .historyId(history.getId())
            .executedAt(history.getExecutedAt())
            .status(history.getStatus())
            .statusLabel(getStatusLabel(history.getStatus()))
            .details(history.getDetails())
            .datasetId(history.getDataset() != null ? history.getDataset().getId() : null)
            .datasetName(history.getDataset() != null ? history.getDataset().getDatasetName() : null)
            .ruleId(history.getRule() != null ? history.getRule().getId() : null)
            .ruleType(history.getRule() != null ? history.getRule().getRuleType().name() : null)
            .userId(history.getUser() != null ? history.getUser().getId() : null)
            .userFullName(
                history.getUser() != null
                    ? history.getUser().getFirstName() + " " + history.getUser().getLastName()
                    : null
            )
            .build();
    }

    private String getStatusLabel(CleaningHistoryStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case SUCCESS -> "Success";
            case FAILURE -> "Failure";
            case PARTIAL_SUCCESS -> "Partial Success";
            case IN_PROGRESS -> "In Progress";
            case CANCELLED -> "Cancelled";
            case PENDING -> "Pending";
        };
    }
}
```

---

## 11. PredefinedAnalysisMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.PredefinedAnalysisResponse;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import org.springframework.stereotype.Component;

@Component
public class PredefinedAnalysisMapper {

    public PredefinedAnalysisResponse toResponse(PredefinedAnalysis analysis) {
        if (analysis == null) {
            return null;
        }

        return PredefinedAnalysisResponse.builder()
            .analysisId(analysis.getId())
            .analysisName(analysis.getAnalysisName())
            .description(analysis.getDescription())
            .category(analysis.getCategory())
            .categoryLabel(getCategoryLabel(analysis.getCategory()))
            .referenceScript(analysis.getReferenceScript())
            .requiredParametersJson(analysis.getRequiredParametersJson())
            .executionCount(
                analysis.getAnalysisExecutions() != null
                    ? (long) analysis.getAnalysisExecutions().size()
                    : 0L
            )
            .build();
    }

    private String getCategoryLabel(String category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case "STATISTICAL" -> "Statistical Analysis";
            case "CORRELATION" -> "Correlation Analysis";
            case "TEMPORAL" -> "Temporal Analysis";
            case "SEGMENTATION" -> "Data Segmentation";
            case "FINANCIAL" -> "Financial Analysis";
            default -> category;
        };
    }
}
```

---

## 12. AnalysisExecutionMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.AnalysisExecutionRequest;
import com.henri_fraise.hff_data_studio.dto.response.AnalysisExecutionResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalysisExecutionMapper {

    private final AnalysisResultMapper resultMapper;

    public AnalysisExecutionResponse toResponse(AnalysisExecution execution) {
        if (execution == null) {
            return null;
        }

        return AnalysisExecutionResponse.builder()
            .executionId(execution.getId())
            .executedAt(execution.getExecutedAt())
            .status(execution.getStatus())
            .statusLabel(getStatusLabel(execution.getStatus()))
            .durationMs(execution.getDurationMs())
            .durationFormatted(formatDuration(execution.getDurationMs()))
            .usedParametersJson(execution.getUsedParametersJson())
            .datasetId(execution.getDataset() != null ? execution.getDataset().getId() : null)
            .datasetName(execution.getDataset() != null ? execution.getDataset().getDatasetName() : null)
            .analysisId(
                execution.getPredefinedAnalysis() != null
                    ? execution.getPredefinedAnalysis().getId()
                    : null
            )
            .analysisName(
                execution.getPredefinedAnalysis() != null
                    ? execution.getPredefinedAnalysis().getAnalysisName()
                    : "Custom Analysis"
            )
            .analysisCategory(
                execution.getPredefinedAnalysis() != null
                    ? execution.getPredefinedAnalysis().getCategory()
                    : null
            )
            .userId(execution.getUser() != null ? execution.getUser().getId() : null)
            .userFullName(
                execution.getUser() != null
                    ? execution.getUser().getFirstName() + " " + execution.getUser().getLastName()
                    : null
            )
            .results(
                execution.getResults() != null
                    ? execution.getResults().stream()
                        .map(resultMapper::toResponse)
                        .collect(Collectors.toList())
                    : null
            )
            .resultCount(execution.getResults() != null ? execution.getResults().size() : 0)
            .build();
    }

    public AnalysisExecution toEntity(
            AnalysisExecutionRequest request,
            Dataset dataset,
            PredefinedAnalysis predefinedAnalysis,
            User user) {
        if (request == null) {
            return null;
        }

        return AnalysisExecution.builder()
            .status(AnalysisExecutionStatus.IN_PROGRESS)
            .usedParametersJson(request.getParameters())
            .dataset(dataset)
            .predefinedAnalysis(predefinedAnalysis)
            .user(user)
            .build();
    }

    private String getStatusLabel(AnalysisExecutionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case IN_PROGRESS -> "In Progress";
            case COMPLETED -> "Completed";
            case ERROR -> "Error";
            case CANCELLED -> "Cancelled";
            case PENDING -> "Pending";
        };
    }

    private String formatDuration(Integer durationMs) {
        if (durationMs == null) {
            return null;
        }
        if (durationMs < 1000) {
            return durationMs + "ms";
        }
        if (durationMs < 60000) {
            return String.format("%.2fs", durationMs / 1000.0);
        }
        long minutes = durationMs / 60000;
        long seconds = (durationMs % 60000) / 1000;
        return String.format("%dm %ds", minutes, seconds);
    }
}
```

---

## 13. AnalysisResultMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.AnalysisResultResponse;
import com.henri_fraise.hff_data_studio.dto.response.ChartResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.enums.ResultType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisResultMapper {

    private final ChartMapper chartMapper;

    public AnalysisResultResponse toResponse(AnalysisResult result) {
        if (result == null) {
            return null;
        }

        ChartResponse chartResponse = null;
        if (result.getChart() != null) {
            chartResponse = chartMapper.toResponse(result.getChart());
        }

        return AnalysisResultResponse.builder()
            .resultId(result.getId())
            .resultType(result.getResultType())
            .resultTypeLabel(getResultTypeLabel(result.getResultType()))
            .title(result.getTitle())
            .filePath(result.getFilePath())
            .fileFormat(result.getFileFormat())
            .displayOrder(result.getDisplayOrder())
            .executionId(result.getExecution() != null ? result.getExecution().getId() : null)
            .chart(chartResponse)
            .downloadUrl("/api/v1/results/" + result.getId() + "/download")
            .build();
    }

    private String getResultTypeLabel(ResultType resultType) {
        if (resultType == null) {
            return null;
        }
        return switch (resultType) {
            case TABLE -> "Table";
            case CHART -> "Chart";
            case KPI -> "KPI";
        };
    }
}
```

---

## 14. ChartMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ChartResponse;
import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.ChartType;
import org.springframework.stereotype.Component;

@Component
public class ChartMapper {

    public ChartResponse toResponse(Chart chart) {
        if (chart == null) {
            return null;
        }

        return ChartResponse.builder()
            .chartId(chart.getId())
            .chartType(chart.getChartType())
            .chartTypeLabel(getChartTypeLabel(chart.getChartType()))
            .configJson(chart.getConfigJson())
            .resultId(chart.getResult() != null ? chart.getResult().getId() : null)
            .build();
    }

    private String getChartTypeLabel(ChartType chartType) {
        if (chartType == null) {
            return null;
        }
        return switch (chartType) {
            case BAR -> "Bar Chart";
            case LINE -> "Line Chart";
            case PIE -> "Pie Chart";
            case SCATTER -> "Scatter Plot";
            case AREA -> "Area Chart";
        };
    }
}
```

---

## 15. ExportMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ExportResponse;
import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import org.springframework.stereotype.Component;

@Component
public class ExportMapper {

    public ExportResponse toResponse(Export export) {
        if (export == null) {
            return null;
        }

        return ExportResponse.builder()
            .exportId(export.getId())
            .exportFormat(export.getExportFormat())
            .formatLabel(getFormatLabel(export.getExportFormat()))
            .exportedAt(export.getExportedAt())
            .filePath(export.getFilePath())
            .fileName(extractFileName(export.getFilePath()))
            .fileSize(export.getFileSize() != null ? export.getFileSize() : 0L)
            .fileSizeFormatted(formatFileSize(export.getFileSize()))
            .executionId(export.getExecution() != null ? export.getExecution().getId() : null)
            .executionStatus(
                export.getExecution() != null && export.getExecution().getStatus() != null
                    ? export.getExecution().getStatus().name()
                    : null
            )
            .userId(export.getUser() != null ? export.getUser().getId() : null)
            .userFullName(
                export.getUser() != null
                    ? export.getUser().getFirstName() + " " + export.getUser().getLastName()
                    : null
            )
            .downloadUrl("/api/v1/exports/" + export.getId() + "/download")
            .build();
    }

    private String getFormatLabel(ExportFormat format) {
        if (format == null) {
            return null;
        }
        return switch (format) {
            case CSV -> "CSV File";
            case XLSX -> "Excel File";
            case PNG -> "PNG Image";
            case ZIP -> "ZIP Archive";
        };
    }

    private String extractFileName(String filePath) {
        if (filePath == null) {
            return null;
        }
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash == -1) {
            lastSlash = filePath.lastIndexOf('\\');
        }
        return lastSlash == -1 ? filePath : filePath.substring(lastSlash + 1);
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
```

---

## 16. AuditLogMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.AuditLogResponse;
import com.henri_fraise.hff_data_studio.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog log) {
        if (log == null) {
            return null;
        }

        return AuditLogResponse.builder()
            .logId(log.getId())
            .action(log.getAction())
            .concernedEntity(log.getConcernedEntity())
            .entityId(log.getEntityId())
            .actionDate(log.getActionDate())
            .ipAddress(log.getIpAddress())
            .userId(log.getUser() != null ? log.getUser().getId() : null)
            .userFullName(
                log.getUser() != null
                    ? log.getUser().getFirstName() + " " + log.getUser().getLastName()
                    : null
            )
            .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
            .build();
    }
}
```

---

## 17. PageMapper.java

```java
package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PageMapper {

    public <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        if (page == null) {
            return PageResponse.<R>builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .isFirst(true)
                .isLast(true)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        }

        List<R> content = page.getContent().stream()
            .map(mapper)
            .toList();

        return PageResponse.<R>builder()
            .content(content)
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

    public <T, R> PageResponse<R> toPageResponse(
            List<T> content,
            Page<T> page,
            Function<T, R> mapper) {
        if (page == null || content == null) {
            return PageResponse.<R>builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .isFirst(true)
                .isLast(true)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        }

        List<R> mappedContent = content.stream()
            .map(mapper)
            .toList();

        return PageResponse.<R>builder()
            .content(mappedContent)
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

## 18. Utilisation dans les Services

### UserService.java (extrait)

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.UserMapper;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserCategoryService categoryService;
    private final UserMapper userMapper;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponse);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        UserCategory category = categoryService.getCategoryById(request.getCategoryId());
        User user = userMapper.toEntity(request, category);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UserCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryById(request.getCategoryId());
        }

        userMapper.updateEntity(user, request, category);
        User updated = userRepository.save(user);
        return userMapper.toResponse(updated);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
    }
}
```

---

## 19. Utilisation dans les Controllers

### UserController.java

```java
package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
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
    private final PageMapper pageMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(pageMapper.toPageResponse(users, user -> user));
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

## Résumé des Mappers

| Mapper | Entité → Response | Request → Entité | Update |
|--------|-------------------|------------------|--------|
| UserMapper | ✅ toResponse() | ✅ toEntity() | ✅ updateEntity() |
| UserCategoryMapper | ✅ toResponse() | ✅ toEntity() | ❌ |
| PermissionMapper | ✅ toResponse() | ❌ | ❌ |
| ProjectMapper | ✅ toResponse() | ✅ toEntity() | ✅ updateEntity() |
| SourceFileMapper | ✅ toResponse() | ❌ | ❌ |
| DatasetMapper | ✅ toResponse() | ❌ | ❌ |
| DatasetColumnMapper | ✅ toResponse() | ❌ | ✅ updateEntity() |
| ExplorationReportMapper | ✅ toResponse() | ❌ | ❌ |
| CleaningRuleMapper | ✅ toResponse() | ✅ toEntity() | ✅ updateEntity() |
| CleaningHistoryMapper | ✅ toResponse() | ❌ | ❌ |
| PredefinedAnalysisMapper | ✅ toResponse() | ❌ | ❌ |
| AnalysisExecutionMapper | ✅ toResponse() | ✅ toEntity() | ❌ |
| AnalysisResultMapper | ✅ toResponse() | ❌ | ❌ |
| ChartMapper | ✅ toResponse() | ❌ | ❌ |
| ExportMapper | ✅ toResponse() | ❌ | ❌ |
| AuditLogMapper | ✅ toResponse() | ❌ | ❌ |
| PageMapper | ✅ toPageResponse() | ❌ | ❌ |