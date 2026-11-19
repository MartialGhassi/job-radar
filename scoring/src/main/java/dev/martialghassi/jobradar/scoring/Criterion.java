package dev.martialghassi.jobradar.scoring;

/**
 * The six weighted criteria a posting is scored on. The weights add up to 100.
 *
 * <p>The weighting is deliberate: it favours postings where a careful application
 * is actually read, rather than postings with the highest hourly rate.
 */
public enum Criterion {

    /** How closely the required skills overlap with what the candidate has actually built. */
    FIT(35),

    /** Smaller and less visible employers receive more points; large, saturated ones fewer. */
    VISIBILITY(20),

    /** Commute time, or the share of remote work when the posting is remote. */
    ACCESSIBILITY(15),

    /** Published pay, and whether it is published at all. */
    PAY(12),

    /** How recently the posting was published or refreshed. */
    FRESHNESS(10),

    /** What the role would teach that the candidate cannot learn alone. */
    LEARNING(8);

    private final int weight;

    Criterion(int weight) {
        this.weight = weight;
    }

    /** The maximum number of points this criterion contributes to the total of 100. */
    public int weight() {
        return weight;
    }
}
