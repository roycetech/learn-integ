package ph.rye.jws24;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "prediction")
public class Prediction implements Comparable<Prediction> {


    private String who;
    private String what;
    private int id;


    @Override
    public String toString() {
        return String.format("%2d: ", id) + who + " ==> " + what + "\n";
    }

    public void setWho(final String who) {
        this.who = who;
    }

    @XmlElement
    public String getWho() {
        return who;
    }

    public void setWhat(final String what) {
        this.what = what;
    }

    @XmlElement
    public String getWhat() {
        return what;
    }

    public void setId(final int id) {
        this.id = id;
    }

    @XmlElement
    public int getId() {
        return id;
    }

    @Override
    public int compareTo(final Prediction other) {
        return id - other.id;
    }

}