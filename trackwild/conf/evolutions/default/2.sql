# Projects schema

# --- !Ups

CREATE TABLE all_projects(
 project_title text NOT NULL PRIMARY KEY UNIQUE,
 created_date timestamp NOT NULL,
 project_lead text NOT NULL REFERENCES verified_users(userName),
 isActive boolean NOT NULL
);

CREATE TABLE collaborations(
  userName text NOT NULL REFERENCES verified_users(userName),
  project_title text NOT NULL REFERENCES all_projects(project_title)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  permission_level smallint NOT NULL
);

CREATE TABLE project_notes(
    note_id  bigserial PRIMARY KEY,
    project_title text NOT NULL REFERENCES all_projects(project_title)
       ON DELETE CASCADE
       ON UPDATE CASCADE,
    note_title text DEFAULT (CURRENT_TIMESTAMP, 'YYYY:MM:DD:HH:MM'),
    note_author text NOT NULL REFERENCES verified_users(userName),
    note_content text NOT NULL,
    note_date timestamp default current_timestamp
);

-- sample data for demoing
INSERT INTO all_projects VALUES('Sample Project: Crocodile Tracking', '2017-06-06 21:46:00', 'DemoUser', TRUE);
INSERT INTO collaborations VALUES('nhanak', 'Sample Project: Crocodile Tracking', 900);
INSERT INTO project_notes(project_title, note_title, note_author, note_content)
    VALUES('Sample Project: Crocodile Tracking', 'Captued Croc', 'DemoUser', 'crikey, a big one!');
INSERT INTO project_notes(project_title, note_author, note_content)
    VALUES('Sample Project: Crocodile Tracking', 'nhanak', 'Lost another intern');

# --- !Downs

DROP TABLE collaborations;
DROP TABLE project_notes;
DROP TABLE all_projects CASCADE;

