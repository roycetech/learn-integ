/**
 *   Copyright 2016 Royce Remulla
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ph.rye.jws21;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

/**
 * A workaround is necessary for a PUT request because neither Tomcat nor Jetty
 * generates a workable parameter map for this HTTP verb.
 *
 * @author royce
 */
public class RequestParser {


    private final String id;
    private final boolean whoFlag;
    private final String who;
    private final String what;


    /**
     * To simplify the hack, assume that the PUT request has exactly two
     * parameters: the id and either who or what. Assume, further, that the id
     * comes first. From the client side, a hash character # separates the id
     * and the who/what, e.g.,
     * 
     * id=33#who=Homer Allision
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    RequestParser(final HttpServletRequest request) {
        try {
            final BufferedReader buffReader = new BufferedReader(
                new InputStreamReader(request.getInputStream()));

            final String data = buffReader.readLine();
            final String[] args = data.split("#"); // id in args[0], rest in args[1]
            final String[] parts1 = args[0].split("="); // id = parts1[1]
            id = parts1[1];

            final String[] parts2 = args[1].split("="); // parts2[0] is key
            if (parts2[0].contains("who")) {
                whoFlag = true;
                who = parts2[1];
                what = null;
            } else if (parts2[0].contains("what")) {
                whoFlag = false;
                what = parts2[1];
                who = null;
            } else {
                throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (final IOException e) {
            throw new HTTPException(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }


    /**
     * @return the id
     */
    public String getId() {
        return id;
    }


    /**
     * @return the whoFlag
     */
    public boolean isWhoFlag() {
        return whoFlag;
    }


    /**
     * @return the who
     */
    public String getWho() {
        return who;
    }


    /**
     * @return the what
     */
    public String getWhat() {
        return what;
    }


}
