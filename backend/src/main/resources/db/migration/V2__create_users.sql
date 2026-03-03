CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role ENUM('EMPLOYEE', 'MANAGER', 'ADMIN') NOT NULL DEFAULT 'EMPLOYEE',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default users (password: admin123 BCrypt hashed with strength 12)
INSERT INTO users (email, password, first_name, last_name, role) VALUES
('admin@parking.com', '$2b$12$waeZ6upp2vgIJHLOze6zTOnMilWn7rnE78zgoIWHSUFwqx93pWtpy', 'Admin', 'User', 'ADMIN'),
('manager@parking.com', '$2b$12$waeZ6upp2vgIJHLOze6zTOnMilWn7rnE78zgoIWHSUFwqx93pWtpy', 'Manager', 'User', 'MANAGER'),
('employee@parking.com', '$2b$12$waeZ6upp2vgIJHLOze6zTOnMilWn7rnE78zgoIWHSUFwqx93pWtpy', 'Employee', 'User', 'EMPLOYEE');
