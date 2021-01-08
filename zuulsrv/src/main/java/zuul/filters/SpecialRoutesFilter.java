package zuul.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.bouncycastle.cert.ocsp.Req;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import zuul.model.AbTestingRoute;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

//@Component
@Slf4j
public class SpecialRoutesFilter extends ZuulFilter {

  private static final int FILTER_ORDER = 1;
  private static final boolean SHOULD_FILTER = true;

  @Autowired FilterUtils filterUtils;

  @Autowired RestTemplate restTemplate;

  @Autowired private ProxyRequestHelper helper;

  @Override
  public String filterType() {
    return filterUtils.ROUTE_FILTER_TYPE;
  }

  @Override
  public int filterOrder() {
    return FILTER_ORDER;
  }

  @Override
  public boolean shouldFilter() {
    return SHOULD_FILTER;
  }

  @Override
  public Object run() throws ZuulException {
      RequestContext ctx = RequestContext.getCurrentContext();
      AbTestingRoute abTestingRoute = getAbRoutingInfo(filterUtils.getServiceId());

      if(abTestingRoute != null && useSpecialRoute(abTestingRoute)){
          String route = buildRouteString(ctx.getRequest().getRequestURI(), abTestingRoute.getEndpoint(), ctx.get("serviceId").toString());
          forwardToSpecialRoute(route);
      }
    return null;
  }

  private AbTestingRoute getAbRoutingInfo(String serviceName) {
    ResponseEntity<AbTestingRoute> restExchange = null;
    try {
      restExchange =
          restTemplate.exchange(
              "http://specialroutesservice/v1/route/abtesting/{serviceName}",
              HttpMethod.GET,
              null,
              AbTestingRoute.class,
              serviceName);
    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return null;
      throw ex;
    }
    return restExchange.getBody();
  }

  private String buildRouteString(String oldEndpoint, String newEndpoint, String serviceName) {
    int index = oldEndpoint.indexOf(serviceName);
    String strippedRoute = oldEndpoint.substring(index + serviceName.length());
    log.info("TargetRoute: {} , {}", newEndpoint, strippedRoute);
    return String.format("%s/%s", newEndpoint, strippedRoute);
  }

  private String getVerb(HttpServletRequest request) {
    return request.getMethod().toUpperCase();
  }

  private HttpHost getHttpHost(URL host) {
    HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(), host.getProtocol());
    return httpHost;
  }

  private Header[] convertHeaders(MultiValueMap<String, String> headers) {
    List<Header> list = new ArrayList<>();
    for (String name : headers.keySet()) {
      for (String value : headers.get(name)) {
        list.add(new BasicHeader(name, value));
      }
    }
    return list.toArray(new BasicHeader[0]);
  }

  private HttpResponse forwardRequest(
      HttpClient httpClient, HttpHost httpHost, HttpRequest httpRequest) throws IOException {
    return httpClient.execute(httpHost, httpRequest);
  }

  private MultiValueMap<String, String> revertHeaders(Header[] headers) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
    Arrays.stream(headers)
        .forEach(
            header -> {
              if (!map.containsKey(header.getName())) {
                map.put(header.getName(), new ArrayList<>());
              }
              map.get(header.getName()).add(header.getValue());
            });

    return map;
  }

  private InputStream getRequestBody(HttpServletRequest request) {
    InputStream requestEntity = null;
    try {
      requestEntity = request.getInputStream();
    } catch (IOException ex) {
      // no reqBody is ok
    }
    return requestEntity;
  }

  private void setResponse(HttpResponse response) throws IOException {
    this.helper.setResponse(
        response.getStatusLine().getStatusCode(),
        response.getEntity() == null ? null : response.getEntity().getContent(),
        revertHeaders(response.getAllHeaders()));
  }

  private HttpResponse forward(
      HttpClient httpClient,
      String verb,
      String uri,
      HttpServletRequest request,
      MultiValueMap<String, String> headers,
      MultiValueMap<String, String> params,
      InputStream requestEntity)
      throws Exception {

    Map<String, Object> info = this.helper.debug(verb, uri, headers, params, requestEntity);
    URL host = new URL(uri);
    HttpHost httpHost = getHttpHost(host);

    HttpRequest httpRequest;
    int contentLength = request.getContentLength();
    InputStreamEntity entity =
        new InputStreamEntity(
            requestEntity,
            contentLength,
            request.getContentType() != null ? ContentType.create(request.getContentType()) : null);

    switch (verb.toUpperCase()) {
      case "POST":
        HttpPost httpPost = new HttpPost(uri);
        httpRequest = httpPost;
        httpPost.setEntity(entity);
        break;
      case "PUT":
        HttpPut httpPut = new HttpPut(uri);
        httpRequest = httpPut;
        httpPut.setEntity(entity);
        break;
      case "PATCH":
        HttpPatch httpPpatch = new HttpPatch(uri);
        httpRequest = httpPpatch;
        httpPpatch.setEntity(entity);
        break;
      default:
        httpRequest = new BasicHttpRequest(verb, uri);
    }
    try {
      httpRequest.setHeaders(convertHeaders(headers));
      HttpResponse zuulResponse = forwardRequest(httpClient, httpHost, httpRequest);

      return zuulResponse;
    } finally {
    }
  }

  public boolean useSpecialRoute(AbTestingRoute testingRoute) {
    Random random = new Random();
    if (testingRoute.getActive().equals("N")) return false;
    int value = random.nextInt((10 - 1) + 1) + 1;
    if (testingRoute.getWeight() < value) return true;
    return false;
  }

  private void forwardToSpecialRoute(String route) {
    RequestContext context = RequestContext.getCurrentContext();
    HttpServletRequest request = context.getRequest();

    MultiValueMap<String, String> headers = this.helper.buildZuulRequestHeaders(request);
    MultiValueMap<String, String> params = this.helper.buildZuulRequestQueryParams(request);

    String verb = getVerb(request);
    InputStream requestEntity = getRequestBody(request);

    if (request.getContentLength() < 0) {
      context.setChunkedRequestBody();
    }

    this.helper.addIgnoredHeaders();
    CloseableHttpClient httpClient = null;
    HttpResponse response = null;

    try {
      httpClient = HttpClients.createDefault();
      response = forward(httpClient, verb, route, request, headers, params, requestEntity);
      setResponse(response);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
      }
    }
  }
}
