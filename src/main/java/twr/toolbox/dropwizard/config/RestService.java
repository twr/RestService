package twr.toolbox.dropwizard.config;

import com.mongodb.MongoClient;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import twr.toolbox.dropwizard.jersey.TrackingResourceMethodDispatchAdapter;
import twr.toolbox.dropwizard.resources.ClockResource;
import twr.toolbox.dropwizard.servlets.HtmlMetricsServlet;
import twr.toolbox.dropwizard.tracking.ApiCallsTracker;
import twr.toolbox.dropwizard.tracking.IgnoringApiCallsTracker;
import twr.toolbox.dropwizard.tracking.LoggingApiCallsTracker;
import twr.toolbox.dropwizard.tracking.MongoApiCallsTracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class RestService extends Service<RestServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new RestService().run(args);
    }

    @Override
    public void initialize(Bootstrap<RestServiceConfiguration> bootstrap) {
        bootstrap.setName("rest-service");
        bootstrap.addBundle(new AssetsBundle("/assets/", "/assets/"));
    }

    @Override
    public void run(RestServiceConfiguration configuration, Environment environment) throws Exception {
        environment.addResource(new ClockResource());
        environment.addServlet(HtmlMetricsServlet.class, "/stats");

        MongoClient mongoClient = new MongoClient(configuration.getMongo().getHost(), configuration.getMongo().getPort());
        environment.manage(new MongoManager(mongoClient));

        // ApiCallsTracker tracker = new IgnoringApiCallsTracker();
        // ApiCallsTracker tracker = new LoggingApiCallsTracker();
        ExecutorService executorService = environment.managedExecutorService("thread-%d", 5, 10, 500, TimeUnit.MILLISECONDS);
        ApiCallsTracker tracker = new MongoApiCallsTracker(mongoClient, executorService);
        TrackingResourceMethodDispatchAdapter adapter = new TrackingResourceMethodDispatchAdapter(tracker);
        environment.addProvider(adapter);

    }

}
