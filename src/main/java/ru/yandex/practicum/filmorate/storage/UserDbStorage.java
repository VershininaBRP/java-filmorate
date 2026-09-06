package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(new HashSet<>());
        return user;
    };

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return findById(user.getId()).orElse(user);
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found after update"));
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userMapper, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        User user = users.get(0);
        loadFriends(user);
        return Optional.of(user);
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userMapper);
        for (User user : users) {
            loadFriends(user);
        }
        return users;
    }

    @Override
    public boolean existsById(int id) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)", Boolean.class, id));
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<Integer> friendIds = jdbcTemplate.queryForList(sql, Integer.class, user.getId());
        user.setFriends(new HashSet<>(friendIds));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        jdbcTemplate.update("INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'PENDING')", userId, friendId);
    }

    @Override
    public void confirmFriend(int userId, int friendId) {
        jdbcTemplate.update("UPDATE friends SET status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? AND f.status = 'CONFIRMED'";
        List<User> friends = jdbcTemplate.query(sql, userMapper, userId);
        for (User friend : friends) {
            loadFriends(friend);
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f1 ON u.id = f1.friend_id AND f1.status = 'CONFIRMED' " +
                "JOIN friends f2 ON u.id = f2.friend_id AND f2.status = 'CONFIRMED' " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        List<User> commonFriends = jdbcTemplate.query(sql, userMapper, userId, otherId);
        for (User friend : commonFriends) {
            loadFriends(friend);
        }
        return commonFriends;
    }
}