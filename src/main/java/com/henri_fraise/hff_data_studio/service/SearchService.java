package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.SearchResponse;
import com.henri_fraise.hff_data_studio.dto.response.SearchResult;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

	@PersistenceContext
	private EntityManager entityManager;

	private final SecurityUtils securityUtils;

	@Transactional(readOnly = true)
	public SearchResponse search(String query, List<String> entityTypes, int limit) {
		long start = System.currentTimeMillis();
		UUID userId = securityUtils.getCurrentUserId();

		List<SearchResult> results = new ArrayList<>();

		if (entityTypes == null || entityTypes.isEmpty() || entityTypes.contains("PROJECT")) {
			results.addAll(searchProjects(query, userId, limit));
		}
		if (entityTypes == null || entityTypes.isEmpty() || entityTypes.contains("DATASET")) {
			results.addAll(searchDatasets(query, userId, limit));
		}
		if (entityTypes == null || entityTypes.isEmpty() || entityTypes.contains("FILE")) {
			results.addAll(searchFiles(query, userId, limit));
		}

		return SearchResponse.builder()
				.query(query)
				.totalHits(results.size())
				.results(results.stream().limit(limit).toList())
				.tookMs(System.currentTimeMillis() - start)
				.build();
	}

	private List<SearchResult> searchProjects(String query, UUID userId, int limit) {
		String sql = """
                SELECT p.project_id, p.project_name, p.description,
                       ts_rank(to_tsvector('simple', p.project_name || ' ' || COALESCE(p.description, '')),
                               plainto_tsquery('simple', :q)) AS rank
                FROM project p
                WHERE p.creator_user_id = :userId
                  AND to_tsvector('simple', p.project_name || ' ' || COALESCE(p.description, ''))
                      @@ plainto_tsquery('simple', :q)
                ORDER BY rank DESC
                LIMIT :limit
                """;
		return executeSql(sql, query, userId, limit, "PROJECT", "projectId", "projectName", "description");
	}

	private List<SearchResult> searchDatasets(String query, UUID userId, int limit) {
		String sql = """
                SELECT d.dataset_id, d.dataset_name, NULL::text AS description,
                       ts_rank(to_tsvector('simple', d.dataset_name),
                               plainto_tsquery('simple', :q)) AS rank
                FROM dataset d
                JOIN source_file sf ON d.source_file_id = sf.file_id
                JOIN project p ON sf.project_id = p.project_id
                WHERE p.creator_user_id = :userId
                  AND to_tsvector('simple', d.dataset_name) @@ plainto_tsquery('simple', :q)
                ORDER BY rank DESC
                LIMIT :limit
                """;
		return executeSql(sql, query, userId, limit, "DATASET", "datasetId", "datasetName", "description");
	}

	private List<SearchResult> searchFiles(String query, UUID userId, int limit) {
		String sql = """
                SELECT sf.file_id, sf.file_name, NULL::text AS description,
                       ts_rank(to_tsvector('simple', sf.file_name),
                               plainto_tsquery('simple', :q)) AS rank
                FROM source_file sf
                WHERE sf.user_id = :userId
                  AND to_tsvector('simple', sf.file_name) @@ plainto_tsquery('simple', :q)
                ORDER BY rank DESC
                LIMIT :limit
                """;
		return executeSql(sql, query, userId, limit, "FILE", "fileId", "fileName", "description");
	}

	private List<SearchResult> executeSql(String sql, String query, UUID userId, int limit,
	                                      String entityType, String idKey,
	                                      String titleKey, String descKey) {
		Query q = entityManager.createNativeQuery(sql);
		q.setParameter("q", query);
		q.setParameter("userId", userId);
		q.setParameter("limit", limit);
		List<?> rows = q.getResultList();
		List<SearchResult> results = new ArrayList<>();
		for (Object row : rows) {
			Object[] r = (Object[]) row;
			results.add(SearchResult.builder()
					.entityType(entityType)
					.entityId(r[0].toString())
					.title(r[1] != null ? r[1].toString() : null)
					.snippet(r[2] != null ? r[2].toString() : null)
					.rank(r[3] != null ? ((Number) r[3]).doubleValue() : 0.0)
					.build());
		}
		return results;
	}
}