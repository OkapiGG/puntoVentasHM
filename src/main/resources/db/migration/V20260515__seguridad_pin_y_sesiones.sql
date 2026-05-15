-- Refuerzo de seguridad: PINs hasheados con BCrypt + tokens de sesión
-- Idempotente: usa IF NOT EXISTS / verifica longitud existente.

-- 1. Ampliar pin_acceso para acomodar hashes BCrypt (60 chars)
ALTER TABLE usuarios ALTER COLUMN pin_acceso TYPE VARCHAR(100);

-- 2. Crear tabla de sesiones (tokens Bearer)
CREATE TABLE IF NOT EXISTS sesiones_usuario (
    id_sesion_usuario BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    creada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_en TIMESTAMP NOT NULL,
    ultimo_uso TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sesiones_usuario_token_hash ON sesiones_usuario (token_hash);
CREATE INDEX IF NOT EXISTS idx_sesiones_usuario_id_usuario ON sesiones_usuario (id_usuario);
