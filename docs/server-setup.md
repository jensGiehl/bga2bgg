# Server setup — running many app containers on one host with HTTPS

This guide sets up a single Hetzner (or any Linux) server so that:

- **several** Docker images published by CI run side by side as containers,
- **each** is reachable at its **own subdomain over HTTPS**, with certificates
  obtained and renewed automatically,
- and on every new push, the **old container is stopped and the new image is
  rolled out automatically**.

The design separates two concerns:

- **Infra stack** — one shared **reverse proxy ([Traefik](https://traefik.io/traefik/))**
  that terminates TLS and routes each subdomain to the right container, plus an
  **auto-updater ([Watchtower](https://containrrr.dev/watchtower/))** that swaps
  containers when a new image appears. You bring this up **once**.
- **App stacks** — one small Compose file **per app**, deployed and updated
  independently. Adding the Nth app never touches the others.

Everything you need is in the [`deploy/`](../deploy) folder:

```
deploy/
  infra/   docker-compose.yml + .env.example   → Traefik + Watchtower (once)
  app/     docker-compose.yml + .env.example   → template, copy per app
```

---

## Your options at a glance

| Concern | Recommended | Alternatives |
| --- | --- | --- |
| Reverse proxy + auto HTTPS | **Traefik** (Docker-label driven) | Caddy, `nginx-proxy` + `acme-companion` |
| Rolling out new images | **Watchtower** (server pulls) | GitHub Actions SSH deploy (CI pushes) |
| TLS challenge | **TLS-ALPN-01** (only needs :443) | HTTP-01 (:80), DNS-01 (wildcard certs) |
| DNS for many apps | **Wildcard `*.example.com`** | One A record per subdomain |

The rest of this document uses the recommended path. The
[SSH-deploy alternative](#alternative-push-based-deploy-from-ci) is described at
the end.

---

## 1. DNS

Because every app gets its own subdomain, point a **wildcard** record at the
server once — then new apps need no further DNS changes:

```
A     *.example.com    ->  <SERVER_PUBLIC_IP>
AAAA  *.example.com    ->  <SERVER_IPV6>        (optional)
```

> Let's Encrypt validates each subdomain, so DNS must resolve **before** the
> first start of each app. The wildcard *record* above is enough for the
> per-subdomain certificates used here (each `app.example.com` gets its own
> cert). A wildcard *certificate* would additionally require the DNS-01
> challenge, which we don't use.

If you prefer, you can still add one `A` record per app instead of the wildcard.

---

## 2. Prepare the server

SSH in as a sudo-capable user and install Docker Engine + the Compose plugin:

```bash
# Docker's convenience script (Debian/Ubuntu, which Hetzner images use)
curl -fsSL https://get.docker.com | sh

# Manage Docker as your user (log out/in afterwards)
sudo usermod -aG docker "$USER"
```

Open only the web ports on the firewall:

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

If you use the **Hetzner Cloud Firewall** instead of `ufw`, allow inbound
`22`, `80` and `443` there.

---

## 3. Deploy the infra stack (once)

Copy the `deploy/infra/` folder to the server and start it:

```bash
# from your machine, in the repo root
scp -r deploy/infra user@<SERVER_PUBLIC_IP>:~/infra
```

On the server:

```bash
cd ~/infra
cp .env.example .env
nano .env          # set ACME_EMAIL
docker compose up -d
```

This starts Traefik and Watchtower and **creates the shared `web` network**
that every app joins. Check it:

```bash
docker compose ps
docker compose logs -f traefik   # watch certificate issuance
docker network ls | grep web     # the shared network exists
```

You only repeat this step to update Traefik/Watchtower themselves.

---

## 4. Deploy an app

Each app is its own folder with a copy of the `deploy/app/` template. Copy it
to the server under `~/apps/<name>/`:

```bash
# from your machine, in the repo root
scp -r deploy/app user@<SERVER_PUBLIC_IP>:~/apps/bga2bgg
```

On the server:

```bash
cd ~/apps/bga2bgg
cp .env.example .env
nano .env          # set APP_IMAGE, APP_NAME, BASE_DOMAIN
docker compose up -d
```

The subdomain is **built from the app name**: with `APP_NAME=bga2bgg` and
`BASE_DOMAIN=example.com`, Traefik serves the app at
`https://bga2bgg.example.com` and requests its certificate on first start.

```bash
docker compose ps
docker compose logs -f app
```

### How the pieces fit together

- **Traefik** (infra stack) listens on 80/443, redirects HTTP→HTTPS, and reads
  container labels to know which host maps to which service. Each app container
  carries labels keyed by `APP_NAME` so multiple apps never clash:

  ```
  traefik.http.routers.<APP_NAME>.rule=Host(`<APP_NAME>.<BASE_DOMAIN>`)
  traefik.http.routers.<APP_NAME>.tls.certresolver=le
  traefik.http.services.<APP_NAME>.loadbalancer.server.port=8080
  ```

- **The app** exposes port 8080 only on the shared `web` network — it is never
  published directly to the internet.

- **Watchtower** (infra stack) polls the registry every 5 minutes. When CI
  pushes a new `:latest`, it pulls the image, stops the old container and starts
  a new one (`--cleanup` removes the old image). Only containers labelled
  `com.centurylinklabs.watchtower.enable=true` are touched — which the template
  sets for you.

---

## 5. Adding another app

This is the everyday operation. No existing app or the infra stack is touched:

```bash
# copy the template to a new folder (locally, then scp — or scp then rename)
scp -r deploy/app user@<SERVER_PUBLIC_IP>:~/apps/otherapp

# on the server
cd ~/apps/otherapp
cp .env.example .env
nano .env          # APP_IMAGE=ghcr.io/jensgiehl/otherapp:latest
                   # APP_NAME=otherapp   BASE_DOMAIN=example.com
docker compose up -d
# → https://otherapp.example.com is live, certificate issued automatically
```

With the wildcard DNS record from step 1 there is **no DNS change** to make.
Traefik issues a separate certificate per host automatically.

To stop or remove a single app, work in its folder only:

```bash
cd ~/apps/otherapp
docker compose down          # stop & remove just this app
```

---

## 6. Automatic rollout on every push

With the infra stack running and an app deployed, the end-to-end flow is:

```
git push origin main
   → GitHub Actions builds & pushes ghcr.io/jensgiehl/bga2bgg:latest
   → Watchtower (≤5 min later) pulls it, stops the old container,
     starts the new one
   → Traefik keeps serving https://bga2bgg.example.com (cert reused)
```

No manual step on the server. To force an immediate update instead of waiting
for the poll interval, run it in that app's folder:

```bash
cd ~/apps/bga2bgg
docker compose pull app && docker compose up -d app
```

### Private registry (only if the package is NOT public)

If you keep the GHCR package private, the server must authenticate before it
can pull:

1. Create a GitHub **Personal Access Token (classic)** with the `read:packages`
   scope.
2. Log in on the server:

   ```bash
   echo <TOKEN> | docker login ghcr.io -u <github-username> --password-stdin
   ```

   This writes `/root/.docker/config.json` (or `~/.docker/config.json`).
3. Uncomment the config-mount line in `deploy/infra/docker-compose.yml` so
   Watchtower can reuse those credentials:

   ```yaml
   - /root/.docker/config.json:/config.json:ro
   ```

Making the package public (see `docker-build.md`) avoids all of this.

---

## Alternative: push-based deploy from CI

If you'd rather have CI push the deployment (instant, no polling, and it works
without exposing anything extra), replace Watchtower with an SSH step at the end
of the workflow. Keep Traefik for HTTPS.

1. Create an SSH key pair; put the **public** key in the server's
   `~/.ssh/authorized_keys`.
2. Add repository secrets: `SSH_HOST`, `SSH_USER`, `SSH_KEY` (private key).
3. Append a job to `.github/workflows/docker-publish.yml`, pointing at the
   app's own folder:

   ```yaml
     deploy:
       needs: build-and-push
       runs-on: ubuntu-latest
       steps:
         - name: Deploy over SSH
           uses: appleboy/ssh-action@v1
           with:
             host: ${{ secrets.SSH_HOST }}
             username: ${{ secrets.SSH_USER }}
             key: ${{ secrets.SSH_KEY }}
             script: |
               cd ~/apps/bga2bgg
               docker compose pull app
               docker compose up -d app
               docker image prune -f
   ```

`docker compose up -d app` recreates the container only if the image changed,
so the old one is stopped and replaced automatically. Traefik is untouched and
keeps serving TLS.

---

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Certificate not issued | `docker compose logs traefik` (in `~/infra`); confirm DNS resolves to the host and ports 80/443 are open. Let's Encrypt has rate limits — avoid rapid restarts. |
| 404 from Traefik | The app's `Host(...)` label must exactly match the domain you open (`APP_NAME.BASE_DOMAIN`); the app and Traefik must share the `web` network. |
| `network web not found` | The infra stack isn't up, or was brought up after the app. Start `~/infra` first (it owns `web`), then the app. |
| Router name conflict | Two apps share the same `APP_NAME`. It must be unique per host — it keys both the subdomain and the Traefik router. |
| New image not picked up | `docker compose logs watchtower` (in `~/infra`); for a private registry verify the config-mount and `docker login`. |
| App up but 502 | The app container must listen on 8080 and be healthy (`docker compose ps`). |

## Maintenance

```bash
# update Traefik/Watchtower themselves
cd ~/infra && docker compose pull && docker compose up -d

# update / restart a single app
cd ~/apps/<name> && docker compose pull && docker compose up -d

docker system prune -f     # reclaim disk from old images
```
