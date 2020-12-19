create table client
(
    id   bigserial not null primary key,
    name varchar(50),
    age  integer
);

create table account
(
    no varchar(50) not null primary key,
    type varchar(50) not null,
    rest real not null
);

