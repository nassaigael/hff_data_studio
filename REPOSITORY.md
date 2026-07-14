Parfait ! Passons maintenant au package `repository` avec tous les repositories JPA pour les entités.

---

## Structure du package

```
com.henri_fraise.hff_data_studio.repository
├── UserRepository.java
├── UserCategoryRepository.java
├── PermissionRepository.java
├── ProjectRepository.java
├── SourceFileRepository.java
├── DatasetRepository.java
├── DatasetColumnRepository.java
├── ExplorationReportRepository.java
├── CleaningRuleRepository.java
├── CleaningHistoryRepository.java
├── PredefinedAnalysisRepository.java
├── AnalysisExecutionRepository.java
├── AnalysisResultRepository.java
├── ChartRepository.java
├── ExportRepository.java
├── AuditLogRepository.java
└── custom
    ├── CustomUserRepository.java
    ├── CustomUserRepositoryImpl.java
    ├── CustomProjectRepository.java
    ├── CustomProjectRepositoryImpl.java
    ├── CustomDatasetRepository.java
    └── CustomDatasetRepositoryImpl.java
```

---

## 1. Repositories de base

### UserRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ==================== Find Methods ====================

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    Page<User> findAllByIsActiveTrue(Pageable pageable);

    List<User> findAllByIsActiveTrue();

    Page<User> findByIsActiveTrueAndLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(
            String lastName, String firstName, Pageable pageable);

    Page<User> findByCategoryCategoryId(UUID categoryId, Pageable pageable);

    List<User> findByLastLoginBefore(LocalDateTime date);

    // ==================== Exists Methods ====================

    boolean existsByEmail(String email);

    boolean existsByEmailAndIsActiveTrue(String email);

    boolean existsByEmailAndIdNot(String email, UUID userId);

    // ==================== Count Methods ====================

    long countByIsActiveTrue();

    long countByCategoryCategoryId(UUID categoryId);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("lastLogin") LocalDateTime lastLogin);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordHash = :passwordHash WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId, @Param("passwordHash") String passwordHash);

    // ==================== Custom Queries ====================

    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.category.label = :categoryLabel")
    List<User> findActiveUsersByCategoryLabel(@Param("categoryLabel") String categoryLabel);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countUsersCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
```

---

### UserCategoryRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCategoryRepository extends JpaRepository<UserCategory, UUID> {

    // ==================== Find Methods ====================

    Optional<UserCategory> findByLabel(String label);

    List<UserCategory> findAllByOrderByAccessLevelDesc();

    List<UserCategory> findByAccessLevelGreaterThanEqual(Integer accessLevel);

    // ==================== Exists Methods ====================

    boolean existsByLabel(String label);

    boolean existsByLabelAndIdNot(String label, UUID categoryId);

    // ==================== Query Methods ====================

    @Query("SELECT c FROM UserCategory c JOIN c.users u WHERE u.isActive = true GROUP BY c ORDER BY COUNT(u) DESC")
    List<UserCategory> findCategoriesWithMostActiveUsers();

    @Query("SELECT c FROM UserCategory c WHERE c.accessLevel <= :maxAccessLevel")
    List<UserCategory> findByMaxAccessLevel(@Param("maxAccessLevel") Integer maxAccessLevel);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE UserCategory c SET c.accessLevel = :accessLevel WHERE c.id = :categoryId")
    void updateAccessLevel(@Param("categoryId") UUID categoryId, @Param("accessLevel") Integer accessLevel);
}
```

---

### PermissionRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    // ==================== Find Methods ====================

    Optional<Permission> findByCode(String code);

    List<Permission> findByModule(String module);

    List<Permission> findByModuleOrderByCode(String module);

    // ==================== Exists Methods ====================

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID permissionId);

    // ==================== Query Methods ====================

    @Query("SELECT p FROM Permission p WHERE p.code IN :codes")
    List<Permission> findByCodes(@Param("codes") List<String> codes);

    @Query("SELECT p FROM Permission p ORDER BY p.module, p.code")
    List<Permission> findAllOrdered();

    @Query("SELECT DISTINCT p.module FROM Permission p")
    List<String> findAllModules();

    // ==================== Category Permissions ====================

    @Query("SELECT p FROM Permission p JOIN p.categories c WHERE c.id = :categoryId")
    List<Permission> findPermissionsByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT p FROM Permission p JOIN p.categories c WHERE c.label = :categoryLabel")
    List<Permission> findPermissionsByCategoryLabel(@Param("categoryLabel") String categoryLabel);

    @Query("SELECT COUNT(p) FROM Permission p JOIN p.categories c WHERE c.id = :categoryId")
    long countPermissionsByCategoryId(@Param("categoryId") UUID categoryId);
}
```

---

### ProjectRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // ==================== Find Methods ====================

    Page<Project> findByCreatorUserId(UUID userId, Pageable pageable);

    Page<Project> findByCreatorUserIdAndStatus(UUID userId, ProjectStatus status, Pageable pageable);

    List<Project> findByCreatorUserIdAndStatus(UUID userId, ProjectStatus status);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    List<Project> findByStatus(ProjectStatus status);

    Optional<Project> findByProjectNameAndCreatorUserId(String projectName, UUID userId);

    Page<Project> findByProjectNameContainingIgnoreCase(String projectName, Pageable pageable);

    // ==================== Exists Methods ====================

    boolean existsByProjectName(String projectName);

    boolean existsByProjectNameAndCreatorUserId(String projectName, UUID userId);

    boolean existsByProjectNameAndIdNot(String projectName, UUID projectId);

    // ==================== Count Methods ====================

    long countByCreatorUserId(UUID userId);

    long countByStatus(ProjectStatus status);

    long countByCreatorUserIdAndStatus(UUID userId, ProjectStatus status);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE Project p SET p.status = :status WHERE p.id = :projectId")
    void updateProjectStatus(@Param("projectId") UUID projectId, @Param("status") ProjectStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Project p SET p.status = :status WHERE p.creatorUserId = :userId")
    void updateAllProjectsStatusForUser(@Param("userId") UUID userId, @Param("status") ProjectStatus status);

    // ==================== Custom Queries ====================

    @Query("SELECT p FROM Project p WHERE p.creatorUserId = :userId AND " +
           "(:searchTerm IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Project> searchUserProjects(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Project> findProjectsCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.createdAt < :date")
    List<Project> findProjectsByStatusAndOlderThan(
            @Param("status") ProjectStatus status,
            @Param("date") LocalDateTime date
    );
}
```

