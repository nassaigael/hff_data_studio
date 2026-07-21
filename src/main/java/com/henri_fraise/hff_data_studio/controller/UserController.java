package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.ChangePasswordRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

  @GetMapping("/active")
  @PreAuthorize("hasAuthority('USER_VIEW')")
  public ResponseEntity<PageResponse<UserResponse>> getActiveUsers(
      @PageableDefault(size = 20) Pageable pageable) {
    Page<UserResponse> users = userService.getActiveUsers(pageable);
    return ResponseEntity.ok(pageMapper.toPageResponse(users, user -> user));
  }

  @GetMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER_VIEW')")
  public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
    return ResponseEntity.ok(userService.getUserById(userId));
  }

  @GetMapping("/email/{email}")
  @PreAuthorize("hasAuthority('USER_VIEW')")
  public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.getUserByEmail(email));
  }

  @GetMapping("/category/{categoryId}")
  @PreAuthorize("hasAuthority('USER_VIEW')")
  public ResponseEntity<PageResponse<UserResponse>> getUsersByCategory(
      @PathVariable UUID categoryId, @PageableDefault(size = 20) Pageable pageable) {
    Page<UserResponse> users = userService.getUsersByCategory(categoryId, pageable);
    return ResponseEntity.ok(pageMapper.toPageResponse(users, user -> user));
  }

  @PostMapping
  @PreAuthorize("hasAuthority('USER_MANAGE')")
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
  }

  @PutMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER_MANAGE')")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable UUID userId, @Valid @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(userService.updateUser(userId, request));
  }

  @DeleteMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER_MANAGE')")
  public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
    userService.deactivateUser(userId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}/activate")
  @PreAuthorize("hasAuthority('USER_MANAGE')")
  public ResponseEntity<UserResponse> activateUser(@PathVariable UUID userId) {
    return ResponseEntity.ok(userService.activateUser(userId));
  }

  @PatchMapping("/{userId}/password")
  @PreAuthorize("hasAuthority('USER_MANAGE')")
  public ResponseEntity<Void> changePassword(
      @PathVariable UUID userId, @Valid @RequestBody ChangePasswordRequest request) {
    userService.changePassword(userId, request.getNewPassword());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search")
  @PreAuthorize("hasAuthority('USER_VIEW')")
  public ResponseEntity<PageResponse<UserResponse>> searchUsers(
      @RequestParam String searchTerm, @PageableDefault(size = 20) Pageable pageable) {
    Page<UserResponse> users = userService.searchUsers(searchTerm, pageable);
    return ResponseEntity.ok(pageMapper.toPageResponse(users, user -> user));
  }

  @GetMapping("/statistics")
  @PreAuthorize("hasAuthority('USER_VIEW')")
  public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
    return ResponseEntity.ok(userService.getUserStatistics());
  }

  @GetMapping("/statistics/yearly")
  @PreAuthorize("hasAuthority('USER_VIEW')")
  public ResponseEntity<List<Object[]>> getUsersByYear() {
    return ResponseEntity.ok(userService.getUsersGroupedByMonth());
  }

  @GetMapping("/statistics/monthly")
  @PreAuthorize("hasAuthority('USER_VIEW')")
  public ResponseEntity<List<Object[]>> getUsersByMonth(@RequestParam int year) {
    return ResponseEntity.ok(userService.getUsersGroupedByYear(year));
  }
}
