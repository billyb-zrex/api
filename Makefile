.PHONY: teardown setup gen clean-data setup db-schema-migrate env

include env.now

teardown:
	docker-compose down;

clean-data:
	docker rm fable-db; docker rm fable-db-flyway; docker volume rm api_mysql-data

setup:
	docker-compose --profile ${APP_ENV} up -d

db-schema-migrate:
	docker-compose up schema;

gen:
	mvn process-classes
	cp -r ./gen/api-contract.d.ts ../app/workspace/packages/common/src/api-contract.ts

# --------------------------------------------------------------
# Different env file is required for different tool. Like idea
# needs env file in a different format which could be loaded via
# env plugin. This commands generate those file format.
# This is the first command that needs to be ran
# --------------------------------------------------------------
env:
	@echo "Generating env file"
	@echo "docker-compose version must be >= 1.28.0. docker-compose version found (see below)"
	@docker-compose --version

	@if [ "$(staging)" ]; then \
        cp env.staging env.now; \
        echo "[staging]"; \
    elif [ "$(dev)" ]; then \
        sed -r 's/^export[[:space:]]+//' env.dev > env.idea; \
        cp env.dev env.now; \
        echo "[dev]"; \
    elif [ "$(prod)" ]; then \
        cp env.prod env.now; \
        echo "[PROD]"; \
    else \
        echo "Not known. Allowed [ide, staging, dev, prod]"; \
    fi


# --------------------------------------------------------------
# Command for application build + execution setps
# --------------------------------------------------------------
build:
	mvn clean compile

run:
	mvn spring-boot:run
