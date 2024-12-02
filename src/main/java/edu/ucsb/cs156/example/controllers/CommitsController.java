package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Commit;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.CommitRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.ZonedDateTime;

/**
 * This is a REST controller for Commits
 */

 @Tag(name = "Commits")
 @RequestMapping("/api/commits")
 @RestController
 @Slf4j
public class CommitsController extends ApiController {
    
    @Autowired
    CommitRepository commitRepository;

     /**
     * List all Commits
     * 
     * @return an iterable of Commits
     */
    @Operation(summary= "List all commits")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Commit> allCommits() {
        Iterable<Commit> commits = commitRepository.findAll();
        return commits;
    }

     /**
     * Create a new commit
     * 
     * @param message  the commit message
     * @param url the commit url 
     * @param authorLogin the github login id of the user that authored the commit
     * @param commitTime the timestamp on the commit, with time zone information
     * @return the saved commit
     */
    @Operation(summary= "Create a new commit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Commit postCommit(
            @Parameter(name="message") @RequestParam String message,
            @Parameter(name="url") @RequestParam String url,
            @Parameter(name="authorLogin") @RequestParam String authorLogin,
            @Parameter(name="commitTime") @RequestParam("commitTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime commitTime)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("commitTime={}", commitTime);

        Commit commit = new Commit();
        commit.setMessage(message);
        commit.setUrl(url);
        commit.setAuthorLogin(authorLogin);
        commit.setCommitTime(commitTime);

        Commit savedCommit = commitRepository.save(commit);

        return savedCommit;
    }

}