-- =============================================================================
-- TuneVaultFX — Sample Data Seed
-- Tests genre discovery quiz preferences across all 10 quiz genres.
--
-- Apply after schema.sql:
--   mysql -u root -p tune_vault_db < sample_data.sql
--
-- Safe to re-run: wrapped in a transaction; existing rows are left alone.
-- Genres inserted here match the quiz_answer genre_name values exactly so
-- the recommendation engine can match them.
-- =============================================================================

USE tune_vault_db;

-- ---------------------------------------------------------------------------
-- Genres (10 — one per quiz answer category)
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO genre (genre_id, genre_name) VALUES
(1,  'Classical'),
(2,  'R&B'),
(3,  'Pop'),
(4,  'Metal'),
(5,  'Folk'),
(6,  'Hip Hop'),
(7,  'Electronic'),
(8,  'Indie'),
(9,  'Rock'),
(10, 'Jazz');

-- ---------------------------------------------------------------------------
-- Artists (3 per genre = 30 total)
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO artist (artist_id, name) VALUES
-- Classical
(1,  'Ludwig van Beethoven'),
(2,  'Johann Sebastian Bach'),
(3,  'Wolfgang Amadeus Mozart'),
-- R&B
(4,  'Alicia Keys'),
(5,  'Frank Ocean'),
(6,  'SZA'),
-- Pop
(7,  'Taylor Swift'),
(8,  'Harry Styles'),
(9,  'Dua Lipa'),
-- Metal
(10, 'Metallica'),
(11, 'Black Sabbath'),
(12, 'Tool'),
-- Folk
(13, 'Phoebe Bridgers'),
(14, 'Bon Iver'),
(15, 'Fleet Foxes'),
-- Hip Hop
(16, 'Kendrick Lamar'),
(17, 'J. Cole'),
(18, 'Tyler, the Creator'),
-- Electronic
(19, 'Aphex Twin'),
(20, 'Boards of Canada'),
(21, 'Four Tet'),
-- Indie
(22, 'Arctic Monkeys'),
(23, 'Mitski'),
(24, 'Tame Impala'),
-- Rock
(25, 'Led Zeppelin'),
(26, 'The Strokes'),
(27, 'Radiohead'),
-- Jazz
(28, 'Miles Davis'),
(29, 'John Coltrane'),
(30, 'Thelonious Monk');

-- ---------------------------------------------------------------------------
-- Songs (~10 per genre = 100 total)
-- duration_seconds: realistic track lengths
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO song (song_id, title, artist_id, genre_id, duration_seconds) VALUES

-- Classical (genre_id = 1)
(1,  'Moonlight Sonata',                    1,  1, 900),
(2,  'Symphony No. 5 in C Minor',           1,  1, 1980),
(3,  'Ode to Joy',                          1,  1, 750),
(4,  'Cello Suite No. 1 in G Major',        2,  1, 630),
(5,  'The Well-Tempered Clavier Book I',    2,  1, 5400),
(6,  'Toccata and Fugue in D Minor',        2,  1, 540),
(7,  'Eine kleine Nachtmusik',              3,  1, 780),
(8,  'Piano Sonata No. 16 in C Major',      3,  1, 510),
(9,  'Symphony No. 40 in G Minor',          3,  1, 1560),
(10, 'Requiem in D Minor',                  3,  1, 3300),

-- R&B (genre_id = 2)
(11, 'Fallin''',                            4,  2, 211),
(12, 'No One',                              4,  2, 254),
(13, 'If I Ain''t Got You',                 4,  2, 234),
(14, 'Superstar',                           4,  2, 196),
(15, 'Thinkin Bout You',                    5,  2, 201),
(16, 'Self Control',                        5,  2, 249),
(17, 'Pink + White',                        5,  2, 182),
(18, 'Good Days',                           6,  2, 275),
(19, 'Kill Bill',                           6,  2, 153),
(20, 'Snooze',                              6,  2, 201),

-- Pop (genre_id = 3)
(21, 'Anti-Hero',                           7,  3, 200),
(22, 'Shake It Off',                        7,  3, 219),
(23, 'Cruel Summer',                        7,  3, 178),
(24, 'Love Story',                          7,  3, 235),
(25, 'As It Was',                           8,  3, 167),
(26, 'Watermelon Sugar',                    8,  3, 174),
(27, 'Adore You',                           8,  3, 207),
(28, 'Levitating',                          9,  3, 203),
(29, 'Don''t Start Now',                    9,  3, 183),
(30, 'Physical',                            9,  3, 193),

