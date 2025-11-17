package kz.gov.rfs.repository;

import kz.gov.rfs.entity.Service;
import kz.gov.rfs.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByIsActiveTrueOrderByDisplayOrder();

    List<Service> findByServiceTypeAndIsActiveTrue(ServiceType serviceType);

    Long countByIsActive(Boolean isActive);
}