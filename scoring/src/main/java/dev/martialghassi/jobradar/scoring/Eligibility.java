package dev.martialghassi.jobradar.scoring;

import java.util.ArrayList;
import java.util.List;

/**
 * Hard requirements that reject a posting outright instead of costing it points.
 *
 * <p>This separation matters. A language level above the one the candidate can prove is
 * not a weakness to acknowledge in a cover letter, it is a wall. Mixing the two into one
 * score produces postings that look promising and cannot be won.
 */
public final class Eligibility {

    /** CEFR levels in ascending order, used to compare a requirement against an attestation. */
    private static final List<String> CEFR = List.of("A1", "A2", "B1", "B2", "C1", "C2");

    private Eligibility() {
    }

    /**
     * Returns the reasons this posting is out of reach, or an empty list when it is eligible.
     *
     * @param posting the posting, already read in full
     * @param profile what the candidate can actually prove
     * @return one human-readable reason per failed requirement, in the order checked
     */
    public static List<String> check(JobPosting posting, CandidateProfile profile) {
        List<String> reasons = new ArrayList<>();

        if (posting.requiresMasters() && !profile.holdsMasters()) {
            reasons.add("requires a master's degree");
        }
        if (posting.weeklyHoursMax() > profile.maxWeeklyHours()) {
            reasons.add("requires more than " + profile.maxWeeklyHours() + " hours per week");
        }
        if (exceeds(posting.germanLevel(), profile.germanLevel())) {
            reasons.add("requires German at " + posting.germanLevel());
        }
        if (exceeds(posting.englishLevel(), profile.englishLevel())) {
            reasons.add("requires English at " + posting.englishLevel());
        }
        if (!posting.fullyRemote() && posting.commuteMinutes() > profile.maxCommuteMinutes()) {
            reasons.add("commute of " + posting.commuteMinutes() + " minutes is too long");
        }
        return reasons;
    }

    /**
     * True when {@code required} sits above {@code attested} on the CEFR scale.
     * An unknown or absent level is treated as no requirement.
     */
    static boolean exceeds(String required, String attested) {
        int req = CEFR.indexOf(normalise(required));
        int att = CEFR.indexOf(normalise(attested));
        if (req < 0 || att < 0) {
            return false;
        }
        return req > att;
    }

    private static String normalise(String level) {
        return level == null ? "" : level.trim().toUpperCase();
    }
}
