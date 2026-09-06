CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     email VARCHAR(255) NOT NULL,
    login VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    birthday DATE
    );

CREATE TABLE IF NOT EXISTS mpa_ratings (
                                           id INTEGER PRIMARY KEY,
                                           name VARCHAR(10) NOT NULL
    );

CREATE TABLE IF NOT EXISTS genres (
                                      id INTEGER PRIMARY KEY,
                                      name VARCHAR(20) NOT NULL
    );

CREATE TABLE IF NOT EXISTS films (
                                     id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE,
    duration INTEGER,
    mpa_rating_id INTEGER,
    FOREIGN KEY (mpa_rating_id) REFERENCES mpa_ratings(id)
    );

CREATE TABLE IF NOT EXISTS film_genres (
                                           film_id INTEGER,
                                           genre_id INTEGER,
                                           PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS likes (
                                     film_id INTEGER,
                                     user_id INTEGER,
                                     PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS friends (
                                       user_id INTEGER,
                                       friend_id INTEGER,
                                       status VARCHAR(10) DEFAULT 'PENDING',
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
    );