# Data Tables Schema

# --- !Ups

CREATE TABLE all_data_tables(
    userview_table_name text NOT NULL,
    SQL_table_name text UNIQUE PRIMARY KEY,
    project_title text REFERENCES all_projects(project_title)
       ON DELETE CASCADE
       ON UPDATE CASCADE
);

# --- !Downs

DROP TABLE all_data_tables;