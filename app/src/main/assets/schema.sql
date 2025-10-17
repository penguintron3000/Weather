
--user
CREATE TABLE user (
  user_id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  is_locked INTEGER DEFAULT 0,
  locked_at INTEGER,
  locked_until INTEGER,
  theme_json TEXT
);

--city
CREATE TABLE city (
  city_id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  display_name TEXT NOT NULL,
  country_code TEXT,
  lat REAL,
  lon REAL,
  FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE
);
