package twr.toolbox.dropwizard.tracking;

public class ApiCall {

    public final String client;
    public final String httpMethod;
    public final String path;

    public ApiCall(String client, String httpMethod, String path) {
        this.client = client;
        this.httpMethod = httpMethod;
        this.path = path;
    }

}
