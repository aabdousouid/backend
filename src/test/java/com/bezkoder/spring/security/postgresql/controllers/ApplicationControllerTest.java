package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.services.ApplicationServiceImpl;
import com.bezkoder.spring.security.postgresql.services.JobServiceImpl;
import com.bezkoder.spring.security.postgresql.services.OllamaService;
import com.bezkoder.spring.security.postgresql.services.ProfileServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationServiceImpl applicationService;

    @Mock
    private JobServiceImpl jobService;

    @Mock
    private ProfileServiceImpl profileService;

    @Mock
    private OllamaService ollamaService;

    @Mock
    private Tika tika;

    @InjectMocks
    private ApplicationController applicationController;

    private User testUser;
    private Job testJob;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        System.out.println("=== Setting up test data ===");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        System.out.println("Created test user: " + testUser.getUsername());

        testJob = new Job();
        testJob.setJobId(1L);
        testJob.setTitle("Software Engineer");
        testJob.setDescription("Java Developer Position");
        System.out.println("Created test job: " + testJob.getTitle());

        testApplication = new Application();
        testApplication.setApplicationId(1L);
        testApplication.setUser(testUser);
        testApplication.setJob(testJob);
        testApplication.setStatus(ApplicationStatus.valueOf("PENDING"));
        System.out.println("Created test application with ID: " + testApplication.getApplicationId());

        System.out.println("=== Setup completed ===\n");
    }

    @Test
    void testApplyJob_WithoutCV_Success() throws Exception {
        System.out.println("🧪 TEST: Apply job without CV");

        // Given
        Long jobId = 1L;
        Long userId = 1L;

        when(profileService.getUserById(userId)).thenReturn(Optional.of(testUser));
        when(jobService.getByIdJob(jobId)).thenReturn(Optional.of(testJob));
        when(applicationService.applyJob(any(Job.class), any(User.class), any(Application.class)))
                .thenReturn(testApplication);

        // When
        System.out.println("Executing applyJob without CV file...");
        ResponseEntity<Application> response = applicationController.applyJob(jobId, userId, null);

        // Then
        System.out.println("✅ Response status: " + response.getStatusCode());
        System.out.println("✅ Application ID: " + response.getBody().getApplicationId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testApplication.getApplicationId(), response.getBody().getApplicationId());

        verify(profileService).getUserById(userId);
        verify(jobService).getByIdJob(jobId);
        verify(applicationService).applyJob(any(Job.class), any(User.class), any(Application.class));

        System.out.println("✅ Test passed: Application created without CV\n");
    }

    @Test
    void testApplyJob_WithCV_Success() throws Exception {
        System.out.println("🧪 TEST: Apply job with CV file");

        // Given
        Long jobId = 1L;
        Long userId = 1L;
        MockMultipartFile cvFile = new MockMultipartFile(
                "cv",
                "test-cv.pdf",
                "application/pdf",
                "Test CV content".getBytes()
        );

        String ollamaResponse = "{\"skills\": [\"Java\", \"Spring Boot\"], \"summary\": \"Experienced developer\"}";

        when(profileService.getUserById(userId)).thenReturn(Optional.of(testUser));
        when(jobService.getByIdJob(jobId)).thenReturn(Optional.of(testJob));
        when(tika.parseToString(any(Path.class))).thenReturn("CV text content");


        when(ollamaService.extractCvData(anyString())).thenReturn(ollamaResponse);
        when(applicationService.applyJob(any(Job.class), any(User.class), any(Application.class)))
                .thenReturn(testApplication);

        // Mock static Files methods
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.copy(any(java.io.InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(100L);

            // When
            System.out.println("Executing applyJob with CV file: " + cvFile.getOriginalFilename());
            ResponseEntity<Application> response = applicationController.applyJob(jobId, userId, cvFile);

            // Then
            System.out.println("✅ Response status: " + response.getStatusCode());
            System.out.println("✅ CV processed successfully");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            verify(tika).parseToString(any(Path.class));
            verify(ollamaService).extractCvData(anyString());
        }

        System.out.println("✅ Test passed: Application created with CV processing\n");
    }

    @Test
    void testApplyJob_UserNotFound_ThrowsException() {
        System.out.println("🧪 TEST: Apply job with non-existent user");

        // Given
        Long jobId = 1L;
        Long userId = 999L;

        when(profileService.getUserById(userId)).thenReturn(Optional.empty());

        // When & Then
        System.out.println("Attempting to apply with non-existent user ID: " + userId);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            applicationController.applyJob(jobId, userId, null);
        });

        System.out.println("✅ Expected exception caught: " + exception.getMessage());
        assertEquals("User not found", exception.getMessage());

        verify(profileService).getUserById(userId);
        verify(jobService, never()).getByIdJob(anyLong());

        System.out.println("✅ Test passed: User not found exception handled correctly\n");
    }

    @Test
    void testApplyJob_JobNotFound_ThrowsException() {
        System.out.println("🧪 TEST: Apply job with non-existent job");

        // Given
        Long jobId = 999L;
        Long userId = 1L;

        when(profileService.getUserById(userId)).thenReturn(Optional.of(testUser));
        when(jobService.getByIdJob(jobId)).thenReturn(Optional.empty());

        // When & Then
        System.out.println("Attempting to apply for non-existent job ID: " + jobId);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            applicationController.applyJob(jobId, userId, null);
        });

        System.out.println("✅ Expected exception caught: " + exception.getMessage());
        assertEquals("Job not found", exception.getMessage());

        verify(profileService).getUserById(userId);
        verify(jobService).getByIdJob(jobId);

        System.out.println("✅ Test passed: Job not found exception handled correctly\n");
    }

    @Test
    void testGetApplications_Success() {
        System.out.println("🧪 TEST: Get all applications");

        // Given
        List<Application> applications = Arrays.asList(testApplication);
        when(applicationService.findAllApplications()).thenReturn(applications);

        // When
        System.out.println("Fetching all applications...");
        ResponseEntity<?> response = applicationController.getApplications();

        // Then
        System.out.println("✅ Response status: " + response.getStatusCode());
        System.out.println("✅ Applications found: " + ((List<?>) response.getBody()).size());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(applications, response.getBody());

        verify(applicationService).findAllApplications();

        System.out.println("✅ Test passed: Successfully retrieved all applications\n");
    }

    @Test
    void testGetUserApplications_Success() {
        System.out.println("🧪 TEST: Get user applications");

        // Given
        Long userId = 1L;
        List<Application> userApplications = Arrays.asList(testApplication);
        when(applicationService.findUserApplications(userId)).thenReturn(userApplications);

        // When
        System.out.println("Fetching applications for user ID: " + userId);
        ResponseEntity<?> response = applicationController.getUserApplications(userId);

        // Then
        System.out.println("✅ Response status: " + response.getStatusCode());
        System.out.println("✅ User applications found: " + ((List<?>) response.getBody()).size());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userApplications, response.getBody());

        verify(applicationService).findUserApplications(userId);

        System.out.println("✅ Test passed: Successfully retrieved user applications\n");
    }

    @Test
    void testDeleteApplication_Success() {
        System.out.println("🧪 TEST: Delete application");

        // Given
        Long applicationId = 1L;
        doNothing().when(applicationService).deleteById(applicationId);

        // When
        System.out.println("Deleting application with ID: " + applicationId);
        ResponseEntity<?> response = applicationController.deleteById(applicationId);

        // Then
        System.out.println("✅ Response status: " + response.getStatusCode());

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(applicationService).deleteById(applicationId);

        System.out.println("✅ Test passed: Application deleted successfully\n");
    }

    @Test
    void testFindById_Success() {
        System.out.println("🧪 TEST: Find application by ID");

        // Given
        Long applicationId = 1L;
        when(applicationService.findById(applicationId)).thenReturn(testApplication);

        // When
        System.out.println("Finding application with ID: " + applicationId);
        ResponseEntity<Application> response = applicationController.findById(applicationId);

        // Then
        System.out.println("✅ Response status: " + response.getStatusCode());
        System.out.println("✅ Found application ID: " + response.getBody().getApplicationId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testApplication, response.getBody());

        verify(applicationService).findById(applicationId);

        System.out.println("✅ Test passed: Application found successfully\n");
    }

    @Test
    void testUpdateStatus_Success() {
        System.out.println("🧪 TEST: Update application status");

        // Given
        Long applicationId = 1L;
        String newStatus = "APPROVED";
        testApplication.setStatus(ApplicationStatus.valueOf(newStatus));

        when(applicationService.updateStatus(applicationId, newStatus)).thenReturn(testApplication);

        // When
        System.out.println("Updating application " + applicationId + " status to: " + newStatus);
        ResponseEntity<Application> response = applicationController.updateStatus(applicationId, newStatus);

        // Then
        System.out.println("✅ Response status: " + response.getStatusCode());
        System.out.println("✅ Updated status: " + response.getBody().getStatus());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newStatus, response.getBody().getStatus().name());

        verify(applicationService).updateStatus(applicationId, newStatus);

        System.out.println("✅ Test passed: Status updated successfully\n");
    }

    @Test
    void testAddComments_Success() {
        System.out.println("🧪 TEST: Add comments to application");

        // Given
        Long applicationId = 1L;
        String comment = "Great candidate, proceed with interview";
        List<String> comments = new ArrayList<String>();
        comments.add(comment);
        testApplication.setAdminComments(comments);

        when(applicationService.addComment(applicationId, comment)).thenReturn(testApplication);

        // When
        System.out.println("Adding comment to application " + applicationId + ": " + comment);
        ResponseEntity<Application> response = applicationController.addComments(applicationId, comment);

        // Then
        System.out.println("✅ Response status: " + response.getStatusCode());
        System.out.println("✅ Added comment: " + response.getBody().getAdminComments());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getAdminComments().contains(comment));

        verify(applicationService).addComment(applicationId, comment);

        System.out.println("✅ Test passed: Comment added successfully\n");
    }

    @Test
    void testFindByUserAndJob_Success() {
        System.out.println("🧪 TEST: Find application by user and job");

        // Given
        Long userId = 1L;
        Long jobId = 1L;
        Optional<Boolean> exists = Optional.of(true);

        when(applicationService.findByUserAndJob(userId, jobId)).thenReturn(exists);

        // When
        System.out.println("Checking if user " + userId + " applied for job " + jobId);
        Optional<Boolean> result = applicationController.findByUserAndJob(userId, jobId);

        // Then
        System.out.println("✅ Application exists: " + result.orElse(false));

        assertTrue(result.isPresent());
        assertTrue(result.get());

        verify(applicationService).findByUserAndJob(userId, jobId);

        System.out.println("✅ Test passed: User-job application relationship found\n");
    }

    @Test
    void testFindByApplicationId_Success() {
        System.out.println("🧪 TEST: Find quiz results by application ID");

        // Given
        Long applicationId = 1L;
        QuizResults quizResults = new QuizResults();
        quizResults.setId(1L);
        quizResults.setScore(85);
        Optional<QuizResults> results = Optional.of(quizResults);

        when(applicationService.findQuizByApplicationId(applicationId)).thenReturn(results);

        // When
        System.out.println("Finding quiz results for application ID: " + applicationId);
        Optional<QuizResults> result = applicationController.findByApplicationId(applicationId);

        // Then
        System.out.println("✅ Quiz results found with score: " + result.get().getScore());

        assertTrue(result.isPresent());
        assertEquals(85, result.get().getScore());

        verify(applicationService).findQuizByApplicationId(applicationId);

        System.out.println("✅ Test passed: Quiz results found successfully\n");
    }

    @Test
    void testApplyJob_InvalidOllamaResponse_ThrowsException() throws Exception {
        System.out.println("🧪 TEST: Apply job with invalid Ollama response");

        // Given
        Long jobId = 1L;
        Long userId = 1L;
        MockMultipartFile cvFile = new MockMultipartFile(
                "cv",
                "test-cv.pdf",
                "application/pdf",
                "Test CV content".getBytes()
        );

        String invalidOllamaResponse = "Invalid response without JSON";

        when(profileService.getUserById(userId)).thenReturn(Optional.of(testUser));
        when(jobService.getByIdJob(jobId)).thenReturn(Optional.of(testJob));
        when(tika.parseToString(any(Path.class))).thenReturn("CV text content");


        when(ollamaService.extractCvData(anyString())).thenReturn(invalidOllamaResponse);

        // Mock static Files methods
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.copy(any(java.io.InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenReturn(100L);

            // When & Then
            System.out.println("Processing CV with invalid Ollama response...");

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                applicationController.applyJob(jobId, userId, cvFile);
            });

            System.out.println("✅ Expected exception caught: " + exception.getMessage());
            assertEquals("No valid JSON block found in Ollama response.", exception.getMessage());
        }

        System.out.println("✅ Test passed: Invalid Ollama response handled correctly\n");
    }
}