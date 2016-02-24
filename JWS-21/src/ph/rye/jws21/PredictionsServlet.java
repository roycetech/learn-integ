/*
 *
 */
package ph.rye.jws21;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import ph.rye.common.lang.Ano;
import ph.rye.jws21.util.Util;


public class PredictionsServlet extends HttpServlet {

    /** */
    private static final long serialVersionUID = -3012703437038837662L;


    private static final Logger LOGGER =
            Logger.getLogger(PredictionsServlet.class.getName());


    private transient Predictions predictions; // back-end bean


    /**
     * Executed when servlet is first loaded into container. Create a
     * Predictions object and set its servletContext property so that the object
     * can do I/O.
     */
    @Override
    public void init() {
        predictions = new Predictions();
        predictions.setServletContext(this.getServletContext());
    }

    /**
     * GET /predictions2<br />
     * GET /predictions2?id=1<br />
     * If the HTTP Accept header is set to application/json (or an equivalent
     * such as text/x-json), the response is JSON and XML otherwise.
     *
     * @param request
     * @param response
     */
    @Override
    public void doGet(final HttpServletRequest request,
                      final HttpServletResponse response) {
        LOGGER.log(Level.FINE, "doGet()");


        final String param = request.getParameter("id");
        final Integer key =
                param == null ? null : Integer.valueOf(param.trim());

        String xmlPayLoad;
        // If no query string, assume client wants the full list.
        if (key == null) {
            final ConcurrentMap<Integer, Prediction> map = predictions.getMap();
            final Object[] list = map.values().toArray();
            Arrays.sort(list);
            xmlPayLoad = Util.toXML(list);
        }
        // Otherwise, return the specified Prediction.
        else {
            final Prediction pred = predictions.getMap().get(key);
            if (pred == null) {
                xmlPayLoad =
                        Util.toXML(key + " does not map to a prediction.\n");
            } else {
                xmlPayLoad = Util.toXML(pred);
            }
        }

        // Check user preference for XML or JSON by inspecting
        // the HTTP headers for the Accept key.
        final Ano<Boolean> useJson = new Ano<>(false);
        final String accept = request.getHeader("accept");
        if (accept != null && accept.contains("json")) {
            useJson.set(true);
        }
        sendResponse(response, xmlPayLoad, useJson.get());

    }


    /**
     * POST /predictions2<br />
     * HTTP body should contain two keys, one for the predictor ("who") and
     * another for the prediction ("what").
     */
    @Override
    public void doPost(final HttpServletRequest request,
                       final HttpServletResponse response) {

        final String who = request.getParameter("who");
        final String what = request.getParameter("what");

        // Are the data to create a new prediction present?
        if (who == null || what == null) {
            throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        }

        // Create a Prediction.
        final Prediction prediction = new Prediction();
        prediction.setWho(who);
        prediction.setWhat(what);

        // Save the ID of the newly created Prediction.
        final int id = predictions.addPrediction(prediction);

        // Generate the confirmation message.
        final String msg = "Prediction " + id + " created.\n";
        sendResponse(response, Util.toXML(msg), false);
    }

    /**
     * PUT /predictions <br />
     * HTTP body should contain at least two keys: the prediction's id and
     * either who or what.
     */
    @Override
    public void doPut(final HttpServletRequest request,
                      final HttpServletResponse response) {


        final RequestParser requestParser = new RequestParser(request);

        // If no key, then the request is ill formed.
        if (requestParser.getId() == null) {
            throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        }

        // Look up the specified prediction.
        final Prediction prediction = predictions
            .getMap()
            .get(Integer.parseInt(requestParser.getId().trim()));

        if (prediction == null) { // not found?
            final String msg =
                    requestParser.getId() + " does not map to a Prediction.\n";
            sendResponse(response, Util.toXML(msg), false);

        } else { // found

            if (requestParser.isWhoFlag()) {
                prediction.setWho(requestParser.getWho());
            } else {
                prediction.setWhat(requestParser.getWhat());
            }

            final String msg = "Prediction " + requestParser.getId()
                    + " has been edited.\n";
            sendResponse(response, Util.toXML(msg), false);
        }

    }

    /** DELETE /predictions2?id=1 */
    @Override
    public void doDelete(final HttpServletRequest request,
                         final HttpServletResponse response) {

        final String param = request.getParameter("id");

        if (param == null) {
            throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            final Integer key = Integer.valueOf(param.trim());
            predictions.getMap().remove(key);
            final String msg = "Prediction " + key + " removed.\n";
            sendResponse(response, Util.toXML(msg), false);
        }

    }

    @Override
    public void doTrace(final HttpServletRequest request,
                        final HttpServletResponse response) {
        throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public void doHead(final HttpServletRequest request,
                       final HttpServletResponse response) {
        throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public void doOptions(final HttpServletRequest request,
                          final HttpServletResponse response) {
        throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @SuppressWarnings("PMD.PreserveStackTrace" /* Logged separately. */)
    /**
     * @param response
     * @param xmlPayload
     * @param useJson set to true to use JSON, false to use XML payload.
     */
    private void sendResponse(final HttpServletResponse response,
                              final String xmlPayload, final boolean useJson) {
        try {

            String actualPayLoad;
            if (useJson) {
                final JSONObject jobt = XML.toJSONObject(xmlPayload);
                actualPayLoad = jobt.toString(3); // 3 is indentation level for nice look
            } else {
                actualPayLoad = xmlPayload;
            }

            final OutputStream out = response.getOutputStream();
            out.write(actualPayLoad.getBytes());
            out.flush();

        } catch (JSONException | IOException e) {

            LOGGER.log(Level.SEVERE, "Unexpected exception", e);

            throw new HTTPException(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
