package license.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import license.clients.OrganizationDiscoveryClient;
import license.clients.OrganizationFeignClient;
import license.clients.OrganizationRestTemplateClient;
import license.config.ServiceConfig;
import license.model.License;
import license.model.Organization;
import license.repository.LicenseRepository;
import license.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RefreshScope
public class LicenseService {

  @Autowired private LicenseRepository licenseRepository;

  @Autowired ServiceConfig config;

  @Autowired OrganizationFeignClient organizationFeignClient;

  @Autowired OrganizationRestTemplateClient organizationRestClient;

  @Autowired OrganizationDiscoveryClient organizationDiscoveryClient;

  private Organization retrieveOrgInfo(String organizationId, String clientType) {
    Organization organization = null;
    switch (clientType) {
      case "feign":
        System.out.println("I am using the feign client");
        organization = organizationFeignClient.getOrganization(organizationId);
        break;
      case "rest":
        System.out.println("I am using the rest client");
        organization = organizationRestClient.getOrganization(organizationId);
        break;
      case "discovery":
        System.out.println("I am using the discovery client");
        organization = organizationDiscoveryClient.getOrganization(organizationId);
        break;
      default:
        organization = organizationRestClient.getOrganization(organizationId);
    }
    return organization;
  }

  public License getLicense(String organizationId, String licenseId, String clientType) {
    License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
    Organization org = retrieveOrgInfo(organizationId, clientType);
    return license
        .withOrganizationName(org.getName())
        .withContactName(org.getContactName())
        .withContactEmail(org.getContactEmail())
        .withContactPhone(org.getContactPhone())
        .withComment(config.getExampleProperty());
  }

  @HystrixCommand(
      fallbackMethod = "buildFallbackLicenseList",
      threadPoolKey = "licenseByOrgThreadPool",
      threadPoolProperties = {
        @HystrixProperty(name = "coreSize", value = "30"),
        @HystrixProperty(name = "maxQueueSize", value = "10"),
      },
      commandProperties = {
        @HystrixProperty(
            name = "circuitBreaker.requestVolumeThreshold",
            value =
                "10"), // Controls ammount of calls needed in a 10 second window before considering
        @HystrixProperty(
            name = "circuitBreaker.errorThresholdPercentage",
            value = "75"), // Percentage of failed requests needed to consider breaking
        @HystrixProperty(
            name = "circuitBreaker.sleepWindowInMilliseconds",
            value = "7000"), // Amount of sec the breaker will sleep once the circuit it tripped
        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),
        @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
        @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") // chunks in which rolling stats are to be collected
      })
  public List<License> getLicenseByOrg(String organizationId) {
    log.debug("LicenseService.getLicensesByOrg Correlation id {}", UserContextHolder.getContext().getCorrelationId());
    randomlyRunLong();
    return licenseRepository.findByOrganizationId(organizationId);
  }

  public void saveLicense(License license) {
    license.withId(UUID.randomUUID().toString());
    licenseRepository.save(license);
  }

  public void updateLicense(License license) {
    licenseRepository.save(license);
  }

  public void deleteLicense(License license) {
    licenseRepository.delete(license);
  }

  private List<License> buildFallbackLicenseList(String organizationId) {
    List<License> fallbackList = new ArrayList<>();
    License defaultLicense =
        new License()
            .withId("00000000-000-0000000000")
            .withOrganizationId(organizationId)
            .withProductName("Sorry there is no licensing information available ATM!");
    fallbackList.add(defaultLicense);
    return fallbackList;
  }

  private void randomlyRunLong() {
    Random random = new Random();

    int randomNum = random.nextInt((3 - 1) + 1) + 1;
    if (randomNum == 3) sleep();
  }

  private void sleep() {
    try {
      Thread.sleep(4000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
