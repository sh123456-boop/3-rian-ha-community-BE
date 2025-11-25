USE board;

-- 1. Seed deterministic users for load and soak tests
INSERT INTO users (email, password, nickname, role, created_at, updated_at) VALUES
('loadtest1@example.com', '$2y$10$r8Wcsl7zV1i2la94TM3zjOxsYsRSXWLzvT32iKTODXk0Z/p/6A4p2', 'tester01', 'USER', NOW(), NOW()),
('loadtest2@example.com', '$2y$10$r8Wcsl7zV1i2la94TM3zjOxsYsRSXWLzvT32iKTODXk0Z/p/6A4p2', 'tester02', 'USER', NOW(), NOW()),
('loadtest3@example.com', '$2y$10$r8Wcsl7zV1i2la94TM3zjOxsYsRSXWLzvT32iKTODXk0Z/p/6A4p2', 'tester03', 'USER', NOW(), NOW()),
('loadtest4@example.com', '$2y$10$r8Wcsl7zV1i2la94TM3zjOxsYsRSXWLzvT32iKTODXk0Z/p/6A4p2', 'tester04', 'USER', NOW(), NOW()),
('loadtest-admin@example.com', '$2y$10$r8Wcsl7zV1i2la94TM3zjOxsYsRSXWLzvT32iKTODXk0Z/p/6A4p2', 'loadadmin', 'ADMIN', NOW(), NOW());

