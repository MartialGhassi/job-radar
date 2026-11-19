package dev.martialghassi.jobradar.scoring;

import java.util.Set;

/**
 * What the candidate can actually claim, and the limits the candidate works within.
 *
 * <p>The skill set is rebuilt from the source code of the candidate's own projects before every run.
 * A technology announced in a README but absent from the code does not belong here.
 *
 * @param skills              technologies visible in committed code, lower-cased
 * @param maxWeeklyHours      the legal weekly limit for a working student during term time
 * @param maxCommuteMinutes   the commute ceiling the candidate accepts
 * @param germanLevel         attested German level on the CEFR scale
 * @param englishLevel        attested English level on the CEFR scale
 * @param holdsMasters        whether the candidate is enrolled in or holds a master's degree
 */
public record CandidateProfile(
        Set<String> skills,
        int maxWeeklyHours,
        int maxCommuteMinutes,
        String germanLevel,
        String englishLevel,
        boolean holdsMasters) {
}
