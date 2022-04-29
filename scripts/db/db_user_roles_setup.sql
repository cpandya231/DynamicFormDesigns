create database if not exists logever;

create user if not exists 'logever'@'%' identified by 'logever'; 
GRANT SUPER ON *.* TO 'logever'@'%'; IDENTIFIED BY 'logever';
grant all on logever.* to 'logever'@'%';

