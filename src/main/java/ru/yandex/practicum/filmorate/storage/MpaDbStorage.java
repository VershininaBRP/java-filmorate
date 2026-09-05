package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<MpaRating> mpaRowMapper = new MpaRowMapper();

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MpaRating> findAll() {
        return jdbcTemplate.query("SELECT * FROM mpa_ratings ORDER BY id", mpaRowMapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        List<MpaRating> mpaRatings = jdbcTemplate.query("SELECT * FROM mpa_ratings WHERE id = ?", mpaRowMapper, id);
        return mpaRatings.isEmpty() ? Optional.empty() : Optional.of(mpaRatings.get(0));
    }

    private static class MpaRowMapper implements RowMapper<MpaRating> {
        @Override
        public MpaRating mapRow(ResultSet rs, int rowNum) throws SQLException {
            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        }
    }
}