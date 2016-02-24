package adages;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ph.rye.common.lang.Ano;

@Path("/")
public class Adages {


    private static final Logger LOGGER =
            Logger.getLogger(Adages.class.getName());


    // Add aphorisms to taste...
    private final transient String[] aphorisms = {
            "What can be shown cannot be said.",
            "If a lion could talk, we could not understand him.",
            "Philosophy is a battle against the bewitchment of our intelligence by means of language.",
            "Ambition is the death of thought.",
            "The limits of my language mean the limits of my world." };

    @GET
    @Produces({
            MediaType.APPLICATION_XML })
    public JAXBElement<Adage> getXml() {
        return toXml(createAdage());
    }

    @GET
    @Produces({
            MediaType.APPLICATION_JSON })
    @Path("/json")
    public String getJson() {
        return toJson(createAdage());
    }

    @GET
    @Produces({
            MediaType.TEXT_PLAIN })
    @Path("/plain")
    public String getPlain() {
        return createAdage().toString() + "\n";
    }


    /**
     * Create an Adage and set the words property, which likewise sets the
     * wordCount property. The adage is randomly selected from the array,
     * aphorisms.
     */
    private Adage createAdage() {
        final Adage adage = new Adage();
        adage.setWords(aphorisms[new Random().nextInt(aphorisms.length)]);
        return adage;
    }

    /**
     * Java Adage --> XML document
     *
     * @param adage
     * @return
     */
    @XmlElementDecl(namespace = "http://aphorism.adage", name = "adage")
    private JAXBElement<Adage> toXml(final Adage adage) {
        return new JAXBElement<Adage>(new QName("adage"), Adage.class, adage);
    }


    /**
     * Java Adage --> JSON document Jersey provides automatic conversion to JSON
     * using the Jackson libraries. In this example, the conversion is done
     * manually with the Jackson libraries just to indicate how straightforward
     * it is.
     *
     * @param adage
     */
    private String toJson(final Adage adage) {
        final Ano<String> retval =
                new Ano<>("If you see this, there's a problem.");
        try {
            retval.set(new ObjectMapper().writeValueAsString(adage));
        } catch (final JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return retval.get();
    }

}