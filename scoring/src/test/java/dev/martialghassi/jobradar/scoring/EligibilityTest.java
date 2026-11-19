package dev.martialghassi.jobradar.scoring;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityTest {

    private static final CandidateProfile PROFILE = new CandidateProfile(
            Set.of("java", "sql"), 20, 90, "C1", "B2", false);

    private static JobPosting posting(boolean masters, int hours, String german, String english) {
        return new JobPosting("Working Student", "Example GmbH", LocalDate.of(2026, 11, 18),
                40, 30, false, 16.0, hours, masters, german, english);
    }

    @Test
    void acceptsAPostingThatMatchesEveryHardRequirement() {
        assertTrue(Eligibility.check(posting(false, 20, "C1", "B2"), PROFILE).isEmpty());
    }

    @Test
    void rejectsAPostingThatRequiresAMastersDegree() {
        assertEquals(1, Eligibility.check(posting(true, 20, "C1", "B2"), PROFILE).size());
    }

    @Test
    void rejectsAPostingAboveTheWeeklyHourLimit() {
        assertEquals(1, Eligibility.check(posting(false, 40, "C1", "B2"), PROFILE).size());
    }

    @Test
    void rejectsALanguageLevelAboveTheAttestedOne() {
        assertEquals(1, Eligibility.check(posting(false, 20, "C2", "B2"), PROFILE).size());
    }

    @Test
    void acceptsARequirementBelowTheAttestedLevel() {
        assertTrue(Eligibility.check(posting(false, 20, "B2", "B1"), PROFILE).isEmpty());
    }

    @Test
    void treatsAnUnknownLevelAsNoRequirement() {
        assertFalse(Eligibility.exceeds("", "B2"));
        assertFalse(Eligibility.exceeds("fluent", "B2"));
    }

    @Test
    void reportsEveryFailedRequirementAtOnce() {
        assertEquals(List.of(
                        "requires a master's degree",
                        "requires more than 20 hours per week",
                        "requires German at C2"),
                Eligibility.check(posting(true, 40, "C2", "B2"), PROFILE));
    }

    @Test
    void cefrRequirementAboveTheAttestedLevelExceedsIt() {
        assertTrue(Eligibility.exceeds("C2", "C1"));
    }

    @Test
    void cefrRequirementAtOrBelowTheAttestedLevelDoesNotExceedIt() {
        assertFalse(Eligibility.exceeds("B1", "C1"));
        assertFalse(Eligibility.exceeds("c1", "C1"));
    }

    @Test
    void unrecognisedCefrRequirementCountsAsNoRequirement() {
        assertFalse(Eligibility.exceeds("verhandlungssicher", "A1"));
        assertFalse(Eligibility.exceeds(null, "A1"));
    }

    @Test
    void rejectsACommuteAboveTheCeilingUnlessFullyRemote() {
        JobPosting onSite = new JobPosting("Working Student", "Lindhof Software", LocalDate.of(2026, 11, 18),
                40, 120, false, 16.0, 20, false, "B2", "B2");
        JobPosting remote = new JobPosting("Working Student", "Lindhof Software", LocalDate.of(2026, 11, 18),
                40, 120, true, 16.0, 20, false, "B2", "B2");

        assertEquals(List.of("commute of 120 minutes is too long"), Eligibility.check(onSite, PROFILE));
        assertTrue(Eligibility.check(remote, PROFILE).isEmpty());
    }
}
