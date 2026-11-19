# Agent specification

This is the structure of the specification the agent reads at the start of every run.
Personal values (thresholds, locations, language levels, the exclusion list) and every
employer name have been removed; constraints are described generically.

The agent runs every 48 hours from a scheduled task. A run has eleven steps. Each step
has a goal, inputs, outputs and guardrails. A step that fails its guardrail stops the
run or drops the posting; it never continues on a guess.

---

## 1. Load context

**Goal.** Start every run from what is already known, so that no decision is repeated
or contradicted.

**Inputs.** The rule set, the skill profile, the posting log, the exclusion list, the
previous run report.

**Outputs.** An in-memory set of known postings and excluded employers.

**Guardrails.**
- No search starts before the log and the exclusion list are loaded.
- If the log cannot be read, the run stops. Running without it would mean writing to
  the same employer twice.

## 2. Rebuild the skill profile

**Goal.** Claim only what the code proves.

**Inputs.** The source repositories of the author's own projects.

**Outputs.** A lower-cased set of technologies visible in committed code.

**Guardrails.**
- A technology mentioned in a README, a course title or a CV but absent from the code
  is not added.
- The profile is rebuilt every run, not cached, so it never overstates.

## 3. Check delivery of earlier applications

**Goal.** Know whether what was sent actually arrived.

**Inputs.** The mail server's bounce messages since the previous run.

**Outputs.** Log entries updated to "delivered" or "bounced"; bounces listed as
incidents.

**Guardrails.**
- An entry in the sent folder is not proof of delivery.
- A bounced application is flagged for a human; the agent does not resend it on its own.

## 4. Query sources

**Goal.** Collect every candidate posting published since the previous run.

**Inputs.** Structured job sources filtered by date, radius and work model; web search;
employers' own career pages.

**Outputs.** A raw list of postings with their source URL.

**Guardrails.**
- Query broadly and filter locally. Narrow source-side queries miss postings that are
  mislabelled by the source.
- Every posting keeps the URL it came from.

## 5. Deduplicate

**Goal.** Look at each posting once.

**Inputs.** The raw list, the posting log, the exclusion list.

**Outputs.** New postings only, each with a stable key.

**Guardrails.**
- A posting already in the log, or from an excluded employer, is dropped and counted.
- The same role published on several sources counts once.

## 6. Read each posting in full

**Goal.** Base every decision on the employer's own page.

**Inputs.** The deduplicated postings.

**Outputs.** One structured record per posting: title, employer, publication date,
staff count, location and work model, commute, pay, weekly hours, degree and language
requirements, required technologies.

**Guardrails.**
- Nothing is extracted from a search-result snippet.
- Availability is rechecked the same day. A posting that is offline is dropped with
  that reason.
- Where the aggregator and the employer's page disagree (city, region, work model),
  the employer's page wins and the discrepancy is noted as an incident.
- A field the page does not state is recorded as unknown, never estimated.

## 7. Apply hard requirements

**Goal.** Remove postings that cannot be won, before they are scored.

**Inputs.** The structured records, the candidate's limits.

**Outputs.** Eligible postings, and rejected postings with every failed requirement.

**Guardrails.**
- Checked: required master's degree, weekly hours above the working-student limit,
  German level above the attested level, English level above the attested level,
  commute above the ceiling unless fully remote.
- All reasons are recorded, not only the first.

## 8. Score

**Goal.** Rank eligible postings on six weighted criteria.

**Inputs.** Eligible postings, the skill profile.

**Outputs.** A total out of 100 and a per-criterion breakdown for each posting.

**Guardrails.**
- The breakdown is always kept and always adds up to the total.
- Only postings at or above the send threshold go further. The others are logged with
  their score.

The model is described in [`scoring-model.md`](scoring-model.md).

## 9. Generate documents

**Goal.** One honest, tailored application per selected posting.

**Inputs.** Selected postings, the skill profile, the CV and letter templates.

**Outputs.** For each posting, a CV and a cover letter as PDF, and an email draft.

**Guardrails.**
- No skill that is not in the profile.
- Every cover letter has a paragraph naming what the author cannot do yet.
- No employer, salary, date or link that was not on the posting page.
- PDF metadata is stripped.
- The recipient address comes only from a source the employer published itself.

## 10. Verify

**Goal.** Catch a broken document before a human sees it.

**Inputs.** The generated PDFs and drafts.

**Outputs.** A pass or fail per file.

**Guardrails.**
- Each PDF is checked automatically: it opens, has the expected page count, contains
  the right employer and role, and has no leftover template placeholders or metadata.
- A failed file is regenerated once; if it fails again, the posting is moved to
  incidents and no draft is left for it.
- No second application goes to a role already in the log.

## 11. Deliverables

**Goal.** Hand a complete, explained run to a human.

**Inputs.** Everything above.

**Outputs.**
- a Markdown run report: header, top postings with comments, the full scored table,
  the rejected postings with their reasons, incidents;
- the updated log, in CSV and XLSX;
- the PDF packages;
- email drafts, not sent;
- a notification that the run is finished.

**Guardrails.**
- Nothing is sent by the agent. A human reviews every draft and sends it.
- Every posting seen in the run appears in the log with its outcome and reason.
- Applications through portals that require a login are prepared, not submitted.
