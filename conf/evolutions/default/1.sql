# Users schema

# --- !Ups

CREATE TABLE User (
    id varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    fullname varchar(255) NOT NULL,
    isAdmin boolean NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE User;