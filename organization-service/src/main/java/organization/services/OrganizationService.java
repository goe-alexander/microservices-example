package organization.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import organization.model.Organization;
import organization.repository.OrganizationRepository;

import java.util.UUID;

@Service
public class OrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;

    public Organization getOrg(String organizationId) {
        return organizationRepository.findById(organizationId).get();
    }

    public void saveOrg(Organization org) {
        org.setId(UUID.randomUUID().toString());
        organizationRepository.save(org);
    }

    public void updateOrg(Organization org) {
        organizationRepository.save(org);
    }

    public void deleteOrg(Organization org){
        organizationRepository.delete(org);
    }
}