---

### SourceFileRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SourceFileRepository extends JpaRepository<SourceFile, UUID> {

    // ==================== Find Methods ====================

    Page<SourceFile> findByProjectId(UUID projectId, Pageable pageable);

    Page<SourceFile> findByUserId(UUID userId, Pageable pageable);

    Page<SourceFile> findByProjectIdAndUserId(UUID projectId, UUID userId, Pageable pageable);

    List<SourceFile> findByProjectId(UUID projectId);

    List<SourceFile> findByUserId(UUID userId);

    Page<SourceFile> findByProcessingStatus(FileProcessingStatus status, Pageable pageable);

    List<SourceFile> findByProcessingStatus(FileProcessingStatus status);

    Page<SourceFile> findByFileNameContainingIgnoreCase(String fileName, Pageable pageable);

    // ==================== Count Methods ====================

    long countByProjectId(UUID projectId);

    long countByUserId(UUID userId);

    long countByProcessingStatus(FileProcessingStatus status);

    long countByProjectIdAndProcessingStatus(UUID projectId, FileProcessingStatus status);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE SourceFile f SET f.processingStatus = :status WHERE f.id = :fileId")
    void updateProcessingStatus(@Param("fileId") UUID fileId, @Param("status") FileProcessingStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE SourceFile f SET f.processingStatus = :status WHERE f.id = :fileId AND f.processingStatus = :currentStatus")
    int updateProcessingStatusIfCurrent(
            @Param("fileId") UUID fileId,
            @Param("status") FileProcessingStatus status,
            @Param("currentStatus") FileProcessingStatus currentStatus
    );

    // ==================== Custom Queries ====================

    @Query("SELECT f FROM SourceFile f WHERE f.projectId = :projectId AND " +
           "(:searchTerm IS NULL OR LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<SourceFile> searchProjectFiles(@Param("projectId") UUID projectId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT f FROM SourceFile f WHERE f.processingStatus IN :statuses")
    List<SourceFile> findByProcessingStatusIn(@Param("statuses") List<FileProcessingStatus> statuses);

    @Query("SELECT f FROM SourceFile f WHERE f.uploadedAt < :date AND f.processingStatus = :status")
    List<SourceFile> findOldFilesByStatus(
            @Param("date") LocalDateTime date,
            @Param("status") FileProcessingStatus status
    );

    @Query("SELECT SUM(f.sizeBytes) FROM SourceFile f WHERE f.projectId = :projectId")
    Long sumFileSizeByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT COUNT(f) FROM SourceFile f WHERE f.uploadedAt BETWEEN :startDate AND :endDate")
    long countFilesUploadedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
```

---

### DatasetRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, UUID> {

    // ==================== Find Methods ====================

    Page<Dataset> findBySourceFileProjectId(UUID projectId, Pageable pageable);

    Page<Dataset> findBySourceFileProjectIdAndSourceFileUserId(UUID projectId, UUID userId, Pageable pageable);

    Page<Dataset> findBySourceFileId(UUID fileId, Pageable pageable);

    List<Dataset> findBySourceFileId(UUID fileId);

    Page<Dataset> findByIsCleanedTrue(Pageable pageable);

    Page<Dataset> findByIsCleanedFalse(Pageable pageable);

    Optional<Dataset> findByDatasetNameAndSourceFileId(String datasetName, UUID fileId);

    Page<Dataset> findByDatasetNameContainingIgnoreCase(String datasetName, Pageable pageable);

    // ==================== Exists Methods ====================

    boolean existsByDatasetNameAndSourceFileId(String datasetName, UUID fileId);

    // ==================== Count Methods ====================

    long countBySourceFileProjectId(UUID projectId);

    long countBySourceFileId(UUID fileId);

    long countByIsCleaned(boolean isCleaned);

    long countBySourceFileProjectIdAndIsCleaned(UUID projectId, boolean isCleaned);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE Dataset d SET d.isCleaned = :isCleaned WHERE d.id = :datasetId")
    void updateIsCleaned(@Param("datasetId") UUID datasetId, @Param("isCleaned") boolean isCleaned);

    @Modifying
    @Transactional
    @Query("UPDATE Dataset d SET d.rowCount = :rowCount, d.columnCount = :columnCount WHERE d.id = :datasetId")
    void updateStats(@Param("datasetId") UUID datasetId, @Param("rowCount") Integer rowCount, @Param("columnCount") Integer columnCount);

    // ==================== Custom Queries ====================

    @Query("SELECT d FROM Dataset d WHERE d.sourceFileProjectId = :projectId AND " +
           "(:searchTerm IS NULL OR LOWER(d.datasetName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Dataset> searchProjectDatasets(@Param("projectId") UUID projectId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT d FROM Dataset d WHERE d.isCleaned = false AND d.createdAt < :date")
    List<Dataset> findUncleanedDatasetsOlderThan(@Param("date") LocalDateTime date);

    @Query("SELECT d FROM Dataset d WHERE d.sourceFileProjectId = :projectId ORDER BY d.createdAt DESC")
    List<Dataset> findRecentDatasetsByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    @Query("SELECT COUNT(d) FROM Dataset d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    long countDatasetsCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT AVG(d.rowCount) FROM Dataset d WHERE d.sourceFileProjectId = :projectId")
    Double averageRowCountByProjectId(@Param("projectId") UUID projectId);
}
```

---

### DatasetColumnRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.ColumnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DatasetColumnRepository extends JpaRepository<DatasetColumn, UUID> {

    // ==================== Find Methods ====================

    List<DatasetColumn> findByDatasetIdOrderByPositionAsc(UUID datasetId);

    List<DatasetColumn> findByDatasetId(UUID datasetId);

    Optional<DatasetColumn> findByDatasetIdAndOriginalName(UUID datasetId, String originalName);

    List<DatasetColumn> findByDatasetIdAndDetectedType(UUID datasetId, ColumnType detectedType);

    List<DatasetColumn> findByDatasetIdAndNormalizedNameIsNotNull(UUID datasetId);

    // ==================== Exists Methods ====================

    boolean existsByDatasetIdAndOriginalName(UUID datasetId, String originalName);

    // ==================== Count Methods ====================

    long countByDatasetId(UUID datasetId);

    long countByDatasetIdAndDetectedType(UUID datasetId, ColumnType detectedType);

    long countByDatasetIdAndNullCountGreaterThan(UUID datasetId, int nullCount);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE DatasetColumn c SET c.normalizedName = :normalizedName WHERE c.id = :columnId")
    void updateNormalizedName(@Param("columnId") UUID columnId, @Param("normalizedName") String normalizedName);

    @Modifying
    @Transactional
    @Query("UPDATE DatasetColumn c SET c.targetType = :targetType WHERE c.id = :columnId")
    void updateTargetType(@Param("columnId") UUID columnId, @Param("targetType") ColumnType targetType);

    @Modifying
    @Transactional
    @Query("UPDATE DatasetColumn c SET c.nullCount = :nullCount, c.uniqueCount = :uniqueCount WHERE c.id = :columnId")
    void updateStats(@Param("columnId") UUID columnId, @Param("nullCount") Integer nullCount, @Param("uniqueCount") Integer uniqueCount);

    // ==================== Custom Queries ====================

    @Query("SELECT c FROM DatasetColumn c WHERE c.datasetId = :datasetId AND " +
           "(c.normalizedName IS NULL OR c.targetType IS NULL OR c.targetType != c.detectedType)")
    List<DatasetColumn> findColumnsNeedingNormalization(@Param("datasetId") UUID datasetId);

    @Query("SELECT c FROM DatasetColumn c WHERE c.datasetId = :datasetId AND c.nullCount > 0")
    List<DatasetColumn> findColumnsWithNullValues(@Param("datasetId") UUID datasetId);

    @Query("SELECT c FROM DatasetColumn c WHERE c.datasetId = :datasetId AND c.uniqueCount = c.datasetRowCount")
    List<DatasetColumn> findUniqueColumns(@Param("datasetId") UUID datasetId);

    @Query("SELECT MAX(c.position) FROM DatasetColumn c WHERE c.datasetId = :datasetId")
    Integer findMaxPositionByDatasetId(@Param("datasetId") UUID datasetId);

    @Query("SELECT c FROM DatasetColumn c WHERE c.datasetId = :datasetId AND c.detectedType = :detectedType AND c.targetType IS NULL")
    List<DatasetColumn> findColumnsWithDetectedTypeNotConverted(
            @Param("datasetId") UUID datasetId,
            @Param("detectedType") ColumnType detectedType
    );
}
```

---

### ExplorationReportRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.ExplorationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExplorationReportRepository extends JpaRepository<ExplorationReport, UUID> {

    // ==================== Find Methods ====================

    Optional<ExplorationReport> findByDatasetId(UUID datasetId);

    List<ExplorationReport> findByQualityScoreGreaterThanEqual(BigDecimal score);

    List<ExplorationReport> findByQualityScoreLessThan(BigDecimal score);

    // ==================== Count Methods ====================

    long countByQualityScoreGreaterThanEqual(BigDecimal score);

    long countByQualityScoreLessThan(BigDecimal score);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE ExplorationReport r SET r.reportPdfPath = :pdfPath WHERE r.id = :reportId")
    void updateReportPdfPath(@Param("reportId") UUID reportId, @Param("pdfPath") String pdfPath);

    @Modifying
    @Transactional
    @Query("UPDATE ExplorationReport r SET r.qualityScore = :score WHERE r.id = :reportId")
    void updateQualityScore(@Param("reportId") UUID reportId, @Param("score") BigDecimal score);

    // ==================== Custom Queries ====================

    @Query("SELECT AVG(r.qualityScore) FROM ExplorationReport r")
    BigDecimal averageQualityScore();

    @Query("SELECT r FROM ExplorationReport r WHERE r.generatedAt BETWEEN :startDate AND :endDate")
    List<ExplorationReport> findByGeneratedDateRange(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    @Query("SELECT r FROM ExplorationReport r ORDER BY r.qualityScore DESC")
    List<ExplorationReport> findTopByQualityScore(Pageable pageable);

    @Query("SELECT COUNT(r) FROM ExplorationReport r WHERE r.qualityScore < :threshold")
    long countByQualityScoreBelowThreshold(@Param("threshold") BigDecimal threshold);
}
```

