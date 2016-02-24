package aphorism3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.catalina.connector.Response;
import org.xml.sax.InputSource;

import aphorism3.util.Util;

@WebServiceProvider
@ServiceMode(javax.xml.ws.Service.Mode.MESSAGE)
@BindingType(HTTPBinding.HTTP_BINDING)
public class AdagesProvider implements Provider<Source> {


    private static final Logger LOGGER =
            Logger.getLogger(AdagesProvider.class.getName());


    private static final int BAD_ID = -1;

    @Resource
    protected transient WebServiceContext wsContext;


    /**
     * Implement the Provider interface by defining invoke, which expects an XML
     * source (perhaps null) and returns an XML source (perhaps null).
     *
     *
     */
    @Override
    public Source invoke(final Source request) {
        if (wsContext == null) {
            throw new AdageException("Injection failed on WebServiceContext.");
        }

        final MessageContext msgContext = wsContext.getMessageContext();

        final String httpVerbRaw =
                (String) msgContext.get(MessageContext.HTTP_REQUEST_METHOD);

        final String httpVerb = httpVerbRaw.trim().toUpperCase();

        // Dispatch on verb to the handler method. POST and PUT have non-null
        // requests so only these two get the Source request.
        switch (httpVerb) {
            case "GET":
                return doGet(msgContext);
            case "POST":
                return doPost(request);
            case "PUT":
                return doPut(request);
            case "DELETE":
                return doDelete(msgContext);
            default:
                throw new HTTPException(Response.SC_METHOD_NOT_ALLOWED); // bad verb

        }

    }

    private Source doGet(final MessageContext msgContext) {
        // Parse the query string.
        final String queryString =
                (String) msgContext.get(MessageContext.QUERY_STRING);

        // Get all Adages.
        if (queryString == null) {
            return Util.adages2Xml();
        } else {
            final int id = getId(queryString);
            if (id < 0) {
                throw new HTTPException(Response.SC_BAD_REQUEST);
            }

            final Adage adage = Adages.find(id);
            if (adage == null) {
                throw new HTTPException(Response.SC_NOT_FOUND);
            }

            return Util.adage2Xml(adage);
        }
    }

    private Source doPost(final Source request) {
        if (request == null) {
            throw new HTTPException(Response.SC_BAD_REQUEST);
        }

        final InputSource inputSource = toInputSource(request);

        final String words = findElement("//words/text()", inputSource);
        if (words == null) {
            throw new HTTPException(Response.SC_BAD_REQUEST);
        }

        Adages.add(words);
        final String msg = "The adage '" + words + "' has been created.";
        return Util.toSource(Util.toXml(msg));
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    private Source doPut(final Source request) {
        if (request == null) {
            throw new HTTPException(Response.SC_BAD_REQUEST);
        }

        final InputSource inputSource = toInputSource(request);
        final String words = findElement("//words/text()", inputSource);
        if (words == null) {
            throw new HTTPException(Response.SC_BAD_REQUEST);
        }

        // Format in XML is: <words>!<id>
        final String[] parts = words.split("!");
        if (parts[0].length() < 1 || parts[1].length() < 1) {
            throw new HTTPException(Response.SC_BAD_REQUEST);
        }

        final int id = Util.parseId(parts[1].trim());
        final Adage adage = Adages.find(id);
        if (adage == null) {
            throw new HTTPException(Response.SC_NOT_FOUND);
        }

        adage.setWords(parts[0]);
        final String msg = "Adage " + adage.getId() + " has been updated.";
        return Util.toSource(Util.toXml(msg));
    }

    private Source doDelete(final MessageContext mctx) {
        final String queryString =
                (String) mctx.get(MessageContext.QUERY_STRING);

        // Disallow the deletion of all teams at once.
        if (queryString == null) {
            throw new HTTPException(Response.SC_FORBIDDEN);
        } else {
            final int id = getId(queryString);
            if (id < 0) {
                throw new HTTPException(Response.SC_BAD_REQUEST);
            }

            final Adage adage = Adages.find(id);
            if (adage == null) {
                throw new HTTPException(Response.SC_NOT_FOUND); // not found
            }
            Adages.remove(adage);

            final String msg = "Adage " + id + " removed.";
            return Util.toSource(Util.toXml(msg));
        }
    }

    private int getId(final String queryString) {
        final String[] parts = queryString.split("=");

        if (!parts[0].toLowerCase().trim().equals("id")) {
            return BAD_ID;
        }
        try {
            return Integer.parseInt(parts[1].trim());
        } catch (final NumberFormatException e) {
            return BAD_ID;
        }
    }


    @SuppressWarnings("PMD.PreserveStackTrace")
    private InputSource toInputSource(final Source source) {
        try {
            final Transformer trans =
                    TransformerFactory.newInstance().newTransformer();
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final StreamResult result = new StreamResult(bos);
            trans.transform(source, result);
            return new InputSource(new ByteArrayInputStream(bos.toByteArray()));
        } catch (final TransformerFactoryConfigurationError
                | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new HTTPException(Response.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    private String findElement(final String expression,
                               final InputSource source) {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            return (String) xpath
                .evaluate(expression, source, XPathConstants.STRING);
        } catch (final XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new HTTPException(Response.SC_BAD_REQUEST);
        }
    }
}