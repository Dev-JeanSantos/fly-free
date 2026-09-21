-- V1: Criação das tabelas iniciais do fly-free

CREATE TABLE routes (
    id           BIGSERIAL PRIMARY KEY,
    origin       VARCHAR(3)  NOT NULL,
    destination  VARCHAR(3)  NOT NULL,
    travel_date  DATE        NOT NULL,
    active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_routes UNIQUE (origin, destination, travel_date)
);

CREATE TABLE flight_results (
    id              BIGSERIAL PRIMARY KEY,
    route_id        BIGINT          NOT NULL REFERENCES routes(id),
    flight_number   VARCHAR(10)     NOT NULL,
    departure_time  TIMESTAMP       NOT NULL,
    arrival_time    TIMESTAMP       NOT NULL,
    price           NUMERIC(10, 2)  NOT NULL,
    stops           INT             NOT NULL DEFAULT 0,
    fare_class      VARCHAR(50),
    raw_json        TEXT,
    synced_at       TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE best_offers (
    id               BIGSERIAL PRIMARY KEY,
    route_id         BIGINT          NOT NULL UNIQUE REFERENCES routes(id),
    flight_result_id BIGINT          NOT NULL REFERENCES flight_results(id),
    price            NUMERIC(10, 2)  NOT NULL,
    updated_at       TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE sync_logs (
    id              BIGSERIAL PRIMARY KEY,
    status          VARCHAR(20)  NOT NULL,
    routes_synced   INT          NOT NULL DEFAULT 0,
    routes_failed   INT          NOT NULL DEFAULT 0,
    duration_ms     BIGINT,
    error_message   TEXT,
    started_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Índices para performance
CREATE INDEX idx_flight_results_route_id ON flight_results(route_id);
CREATE INDEX idx_flight_results_price    ON flight_results(price);
CREATE INDEX idx_best_offers_route_id    ON best_offers(route_id);
CREATE INDEX idx_sync_logs_started_at   ON sync_logs(started_at DESC);
