package aphorism3;

import javax.xml.ws.Endpoint;

public final class Publisher {

    static final int PORT = 8888;


    private Publisher() {}

    @SuppressWarnings("PMD.SystemPrintln")
    public static void main(final String[] args) {

        final String url = "http://localhost:" + PORT + "/";
        System.out.println("Restfully publishing on port " + PORT);
        Endpoint.publish(url, new AdagesProvider());

    }
}
