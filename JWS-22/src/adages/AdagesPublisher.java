package adages;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import javax.ws.rs.ext.RuntimeDelegate;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


/**
 * Standalone Java application to publish the 'adages' JAX-RS service. This is
 * an alternative, typically for development, to publishing with a web server
 * such as Tomcat or Jetty.
 */
public class AdagesPublisher {


    private static final Logger LOGGER =
            Logger.getLogger(AdagesPublisher.class.getName());


    private static final int PORT = 9876;
    private static final String URI = "/resourcesA/";
    private static final String URL = "http://localhost:" + PORT + URI;
    private static final int BACKLOG = 8;


    public static void main(final String[] args) {
        new AdagesPublisher().publish();
    }


    private void publish() {
        final HttpServer server = getServer();
        final HttpHandler requestHandler =
                RuntimeDelegate.getInstance().createEndpoint(
                    new RestfulAdage(),
                    HttpHandler.class);
        server.createContext(URI, requestHandler);
        server.start();
        prompt(server);
    }

    private HttpServer getServer() {
        try {
            return HttpServer
                .create(new InetSocketAddress("localhost", PORT), BACKLOG);
        } catch (final IOException e) {
            throw new AdageException(e);
        }
    }

    private void prompt(final HttpServer server) {
        LOGGER.info(
            "Publishing RestfulAdage on " + URL + ". Hit any key to stop.");

        try {
            System.in.read();
        } catch (final IOException e) {
            LOGGER.info("Server stopping...");
        }

        server.stop(0); // normal termination
    }
}
