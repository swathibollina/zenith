# Drives the local stack. `make help` (or just `make`) lists everything.
# Make's one hard rule: recipe lines start with a TAB, never spaces.

COMPOSE := docker compose -f deploy/compose/docker-compose.infra.yml

# Overridable from the command line: make up profile=core
profile ?= full

.DEFAULT_GOAL := help
.PHONY: help up down reset logs ps psql redis-cli topics test fmt smoke seed ci cd demo

help: ## this menu
	@grep -E '^[a-zA-Z_-]+:.*?##' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "} {printf "  make %-10s %s\n", $$1, $$2}'

up: ## start the stack and wait for healthy (make up profile=core|events|media|full)
	$(COMPOSE) --profile $(profile) up -d --wait
	@case "$(profile)" in media|full) $(COMPOSE) run --rm minio-init ;; esac

down: ## stop and remove containers; data survives in the volumes
	$(COMPOSE) down --remove-orphans

reset: ## DESTRUCTIVE factory reset: containers AND volumes gone
	$(COMPOSE) down -v --remove-orphans

logs: ## follow logs (make logs s=kafka for one service)
	$(COMPOSE) logs -f $(s)

ps: ## status of every service
	$(COMPOSE) ps -a

psql: ## interactive SQL shell in Postgres
	docker exec -it infra-postgres-1 psql -U postgres -d app

redis-cli: ## interactive Redis shell
	docker exec -it infra-redis-1 redis-cli

topics: ## list Kafka topics
	docker exec infra-kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# ---------------------------------------------------------------------------
# Placeholders: the menu shows the destination before the road is built.
# Each becomes real in a named task; until then it prints one honest line.
# ---------------------------------------------------------------------------

test: ## run the Java tests
	@echo "placeholder: real once the first Maven module lands (Phase 0)"

fmt: ## format the code
	mvn spotless:apply

smoke: ## end-to-end smoke test against the running stack
	@echo "placeholder: real in Task 1.11 - the Testcontainers harness"

seed: ## load demo data (will take scale=small|full from Task 3.1 on)
	@echo "placeholder: real in Task 3.1 - realistic data volume and a measured baseline"

ci: ## run the whole CI pipeline locally
	@echo "placeholder: real in Task 0.4 - git workflow, the remote, and a local CI pipeline"

cd: ## deploy pipeline
	@echo "placeholder: real in Task 13.4 - CI/CD pipeline"

demo: ## bring up the demo path end to end
	@echo "placeholder: real in Task 14.3 - demo path and interview preparation"