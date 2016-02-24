package ph.rye.jws21;

import java.io.Serializable;

/**
 * An array of Predictions is to be serialized into an XML or JSON document,
 * which is returned to the consumer on a request.
 *
 * @author royce
 */
public class Prediction implements Serializable, Comparable<Prediction> {


    /** */
    private static final long serialVersionUID = -6258036711691832468L;


    private String who; // person
    private String what; // his/her prediction
    private int id; // identifier used as lookup-key


    public void setWho(final String who) {
        this.who = who;
    }

    public String getWho() {
        return who;
    }

    public void setWhat(final String what) {
        this.what = what;
    }

    public String getWhat() {
        return what;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // implementation of Comparable interface
    @Override
    public int compareTo(final Prediction other) {
        return id - other.id;
    }
}