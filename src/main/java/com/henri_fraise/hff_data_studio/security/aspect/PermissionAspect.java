package com.henri_fraise.hff_data_studio.security.aspect;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.ForbiddenException;
import com.henri_fraise.hff_data_studio.security.annotation.HasPermission;
import com.henri_fraise.hff_data_studio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

	private final UserService userService;

	@Around("@annotation(com.henri_fraise.hff_data_studio.security.annotation.HasPermission)")
	public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		HasPermission hasPermission = method.getAnnotation(HasPermission.class);

		String permission = hasPermission.value();
		String entity = hasPermission.entity();
		String operation = hasPermission.operation();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ForbiddenException("Authentication required");
		}

		String email = authentication.getName();
		User currentUser = userService.getUserEntityByEmail(email);

		boolean hasAccess = currentUser.getCategory().getPermissions().stream()
				.anyMatch(p -> p.getCode().equals(permission));

		if (!hasAccess) {
			log.warn("User {} does not have permission '{}' for {} on {}",
					currentUser.getEmail(), permission, operation, entity);
			throw new ForbiddenException("You don't have permission: " + permission);
		}

		return joinPoint.proceed();
	}
}