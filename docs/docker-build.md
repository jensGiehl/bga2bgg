# Docker build & publish pipeline

Every push to `main` or `master` builds the application, runs the test suite,
packages it as a Docker image and publishes that image to the
[GitHub Container Registry (GHCR)](https://ghcr.io).

## What runs where

| File | Purpose |
| --- | --- |
| `Dockerfile` | Multi-stage build: compiles the Spring Boot jar with JDK 25, then ships it on a slim JRE 25 runtime as an unprivileged user. |
| `.dockerignore` | Keeps the build context small (excludes `target/`, IDE files, docs, etc.). |
| `.github/workflows/docker-publish.yml` | CI pipeline: `test` job → `build-and-push` job. |

The image is built with Spring Boot **layered jars**, so dependency layers are
cached separately from application code and rebuilds/pulls stay small.

## The pipeline

1. **`test`** — sets up JDK 25 and runs `./mvnw verify`. (Playwright UI tests
   stay skipped by default, exactly as in local builds.)
2. **`build-and-push`** — only runs if tests pass. It logs in to GHCR using the
   built-in `GITHUB_TOKEN`, then builds and pushes the image.

### Image name and tags

The image is published as:

```
ghcr.io/<owner>/<repo>        # e.g. ghcr.io/jensgiehl/bga2bgg
```

with these tags on every default-branch push:

- `latest` — always the newest default-branch build.
- `sha-<commit>` — the exact commit, for pinning/rollbacks.

## Required setup

Almost nothing — the pipeline uses the automatically provided `GITHUB_TOKEN`,
so **no secrets need to be configured**. Two one-time checks:

1. **Actions may write packages.** Repo → *Settings → Actions → General →
   Workflow permissions* → ensure "Read and write permissions" is enabled (or
   the workflow's `permissions: packages: write` block is honored, which it is
   by default on personal repos).

2. **Package visibility.** The first successful run creates the package as
   **private**. To let the server pull it without credentials, make it public:
   GitHub profile → *Packages* → the package → *Package settings* → *Change
   visibility → Public*. If you prefer to keep it private, see the registry
   authentication section in [`server-setup.md`](./server-setup.md).

## Using the image

Pull and run locally:

```bash
docker pull ghcr.io/jensgiehl/bga2bgg:latest
docker run --rm -p 8080:8080 ghcr.io/jensgiehl/bga2bgg:latest
# open http://localhost:8080
```

Build the image locally (without CI):

```bash
docker build -t bga2bgg:local .
docker run --rm -p 8080:8080 bga2bgg:local
```

## Deploying it to a server

See [`server-setup.md`](./server-setup.md) for running published images on a
Hetzner Linux host, where a shared Traefik reverse proxy fronts **many apps at
once**, each at its own subdomain over HTTPS, with the old container replaced
automatically on each new push.

The image name here is what you plug into an app's `.env` there:

```
ghcr.io/jensgiehl/bga2bgg:latest   →   APP_IMAGE=ghcr.io/jensgiehl/bga2bgg:latest
                        ^^^^^^^                    APP_NAME=bga2bgg
                        repo = app name            (→ https://bga2bgg.example.com)
```

Each app gets its own `deploy/app` folder + `.env`; the `APP_NAME` becomes the
subdomain label. See [`server-setup.md`](./server-setup.md) § "Adding another
app".
