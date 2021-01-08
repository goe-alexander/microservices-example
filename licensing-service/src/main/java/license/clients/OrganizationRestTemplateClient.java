package license.clients;

import license.model.Organization;
import license.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class OrganizationRestTemplateClient {
    @Autowired
    RestTemplate oAuth2RestTemplate;

    public Organization getOrganization(String organizationId){
        log.info("In licensing service, corelationID: {}", UserContext.getCorrelationId());
        ResponseEntity<Organization> restExchange =
                oAuth2RestTemplate.exchange(
                        "http://zuulserver:5555/api/organization/v1/organizations/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        return restExchange.getBody();
    }
}