---

### CleaningRuleRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface CleaningRuleRepository extends JpaRepository<CleaningRule, UUID> {

    // ==================== Find Methods ====================

    List<CleaningRule> findByColumnDatasetIdOrderByExecutionOrderAsc(UUID datasetId);

    List<CleaningRule> findByColumnIdOrderByExecutionOrderAsc(UUID columnId);

    List<CleaningRule> findByColumnIdAndIsActiveTrueOrderByExecutionOrderAsc(UUID columnId);

    List<CleaningRule> findByColumnDatasetIdAndIsActiveTrue(UUID datasetId);

    List<CleaningRule> findByRuleType(RuleType ruleType);

    List<CleaningRule> findByIsActiveTrue();

    // ==================== Count Methods ====================

    long countByColumnId(UUID columnId);

    long countByColumnDatasetId(UUID datasetId);

    long countByColumnIdAndIsActiveTrue(UUID columnId);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE CleaningRule r SET r.isActive = :isActive WHERE r.id = :ruleId")
    void setActiveStatus(@Param("ruleId") UUID ruleId, @Param("isActive") boolean isActive);

    @Modifying
    @Transactional
    @Query("UPDATE CleaningRule r SET r.executionOrder = :order WHERE r.id = :ruleId")
    void updateExecutionOrder(@Param("ruleId") UUID ruleId, @Param("order") Integer order);

    @Modifying
    @Transactional
    @Query("UPDATE CleaningRule r SET r.isActive = false WHERE r.column.id = :columnId")
    void deactivateAllRulesForColumn(@Param("columnId") UUID columnId);

    // ==================== Custom Queries ====================

    @Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.isActive = true ORDER BY r.executionOrder ASC")
    List<CleaningRule> findActiveRulesByDatasetIdOrdered(@Param("datasetId") UUID datasetId);

    @Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.executionOrder = :order")
    List<CleaningRule> findByDatasetIdAndExecutionOrder(
            @Param("datasetId") UUID datasetId,
            @Param("order") Integer order
    );

    @Query("SELECT MAX(r.executionOrder) FROM CleaningRule r WHERE r.column.dataset.id = :datasetId")
    Integer findMaxExecutionOrderByDatasetId(@Param("datasetId") UUID datasetId);

    @Query("SELECT r FROM CleaningRule r WHERE r.column.id = :columnId AND r.ruleType = :ruleType")
    List<CleaningRule> findByColumnIdAndRuleType(
            @Param("columnId") UUID columnId,
            @Param("ruleType") RuleType ruleType
    );

    @Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.ruleType = :ruleType")
    List<CleaningRule> findByDatasetIdAndRuleType(
            @Param("datasetId") UUID datasetId,
            @Param("ruleType") RuleType ruleType
    );
}
```

---

### CleaningHistoryRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.CleaningHistory;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CleaningHistoryRepository extends JpaRepository<CleaningHistory, UUID> {

    // ==================== Find Methods ====================

    Page<CleaningHistory> findByDatasetIdOrderByExecutedAtDesc(UUID datasetId, Pageable pageable);

    List<CleaningHistory> findByDatasetIdOrderByExecutedAtDesc(UUID datasetId);

    Page<CleaningHistory> findByUserIdOrderByExecutedAtDesc(UUID userId, Pageable pageable);

    List<CleaningHistory> findByStatus(CleaningHistoryStatus status);

    Page<CleaningHistory> findByStatus(CleaningHistoryStatus status, Pageable pageable);

    // ==================== Count Methods ====================

    long countByDatasetId(UUID datasetId);

    long countByStatus(CleaningHistoryStatus status);

    long countByDatasetIdAndStatus(UUID datasetId, CleaningHistoryStatus status);

    long countByUserIdAndStatus(UUID userId, CleaningHistoryStatus status);

    // ==================== Custom Queries ====================

    @Query("SELECT h FROM CleaningHistory h WHERE h.datasetId = :datasetId AND h.executedAt >= :since")
    List<CleaningHistory> findByDatasetIdSince(
            @Param("datasetId") UUID datasetId,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT h FROM CleaningHistory h WHERE h.executedAt BETWEEN :startDate AND :endDate")
    List<CleaningHistory> findByExecutedDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT h FROM CleaningHistory h WHERE h.status = :status AND h.executedAt < :date")
    List<CleaningHistory> findOldRecordsByStatus(
            @Param("status") CleaningHistoryStatus status,
            @Param("date") LocalDateTime date
    );

    @Query("SELECT COUNT(h) FROM CleaningHistory h WHERE h.executedAt BETWEEN :startDate AND :endDate")
    long countCleaningOperationsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT AVG(h.durationMs) FROM CleaningHistory h WHERE h.status = 'SUCCESS'")
    Double averageSuccessfulCleaningDuration();
}
```

