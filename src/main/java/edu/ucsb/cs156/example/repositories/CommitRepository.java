package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Commit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The CommitRepository is a repository for Commit entities.
 */

@Repository
public interface CommitRepository extends CrudRepository<Commit, Long> {
  
}