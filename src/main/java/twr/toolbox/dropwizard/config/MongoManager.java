package twr.toolbox.dropwizard.config;

import com.mongodb.MongoClient;
import com.yammer.dropwizard.lifecycle.Managed;

public class MongoManager implements Managed {

    private final MongoClient client;

    public MongoManager(MongoClient client) {
        this.client = client;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        client.close();
    }

}