---

### PredefinedAnalysisRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PredefinedAnalysisRepository extends JpaRepository<PredefinedAnalysis, UUID> {

    // ==================== Find Methods ====================

    Page<PredefinedAnalysis> findByCategory(String category, Pageable pageable);

    List<PredefinedAnalysis> findByCategory(String category);

    Page<PredefinedAnalysis> findByAnalysisNameContainingIgnoreCase(String name, Pageable pageable);

    List<PredefinedAnalysis> findAllByOrderByCategoryAscAnalysisNameAsc();

    // ==================== Exists Methods ====================

    boolean existsByAnalysisName(String analysisName);

    boolean existsByReferenceScript(String referenceScript);

    // ==================== Count Methods ====================

    long countByCategory(String category);

    // ==================== Custom Queries ====================

    @Query("SELECT DISTINCT a.category FROM PredefinedAnalysis a")
    List<String> findAllCategories();

    @Query("SELECT a FROM PredefinedAnalysis a WHERE a.category = :category AND " +
           "(:searchTerm IS NULL OR LOWER(a.analysisName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<PredefinedAnalysis> searchByCategory(
            @Param("category") String category,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT a FROM PredefinedAnalysis a WHERE a.requiredParametersJson IS NULL OR a.requiredParametersJson = ''")
    List<PredefinedAnalysis> findAnalysesWithoutParameters();

    @Query("SELECT a FROM PredefinedAnalysis a WHERE a.requiredParametersJson IS NOT NULL AND a.requiredParametersJson != ''")
    List<PredefinedAnalysis> findAnalysesWithParameters();
}
```

---

### AnalysisExecutionRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisExecutionRepository extends JpaRepository<AnalysisExecution, UUID> {

    // ==================== Find Methods ====================

    Page<AnalysisExecution> findByDatasetId(UUID datasetId, Pageable pageable);

    Page<AnalysisExecution> findByUserId(UUID userId, Pageable pageable);

    Page<AnalysisExecution> findByStatus(AnalysisExecutionStatus status, Pageable pageable);

    List<AnalysisExecution> findByStatus(AnalysisExecutionStatus status);

    Page<AnalysisExecution> findByDatasetIdAndUserId(UUID datasetId, UUID userId, Pageable pageable);

    Page<AnalysisExecution> findByPredefinedAnalysisId(UUID analysisId, Pageable pageable);

    // ==================== Count Methods ====================

    long countByDatasetId(UUID datasetId);

    long countByUserId(UUID userId);

    long countByStatus(AnalysisExecutionStatus status);

    long countByPredefinedAnalysisId(UUID analysisId);

    long countByDatasetIdAndStatus(UUID datasetId, AnalysisExecutionStatus status);

    // ==================== Update Methods ====================

    @Modifying
    @Transactional
    @Query("UPDATE AnalysisExecution e SET e.status = :status WHERE e.id = :executionId")
    void updateStatus(@Param("executionId") UUID executionId, @Param("status") AnalysisExecutionStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE AnalysisExecution e SET e.status = :status, e.durationMs = :durationMs WHERE e.id = :executionId")
    void completeExecution(@Param("executionId") UUID executionId, @Param("status") AnalysisExecutionStatus status, @Param("durationMs") Integer durationMs);

    // ==================== Custom Queries ====================

    @Query("SELECT e FROM AnalysisExecution e WHERE e.status = 'IN_PROGRESS' AND e.executedAt < :timeout")
    List<AnalysisExecution> findStalledExecutions(@Param("timeout") LocalDateTime timeout);

    @Query("SELECT e FROM AnalysisExecution e WHERE e.datasetId = :datasetId AND e.status IN :statuses")
    List<AnalysisExecution> findByDatasetIdAndStatuses(
            @Param("datasetId") UUID datasetId,
            @Param("statuses") List<AnalysisExecutionStatus> statuses
    );

    @Query("SELECT e FROM AnalysisExecution e WHERE e.userId = :userId AND " +
           "(:searchTerm IS NULL OR LOWER(e.dataset.datasetName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.predefinedAnalysis.analysisName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<AnalysisExecution> searchUserExecutions(
            @Param("userId") UUID userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM AnalysisExecution e WHERE e.executedAt BETWEEN :startDate AND :endDate")
    long countExecutionsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT AVG(e.durationMs) FROM AnalysisExecution e WHERE e.status = 'COMPLETED'")
    Double averageSuccessfulExecutionDuration();
}
```

