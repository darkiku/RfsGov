package kz.gov.rfs.service;

import kz.gov.rfs.entity.Contact;
import kz.gov.rfs.entity.ContactType;
import kz.gov.rfs.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    public List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByDisplayOrder();
    }

    public List<Contact> getContactsByType(ContactType type) {
        return contactRepository.findByContactTypeOrderByDisplayOrder(type);
    }

    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
    }

    @Transactional
    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    @Transactional
    public Contact updateContact(Long id, Contact contactDetails) {
        Contact contact = getContactById(id);
        contact.setLabelRu(contactDetails.getLabelRu());
        contact.setLabelKk(contactDetails.getLabelKk());
        contact.setLabelEn(contactDetails.getLabelEn());
        contact.setValue(contactDetails.getValue());
        contact.setContactType(contactDetails.getContactType());
        contact.setDisplayOrder(contactDetails.getDisplayOrder());
        return contactRepository.save(contact);
    }

    @Transactional
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}