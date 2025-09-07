package com.bezkoder.spring.security.postgresql.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PythonBridgeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PythonBridgeService service;

    private static final String BASE_URL = "http://localhost:5001";

    @BeforeEach
    void setUp() {
        System.out.println("=== Setting up PythonBridgeServiceTest ===");
        ReflectionTestUtils.setField(service, "pythonApiUrl", BASE_URL);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        System.out.println("Injected base url: " + BASE_URL);
        System.out.println("=========================================\n");
    }

    @Test
    void parseCv_returnsResponse() throws Exception {
        System.out.println("🧪 TEST: parseCv_returnsResponse");

        var file = new MockMultipartFile("cv", "test.pdf", "application/pdf", "pdf bytes".getBytes());
        var expected = Map.of("name", "John", "skills", List.of("Java", "Spring"));

        when(restTemplate.postForEntity(eq(BASE_URL + "/parse-cv"), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(expected));

        var result = service.parseCv(file);

        System.out.println("✅ Received: " + result);
        assertEquals(expected, result);
        System.out.println("✅ Test passed\n");
    }

    @Test
    void matchJobs_returnsResponse() {
        var parsedCv = Map.<String, Object>of("name", "Jane", "years", 3);

        var jobs = List.<Map<String, Object>>of(
                Map.<String, Object>of("id", 1, "title", "Backend Engineer"),
                Map.<String, Object>of("id", 2, "title", "Fullstack Developer")
        );

        var expected = List.<Map<String, Object>>of(
                Map.<String, Object>of("jobId", 1, "score", 0.95),
                Map.<String, Object>of("jobId", 2, "score", 0.76)
        );

        when(restTemplate.postForEntity(eq(BASE_URL + "/match-jobs"), any(), eq(List.class)))
                .thenReturn(ResponseEntity.ok(expected));

        var result = service.matchJobs(parsedCv, jobs);

        assertEquals(expected, result);
    }

    @Test

    void startQuiz_returnsResponse() {
        // ✅ Force value type = Object
        Map<String, Object> parsedCv = new HashMap<>();
        parsedCv.put("name", "Alex");

        Map<String, Object> job = new HashMap<>();
        job.put("id", 99);                 // Integer is fine; value type is Object
        job.put("title", "AI Engineer");   // String is also Object

        Map<String, Object> expected = new HashMap<>();
        expected.put("quizId", "q-123");
        expected.put("questions", List.of(
                Map.of("q", "What is OOP?"),
                Map.of("q", "Explain DI?")
        ));

        when(restTemplate.postForEntity(eq(BASE_URL + "/generate-quiz"), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(expected));

        Map<String, Object> result = service.startQuiz(parsedCv, job, "Alex Morgan");

        assertEquals(expected, result);
    }


    @Test
    void submitQuiz_returnsResponse() {
        System.out.println("🧪 TEST: submitQuiz_returnsResponse");

        var answers = List.of(1, 0, 2);
        var expected = Map.of("score", 85, "passed", true);

        when(restTemplate.postForEntity(eq(BASE_URL + "/submit-quiz"), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(expected));

        var result = service.submitQuiz(answers, "Sam");

        System.out.println("✅ Received: " + result);
        assertEquals(expected, result);
        System.out.println("✅ Test passed\n");
    }

    @Test
    void parseCv_whenDownstreamFails_bubblesUp() throws Exception {
        System.out.println("🧪 TEST: parseCv_whenDownstreamFails_bubblesUp");

        var file = new MockMultipartFile("cv", "test.pdf", "application/pdf", "pdf".getBytes());

        when(restTemplate.postForEntity(eq(BASE_URL + "/parse-cv"), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Downstream error"));

        assertThrows(Exception.class, () -> service.parseCv(file));

        System.out.println("✅ Exception bubbled as expected\n");
    }
}
