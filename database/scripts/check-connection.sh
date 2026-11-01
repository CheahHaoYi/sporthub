#!/usr/bin/env sh
set -eu
: "${PGHOST:=localhost}" "${PGPORT:=5432}" "${PGDATABASE:=sporthub}" "${PGUSER:=sporthub}"
pg_isready -h "$PGHOST" -p "$PGPORT" -d "$PGDATABASE" -U "$PGUSER"
psql -v ON_ERROR_STOP=1 -c 'SELECT current_database(), current_user, CURRENT_TIMESTAMP;'
