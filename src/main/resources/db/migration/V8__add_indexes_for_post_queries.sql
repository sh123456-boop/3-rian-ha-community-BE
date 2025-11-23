USE board;

-- FK lookup for posts -> users fetch join
CREATE INDEX idx_posts_user_id ON posts (user_id);

-- Faster image lookup for user profile fetch join
CREATE INDEX idx_images_user_id ON images (user_id);

-- Speed up ordered image retrieval per post
CREATE INDEX idx_post_images_post_id_orders ON post_images (post_id, orders);

-- Popular posts sorting (view_cnt DESC, post_id DESC)
CREATE INDEX idx_counts_viewcnt_postid ON counts (view_cnt DESC, post_id DESC);
