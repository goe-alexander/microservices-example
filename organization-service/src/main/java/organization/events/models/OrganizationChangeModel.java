package organization.events.models;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationChangeModel{
    private String type;
    private String action;
    private String organizationId;
    private String correlationId;
}