create database logever;

create table user (
	id INT NOT NULL AUTO_INCREMENT,
	username VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	first_name VARCHAR(255) NOT NULL,
	last_name VARCHAR(255),
	password VARCHAR(255) NOT NULL,
	create_dt DATETIME DEFAULT CURRENT_TIMESTAMP,
	update_dt DATETIME ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (id),
	CONSTRAINT UC_USER UNIQUE (username,email)
);

create table role (
	id INT NOT NULL AUTO_INCREMENT,
	role VARCHAR(255) NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT UC_ROLE UNIQUE (role)
);


create table user_role (
	id INT NOT NULL AUTO_INCREMENT,
	user_id INT NOT NULL,
	role_id INT NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (user_id) REFERENCES user(id),
	FOREIGN KEY (role_id) REFERENCES role(id)
);


create user 'logever'@'%' identified by 'logever'; -- Creates the user
grant all on logever.* to 'logever'@'%'; --grant all access to user

alter table role add column description VARCHAR(255) NOT NULL;

alter table user add column department VARCHAR(255) NOT NULL after password;

create table role_permission (
	id INT NOT NULL AUTO_INCREMENT,
	role_id INT NOT NULL,
	permission VARCHAR(255),
	PRIMARY KEY (id),
	FOREIGN KEY (role_id) REFERENCES role(id)
);
