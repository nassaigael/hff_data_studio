Parfait ! Passons maintenant au package `security` avec toute la configuration de sécurité Spring Boot.

---

## Structure du package

```
com.henri_fraise.hff_data_studio.security
├── config
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── MethodSecurityConfig.java
├── filter
│   └── JwtAuthenticationFilter.java
├── handler
│   ├── CustomAuthenticationEntryPoint.java
│   ├── CustomAccessDeniedHandler.java
│   └── CustomAuthenticationSuccessHandler.java
├── provider
│   └── CustomAuthenticationProvider.java
├── token
│   └── TokenBlacklistService.java
└── annotation
    ├── CurrentUser.java
    └── HasPermission.java
```

---

## 1. Configuration de sécurité

### SecurityConfig.java

```java
package com.henri_fraise.hff_data_studio.security.config;

import com.henri_fraise.hff_data_studio.security.filter.JwtAuthenticationFilter;
import com.henri_fraise.hff_data_studio.security.handler.CustomAccessDeniedHandler;
import com.henri_fraise.hff_data_studio.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    // ==================== Public Endpoints ====================
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/**",
        "/api/v1/health",
        "/api/v1/actuator/health",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/v3/api-docs.yaml",
        "/api/v1/actuator/info"
    };

    // ==================== Admin Endpoints ====================
    private static final String[] ADMIN_ENDPOINTS = {
        "/api/v1/admin/**",
        "/api/v1/users/**",
        "/api/v1/categories/**",
        "/api/v1/permissions/**",
        "/api/v1/audit/**"
    };

    // ==================== User Endpoints ====================
    private static final String[] USER_ENDPOINTS = {
        "/api/v1/projects/**",
        "/api/v1/files/**",
        "/api/v1/datasets/**",
        "/api/v1/exploration/**",
        "/api/v1/cleaning/**",
        "/api/v1/analyses/**",
        "/api/v1/executions/**",
        "/api/v1/exports/**",
        "/api/v1/results/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                // Admin endpoints
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                // User endpoints
                .requestMatchers(USER_ENDPOINTS).authenticated()
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:8080",
            "https://catalyst.hff.re"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

---

### CorsConfig.java

```java
package com.henri_fraise.hff_data_studio.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:8080",
            "https://catalyst.hff.re"
        ));
        config.setAllowedHeaders(Arrays.asList(
            "Origin",
            "Content-Type",
            "Accept",
            "Authorization",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        config.setExposedHeaders(List.of(
            "Authorization",
            "Content-Disposition",
            "X-Total-Count"
        ));
        config.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
```

---

### MethodSecurityConfig.java

```java
package com.henri_fraise.hff_data_studio.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return super.createExpressionHandler();
    }
}
```

---

## 2. Filtres

### JwtAuthenticationFilter.java

```java
package com.henri_fraise.hff_data_studio.security.filter;

import com.henri_fraise.hff_data_studio.exception.TokenExpiredException;
import com.henri_fraise.hff_data_studio.exception.TokenInvalidException;
import com.henri_fraise.hff_data_studio.security.token.TokenBlacklistService;
import com.henri_fraise.hff_data_studio.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip authentication for OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(BEARER_PREFIX.length());

            // Check if token is blacklisted
            if (tokenBlacklistService.isBlacklisted(jwt)) {
                log.warn("Token is blacklisted: {}", jwt);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been invalidated");
                return;
            }

            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Authenticated user: {}", userEmail);
                }
            }
        } catch (TokenExpiredException ex) {
            log.warn("Token expired: {}", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has expired");
            return;
        } catch (TokenInvalidException ex) {
            log.warn("Invalid token: {}", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        } catch (Exception ex) {
            log.error("Error processing JWT: {}", ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/v1/health") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/");
    }
}
```

---

## 3. Handlers

### CustomAuthenticationEntryPoint.java

```java
package com.henri_fraise.hff_data_studio.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("UNAUTHORIZED")
            .message("Authentication required to access this resource")
            .details(authException.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.UNAUTHORIZED.value())
            .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
```

---

### CustomAccessDeniedHandler.java

```java
package com.henri_fraise.hff_data_studio.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("FORBIDDEN")
            .message("You don't have permission to access this resource")
            .details(accessDeniedException.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.FORBIDDEN.value())
            .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
```

---

### CustomAuthenticationSuccessHandler.java

```java
package com.henri_fraise.hff_data_studio.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        log.info("Authentication successful: {}", authentication.getName());
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Authentication successful");
    }
}
```

---

## 4. Provider

### CustomAuthenticationProvider.java

```java
package com.henri_fraise.hff_data_studio.security.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new BadCredentialsException("Invalid credentials");
        }

        return new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

---

## 5. Token Blacklist

### TokenBlacklistService.java

