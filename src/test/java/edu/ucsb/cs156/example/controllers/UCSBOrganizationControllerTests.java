package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

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

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsborganization/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganization() throws Exception {

                // arrange

                UCSBOrganization muDelta = UCSBOrganization.builder()
                                .orgCode("MD")
                                .orgTranslationShort("Mu Delta")
                                .orgTranslation("Mu Delta")
                                .inactive(true)
                                .build();

                UCSBOrganization thetaTau = UCSBOrganization.builder()
                                .orgCode("TT")
                                .orgTranslationShort("Theta Tau")   
                                .orgTranslation("Theta Tau")
                                .inactive(true)
                                .build();


                ArrayList<UCSBOrganization> expectedOrgs = new ArrayList<>();
                expectedOrgs.addAll(Arrays.asList(muDelta, thetaTau));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrgs);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrgs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsborganization...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organization() throws Exception {
                // arrange

                UCSBOrganization muDelta = UCSBOrganization.builder()
                                .orgCode("MD")
                                .orgTranslationShort("Mu_Delta")
                                .orgTranslation("Mu_Delta")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.save(eq(muDelta))).thenReturn(muDelta);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganization/post?orgCode=MD&orgTranslationShort=Mu_Delta&orgTranslation=Mu_Delta&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(muDelta);
                String expectedJson = mapper.writeValueAsString(muDelta);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/ucsborganization?...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?orgCode=MD"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                UCSBOrganization muDelta = UCSBOrganization.builder()
                                .orgCode("MD")
                                .orgTranslationShort("Mu_Delta")
                                .orgTranslation("Mu_Delta")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("MD"))).thenReturn(Optional.of(muDelta));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=MD"))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById(eq("MD"));
                String expectedJson = mapper.writeValueAsString(muDelta);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange
                when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=munger-hall"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById(eq("munger-hall"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
        }

        // Tests for PUT /api/ucsborganization?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {
                // arrange
                UCSBOrganization muDeltaOrig = UCSBOrganization.builder()
                                .orgCode("MD")
                                .orgTranslationShort("Mu_Delta")
                                .orgTranslation("Mu_Delta")
                                .inactive(true)
                                .build();

                UCSBOrganization muDeltaEdit = UCSBOrganization.builder()
                                .orgCode("md")
                                .orgTranslationShort("Magnum_Dong")
                                .orgTranslation("Magnum_Dong")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(muDeltaEdit);

                when(ucsbOrganizationRepository.findById(eq("MD"))).thenReturn(Optional.of(muDeltaOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=MD")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("MD");
                verify(ucsbOrganizationRepository, times(1)).save(muDeltaEdit); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange
                UCSBOrganization muDeltaEdit = UCSBOrganization.builder()
                                .orgCode("MD")
                                .orgTranslationShort("Magnum_Dong")
                                .orgTranslation("Magnum_Dong")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(muDeltaEdit);

                when(ucsbOrganizationRepository.findById(eq("MD"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=MD")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("MD");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id MD not found", json.get("message"));
        }

        // Tests for DELETE /api/ucsborganization?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange
                UCSBOrganization muDelta = UCSBOrganization.builder()
                                .orgCode("MD")
                                .orgTranslationShort("Mu_Delta")
                                .orgTranslation("Mu_Delta")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("MD"))).thenReturn(Optional.of(muDelta));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=MD")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("MD");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id MD deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {
                // arrange
                when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=munger-hall")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
        }
}