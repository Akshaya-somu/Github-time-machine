package com.githubtimemachine.repository;

import com.githubtimemachine.entity.FileChangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileChangeRepository extends JpaRepository<FileChangeEntity, Long> {

    @Query("select f from FileChangeEntity f where f.repository.id = :repositoryId order by f.filePath asc, f.id asc")
    List<FileChangeEntity> findByRepositoryId(@Param("repositoryId") Long repositoryId);

    @Query("select f from FileChangeEntity f where f.repository.id = :repositoryId and f.filePath = :filePath order by f.commit.commitTimestamp desc, f.id desc")
    List<FileChangeEntity> findByRepositoryIdAndFilePathOrderByCommitTimestamp(@Param("repositoryId") Long repositoryId, @Param("filePath") String filePath);

    @Query("select f from FileChangeEntity f where f.commit.repository.id = :repositoryId and f.filePath = :filePath order by f.commit.commitTimestamp desc, f.id desc")
    List<FileChangeEntity> findByRepositoryIdAndFilePath(@Param("repositoryId") Long repositoryId, @Param("filePath") String filePath);

    @Query("select f from FileChangeEntity f where f.repository.id = :repositoryId and f.commit.id = :commitId and f.filePath = :filePath")
    java.util.Optional<FileChangeEntity> findByRepositoryIdAndCommitIdAndFilePath(@Param("repositoryId") Long repositoryId, @Param("commitId") Long commitId, @Param("filePath") String filePath);
}
