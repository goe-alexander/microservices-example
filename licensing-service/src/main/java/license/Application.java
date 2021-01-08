package license;

import license.config.ServiceConfig;
import license.utils.UserContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
@RefreshScope
@EnableFeignClients
@EnableDiscoveryClient
@EnableEurekaClient
@EnableCircuitBreaker
@EnableResourceServer
public class Application {
  @Autowired
  private ServiceConfig serviceConfig;

  @Primary
  @Bean
  public RestTemplate getCustomRestTemplate() {
    RestTemplate template = new RestTemplate();
    List interceptors = template.getInterceptors();
    if (interceptors == null) {
      template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
    } else {
      interceptors.add(new UserContextInterceptor());
      template.setInterceptors(interceptors);
    }

    return template;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }
}
