CREATE OR REPLACE VIEW user_leaderboard_view AS
SELECT
    DENSE_RANK() OVER (ORDER BY ROUND(AVG(ri.speed), 2) DESC) AS rank_place,
    ri.id_user AS id_user,
    u.username AS username,
    CAST(ROUND(AVG(ri.speed), 2) AS NUMERIC(10, 2)) AS average_speed,
    COUNT(ri.id_contest) AS count_contest,
    COUNT(ri.place) FILTER (WHERE ri.place = 'FIRST') AS first_places_count
FROM result_items ri
JOIN users u ON ri.id_user = u.id
GROUP BY ri.id_user, u.username;

CREATE OR REPLACE VIEW exercise_records_view AS
SELECT DISTINCT ON (e.id)
    e.id AS id_exercise,
    e.title AS title,
    u.username AS username,
    ri.speed AS max_speed,
    ri.duration_seconds AS min_duration
FROM exercises e
JOIN contests c ON e.id = c.id_exercises
JOIN result_items ri ON c.id = ri.id_contest
JOIN users u ON ri.id_user = u.id
ORDER BY e.id, ri.speed DESC, ri.duration_seconds ASC;