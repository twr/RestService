package twr.toolbox.dropwizard.tracking;

public class IgnoringApiCallsTracker implements ApiCallsTracker {

    @Override
    public void track(ApiCall apiCall) {
        // ignores
    }

}
