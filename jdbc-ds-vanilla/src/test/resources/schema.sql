create table if not exists POSTS (
    ID serial not null,
    TITLE varchar(80) not null,
    BODY varchar(255) not null,
    CREATED_AT timestamp,
    UPDATED_AT timestamp,
    constraint PK_POSTS primary key (ID)
);