
INSERT INTO permission (permission,created_by,create_dt)
SELECT * FROM (SELECT 'CREATE-ROLE','admin',current_timestamp) AS tmp
WHERE NOT EXISTS (
    SELECT permission FROM permission WHERE permission = 'CREATE-ROLE'
) LIMIT 1 ^;
INSERT INTO permission (permission,created_by,create_dt)
SELECT * FROM (SELECT 'CREATE-USER','admin',current_timestamp) AS tmp
WHERE NOT EXISTS (
    SELECT permission FROM permission WHERE permission = 'CREATE-USER'
) LIMIT 1 ^;
INSERT INTO permission (permission,created_by,create_dt)
SELECT * FROM (SELECT 'CREATE-PERMISSION','admin',current_timestamp) AS tmp
WHERE NOT EXISTS (
    SELECT permission FROM permission WHERE permission = 'CREATE-PERMISSION'
) LIMIT 1 ^;



INSERT INTO role (role,description,created_by,create_dt)
SELECT * FROM (SELECT 'ROLE_ADMIN','Administrator role for the Application','admin',current_timestamp) AS tmp
WHERE NOT EXISTS (
    SELECT role FROM role WHERE role = 'ROLE_ADMIN'
) LIMIT 1 ^;


INSERT INTO role_permission(role_id,permission_id)
SELECT * FROM (SELECT 1 as role_id,1 as permission_id) AS tmp
WHERE NOT EXISTS (
    SELECT permission_id FROM role_permission WHERE permission_id = "1"
) LIMIT 1 ^;
INSERT INTO role_permission(role_id,permission_id)
SELECT * FROM (SELECT 1,2) AS tmp
WHERE NOT EXISTS (
    SELECT permission_id FROM role_permission WHERE permission_id = 2
) LIMIT 1 ^;
INSERT INTO role_permission(role_id,permission_id)
SELECT * FROM (SELECT 1,3) AS tmp
WHERE NOT EXISTS (
    SELECT permission_id FROM role_permission WHERE permission_id = 3
) LIMIT 1 ^;


INSERT INTO user (username,password,first_name,department,is_active,created_by,create_dt)
SELECT * FROM (SELECT 'admin' as username,'$2a$10$neAtxbkTe3P1lUkvghFG0e9tO7Lfx47i4wuvl/UJqPSqF5lvpgRBa','ADMIN' as fname,'ZYDUS',true,'admin',current_timestamp) AS tmp
WHERE NOT EXISTS (
    SELECT username FROM user WHERE username = 'admin'
) LIMIT 1 ^;


INSERT INTO user_role (user_id,role_id)
SELECT * FROM (SELECT 1 as user_id,1 as role_id) AS tmp
WHERE NOT EXISTS (
    SELECT role_id FROM user_role WHERE role_id = 1
) LIMIT 1 ^;