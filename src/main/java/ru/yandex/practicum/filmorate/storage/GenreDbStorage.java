package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate; // Теперь инициализируется через конструктор

    private final RowMapper<Genre> genreMapper = (rs, rowNum) ->
            new Genre(rs.getInt("id"), rs.getString("name"));

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreMapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, genreMapper, id);
        return genres.isEmpty() ? Optional.empty() : Optional.of(genres.get(0));
    }
}