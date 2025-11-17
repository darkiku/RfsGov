package kz.gov.rfs.repository;

import kz.gov.rfs.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartmentIdOrderByDisplayOrder(Long departmentId);
}