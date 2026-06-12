CREATE TABLE posts (
    id              BIGSERIAL    PRIMARY KEY,
    author_username VARCHAR(50)  NOT NULL,
    content         VARCHAR(500) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_posts_created ON posts (created_at DESC);

CREATE TABLE comments (
    id              BIGSERIAL    PRIMARY KEY,
    post_id         BIGINT       NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    author_username VARCHAR(50)  NOT NULL,
    content         VARCHAR(500) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_comments_post ON comments (post_id);

CREATE TABLE likes (
    post_id    BIGINT      NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    username   VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (post_id, username)
);
