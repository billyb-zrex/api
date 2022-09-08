# api

[Common Project Information](https://github.com/sharefable/dev-docs/blob/master/README.md)

- Checkout the _Makefile_ for detailed running instructions.
- _dev/api.http_ file for http request response

## General 

- The _entity_ classes use mysql `auto increment` for id. [Ref](
https://stackoverflow.com/a/4103347).

# IDE setup

- Use IntelliJ
- Use plugin EnvFile. An _env.idea_ file with all the secrets can be generated from `make envgen` command
- `spring-boot-devtools` is already added as dependency. [Set up the IDE properly](https://www.youtube.com/watch?v=uv-Mku3l0ls) to make auto reloading works. [See this](https://youtrack.jetbrains.com/issue/IDEA-274903/In-IntelliJ-20212-compilerautomakeallowwhenapprunning-disappear-Unable-to-enable-live-reload-under-Spring-boot) for Intellij 2022.