---

### AnalysisResultRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.enums.ResultType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

    // ==================== Find Methods ====================

    List<AnalysisResult> findByExecutionIdOrderByDisplayOrderAsc(UUID executionId);

    List<AnalysisResult> findByExecutionIdAndResultType(UUID executionId, ResultType resultType);

    List<AnalysisResult> findByExecutionIdAndResultTypeIn(UUID executionId, List<ResultType> resultTypes);

    // ==================== Delete Methods ====================

    @Modifying
    @Transactional
    @Query("DELETE FROM AnalysisResult r WHERE r.executionId = :executionId")
    void deleteByExecutionId(@Param("executionId") UUID executionId);

    // ==================== Count Methods ====================

    long countByExecutionId(UUID executionId);

    long countByExecutionIdAndResultType(UUID executionId, ResultType resultType);

    // ==================== Custom Queries ====================

    @Query("SELECT r FROM AnalysisResult r WHERE r.executionId = :executionId AND r.resultType = 'CHART'")
    List<AnalysisResult> findChartsByExecutionId(@Param("executionId") UUID executionId);

    @Query("SELECT r FROM AnalysisResult r WHERE r.executionId = :executionId AND r.resultType = 'TABLE'")
    List<AnalysisResult> findTablesByExecutionId(@Param("executionId") UUID executionId);

    @Query("SELECT r FROM AnalysisResult r WHERE r.executionId = :executionId AND r.resultType = 'KPI'")
    List<AnalysisResult> findKPIsByExecutionId(@Param("executionId") UUID executionId);

    @Query("SELECT r FROM AnalysisResult r WHERE r.fileFormat = :fileFormat AND r.executionId = :executionId")
    List<AnalysisResult> findByExecutionIdAndFileFormat(
            @Param("executionId") UUID executionId,
            @Param("fileFormat") String fileFormat
    );
}
```

---

### ChartRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.ChartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChartRepository extends JpaRepository<Chart, UUID> {

    // ==================== Find Methods ====================

    Optional<Chart> findByResultId(UUID resultId);

    List<Chart> findByChartType(ChartType chartType);

    // ==================== Count Methods ====================

    long countByChartType(ChartType chartType);

    // ==================== Custom Queries ====================

    @Query("SELECT c FROM Chart c WHERE c.result.execution.id = :executionId")
    List<Chart> findByExecutionId(@Param("executionId") UUID executionId);

    @Query("SELECT c FROM Chart c WHERE c.result.execution.id = :executionId AND c.chartType = :chartType")
    List<Chart> findByExecutionIdAndChartType(
            @Param("executionId") UUID executionId,
            @Param("chartType") ChartType chartType
    );

    @Query("SELECT COUNT(c) FROM Chart c WHERE c.chartType = :chartType AND c.result.execution.id = :executionId")
    long countByExecutionIdAndChartType(
            @Param("executionId") UUID executionId,
            @Param("chartType") ChartType chartType
    );

    @Query("SELECT DISTINCT c.chartType FROM Chart c")
    List<ChartType> findAllDistinctChartTypes();
}
```

