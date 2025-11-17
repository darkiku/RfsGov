package kz.gov.rfs.service;

import kz.gov.rfs.entity.Procurement;
import kz.gov.rfs.entity.ProcurementType;
import kz.gov.rfs.repository.ProcurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcurementService {
    private final ProcurementRepository procurementRepository;

    public Page<Procurement> getAllActiveProcurements(Pageable pageable) {
        return procurementRepository.findByIsActiveTrueOrderByPublishDateDesc(pageable);
    }

    public List<Procurement> getProcurementsByYear(Integer year) {
        return procurementRepository.findByYearAndIsActiveTrueOrderByPublishDateDesc(year);
    }

    public List<Procurement> getProcurementsByType(ProcurementType type) {
        return procurementRepository.findByProcurementTypeAndIsActiveTrueOrderByPublishDateDesc(type);
    }

    public List<Procurement> getProcurementsByYearAndType(Integer year, ProcurementType type) {
        return procurementRepository.findByYearAndProcurementTypeAndIsActiveTrue(year, type);
    }

    public Procurement getProcurementById(Long id) {
        return procurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Procurement not found with id: " + id));
    }

    @Transactional
    public Procurement createProcurement(Procurement procurement) {
        procurement.setIsActive(true);
        return procurementRepository.save(procurement);
    }

    @Transactional
    public Procurement updateProcurement(Long id, Procurement procurementDetails) {
        Procurement procurement = getProcurementById(id);
        procurement.setTitleRu(procurementDetails.getTitleRu());
        procurement.setTitleKk(procurementDetails.getTitleKk());
        procurement.setTitleEn(procurementDetails.getTitleEn());
        procurement.setDescriptionRu(procurementDetails.getDescriptionRu());
        procurement.setDescriptionKk(procurementDetails.getDescriptionKk());
        procurement.setDescriptionEn(procurementDetails.getDescriptionEn());
        procurement.setYear(procurementDetails.getYear());
        procurement.setPublishDate(procurementDetails.getPublishDate());
        procurement.setDeadline(procurementDetails.getDeadline());
        procurement.setDocumentUrl(procurementDetails.getDocumentUrl());
        procurement.setProcurementType(procurementDetails.getProcurementType());
        return procurementRepository.save(procurement);
    }

    @Transactional
    public void deleteProcurement(Long id) {
        Procurement procurement = getProcurementById(id);
        procurement.setIsActive(false);
        procurementRepository.save(procurement);
    }

    public Long getProcurementCount() {
        return procurementRepository.countByIsActive(true);
    }
}