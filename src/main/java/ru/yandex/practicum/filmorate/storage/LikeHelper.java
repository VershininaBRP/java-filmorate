package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class LikeHelper {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public LikeHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = new FilmRowMapper();
    }

    public void addLike(int filmId, int userId) {
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    public List<Film> findPopular(int count) {
        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, COUNT(fl.user_id) as like_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        films.forEach(this::loadLikesAndGenres);
        return films;
    }

    private void loadLikesAndGenres(Film film) {
        List<Integer> likes = jdbcTemplate.queryForList("SELECT user_id FROM film_likes WHERE film_id = ?", Integer.class, film.getId());
        film.setLikes(new java.util.HashSet<>(likes));

        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(new java.util.LinkedHashSet<>(genres));
    }

    private static class FilmRowMapper implements org.springframework.jdbc.core.RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date") != null ? rs.getDate("release_date").toLocalDate() : null);
            film.setDuration(rs.getInt("duration"));

            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpaRating(mpa);

            return film;
        }
    }
}