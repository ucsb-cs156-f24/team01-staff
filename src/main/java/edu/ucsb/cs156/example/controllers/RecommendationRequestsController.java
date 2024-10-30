package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * REST controller for managing recommendation requests.
 * Provides endpoints to create, read, update, and delete recommendation requests.
 */
@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestsController extends ApiController {

    @Autowired
    RecommendationRequestRepository recommendationRequestRepository;

    /**
     * List all recommendation requests.
     * 
     * @return all recommendation requests in the system.
     */
    @Operation(summary = "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequest> allRecommendationRequests() {
        Iterable<RecommendationRequest> recommendationRequests = recommendationRequestRepository.findAll();
        return recommendationRequests;
    }

    /**
     * Retrieve a single recommendation request by ID.
     * 
     * @param id the ID of the recommendation request to retrieve.
     * @return the recommendation request with the specified ID.
     * @throws EntityNotFoundException if no recommendation request with the given ID is found.
     */
    @Operation(summary = "Get a single recommendation request by ID")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequest getById(
            @Parameter(name = "id") @RequestParam Long id) {
        RecommendationRequest recommendationRequest = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));
        return recommendationRequest;
    }

    /**
     * Create a new recommendation request.
     * 
     * @param requesterEmail the email of the requester.
     * @param professorEmail the email of the professor.
     * @param explanation a brief explanation for the recommendation request.
     * @param dateRequested the date when the recommendation was requested.
     * @param dateNeeded the date by which the recommendation is needed.
     * @param done whether the recommendation request is completed.
     * @return the created recommendation request.
     * @throws JsonProcessingException if there is an error processing JSON input.
     */
    @Operation(summary = "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequest postRecommendationRequest(
            @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name = "professorEmail") @RequestParam String professorEmail,
            @Parameter(name = "explanation") @RequestParam String explanation,
            @Parameter(name = "dateRequested", description = "Date in ISO format (e.g., YYYY-MM-DDTHH:MM:SS)") 
            @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
            @Parameter(name = "dateNeeded", description = "Date in ISO format (e.g., YYYY-MM-DDTHH:MM:SS)") 
            @RequestParam("dateNeeded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded,
            @Parameter(name = "done") @RequestParam boolean done)
            throws JsonProcessingException {

        log.info("dateRequested={}", dateRequested);
        log.info("dateNeeded={}", dateNeeded);

        RecommendationRequest recommendationRequest = new RecommendationRequest();
        recommendationRequest.setRequesterEmail(requesterEmail);
        recommendationRequest.setProfessorEmail(professorEmail);
        recommendationRequest.setExplanation(explanation);
        recommendationRequest.setDateNeeded(dateNeeded);
        recommendationRequest.setDateRequested(dateRequested);
        recommendationRequest.setDone(done);

        return recommendationRequestRepository.save(recommendationRequest);
    }

    /**
     * Update an existing recommendation request by ID.
     * 
     * @param id the ID of the recommendation request to update.
     * @param incoming the updated recommendation request details.
     * @return the updated recommendation request.
     * @throws EntityNotFoundException if no recommendation request with the given ID is found.
     */
    @Operation(summary = "Update a single recommendation request by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public RecommendationRequest updateRecommendationRequest(
            @Parameter(name = "id") @RequestParam Long id,
            @RequestBody @Valid RecommendationRequest incoming) {

        RecommendationRequest recommendationRequest = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        recommendationRequest.setRequesterEmail(incoming.getRequesterEmail());
        recommendationRequest.setProfessorEmail(incoming.getProfessorEmail());
        recommendationRequest.setExplanation(incoming.getExplanation());
        recommendationRequest.setDateNeeded(incoming.getDateNeeded());
        recommendationRequest.setDateRequested(incoming.getDateRequested());
        recommendationRequest.setDone(incoming.getDone());

        return recommendationRequestRepository.save(recommendationRequest);
    }

    /**
     * Delete a recommendation request by ID.
     * 
     * @param id the ID of the recommendation request to delete.
     * @return a message indicating that the recommendation request was deleted.
     * @throws EntityNotFoundException if no recommendation request with the given ID is found.
     */
    @Operation(summary = "Delete a recommendation request by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRecommendationRequest(
            @Parameter(name = "id") @RequestParam Long id) {
        RecommendationRequest recRequest = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        recommendationRequestRepository.delete(recRequest);
        return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
    }
}
