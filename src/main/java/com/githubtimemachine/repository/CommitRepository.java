package com.githubtimemachine.repository;

import com.githubtimemachine.entity.CommitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommitRepository extends JpaRepository<CommitEntity, Long> {

    @Query("select c from CommitEntity c where c.repository.id = :repositoryId order by c.commitTimestamp desc, c.id desc")
    Page<CommitEntity> findByRepositoryIdOrderByCommitTimestamp(@Param("repositoryId") Long repositoryId, Pageable pageable);

    @Query("select c from CommitEntity c where c.repository.id = :repositoryId order by c.commitTimestamp desc, c.id desc")
    List<CommitEntity> findByRepositoryIdOrderByCommitTimestampDesc(@Param("repositoryId") Long repositoryId);

    Optional<CommitEntity> findByRepositoryIdAndHash(Long repositoryId, String hash);

    Long countByRepositoryId(Long repositoryId);
}
