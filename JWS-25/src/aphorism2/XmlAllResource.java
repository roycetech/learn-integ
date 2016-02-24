package aphorism2;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlAllResource extends ServerResource {


    private static final Logger LOGGER =
            Logger.getLogger(XmlAllResource.class.getName());


    @Get
    public Representation toXml() {
        final List<Adage> list = Adages.getList();
        try {
            final DomRepresentation dom =
                    new DomRepresentation(MediaType.TEXT_XML);
            dom.setIndenting(true);
            final Document doc = dom.getDocument();

            final Element root = doc.createElement("adages");
            for (final Adage adage : list) {
                final Element next = doc.createElement("adage");
                next.appendChild(doc.createTextNode(adage.toString()));
                root.appendChild(next);
            }
            doc.appendChild(root);
            return dom;
        } catch (final IOException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
            return null;
        }
    }
}

