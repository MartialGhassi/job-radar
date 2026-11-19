package dev.martialghassi.jobradar.scoring;

import java.time.LocalDate;

/**
 * A posting after it has been read in full.
 *
 * <p>Every field here comes from the posting page itself. Nothing is inferred from a
 * search-result snippet, because aggregators routinely report the city, the region and
 * the work model incorrectly.
 *
 * <p>Convention: a numeric field set to {@code -1} means the posting does not publish
 * that value. A {@code null} date or language level means the same.
 *
 * @param title            the posting title as published
 * @param employer         the employer name as published
 * @param publishedOn      the date the employer's own page states, or {@code null} when not published
 * @param employeeCount    staff count, or {@code -1} when not published
 * @param commuteMinutes   door-to-door commute in minutes, or {@code -1} when unknown
 * @param fullyRemote      true when the posting states remote work without a presence requirement
 * @param hourlyPayEur     published gross hourly pay, or {@code -1} when not published
 * @param weeklyHoursMax   the maximum weekly hours the posting allows, or {@code -1} when not published
 * @param requiresMasters  true when an enrolled or completed master's degree is required
 * @param germanLevel      required German level on the CEFR scale, for example "C1", or {@code null}
 * @param englishLevel     required English level on the CEFR scale, for example "B2", or {@code null}
 */
public record JobPosting(
        String title,
        String employer,
        LocalDate publishedOn,
        int employeeCount,
        int commuteMinutes,
        boolean fullyRemote,
        double hourlyPayEur,
        int weeklyHoursMax,
        boolean requiresMasters,
        String germanLevel,
        String englishLevel) {
}
