package ph.rye.jws24;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ph.rye.common.io.BufferedReaderIterator;

@Path("/")
public class PredictionsRS {


    private static final Logger LOGGER =
            Logger.getLogger(PredictionsRS.class.getName());


    private static final String FILENAME = "/WEB-INF/data/predictions.db";


    @Context
    private transient ServletContext servletContext; // dependency injection
    private static PredictionsList predictionList;


    @GET
    @Path("/xml")
    @Produces({
            MediaType.APPLICATION_XML })
    public Response getXml() {
        checkContext();
        return Response.ok(predictionList, "application/xml").build();
    }

    @GET
    @Path("/xml/{id: \\d+}")
    @Produces({
            MediaType.APPLICATION_XML }) // could use "application/xml" instead
    public Response getXml(@PathParam("id") final int id) {
        checkContext();
        return toRequestedType(id, "application/xml");
    }

    @GET
    @Produces({
            MediaType.APPLICATION_JSON })
    @Path("/json")
    public Response getJson() {
        checkContext();
        return Response.ok(toJson(predictionList), "application/json").build();
    }

    @GET
    @Produces({
            MediaType.APPLICATION_JSON })
    @Path("/json/{id: \\d+}")
    public Response getJson(@PathParam("id") final int id) {
        checkContext();
        return toRequestedType(id, "application/json");
    }

    @GET
    @Path("/plain")
    @Produces({
            MediaType.TEXT_PLAIN })
    public String getPlain() {
        checkContext();
        return predictionList.toString();
    }

    @POST
    @Produces({
            MediaType.TEXT_PLAIN })
    @Path("/create")
    public Response create(@FormParam("who") final String who,
                           @FormParam("what") final String what) {

        checkContext();

        // Require both properties to create.
        if (who == null || what == null) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Property 'who' or 'what' is missing.\n")
                .type(MediaType.TEXT_PLAIN)
                .build();
        } else {

            // Otherwise, create the Prediction and add it to the collection.
            final int id = addPrediction(who, what);
            return Response
                .ok(
                    "Prediction " + id + " created: (who = " + who + " what = "
                            + what + ").\n",
                    "text/plain")
                .build();
        }

    }

    @PUT
    @Produces({
            MediaType.TEXT_PLAIN })
    @Path("/update")
    public Response update(@FormParam("id") final int id,
                           @FormParam("who") final String who,
                           @FormParam("what") final String what) {
        checkContext();

        final Prediction prediction = predictionList.find(id);
        if (prediction == null) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("There is no prediction with ID " + id + "\n")
                .type(MediaType.TEXT_PLAIN)
                .build();

        } else if (who == null && what == null) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Neither who nor what is given: nothing to edit.\n")
                .type(MediaType.TEXT_PLAIN)
                .build();
        } else {
            if (who != null) {
                prediction.setWho(who);
            }
            if (what != null) {
                prediction.setWhat(what);
            }
            return Response
                .ok("Prediction " + id + " has been updated.\n", "text/plain")
                .build();
        }

    }

    @DELETE
    @Produces({
            MediaType.TEXT_PLAIN })
    @Path("/delete/{id: \\d+}")
    public Response delete(@PathParam("id") final int id) {

        checkContext();

        final Prediction prediction = predictionList.find(id);
        if (prediction == null) {

            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(
                    "There is no prediction with ID " + id
                            + ". Cannot delete.\n")
                .type(MediaType.TEXT_PLAIN)
                .build();

        } else {

            predictionList.getPredictions().remove(prediction);
            return Response
                .ok("Prediction " + id + " deleted.\n", "text/plain")
                .build();

        }
    }

    //** utilities
    private void checkContext() {
        if (predictionList == null) {
            populate();
        }
    }

    private void populate() {
        predictionList = new PredictionsList();


        final InputStream inputStream =
                servletContext.getResourceAsStream(FILENAME);

        if (inputStream != null) {
            try (final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream))) {

                new BufferedReaderIterator(reader, (index, nextElement) -> {
                    final String[] parts = nextElement.split("!");
                    addPrediction(parts[0], parts[1]);

                }).eachLine();

            } catch (final IOException e) {
                throw new PredictionException(e);
            }
        }
    }

    /** Add a new prediction to the list. */
    private int addPrediction(final String who, final String what) {
        return predictionList.add(who, what);
    }

    private String toJson(final Prediction prediction) {
        return _toJson(prediction);
    }

    private String toJson(final PredictionsList plist) {
        return _toJson(plist);
    }

    /**
     * For internal use only.
     *
     * @param object
     */
    @SuppressWarnings("PMD.MethodNamingConventions")
    private String _toJson(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Ignored EX: " + e.getMessage(), e);
            return "If you see this, there's a problem.";
        }
    }


    /**
     * Generate an HTTP error response or typed OK response.
     *
     * @param id
     * @param type
     * @return
     */
    private Response toRequestedType(final int id, final String type) {
        final Prediction pred = predictionList.find(id);
        if (pred == null) {
            final String msg = id + " is a bad ID.\n";
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(msg)
                .type(MediaType.TEXT_PLAIN)
                .build();
        } else if (type.contains("json")) {
            return Response.ok(toJson(pred), type).build();
        } else {
            return Response.ok(pred, type).build(); // toXml is automatic
        }
    }

}

