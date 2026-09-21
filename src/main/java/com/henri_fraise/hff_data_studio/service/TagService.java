package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.TagRequest;
import com.henri_fraise.hff_data_studio.dto.response.TagResponse;
import com.henri_fraise.hff_data_studio.entity.EntityTag;
import com.henri_fraise.hff_data_studio.entity.Tag;
import com.henri_fraise.hff_data_studio.enums.TagColor;
import com.henri_fraise.hff_data_studio.exception.ResourceAlreadyExistsException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.EntityTagRepository;
import com.henri_fraise.hff_data_studio.repository.TagRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

	private final TagRepository tagRepository;
	private final EntityTagRepository entityTagRepository;
	private final SecurityUtils securityUtils;

	@Transactional
	public TagResponse create(TagRequest request) {
		UUID userId = securityUtils.getCurrentUserId();
		if (tagRepository.existsByUserIdAndName(userId, request.getName())) {
			throw new ResourceAlreadyExistsException("Tag already exists: " + request.getName());
		}
		Tag tag = Tag.builder()
				.name(request.getName())
				.color(request.getColor() != null ? request.getColor() : TagColor.BLUE)
				.user(securityUtils.getCurrentUser())
				.build();
		return toResponse(tagRepository.save(tag));
	}

	@Transactional(readOnly = true)
	public List<TagResponse> list() {
		return tagRepository.findByUserIdOrderByNameAsc(securityUtils.getCurrentUserId())
				.stream().map(this::toResponse).toList();
	}

	@Transactional
	public void delete(UUID tagId) {
		Tag tag = tagRepository.findById(tagId)
				.orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
		tagRepository.delete(tag);
	}

	@Transactional
	public void assign(TagRequest.TagAssignRequest request) {
		for (UUID tagId : request.getTagIds()) {
			Tag tag = tagRepository.findById(tagId)
					.orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
			if (entityTagRepository.findByEntityTypeAndEntityId(
							request.getEntityType(), request.getEntityId()).stream()
					.noneMatch(et -> et.getTag().getId().equals(tagId))) {
				entityTagRepository.save(EntityTag.builder()
						.tag(tag)
						.entityType(request.getEntityType())
						.entityId(request.getEntityId())
						.build());
			}
		}
	}

	@Transactional
	public void unassign(UUID tagId, String entityType, UUID entityId) {
		entityTagRepository.deleteByTagIdAndEntityTypeAndEntityId(tagId, entityType, entityId);
	}

	@Transactional(readOnly = true)
	public List<TagResponse> getTagsForEntity(String entityType, UUID entityId) {
		return entityTagRepository.findByEntityTypeAndEntityId(entityType, entityId)
				.stream().map(et -> toResponse(et.getTag())).toList();
	}

	private TagResponse toResponse(Tag tag) {
		return TagResponse.builder()
				.tagId(tag.getId())
				.name(tag.getName())
				.color(tag.getColor())
				.createdAt(tag.getCreatedAt())
				.build();
	}
}