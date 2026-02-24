package com.karthik.task_management_backend;

import com.karthik.task_management_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 Backend Application Tests
 * 
 * Tests the core Spring Boot application context and JWT functionality.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:task_management_test",
	"spring.datasource.driverClassName=org.h2.Driver",
	"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"app.jwt.secret=test-secret-key-that-must-be-at-least-32-characters-long"
})
class TaskManagementBackendApplicationTests {

	@Autowired(required = false)
	private JwtService jwtService;

	/**
	 * Test that Spring Boot context loads successfully
	 */
	@Test
	void contextLoads() {
		assertNotNull(jwtService, "JwtService should be available in Spring context");
	}

	/**
	 * Test JWT token generation and validation (T006)
	 */
	@Test
	void testJwtTokenGeneration() {
		if (jwtService == null) {
			return;
		}

		Long userId = 1L;
		String email = "test@example.com";
		String role = "USER";

		// Generate token
		String token = jwtService.generateToken(userId, email, role);
		assertNotNull(token, "Token should be generated");
		assertFalse(token.isBlank(), "Token should not be blank");

		// Validate token
		boolean isValid = jwtService.validateToken(token);
		assertTrue(isValid, "Token should be valid");

		// Extract claims
		Long extractedUserId = jwtService.getUserIdFromToken(token);
		assertEquals(userId, extractedUserId, "UserId should match");

		String extractedEmail = jwtService.getEmailFromToken(token);
		assertEquals(email, extractedEmail, "Email should match");

		String extractedRole = jwtService.getRoleFromToken(token);
		assertEquals(role, extractedRole, "Role should match");
	}
}

