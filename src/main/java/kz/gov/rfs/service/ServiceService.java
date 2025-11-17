package kz.gov.rfs.service;

import kz.gov.rfs.entity.Service;
import kz.gov.rfs.entity.ServiceType;
import kz.gov.rfs.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public List<Service> getAllActiveServices() {
        return serviceRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    public List<Service> getServicesByType(ServiceType type) {
        return serviceRepository.findByServiceTypeAndIsActiveTrue(type);
    }

    public Service getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
    }

    @Transactional
    public Service createService(Service service) {
        service.setIsActive(true);
        return serviceRepository.save(service);
    }

    @Transactional
    public Service updateService(Long id, Service serviceDetails) {
        Service service = getServiceById(id);
        service.setTitleRu(serviceDetails.getTitleRu());
        service.setTitleKk(serviceDetails.getTitleKk());
        service.setTitleEn(serviceDetails.getTitleEn());
        service.setDescriptionRu(serviceDetails.getDescriptionRu());
        service.setDescriptionKk(serviceDetails.getDescriptionKk());
        service.setDescriptionEn(serviceDetails.getDescriptionEn());
        service.setIconUrl(serviceDetails.getIconUrl());
        service.setLink(serviceDetails.getLink());
        service.setServiceType(serviceDetails.getServiceType());
        service.setDisplayOrder(serviceDetails.getDisplayOrder());
        return serviceRepository.save(service);
    }

    @Transactional
    public void deleteService(Long id) {
        Service service = getServiceById(id);
        service.setIsActive(false);
        serviceRepository.save(service);
    }

    public Long getServiceCount() {
        return serviceRepository.countByIsActive(true);
    }
}