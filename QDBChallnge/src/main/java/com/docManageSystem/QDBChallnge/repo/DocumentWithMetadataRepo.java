package com.docManageSystem.QDBChallnge.repo;

import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.docManageSystem.QDBChallnge.entity.DocumentWithMetadata;

@Repository
@Transactional
public interface DocumentWithMetadataRepo extends JpaRepository<DocumentWithMetadata, Long> {
	
	Optional<DocumentWithMetadata> findByUserName(String fileName);
	
	@Query("select dId from DocumentWithMetadata where nameOfDocument=:dName")
	Optional<Long> findDidByNameOfDoc(@Param("dName") String dName);
	
	Optional<DocumentWithMetadata> findByNameOfDocument(String fileName);
	
	@Modifying
	@Query("update DocumentWithMetadata d set d.pdfFile = :pdfFile where d.dId = :dId")
	void updateDocumentBydId(@Param(value = "pdfFile")  byte[] pdfFile, @Param(value = "dId") long dId);
	
	@Query("select userId from DocumentWithMetadata where userName=:userName")
	Optional<String> findUserIdByUserName(@Param("userName") String userName);
	
//	Set<Optional<DocumentWithMetadata>> findAllByUserId(String userId);
	Set<Optional<DocumentWithMetadata>> findAllByUserName(String userName);
}
