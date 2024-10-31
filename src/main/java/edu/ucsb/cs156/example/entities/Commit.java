package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * This is a JPA entity that represents a Github Commit.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "commits")
public class Commit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String message;
  private String url;
  private String authorLogin;
  ZonedDateTime commitTime;
}