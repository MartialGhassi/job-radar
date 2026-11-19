# job-radar

An agent workflow that searches for working-student positions every 48 hours, reads each
posting in full, scores it against a fixed rule set and prepares a tailored application
for the ones worth sending.

## Why

Applying seriously means reading every posting in full, and at a real search volume
that is impossible by hand. The system does not replace judgement; it applies the same
judgement to every posting and records why each one was kept or dropped.

In service since 08/2026, one run every 48 hours from a scheduled task, more than 650
log entries. Last documented run (19.11.2026): 121 postings seen and deduplicated,
8 kept after a full read, 5 packages (10 PDFs), 0 quality-check failures.

## How it works

1. **Load context.** Reread the rule set, skill profile, log of postings already seen and
   exclusion list. Without this, the agent would write to the same company twice.
2. **Query sources.** Structured sources filtered by date, radius and work model, plus web
   search and career pages. Querying broadly and filtering locally returns measurably
   more than a narrow query.
3. **Read each posting in full.** Never score from a snippet; recheck availability the
   same day. Aggregators regularly report the wrong city, region or work model.
4. **Score.** Six weighted criteria, and separate hard requirements.
5. **Generate documents.** A tailored CV and cover letter per selected posting, as PDF
   with metadata stripped.
6. **Verify and record.** Automatic PDF check, a report explaining each decision, log
   update, notification. Every rejected posting is recorded with its reason.

Output: a Markdown report, the log as CSV and XLSX, PDF packages, email drafts, a
notification. See the [sample run report](examples/sample-run-report.md) (fictional
employers) and the fifteen-step [agent specification](docs/agent-specification.md).

## Scoring model

| Criterion       | Weight | What it measures                                                      |
|-----------------|-------:|-----------------------------------------------------------------------|
| `FIT`           |     35 | share of required technologies the candidate can prove in committed code |
| `VISIBILITY`    |     20 | size and visibility of the employer; smaller is better                |
| `ACCESSIBILITY` |     15 | commute time, or remote share if the posting is remote                |
| `PAY`           |     12 | published pay, and whether it is published at all                     |
| `FRESHNESS`     |     10 | age of the posting                                                    |
| `LEARNING`      |      8 | what the role would teach that cannot be learned alone                |

The send threshold is 70 out of 100. Small employers are favoured on purpose: a careful
student application is read by a person at a twenty-person company, and by screening
software at a twenty-thousand-person one.

**Hard requirements are kept out of the score.** A language level above the one the
candidate can prove is not a weakness to address in a letter, it is a wall. Folding it
into the score produces postings that look attractive and cannot be won. Such a posting
is rejected unscored, with every failed requirement recorded. Details:
[`docs/scoring-model.md`](docs/scoring-model.md).

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

Included: the agent specification, the scoring model and the Java scoring module.
Not included: the application log, the operational rules and the run reports, because
they hold data about third parties. The `.gitignore` excludes them by type.

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

- Nothing is sent without human approval.
- The score is a heuristic, not a prediction of success.
- Portals behind a login are not automated: the agent prepares, a human submits.
- The weights are tuned for one profile and one market and do not transfer as they are.
