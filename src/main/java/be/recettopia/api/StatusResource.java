package be.recettopia.api;

import be.recettopia.connection.DatabaseConnection;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * Technical REST resource used to check that the application is correctly deployed.
 */
@Path("/status")
public class StatusResource {

    /** Logger: writes messages in the Tomcat log (visible in IntelliJ's Run/Services window). */
    private static final Logger LOGGER = Logger.getLogger(StatusResource.class.getName());

    /** Maximum time (in seconds) to wait for the database to answer the validity check. */
    private static final int DB_TIMEOUT_SECONDS = 5;

    /**
     * Checks that the API is running and return HTTP 200 with {"status":"ok"}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() {
        // Build the JSON body {"status":"ok"}
        JSONObject body = new JSONObject().put("status", "ok");
        // Send it back with HTTP code 200 (OK)
        return Response.status(Response.Status.OK).entity(body.toString()).build();
    }

    /**
     * Checks that the Oracle database can be reached and return HTTP 200 with {"database":"ok"}, or HTTP 503 with {"database":"ko"}
     */
    @GET
    @Path("/db")
    @Produces(MediaType.APPLICATION_JSON)
    public Response database() {
        // try-with-resources: the connection is closed automatically at the end of the block,
        // even if an exception is thrown
        try (Connection connection = DatabaseConnection.getConnection()) {

            // Ask Oracle if the connection is still alive (waits at most DB_TIMEOUT_SECONDS)
            boolean valid = connection.isValid(DB_TIMEOUT_SECONDS);

            // "ok" if the database answered, "ko" else
            JSONObject body = new JSONObject().put("database", valid ? "ok" : "ko");

            // HTTP 200 if valid, HTTP 503 (Service Unavailable) otherwise
            Response.Status status = valid ? Response.Status.OK : Response.Status.SERVICE_UNAVAILABLE;
            return Response.status(status).entity(body.toString()).build();

        } catch (Exception e) {
            // The full error goes ONLY to the server log (for us, developers).
            // The client only receives "ko": we never expose technical details
            // (server address, account name, stack trace...) to the outside.
            LOGGER.log(Level.WARNING, "Database connection check failed", e);
            JSONObject body = new JSONObject().put("database", "ko");
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(body.toString()).build();
        }
    }
}