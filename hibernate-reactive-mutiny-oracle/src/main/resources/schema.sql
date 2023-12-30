-- 23c supports `if not exists`
-- see: https://oracle-base.com/articles/23c/if-not-exists-ddl-clause-23c
CREATE SEQUENCE IF NOT EXISTS todos_seq START WITH 1;

CREATE TABLE IF NOT EXISTS todos
(
    id    NUMBER(10)  DEFAULT todos_seq.nextval NOT NULL,
    title VARCHAR2(200)
);

ALTER TABLE todos
    DROP CONSTRAINT IF EXISTS todos_pk;
ALTER TABLE todos
    ADD CONSTRAINT todos_pk PRIMARY KEY (id);
