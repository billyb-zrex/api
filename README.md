# api

### Checkout [High Level Design](https://www.notion.so/sharefable/High-Level-Design-32386c475c0748a5ba784d19b9a94499)
### Checkout [Common Project Information](https://github.com/sharefable/dev-docs/blob/master/README.md)

## General 
- Checkout the _Makefile_ for detailed running instructions. [Read more about why we use Makefile](https://www.notion.so/sharefable/Why-use-Makefile-24c83d9f6f5d4187b2734626beb01fe1)
- _dev/*.http_ files for http request response from IntelliJ IDEA (We don't need a different UI tool like postman)
- Service dependencies _docker-compose.yml_
- The _entity_ classes use mysql `auto increment` for id. [Ref](https://stackoverflow.com/a/4103347).
- ~~Can't use elasticsearch 8.* cluster as `RestHighLevelClient` is deprecated and has issues. [Read more about it here.](https://github.com/spring-projects/spring-data-elasticsearch#about-elasticsearch-versions-and-clients)~~. We use ElasticSearch native client for compatibility & flexibility.

## Environment Variables

This project requires couple of env variable to be present before we fire the makefile commands.

Each env requires it's own _env.{{env_name}}_ file. For the following environment the following files should be
present. These files are not checked in anywhere.
```text
dev -> env.dev
staging -> env.staging
prod -> env.prod
idea -> env.idea  # For running from Intellij IDEA
```

Each file contains same set of variables to be exported to the service.

```bash
export APP_ENV=dev | staging | prod
export DB_USER=<>
export DB_PWD=<>
# Example value for local
export DB_CONN_URL_DOCKER_COMPOSE=jdbc:mysql://host.docker.internal:3306
# Example value for local
export DB_CONN_URL=jdbc:mysql://localhost:3306
export ASSET_BUCKET_NAME=<>
export AWS_ACCESS_KEY_ID=<>
export AWS_SECRET_ACCESS_KEY=<>
export AWS_S3_REGION=ap-south-1
export AWS_S3_ENDPOINT=https://s3.ap-south-1.amazonaws.com
export ES_ENDPOINT=<>
export ES_PORT=<>
```

`APP_ENV` is mandatory and needs to be present for all env files. The values are predefined `dev | staging | prod`
based on the environment. These values are in turned used to activate profiles from _docker-compose.yml_ file. (Check out
the profile property)

### Commands

Check out _Makefile_ for more detailed capabilities.

# IDE setup

- Use IntelliJ
- Install java 8 and maven 3.6.*
- Use plugin EnvFile. An _env.idea_ file with all the secrets can be generated from `make env dev=1` command
- `spring-boot-devtools` is already added as dependency. [Set up the IDE properly](https://www.youtube.com/watch?v=uv-Mku3l0ls) to make auto reloading works. [See this](https://youtrack.jetbrains.com/issue/IDEA-274903/In-IntelliJ-20212-compilerautomakeallowwhenapprunning-disappear-Unable-to-enable-live-reload-under-Spring-boot) for Intellij 2022.

# Manual deployment in staging server

- Create a box in aws and configure your ssh client for fast & easy access to the box. You can do `ssh fab-api` post this settings
```text
...

Host fab-api
  HostName <elastic ip>
  User ubuntu
  IdentityFile ~/.ssh/fab.pem
  
...

```

- All the required files are in _aws/_ dir
- Run a tmux session to run the servers. We should ideally run it via `systemctl` services, but we currently use tmux so that we get hold of the logs easily as `journalctl` truncates logs. **This is a temporary step.** Upload the tmux file for easier navigation
```bash
scp aws/.tmux.conf fab-api:~/.
```
- Use the commands in _aws/bootstrap.sh_ file to set up env + install toolchains
- Once done you can start running the _Makefile_ scripts
- Create elastic search indexes from _dev/es.http_ file
