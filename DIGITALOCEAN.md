# DigitalOcean App Platform deployment

Deploy this repository as a **Web Service** component using the root
`Dockerfile`.

## Component settings

- Branch: `codex/digitalocean-core` (change to `develop` after merging)
- HTTP port: `8080`
- Health check: `/health`
- Custom domain: `api.demo.zrexsolutions.com`

## Required runtime variables

```text
APP_ENV=prod
APP_URL=https://demo.zrexsolutions.com
API_PUBLIC_ENDPOINT=https://api.demo.zrexsolutions.com
AUTH0_AUDIENCE=https://api.demo.zrexsolutions.com
AUTH0_ISSUER_URI=https://zrexsolutions-demo.us.auth0.com/

DB_CONN_URL=jdbc:mysql://<mysql-host>:<mysql-port>
DB_USER=<mysql-user>
DB_PWD=<mysql-password>
ANALYTICS_DB_CONN_URL=jdbc:postgresql://<postgres-host>:<postgres-port>/<database>?sslmode=require
ANALYTICS_DB_USER=<postgres-user>
ANALYTICS_DB_PWD=<postgres-password>

AWS_ACCESS_KEY_ID=<spaces-access-key>
AWS_SECRET_ACCESS_KEY=<spaces-secret-key>
AWS_S3_REGION=<spaces-region>
AWS_S3_ENDPOINT=https://<spaces-region>.digitaloceanspaces.com
AWS_S3_PATH_STYLE_ACCESS_ENABLED=false
ASSET_BUCKET_NAME=<space-name>
ASSET_CDN=<space-name>.<spaces-region>.digitaloceanspaces.com
PVT_ASSET_BUCKET_NAME=<space-name>
PVT_ASSET_BUCKET_REGION=<spaces-region>
PVT_ASSET_ENDPOINT=https://<spaces-region>.digitaloceanspaces.com

JOBS_ENABLED=false
SENTRY_DSN=
CB_SITE_NAME=
CB_API_KEY=
COBALT_API_KEY=
HUBSPOT_CLIENT_SECRET=
APPSUMO_CLIENT_ID=
APPSUMO_CLIENT_SECRET=
```

Use encrypted App Platform variables for database passwords and Spaces keys.

## Database migrations

The API does not run Flyway automatically. Before the first API deployment,
run both migration images as one-time jobs:

```text
Dockerfile.migrate-api
  flyway -url=jdbc:mysql://<host>:<port>/fable_tour_app -schemas=migration -user=<user> -password=<password> migrate

Dockerfile.migrate-analytics
  flyway -url=jdbc:postgresql://<host>:<port>/<database> -schemas=migration -user=<user> -password=<password> migrate
```

The core deployment deliberately treats SQS as optional. Calls that would
enqueue AI, media-transcoding, integration, or subscription side-effect jobs
are logged and skipped while `JOBS_ENABLED=false`.
