create database if not exists logever;

create user if not exists 'logever'@'%' identified by 'logever'; 
GRANT SUPER ON logever.* TO 'logever'@'%';
grant all on logever.* to 'logever'@'%';

