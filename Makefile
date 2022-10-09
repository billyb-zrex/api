.PHONY: teardown setup

include env.dev

# -----------------------------------------------------------------
# Setup and teardown all  dependent services to run the server
# While teardown use use `make teardown clean=1` to delete db data
#
# `make setup` keeps db data across service restarts
# `make fullsetup` resets db data acorss service restarts
# -----------------------------------------------------------------
teardown:
	@if [ -z "$(clean)" ]; then \
  		docker-compose down; \
    else \
        docker-compose down; docker rm fable-db; \
    fi


setup: teardown
	docker-compose up -d


# --------------------------------------------------------------
# Different env file is required for different tool. Like idea
# needs env file in a different format which could be loaded via
# envfile plugin. This commands generate those file format
# --------------------------------------------------------------
envgen:
	sed -r 's/^export[[:space:]]+//' env.dev > env.idea


# --------------------------------------------------------------
# Command for application build + execution setps
# --------------------------------------------------------------
build:
	mvn clean compile

run:
	mvn spring-boot:run
