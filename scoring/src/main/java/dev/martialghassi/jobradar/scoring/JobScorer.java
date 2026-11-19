package dev.martialghassi.jobradar.scoring;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Scores one posting out of 100 against a candidate profile.
 *
 * <p>Each criterion produces a ratio between 0 and 1, which is multiplied by that
 * criterion's weight. The breakdown is kept so that every total can be explained in the
 * run report, which is the point: a number nobody can justify is worse than no number.
 */
public final class JobScorer {

    private final LocalDate today;

    /** @param today the reference date for the freshness criterion */
    public JobScorer(LocalDate today) {
        this.today = today;
    }

    /**
     * Scores a posting, or rejects it when a hard requirement fails.
     *
     * @param posting  the posting, already read in full
     * @param profile  what the candidate can actually prove
     * @param required the technologies the posting asks for, lower-cased
     * @param learning how much the role would teach, from 0 (nothing new) to 1 (a great deal)
     */
    public ScoreResult score(JobPosting posting,
                             CandidateProfile profile,
                             List<String> required,
                             double learning) {

        List<String> disqualifiers = Eligibility.check(posting, profile);
        if (!disqualifiers.isEmpty()) {
            return new ScoreResult(0, Map.of(), disqualifiers, false);
        }

        Map<Criterion, Double> ratios = new EnumMap<>(Criterion.class);
        ratios.put(Criterion.FIT, skillOverlap(required, profile));
        ratios.put(Criterion.VISIBILITY, visibility(posting.employeeCount()));
        ratios.put(Criterion.ACCESSIBILITY, accessibility(posting, profile));
        ratios.put(Criterion.PAY, pay(posting.hourlyPayEur()));
        ratios.put(Criterion.FRESHNESS, freshness(posting.publishedOn(), today));
        ratios.put(Criterion.LEARNING, clamp(learning));

        Map<Criterion, Integer> breakdown = new EnumMap<>(Criterion.class);
        int total = 0;
        for (Criterion c : Criterion.values()) {
            int points = (int) Math.round(clamp(ratios.get(c)) * c.weight());
            breakdown.put(c, points);
            total += points;
        }
        return new ScoreResult(total, breakdown, List.of(), total >= ScoreResult.SEND_THRESHOLD);
    }

    /** Share of the required technologies the candidate can already evidence in code. */
    static double skillOverlap(List<String> required, CandidateProfile profile) {
        if (required.isEmpty()) {
            return 0.5;
        }
        long matched = required.stream()
                .map(s -> s.toLowerCase().trim())
                .filter(profile.skills()::contains)
                .count();
        return (double) matched / required.size();
    }

    /**
     * Smaller employers score higher. A student application is read by a person in a
     * twenty-person company and by an applicant tracking system in a twenty-thousand-person one.
     */
    static double visibility(int employeeCount) {
        if (employeeCount < 0) {
            return 0.5;
        }
        if (employeeCount <= 50) {
            return 1.0;
        }
        if (employeeCount <= 250) {
            return 0.8;
        }
        if (employeeCount <= 2000) {
            return 0.5;
        }
        return 0.2;
    }

    /** Fully remote removes the geographic constraint; otherwise the commute decides. */
    static double accessibility(JobPosting posting, CandidateProfile profile) {
        if (posting.fullyRemote()) {
            return 1.0;
        }
        if (posting.commuteMinutes() < 0 || profile.maxCommuteMinutes() <= 0) {
            return 0.5;
        }
        double ratio = 1.0 - ((double) posting.commuteMinutes() / profile.maxCommuteMinutes());
        return clamp(ratio);
    }

    /**
     * Maps 13 to 20 euros per hour onto 0 to 1. Pay that is not published scores 0.4,
     * because silence about pay is itself a piece of information.
     */
    static double pay(double hourlyPayEur) {
        if (hourlyPayEur < 0) {
            return 0.4;
        }
        double ratio = (hourlyPayEur - 13.0) / 7.0;
        return clamp(ratio);
    }

    /** A posting loses its freshness points over four weeks, and never goes below zero. */
    static double freshness(LocalDate publishedOn, LocalDate today) {
        if (publishedOn == null) {
            return 0.3;
        }
        long days = ChronoUnit.DAYS.between(publishedOn, today);
        return clamp(1.0 - (days / 28.0));
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