-- Metal (genre_id = 4)
(31, 'Enter Sandman',                       10, 4, 331),
(32, 'Master of Puppets',                   10, 4, 515),
(33, 'Nothing Else Matters',                10, 4, 387),
(34, 'One',                                 10, 4, 446),
(35, 'Iron Man',                            11, 4, 356),
(36, 'Paranoid',                            11, 4, 171),
(37, 'War Pigs',                            11, 4, 479),
(38, 'Schism',                              12, 4, 403),
(39, 'Parabola',                            12, 4, 386),
(40, 'The Pot',                             12, 4, 387),

-- Folk (genre_id = 5)
(41, 'Motion Sickness',                     13, 5, 231),
(42, 'Garden Song',                         13, 5, 193),
(43, 'Savior Complex',                      13, 5, 218),
(44, 'Moon Song',                           13, 5, 183),
(45, 'Skinny Love',                         14, 5, 222),
(46, 'Holocene',                            14, 5, 282),
(47, 'Towers',                              14, 5, 259),
(48, 'White Winter Hymnal',                 15, 5, 139),
(49, 'Helplessness Blues',                  15, 5, 319),
(50, 'Mykonos',                             15, 5, 248),

-- Hip Hop (genre_id = 6)
(51, 'HUMBLE.',                             16, 6, 177),
(52, 'DNA.',                                16, 6, 185),
(53, 'Money Trees',                         16, 6, 386),
(54, 'Alright',                             16, 6, 219),
(55, 'No Role Modelz',                      17, 6, 293),
(56, 'Love Yourz',                          17, 6, 224),
(57, 'Forest Hill Drive',                   17, 6, 266),
(58, 'See You Again',                       18, 6, 210),
(59, 'EARFQUAKE',                           18, 6, 188),
(60, 'New Magic Wand',                      18, 6, 193),

-- Electronic (genre_id = 7)
(61, 'Windowlicker',                        19, 7, 600),
(62, 'Flim',                                19, 7, 178),
(63, 'Avril 14th',                          19, 7, 121),
(64, 'Roygbiv',                             20, 7, 199),
(65, 'Olson',                               20, 7, 249),
(66, 'Dayvan Cowboy',                       20, 7, 367),
(67, 'Baby',                                21, 7, 374),
(68, 'Moth',                                21, 7, 392),
(69, 'Parallel Jalebi',                     21, 7, 312),
(70, 'Two Thousand and Seventeen',          21, 7, 298),

-- Indie (genre_id = 8)
(71, 'R U Mine?',                           22, 8, 202),
(72, 'Do I Wanna Know?',                    22, 8, 272),
(73, '505',                                 22, 8, 254),
(74, 'Why''d You Only Call Me When You''re High?', 22, 8, 161),
(75, 'Nobody',                              23, 8, 215),
(76, 'Washing Machine Heart',               23, 8, 158),
(77, 'Bug Like an Angel',                   23, 8, 153),
(78, 'The Less I Know the Better',          24, 8, 216),
(79, 'Let It Happen',                       24, 8, 467),
(80, 'Eventually',                          24, 8, 318),

-- Rock (genre_id = 9)
(81, 'Stairway to Heaven',                  25, 9, 482),
(82, 'Whole Lotta Love',                    25, 9, 334),
(83, 'Kashmir',                             25, 9, 516),
(84, 'Black Dog',                           25, 9, 296),
(85, 'Last Nite',                           26, 9, 193),
(86, 'Reptilia',                            26, 9, 222),
(87, 'Hard to Explain',                     26, 9, 218),
(88, 'Creep',                               27, 9, 238),
(89, 'Karma Police',                        27, 9, 264),
(90, 'Paranoid Android',                    27, 9, 383),

