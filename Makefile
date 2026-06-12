.PHONY: test-all test-bot test-admin-api test-admin-web dev-compose lint

test-all: test-bot test-admin-api test-admin-web

test-bot:
	./gradlew test --no-daemon

test-admin-api:
	pip install -e "./admin-api[dev]"
	pytest admin-api/tests -q

test-admin-web:
	cd admin-web && npm ci && npm run build

dev-compose:
	docker compose up -d --build

lint:
	cd admin-api && ruff check src tests
	cd admin-web && npm run lint
