# Users schema

# --- !Ups

CREATE TABLE verified_users (
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    firstname varchar(255) NOT NULL,
    surname varchar(255) NOT NULL,
    username varchar(255) NOT NULL,
    verified boolean NOT NULL,
    PRIMARY KEY (username)
);

# --- !Downs

DROP TABLE verified_users;