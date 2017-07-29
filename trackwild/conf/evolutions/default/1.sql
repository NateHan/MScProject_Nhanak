# Verified Users schema

# --- !Ups

CREATE TABLE verified_users (
    uEmail varchar(255) NOT NULL UNIQUE,
    uPassword varchar(255) NOT NULL,
    userName varchar(255) NOT NULL UNIQUE,
    fullName varchar(255) NOT NULL,
    organization varchar(255),
    verified boolean NOT NULL DEFAULT false,
    PRIMARY KEY (username)
);

 -- insert in 2 users to pass DB testing upon initial deploy
insert into verified_users values (
    'nathan.hanak@gmail.com',
    'trackwild',
    'nhanak',
    'Nathan Hanak',
    'Birkbeck',
    true
);

insert into verified_users values (
    'demo@demo.com',
    'demo',
    'DemoUser',
    'Demo User',
    'Wide World of Science',
    true
)



# --- !Downs

DROP TABLE verified_users;