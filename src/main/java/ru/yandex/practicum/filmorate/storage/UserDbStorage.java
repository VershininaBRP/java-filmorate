package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage extends BaseRepository implements UserStorage {
    private final RowMapper<User> userRowMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.userRowMapper = new UserRowMapper();
    }

    @Override
    public User create(User user) {
        int id = insert("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);
        return loadFriends(user);
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update("UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return loadFriends(user);
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public Optional<User> findById(int id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", userRowMapper, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(loadFriends(users.get(0)));
    }

    @Override
    public List<User> findAll() {
        List<User> users = jdbcTemplate.query("SELECT * FROM users", userRowMapper);
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public boolean existsById(int id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    private User loadFriends(User user) {
        List<Integer> friends = jdbcTemplate.queryForList(
                "SELECT friend_id FROM friendships WHERE user_id = ? AND status = 'ACCEPTED'",
                Integer.class, user.getId());
        user.setFriends(new java.util.HashSet<>(friends));
        return user;
    }
}