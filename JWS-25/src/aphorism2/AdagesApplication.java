package aphorism2;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.routing.Router;

import ph.rye.common.lang.Ano;

public class AdagesApplication extends Application {

    /**
     * To illustrate the different API possibilities, implement the DELETE
     * operation as an anonymous Restlet class. For the remaining operations,
     * follow Restlet best practices and implement each as a Java class.
     */
    @Override
    public Restlet createInboundRoot() {

        final Restlet deleteHandler = new Restlet(getContext()) {

            @Override
            public void handle(final Request request, final Response response) {

                final Ano<String> msg = new Ano<>();

                final String sid = (String) request.getAttributes().get("id");
                if (sid == null) {
                    msg.set(badRequest("No ID given.\n"));
                } else {
                    final Integer id = Integer.parseInt(sid.trim());
                    final Adage adage = Adages.find(id);
                    if (adage == null) {
                        msg.set(badRequest("No adage with ID " + id + "\n"));
                    } else {
                        Adages.getList().remove(adage);
                        msg.set("Adage " + id + " removed.\n");
                    }
                }


                response.setEntity(msg.get(), MediaType.TEXT_PLAIN);
            }
        };

        // Create the routing table.
        final Router router = new Router(getContext());
        router.attach("/", PlainResource.class);
        router.attach("/xml", XmlAllResource.class);
        router.attach("/xml/{id}", XmlOneResource.class);
        router.attach("/json", JsonAllResource.class);
        router.attach("/create", CreateResource.class);
        router.attach("/update", UpdateResource.class);
        router.attach("/delete/{id}", deleteHandler);

        return router;
    }

    private String badRequest(final String msg) {
        final Status error = new Status(Status.CLIENT_ERROR_BAD_REQUEST, msg);
        return error.toString();
    }

}
