FROM postgres:16

# Scripts run on first startup (empty data volume)
COPY initdb/ /docker-entrypoint-initdb.d/
