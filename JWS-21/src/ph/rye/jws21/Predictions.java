package ph.rye.jws21;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;

import ph.rye.common.io.BufferedReaderIterator;


public class Predictions {


    private static final String FILENAME = "/WEB-INF/data/predictions.db";


    private final transient ConcurrentMap<Integer, Prediction> predictionMap;
    private transient ServletContext sctx;
    private final transient AtomicInteger mapKey;


    public Predictions() {
        predictionMap = new ConcurrentHashMap<Integer, Prediction>();
        mapKey = new AtomicInteger();
    }

    //** properties

    // The ServletContext is required to read the data from
    // a text file packaged inside the WAR file
    public void setServletContext(final ServletContext sctx) {
        this.sctx = sctx;
    }

    public ServletContext getServletContext() {
        return sctx;
    }

    public void setMap(final ConcurrentMap<String, Prediction> predictions) {
        // no-op for now
    }

    public ConcurrentMap<Integer, Prediction> getMap() {
        // Has the ServletContext been set?
        if (getServletContext() == null) {
            return null;
        }

        // Have the data been read already?
        if (predictionMap.size() < 1) {
            populate();
        }

        return predictionMap;
    }

    public int addPrediction(final Prediction p) {
        final int id = mapKey.incrementAndGet();
        p.setId(id);
        predictionMap.put(id, p);
        return id;
    }

    //** utility
    private void populate() {

        final InputStream inputStream = sctx.getResourceAsStream(FILENAME);
        if (inputStream != null) {

            final BufferedReader buffReader =
                    new BufferedReader(new InputStreamReader(inputStream));

            new BufferedReaderIterator(buffReader, (index, nextElement) -> {
                final String[] parts = nextElement.split("!");
                final Prediction p = new Prediction();
                p.setWho(parts[0]);
                p.setWhat(parts[1]);
                addPrediction(p);
            }).eachLine();
        }
    }

}

