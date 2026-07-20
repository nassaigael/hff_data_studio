package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.CategoryCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.PermissionUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.PermissionResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserCategoryResponse;
import com.henri_fraise.hff_data_studio.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping
	@PreAuthorize("hasAuthority('CATEGORY_VIEW')")
	public ResponseEntity<List<UserCategoryResponse>> getAllCategories() {
		return ResponseEntity.ok(categoryService.getAllCategories());
	}

	@GetMapping("/{categoryId}")
	@PreAuthorize("hasAuthority('CATEGORY_VIEW')")
	public ResponseEntity<UserCategoryResponse> getCategoryById(@PathVariable UUID categoryId) {
		return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
	}

	@PostMapping
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ResponseEntity<UserCategoryResponse> createCategory(
			@Valid @RequestBody CategoryCreationRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(categoryService.createCategory(request));
	}

	@PutMapping("/{categoryId}")
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ResponseEntity<UserCategoryResponse> updateCategory(
			@PathVariable UUID categoryId,
			@Valid @RequestBody CategoryCreationRequest request) {
		return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
	}

	@DeleteMapping("/{categoryId}")
	@PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
	public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
		categoryService.deleteCategory(categoryId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{categoryId}/permissions")
	@PreAuthorize("hasAuthority('PERMISSION_VIEW')")
	public ResponseEntity<List<PermissionResponse>> getCategoryPermissions(
			@PathVariable UUID categoryId) {
		return ResponseEntity.ok(categoryService.getCategoryPermissions(categoryId));
	}

	@PutMapping("/{categoryId}/permissions")
	@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
	public ResponseEntity<Void> updateCategoryPermissions(
			@PathVariable UUID categoryId,
			@Valid @RequestBody PermissionUpdateRequest request) {
		categoryService.updateCategoryPermissions(categoryId, request.getPermissionIds());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/permissions")
	@PreAuthorize("hasAuthority('PERMISSION_VIEW')")
	public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
		return ResponseEntity.ok(categoryService.getAllPermissions());
	}
}