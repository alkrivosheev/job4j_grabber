create table post (
    id serial primary key,
    name varchar(100),
	text text,
	link text,
    created timestamp,
	CONSTRAINT link_unique UNIQUE (link)
);