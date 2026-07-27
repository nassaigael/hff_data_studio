package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {

  private Long totalUsers;

  private Long activeUsers;

  private Long inactiveUsers;

  private Long totalCategories;

  // Role statistics
  private Long adminCount;

  private Long analystCount;

  private Long consultantCount;

  private Long guestCount;

  private Map<UserRole, Long> roleDistribution;

  private Long newUsersLast30Days;

  private Long newUsersLast7Days;

  private Long newUsersLast24Hours;

  private Long newUsersToday;

  private Long activeUsersLast30Days;

  private Long activeUsersLast7Days;

  private Long activeUsersLast24Hours;

  private Long activeUsersToday;

  private Long usersWithLogin;

  private Long usersWithoutLogin;

  private Double averageLoginFrequency;

  private Double averageDaysSinceLastLogin;

  private LocalDateTime firstUser;

  private LocalDateTime lastUser;

  private LocalDateTime lastLoginEver;

  private LocalDateTime firstLoginEver;

  private Double activityRate;

  private Double activePercentage;

  private Double inactivePercentage;

  private Double newUsersPercentage;

  private Double adminPercentage;

  private Double analystPercentage;

  private Double consultantPercentage;

  private Double guestPercentage;

  private Long growthLast30Days;

  private Long growthLast7Days;

  private Double growthPercentageLast30Days;

  private Double growthPercentageLast7Days;

  private LocalDateTime statisticsGeneratedAt;
}