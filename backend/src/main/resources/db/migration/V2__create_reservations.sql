-- Flyway migration V2: Create reservations table

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parking_spot_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    reservation_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_spot FOREIGN KEY (parking_spot_id) REFERENCES parking_spots(id),
    CONSTRAINT uk_spot_date UNIQUE (parking_spot_id, reservation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_reservation_date ON reservations(reservation_date);
CREATE INDEX idx_reservation_email ON reservations(user_email);
