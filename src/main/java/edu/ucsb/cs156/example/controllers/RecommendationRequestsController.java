package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequests;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestsRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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
 * This is a REST controller for RecommendationRequests
 */
@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestsController extends ApiController {

    @Autowired
    RecommendationRequestsRepository recommendationRequestsRepository;

    @Operation(summary = "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequests> getAllRequests() {
        return recommendationRequestsRepository.findAll();
    }

    @Operation(summary = "Get a single recommendation request by ID")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequests getRequestById(
            @Parameter(name = "id") @RequestParam long id) {
        return recommendationRequestsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));
    }

    @Operation(summary = "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/post")
    public RecommendationRequests createRequest(
            @RequestParam String requesterEmail,
            @RequestParam String professorEmail,
            @RequestParam String explanation,
            @RequestParam LocalDateTime dateRequested,
            @RequestParam LocalDateTime dateNeeded,
            @RequestParam String name,
            @RequestParam boolean done) {

        RecommendationRequests request = RecommendationRequests.builder()
                .requesterEmail(requesterEmail)
                .professorEmail(professorEmail)
                .explanation(explanation)
                .dateRequested(dateRequested)
                .dateNeeded(dateNeeded)
                .name(name)
                .done(done)
                .build();

        return recommendationRequestsRepository.save(request);
    }

    @Operation(summary = "Delete a recommendation request by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRequest(
            @Parameter(name = "id") @RequestParam long id) {
        RecommendationRequests request = recommendationRequestsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));

        recommendationRequestsRepository.delete(request);
        return genericMessage("Recommendation request with ID %s deleted".formatted(id));
    }

    @Operation(summary = "Update an existing recommendation request by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public RecommendationRequests updateRequest(
            @Parameter(name = "id") @RequestParam long id,
            @RequestBody @Valid RecommendationRequests incomingRequest) {

        RecommendationRequests request = recommendationRequestsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequests.class, id));

        request.setRequesterEmail(incomingRequest.getRequesterEmail());
        request.setProfessorEmail(incomingRequest.getProfessorEmail());
        request.setExplanation(incomingRequest.getExplanation());
        request.setDateRequested(incomingRequest.getDateRequested());
        request.setDateNeeded(incomingRequest.getDateNeeded());
        request.setName(incomingRequest.getName());
        request.setDone(incomingRequest.isDone());

        return recommendationRequestsRepository.save(request);
    }
}
