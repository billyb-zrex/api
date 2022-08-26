.PHONY: teardown setup

include env.dev

# --------------------------------------------------------------
# Setup and teardown all  dependent services to run the server
# --------------------------------------------------------------
teardown:
	docker-compose stop

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
