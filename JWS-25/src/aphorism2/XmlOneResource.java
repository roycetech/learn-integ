package aphorism2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlOneResource extends ServerResource {


    private static final Logger LOGGER =
            Logger.getLogger(XmlAllResource.class.getName());


    @Get
    public Representation toXml() {
        final String sid = (String) getRequest().getAttributes().get("id");
        if (sid == null) {
            return badRequest("No ID provided\n");
        } else {
            final int id = Integer.parseInt(sid.trim());

            // Search for the Adage.
            final Adage adage = Adages.find(id);
            if (adage == null) {
                return badRequest("No adage with ID " + id + "\n");
            }

            try {
                final DomRepresentation dom =
                        new DomRepresentation(MediaType.TEXT_XML);
                dom.setIndenting(true);
                final Document doc = dom.getDocument();

                final Element root = doc.createElement("adage");
                root.appendChild(doc.createTextNode(adage.toString()));
                doc.appendChild(root);
                return dom;
            } catch (final IOException e) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
                return null;
            }
        }

    }

    private StringRepresentation badRequest(final String msg) {
        final Status error = new Status(Status.CLIENT_ERROR_BAD_REQUEST, msg);
        return new StringRepresentation(error.toString());
    }

}

