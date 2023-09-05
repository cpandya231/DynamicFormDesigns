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

INSERT INTO department (name,code,parent_id)
SELECT * FROM (SELECT 'ZYDUS','001',0) AS tmp
WHERE NOT EXISTS (
    SELECT name FROM department WHERE name = 'ZYDUS'
) LIMIT 1 ^;
INSERT INTO department (name,code,parent_id)
SELECT * FROM (SELECT 'Initiator Department','000',-1) AS tmp
WHERE NOT EXISTS (
    SELECT name FROM department WHERE name = 'Initiator Department'
) LIMIT 1 ^;

INSERT INTO user (username,password,first_name,date_of_birth,department_id,is_active,created_by,create_dt)
SELECT * FROM (SELECT 'admin' as username,'$2a$10$neAtxbkTe3P1lUkvghFG0e9tO7Lfx47i4wuvl/UJqPSqF5lvpgRBa','ADMIN' as fname,'1970-01-01',1,true,'admin',current_timestamp) AS tmp
WHERE NOT EXISTS (
    SELECT username FROM user WHERE username = 'admin'
) LIMIT 1 ^;


INSERT INTO user_role (user_id,role_id)
SELECT * FROM (SELECT 1 as user_id,1 as role_id) AS tmp
WHERE NOT EXISTS (
    SELECT role_id FROM user_role WHERE role_id = 1
) LIMIT 1 ^;

INSERT INTO settings (type,`key`,value)
SELECT * FROM (SELECT 'PASSWORD','ALPHANUMERIC','false') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'ALPHANUMERIC'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'PASSWORD','PASSWORD_MIN_LENGTH','6') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'PASSWORD_MIN_LENGTH'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'PASSWORD','EXPIRY_DAYS','365') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'EXPIRY_DAYS'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'PASSWORD','MAX_ATTEMPTS','10') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'MAX_ATTEMPTS'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'PASSWORD','SPECIAL_CHARS_REQD','false') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'SPECIAL_CHARS_REQD'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'PASSWORD','UPPER_AND_LOWER_REQD','false') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'UPPER_AND_LOWER_REQD'
) LIMIT 1 ^;

INSERT INTO settings (type,`key`, app_key,`value`)
SELECT * FROM (SELECT 'SMTP','SMTP_SERVER_IP','spring.mail.host','') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'SMTP_SERVER_IP'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,app_key,`value`)
SELECT * FROM (SELECT 'SMTP','SMTP_SERVER_PORT','spring.mail.port','') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'SMTP_SERVER_PORT'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'SMTP','FROM_EMAIL','') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'FROM_EMAIL'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,app_key,`value`)
SELECT * FROM (SELECT 'SMTP','USERNAME','spring.mail.username','') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'USERNAME'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,app_key,`value`)
SELECT * FROM (SELECT 'SMTP','PASSWORD','spring.mail.password','') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'PASSWORD'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'SMTP','CONFIRM_PASSWORD','') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'CONFIRM_PASSWORD'
) LIMIT 1 ^;

INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'SESSION','TIMEOUT','10') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'TIMEOUT'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'SESSION','TIMEOUT_ALERT','10') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'TIMEOUT_ALERT'
) LIMIT 1 ^;

INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'USERNAME','USERNAME_MIN_LENGTH','1') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'USERNAME_MIN_LENGTH'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'USERNAME','USERNAME_MAX_LENGTH','255') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'USERNAME_MAX_LENGTH'
) LIMIT 1 ^;

INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'GLOBAL','DATE_FORMAT','YYYY-MM-dd') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'DATE_FORMAT'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'GLOBAL','TIME_FORMAT','HH:mm:ss') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'TIME_FORMAT'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'GLOBAL','TIMESTAMP_FORMAT','YYYY-MM-dd HH:mm:ss') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'TIMESTAMP_FORMAT'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'GLOBAL','NO_OF_ACTIVE_USERS','1000000') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'NO_OF_ACTIVE_USERS'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'GLOBAL','VALIDITY_TILL','2022-12-31') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'VALIDITY_TILL'
) LIMIT 1 ^;
INSERT INTO settings (type,`key`,`value`)
SELECT * FROM (SELECT 'GLOBAL','VALIDITY_EXPIRY_MSG','Validity expires soon. Please contact administrator.') AS tmp
WHERE NOT EXISTS (
    SELECT `key` FROM settings WHERE `key` = 'VALIDITY_EXPIRY_MSG'
) LIMIT 1 ^;