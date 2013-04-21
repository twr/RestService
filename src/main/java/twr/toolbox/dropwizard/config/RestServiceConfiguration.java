package twr.toolbox.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class RestServiceConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private MongoConfiguration mongo = new MongoConfiguration();

    public MongoConfiguration getMongo() {
        return mongo;
    }

}
