-- clean all data
DELETE FROM posts;
-- inserting sample data
INSERT INTO posts(title, content)
                    VALUES
                    ('Hello Spring', 'This is my first post of Spring'),
                    ('Hello Again', 'The second post');