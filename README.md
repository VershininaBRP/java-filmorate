# Filmorate

## Схема базы данных

<img width="1168" height="698" alt="2026-09-05_00-49-07" src="https://github.com/user-attachments/assets/34cea3b2-c781-44d6-867e-b3d11774f221" />

### Структура БД

База данных состоит из 6 таблиц:

| Таблица | Назначение |
|---------|------------|
| **users** | Хранит информацию о пользователях |
| **films** | Хранит информацию о фильмах |
| **genres** | Справочник жанров |
| **film_genres** | Связь фильмов и жанров (многие-ко-многим) |
| **friendships** | Дружеские связи со статусами |
| **likes** | Лайки пользователей на фильмы |

### Примеры SQL-запросов

#### 1. Получить все фильмы с жанрами
```sql
SELECT 
    f.id,
    f.name,
    f.description,
    f.release_date,
    f.duration,
    f.mpa_rating,
    GROUP_CONCAT(g.name SEPARATOR ', ') AS genres
FROM films f
LEFT JOIN film_genres fg ON f.id = fg.film_id
LEFT JOIN genres g ON fg.genre_id = g.id
GROUP BY f.id;
