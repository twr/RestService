package twr.toolbox.dropwizard.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twr.toolbox.dropwizard.jersey.TrackingResourceMethodDispatchProvider;

import java.util.Date;

public class LoggingApiCallsTracker implements ApiCallsTracker {

    private static final Logger logger = LoggerFactory.getLogger(TrackingResourceMethodDispatchProvider.class);

    @Override
    public void track(ApiCall apiCall) {
        logger.info("{} called {} {} on {}", apiCall.client, apiCall.httpMethod, apiCall.path, new Date());
    }

}
