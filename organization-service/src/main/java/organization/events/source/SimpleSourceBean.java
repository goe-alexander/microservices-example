package organization.events.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import organization.events.models.OrganizationChangeModel;
import organization.utils.UserContext;

@Component
@Slf4j
public class SimpleSourceBean {
  private Source source;

  @Autowired
  public SimpleSourceBean(Source source) {
    this.source = source;
  }

  public void publishChange(String action, String orgId) {
    log.info("Sending Kafka Message {} for OrID: {}", action, orgId);
    OrganizationChangeModel changeModel =
        OrganizationChangeModel.builder()
            .type(OrganizationChangeModel.class.getTypeName())
            .action(action)
            .organizationId(orgId)
            .correlationId(UserContext.getCorrelationId())
            .build();

    source
            .output()
            .send(MessageBuilder.withPayload(changeModel).build());
  }
}
