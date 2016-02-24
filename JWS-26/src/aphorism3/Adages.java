package aphorism3;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import ph.rye.common.lang.Ano;

public final class Adages {


    private static CopyOnWriteArrayList<Adage> adageList;
    private static AtomicInteger id;

    static {
        final String[] aphorisms = {
                "What can be shown cannot be said.",
                "If a lion could talk, we could not understand him.",
                "Philosophy is a battle against the bewitchment of our intelligence by means of language.",
                "Ambition is the death of thought.",
                "The limits of my language mean the limits of my world." };

        adageList = new CopyOnWriteArrayList<Adage>();
        id = new AtomicInteger();
        for (final String str : aphorisms) {
            add(str);
        }
    }

    private Adages() {}

    public static String toPlain() {
        final StringBuilder strBuilder = new StringBuilder();
        for (final Adage adage : adageList) {
            strBuilder.append(adage).append('\n');
        }
        return strBuilder.toString();
    }

    public static Object[] getListAsArray() {
        return adageList.toArray();
    }

    public static Adage find(final int id) {
        final Ano<Adage> adage = new Ano<>();
        for (final Adage nextAdage : adageList) {
            if (nextAdage.getId() == id) {
                adage.set(nextAdage);
                break;
            }
        }
        return adage.get();
    }

    public static void add(final String words) {
        final int localId = id.incrementAndGet();
        final Adage adage = new Adage();
        adage.setWords(words);
        adage.setId(localId);
        adageList.add(adage);
    }

    public static void remove(final Adage adage) {
        adageList.remove(adage);
    }
}
