package twr.toolbox.dropwizard.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import twr.toolbox.dropwizard.tracking.ApiCall;
import twr.toolbox.dropwizard.tracking.ApiCallsTracker;

public class TrackingResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {

    private final ResourceMethodDispatchProvider provider;
    private final ApiCallsTracker apiCallsTracker;

    public TrackingResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider, ApiCallsTracker apiCallsTracker) {
        this.provider = provider;
        this.apiCallsTracker = apiCallsTracker;
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod method) {
        RequestDispatcher dispatcher = provider.create(method);
        if (dispatcher == null) {
            return null;
        }

        return new TrackingRequestDispatcher(dispatcher, getPath(method), apiCallsTracker);
    }

    private static class TrackingRequestDispatcher implements RequestDispatcher {

        private final RequestDispatcher underlying;
        private final String api;
        private final ApiCallsTracker apiCallsTracker;

        private TrackingRequestDispatcher(RequestDispatcher underlying, String api, ApiCallsTracker apiCallsTracker) {
            this.underlying = underlying;
            this.api = api;
            this.apiCallsTracker = apiCallsTracker;
        }

        @Override
        public void dispatch(Object resource, HttpContext httpContext) {
            Optional<String> client = Optional.fromNullable(httpContext.getUriInfo().getQueryParameters().getFirst("client"));
            apiCallsTracker.track(new ApiCall(client.or("undefined"), httpContext.getRequest().getMethod(), api));

            underlying.dispatch(resource, httpContext);
        }

    }

    private String getPath(AbstractResourceMethod method) {
        StringBuilder result = new StringBuilder(method.getResource().getPath().getValue());
        if (method instanceof AbstractSubResourceMethod) {
            result.append(((AbstractSubResourceMethod) method).getPath().getValue());
        }
        return result.toString();
    }

}