---

### ExportRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.enums.FileFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExportRepository extends JpaRepository<Export, UUID> {

    // ==================== Find Methods ====================

    Page<Export> findByUserIdOrderByExportedAtDesc(UUID userId, Pageable pageable);

    Page<Export> findByExecutionIdOrderByExportedAtDesc(UUID executionId, Pageable pageable);

    Page<Export> findByFileFormat(FileFormat fileFormat, Pageable pageable);

    List<Export> findByUserIdOrderByExportedAtDesc(UUID userId);

    // ==================== Count Methods ====================

    long countByUserId(UUID userId);

    long countByExecutionId(UUID executionId);

    long countByFileFormat(FileFormat fileFormat);

    long countByUserIdAndFileFormat(UUID userId, FileFormat fileFormat);

    // ==================== Custom Queries ====================

    @Query("SELECT e FROM Export e WHERE e.exportedAt BETWEEN :startDate AND :endDate")
    List<Export> findByExportedDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM Export e WHERE e.userId = :userId AND " +
           "(:searchTerm IS NULL OR LOWER(e.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.fileFormat) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Export> searchUserExports(
            @Param("userId") UUID userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM Export e WHERE e.exportedAt BETWEEN :startDate AND :endDate")
    long countExportsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e.fileFormat, COUNT(e) FROM Export e GROUP BY e.fileFormat")
    List<Object[]> countExportsByFormat();

    @Query("SELECT e FROM Export e WHERE e.exportedAt < :date")
    List<Export> findOldExports(@Param("date") LocalDateTime date);
}
```

---

### AuditLogRepository.java

```java
package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // ==================== Find Methods ====================

    Page<AuditLog> findByUserIdOrderByActionDateDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findByConcernedEntityOrderByActionDateDesc(String entity, Pageable pageable);

    Page<AuditLog> findByActionContainingIgnoreCase(String action, Pageable pageable);

    List<AuditLog> findByUserIdOrderByActionDateDesc(UUID userId);

    // ==================== Count Methods ====================

    long countByUserId(UUID userId);

    long countByConcernedEntity(String entity);

    long countByActionContainingIgnoreCase(String action);

    // ==================== Custom Queries ====================

    @Query("SELECT a FROM AuditLog a WHERE a.actionDate BETWEEN :startDate AND :endDate ORDER BY a.actionDate DESC")
    Page<AuditLog> findByActionDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.actionDate BETWEEN :startDate AND :endDate")
    List<AuditLog> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT a FROM AuditLog a WHERE a.concernedEntity = :entity AND a.entityId = :entityId")
    List<AuditLog> findByConcernedEntityAndEntityId(
            @Param("entity") String entity,
            @Param("entityId") UUID entityId
    );

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countActions();

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.actionDate BETWEEN :startDate AND :endDate")
    long countLogsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT a FROM AuditLog a WHERE a.actionDate < :date")
    List<AuditLog> findOldAuditLogs(@Param("date") LocalDateTime date);
}
```

---

## 2. Repositories personnalisés (Custom)

### CustomUserRepository.java

```java
package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CustomUserRepository {

    UserStatisticsResponse getUserStatistics();

    long countActiveUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    long countNewUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    void updateUserActivity(UUID userId, String activity);
}
```

---

### CustomUserRepositoryImpl.java

```java
package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserStatisticsResponse getUserStatistics() {
        Query query = entityManager.createNativeQuery(
            "SELECT " +
            "COUNT(*) as total_users, " +
            "SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as active_users, " +
            "SUM(CASE WHEN is_active = false THEN 1 ELSE 0 END) as inactive_users, " +
            "COUNT(DISTINCT category_id) as total_categories, " +
            "(SELECT COUNT(*) FROM user WHERE created_at >= NOW() - INTERVAL '30 days') as new_users_last_30_days, " +
            "(SELECT COUNT(*) FROM user WHERE created_at >= NOW() - INTERVAL '7 days') as new_users_last_7_days, " +
            "(SELECT COUNT(*) FROM user WHERE last_login >= NOW() - INTERVAL '30 days') as active_users_last_30_days, " +
            "(SELECT COUNT(*) FROM user WHERE last_login >= NOW() - INTERVAL '7 days') as active_users_last_7_days " +
            "FROM user"
        );

        Object[] result = (Object[]) query.getSingleResult();

        return UserStatisticsResponse.builder()
            .totalUsers(((Number) result[0]).longValue())
            .activeUsers(((Number) result[1]).longValue())
            .inactiveUsers(((Number) result[2]).longValue())
            .totalCategories(((Number) result[3]).longValue())
            .newUsersLast30Days(((Number) result[4]).longValue())
            .newUsersLast7Days(((Number) result[5]).longValue())
            .activeUsersLast30Days(((Number) result[6]).longValue())
            .activeUsersLast7Days(((Number) result[7]).longValue())
            .build();
    }

    @Override
    public long countActiveUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.createdAt BETWEEN :startDate AND :endDate"
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public long countNewUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate"
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    @Transactional
    public void updateUserActivity(UUID userId, String activity) {
        Query query = entityManager.createNativeQuery(
            "INSERT INTO user_activity (user_id, activity, activity_date) VALUES (:userId, :activity, NOW())"
        );
        query.setParameter("userId", userId);
        query.setParameter("activity", activity);
        query.executeUpdate();
    }
}
```

---

### CustomProjectRepository.java

```java
package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.ProjectStatisticsResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CustomProjectRepository {

    ProjectStatisticsResponse getProjectStatistics();

    long countProjectsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    long countActiveProjectsByUser(UUID userId);

    void archiveInactiveProjects(LocalDateTime olderThan);
}
```

---

### CustomProjectRepositoryImpl.java

```java
package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.ProjectStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class CustomProjectRepositoryImpl implements CustomProjectRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ProjectStatisticsResponse getProjectStatistics() {
        Query query = entityManager.createNativeQuery(
            "SELECT " +
            "COUNT(*) as total_projects, " +
            "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress, " +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN status = 'ARCHIVED' THEN 1 ELSE 0 END) as archived, " +
            "(SELECT COUNT(DISTINCT creator_user_id) FROM project) as total_creators, " +
            "(SELECT COUNT(*) FROM project WHERE created_at >= NOW() - INTERVAL '30 days') as new_projects_last_30_days " +
            "FROM project"
        );

        Object[] result = (Object[]) query.getSingleResult();

        return ProjectStatisticsResponse.builder()
            .totalProjects(((Number) result[0]).longValue())
            .inProgress(((Number) result[1]).longValue())
            .completed(((Number) result[2]).longValue())
            .archived(((Number) result[3]).longValue())
            .totalCreators(((Number) result[4]).longValue())
            .newProjectsLast30Days(((Number) result[5]).longValue())
            .build();
    }

    @Override
    public long countProjectsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(p) FROM Project p WHERE p.createdAt BETWEEN :startDate AND :endDate"
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public long countActiveProjectsByUser(UUID userId) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(p) FROM Project p WHERE p.creator.id = :userId AND p.status != 'ARCHIVED'"
        );
        query.setParameter("userId", userId);
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    @Transactional
    public void archiveInactiveProjects(LocalDateTime olderThan) {
        Query query = entityManager.createQuery(
            "UPDATE Project p SET p.status = 'ARCHIVED' " +
            "WHERE p.status = 'COMPLETED' AND p.createdAt < :olderThan"
        );
        query.setParameter("olderThan", olderThan);
        query.executeUpdate();
    }
}
```

---

### CustomDatasetRepository.java

```java
package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CustomDatasetRepository {

    DatasetStatisticsResponse getDatasetStatistics();

    long countDatasetsByProjectId(UUID projectId);

    long countCleanedDatasetsByProjectId(UUID projectId);

    double getAverageDatasetQualityScore();

    void updateDatasetQualityScore(UUID datasetId, double score);
}
```

---

### CustomDatasetRepositoryImpl.java

```java
package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class CustomDatasetRepositoryImpl implements CustomDatasetRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DatasetStatisticsResponse getDatasetStatistics() {
        Query query = entityManager.createNativeQuery(
            "SELECT " +
            "COUNT(*) as total_datasets, " +
            "SUM(row_count) as total_rows, " +
            "SUM(column_count) as total_columns, " +
            "SUM(CASE WHEN is_cleaned = true THEN 1 ELSE 0 END) as cleaned_datasets, " +
            "SUM(CASE WHEN is_cleaned = false THEN 1 ELSE 0 END) as uncleaned_datasets, " +
            "AVG(er.quality_score) as avg_quality_score, " +
            "(SELECT COUNT(*) FROM dataset WHERE created_at >= NOW() - INTERVAL '30 days') as new_datasets_last_30_days " +
            "FROM dataset d " +
            "LEFT JOIN exploration_report er ON d.dataset_id = er.dataset_id"
        );

        Object[] result = (Object[]) query.getSingleResult();

        return DatasetStatisticsResponse.builder()
            .totalDatasets(((Number) result[0]).longValue())
            .totalRows(((Number) result[1]).longValue())
            .totalColumns(((Number) result[2]).longValue())
            .cleanedDatasets(((Number) result[3]).longValue())
            .uncleanedDatasets(((Number) result[4]).longValue())
            .averageQualityScore(result[5] != null ? ((BigDecimal) result[5]).doubleValue() : 0.0)
            .newDatasetsLast30Days(((Number) result[6]).longValue())
            .build();
    }

    @Override
    public long countDatasetsByProjectId(UUID projectId) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(d) FROM Dataset d WHERE d.sourceFile.project.id = :projectId"
        );
        query.setParameter("projectId", projectId);
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public long countCleanedDatasetsByProjectId(UUID projectId) {
        Query query = entityManager.createQuery(
            "SELECT COUNT(d) FROM Dataset d WHERE d.sourceFile.project.id = :projectId AND d.isCleaned = true"
        );
        query.setParameter("projectId", projectId);
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public double getAverageDatasetQualityScore() {
        Query query = entityManager.createQuery(
            "SELECT AVG(r.qualityScore) FROM ExplorationReport r"
        );
        BigDecimal result = (BigDecimal) query.getSingleResult();
        return result != null ? result.doubleValue() : 0.0;
    }

    @Override
    @Transactional
    public void updateDatasetQualityScore(UUID datasetId, double score) {
        Query query = entityManager.createNativeQuery(
            "UPDATE exploration_report SET quality_score = :score WHERE dataset_id = :datasetId"
        );
        query.setParameter("score", score);
        query.setParameter("datasetId", datasetId);
        query.executeUpdate();
    }
}
```

---

## 3. DTOs de statistiques

### UserStatisticsResponse.java

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
public class UserStatisticsResponse {
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long totalCategories;
    private Long newUsersLast30Days;
    private Long newUsersLast7Days;
    private Long activeUsersLast30Days;
    private Long activeUsersLast7Days;
}
```