```java
package com.henri_fraise.hff_data_studio.security.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    // In-memory blacklist (for production, use Redis or database)
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    private static final long TOKEN_BLACKLIST_DURATION = 24 * 60 * 60 * 1000; // 24 hours

    public void blacklist(String token) {
        if (token != null && !token.isEmpty()) {
            blacklistedTokens.put(token, System.currentTimeMillis() + TOKEN_BLACKLIST_DURATION);
            log.debug("Token blacklisted: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        Long expiry = blacklistedTokens.get(token);
        if (expiry == null) {
            return false;
        }
        
        if (expiry < System.currentTimeMillis()) {
            // Token has expired from blacklist
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }

    public void removeFromBlacklist(String token) {
        if (token != null && !token.isEmpty()) {
            blacklistedTokens.remove(token);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void cleanupBlacklist() {
        long now = System.currentTimeMillis();
        long before = blacklistedTokens.size();
        
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
        
        long after = blacklistedTokens.size();
        if (before != after) {
            log.info("Cleaned up {} expired tokens from blacklist", before - after);
        }
    }

    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}
```

---

## 6. Annotations

### CurrentUser.java

```java
package com.henri_fraise.hff_data_studio.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
```

---

### HasPermission.java

```java
package com.henri_fraise.hff_data_studio.security.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

    String value();

    String entity() default "";

    String operation() default "READ";
}
```

---

## 7. Aspect pour l'annotation @HasPermission

### PermissionAspect.java

```java
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

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }

        String email = authentication.getName();
        User currentUser = userService.getUserEntityByEmail(email);

        // Check if user has permission
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
```

---

## 8. Configuration pour la sécurité des méthodes

### SecurityExpressionConfig.java

```java
package com.henri_fraise.hff_data_studio.security.config;

import com.henri_fraise.hff_data_studio.security.SecurityExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityExpressionConfig {

    @Bean
    public SecurityExpression securityExpression() {
        return new SecurityExpression();
    }
}
```

---

### SecurityExpression.java

```java
package com.henri_fraise.hff_data_studio.security;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.ForbiddenException;
import com.henri_fraise.hff_data_studio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityExpression {

    private final UserService userService;

    public boolean isOwner(UUID resourceUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String email = authentication.getName();
        User currentUser = userService.getUserEntityByEmail(email);
        
        return currentUser.getId().equals(resourceUserId);
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(permission));
    }

    public boolean isOwnerOrAdmin(UUID resourceUserId) {
        return isOwner(resourceUserId) || isAdmin();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }

        String email = authentication.getName();
        return userService.getUserEntityByEmail(email);
    }
}
```

---

## 9. Utilisation dans les Controllers

### ProjectController.java (avec sécurité)

```java
package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.ProjectCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.ProjectUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.security.annotation.CurrentUser;
import com.henri_fraise.hff_data_studio.security.annotation.HasPermission;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.service.ProjectService;
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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ProjectResponse>> getUserProjects(
            @CurrentUser User currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProjectResponse> projects = projectService.getUserProjects(currentUser.getId(), pageable);
        return ResponseEntity.ok(PageResponse.from(projects));
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @HasPermission(value = "PROJECT_VIEW", entity = "Project", operation = "READ")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID projectId) {
        ProjectResponse project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @HasPermission(value = "PROJECT_CREATE", entity = "Project", operation = "CREATE")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreationRequest request,
            @CurrentUser User currentUser) {
        ProjectResponse created = projectService.createProject(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @HasPermission(value = "PROJECT_UPDATE", entity = "Project", operation = "UPDATE")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectUpdateRequest request,
            @CurrentUser User currentUser) {
        ProjectResponse updated = projectService.updateProject(projectId, request, currentUser.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @HasPermission(value = "PROJECT_DELETE", entity = "Project", operation = "DELETE")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId,
            @CurrentUser User currentUser) {
        projectService.deleteProject(projectId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/archive")
    @PreAuthorize("isAuthenticated()")
    @HasPermission(value = "PROJECT_UPDATE", entity = "Project", operation = "UPDATE")
    public ResponseEntity<Void> archiveProject(
            @PathVariable UUID projectId,
            @CurrentUser User currentUser) {
        projectService.archiveProject(projectId, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
```

---

### UserController.java (avec sécurité)

