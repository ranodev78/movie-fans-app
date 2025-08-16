GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON DATABASE movie_share_db TO postgres;

DROP TABLE IF EXISTS subscription_platforms;
DROP TABLE IF EXISTS movie_streaming_release_subscriptions;
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

CREATE TABLE streaming_platforms (
    name VARCHAR(50) PRIMARY KEY
);

INSERT INTO streaming_platforms (name) VALUES
  ('APPLE_TV'),
  ('AMAZON_PRIME'),
  ('HBO_MAX'),
  ('HULU'),
  ('NETFLIX'),
  ('PARAMOUNT'),
  ('PEACOCK'),
  ('PRIME');

CREATE TABLE movie_streaming_release_subscriptions (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tmdb_movie_id CHAR(36) NOT NULL,
    movie_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscription_platforms (
    subscription_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    PRIMARY KEY (subscription_id, platform),
    FOREIGN KEY (subscription_id)
        REFERENCES movie_streaming_release_subscriptions(id)
        ON DELETE CASCADE,
    FOREIGN KEY (platform)
        REFERENCES streaming_platforms(name)
);