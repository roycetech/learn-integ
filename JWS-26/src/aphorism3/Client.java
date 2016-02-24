package aphorism3;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;

public class Client {

    private static final Logger LOGGER =
            Logger.getLogger(Client.class.getName());


    private static final String URL = "http://localhost:8888/";


    private transient Service service;


    private final transient QName serviceName =
            new QName("http://sample.org", "VerySimpleService");

    private final transient QName portName =
            new QName("http://sample.org", "PortName");


    public static void main(final String[] args) {
        new Client().testProvider();
    }


    private void init() {
        service = Service.create(serviceName);
        service.addPort(portName, HTTPBinding.HTTP_BINDING, URL);
        LOGGER.info("Setup done.");
    }

    private Source invoke(final Dispatch<Source> dispatch, final String verb,
                          final String request) {
        final Map<String, Object> requestContext = dispatch.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, verb);
        return dispatch.invoke(new StreamSource(new StringReader(request)));
    }

    @SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
    public void testProvider() {
        init();
        final Dispatch<Source> dispatch = service
            .createDispatch(portName, Source.class, Service.Mode.MESSAGE);

        // POST test
        @SuppressWarnings("PMD.AvoidFinalLocalVariable")
        final String request =
                "<ns1:foo xmlns:ns1='http://sample.org'><words>This is the way the world ends.</words></ns1:foo>";

        LOGGER.info("\nInvoking xml request: " + request);
        final Source result = this.invoke(dispatch, "POST", request);
        LOGGER.info("Response ==> " + toXmlString(result));
    }

    private String toXmlString(final Source result) {
        try {
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer();
            transformer
                .setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            final OutputStream out = new ByteArrayOutputStream();
            final StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(out);
            transformer.transform(result, streamResult);
            return streamResult.getOutputStream().toString();
        } catch (final TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }
}
