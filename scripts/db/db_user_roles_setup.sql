create database if not exists logever;

create user if not exists 'logever'@'%' identified by 'logever'; 
GRANT SUPER ON *.* TO 'logever'@'%' IDENTIFIED BY 'Logever@123';
grant all on logever.* to 'logever'@'%';

