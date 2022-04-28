set global log_bin_trust_function_creators=1;

create database if not exists logever;

create user if not exists 'logever'@'%' identified by 'logever'; 
grant all on logever.* to 'logever'@'%';

