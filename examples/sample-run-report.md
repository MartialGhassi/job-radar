# Run report — 2026-11-03

> **Illustrative example.** The employers (Example GmbH, Northgate Systems, Lindhof
> Software), postings and figures below are fictional. The structure is the one every
> real run produces. Scores were computed with the scoring module in this repository.

## Run header

| Field                                    | Value                         |
|------------------------------------------|-------------------------------|
| Started                                  | 2026-11-03 07:00 (scheduled)  |
| Previous run                             | 2026-11-01                    |
| Delivery check of earlier applications   | 0 bounces                     |
| Postings collected from all sources      | 19                            |
| Already in the log                       | 5                             |
| Duplicates across sources                | 2                             |
| New postings read in full                | 12                            |
| Dropped while reading                    | 4                             |
| Rejected on a hard requirement           | 3                             |
| Scored                                   | 5                             |
| At or above the send threshold (70)      | 3                             |
| Application packages / PDFs              | 3 / 6                         |
| PDF quality check                        | 6 passed, 0 failed            |
| Email drafts created (not sent)          | 3                             |

## Top 3

**1. Example GmbH — Working Student Backend Development (Java) — 83**
Small team, fully remote, pay published. Three of four required technologies are in
the profile; Docker is not, and the cover letter says so. Strongest posting of the run.

**2. Northgate Systems — Working Student IT Automation — 78**
Large employer, so visibility is low, but fully remote, well paid and published
yesterday. The role is Python-heavy; Python is not in the profile, which costs fit
points and is named in the letter. Highest learning value of the run.

**3. Lindhof Software — Working Student Software Testing — 77**
Every required technology is in the profile. Pay is not published (0.4 on `PAY`) and
the commute is moderate. Contact address taken from the employer's careers page.

## All scored postings

| # | Employer          | Role                                   | FIT | VIS | ACC | PAY | FRE | LEA | Total | Decision        |
|---|-------------------|----------------------------------------|----:|----:|----:|----:|----:|----:|------:|-----------------|
| 1 | Example GmbH      | Working Student Backend Development    |  26 |  20 |  15 |   7 |   9 |   6 |    83 | package prepared |
| 2 | Northgate Systems | Working Student IT Automation          |  26 |  10 |  15 |   9 |  10 |   8 |    78 | package prepared |
| 3 | Lindhof Software  | Working Student Software Testing       |  35 |  16 |   9 |   5 |   8 |   4 |    77 | package prepared |
| 4 | Example GmbH      | Working Student IT Support             |  12 |  20 |   8 |   5 |   6 |   2 |    53 | logged, below threshold |
| 5 | Northgate Systems | Working Student Data Platform          |   9 |  10 |   2 |   3 |   3 |   8 |    35 | logged, below threshold |

Weights: FIT 35 · VISIBILITY 20 · ACCESSIBILITY 15 · PAY 12 · FRESHNESS 10 · LEARNING 8.

## Rejected postings

| Employer          | Role                                   | Stage              | Reason                                                         |
|-------------------|----------------------------------------|--------------------|----------------------------------------------------------------|
| Lindhof Software  | Working Student DevOps                 | hard requirement   | requires German at C2                                          |
| Example GmbH      | Working Student Machine Learning       | hard requirement   | requires a master's degree; requires English at C1             |
| Northgate Systems | Working Student Cloud Operations       | hard requirement   | requires more than 20 hours per week; commute of 95 minutes is too long |
| Northgate Systems | Working Student Frontend               | full read          | posting offline on the employer's page                         |
| Lindhof Software  | Junior Developer (full time)           | full read          | not a working-student position                                 |
| Example GmbH      | Internship Quality Assurance           | full read          | mandatory internship only, not open to this status             |
| Lindhof Software  | Working Student Support                | full read          | same role as an application already in the log                 |

## Incidents

- **Work model corrected.** An aggregator listed *Northgate Systems — Working Student
  Data Platform* as fully remote. The employer's page states three on-site days per
  week. The record uses the employer's page, and the commute is scored.
- **Source timeout.** One structured source did not answer on the first request; the
  retry succeeded. No postings were lost.
- **No bounces.** All applications from the previous run were delivered.