```java
package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.security.annotation.CurrentUser;
import com.henri_fraise.hff_data_studio.security.annotation.HasPermission;
import com.henri_fraise.hff_data_studio.entity.User;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @HasPermission(value = "USER_VIEW", entity = "User", operation = "READ")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(PageResponse.from(users));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @HasPermission(value = "USER_VIEW", entity = "User", operation = "READ")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser User currentUser) {
        return ResponseEntity.ok(userService.getUserById(currentUser.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @HasPermission(value = "USER_MANAGE", entity = "User", operation = "CREATE")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @HasPermission(value = "USER_MANAGE", entity = "User", operation = "UPDATE")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updated = userService.updateUser(userId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @HasPermission(value = "USER_MANAGE", entity = "User", operation = "DELETE")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @HasPermission(value = "USER_MANAGE", entity = "User", operation = "UPDATE")
    public ResponseEntity<Void> activateUser(@PathVariable UUID userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID userId,
            @RequestBody ChangePasswordRequest request,
            @CurrentUser User currentUser) {
        
        // Only allow users to change their own password or admins
        if (!currentUser.getId().equals(userId) && !currentUser.getCategory().getLabel().equals("ADMIN")) {
            throw new ForbiddenException("You can only change your own password");
        }
        
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}

@Data
class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
}
```

---

## 10. Configuration application.properties

```properties
# ==================== Security Configuration ====================

# JWT Configuration
jwt.secret=${JWT_SECRET:your-256-bit-secret-key-for-jwt-signing-please-change-in-production}
jwt.expiration=${JWT_EXPIRATION:900000}
jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION:604800000}

# Security Headers
security.headers.frame-options=SAMEORIGIN
security.headers.xss-protection=1; mode=block
security.headers.content-type-options=nosniff
security.headers.hsts-enabled=true
security.headers.hsts-max-age=31536000

# CORS Configuration
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001}
cors.allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
cors.allowed-headers=Authorization,Content-Type,X-Requested-With,Accept,Origin
cors.exposed-headers=Authorization,Content-Disposition,X-Total-Count
cors.allow-credentials=true
cors.max-age=3600

# Session Configuration
server.servlet.session.timeout=30m
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict

# Password Encoding
security.password-encoder.strength=12

# Rate Limiting (if using Bucket4j or similar)
rate-limit.enabled=true
rate-limit.default.limit=100
rate-limit.default.duration=60
rate-limit.analyses.limit=50
rate-limit.exports.limit=20
```

---

## 11. Mise à jour du GlobalExceptionHandler

### Ajout dans GlobalExceptionHandler.java

```java
// ==================== Security Exceptions ====================

@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDeniedException(
        AccessDeniedException ex,
        HttpServletRequest request) {
    
    log.warn("Access denied: {}", ex.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
        .code("FORBIDDEN")
        .message("Access denied: " + ex.getMessage())
        .timestamp(LocalDateTime.now())
        .path(request.getRequestURI())
        .method(request.getMethod())
        .status(HttpStatus.FORBIDDEN.value())
        .build();
    
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
}

@ExceptionHandler(AuthenticationException.class)
public ResponseEntity<ErrorResponse> handleAuthenticationException(
        AuthenticationException ex,
        HttpServletRequest request) {
    
    log.warn("Authentication error: {}", ex.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
        .code("UNAUTHORIZED")
        .message("Authentication failed: " + ex.getMessage())
        .timestamp(LocalDateTime.now())
        .path(request.getRequestURI())
        .method(request.getMethod())
        .status(HttpStatus.UNAUTHORIZED.value())
        .build();
    
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
}

@ExceptionHandler(ForbiddenException.class)
public ResponseEntity<ErrorResponse> handleForbiddenException(
        ForbiddenException ex,
        HttpServletRequest request) {
    
    log.warn("Forbidden: {}", ex.getMessage());
    
    ErrorResponse response = ErrorResponse.builder()
        .code("FORBIDDEN")
        .message(ex.getMessage())
        .timestamp(LocalDateTime.now())
        .path(request.getRequestURI())
        .method(request.getMethod())
        .status(HttpStatus.FORBIDDEN.value())
        .build();
    
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
}
```

---

## Résumé du package Security

| Composant | Responsabilité |
|-----------|---------------|
| **SecurityConfig** | Configuration principale de sécurité, filtres, authentification |
| **CorsConfig** | Configuration CORS pour les requêtes cross-origin |
| **MethodSecurityConfig** | Configuration de la sécurité au niveau des méthodes |
| **JwtAuthenticationFilter** | Filtre d'authentification JWT |
| **CustomAuthenticationEntryPoint** | Gestion des erreurs d'authentification |
| **CustomAccessDeniedHandler** | Gestion des erreurs d'accès |
| **CustomAuthenticationProvider** | Fournisseur d'authentification personnalisé |
| **TokenBlacklistService** | Gestion de la blacklist des tokens |
| **@CurrentUser** | Annotation pour injecter l'utilisateur courant |
| **@HasPermission** | Annotation pour vérifier les permissions |
| **SecurityExpression** | Expressions de sécurité personnalisées |
| **PermissionAspect** | Aspect pour l'annotation @HasPermission |