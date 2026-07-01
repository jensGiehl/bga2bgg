# Dependabot & Auto-merge

This repository uses [GitHub Dependabot](https://docs.github.com/code-security/dependabot)
to keep dependencies up to date automatically, with optional auto-merge for
low-risk (patch/minor) updates.

## What is kept up to date

Configuration lives in [`.github/dependabot.yml`](../.github/dependabot.yml).
Three ecosystems are watched, all on a **weekly** schedule (Monday 06:00 Europe/Berlin):

| Ecosystem        | What it updates                                   | Source        |
| ---------------- | ------------------------------------------------- | ------------- |
| `maven`          | Spring Boot & other Java dependencies             | `pom.xml`     |
| `github-actions` | Action versions used in `.github/workflows/*.yml` | workflow files |
| `docker`         | Base images (`eclipse-temurin:25-*`)              | `Dockerfile`  |

To reduce PR noise, updates are **grouped**:

- Maven: one PR for `spring-*` packages, one PR for all remaining minor/patch bumps.
- GitHub Actions: a single PR for all action updates.

## Enabling Dependabot in GitHub

Version updates (the `dependabot.yml` above) are enabled automatically once the
file is on the default branch — no UI toggle required.

To also get **security updates** (patches for known CVEs) and the dependency
graph, enable them once in the repository settings:

1. Go to **Settings → Advanced Security** (or **Code security**).
2. Enable **Dependency graph**.
3. Enable **Dependabot alerts**.
4. Enable **Dependabot security updates**.

## Auto-merge

The workflow [`.github/workflows/dependabot-auto-merge.yml`](../.github/workflows/dependabot-auto-merge.yml)
approves each Dependabot PR and turns on GitHub's **auto-merge** for
**patch and minor** updates. Major updates are intentionally left for a human to
review.

Auto-merge only *queues* the merge — GitHub completes it once all required
status checks pass. This means the merge is still gated on CI being green.

### Required repository configuration

Auto-merge does not work out of the box. Configure the following once:

1. **Allow auto-merge**
   Settings → General → *Pull Requests* → check **Allow auto-merge**.

2. **Allow Actions to approve PRs**
   Settings → Actions → General → *Workflow permissions* →
   check **Allow GitHub Actions to create and approve pull requests**.
   (Required for the auto-approve step.)

3. **Add a branch protection rule** for the default branch
   Settings → Branches → *Add rule*:
   - **Require status checks to pass before merging** and select your CI check(s).
   - Optionally **Require a pull request before merging** with 1 approval — the
     workflow's auto-approve step satisfies this for Dependabot PRs.

   Without a required status check, auto-merge would merge immediately without
   waiting for CI.

### CI on pull requests

The existing workflows (`docker-publish.yml`, `release.yml`) only trigger on
`push` to `main`/`master`, so they do **not** run on pull requests. To give
branch protection something to gate on, a dedicated workflow
[`.github/workflows/pr-build.yml`](../.github/workflows/pr-build.yml) runs on
every pull request (including Dependabot PRs) and simply **builds the
application and runs the tests**:

```
./mvnw --batch-mode --no-transfer-progress verify
```

Select the **`build-and-test`** job from this workflow as a **required status
check** in branch protection (step 3 above). Once it passes on a patch/minor
Dependabot PR, auto-merge completes automatically.

> **Note on secrets:** Dependabot PRs run with a read-only `GITHUB_TOKEN` and
> **cannot access repository secrets** by default. The auto-merge workflow only
> uses the default token, so it works without extra setup. If a *CI* job on a
> Dependabot PR needs secrets, expose them via
> **Settings → Secrets → Dependabot** rather than Actions secrets.

## Day-to-day usage

- **Patch/minor PRs**: open → CI runs → auto-approved → auto-merged when green.
  No action needed.
- **Major PRs**: open → CI runs → **wait for manual review & merge**.
- To pause updates for a package, add an `ignore` entry in `dependabot.yml`.
- To trigger Dependabot manually, open the **Insights → Dependency graph →
  Dependabot** tab and use *Check for updates*.
