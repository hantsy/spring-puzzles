
CREATE TABLE IF NOT EXISTS posts(
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255),
                    content VARCHAR(255),
                    created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
                    );