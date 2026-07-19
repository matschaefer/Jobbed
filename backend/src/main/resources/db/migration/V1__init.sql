-- ============================================================================
-- V1 – Baseline
-- Aktiviert benötigte Extensions. Fachliche Tabellen folgen in späteren
-- Migrationen (Phase 3+). Migrationen sind nach dem Merge unveränderlich.
-- ============================================================================

-- pgcrypto stellt gen_random_uuid() bereit (UUID-Primärschlüssel).
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
