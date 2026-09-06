package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        return film;
    };

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        Integer mpaId = film.getMpaRating() != null ? film.getMpaRating().getId() : null;
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, mpaId);
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        updateGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        Integer mpaId = film.getMpaRating() != null ? film.getMpaRating().getId() : null;
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), mpaId, film.getId());
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        updateGenres(film);
        return film;
    }

    private void updateGenres(Film film) {
        if (film.getGenres() == null) return;
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), genre.getId());
        }
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Optional<Film> findById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.get(0);
        loadMpa(film);
        loadGenres(film);
        loadLikes(film);
        return Optional.of(film);
    }

    private void loadMpa(Film film) {
        String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";
        List<MpaRating> mpaList = jdbcTemplate.query(sql, (rs, rowNum) ->
                        new MpaRating(rs.getInt("id"), rs.getString("name")),
                jdbcTemplate.queryForObject("SELECT mpa_rating_id FROM films WHERE id = ?", Integer.class, film.getId()));
        if (!mpaList.isEmpty()) {
            film.setMpaRating(mpaList.get(0));
        }
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.id, g.name FROM film_genres fg JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id = ? ORDER BY g.id";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), film.getId());
        film.setGenres(new HashSet<>(genres));
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sql, Integer.class, film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmMapper);
        for (Film film : films) {
            loadMpa(film);
            loadGenres(film);
            loadLikes(film);
        }
        return films;
    }

    @Override
    public boolean existsById(int id) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM films WHERE id = ?)", Boolean.class, id));
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }
}