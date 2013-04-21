package twr.toolbox.dropwizard.resources;

import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@Path("/clock")
@Produces(MediaType.APPLICATION_JSON)
public class ClockResource {

    @Path("/date")
    @GET
    @Timed
    public String date() {
        return new Date().toString();
    }

    @Path("/timestamp")
    @GET
    @Timed
    public String timestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

}
