package twr.toolbox.dropwizard.tracking;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

import java.util.Date;
import java.util.concurrent.ExecutorService;

public class MongoApiCallsTracker implements ApiCallsTracker {

    private static final String DB_NAME = "restservice";
    private static final String API_CALLS_COLLECTION_NAME = "apicalls";

    private final MongoClient mongoClient;
    private ExecutorService executorService;

    public MongoApiCallsTracker(MongoClient mongoClient, ExecutorService executorService) {
        this.mongoClient = mongoClient;
        this.executorService = executorService;
    }

    @Override
    public void track(final ApiCall apiCall) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                DBCollection collection = getApiCallsCollection();
                BasicDBObject entry = new BasicDBObject("client", apiCall.client)
                        .append("api", apiCall.httpMethod + " " + apiCall.path) //
                        .append("timestamp", new Date());
                collection.insert(entry, WriteConcern.NORMAL);
            }
        });
    }

    private DBCollection getApiCallsCollection() {
        return mongoClient.getDB(DB_NAME).getCollection(API_CALLS_COLLECTION_NAME);
    }

}
