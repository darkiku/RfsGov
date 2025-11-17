package kz.gov.rfs.repository;

import kz.gov.rfs.entity.Procurement;
import kz.gov.rfs.entity.ProcurementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcurementRepository extends JpaRepository<Procurement, Long> {
    Page<Procurement> findByIsActiveTrueOrderByPublishDateDesc(Pageable pageable);

    List<Procurement> findByYearAndIsActiveTrueOrderByPublishDateDesc(Integer year);

    List<Procurement> findByProcurementTypeAndIsActiveTrueOrderByPublishDateDesc(ProcurementType type);

    List<Procurement> findByYearAndProcurementTypeAndIsActiveTrue(Integer year, ProcurementType type);

    Long countByIsActive(Boolean isActive);
}