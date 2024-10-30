package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.repositories.RecommendationRequestsRepository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = RecommendationRequestsController.class)
@Import(TestConfig.class)
public class RecommendationRequestsControllerTests extends ControllerTestCase {

    @MockBean
    RecommendationRequestsRepository recommendationRequestsRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/all"))
               .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all_requests() throws Exception {
        RecommendationRequests request1 = RecommendationRequests.builder()
                .requesterEmail("requester1@example.com")
                .professorEmail("prof1@example.com")
                .explanation("Request 1")
                .dateRequested(LocalDateTime.now())
                .dateNeeded(LocalDateTime.now().plusDays(1))
                .name("Request 1")
                .done(false)
                .build();

        RecommendationRequests request2 = RecommendationRequests.builder()
                .requesterEmail("requester2@example.com")
                .professorEmail("prof2@example.com")
                .explanation("Request 2")
                .dateRequested(LocalDateTime.now())
                .dateNeeded(LocalDateTime.now().plusDays(2))
                .name("Request 2")
                .done(false)
                .build();

        ArrayList<RecommendationRequests> expectedRequests = new ArrayList<>(Arrays.asList(request1, request2));
        when(recommendationRequestsRepository.findAll()).thenReturn(expectedRequests);

        MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
                                    .andExpect(status().isOk()).andReturn();

        verify(recommendationRequestsRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedRequests);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @Test
    public void logged_out_users_cannot_get_request_by_id() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests?id=1"))
               .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void user_can_get_request_by_id_when_id_exists() throws Exception {
        RecommendationRequests request = RecommendationRequests.builder()
                .requesterEmail("requester@example.com")
                .professorEmail("prof@example.com")
                .explanation("Sample explanation")
                .dateRequested(LocalDateTime.now())
                .dateNeeded(LocalDateTime.now().plusDays(5))
                .name("Sample Request")
                .done(false)
                .build();

        when(recommendationRequestsRepository.findById(1L)).thenReturn(Optional.of(request));

        MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=1"))
                                    .andExpect(status().isOk()).andReturn();

        verify(recommendationRequestsRepository, times(1)).findById(1L);
        String expectedJson = mapper.writeValueAsString(request);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void user_cannot_get_request_by_id_when_id_does_not_exist() throws Exception {
        when(recommendationRequestsRepository.findById(999L)).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=999"))
                                    .andExpect(status().isNotFound()).andReturn();

        verify(recommendationRequestsRepository, times(1)).findById(999L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("RecommendationRequests with id 999 not found", json.get("message"));
    }
    @Test
    public void logged_out_users_cannot_create_request() throws Exception {
        mockMvc.perform(post("/api/recommendationrequests/post"))
               .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_create_request() throws Exception {
        mockMvc.perform(post("/api/recommendationrequests/post")
                .param("requesterEmail", "user@example.com")
                .param("professorEmail", "prof@example.com")
                .param("explanation", "Test explanation")
                .param("dateRequested", LocalDateTime.now().toString())
                .param("dateNeeded", LocalDateTime.now().plusDays(5).toString())
                .param("name", "Test Request")
                .param("done", "false")
                .with(csrf()))
                .andExpect(status().isForbidden()); 
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_create_new_request() throws Exception {
        RecommendationRequests newRequest = RecommendationRequests.builder()
                .requesterEmail("requester@example.com")
                .professorEmail("prof@example.com")
                .explanation("Need recommendation")
                .dateRequested(LocalDateTime.now())
                .dateNeeded(LocalDateTime.now().plusDays(10))
                .name("New Request")
                .done(false)
                .build();

        when(recommendationRequestsRepository.save(any())).thenReturn(newRequest);

        MvcResult response = mockMvc.perform(
                post("/api/recommendationrequests/post")
                        .param("requesterEmail", "requester@example.com")
                        .param("professorEmail", "prof@example.com")
                        .param("explanation", "Need recommendation")
                        .param("dateRequested", LocalDateTime.now().toString())
                        .param("dateNeeded", LocalDateTime.now().plusDays(10).toString())
                        .param("name", "New Request")
                        .param("done", "false")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        verify(recommendationRequestsRepository, times(1)).save(any(RecommendationRequests.class));
        String expectedJson = mapper.writeValueAsString(newRequest);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_request() throws Exception {
        RecommendationRequests request = RecommendationRequests.builder()
                .requesterEmail("requester@example.com")
                .professorEmail("prof@example.com")
                .explanation("Sample explanation")
                .dateRequested(LocalDateTime.now())
                .dateNeeded(LocalDateTime.now().plusDays(5))
                .name("Sample Request")
                .done(false)
                .build();

        when(recommendationRequestsRepository.findById(1L)).thenReturn(Optional.of(request));

        MvcResult response = mockMvc.perform(delete("/api/recommendationrequests?id=1").with(csrf()))
                                    .andExpect(status().isOk()).andReturn();

        verify(recommendationRequestsRepository, times(1)).findById(1L);
        verify(recommendationRequestsRepository, times(1)).delete(any(RecommendationRequests.class));

        Map<String, Object> json = responseToJson(response);
        assertEquals("Recommendation request with ID 1 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_delete_request_when_id_does_not_exist() throws Exception {
        when(recommendationRequestsRepository.findById(999L)).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(delete("/api/recommendationrequests?id=999").with(csrf()))
                                    .andExpect(status().isNotFound()).andReturn();

        verify(recommendationRequestsRepository, times(1)).findById(999L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("RecommendationRequests with id 999 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_update_existing_request() throws Exception {
        RecommendationRequests originalRequest = RecommendationRequests.builder()
                .requesterEmail("original@example.com")
                .professorEmail("prof@example.com")
                .explanation("Original explanation")
                .dateRequested(LocalDateTime.now())
                .dateNeeded(LocalDateTime.now().plusDays(5))
                .name("Original Request")
                .done(false)
                .build();

        RecommendationRequests updatedRequest = RecommendationRequests.builder()
                .requesterEmail("updated@example.com")
                .professorEmail("prof@example.com")
                .explanation("Updated explanation")
                .dateRequested(LocalDateTime.now())
                .dateNeeded(LocalDateTime.now().plusDays(7))
                .name("Updated Request")
                .done(true)
                .build();

        String requestBody = mapper.writeValueAsString(updatedRequest);

        when(recommendationRequestsRepository.findById(1L)).thenReturn(Optional.of(originalRequest));
        when(recommendationRequestsRepository.save(any(RecommendationRequests.class))).thenReturn(updatedRequest);

        MvcResult response = mockMvc.perform(
                put("/api/recommendationrequests?id=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        verify(recommendationRequestsRepository, times(1)).findById(1L);
        verify(recommendationRequestsRepository, times(1)).save(any(RecommendationRequests.class));
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_update_request_when_id_does_not_exist() throws Exception {
        RecommendationRequests updatedRequest = RecommendationRequests.builder()
                .requesterEmail("updated@example.com")
                .professorEmail("prof@example.com")
                .explanation("Updated explanation")
                .dateRequested(LocalDateTime.now())
                .dateNeeded(LocalDateTime.now().plusDays(7))
                .name("Updated Request")
                .done(true)
                .build();

        String requestBody = mapper.writeValueAsString(updatedRequest);

        when(recommendationRequestsRepository.findById(999L)).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(
                put("/api/recommendationrequests?id=999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        verify(recommendationRequestsRepository, times(1)).findById(999L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("RecommendationRequests with id 999 not found", json.get("message"));
    }
}
