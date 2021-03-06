CREATE TABLE movies (
id INTEGER PRIMARY KEY AUTOINCREMENT,
title TEXT NOT NULL UNIQUE,
title_hu TEXT NOT NULL UNIQUE,
storage_a TEXT NOT NULL,
storage_b TEXT NOT NULL,
source TEXT NOT NULL);

CREATE TABLE mediainfo (
title TEXT PRIMARY KEY,
size TEXT NOT NULL,
runtime TEXT NOT NULL,
audio_lng TEXT NOT NULL,
subtitle_lng TEXT NOT_NULL,
resolution TEXT NOT NULL,
aspect_ratio TEXT NOT NULL,
framerate TEXT NOT NULL,
FOREIGN KEY(title) REFERENCES movie(title)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

