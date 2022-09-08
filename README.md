# api

[Common Project Information](https://github.com/sharefable/dev-docs/blob/master/README.md)

- Checkout the _Makefile_ for detailed running instructions.
- _dev/api.http_ file for http request response

## General 

- The _entity_ classes use mysql `auto increment` for id. [Ref](https://stackoverflow.com/a/4103347).

## Env variables

This project requires couple of env variable to be present before we fire the makefile commands.
Save these variables in a file called `env.dev` and then run `make envgen` to generate env files for intellij
```
export DB_USER=<>
export DB_PWD=<>
export DB_HOST=jdbc:mysql://localhost:3306
export ASSET_BUCKET_NAME=proxy-asset-2
export AWS_ACCESS_KEY_ID=<>
export AWS_SECRET_ACCESS_KEY=<>
export AWS_S3_REGION=ap-south-1
```

# IDE setup

- Use IntelliJ
- Install java 8 and maven 3.6.*
- Use plugin EnvFile. An _env.idea_ file with all the secrets can be generated from `make envgen` command
- `spring-boot-devtools` is already added as dependency. [Set up the IDE properly](https://www.youtube.com/watch?v=uv-Mku3l0ls) to make auto reloading works. [See this](https://youtrack.jetbrains.com/issue/IDEA-274903/In-IntelliJ-20212-compilerautomakeallowwhenapprunning-disappear-Unable-to-enable-live-reload-under-Spring-boot) for Intellij 2022.
