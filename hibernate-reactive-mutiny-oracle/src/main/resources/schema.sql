CREATE SEQUENCE todos_seq START WITH 1;
-- 23c supports `if not exists`
CREATE TABLE IF NOT EXISTS todos
(
    id    NUMBER(10)  DEFAULT todos_seq.nextval NOT NULL,
    title VARCHAR2(200)
);

-- ALTER TABLE todos
--     DROP CONSTRAINT IF EXISTS todos_pk;
ALTER TABLE todos
    ADD CONSTRAINT todos_pk PRIMARY KEY (id);
