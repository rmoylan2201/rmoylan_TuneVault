# Database — `tune_vault_db`

MySQL database used by TuneVaultFX. **Canonical DDL** for a fresh environment lives in:

**[`src/main/resources/db/schema.sql`](src/main/resources/db/schema.sql)**

Apply (example):

```bash
mysql -u root -p < src/main/resources/db/schema.sql
# or, if the database already exists:
mysql -u root -p tune_vault_db < src/main/resources/db/schema.sql
```

Connection defaults are documented in the main [README](README.md) (`TUNEVAULT_DB_URL`, `TUNEVAULT_DB_USER`, `TUNEVAULT_DB_PASSWORD`).

---

## Tables the app uses (from `schema.sql`)

Below matches the **checked-in** schema and the **DAO layer** under `com.example.tunevaultfx.db`.

### `app_user`

| Column | Type | Notes |
|--------|------|--------|
| `user_id` | `INT UNSIGNED` | PK, auto-increment |
| `username` | `VARCHAR(64)` | unique |
| `email` | `VARCHAR(255)` | unique |
| `password_hash` | `CHAR(64)` | SHA-256 hex (`PasswordUtil`) |
| `profile_avatar_key` | `VARCHAR(512)` | nullable; relative path under `~/.tunevaultfx/profile-media` |

Older databases may be missing `profile_avatar_key`; the app runs **`DbSchemaPatches.ensureAppUserProfileMediaColumns()`** to add it idempotently.

### `artist`

| Column | Type | Notes |
|--------|------|--------|
| `artist_id` | `INT UNSIGNED` | PK |
| `name` | `VARCHAR(255)` | |

### `genre`

| Column | Type | Notes |
|--------|------|--------|
| `genre_id` | `INT UNSIGNED` | PK |
| `genre_name` | `VARCHAR(128)` | |

### `song`

| Column | Type | Notes |
|--------|------|--------|
| `song_id` | `INT UNSIGNED` | PK |
| `title` | `VARCHAR(512)` | |
| `artist_id` | `INT UNSIGNED` | FK → `artist` |
| `genre_id` | `INT UNSIGNED` | FK → `genre` |
| `duration_seconds` | `INT UNSIGNED` | default `0` |

### `playlist`

| Column | Type | Notes |
|--------|------|--------|
| `playlist_id` | `INT UNSIGNED` | PK |
| `user_id` | `INT UNSIGNED` | FK → `app_user` |
| `name` | `VARCHAR(255)` | unique per user |
| `is_system_playlist` | `TINYINT(1)` | e.g. “Liked Songs” |

### `playlist_song`

| Column | Type | Notes |
|--------|------|--------|
| `playlist_id` | `INT UNSIGNED` | PK part, FK → `playlist` |
| `song_id` | `INT UNSIGNED` | PK part, FK → `song` |

### `listening_event`

| Column | Type | Notes |
|--------|------|--------|
| `event_id` | `INT UNSIGNED` | PK |
| `user_id` | `INT UNSIGNED` | FK → `app_user` |
| `song_id` | `INT UNSIGNED` | FK → `song` |
| `action_type` | `VARCHAR(32)` | e.g. `PLAY`, `SKIP` |
| `played_seconds` | `INT UNSIGNED` | |
| `song_duration_seconds` | `INT UNSIGNED` | |
| `completion_ratio` | `DOUBLE` | not `completion_rate` |
| `count_as_play` | `TINYINT(1)` | |
| `event_timestamp` | `TIMESTAMP` | default `CURRENT_TIMESTAMP` |

Index: `(user_id, event_timestamp)`.

### `search_history`

| Column | Type | Notes |
|--------|------|--------|
| `search_history_id` | `INT UNSIGNED` | PK |
| `user_id` | `INT UNSIGNED` | FK → `app_user` |
| `item_type` | `VARCHAR(16)` | `SONG` or `ARTIST` (see `SearchRecentItem.Type`) |
| `song_id` | `INT UNSIGNED` | nullable; FK → `song` |
| `artist_name` | `VARCHAR(255)` | nullable; used for artist searches |
| `searched_at` | `TIMESTAMP` | default `CURRENT_TIMESTAMP` |

### `user_genre_discovery`

| Column | Type | Notes |
|--------|------|--------|
| `user_id` | `INT UNSIGNED` | PK (same id as `app_user`) |
| `top_genre` | `VARCHAR(128)` | |
| `second_genre` | `VARCHAR(128)` | nullable |
| `third_genre` | `VARCHAR(128)` | nullable |
| `quiz_mode` | `VARCHAR(16)` | `QUICK` or `FULL` |
| `weights_boost` | `VARCHAR(768)` | pipe-separated `genre:weight` pairs |
| `updated_at` | `TIMESTAMP` | maintained by MySQL |

Used by **Find Your Genre** and **`RecommendationEngine`** (`UserGenreDiscoveryDAO`). There is **no FK** to `app_user` in `schema.sql` (see comment in SQL file about type mismatches on legacy DBs).

---

## Tables in your MySQL that are **not** in `schema.sql`

Your Workbench instance may also include **quiz catalog / result** tables, for example:

- `quiz_question` — e.g. `question_id`, `question_text`
- `quiz_answer` — e.g. `answer_id`, `question_id`, `answer_text`, `genre_id`
- `user_quiz_results` — e.g. `user_id`, `question_id`, `answer_id`, `response_time`

The **current Find Your Genre flow** builds the quiz in code (`GenreQuiz` / `FindYourGenrePageController`) and persists the outcome in **`user_genre_discovery`** only. Those extra tables are **not referenced** by the Java DAOs in this repository. If you want one source of truth, either add their DDL to `schema.sql` or treat them as legacy/experimental.

---

## Possible drift from `schema.sql`

If your live database was created or altered separately, you might have columns the repo SQL does **not** define, for example:

- `playlist.created_at`
- `playlist_song.added_at`

The **current DAOs** do not depend on those columns. If we add features that need them, we will document **exact `ALTER TABLE` statements** and update `schema.sql` in the same change.

---

## Schema patches in code

| Class | Purpose |
|--------|---------|
| `DbSchemaPatches` | Idempotent `ALTER TABLE` for `app_user.profile_avatar_key` if missing |

---

## When we change the database

Any feature work that needs DDL will include:

1. **SQL** you can run in Workbench (or migrations, if you adopt them later).  
2. Updates to **`schema.sql`** so new environments stay consistent.  
3. Notes for **`CHANGELOG.md`** / version bumps per [VERSIONING.md](VERSIONING.md).

If something fails locally, compare your table list to this doc and to `schema.sql`, then share a screenshot or `SHOW CREATE TABLE` output.
