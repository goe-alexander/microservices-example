package organization.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import organization.model.Organization;
import organization.services.OrganizationService;

@Slf4j
@RestController
@RequestMapping(value = "v1/organizations/")
public class OrganizationServiceController {

    @Autowired
    private OrganizationService orgService;

    @RequestMapping(value="/{organizationId}",method = RequestMethod.GET)
    public Organization getOrganization(@PathVariable("organizationId") String organizationId) {
        log.info("######### Organization called:");
        return orgService.getOrg(organizationId);
    }

    @RequestMapping(value="/{organizationId}",method = RequestMethod.PUT)
    public void updateOrganization( @PathVariable("organizationId") String orgId, @RequestBody Organization org) {
        orgService.updateOrg( org );
    }

    @RequestMapping(value="/{organizationId}",method = RequestMethod.POST)
    public void saveOrganization(@RequestBody Organization org) {
        orgService.saveOrg( org );
    }

//    @RequestMapping(value="/{orgId}",method = RequestMethod.DELETE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deleteOrganization( @PathVariable("orgId") String orgId,  @RequestBody Organization org) {
//        orgService.deleteOrg( org );
//    }
    @RequestMapping(value="/{organizationId}",method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganizationByID( @PathVariable("organizationId") String organizationId) {
        orgService.deletOrgById( organizationId );
    }
}
