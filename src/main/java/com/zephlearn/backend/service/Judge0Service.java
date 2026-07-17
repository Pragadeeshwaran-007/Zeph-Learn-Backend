package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.RunResponse;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Service {

    private static final String API_URL = "https://ce.judge0.com/submissions?base64_encoded=false&wait=true";

    private final RestTemplate restTemplate = new RestTemplate();

    public RunResponse execute(String code, String languageId, String stdin) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("source_code", code);
            requestBody.put("language_id", Integer.parseInt(languageId));
            if (stdin != null && !stdin.isEmpty()) {
                requestBody.put("stdin", stdin);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null) throw new RuntimeException("Empty response from Judge0");

            RunResponse runResponse = new RunResponse();

            String stdout = body.get("stdout") != null ? (String) body.get("stdout") : null;
            String stderr = body.get("stderr") != null ? (String) body.get("stderr") : null;
            String compileOutput = body.get("compile_output") != null ? (String) body.get("compile_output") : null;

            runResponse.setStdout(stdout);
            runResponse.setStderr(stderr != null ? stderr : compileOutput);

            Object timeObj = body.get("time");
            runResponse.setExecutionTime(timeObj != null ? timeObj.toString() : "0");
            Object memoryObj = body.get("memory");
            runResponse.setMemory(memoryObj != null ? memoryObj.toString() : "0");

            Map<String, Object> statusObj = (Map<String, Object>) body.get("status");
            int id = (int) statusObj.get("id");

            runResponse.setVerdict(mapVerdict(id));

            return runResponse;
        } catch (Exception e) {
            e.printStackTrace();
            RunResponse runResponse = new RunResponse();
            runResponse.setVerdict("Runtime Error");
            runResponse.setStderr(e.getMessage());
            return runResponse;
        }
    }

    private String mapVerdict(int judge0StatusId) {
        switch (judge0StatusId) {
            case 3: return "Accepted";
            case 4: return "Wrong Answer";
            case 5: return "TLE";
            case 6: return "Compilation Error";
            case 11:
            case 12:
            case 13: return "Runtime Error";
            default: return "Error";
        }
    }

    public String getLanguageId(String language) {
        switch (language.toLowerCase()) {
            case "cpp": return "54";
            case "java": return "62";
            case "python": return "71";
            case "javascript": return "63";
            default: throw new RuntimeException("Unsupported language: " + language);
        }
    }
}
