DROP DATABASE IF EXISTS movie_share_db;
CREATE DATABASE movie_share_db;
USE movie_share_db;
DROP TABLE IF EXISTS movie_details;
CREATE TABLE movie_details (
	id CHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    released_year INT,
    director VARCHAR(255),
    genre VARCHAR(255),
    ttid VARCHAR(255) NOT NULL UNIQUE,
    rental_cost DECIMAL(8,2) CHECK (rental_cost >= 0.00 AND rental_cost <= 100.00),
    awards VARCHAR(255),
    rating DECIMAL(2,1) CHECK (rating >= 0.0 AND rating <= 10.0)
);