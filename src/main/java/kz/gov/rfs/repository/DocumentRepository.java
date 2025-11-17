package kz.gov.rfs.repository;

import kz.gov.rfs.entity.Document;
import kz.gov.rfs.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findByIsActiveTrueOrderByUploadDateDesc(Pageable pageable);

    List<Document> findByDocumentTypeAndIsActiveTrueOrderByUploadDateDesc(DocumentType type);
}