-- 2. Attach profile images to each synthetic user
INSERT INTO images (s3_key, user_id) VALUES
('default-profile.png', (SELECT user_id FROM users WHERE email = 'loadtest1@example.com')),
('default-profile.png', (SELECT user_id FROM users WHERE email = 'loadtest2@example.com')),
('default-profile.png', (SELECT user_id FROM users WHERE email = 'loadtest3@example.com')),
('default-profile.png', (SELECT user_id FROM users WHERE email = 'loadtest4@example.com')),
('default-profile.png', (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'));

-- 3. Pre-create post image metadata so PostImage rows can reference stable IDs
INSERT INTO images (s3_key) VALUES
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png'),
('default-profile.png');

-- 4. Seed enough posts to exercise PostServiceImpl#getPostSlice
INSERT INTO posts (title, contents, user_id, created_at, updated_at) VALUES
('Load Test Post 01', 'HTTP load test seed post 01 keeps the infinite scroll cursor busy.', (SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
('Load Test Post 02', 'HTTP load test seed post 02 contains longer markdown-like text for serialization.', (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),
('Load Test Post 03', 'HTTP load test seed post 03 references caching strategies for warm up traffic.', (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
('Load Test Post 04', 'HTTP load test seed post 04 highlights S3 image fanout behavior.', (SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)),
('Load Test Post 05', 'HTTP load test seed post 05 documents API dependencies for TPS modeling.', (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
('Load Test Post 06', 'HTTP load test seed post 06 keeps cursor pagination stable for QA builds.', (SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
('Load Test Post 07', 'HTTP load test seed post 07 mentions synthetic engagement for PostCount.', (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
('Load Test Post 08', 'HTTP load test seed post 08 adds variety to DTO projection.', (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
('Load Test Post 09', 'HTTP load test seed post 09 touches on security context usage.', (SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('Load Test Post 10', 'HTTP load test seed post 10 tracks view count thrash for ranking features.', (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('Load Test Post 11', 'HTTP load test seed post 11 ensures there are multiple slices.', (SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('Load Test Post 12', 'HTTP load test seed post 12 simulates client retries.', (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('Load Test Post 13', 'HTTP load test seed post 13 includes lightweight metadata.', (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('Load Test Post 14', 'HTTP load test seed post 14 links to base assets.', (SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('Load Test Post 15', 'HTTP load test seed post 15 keeps recency churn active.', (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), NOW(), NOW());

-- 5. Provide PostCount rows so DTOs can expose derived metrics
INSERT INTO counts (post_id, likes_cnt, cmt_cnt, view_cnt) VALUES
((SELECT post_id FROM posts WHERE title = 'Load Test Post 01'), 45, 18, 540),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 02'), 33, 15, 410),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 03'), 28, 12, 380),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 04'), 24, 9, 345),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 05'), 26, 16, 300),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 06'), 19, 8, 275),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 07'), 17, 7, 260),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 08'), 15, 5, 240),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 09'), 14, 4, 225),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 10'), 13, 4, 210),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 11'), 12, 3, 190),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 12'), 11, 3, 180),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 13'), 11, 2, 170),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 14'), 10, 2, 160),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 15'), 9, 2, 150);

-- 6. Simulate likes for deterministic liked-by-user logic
INSERT INTO user_like_posts (user_id, post_id, liked_at) VALUES
((SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 01'), DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 01'), DATE_SUB(NOW(), INTERVAL 14 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 02'), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 02'), DATE_SUB(NOW(), INTERVAL 28 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 03'), DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 04'), DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 05'), DATE_SUB(NOW(), INTERVAL 32 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 05'), DATE_SUB(NOW(), INTERVAL 31 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 05'), DATE_SUB(NOW(), INTERVAL 29 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 06'), DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 06'), DATE_SUB(NOW(), INTERVAL 24 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 07'), DATE_SUB(NOW(), INTERVAL 23 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 08'), DATE_SUB(NOW(), INTERVAL 22 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 09'), DATE_SUB(NOW(), INTERVAL 21 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 09'), DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 10'), DATE_SUB(NOW(), INTERVAL 19 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 10'), DATE_SUB(NOW(), INTERVAL 18 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 11'), DATE_SUB(NOW(), INTERVAL 17 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 12'), DATE_SUB(NOW(), INTERVAL 16 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 13'), DATE_SUB(NOW(), INTERVAL 13 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 13'), DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 14'), DATE_SUB(NOW(), INTERVAL 11 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 14'), DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
((SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), (SELECT post_id FROM posts WHERE title = 'Load Test Post 15'), DATE_SUB(NOW(), INTERVAL 9 MINUTE));

-- 7. Link post images in deterministic order for slice responses
INSERT INTO post_images (post_id, image_id, orders) VALUES
((SELECT post_id FROM posts WHERE title = 'Load Test Post 01'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 0), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 01'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 1), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 02'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 2), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 02'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 3), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 03'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 4), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 03'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 5), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 04'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 6), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 04'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 7), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 05'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 8), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 05'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 9), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 06'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 10), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 06'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 11), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 07'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 12), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 07'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 13), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 08'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 14), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 08'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 15), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 09'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 16), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 09'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 17), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 10'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 18), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 10'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 19), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 11'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 20), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 11'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 21), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 12'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 22), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 12'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 23), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 13'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 24), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 13'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 25), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 14'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 26), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 14'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 27), 2),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 15'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 28), 1),
((SELECT post_id FROM posts WHERE title = 'Load Test Post 15'), (SELECT image_id FROM images WHERE user_id IS NULL AND s3_key = 'default-profile.png' ORDER BY image_id LIMIT 1 OFFSET 29), 2);

