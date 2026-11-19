# Scoring model

The scoring model answers one question: of the postings the candidate is eligible for,
which ones deserve a full, tailored application? It is implemented in
[`scoring/`](../scoring) and every rule below has a unit test.

## Two stages, kept apart

```
posting (read in full)
   │
   ▼
Eligibility.check ──── any reason? ──► rejected, total 0, all reasons recorded
   │ no reason
   ▼
JobScorer.score ─────► total out of 100, breakdown per criterion
   │
   ▼
total >= 70 ─────────► application package is prepared
```

**Stage 1 — hard requirements.** A posting is rejected without a score when:

1. it requires an enrolled or completed master's degree the candidate does not have;
2. its weekly hours exceed the legal limit of the working-student (Werkstudent) status;
3. its required German level is above the attested level;
4. its required English level is above the attested level;
5. its commute exceeds the accepted ceiling, unless the posting is fully remote.

All failed requirements are returned together, not only the first one, so the report
shows the complete picture.

Language levels are compared on the CEFR scale `A1 < A2 < B1 < B2 < C1 < C2`.
A level that is absent or not on that scale ("fluent", "verhandlungssicher") counts as
**no requirement**, not as the highest one. The reasoning: an unrecognised phrase is
handled by reading the posting, which step 3 of the pipeline already does; treating it
as C2 would silently discard postings that a human would apply to.

Why separate the two stages at all? A requirement the candidate cannot meet is not a
cost to be weighed against good pay or a short commute. If it were part of the score,
an excellent but unwinnable posting could still reach 70 points and consume an
application.

**Stage 2 — weighted score.** Each criterion produces a ratio between 0 and 1, which is
multiplied by its weight and rounded to whole points. The total is the sum of those
points, so the breakdown always adds up to the total and the total never exceeds 100.

## Criteria and scales

| Criterion       | Weight | Ratio                                                                 | Unknown value |
|-----------------|-------:|-----------------------------------------------------------------------|---------------|
| `FIT`           |     35 | required technologies found in the profile ÷ required technologies    | empty list → 0.5 |
| `VISIBILITY`    |     20 | ≤ 50 staff → 1.0 · ≤ 250 → 0.8 · ≤ 2000 → 0.5 · more → 0.2            | 0.5           |
| `ACCESSIBILITY` |     15 | fully remote → 1.0 · otherwise `1 − commute / maxCommute`, in [0, 1]   | 0.5           |
| `PAY`           |     12 | `(hourlyPay − 13) / 7`, in [0, 1]                                     | not published → 0.4 |
| `FRESHNESS`     |     10 | `1 − daysSincePublished / 28`, in [0, 1]                              | 0.3           |
| `LEARNING`      |      8 | assessed after reading the posting, from 0 to 1                       | —             |

In the data model, `-1` means "not published" for numeric fields and `null` means the
same for dates and language levels.

## Why these weights

**FIT (35)** is the largest because it is the only criterion that measures whether the
application can be honest. It counts only technologies visible in the candidate's
committed code; the profile is rebuilt from source before every run.

**VISIBILITY (20)** rewards small employers. A careful student application is read by a
person at a twenty-person company, and by screening software at a twenty-thousand-person
one. The effort put into a tailored letter pays off where someone reads it.

**ACCESSIBILITY (15)** is linear in commute time. The hard ceiling lives in stage 1;
here, shorter is simply better.

**PAY (12)** maps 13 to 20 euros per hour onto the full range. An unpublished salary
scores 0.4, below most published ones: silence about pay is itself information, but not
proof of low pay, so it is not scored as zero.

**FRESHNESS (10)** decays to zero over four weeks. Older postings are often already
filled, or kept online without an active process.

**LEARNING (8)** is the smallest and the only judgement-based input. It is kept small on
purpose, so that it can break a tie but cannot carry a posting over the threshold alone.

## Threshold

At 70 points or more, an application package is prepared. Below 70, the posting is
recorded in the log with its score and breakdown, and is not contacted.

The threshold is a working value for one profile and one market. It is not a
prediction of success.
