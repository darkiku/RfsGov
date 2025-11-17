package kz.gov.rfs.repository;

import kz.gov.rfs.entity.About;
import kz.gov.rfs.entity.AboutSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AboutRepository extends JpaRepository<About, Long> {
    List<About> findBySectionOrderByDisplayOrder(AboutSection section);

    Optional<About> findBySectionKey(String sectionKey);
}