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