CREATE DATABASE IF NOT EXISTS piersen_hr
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'piersen'@'localhost' IDENTIFIED BY 'piersen';

ALTER USER 'piersen'@'localhost' IDENTIFIED BY 'piersen';

GRANT ALL PRIVILEGES ON piersen_hr.* TO 'piersen'@'localhost';

FLUSH PRIVILEGES;

SELECT user, host FROM mysql.user WHERE user = 'piersen';

SHOW GRANTS FOR 'piersen'@'localhost';
