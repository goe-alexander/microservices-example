package license.events.handlers;

import license.events.CustomChannels;
import license.events.models.OrganizationChangeModel;
import license.repository.OrganizationRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@EnableBinding(CustomChannels.class)
@Slf4j
public class OrganizationChangeHandler {

    @Autowired
    private OrganizationRedisRepository organizationRedisRepository;

    @StreamListener("inboundOrgChanges")
    public void loggerSink(OrganizationChangeModel orgChange){
            log.info("Received a message of type " + orgChange.getType());
            switch(orgChange.getAction().toUpperCase()){
                case "GET":
                    log.info("Received a GET event from the organization service for organization id {}", orgChange.getOrganizationId());
                    break;
                case "SAVE":
                    log.info("Received a SAVE event from the organization service for organization id {}", orgChange.getOrganizationId());
                    break;
                case "UPDATE":
                    log.info("Received a UPDATE event from the organization service for organization id {}", orgChange.getOrganizationId());
                    organizationRedisRepository.deleteOrganization(orgChange.getOrganizationId());
                    break;
                case "DELETE":
                    log.info("Received a DELETE event from the organization service for organization id {}", orgChange.getOrganizationId());
                    organizationRedisRepository.deleteOrganization(orgChange.getOrganizationId());
                    break;
                default:
                    log.info("Received an UNKNOWN event from the organization service of type {}", orgChange.getType());
                    break;

            }
    }
}
