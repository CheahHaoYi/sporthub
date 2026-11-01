#!/usr/bin/env sh
set -eu
# DESTRUCTIVE: permanently deletes the local SportHub PostgreSQL volume and all data in it.
printf '%s\n' 'WARNING: this deletes ALL local SportHub database data and the named volume.'
if [ "${1:-}" != "--confirm-delete-local-data" ]; then
  printf '%s\n' 'To proceed, rerun with --confirm-delete-local-data.' >&2
  exit 2
fi
docker compose -f database/docker-compose.yml down -v