-- Jazz (genre_id = 10)
(91, 'So What',                             28, 10, 562),
(92, 'Kind of Blue',                        28, 10, 346),
(93, 'All Blues',                           28, 10, 693),
(94, 'Blue in Green',                       28, 10, 337),
(95, 'A Love Supreme Part I',               29, 10, 459),
(96, 'My Favorite Things',                  29, 10, 853),
(97, 'Giant Steps',                         29, 10, 219),
(98, 'Round Midnight',                      30, 10, 348),
(99, 'Blue Monk',                           30, 10, 281),
(100,'Straight, No Chaser',                 30, 10, 264);

-- ---------------------------------------------------------------------------
-- Sample data — 10 questions (5 QUICK + 5 FULL) matching GenreQuiz.java bank
-- ---------------------------------------------------------------------------

INSERT INTO quiz_question (question_id, prompt, quiz_mode, display_order, is_active) VALUES
(1,  'What energy do you want from music right now?', 'QUICK', 1, 1),
(2,  'What hooks you first?',                         'QUICK', 2, 1),
(3,  'Where do you listen most?',                     'QUICK', 3, 1),
(4,  'Pick a sound "colour":',                        'QUICK', 4, 1),
(5,  'Old or new sounds?',                            'QUICK', 5, 1),
(6,  'Production style that pulls you in:',           'FULL',  6, 1),
(7,  'How important are vocals?',                     'FULL',  7, 1),
(8,  'What setting fits your mood right now?',        'FULL',  8, 1),
(9,  'What feeling do you want music to give you?',   'FULL',  9, 1),
(10, 'Which instrument drives your favourite tracks?','FULL', 10, 1);

-- Q1 — energy
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(1, 'Soft & calm',    'Classical', 1, 1),
(1, 'Steady groove',  'R&B',       1, 2),
(1, 'Up & dancey',    'Pop',       2, 3),
(1, 'Loud & intense', 'Metal',     2, 4);

-- Q2 — entry point
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(2, 'The words',      'Folk',       1, 1),
(2, 'The rhythm',     'Hip Hop',    1, 2),
(2, 'The melody',     'Pop',        1, 3),
(2, 'The atmosphere', 'Electronic', 1, 4);

-- Q3 — space
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(3, 'Cozy at home',  'Indie',      1, 1),
(3, 'Night drive',   'Rock',       1, 2),
(3, 'With friends',  'Pop',        1, 3),
(3, 'Deep focus',    'Electronic', 1, 4);

-- Q4 — colour
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(4, 'Warm & smoky',     'Jazz', 1, 1),
(4, 'Cool & smooth',    'R&B',  1, 2),
(4, 'Bright & sparkly', 'Pop',  1, 3),
(4, 'Raw & organic',    'Folk', 1, 4);

-- Q5 — era
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(5, 'Timeless classics', 'Classical',  1, 1),
(5, 'Throwback eras',    'Rock',       1, 2),
(5, 'Fresh releases',    'Pop',        1, 3),
(5, 'Mix it all',        'Electronic', 1, 4);

-- Q6 — structure (weighted)
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(6, 'Catchy & tight',   'Pop',        2, 1),
(6, 'Room to breathe',  'Jazz',       1, 2),
(6, 'Long builds',      'Electronic', 2, 3),
(6, 'Loops that hit',   'Hip Hop',    1, 4);

-- Q7 — vocals
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(7, 'Sing-along essential',    'Pop',        1, 1),
(7, 'Nice when they''re there','Indie',      1, 2),
(7, 'Background is fine',      'Rock',       1, 3),
(7, 'Instrumentals rule',      'Electronic', 1, 4);

-- Q8 — social
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(8, 'Solo reset',        'Classical',  1, 1),
(8, 'Small hangout',     'Indie',      1, 2),
(8, 'Big crowd energy',  'Rock',       1, 3),
(8, 'Study / work flow', 'Electronic', 1, 4);

-- Q9 — emotion
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(9, 'Happy boost',       'Pop',   1, 1),
(9, 'Soft & reflective', 'R&B',   1, 2),
(9, 'Angry catharsis',   'Metal', 1, 3),
(9, 'Hopeful glow',      'Indie', 1, 4);

-- Q10 — instrument
INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES
(10, 'Strings',        'Classical',  1, 1),
(10, 'Drums & bass',   'Hip Hop',    1, 2),
(10, 'Synths & pads',  'Electronic', 1, 3),
(10, 'Guitar forward', 'Rock',       1, 4);