-- 8. Prepare chat rooms to exercise websocket subscription flows
INSERT INTO chat_rooms (name, is_group_chat, created_at, updated_at) VALUES
('lt-general', TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('lt-random', TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 9. Register chat participants
INSERT INTO chat_participants (user_id, chat_room_id, created_at, updated_at) VALUES
((SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), NOW(), NOW()),
((SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), NOW(), NOW()),
((SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), NOW(), NOW()),
((SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), NOW(), NOW()),
((SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), NOW(), NOW()),
((SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), NOW(), NOW()),
((SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), NOW(), NOW());

-- 10. Insert chat history so /chat/history queries always have rows
INSERT INTO chat_messages (contents, chat_room_id, user_id, created_at, updated_at) VALUES
('General kickoff for load test', (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
('Confirming TPS budget in general room', (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), DATE_SUB(NOW(), INTERVAL 70 MINUTE), DATE_SUB(NOW(), INTERVAL 70 MINUTE)),
('Posting cache warmup status general', (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), DATE_SUB(NOW(), INTERVAL 50 MINUTE), DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
('General room ready for websocket trial', (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('Random room greetings', (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), DATE_SUB(NOW(), INTERVAL 80 MINUTE), DATE_SUB(NOW(), INTERVAL 80 MINUTE)),
('Random room scenario sync', (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), DATE_SUB(NOW(), INTERVAL 60 MINUTE), DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
('Random room ready signal', (SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 40 MINUTE));

-- 11. Reflect read receipts so messageRead API has deterministic state
INSERT INTO read_status (chat_room_id, chat_message_id, user_id, is_read, created_at, updated_at) VALUES
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'General kickoff for load test'), (SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), TRUE, DATE_SUB(NOW(), INTERVAL 89 MINUTE), DATE_SUB(NOW(), INTERVAL 89 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'General kickoff for load test'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 88 MINUTE), DATE_SUB(NOW(), INTERVAL 88 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'General kickoff for load test'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 87 MINUTE), DATE_SUB(NOW(), INTERVAL 87 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'General kickoff for load test'), (SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 86 MINUTE), DATE_SUB(NOW(), INTERVAL 86 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Confirming TPS budget in general room'), (SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 69 MINUTE), DATE_SUB(NOW(), INTERVAL 69 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Confirming TPS budget in general room'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), TRUE, DATE_SUB(NOW(), INTERVAL 70 MINUTE), DATE_SUB(NOW(), INTERVAL 70 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Confirming TPS budget in general room'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 68 MINUTE), DATE_SUB(NOW(), INTERVAL 68 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Confirming TPS budget in general room'), (SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 67 MINUTE), DATE_SUB(NOW(), INTERVAL 67 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Posting cache warmup status general'), (SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 49 MINUTE), DATE_SUB(NOW(), INTERVAL 49 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Posting cache warmup status general'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 48 MINUTE), DATE_SUB(NOW(), INTERVAL 48 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Posting cache warmup status general'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), TRUE, DATE_SUB(NOW(), INTERVAL 50 MINUTE), DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Posting cache warmup status general'), (SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 47 MINUTE), DATE_SUB(NOW(), INTERVAL 47 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'General room ready for websocket trial'), (SELECT user_id FROM users WHERE email = 'loadtest1@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 29 MINUTE), DATE_SUB(NOW(), INTERVAL 29 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'General room ready for websocket trial'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 28 MINUTE), DATE_SUB(NOW(), INTERVAL 28 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'General room ready for websocket trial'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 27 MINUTE), DATE_SUB(NOW(), INTERVAL 27 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-general'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'General room ready for websocket trial'), (SELECT user_id FROM users WHERE email = 'loadtest4@example.com'), TRUE, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room greetings'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), TRUE, DATE_SUB(NOW(), INTERVAL 80 MINUTE), DATE_SUB(NOW(), INTERVAL 80 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room greetings'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 79 MINUTE), DATE_SUB(NOW(), INTERVAL 79 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room greetings'), (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 78 MINUTE), DATE_SUB(NOW(), INTERVAL 78 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room scenario sync'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 59 MINUTE), DATE_SUB(NOW(), INTERVAL 59 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room scenario sync'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), TRUE, DATE_SUB(NOW(), INTERVAL 60 MINUTE), DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room scenario sync'), (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 58 MINUTE), DATE_SUB(NOW(), INTERVAL 58 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room ready signal'), (SELECT user_id FROM users WHERE email = 'loadtest2@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 39 MINUTE), DATE_SUB(NOW(), INTERVAL 39 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room ready signal'), (SELECT user_id FROM users WHERE email = 'loadtest3@example.com'), FALSE, DATE_SUB(NOW(), INTERVAL 38 MINUTE), DATE_SUB(NOW(), INTERVAL 38 MINUTE)),
((SELECT chat_room_id FROM chat_rooms WHERE name = 'lt-random'), (SELECT chat_message_id FROM chat_messages WHERE contents = 'Random room ready signal'), (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com'), TRUE, DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 40 MINUTE));
