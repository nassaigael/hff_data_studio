package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.CatalogEntryRequest;
import com.henri_fraise.hff_data_studio.dto.request.GlossaryTermRequest;
import com.henri_fraise.hff_data_studio.dto.response.CatalogEntryResponse;
import com.henri_fraise.hff_data_studio.dto.response.GlossaryTermResponse;
import com.henri_fraise.hff_data_studio.entity.DataCatalogEntry;
import com.henri_fraise.hff_data_studio.entity.GlossaryTerm;
import com.henri_fraise.hff_data_studio.exception.ResourceAlreadyExistsException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.DataCatalogEntryRepository;
import com.henri_fraise.hff_data_studio.repository.GlossaryTermRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCatalogService {

	private final DataCatalogEntryRepository catalogRepository;
	private final GlossaryTermRepository glossaryRepository;
	private final SecurityUtils securityUtils;
	private final ObjectMapper objectMapper;

	@Transactional
	public CatalogEntryResponse createEntry(CatalogEntryRequest request) {
		DataCatalogEntry entry = DataCatalogEntry.builder()
				.entityType(request.getEntityType())
				.entityId(request.getEntityId())
				.displayName(request.getDisplayName())
				.description(request.getDescription())
				.businessDomain(request.getBusinessDomain())
				.ownerName(request.getOwnerName())
				.stewardName(request.getStewardName())
				.classification(request.getClassification())
				.sensitivityLevel(request.getSensitivityLevel())
				.tags(request.getTags() != null ? String.join(",", request.getTags()) : null)
				.metadataJson(serialize(request.getMetadata()))
				.user(securityUtils.getCurrentUser())
				.build();
		return toEntryResponse(catalogRepository.save(entry));
	}

	@Transactional
	public CatalogEntryResponse updateEntry(UUID entryId, CatalogEntryRequest request) {
		DataCatalogEntry entry = catalogRepository.findById(entryId)
				.orElseThrow(() -> new ResourceNotFoundException("Catalog entry not found"));
		entry.setDisplayName(request.getDisplayName());
		entry.setDescription(request.getDescription());
		entry.setBusinessDomain(request.getBusinessDomain());
		entry.setOwnerName(request.getOwnerName());
		entry.setStewardName(request.getStewardName());
		entry.setClassification(request.getClassification());
		entry.setSensitivityLevel(request.getSensitivityLevel());
		if (request.getTags() != null) entry.setTags(String.join(",", request.getTags()));
		if (request.getMetadata() != null) entry.setMetadataJson(serialize(request.getMetadata()));
		return toEntryResponse(catalogRepository.save(entry));
	}

	@Transactional(readOnly = true)
	public List<CatalogEntryResponse> searchEntries(String entityType, UUID entityId) {
		return catalogRepository.findByEntityTypeAndEntityId(entityType, entityId)
				.stream().map(this::toEntryResponse).toList();
	}

	@Transactional
	public void deleteEntry(UUID entryId) {
		catalogRepository.deleteById(entryId);
	}

	@Transactional
	public GlossaryTermResponse createTerm(GlossaryTermRequest request) {
		if (glossaryRepository.existsByTermIgnoreCase(request.getTerm())) {
			throw new ResourceAlreadyExistsException("Term already exists: " + request.getTerm());
		}
		GlossaryTerm term = GlossaryTerm.builder()
				.term(request.getTerm())
				.definition(request.getDefinition())
				.synonyms(request.getSynonyms() != null
						? String.join(",", request.getSynonyms()) : null)
				.relatedTerms(request.getRelatedTerms() != null
						? String.join(",", request.getRelatedTerms()) : null)
				.domain(request.getDomain())
				.createdBy(securityUtils.getCurrentUser())
				.build();
		return toTermResponse(glossaryRepository.save(term));
	}

	@Transactional(readOnly = true)
	public List<GlossaryTermResponse> searchTerms(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return glossaryRepository.findAll().stream().map(this::toTermResponse).toList();
		}
		return glossaryRepository.findByTermContainingIgnoreCase(keyword)
				.stream().map(this::toTermResponse).toList();
	}

	@Transactional
	public void deleteTerm(UUID termId) {
		glossaryRepository.deleteById(termId);
	}

	private String serialize(Map<String, Object> metadata) {
		try {
			return metadata != null ? objectMapper.writeValueAsString(metadata) : null;
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

	private List<String> splitCsv(String value) {
		return value != null && !value.isBlank()
				? Arrays.asList(value.split(","))
				: List.of();
	}

	private CatalogEntryResponse toEntryResponse(DataCatalogEntry e) {
		return CatalogEntryResponse.builder()
				.entryId(e.getId())
				.entityType(e.getEntityType())
				.entityId(e.getEntityId())
				.displayName(e.getDisplayName())
				.description(e.getDescription())
				.businessDomain(e.getBusinessDomain())
				.ownerName(e.getOwnerName())
				.stewardName(e.getStewardName())
				.classification(e.getClassification())
				.sensitivityLevel(e.getSensitivityLevel())
				.tags(splitCsv(e.getTags()))
				.metadata(deserialize(e.getMetadataJson()))
				.createdAt(e.getCreatedAt())
				.updatedAt(e.getUpdatedAt())
				.build();
	}

	private GlossaryTermResponse toTermResponse(GlossaryTerm t) {
		return GlossaryTermResponse.builder()
				.termId(t.getId())
				.term(t.getTerm())
				.definition(t.getDefinition())
				.synonyms(splitCsv(t.getSynonyms()))
				.relatedTerms(splitCsv(t.getRelatedTerms()))
				.domain(t.getDomain())
				.createdAt(t.getCreatedAt())
				.build();
	}
}