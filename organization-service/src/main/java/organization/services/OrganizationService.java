package organization.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import organization.events.source.SimpleSourceBean;
import organization.model.Organization;
import organization.repository.OrganizationRepository;

import java.util.UUID;

@Service
public class OrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    SimpleSourceBean simpleSourceBean;

    public Organization getOrg(String organizationId) {
        return organizationRepository.findById(organizationId).get();
    }

    public void saveOrg(Organization org) {
        org.setId(UUID.randomUUID().toString());
        organizationRepository.save(org);
        simpleSourceBean.publishChange("SAVE", org.getId());
    }

    public void updateOrg(Organization org) {
        organizationRepository.save(org);
        simpleSourceBean.publishChange("UPDATE", org.getId());
    }

    public void deleteOrg(Organization org){
        organizationRepository.delete(org);
        simpleSourceBean.publishChange("DELETE", org.getId());
    }

    public void deletOrgById(String orgId){
        organizationRepository.deleteById(orgId);
        simpleSourceBean.publishChange("DELETE", orgId);
    }
}