### ProjectStatisticsResponse.java

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
public class ProjectStatisticsResponse {
    private Long totalProjects;
    private Long inProgress;
    private Long completed;
    private Long archived;
    private Long totalCreators;
    private Long newProjectsLast30Days;
}
```

### DatasetStatisticsResponse.java

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
public class DatasetStatisticsResponse {
    private Long totalDatasets;
    private Long totalRows;
    private Long totalColumns;
    private Long cleanedDatasets;
    private Long uncleanedDatasets;
    private Double averageQualityScore;
    private Long newDatasetsLast30Days;
}
```

---

## Résumé des Repositories

| Repository | Entité | Méthodes clés |
|------------|--------|---------------|
| UserRepository | User | findByEmail, existsByEmail, deactivateUser |
| UserCategoryRepository | UserCategory | findByLabel, existsByLabel |
| PermissionRepository | Permission | findByCode, findByModule |
| ProjectRepository | Project | findByCreatorUserId, updateProjectStatus |
| SourceFileRepository | SourceFile | findByProjectId, updateProcessingStatus |
| DatasetRepository | Dataset | findByProjectId, updateIsCleaned |
| DatasetColumnRepository | DatasetColumn | findByDatasetId, updateNormalizedName |
| ExplorationReportRepository | ExplorationReport | findByDatasetId, updateQualityScore |
| CleaningRuleRepository | CleaningRule | findByColumnId, setActiveStatus |
| CleaningHistoryRepository | CleaningHistory | findByDatasetId, countByStatus |
| PredefinedAnalysisRepository | PredefinedAnalysis | findByCategory, findAllCategories |
| AnalysisExecutionRepository | AnalysisExecution | findByDatasetId, updateStatus |
| AnalysisResultRepository | AnalysisResult | findByExecutionId, deleteByExecutionId |
| ChartRepository | Chart | findByResultId, findByExecutionId |
| ExportRepository | Export | findByUserId, countByFormat |
| AuditLogRepository | AuditLog | findByUserId, countActions |
| CustomUserRepository | User | getUserStatistics, updateUserActivity |
| CustomProjectRepository | Project | getProjectStatistics, archiveInactiveProjects |
| CustomDatasetRepository | Dataset | getDatasetStatistics, updateDatasetQualityScore |