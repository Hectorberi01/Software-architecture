CREATE TABLE parking_spots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    `row` VARCHAR(1) NOT NULL,
    number INT NOT NULL,
    has_charger BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Row A (electric charger)
INSERT INTO parking_spots (code, `row`, number, has_charger) VALUES
('A01', 'A', 1, TRUE), ('A02', 'A', 2, TRUE), ('A03', 'A', 3, TRUE),
('A04', 'A', 4, TRUE), ('A05', 'A', 5, TRUE), ('A06', 'A', 6, TRUE),
('A07', 'A', 7, TRUE), ('A08', 'A', 8, TRUE), ('A09', 'A', 9, TRUE),
('A10', 'A', 10, TRUE);

-- Row B
INSERT INTO parking_spots (code, `row`, number, has_charger) VALUES
('B01', 'B', 1, FALSE), ('B02', 'B', 2, FALSE), ('B03', 'B', 3, FALSE),
('B04', 'B', 4, FALSE), ('B05', 'B', 5, FALSE), ('B06', 'B', 6, FALSE),
('B07', 'B', 7, FALSE), ('B08', 'B', 8, FALSE), ('B09', 'B', 9, FALSE),
('B10', 'B', 10, FALSE);

-- Row C
INSERT INTO parking_spots (code, `row`, number, has_charger) VALUES
('C01', 'C', 1, FALSE), ('C02', 'C', 2, FALSE), ('C03', 'C', 3, FALSE),
('C04', 'C', 4, FALSE), ('C05', 'C', 5, FALSE), ('C06', 'C', 6, FALSE),
('C07', 'C', 7, FALSE), ('C08', 'C', 8, FALSE), ('C09', 'C', 9, FALSE),
('C10', 'C', 10, FALSE);

-- Row D
INSERT INTO parking_spots (code, `row`, number, has_charger) VALUES
('D01', 'D', 1, FALSE), ('D02', 'D', 2, FALSE), ('D03', 'D', 3, FALSE),
('D04', 'D', 4, FALSE), ('D05', 'D', 5, FALSE), ('D06', 'D', 6, FALSE),
('D07', 'D', 7, FALSE), ('D08', 'D', 8, FALSE), ('D09', 'D', 9, FALSE),
('D10', 'D', 10, FALSE);

-- Row E
INSERT INTO parking_spots (code, `row`, number, has_charger) VALUES
('E01', 'E', 1, FALSE), ('E02', 'E', 2, FALSE), ('E03', 'E', 3, FALSE),
('E04', 'E', 4, FALSE), ('E05', 'E', 5, FALSE), ('E06', 'E', 6, FALSE),
('E07', 'E', 7, FALSE), ('E08', 'E', 8, FALSE), ('E09', 'E', 9, FALSE),
('E10', 'E', 10, FALSE);

-- Row F (electric charger)
INSERT INTO parking_spots (code, `row`, number, has_charger) VALUES
('F01', 'F', 1, TRUE), ('F02', 'F', 2, TRUE), ('F03', 'F', 3, TRUE),
('F04', 'F', 4, TRUE), ('F05', 'F', 5, TRUE), ('F06', 'F', 6, TRUE),
('F07', 'F', 7, TRUE), ('F08', 'F', 8, TRUE), ('F09', 'F', 9, TRUE),
('F10', 'F', 10, TRUE);
