# ============================================================================
# Frontend – Multi-Stage-Build
# Stage 1: Angular-Produktionsbuild mit Node
# Stage 2: Auslieferung über nginx
# ============================================================================

# ----- Stage 1: Build -----
FROM node:20-alpine AS build
WORKDIR /app

# Abhängigkeiten cachen (npm ci wenn Lockfile vorhanden, sonst npm install)
COPY package.json package-lock.json* ./
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi

# Quellcode kopieren und bauen
COPY . .
RUN npm run build

# ----- Stage 2: Runtime (nginx) -----
FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/jobbed/browser /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
    CMD wget -q -O - http://127.0.0.1:80/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
