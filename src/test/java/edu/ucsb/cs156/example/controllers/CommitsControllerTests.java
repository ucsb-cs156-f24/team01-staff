package edu.ucsb.cs156.example.controllers;


import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Commit;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.CommitRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

@WebMvcTest(controllers = CommitsController.class)
@Import(TestConfig.class)
public class CommitsControllerTests extends ControllerTestCase  {
    
    @MockBean
    CommitRepository commitRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/commits/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/commits/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/commits/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/commits/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsbdates() throws Exception {

            // arrange

            ZonedDateTime zdt1 = ZonedDateTime.parse("2022-01-03T00:00:00Z");

            Commit commit1 = Commit.builder()
                .url("https://github.com/ucsb-cs156-f24/STARTER-team02/commit/e107dcbce881afa1ea70ebc93c8dfb91cebb8630")
                .message("Merge pull request #212 from ucsb-cs156-f24/pc-update-pom-xml\n" +  "pc - update version in pom.xml")
                .authorLogin("pconrad")
                .commitTime(zdt1)
                .build();

            ArrayList<Commit> expectedCommits = new ArrayList<>();
            expectedCommits.add(commit1);

            when(commitRepository.findAll()).thenReturn(expectedCommits);

            // act
            MvcResult response = mockMvc.perform(get("/api/commits/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(commitRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedCommits);

            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_committ() throws Exception {
            // arrange

            ZonedDateTime zdt1 = ZonedDateTime.parse("2022-01-03T00:00:00Z");

            Commit commit1 = Commit.builder()
            .url("https://github.com/ucsb-cs156-f24/STARTER-team02/commit/e107dcbce881afa1ea70ebc93c8dfb91cebb8630")
            .message("pc-updated")
            .authorLogin("pconrad")
            .commitTime(zdt1)
            .build();
         

            when(commitRepository.save(eq(commit1))).thenReturn(commit1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/commits/post?message=pc-updated&url=https://github.com/ucsb-cs156-f24/STARTER-team02/commit/e107dcbce881afa1ea70ebc93c8dfb91cebb8630&authorLogin=pconrad&commitTime=2022-01-03T00:00:00Z")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(commitRepository, times(1)).save(eq(commit1));
            String expectedJson = mapper.writeValueAsString(commit1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/commits?id=7"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {

            // arrange

            ZonedDateTime zdt1 = ZonedDateTime.parse("2022-01-03T00:00:00Z");

            Commit commit1 = Commit.builder()
            .url("https://github.com/ucsb-cs156-f24/STARTER-team02/commit/e107dcbce881afa1ea70ebc93c8dfb91cebb8630")
            .message("pc-updated")
            .authorLogin("pconrad")
            .commitTime(zdt1)
            .build();

            when(commitRepository.findById(eq(7L))).thenReturn(Optional.of(commit1));

            // act
            MvcResult response = mockMvc.perform(get("/api/commits?id=7"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(commitRepository, times(1)).findById(eq(7L));
            String expectedJson = mapper.writeValueAsString(commit1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);

    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(commitRepository.findById(eq(7L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/commits?id=7"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(commitRepository, times(1)).findById(eq(7L));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("Commit with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_commit() throws Exception {
            // arrange

            ZonedDateTime zdt1 = ZonedDateTime.parse("2022-01-03T00:00:00Z");
            ZonedDateTime zdt2 = ZonedDateTime.parse("2022-01-04T00:00:00Z");


            Commit commit1 = Commit.builder()
            .url("https://github.com/ucsb-cs156-f24/STARTER-team02/commit/e107dcbce881afa1ea70ebc93c8dfb91cebb8630")
            .message("pc-updated")
            .authorLogin("pconrad")
            .commitTime(zdt1)
            .build();


            Commit editedCommit = Commit.builder()
            .url("https://github.com/ucsb-cs156-f24/STARTER-team01/commit/e107dcbce881afa1ea70ebc93c8dfb91cebb8630")
            .message("pc-new-message")
            .authorLogin("cgaucho")
            .commitTime(zdt2)
            .build();
           

            String requestBody = mapper.writeValueAsString(editedCommit);

            when(commitRepository.findById(eq(67L))).thenReturn(Optional.of(commit1));

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/commits?id=67")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(commitRepository, times(1)).findById(67L);
            verify(commitRepository, times(1)).save(editedCommit); 
            String responseString = response.getResponse().getContentAsString();
            assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_commit_that_does_not_exist() throws Exception {
            // arrange

            ZonedDateTime zdt1 = ZonedDateTime.parse("2022-01-03T00:00:00Z");

            Commit commit1 = Commit.builder()
            .url("https://github.com/ucsb-cs156-f24/STARTER-team02/commit/e107dcbce881afa1ea70ebc93c8dfb91cebb8630")
            .message("pc-updated")
            .authorLogin("pconrad")
            .commitTime(zdt1)
            .build();

          
            String requestBody = mapper.writeValueAsString(commit1);

            when(commitRepository.findById(eq(67L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/commits?id=67")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(commitRepository, times(1)).findById(67L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("Commit with id 67 not found", json.get("message"));

    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_commit() throws Exception {
            // arrange

            ZonedDateTime zdt1 = ZonedDateTime.parse("2022-01-03T00:00:00Z");

            Commit commit1 = Commit.builder()
            .url("https://github.com/ucsb-cs156-f24/STARTER-team02/commit/e107dcbce881afa1ea70ebc93c8dfb91cebb8630")
            .message("pc-updated")
            .authorLogin("pconrad")
            .commitTime(zdt1)
            .build();

            when(commitRepository.findById(eq(15L))).thenReturn(Optional.of(commit1));

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/commits?id=15")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(commitRepository, times(1)).findById(15L);
            verify(commitRepository, times(1)).delete(eq(commit1));

            Map<String, Object> json = responseToJson(response);
            assertEquals("Commit with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_commit_and_gets_right_error_message()
                    throws Exception {
            // arrange

            when(commitRepository.findById(eq(15L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/commits?id=15")
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(commitRepository, times(1)).findById(15L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("Commit with id 15 not found", json.get("message"));
    }


}
