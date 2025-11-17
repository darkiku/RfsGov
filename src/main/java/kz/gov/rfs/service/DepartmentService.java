package kz.gov.rfs.service;

import kz.gov.rfs.entity.Department;
import kz.gov.rfs.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAllByOrderByDisplayOrder();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
    }

    @Transactional
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Transactional
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department department = getDepartmentById(id);
        department.setNameRu(departmentDetails.getNameRu());
        department.setNameKk(departmentDetails.getNameKk());
        department.setNameEn(departmentDetails.getNameEn());
        department.setDescriptionRu(departmentDetails.getDescriptionRu());
        department.setDescriptionKk(departmentDetails.getDescriptionKk());
        department.setDescriptionEn(departmentDetails.getDescriptionEn());
        department.setDisplayOrder(departmentDetails.getDisplayOrder());
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}