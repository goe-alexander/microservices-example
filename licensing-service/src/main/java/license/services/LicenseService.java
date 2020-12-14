package license.services;

import license.config.ServiceConfig;
import license.model.License;
import license.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RefreshScope
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    ServiceConfig config;

    public License getLicense(String organizationId, String licenseId){
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        return license.withComment(config.getExampleProperty());
    }
    public List<License> getLicenseByOrg(String organizationId){
        return licenseRepository.findByOrganizationId(organizationId);
    }

    public void saveLicense(License license){
        license.withLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);
    }
    public void updateLicense(License license){
        licenseRepository.save(license);
    }
    public void deleteLicense(License license){
        licenseRepository.delete(license);
    }
}
