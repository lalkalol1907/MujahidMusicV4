FROM oven/bun:1.2-alpine AS base
WORKDIR /app

FROM base AS install
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

FROM base AS release
COPY --from=install /app/node_modules ./node_modules
COPY package.json bun.lock tsconfig.json ./
COPY src ./src

ENV PORT=8080
EXPOSE 8080

CMD ["bun", "run", "src/index.ts"]
