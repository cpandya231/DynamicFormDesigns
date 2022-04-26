

use logever;

insert into permission (permission,created_by,create_dt) values ('CREATE-ROLE','admin',current_timestamp);
insert into permission (permission,created_by,create_dt) values ('CREATE-USER','admin',current_timestamp);
insert into permission (permission,created_by,create_dt) values ('CREATE-PERMISSION','admin',current_timestamp);

insert into role (role,description,created_by,create_dt) values ('ROLE_ADMIN','Administrator role for the Application','admin',current_timestamp);

insert into role_permission(role_id,permission_id) values (1,1);
insert into role_permission(role_id,permission_id) values (1,2);
insert into role_permission(role_id,permission_id) values (1,3);

insert into user (username,password,first_name,department,is_active,created_by,create_dt) values ('admin','$2a$10$neAtxbkTe3P1lUkvghFG0e9tO7Lfx47i4wuvl/UJqPSqF5lvpgRBa','ADMIN','ZYDUS',true,'admin',current_timestamp);

insert into user_role (user_id,role_id) values (1,1);