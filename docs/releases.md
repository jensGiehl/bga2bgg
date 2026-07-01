# Releasing & versioning

Versioning is **fully automated** with
[release-please](https://github.com/googleapis/release-please). You never edit
the version in `pom.xml` by hand and you never create a git tag manually — both
are derived from your commit messages. A release happens when you merge the
"Release PR" that the bot keeps open for you.

## What runs where

| File | Purpose |
| --- | --- |
| `release-please-config.json` | release-please setup: `maven` release type (bumps `pom.xml`), tag format `vX.Y.Z`, changelog file. |
| `.release-please-manifest.json` | The current released version. Must match the `<version>` in `pom.xml`. |
| `.github/workflows/release.yml` | The release pipeline: `release-please` → `publish-jar` + `publish-docker`. |
| `.github/workflows/docker-publish.yml` | Separate, continuous image build (`latest` + `sha`) on every `main` push — see [`docker-build.md`](./docker-build.md). |

## How a new version is created

Everything is driven by [Conventional Commits](https://www.conventionalcommits.org/).

1. **You push commits to `main`** using the conventional prefix that describes
   the change:

   | Commit prefix | Example | Version effect (while < 1.0.0) |
   | --- | --- | --- |
   | `fix:` | `fix: correct player name mapping` | Patch — `0.1.0 → 0.1.1` |
   | `feat:` | `feat: add German UI translation` | Minor — `0.1.0 → 0.2.0` |
   | `feat!:` / `BREAKING CHANGE:` | `feat!: drop legacy export format` | Minor (see note below) |
   | `chore:`, `docs:`, `refactor:`, `test:`, `ci:` … | `docs: update readme` | No release |

2. **release-please opens (or updates) a "Release PR"** automatically. This PR
   contains the bumped `<version>` in `pom.xml` and a generated `CHANGELOG.md`
   entry listing every change since the last release. It stays open and keeps
   updating itself as you push more commits.

3. **You merge the Release PR.** That is the moment a release is cut:
   release-please creates the git tag `vX.Y.Z` and a matching **GitHub
   Release** with the changelog as its notes.

4. **The publish jobs run automatically** (same workflow run):
   - `publish-jar` builds the application with `./mvnw verify` and attaches
     the built `*.jar` to the GitHub Release as a downloadable asset.
   - `publish-docker` builds and pushes the versioned Docker image (see below).

So the full loop is: **conventional commits → merge the Release PR → tag,
GitHub Release, JAR and Docker image appear.** No manual version bump, no
manual tagging.

### Note on versions below 1.0.0

The config sets `bump-minor-pre-major` and `bump-patch-for-minor-pre-major`, so
while the project is still in the `0.x` range a breaking change bumps the
**minor** version instead of jumping to `1.0.0`. When you're ready to commit to
a stable `1.0.0` API, remove those two flags from `release-please-config.json`.

## When a Docker image is published

There are **two** independent triggers, on purpose:

| Trigger | Workflow | Image tag(s) | When |
| --- | --- | --- | --- |
| Every push to `main` | `docker-publish.yml` | `latest`, `sha-<commit>` | Continuously — always reflects the newest build. |
| A release is cut (Release PR merged) | `release.yml` → `publish-docker` | `X.Y.Z` (e.g. `0.2.0`) | Only on an actual version release. |

All images land in the same GHCR repository:

```
ghcr.io/jensgiehl/bga2bgg:latest    # newest main build (moving)
ghcr.io/jensgiehl/bga2bgg:sha-…     # a specific commit
ghcr.io/jensgiehl/bga2bgg:0.2.0     # a specific release (immutable, pinnable)
```

Use `latest` for "always current", a version tag like `0.2.0` for reproducible
deployments and rollbacks. See [`docker-build.md`](./docker-build.md) for the
image internals and [`server-setup.md`](./server-setup.md) for deploying it.

## Required setup (one-time)

release-please uses the built-in `GITHUB_TOKEN` — **no secrets to configure**.
Two repo settings must be enabled, though (Repo → *Settings → Actions → General
→ Workflow permissions*):

1. **Read and write permissions** — so the bot can create tags and releases.
2. **Allow GitHub Actions to create and approve pull requests** — so it can open
   the Release PR.

## FAQ

**Nothing happened after I pushed.** Only `fix:`/`feat:`/breaking commits
produce a release entry. `chore:`, `docs:`, etc. are recorded but don't bump the
version. Check that a Release PR exists.

**I want to release several merged changes at once.** That's the default — keep
merging feature/fix PRs to `main`; the single Release PR accumulates them all.
Merge it when you want to ship.

**How do I force a specific version?** Add a `Release-As: 1.4.0` line to a commit
body (empty commit is fine): `git commit --allow-empty -m "chore: release 1.4.0" -m "Release-As: 1.4.0"`.
