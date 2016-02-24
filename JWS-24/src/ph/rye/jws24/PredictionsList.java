package ph.rye.jws24;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import ph.rye.common.lang.Ano;

@XmlRootElement(name = "predictionsList")
public class PredictionsList {


    private transient List<Prediction> predictionList;
    private final transient AtomicInteger predId;


    public PredictionsList() {
        predictionList = new CopyOnWriteArrayList<Prediction>();
        predId = new AtomicInteger();
    }

    @XmlElement
    @XmlElementWrapper(name = "predictions")
    public List<Prediction> getPredictions() {
        return predictionList;
    }

    public void setPredictions(final List<Prediction> preds) {
        predictionList = preds;
    }

    @Override
    public String toString() {
        final StringBuilder strBuilder = new StringBuilder();
        for (final Prediction p : predictionList) {
            strBuilder.append(p);
        }
        return strBuilder.toString();
    }


    /**
     * Search the list -- for now, the list is short enough that a linear search
     * is OK but binary search would be better if the list got to be an
     * order-of-magnitude larger in size.
     *
     * @param id
     * @return
     */
    public Prediction find(final int id) {
        final Ano<Prediction> retval = new Ano<>();
        for (final Prediction prediction : predictionList) {
            if (prediction.getId() == id) {
                retval.set(prediction);
                break;
            }
        }
        return retval.get();
    }

    public int add(final String who, final String what) {
        final int id = predId.incrementAndGet();
        final Prediction prediction = new Prediction();
        prediction.setWho(who);
        prediction.setWhat(what);
        prediction.setId(id);
        predictionList.add(prediction);
        return id;
    }
}