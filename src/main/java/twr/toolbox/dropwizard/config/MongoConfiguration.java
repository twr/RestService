package twr.toolbox.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class MongoConfiguration {

    @NotEmpty
    @JsonProperty
    private String host;

    @Min(1024)
    @Max(65535)
    @JsonProperty
    private int port = 27017;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
