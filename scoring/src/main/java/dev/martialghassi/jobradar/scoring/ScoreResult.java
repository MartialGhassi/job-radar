package dev.martialghassi.jobradar.scoring;

import java.util.List;
import java.util.Map;

/**
 * The outcome of scoring one posting.
 *
 * @param total               points out of 100, or {@code 0} when the posting is disqualified
 * @param breakdown           points awarded per criterion, so every total can be explained
 * @param disqualifiers       reasons the posting was rejected outright; empty when eligible
 * @param meetsSendThreshold  true when the posting is eligible and scores at least 70
 */
public record ScoreResult(
        int total,
        Map<Criterion, Integer> breakdown,
        List<String> disqualifiers,
        boolean meetsSendThreshold) {

    /** The score at or above which a full application is written and sent. */
    public static final int SEND_THRESHOLD = 70;

    /** True when the posting failed a hard requirement and was never scored. */
    public boolean isDisqualified() {
        return !disqualifiers.isEmpty();
    }
}
