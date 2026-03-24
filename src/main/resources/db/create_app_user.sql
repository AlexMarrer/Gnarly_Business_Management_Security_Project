CREATE USER IF NOT EXISTS 'app_user'@'localhost'
    IDENTIFIED BY 'sicheres_passwort_hier_einsetzen';

GRANT SELECT, INSERT, UPDATE, DELETE, ALTER
    ON businessproject.*
    TO 'app_user'@'localhost';

FLUSH PRIVILEGES;

SHOW GRANTS FOR 'app_user'@'localhost';
