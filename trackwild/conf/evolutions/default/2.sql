# Projects schema

# --- !Ups

CREATE TABLE all_projects(
 project_title text NOT NULL PRIMARY KEY UNIQUE,
 created_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    note_title text DEFAULT ('Note from: ', CURRENT_TIMESTAMP),
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
INSERT INTO project_notes(project_title, note_author, note_content)
    VALUES('Sample Project: Crocodile Tracking', 'DemoUser', 'Specimen traveled extra 5km. Weight has increased by .3kg.');

# --- !Downs

DROP TABLE collaborations;
DROP TABLE project_notes;
DROP TABLE all_projects CASCADE;

