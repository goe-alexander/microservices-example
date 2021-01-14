package license.clients;

import license.model.Organization;
import license.repository.OrganizationRedisRepository;
import license.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
public class OrganizationRestTemplateClient {
    @Autowired
    RestTemplate oAuth2RestTemplate;

    @Autowired
    OrganizationRedisRepository orgRedisRepo;
//
    private Optional<Organization> checkRedisCache(String organizationId){
        try{
            return Optional.ofNullable(orgRedisRepo.findOrganization(organizationId));
        }catch (Exception ex) {
            log.error("Error while retrieving organization {} check Redis Cache. Exception: {}", organizationId, ex);
            return Optional.empty();
        }
    }

    private void cacheOrganizationObject(Organization org){
        try{
            orgRedisRepo.saveOrganization(org);
        }catch (Exception ex) {
            log.error("Unable to cache organization {} check Redis Cache. Exception: {}", org.getId(), ex);
        }
    }

    public Organization getOrganization(String organizationId) {
        log.info("In licengin service correlation Id: {}", UserContext.getCorrelationId() );
        return checkRedisCache(organizationId).orElseGet(() -> getOrganizationFromService(organizationId));
    }

    private Organization getOrganizationFromService(String organizationId){
        log.info("Unable to locate organization in redis cache: {} , corelation id: {}", organizationId, UserContext.getCorrelationId());
        ResponseEntity<Organization> restExchange =
                oAuth2RestTemplate.exchange(
                        "http://zuulserver:5555/api/organization/v1/organizations/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        Organization org =  restExchange.getBody();
        if(org != null){
            cacheOrganizationObject(org);
        }
        return org;
    }
}