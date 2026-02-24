package com.karthik.task_management_backend.config;

import com.karthik.task_management_backend.security.JwtAuthenticationFilter;
import com.karthik.task_management_backend.security.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for OAuth2 and JWT-based authentication.
 * 
 * Configures:
 * - CORS for frontend communication (http://localhost:3000, deployed domains)
 * - Session management with JWT tokens (stateless)
 * - Public endpoints (/api/auth/*, health checks)
 * - Protected resources (all other /api/* endpoints)
 * - JWT authentication filter integration
 * - OAuth2 client auto-configuration
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    /**
     * Configuration constants for CORS and security
     */
    private static final String[] PUBLIC_PATTERNS = {
            "/api/auth/login",
            "/api/auth/callback",
            "/login/oauth2/code/google",
            "/health",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    private static final String[] PROTECTED_PATTERNS = {
            "/api/**"
    };

    /**
     * Password encoder bean for any future use cases (e.g., API keys, internal auth)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("Initializing BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS configuration to allow frontend communication
     * Allows credentials (cookies, authorization headers) for OAuth2 and JWT flows
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("Configuring CORS for frontend communication");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Development and production domains
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",           // Local development
                "http://localhost:5173",           // Vite dev server
                "https://task-management-ui.vercel.app",  // Deployed frontend
                "https://task-management-web.vercel.app"
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.toString(),
                HttpMethod.POST.toString(),
                HttpMethod.PUT.toString(),
                HttpMethod.DELETE.toString(),
                HttpMethod.OPTIONS.toString()
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Set-Cookie",
                "X-Total-Count"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);  // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Security filter chain configuration
     * 
     * Defines:
     * - Public endpoints without authentication
     * - Protected endpoints requiring JWT
     * - Stateless session management
     * - JWT filter positioning in filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring security filter chain");
        
        http
                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Disable CSRF (stateless API with JWT)
                .csrf(csrf -> csrf.disable())
                
                // Configure session management (stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configure authorization
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(PUBLIC_PATTERNS).permitAll()
                        // All other API endpoints require authentication
                        .requestMatchers(PROTECTED_PATTERNS).authenticated()
                        // Any other requests allowed (framework endpoints)
                        .anyRequest().permitAll()
                )
                
                // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                
                // Disable default form login (API-driven authentication)
                .formLogin(form -> form.disable())
                
                // Disable HTTP Basic (JWT-based authentication)
                .httpBasic(basic -> basic.disable())
                
                // Add security headers
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
                        ))
                        .xssProtection()
                        .and()
                        .contentTypeOptions()
                );
        
        return http.build();
    }

    /**
     * Custom authentication manager with JWT provider
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        log.debug("Configuring authentication manager with JWT provider");
        
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        
        authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
        
        return authenticationManagerBuilder.build();
    }
}
