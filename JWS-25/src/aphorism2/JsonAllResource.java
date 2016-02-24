package aphorism2;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class JsonAllResource extends ServerResource {


    private static final Logger LOGGER =
            Logger.getLogger(JsonAllResource.class.getName());


    @Get
    public Representation toJson() {
        final List<Adage> list = Adages.getList();

        try {
            return new JsonRepresentation(
                new StringRepresentation(list.toString()));
        } catch (final IOException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
            return null;
        }
    }

}

