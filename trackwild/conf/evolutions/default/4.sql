# Animals Tables Schema

# --- !Ups

CREATE TABLE all_animals(
    animal_id varchar(50) NOT NULL PRIMARY KEY,
    created_by text REFERENCES verified_users(userName),
    gender varchar(20),
    species varchar(50)
);

CREATE TABLE animals_data(
    animal_id varchar(50) NOT NULL REFERENCES all_animals(animal_id)
       ON UPDATE CASCADE,
    animalID_in_table text NOT NULL,
    SQL_table_name text NOT NULL REFERENCES all_data_tables(SQL_table_name)
      ON DELETE CASCADE
      ON UPDATE CASCADE
);


# --- !Downs

DROP TABLE all_animals CASCADE;
DROP TABLE animals_data CASCADE;