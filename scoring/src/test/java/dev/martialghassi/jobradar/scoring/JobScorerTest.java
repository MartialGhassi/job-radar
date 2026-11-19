package dev.martialghassi.jobradar.scoring;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobScorerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 19);
    private static final JobScorer SCORER = new JobScorer(TODAY);

    private static final CandidateProfile PROFILE = new CandidateProfile(
            Set.of("java", "sql", "rest", "postgresql"), 20, 90, "C1", "B2", false);

    @Test
    void scoresASmallRemoteWellPaidRecentPostingVeryHighly() {
        JobPosting posting = new JobPosting("Working Student Automation", "Example GmbH",
                TODAY.minusDays(1), 30, -1, true, 17.0, 20, false, "B2", "B2");

        ScoreResult result = SCORER.score(posting, PROFILE, List.of("java", "sql", "rest"), 1.0);

        assertFalse(result.isDisqualified());
        assertTrue(result.total() >= ScoreResult.SEND_THRESHOLD,
                "expected the send threshold to be met, got " + result.total());
        assertTrue(result.meetsSendThreshold());
    }

    @Test
    void returnsZeroAndExplainsItselfWhenAHardRequirementFails() {
        JobPosting posting = new JobPosting("Working Student", "Example GmbH",
                TODAY, 30, -1, true, 18.0, 20, true, "B2", "B2");

        ScoreResult result = SCORER.score(posting, PROFILE, List.of("java"), 1.0);

        assertEquals(0, result.total());
        assertTrue(result.isDisqualified());
        assertFalse(result.meetsSendThreshold());
        assertEquals(List.of("requires a master's degree"), result.disqualifiers());
    }

    @Test
    void breakdownAlwaysAddsUpToTheTotal() {
        JobPosting posting = new JobPosting("Working Student", "Northgate Systems",
                TODAY.minusDays(10), 800, 60, false, -1, 20, false, "C1", "B2");

        ScoreResult result = SCORER.score(posting, PROFILE, List.of("java", "python"), 0.5);

        int sum = result.breakdown().values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(result.total(), sum);
        assertTrue(result.total() <= 100);
    }

    @Test
    void anUnpublishedSalaryScoresBelowAPublishedOne() {
        assertTrue(JobScorer.pay(-1) < JobScorer.pay(16.0));
    }

    @Test
    void freshnessFallsToZeroAfterFourWeeksAndNeverBelow() {
        assertEquals(1.0, JobScorer.freshness(TODAY, TODAY), 0.001);
        assertEquals(0.0, JobScorer.freshness(TODAY.minusDays(60), TODAY), 0.001);
    }

    @Test
    void aSmallEmployerOutscoresALargeOneOnVisibility() {
        assertTrue(JobScorer.visibility(30) > JobScorer.visibility(20000));
    }

    @Test
    void halfTheRequiredSkillsGivesHalfTheFitRatio() {
        double ratio = JobScorer.skillOverlap(List.of("java", "kubernetes"), PROFILE);
        assertEquals(0.5, ratio, 0.001);
    }

    @Test
    void breakdownAddsUpAndStaysWithin100AcrossVariedPostings() {
        int[] staff = {-1, 10, 200, 1500, 50000};
        int[] commute = {-1, 0, 45, 89};
        double[] pay = {-1, 12.0, 16.5, 25.0};
        for (int s : staff) {
            for (int c : commute) {
                for (double p : pay) {
                    JobPosting posting = new JobPosting("Working Student", "Lindhof Software",
                            TODAY.minusDays(3), s, c, false, p, 20, false, null, null);
                    ScoreResult result = SCORER.score(posting, PROFILE, List.of("java", "go", "sql"), 0.7);
                    int sum = result.breakdown().values().stream().mapToInt(Integer::intValue).sum();
                    assertEquals(result.total(), sum);
                    assertTrue(result.total() >= 0 && result.total() <= 100);
                }
            }
        }
    }

    @Test
    void aPerfectPostingScoresExactly100() {
        JobPosting posting = new JobPosting("Working Student", "Example GmbH",
                TODAY, 10, -1, true, 20.0, 20, false, null, null);

        ScoreResult result = SCORER.score(posting, PROFILE, List.of("java"), 1.0);

        assertEquals(100, result.total());
    }

    @Test
    void anEmptyRequirementListGivesANeutralFitRatio() {
        assertEquals(0.5, JobScorer.skillOverlap(List.of(), PROFILE), 0.001);
    }
}
