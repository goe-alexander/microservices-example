package zuul.filters;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.netflix.zuul.ZuulFilter;

@Slf4j
@Component
public class ResponseFilter extends ZuulFilter{
    private static final int  FILTER_ORDER=1;
    private static final boolean  SHOULD_FILTER=true;

    @Autowired
    FilterUtils filterUtils;

    @Override
    public String filterType() {
        return filterUtils.POST_FILTER_TYPE;
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
        RequestContext zuulContext = RequestContext.getCurrentContext();
        log.info("Adding the correlation id to the outbound headers: {}", filterUtils.getCorrelationId());
        zuulContext.getResponse().addHeader(FilterUtils.CORRELATION_ID, filterUtils.getCorrelationId());
        log.info("Completing outgoing req for: {}", zuulContext.getRequest().getRequestURI());
        return null;
    }
}
