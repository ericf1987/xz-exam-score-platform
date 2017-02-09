drop table if exists project;
create table project(
	id varchar(36) primary key, 
	name varchar(50), 
	create_time timestamp default CURRENT_TIMESTAMP, 
	grade int not null,
	full_score decimal(4,1)
);
