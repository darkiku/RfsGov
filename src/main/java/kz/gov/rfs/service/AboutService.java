package kz.gov.rfs.service;

import kz.gov.rfs.entity.About;
import kz.gov.rfs.entity.AboutSection;
import kz.gov.rfs.repository.AboutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AboutService {
    private final AboutRepository aboutRepository;

    public List<About> getAboutBySection(AboutSection section) {
        return aboutRepository.findBySectionOrderByDisplayOrder(section);
    }

    public About getAboutBySectionKey(String sectionKey) {
        return aboutRepository.findBySectionKey(sectionKey)
                .orElseThrow(() -> new RuntimeException("About section not found with key: " + sectionKey));
    }

    public About getAboutById(Long id) {
        return aboutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("About not found with id: " + id));
    }

    @Transactional
    public About createAbout(About about) {
        return aboutRepository.save(about);
    }

    @Transactional
    public About updateAbout(Long id, About aboutDetails) {
        About about = getAboutById(id);
        about.setTitleRu(aboutDetails.getTitleRu());
        about.setTitleKk(aboutDetails.getTitleKk());
        about.setTitleEn(aboutDetails.getTitleEn());
        about.setContentRu(aboutDetails.getContentRu());
        about.setContentKk(aboutDetails.getContentKk());
        about.setContentEn(aboutDetails.getContentEn());
        about.setSection(aboutDetails.getSection());
        about.setDisplayOrder(aboutDetails.getDisplayOrder());
        return aboutRepository.save(about);
    }

    @Transactional
    public void deleteAbout(Long id) {
        aboutRepository.deleteById(id);
    }
}