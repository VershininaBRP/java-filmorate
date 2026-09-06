package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage implements MpaRatingStorage {
    private final JdbcTemplate jdbcTemplate; // Теперь инициализируется через конструктор

    private final RowMapper<MpaRating> mpaMapper = (rs, rowNum) ->
            new MpaRating(rs.getInt("id"), rs.getString("name"));

    @Override
    public List<MpaRating> findAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mpaMapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(sql, mpaMapper, id);
        return ratings.isEmpty() ? Optional.empty() : Optional.of(ratings.get(0));
    }
}