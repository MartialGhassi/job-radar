# job-radar

An agent workflow that searches for working-student positions every 48 hours, reads each
posting in full, scores it against a fixed rule set and prepares a tailored application
for the ones worth sending.

## Why

Applying seriously means reading every posting in full: the location, the work model,
the language requirement and the hours are often wrong or missing in search results.
At the volume of a real search, that is not possible by hand. This system does not
replace judgement. It makes the same judgement applicable to every posting, and writes
down why each one was kept or dropped.

It has been in service since 08/2026, running every 48 hours from a scheduled task.
The log holds more than 650 entries. The last documented run (19.11.2026) saw and
deduplicated 121 postings, kept 8 after a full read, produced 5 application packages
(10 PDFs) and had 0 quality-check failures.

## How it works

1. **Load context.** Before any search, the agent rereads its rule set, the skill profile,
   the log of postings already seen and the exclusion list. Without this step it would
   write to the same company twice.
2. **Query sources.** Structured job sources filtered by date, radius and work model, plus
   web search and employers' own career pages. Querying broadly and filtering locally
   returns measurably more than a narrow query.
3. **Read each posting in full.** Nothing is scored from a search-result snippet. The
   posting is opened, read entirely, and its availability is rechecked the same day.
   Aggregators regularly report the wrong city, region or work model.
4. **Score.** Six weighted criteria, and separate hard requirements.
5. **Generate documents.** For each posting above the threshold, a CV and a cover letter
   tailored to it, exported to PDF with metadata stripped.
6. **Verify and record.** Every PDF is checked automatically, the run report explains each
   decision, the log is updated and a notification is sent. Every rejected posting is
   recorded with its reason.

A run produces a Markdown report, an updated CSV and XLSX log, the PDF packages, email
drafts and a notification. See [`examples/sample-run-report.md`](examples/sample-run-report.md)
(fictional employers) and the full eleven-step
[`docs/agent-specification.md`](docs/agent-specification.md).

## Scoring model

| Criterion       | Weight | What it measures                                                      |
|-----------------|-------:|-----------------------------------------------------------------------|
| `FIT`           |     35 | share of required technologies the candidate can prove in committed code |
| `VISIBILITY`    |     20 | size and visibility of the employer; smaller is better                |
| `ACCESSIBILITY` |     15 | commute time, or remote share if the posting is remote                |
| `PAY`           |     12 | published pay, and whether it is published at all                     |
| `FRESHNESS`     |     10 | age of the posting                                                    |
| `LEARNING`      |      8 | what the role would teach that cannot be learned alone                |

An application is prepared at 70 points out of 100. The weighting favours small
employers on purpose: a careful student application is read by a person at a
twenty-person company, and by screening software at a twenty-thousand-person one.

**Hard requirements are kept out of the score.** A required language level above the one
the candidate can prove is not a weakness to address in a cover letter, it is a wall.
Folding it into one number produces postings that look attractive and cannot be won.
A posting that fails a hard requirement is rejected without being scored, and every
failed requirement is recorded as the reason.

Exact scales and the reasoning behind them: [`docs/scoring-model.md`](docs/scoring-model.md).

## Rules the agent may not break

- **No invented facts.** No link, employer, salary or date that was not on the page itself.
- **No claimed skill that is not in the code.** The profile is rebuilt from the source of
  the author's own projects before every run. A technology announced in a README but
  absent from the code does not count.
- **Gaps are named, not hidden.** Every cover letter contains a paragraph stating openly
  what the author cannot do yet.
- **Re-check before sending.** The posting is reopened the same day, the recipient address
  comes only from a source the employer published itself, and no second application goes
  to the same role.
- **Delivery is verified.** An entry in the sent folder proves nothing. Only reading the
  mail server bounces shows whether an application actually arrived. Two failed deliveries
  would otherwise have gone unnoticed.

## What is in this repository, and what is not

This repository contains the agent specification, the scoring model and the scoring
module in Java. It does not contain the application log, the operational rules or the
run reports, because they hold data about third parties: employers, recruiters and
their replies. The `.gitignore` excludes those files by type.

## Running the scoring module

Requires a JDK 17 or later.

```
cd scoring
./gradlew test
```

## Built with

The specification, the rule set and the scoring model are mine. The implementation and
the scheduled execution run on an agent workflow built with Claude and Claude Code.
This repository contains the design and the scoring module; the agent that runs it reads
its instructions from a specification file, not from code in this repository.

## Limitations

- The system decides nothing on its own: nothing is sent without human approval.
- The score is a heuristic, not a prediction. It says nothing about whether an
  application will succeed.
- Portals that require a login cannot be automated. The agent prepares the package;
  a human signs in and submits it.
- The weights are tuned for one profile and one market, and do not transfer as they are.
