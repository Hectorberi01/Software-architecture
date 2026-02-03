-- Flyway migration V3: add users table and link reservations to users

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Temporary nullable column to backfill data before enforcing NOT NULL
ALTER TABLE reservations
    ADD COLUMN user_id BIGINT NULL AFTER parking_spot_id;

-- Backfill existing reservations by creating placeholder users when needed
INSERT INTO users (email, password_hash, first_name, last_name)
SELECT DISTINCT r.user_email,
       '$2a$10$7EqJtq98hPqEX7fNZaFWoOa6dkhOCi/hTyBC0FjHK5bVS9mEd0Gq.', -- bcrypt for "password"
       'Migrated',
       'User'
FROM reservations r
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE u.email = r.user_email
);

UPDATE reservations r
JOIN users u ON u.email = r.user_email
SET r.user_id = u.id
WHERE r.user_id IS NULL;

-- Enforce referential integrity now that backfill is complete
ALTER TABLE reservations
    MODIFY user_id BIGINT NOT NULL;

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE reservations
    DROP COLUMN user_email;

CREATE INDEX idx_reservation_user ON reservations(user_id);
