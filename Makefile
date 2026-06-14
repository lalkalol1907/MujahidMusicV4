.PHONY: test-all test-bot test-admin-api test-admin-web dev-compose lint

test-all: test-bot test-admin-api test-admin-web

test-bot:
	./gradlew test --no-daemon

test-admin-api:
	cd admin-api && bun install --frozen-lockfile && bun test

test-admin-web:
	cd admin-web && npm ci && npm run build

dev-compose:
	docker compose up -d --build

lint:
	cd admin-api && bun run lint
	cd admin-web && npm run lint
