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
package aphorism3.util;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.http.HTTPException;

import org.apache.catalina.connector.Response;

import aphorism3.Adage;
import aphorism3.Adages;

/**
 * @author royce
 *
 */
public final class Util {


    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());


    private Util() {}


    public static StreamSource adages2Xml() {
        final String str = toXml(Adages.getListAsArray());
        return toSource(str);
    }

    public static StreamSource adage2Xml(final Adage adage) {
        final String str = toXml(adage);
        return toSource(str);
    }

    public static String toXml(final Object obj) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLEncoder enc = new XMLEncoder(out);
        enc.writeObject(obj);
        enc.close();
        return out.toString();
    }

    public static StreamSource toSource(final String str) {
        return new StreamSource(new StringReader(str));
    }


    @SuppressWarnings("PMD.PreserveStackTrace")
    public static int parseId(final String idStr) {
        try {
            return Integer.parseInt(idStr.trim());
        } catch (final NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new HTTPException(Response.SC_BAD_REQUEST);
        }

    }

}
