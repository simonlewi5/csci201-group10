

mysql> DESCRIBE match_results;
+------------+----------+------+-----+---------+-------+
| Field      | Type     | Null | Key | Default | Extra |
+------------+----------+------+-----+---------+-------+
| match_id   | char(36) | NO   | PRI | NULL    |       |
| winner_id  | char(36) | NO   | MUL | NULL    |       |
| start_time | bigint   | NO   |     | NULL    |       |
| end_time   | bigint   | NO   |     | NULL    |       |
+------------+----------+------+-----+---------+-------+
4 rows in set (0.00 sec)

mysql> describe players;
+---------------+--------------+------+-----+---------+-------+
| Field         | Type         | Null | Key | Default | Extra |
+---------------+--------------+------+-----+---------+-------+
| id            | char(36)     | NO   | PRI | NULL    |       |
| username      | varchar(255) | NO   | UNI | NULL    |       |
| firebase_uid  | varchar(255) | YES  | UNI | NULL    |       |
| email         | varchar(255) | NO   | UNI | NULL    |       |
| password_hash | char(64)     | YES  |     | NULL    |       |
| games_played  | int          | YES  |     | 0       |       |
| games_won     | int          | YES  |     | 0       |       |
| games_lost    | int          | YES  |     | 0       |       |
| total_score   | int          | YES  |     | 0       |       |
+---------------+--------------+------+-----+---------+-------+
9 rows in set (0.00 sec)

mysql> show tables;
+-------------------+
| Tables_in_game-db |
+-------------------+
| match_results     |
| players           |
+-------------------+
2 rows in set (0.00 sec)
