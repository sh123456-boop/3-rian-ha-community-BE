USE board;

-- MySQL 8 does not allow WITH + INSERT. Use derived tables for numbers/targets.
INSERT INTO comments (contents, post_id, user_id, created_at, updated_at)
SELECT CONCAT(t.title, ' comment ', LPAD(nums.n, 2, '0')),
       (SELECT post_id FROM posts WHERE title = t.title),
       CASE MOD(nums.n + t.author_shift, 5)
           WHEN 1 THEN (SELECT user_id FROM users WHERE email = 'loadtest1@example.com')
           WHEN 2 THEN (SELECT user_id FROM users WHERE email = 'loadtest2@example.com')
           WHEN 3 THEN (SELECT user_id FROM users WHERE email = 'loadtest3@example.com')
           WHEN 4 THEN (SELECT user_id FROM users WHERE email = 'loadtest4@example.com')
           ELSE (SELECT user_id FROM users WHERE email = 'loadtest-admin@example.com')
       END,
       DATE_SUB(NOW(), INTERVAL (t.time_base - nums.n) MINUTE),
       DATE_SUB(NOW(), INTERVAL (t.time_base - nums.n) MINUTE)
FROM (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
    SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL
    SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL
    SELECT 17 UNION ALL SELECT 18
) nums
JOIN (
    SELECT 'Load Test Post 01' AS title, 18 AS cnt, 40 AS time_base, 0 AS author_shift UNION ALL
    SELECT 'Load Test Post 02', 15, 50, 1 UNION ALL
    SELECT 'Load Test Post 03', 12, 60, 2 UNION ALL
    SELECT 'Load Test Post 04',  9, 70, 3 UNION ALL
    SELECT 'Load Test Post 05', 16, 80, 4 UNION ALL
    SELECT 'Load Test Post 06',  8, 90, 0 UNION ALL
    SELECT 'Load Test Post 07',  7, 95, 1 UNION ALL
    SELECT 'Load Test Post 08',  5,100, 2 UNION ALL
    SELECT 'Load Test Post 09',  4,105, 3 UNION ALL
    SELECT 'Load Test Post 10',  4,110, 4 UNION ALL
    SELECT 'Load Test Post 11',  3,115, 0 UNION ALL
    SELECT 'Load Test Post 12',  3,120, 1 UNION ALL
    SELECT 'Load Test Post 13',  2,125, 2 UNION ALL
    SELECT 'Load Test Post 14',  2,130, 3 UNION ALL
    SELECT 'Load Test Post 15',  2,135, 4
) t ON nums.n <= t.cnt;
