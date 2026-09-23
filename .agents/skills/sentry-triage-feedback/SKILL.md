---
name: sentry-triage-feedback
description: Use this skill when the user asks to triage user feedback collected on Sentry. Requires the Sentry MCP server.
---

# Skill: Feedback Triage

ArkPets users can upload their log files through the error dialog.
Each upload is stored in Sentry as a feedback issue with the log files attached.
This skill defines how to fetch, analyze and triage them.

## Target Data

- Feedback issues are found with the search query `issue.category:feedback`.
- Attachments are logs: `desktop.<pid>.log`, and optionally `core.<pid>.log` / `hs_err_pid<pid>.log`.
  They are served as `text/plain` (inlined by the tool) or as `application/octet-stream` (no inline content).
- Attachments follow the plan's retention period. Older feedback may have no attachments.

This skill requires the Sentry MCP server to be configured. If it is not available,
you can remind the user to visit https://mcp.sentry.dev/ and do the setup.

## Master Workflow

### Steps

If you are not a subagent, follow this workflow.

1. **Confirm the time range** with the user: 24h, 7d, 14d, 30d or 90d, together with the target organization,
   project and environment when the user did not state them explicitly.
2. **Confirm write permission** with the user: whether posting comments and resolving issues are allowed.
   Read-only analysis is always allowed. Never perform any write operation without explicit approval.
3. **List the feedback** using `search_issues` (see the tool cheat sheet) and count them.
4. **Dispatch the work**:
   - If there are 10 or fewer, process all of them in the current session by yourself.
   - If there are *more than 10*, split the issue IDs into disjoint batches of at most 10 each,
     keep the number of issues per batch as balanced as possible, and dispatch one subagent per batch.
     Run *at most 3 subagents in parallel* and queue the remaining batches in order to avoid rate limiting.
   - If the listing is truncated, tell the user and ask whether to narrow the time range
     or continue with further batches.
5. **Process every issue** with the per-issue procedure, acting only under the granted write permission.
6. **Report** the aggregated result (see below).

### Subagent Dispatching

1. Subagents start with a fresh context, so every subagent prompt must be self-contained:
   include its issue IDs, the time range, the scope and the write permission.
2. Subagents must have the reference to this SKILL (`sentry-triage-feedback`)
   and must understand that they are subagents.
3. If subagents are not available, you can process all of the issues by yourself.

### Final Report Format

After all batches finish, report:

- The time range, the total number of feedback, and how many were commented/resolved.
- A table: Issue | First seen | Release | Classification | Action | Cause (one line).
- The issues that could not be classified (missing attachments, ambiguous content), listed for manual review.

## Per-Issue Procedure

### Steps

1. Fetch the issue details and the latest event ID with `get_sentry_resource` (with `resourceType: "issue"`).
   Note the `Status`, the first-seen time and the event's `release`.
2. List the event's attachments with `get_event_attachment`.
   Download each `text/plain` attachment by calling the tool again with its `attachmentId`.
   If an attachment is stored as `application/octet-stream`, the tool returns no content,
   so fetch its signed download URL with `curl` instead.
3. Check the existing activity with `get_issue_activity` (with `includeComments: true`).
   If the issue is already resolved, or already carries the same comment, do not duplicate it.
4. Classify the issue by inspecting the log content, following the classification rules.
5. Act *only if* write permission was granted: post exactly the comment required by the rules, with no extra text,
   using `add_issue_note`. Then resolve it with `update_issue` (`status: "resolved"`) and *without* `reason`,
   since `reason` would post a second comment.
6. Record the per-issue result for the final report.

### Classification Rules

Judge from the attachment content, not merely from the task name.

| Classification | Evidence in the logs                                                                                                                                       | Action (only under write permission)   |
|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| Network issue  | A failure while downloading resources over the network: `UnknownHostException`, `Socket*Exception`, SSL handshake errors, target unreachable, and similar. | Comment `Network issue.` and resolve   |
| Unzip issue    | A failure while unpacking an already-downloaded archive: `ZipException`, unexpected EOF, truncated or corrupt zip, and similar.                            | Comment `Unzip issue.` and resolve     |
| Other          | Anything else, or logs unavailable (expired retention, empty upload)                                                                                       | No comment and no resolve; report only |

When a single log contains both kinds of failure, classify by the last failure before the feedback was uploaded, i.e. the one that prompted the user to report; do not reclassify because of earlier failures that were recovered from.

## Tool Cheat Sheet

| Purpose                         | Tool                                               | Key arguments                                                                                                                                                                           |
|---------------------------------|----------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Search feedback                 | `search_issues`                                    | `query: "issue.category:feedback"` (add `is:unresolved` to skip resolved ones) plus the scope filters (e.g. `environment:...` `project:...` `period:...`), `limit: 100`, `sort: "date"` |
| Issue details + latest event ID | `get_sentry_resource`                              | `resourceType: "issue"`, `organizationSlug: "..."`, `resourceId: "..."`                                                                                                                 |
| List / download attachments     | `get_event_attachment` (via `execute_sentry_tool`) | `organizationSlug: "..."`, `projectSlug: "..."`, `eventId`, optional `attachmentId`                                                                                                     |
| Read activity and comments      | `get_issue_activity` (via `execute_sentry_tool`)   | `issueUrl`, `includeComments: true`                                                                                                                                                     |
| Post a comment                  | `add_issue_note` (via `execute_sentry_tool`)       | `issueUrl`, `text`                                                                                                                                                                      |
| Resolve / reopen an issue       | `update_issue`                                     | `issueUrl` or `organizationSlug` + `issueId`, `status: "resolved"/"unresolved"`                                                                                                         |
| Discover other tools            | `search_sentry_tools`                              | keywords                                                                                                                                                                                |

Client note: some tools may be exposed directly with a server-name prefix
(e.g. `sentry_search_issues`, `sentry_get_sentry_resource`).
The remaining catalog tools are reached through `search_sentry_tools` and `execute_sentry_tool`.
Always prefer the exposed variants when present.

In the table above, every `...` should be replaced by the actual organization or resource,
either discovered or specified by the user.
