package com.githubtimemachine.repository;

import com.githubtimemachine.entity.ContributorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributorRepository extends JpaRepository<ContributorEntity, Long> {

    Optional<ContributorEntity> findByRepositoryIdAndEmail(Long repositoryId, String email);

    @Query("select c from ContributorEntity c where c.repository.id = :repositoryId order by c.commitCount desc, c.name asc")
    List<ContributorEntity> findByRepositoryIdOrderByCommitCountDesc(@Param("repositoryId") Long repositoryId);

    List<ContributorEntity> findByRepositoryId(Long repositoryId);
}
