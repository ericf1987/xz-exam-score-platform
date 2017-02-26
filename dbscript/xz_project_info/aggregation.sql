CREATE TABLE aggregation(
  id varchar(40) primary key,
  project_id varchar(40) not null,
  status VARCHAR(20) not null DEFAULT 'Idle',
  start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  end_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);