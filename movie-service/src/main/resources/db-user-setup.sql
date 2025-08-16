CREATE USER 'movie_share_dev'@'localhost' IDENTIFIED BY 'ilove2code@1234HelloWorld2@';
GRANT ALL PRIVILEGES ON `movie_share_db`.`*` TO 'movie_share_dev'@'localhost';

SELECT user FROM mysql.user;