package com.githubtimemachine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.githubtimemachine.dto.RepositoryAnalysisRequest;
import com.githubtimemachine.dto.RepositoryAnalysisResponse;
import com.githubtimemachine.exception.InvalidGitHubUrlException;
import com.githubtimemachine.service.ContributorAnalysisService;
import com.githubtimemachine.service.GitAnalyzerService;
import com.githubtimemachine.service.RepositoryTimelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepositoryAnalysisController.class)
class RepositoryAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GitAnalyzerService gitAnalyzerService;

    @MockBean
    private ContributorAnalysisService contributorAnalysisService;

    @MockBean
    private RepositoryTimelineService repositoryTimelineService;

    @Test
    void analyzeRepository_returnsCreatedRepositoryAnalysis() throws Exception {
        RepositoryAnalysisResponse response = RepositoryAnalysisResponse.builder()
                .repositoryId(1L)
                .fullName("example/project")
                .remoteUrl("https://github.com/example/project.git")
                .commitCount(2)
                .contributorCount(1)
                .fileChangeCount(3)
                .build();

        when(gitAnalyzerService.analyzeRepository(anyString())).thenReturn(response);

        RepositoryAnalysisRequest request = new RepositoryAnalysisRequest();
        request.setRepositoryUrl("https://github.com/example/project.git");

        mockMvc.perform(post("/api/repositories/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryId").value(1))
                .andExpect(jsonPath("$.fullName").value("example/project"));
    }

    @Test
    void analyzeRepository_withInvalidUrl_returnsBadRequest() throws Exception {
        doThrow(new InvalidGitHubUrlException("Invalid GitHub repository URL")).when(gitAnalyzerService).analyzeRepository(anyString());

        RepositoryAnalysisRequest request = new RepositoryAnalysisRequest();
        request.setRepositoryUrl("not-a-url");

        mockMvc.perform(post("/api/repositories/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid GitHub repository URL"));
    }
}
