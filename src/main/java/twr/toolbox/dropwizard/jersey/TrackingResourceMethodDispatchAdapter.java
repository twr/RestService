package twr.toolbox.dropwizard.jersey;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import twr.toolbox.dropwizard.tracking.ApiCallsTracker;

import javax.ws.rs.ext.Provider;

@Provider
public class TrackingResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {

    private final ApiCallsTracker apiCallsTracker;

    public TrackingResourceMethodDispatchAdapter(ApiCallsTracker apiCallsTracker) {
        this.apiCallsTracker = apiCallsTracker;
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new TrackingResourceMethodDispatchProvider(provider, apiCallsTracker);
    }

}
